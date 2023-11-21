package it.vfsfitvnm.vimusic.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalDownloader
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.models.Format
import it.vfsfitvnm.vimusic.service.MyDownloadService
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged

@UnstableApi
@Composable
fun InitDownloader () {
    val context = LocalContext.current
    val contentUri =
        "fakeId".toUri()
    //"https://www.youtube.com/watch?v=fakeId".toUri()
    val downloadRequest = DownloadRequest
        .Builder(
            "fakeId",
            contentUri
        )
        .setCustomCacheKey("fakeId")
        .setData("fakeId".toByteArray())
        .build()

    DownloadService.sendAddDownload(
        context,
        MyDownloadService::class.java,
        downloadRequest,
        false
    )
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