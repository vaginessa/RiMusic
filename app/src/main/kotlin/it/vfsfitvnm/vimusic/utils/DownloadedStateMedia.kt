package it.vfsfitvnm.vimusic.utils

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.media3.common.util.UnstableApi
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.models.Format
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.math.roundToInt

@UnstableApi
@Composable
fun downloadedStateMedia ( mediaId: String ): Boolean {

    val binder = LocalPlayerServiceBinder.current

    var cachedBytes by remember(mediaId) {
        mutableStateOf(binder?.cache?.getCachedBytes(mediaId, 0, -1))
    }

    var format by remember {
        mutableStateOf<Format?>(null)
    }

    var isDownloaded by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(mediaId) {
        Database.format(mediaId).distinctUntilChanged().collectLatest { currentFormat ->
            format = currentFormat
        }
    }

    isDownloaded = format?.contentLength == cachedBytes

//    Log.d("mediaItem", "cachedBytes ${cachedBytes} contentLength ${format?.contentLength} downloadState ${isDownloaded}")

    return isDownloaded

}