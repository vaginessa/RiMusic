package it.vfsfitvnm.vimusic.service


import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.DatabaseProvider
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.datasource.ResolvingDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import it.vfsfitvnm.innertube.Innertube
import it.vfsfitvnm.innertube.models.bodies.PlayerBody
import it.vfsfitvnm.innertube.requests.player
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.models.Format
import it.vfsfitvnm.vimusic.query
import it.vfsfitvnm.vimusic.utils.RingBuffer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.io.File
import java.util.concurrent.Executors
@UnstableApi
object DownloadUtil {
    const val DOWNLOAD_NOTIFICATION_CHANNEL_ID = "download_channel"
    private const val TAG = "DownloadUtil"
    private const val DOWNLOAD_CONTENT_DIRECTORY = "downloads"

    private lateinit var databaseProvider: DatabaseProvider
    private lateinit var downloadCache: Cache
    private lateinit var dataSourceFactory: DataSource.Factory
    private lateinit var httpDataSourceFactory: HttpDataSource.Factory
    private lateinit var ResolvingDataSourceFactory: ResolvingDataSource.Factory
    private lateinit var downloadNotificationHelper: DownloadNotificationHelper
    private lateinit var downloadDirectory: File
    private lateinit var downloadManager: DownloadManager



    var downloads = MutableStateFlow<Map<String, Download>>(emptyMap())

    fun getDownload(songId: String): Flow<Download?> {
        return downloads.map { it[songId] }

    }

    @SuppressLint("LongLogTag")
    @Synchronized
    fun getDownloads() {
        val result = mutableMapOf<String, Download>()
        val cursor = downloadManager.downloadIndex.getDownloads()
        while (cursor.moveToNext()) {
            result[cursor.download.request.id] = cursor.download
        }
        downloads.value = result

    }




