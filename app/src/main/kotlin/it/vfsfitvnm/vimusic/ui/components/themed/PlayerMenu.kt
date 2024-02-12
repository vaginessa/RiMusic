package it.vfsfitvnm.vimusic.ui.components.themed

import android.content.ActivityNotFoundException
import android.content.Intent
import android.media.audiofx.AudioEffect
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import it.vfsfitvnm.innertube.models.NavigationEndpoint
import it.vfsfitvnm.vimusic.service.PlayerService
import it.vfsfitvnm.vimusic.utils.seamlessPlay
import it.vfsfitvnm.vimusic.utils.toast

@ExperimentalTextApi
@ExperimentalAnimationApi
@UnstableApi
@Composable
fun PlayerMenu(
    binder: PlayerService.Binder,
    mediaItem: MediaItem,
    onDismiss: () -> Unit,

    ) {
    val context = LocalContext.current

    val activityResultLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { }

    BaseMediaItemMenu(
        mediaItem = mediaItem,
        onStartRadio = {
            binder.stopRadio()
            binder.player.seamlessPlay(mediaItem)
            binder.setupRadio(NavigationEndpoint.Endpoint.Watch(videoId = mediaItem.mediaId))
        },
        onGoToEqualizer = {
            try {
                activityResultLauncher.launch(
                    Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
                        putExtra(AudioEffect.EXTRA_AUDIO_SESSION, binder.player.audioSessionId)
                        putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
                        putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                    }
                )
            } catch (e: ActivityNotFoundException) {
                context.toast("Couldn't find an application to equalize audio")
            }
        },
        onShowSleepTimer = {},
        onDismiss = onDismiss
    )
}