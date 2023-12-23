package it.vfsfitvnm.vimusic.ui.screens.player

import android.annotation.SuppressLint
import android.text.format.Formatter
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheSpan
import it.vfsfitvnm.innertube.Innertube
import it.vfsfitvnm.innertube.models.bodies.PlayerBody
import it.vfsfitvnm.innertube.requests.player
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.AudioQualityFormat
import it.vfsfitvnm.vimusic.models.Format
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.onOverlay
import it.vfsfitvnm.vimusic.ui.styling.overlay
import it.vfsfitvnm.vimusic.utils.audioQualityFormatKey
import it.vfsfitvnm.vimusic.utils.color
import it.vfsfitvnm.vimusic.utils.medium
import it.vfsfitvnm.vimusic.utils.rememberPreference
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext
@SuppressLint("LongLogTag")
@UnstableApi
@Composable
fun StatsForNerds(
    mediaId: String,
    isDisplayed: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (colorPalette, typography) = LocalAppearance.current
    val context = LocalContext.current
    val binder = LocalPlayerServiceBinder.current ?: return

    val audioQualityFormat by rememberPreference(audioQualityFormatKey, AudioQualityFormat.High)

    AnimatedVisibility(
        visible = isDisplayed,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        var cachedBytes by remember(mediaId) {
            mutableStateOf(binder.cache.getCachedBytes(mediaId, 0, -1))
        }

        var downloadCachedBytes by remember(mediaId) {
            mutableStateOf(binder.downloadCache.getCachedBytes(mediaId, 0, -1))
        }

        var format by remember {
            mutableStateOf<Format?>(null)
        }

        LaunchedEffect(mediaId) {
            Database.format(mediaId).distinctUntilChanged().collectLatest { currentFormat ->
                if (currentFormat?.itag == null) {
                    binder.player.currentMediaItem?.takeIf { it.mediaId == mediaId }?.let { mediaItem ->
                        withContext(Dispatchers.IO) {
                            delay(2000)
                            Innertube.player(PlayerBody(videoId = mediaId))?.onSuccess { response ->
                                //response.streamingData?.highestQualityFormat?.let { format ->
                                when(audioQualityFormat) {
                                    AudioQualityFormat.High -> response.streamingData?.highestQualityFormat
                                    AudioQualityFormat.Medium -> response.streamingData?.mediumQualityFormat
                                    AudioQualityFormat.Low -> response.streamingData?.lowestQualityFormat
                                }?.let { format ->
                                    Database.insert(mediaItem)
                                    Database.insert(
                                        Format(
                                            songId = mediaId,
                                            itag = format.itag,
                                            mimeType = format.mimeType,
                                            bitrate = format.bitrate,
                                            loudnessDb = response.playerConfig?.audioConfig?.normalizedLoudnessDb,
                                            contentLength = format.contentLength,
                                            lastModified = format.lastModified
                                        )
                                    )
                                }
                            }
                        }
                    }
                } else {
                    format = currentFormat
                }
            }
        }

        DisposableEffect(mediaId) {
            val listener = object : Cache.Listener {
                override fun onSpanAdded(cache: Cache, span: CacheSpan) {
                    cachedBytes += span.length
                }

                override fun onSpanRemoved(cache: Cache, span: CacheSpan) {
                    cachedBytes -= span.length
                }

                override fun onSpanTouched(cache: Cache, oldSpan: CacheSpan, newSpan: CacheSpan) =
                    Unit
            }

            binder.cache.addListener(mediaId, listener)

            onDispose {
                binder.cache.removeListener(mediaId, listener)
            }
        }

        Box(
            modifier = modifier
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            onDismiss()
                        }
                    )
                }
                .background(colorPalette.overlay)
                .fillMaxSize()
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(all = 16.dp)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    BasicText(
                        text = stringResource(R.string.id),
                        style = typography.xs.medium.color(colorPalette.onOverlay)
                    )
                    BasicText(
                        text = stringResource(R.string.itag),
                        style = typography.xs.medium.color(colorPalette.onOverlay)
                    )
                    BasicText(
                        text = "Quality",
                        style = typography.xs.medium.color(colorPalette.onOverlay)
                    )
                    BasicText(
                        text = stringResource(R.string.bitrate),
                        style = typography.xs.medium.color(colorPalette.onOverlay)
                    )
                    BasicText(
                        text = stringResource(R.string.size),
                        style = typography.xs.medium.color(colorPalette.onOverlay)
                    )

                    BasicText(
                        text = if (cachedBytes > downloadCachedBytes) stringResource(R.string.cached)
                        else stringResource( R.string.downloaded ),
                        style = typography.xs.medium.color(colorPalette.onOverlay)
                    )

                    BasicText(
                        text = stringResource(R.string.loudness),
                        style = typography.xs.medium.color(colorPalette.onOverlay)
                    )
                }

                Column {
                    BasicText(
                        text = mediaId,
                        maxLines = 1,
                        style = typography.xs.medium.color(colorPalette.onOverlay)
                    )
                    BasicText(
                        text = format?.itag?.toString() ?: "Unknown",
                        maxLines = 1,
                        style = typography.xs.medium.color(colorPalette.onOverlay)
                    )
                    BasicText(
                        text = when (format?.itag?.toString()) {
                            "251" -> stringResource(R.string.audio_quality_format_high)
                            "141" -> stringResource(R.string.audio_quality_format_high)
                            "250" -> stringResource(R.string.audio_quality_format_medium)
                            "140" -> stringResource(R.string.audio_quality_format_medium)
                            "249" -> stringResource(R.string.audio_quality_format_low)
                            "139" -> stringResource(R.string.audio_quality_format_low)
                            else -> stringResource(R.string.audio_quality_format_unknown)
                        },
                        maxLines = 1,
                        style = typography.xs.medium.color(colorPalette.onOverlay)
                    )
                    BasicText(
                        text = format?.bitrate?.let { "${it / 1000} kbps" } ?: "Unknown",
                        maxLines = 1,
                        style = typography.xs.medium.color(colorPalette.onOverlay)
                    )
                    BasicText(
                        text = format?.contentLength
                            ?.let { Formatter.formatShortFileSize(context, it) } ?: "Unknown",
                        maxLines = 1,
                        style = typography.xs.medium.color(colorPalette.onOverlay)
                    )
                    BasicText(
                        text = buildString {
                            if (cachedBytes > downloadCachedBytes)
                            append(Formatter.formatShortFileSize(context, cachedBytes))
                            else append(Formatter.formatShortFileSize(context, downloadCachedBytes))

                            format?.contentLength?.let {
                                if (cachedBytes > downloadCachedBytes)
                                append(" (${(cachedBytes.toFloat() / it * 100).roundToInt()}%)")
                                else append(" (${(downloadCachedBytes.toFloat() / it * 100).roundToInt()}%)")
                            }
                        },
                        maxLines = 1,
                        style = typography.xs.medium.color(colorPalette.onOverlay)
                    )
                    BasicText(
                        text = format?.loudnessDb?.let { "%.2f dB".format(it) } ?: "Unknown",
                        maxLines = 1,
                        style = typography.xs.medium.color(colorPalette.onOverlay)
                    )
                }
            }
        }
    }
}