    @SuppressLint("SuspiciousIndentation")
    @Synchronized
    fun getResolvingDataSourceFactory (context: Context): ResolvingDataSource.Factory {
        val cache = getDownloadCache(context)
        val dataSourceFactory = ResolvingDataSource.Factory(createCacheDataSource(context)) { dataSpec ->
            val videoId = dataSpec.key ?: error("A key must be set")
            //val chunkLength = 1024 * 1024L
            val chunkLength = 10000 * 1024L
            val ringBuffer = RingBuffer<Pair<String, Uri>?>(2) { null }

            if (cache.isCached(videoId, dataSpec.position, chunkLength)) {
                dataSpec
            } else {
                when (videoId) {
                    ringBuffer.getOrNull(0)?.first -> dataSpec.withUri(ringBuffer.getOrNull(0)!!.second)
                    ringBuffer.getOrNull(1)?.first -> dataSpec.withUri(ringBuffer.getOrNull(1)!!.second)
                    "initVideoId" -> dataSpec
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

                                    query {
                                        if (Database.songExist(videoId) == 1)
                                        Database.insert(
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
        return dataSourceFactory
    }

/*
    @Synchronized
    fun getHttpDataSourceFactory(context: Context): HttpDataSource.Factory {
        if(!DownloadUtil::httpDataSourceFactory.isInitialized) {
            httpDataSourceFactory = CronetDataSource.Factory(
                CronetEngine.Builder(context).build(),
                Executors.newSingleThreadExecutor()
            )
        }
        return httpDataSourceFactory
    }

    @Synchronized
    fun getReadOnlyDataSourceFactory(context: Context): DataSource.Factory {
        if(!DownloadUtil::dataSourceFactory.isInitialized) {
            val contextApplication = context.applicationContext
            val upstreamFactory = DefaultDataSource.Factory(
                contextApplication,
                getHttpDataSourceFactory(contextApplication)
            )
            dataSourceFactory = CacheDataSource.Factory()
                .setCache(getDownloadCache(contextApplication))
                .setUpstreamDataSourceFactory(upstreamFactory)
                .setCacheWriteDataSinkFactory(null)
                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        }
        return dataSourceFactory
    }

*/
    private fun createCacheDataSource(context: Context): DataSource.Factory {
        return CacheDataSource.Factory().setCache(getDownloadCache(context)).apply {
            setUpstreamDataSourceFactory(
                DefaultHttpDataSource.Factory()
                    .setConnectTimeoutMs(16000)
                    .setReadTimeoutMs(8000)
                    .setUserAgent("Mozilla/5.0 (Windows NT 10.0; rv:91.0) Gecko/20100101 Firefox/91.0")
            )
            setCacheWriteDataSinkFactory(null)
        }
    }


    @Synchronized
    fun getDownloadNotificationHelper(context: Context?): DownloadNotificationHelper {
        if(!DownloadUtil::downloadNotificationHelper.isInitialized) {
            downloadNotificationHelper =
                DownloadNotificationHelper(context!!, DOWNLOAD_NOTIFICATION_CHANNEL_ID)
        }
        return downloadNotificationHelper
    }

    @Synchronized
    fun getDownloadManager(context: Context): DownloadManager {
        ensureDownloadManagerInitialized(context)
        return downloadManager
    }

/*
    @Synchronized
    fun getDownloadTracker(context: Context): DownloadTracker {
        ensureDownloadManagerInitialized(context)
        return downloadTracker
    }

 */



    fun getDownloadString(context: Context, @Download.State downloadState: Int): String {
        return when (downloadState) {
            /*
            Download.STATE_COMPLETED -> context.resources.getString(R.string.exo_download_completed)
            Download.STATE_DOWNLOADING -> context.resources.getString(R.string.exo_download_downloading)
            Download.STATE_FAILED -> context.resources.getString(R.string.exo_download_failed)
            Download.STATE_QUEUED -> context.resources.getString(R.string.exo_download_queued)
            Download.STATE_REMOVING -> context.resources.getString(R.string.exo_download_removing)
            Download.STATE_RESTARTING -> context.resources.getString(R.string.exo_download_restarting)
            Download.STATE_STOPPED -> context.resources.getString(R.string.exo_download_stopped)
            else -> throw IllegalArgumentException()
             */
            Download.STATE_COMPLETED -> "Completed"
            Download.STATE_DOWNLOADING -> "Downloading"
            Download.STATE_FAILED -> "Failed"
            Download.STATE_QUEUED -> "Queued"
            Download.STATE_REMOVING -> "Removing"
            Download.STATE_RESTARTING -> "Restarting"
            Download.STATE_STOPPED -> "Stopped"
            else -> throw IllegalArgumentException()

        }
    }

    @Synchronized
    private fun getDownloadCache(context: Context): Cache {
        if(!DownloadUtil::downloadCache.isInitialized) {
            val downloadContentDirectory =
                File(getDownloadDirectory(context), DOWNLOAD_CONTENT_DIRECTORY)
            downloadCache = SimpleCache(
                downloadContentDirectory,
                NoOpCacheEvictor(),
                getDatabaseProvider(context)
            )
        }
        return downloadCache
    }

    @Synchronized
    fun getDownloadSimpleCache(context: Context): Cache {
        if(!DownloadUtil::downloadCache.isInitialized) {
            val downloadContentDirectory =
                File(getDownloadDirectory(context), DOWNLOAD_CONTENT_DIRECTORY)
            downloadCache = SimpleCache(
                downloadContentDirectory,
                NoOpCacheEvictor(),
                getDatabaseProvider(context)
            )
        }
        return downloadCache
    }

    @Synchronized
    private fun ensureDownloadManagerInitialized(context: Context) {
        if(!DownloadUtil::downloadManager.isInitialized) {
            downloadManager = DownloadManager(
                context,
                getDatabaseProvider(context),
                getDownloadCache(context),
                //getHttpDataSourceFactory(context),
                //getReadOnlyDataSourceFactory(context),
                getResolvingDataSourceFactory(context),
                Executors.newFixedThreadPool(6)
            ).apply {
                maxParallelDownloads = 2
            }

            //downloadTracker =
            //    DownloadTracker(context, getHttpDataSourceFactory(context), downloadManager)
        }
    }

    @Synchronized
    private fun getDatabaseProvider(context: Context): DatabaseProvider {
        if(!DownloadUtil::databaseProvider.isInitialized) databaseProvider =
            StandaloneDatabaseProvider(context)
        return databaseProvider
    }

    @Synchronized
    fun getDownloadDirectory(context: Context): File {
        if(!DownloadUtil::downloadDirectory.isInitialized) {
            downloadDirectory = context.getExternalFilesDir(null) ?: context.filesDir
            downloadDirectory.resolve(DOWNLOAD_CONTENT_DIRECTORY).also { directory ->
                if (directory.exists()) return@also
                directory.mkdir()
            }
            //Log.d("downloadMedia", downloadDirectory.path)
        }
        return downloadDirectory
    }


}
