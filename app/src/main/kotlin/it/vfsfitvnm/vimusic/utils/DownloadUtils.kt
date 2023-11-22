package it.vfsfitvnm.vimusic.utils


import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.platform.LocalContext

import androidx.core.net.toUri
import androidx.media3.common.util.Consumer
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import it.vfsfitvnm.innertube.models.Context

import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalDownloader
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.models.Format
import it.vfsfitvnm.vimusic.service.MyDownloadService
import it.vfsfitvnm.vimusic.service.PlayerService
import it.vfsfitvnm.vimusic.service.VideoIdMismatchException
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext

@UnstableApi
@Composable
fun InitDownloader () {
    val context = LocalContext.current

    //val idVideo="initVideoId"
    val idVideo="8n4S1-ctsZw"
    //val contentUri = "https://$idVideo".toUri()
    val contentUri = "https://www.youtube.com/watch?v=8n4S1-ctsZw".toUri()

    //val idVideo="8n4S1-ctsZw"
    //val contentUri = "https://www.youtube.com/watch?v=$idVideo".toUri()

    val downloadRequest = DownloadRequest
        .Builder(
            idVideo,
            contentUri
        )
        .setCustomCacheKey(idVideo)
        .setData(idVideo.toByteArray())
        .build()


    runCatching {
            DownloadService.sendAddDownload(
                context,
                MyDownloadService::class.java,
                downloadRequest,
                false
            )
        }.onFailure {
        Log.d("downloadInit","Downloader initialized $it" )
    }

}


@UnstableApi
@Composable
fun downloadedStateMedia ( mediaId: String ): Boolean {

    val binder = LocalPlayerServiceBinder.current
    val downloader = LocalDownloader.current

    var cachedBytes by remember(mediaId) {
        mutableStateOf(binder?.cache?.getCachedBytes(mediaId, 0, -1))
    }

    var format by remember {
        mutableStateOf<Format?>(null)
    }

    var isDownloaded by remember {
        mutableStateOf(false)
    }

    val download by downloader.getDownload(mediaId).collectAsState(initial = null)

    LaunchedEffect(mediaId) {
        Database.format(mediaId).distinctUntilChanged().collectLatest { currentFormat ->
            format = currentFormat
        }
    }

    isDownloaded = (format?.contentLength == cachedBytes) || (download?.state == Download.STATE_COMPLETED)

//    Log.d("mediaItem", "cachedBytes ${cachedBytes} contentLength ${format?.contentLength} downloadState ${isDownloaded}")

    return isDownloaded

}

@UnstableApi
fun manageDownload (
    context: android.content.Context,
    songId: String,
    songTitle: String,
    downloadState: Boolean = false
) {
    if (downloadState)
        DownloadService.sendRemoveDownload(
            context,
            MyDownloadService::class.java,
            songId,
            false
        )
     else {
        val contentUri =
            "https://www.youtube.com/watch?v=${songId}".toUri()
        val downloadRequest = DownloadRequest
            .Builder(
                songId,
                contentUri
            )
            .setCustomCacheKey(songId)
            .setData(songTitle.toByteArray())
            .build()

        DownloadService.sendAddDownload(
            context,
            MyDownloadService::class.java,
            downloadRequest,
            false
        )
    }

}
@UnstableApi
@Composable
fun getDownloadState(mediaId: String): Int {
    val downloader = LocalDownloader.current
    return downloader.getDownload(mediaId).collectAsState(initial = null).value?.state
        ?: 3
}

