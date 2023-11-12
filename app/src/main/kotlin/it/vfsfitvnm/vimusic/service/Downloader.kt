package it.vfsfitvnm.vimusic.service

import android.content.Context
import android.net.Uri

import androidx.core.net.toUri
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player

import androidx.media3.common.util.UnstableApi
import androidx.media3.database.DatabaseProvider

import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.ResolvingDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.engine.callContext

import it.vfsfitvnm.innertube.Innertube
import it.vfsfitvnm.innertube.models.bodies.PlayerBody
import it.vfsfitvnm.innertube.requests.player
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder

import it.vfsfitvnm.vimusic.models.Format
import it.vfsfitvnm.vimusic.query
import it.vfsfitvnm.vimusic.utils.RingBuffer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Singleton

@UnstableApi
@Singleton
class Downloader @Inject constructor(
    context: Context,
    player: PlayerService
) {



    //private val connectivityManager = context.getSystemService<ConnectivityManager>()!!
    //private val audioQuality by enumPreference(context, AudioQualityKey, AudioQuality.AUTO)
    //private val songUrlCache = HashMap<String, Pair<String, Long>>()
    val binder = LocalPlayerServiceBinder
    val database = Database
    //val context = callContext()

    var cacheDirName = "rimusic_cache"
    val directory = player.cacheDir.resolve(cacheDirName)
    val cacheEvictor = NoOpCacheEvictor()

    var cache = SimpleCache(directory, cacheEvictor, StandaloneDatabaseProvider(context))


    private val dataSourceFactory = ResolvingDataSource.Factory(createCacheDataSource()) { dataSpec ->
        val videoId = dataSpec.key ?: error("A key must be set")
        val chunkLength = 512 * 1024L
        val ringBuffer = RingBuffer<Pair<String, Uri>?>(2) { null }

        if (cache.isCached(videoId, dataSpec.position, chunkLength)) {
            dataSpec
        } else {
            when (videoId) {
                ringBuffer.getOrNull(0)?.first -> dataSpec.withUri(ringBuffer.getOrNull(0)!!.second)
                ringBuffer.getOrNull(1)?.first -> dataSpec.withUri(ringBuffer.getOrNull(1)!!.second)
                else -> {
                    val urlResult = runBlocking(Dispatchers.IO) {
                        Innertube.player(PlayerBody(videoId = videoId))
                    }?.mapCatching { body ->
                        if (body.videoDetails?.videoId != videoId) {
                            throw VideoIdMismatchException()
                        }

                        when (val status = body.playabilityStatus?.status) {
                            "OK" -> body.streamingData?.highestQualityFormat?.let { format ->
                                val mediaItem = runBlocking(Dispatchers.Main) {
                                    Innertube.player(PlayerBody(videoId = videoId))
                                }
                            /*
                                if (mediaItem?.mediaMetadata?.extras?.getString("durationText") == null) {
                                    format.approxDurationMs?.div(1000)
                                        ?.let(DateUtils::formatElapsedTime)?.removePrefix("0")
                                        ?.let { durationText ->
                                            mediaItem?.mediaMetadata?.extras?.putString(
                                                "durationText",
                                                durationText
                                            )
                                            Database.updateDurationText(videoId, durationText)
                                        }
                                }

                             */

                                query {
                                    //mediaItem?.let(Database::insert)

                                    database.insert(
                                        Format(
                                            songId = videoId,
                                            itag = format.itag,
                                            mimeType = format.mimeType,
                                            bitrate = format.bitrate,
                                            loudnessDb = body.playerConfig?.audioConfig?.normalizedLoudnessDb,
                                            contentLength = format.contentLength,
                                            lastModified = format.lastModified
                                        )
                                    )
                                }



                                format.url
                            } ?: throw PlayableFormatNotFoundException()

                            "UNPLAYABLE" -> throw UnplayableException()
                            "LOGIN_REQUIRED" -> throw LoginRequiredException()
                            else -> throw PlaybackException(
                                status,
                                null,
                                PlaybackException.ERROR_CODE_REMOTE_ERROR
                            )
                        }
                    }

                    urlResult?.getOrThrow()?.let { url ->
                        ringBuffer.append(videoId to url.toUri())
                        dataSpec.withUri(url.toUri())
                            .subrange(dataSpec.uriPositionOffset, chunkLength)
                    } ?: throw PlaybackException(
                        null,
                        urlResult?.exceptionOrNull(),
                        PlaybackException.ERROR_CODE_REMOTE_ERROR
                    )
                }
            }
        }


    }



    private fun createCacheDataSource(): DataSource.Factory {
        return CacheDataSource.Factory().setCache(cache).apply {
            setUpstreamDataSourceFactory(
                DefaultHttpDataSource.Factory()
                    .setConnectTimeoutMs(16000)
                    .setReadTimeoutMs(8000)
                    .setUserAgent("Mozilla/5.0 (Windows NT 10.0; rv:91.0) Gecko/20100101 Firefox/91.0")
            )
            setCacheWriteDataSinkFactory(null)
        }
    }

    val downloadNotificationHelper = DownloadNotificationHelper(context, DownloaderService.CHANNEL_ID)


    val downloadManager: DownloadManager =
        DownloadManager(context, StandaloneDatabaseProvider(context), cache, dataSourceFactory, Executor(Runnable::run)).apply {
        maxParallelDownloads = 3

        addListener(
            DownloaderService.TerminalStateNotificationHelper(
                context = context,
                notificationHelper = downloadNotificationHelper,
                nextNotificationId = DownloaderService.NOTIFICATION_ID + 1
            )
        )
    }

    val downloads = MutableStateFlow<Map<String, Download>>(emptyMap())

    fun getDownload(songId: String): Flow<Download?> = downloads.map { it[songId] }


/*
    init {

        val result = mutableMapOf<String, Download>()
        val cursor = downloadManager.downloadIndex.getDownloads()
        while (cursor.moveToNext()) {
            result[cursor.download.request.id] = cursor.download
        }
        downloads.value = result
        downloadManager.addListener(
            object : DownloadManager.Listener {
                override fun onDownloadChanged(downloadManager: DownloadManager, download: Download, finalException: Exception?) {
                    downloads.update { map ->
                        map.toMutableMap().apply {
                            set(download.request.id, download)
                        }
                    }
                }
            }
        )
    }

 */

}