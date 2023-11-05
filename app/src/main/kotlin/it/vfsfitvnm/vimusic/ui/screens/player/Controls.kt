package it.vfsfitvnm.vimusic.ui.screens.player

import android.content.ActivityNotFoundException
import android.content.Intent
import android.media.audiofx.AudioEffect
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import it.vfsfitvnm.compose.reordering.rememberReorderingState
import it.vfsfitvnm.innertube.models.NavigationEndpoint
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.models.Format
import it.vfsfitvnm.vimusic.models.Song
import it.vfsfitvnm.vimusic.models.SongWithContentLength
import it.vfsfitvnm.vimusic.query
import it.vfsfitvnm.vimusic.service.DownloaderService
import it.vfsfitvnm.vimusic.service.PlayerService
import it.vfsfitvnm.vimusic.ui.components.LocalMenuState
import it.vfsfitvnm.vimusic.ui.components.SeekBar
import it.vfsfitvnm.vimusic.ui.components.themed.BaseMediaItemMenu
import it.vfsfitvnm.vimusic.ui.components.themed.IconButton
import it.vfsfitvnm.vimusic.ui.components.themed.ScrollText
import it.vfsfitvnm.vimusic.ui.screens.albumRoute
import it.vfsfitvnm.vimusic.ui.screens.artistRoute
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.collapsedPlayerProgressBar
import it.vfsfitvnm.vimusic.ui.styling.favoritesIcon
import it.vfsfitvnm.vimusic.utils.asMediaItem
import it.vfsfitvnm.vimusic.utils.bold
import it.vfsfitvnm.vimusic.utils.downloadedStateMedia
import it.vfsfitvnm.vimusic.utils.effectRotationKey
import it.vfsfitvnm.vimusic.utils.forcePlayAtIndex
import it.vfsfitvnm.vimusic.utils.forceSeekToNext
import it.vfsfitvnm.vimusic.utils.forceSeekToPrevious
import it.vfsfitvnm.vimusic.utils.formatAsDuration
import it.vfsfitvnm.vimusic.utils.isLandscape
import it.vfsfitvnm.vimusic.utils.rememberPreference
import it.vfsfitvnm.vimusic.utils.seamlessPlay
import it.vfsfitvnm.vimusic.utils.secondary
import it.vfsfitvnm.vimusic.utils.semiBold
import it.vfsfitvnm.vimusic.utils.shuffleQueue
import it.vfsfitvnm.vimusic.utils.smoothScrollToTop
import it.vfsfitvnm.vimusic.utils.toast
import it.vfsfitvnm.vimusic.utils.trackLoopEnabledKey
import it.vfsfitvnm.vimusic.utils.windows
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


@ExperimentalFoundationApi
@ExperimentalAnimationApi
@UnstableApi
@Composable
fun Controls(
    mediaId: String,
    title: String?,
    artist: String?,
    artistIds: ArrayList<String>?,
    albumId: String?,
    shouldBePlaying: Boolean,
    position: Long,
    duration: Long,
    modifier: Modifier = Modifier
) {
    val (colorPalette, typography) = LocalAppearance.current

    val binder = LocalPlayerServiceBinder.current
    binder?.player ?: return

    val downloadbinder = DownloaderService()

    var trackLoopEnabled by rememberPreference(trackLoopEnabledKey, defaultValue = false)

    var scrubbingPosition by remember(mediaId) {
        mutableStateOf<Long?>(null)
    }

    val onGoToArtist = artistRoute::global
    val onGoToAlbum = albumRoute::global


    var likedAt by rememberSaveable {
        mutableStateOf<Long?>(null)
    }

    var nextmediaItemIndex = binder.player.nextMediaItemIndex ?: -1
    var nextmediaItemtitle = ""


    if (nextmediaItemIndex.toShort() > -1)
        nextmediaItemtitle = binder.player.getMediaItemAt(nextmediaItemIndex).mediaMetadata.title.toString()

    var isRotated by rememberSaveable { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (isRotated) 360F else 0f,
        animationSpec = tween(durationMillis = 200)
    )
    var effectRotationEnabled by rememberPreference(effectRotationKey, true)


    LaunchedEffect(mediaId) {
        Database.likedAt(mediaId).distinctUntilChanged().collect { likedAt = it }
    }

    var isDownloaded by rememberSaveable {
        mutableStateOf<Boolean>(false)
    }

    isDownloaded = downloadedStateMedia(mediaId)

    val menuState = LocalMenuState.current

    /*
        var cachedBytes by remember(mediaId) {
            mutableStateOf(binder.cache.getCachedBytes(mediaId, 0, -1))
        }

        var format by remember {
            mutableStateOf<Format?>(null)
        }
        var isCached by rememberSaveable { mutableStateOf(false) }



        LaunchedEffect(mediaId) {
            Database.format(mediaId).distinctUntilChanged().collectLatest { currentFormat ->
                format = currentFormat
            }
        }

        format?.contentLength?.let {
            isCached = (cachedBytes.toFloat() / it * 100).roundToInt() == 100

        }

        //Log.d("mediaItem", "Song downloaded? ${isCached} Song ${format?.contentLength}")
    */

    val shouldBePlayingTransition = updateTransition(shouldBePlaying, label = "shouldBePlaying")

    val playPauseRoundness by shouldBePlayingTransition.animateDp(
        transitionSpec = { tween(durationMillis = 100, easing = LinearEasing) },
        label = "playPauseRoundness",
        targetValueByState = { if (it) 32.dp else 16.dp }
    )

    Column(
        horizontalAlignment = Alignment.Start,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
    ) {

        Spacer(
            modifier = Modifier
                .height(20.dp)
        )

        BasicText(
            text = stringResource(R.string.now_playing),
            style = typography.xxs.secondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(
            modifier = Modifier
                .height(5.dp)
        )


        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                icon = R.drawable.disc,
                color = if (albumId == null) colorPalette.textDisabled else colorPalette.text,
                enabled = albumId != null,
                onClick = {
                    if (albumId != null) onGoToAlbum(albumId)
                },
                modifier = Modifier
                    .size(24.dp)
            )

            Spacer(
                modifier = Modifier
                    .width(8.dp)
            )

            ScrollText(
                text = title ?: "",
                style = TextStyle(
                    color = if (albumId == null) colorPalette.textDisabled else colorPalette.text,
                    fontStyle = typography.l.bold.fontStyle,
                    fontSize = typography.l.fontSize
                ),
                onClick = { if (albumId != null) onGoToAlbum(albumId) }
            )

        }

        Spacer(
            modifier = Modifier
                .height(10.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {


            artistIds?.distinct()?.forEach {
                IconButton(
                    icon = R.drawable.person,
                    color = if (it == "") colorPalette.textDisabled else colorPalette.text,
                    enabled = it != "",
                    onClick = {
                        onGoToArtist(it)
                    },
                    modifier = Modifier
                        .size(24.dp)

                )
                Spacer(
                    modifier = Modifier
                        .width(6.dp)
                )
            }

            Spacer(
                modifier = Modifier
                    .width(4.dp)
            )

            ScrollText(
                text = artist ?: "",
                style = TextStyle(
                    color = if (artistIds?.isEmpty() == true) colorPalette.textDisabled else colorPalette.text,
                    fontStyle = typography.l.bold.fontStyle,
                    fontSize = typography.l.fontSize
                ),
                onClick = {
                    if (artistIds?.isEmpty() == false) onGoToArtist(
                        artistIds?.get(0).toString()
                    )
                }
            )

        }
        /*
                Spacer(
                    modifier = Modifier
                        .height(15.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    BasicText(
                        text = stringResource(R.string.next_playing),
                        style = typography.xxs.secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(
                        modifier = Modifier
                            .height(5.dp)
                    )
                }



                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        icon = R.drawable.playlist,
                        color = colorPalette.text,
                        enabled = false,
                        onClick = {
                            //if add future action
                        },
                        modifier = Modifier
                            .size(14.dp)
                    )
                    IconButton(
                        icon = R.drawable.chevron_forward,
                        color = colorPalette.text,
                        enabled = false,
                        onClick = {
                            //if add future action
                        },
                        modifier = Modifier
                            .size(14.dp)
                    )

                    Spacer(
                        modifier = Modifier
                            .width(8.dp)
                    )

                    BasicText(
                        text = AnnotatedString(nextmediaItemtitle.toString() ?: ""),
                        style = typography.xs.secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis

                    )

                }
         */

        Spacer(
            modifier = Modifier
                .height(30.dp)
        )




        SeekBar(
            value = scrubbingPosition ?: position,
            minimumValue = 0,
            maximumValue = duration,
            onDragStart = {
                scrubbingPosition = it
            },
            onDrag = { delta ->
                scrubbingPosition = if (duration != C.TIME_UNSET) {
                    scrubbingPosition?.plus(delta)?.coerceIn(0, duration)
                } else {
                    null
                }
            },
            onDragEnd = {
                scrubbingPosition?.let(binder.player::seekTo)
                scrubbingPosition = null
            },
            color = colorPalette.collapsedPlayerProgressBar,
            backgroundColor = colorPalette.textSecondary,
            shape = RoundedCornerShape(8.dp)
        )


        Spacer(
            modifier = Modifier
                .height(8.dp)
        )



        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            BasicText(
                text = formatAsDuration(scrubbingPosition ?: position),
                style = typography.xxs.semiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            if (duration != C.TIME_UNSET) {
                BasicText(
                    text = formatAsDuration(duration),
                    style = typography.xxs.semiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Spacer(
            modifier = Modifier
                .weight(1f)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
        ) {
/*
            IconButton(
                icon = if (likedAt == null) R.drawable.heart_outline else R.drawable.heart,
                color = colorPalette.favoritesIcon,
                onClick = {
                    val currentMediaItem = binder.player.currentMediaItem
                    query {
                        if (Database.like(
                                mediaId,
                                if (likedAt == null) System.currentTimeMillis() else null
                            ) == 0
                        ) {
                            currentMediaItem
                                ?.takeIf { it.mediaId == mediaId }
                                ?.let {
                                    Database.insert(currentMediaItem, Song::toggleLike)
                                }
                        }
                    }
                    if (effectRotationEnabled) isRotated = !isRotated
                },
                modifier = Modifier
                    .rotate(rotationAngle)
                    .weight(1f)
                    .size(24.dp)
            )
*/
            IconButton(
                icon = R.drawable.play_skip_back,
                color = colorPalette.iconButtonPlayer,
                onClick = {
                    binder.player.forceSeekToPrevious()
                    if (effectRotationEnabled) isRotated = !isRotated
                },
                modifier = Modifier
                    .rotate(rotationAngle)
                    //.weight(1f)
                    .padding(10.dp)
                    .size(26.dp)
            )
            /*
            Spacer(
                modifier = Modifier
                    .width(8.dp)
            )

 */

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(playPauseRoundness))
                    .clickable {
                        if (shouldBePlaying) {
                            binder.player.pause()
                        } else {
                            if (binder.player.playbackState == Player.STATE_IDLE) {
                                binder.player.prepare()
                            }
                            binder.player.play()
                        }
                        if (effectRotationEnabled) isRotated = !isRotated
                    }
                    .background(colorPalette.background3)
                    //.size(50.dp)
                    .width(100.dp)
                    .height(50.dp)
                    //.weight(1f)
            ) {
                Image(
                    painter = painterResource(if (shouldBePlaying) R.drawable.pause else R.drawable.play),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(colorPalette.iconButtonPlayer),
                    modifier = Modifier
                        .rotate(rotationAngle)
                        .align(Alignment.Center)
                        .size(26.dp)
                )
            }
            /*
            Spacer(
                modifier = Modifier
                    .width(8.dp)
            )

 */

            IconButton(
                icon = R.drawable.play_skip_forward,
                color = colorPalette.iconButtonPlayer,
                onClick = {
                    binder.player.forceSeekToNext()
                    if (effectRotationEnabled) isRotated = !isRotated
                },
                modifier = Modifier
                    .rotate(rotationAngle)
                    //.weight(1f)
                    .padding(10.dp)
                    .size(26.dp)
            )
            /*
            IconButton(
                icon = R.drawable.infinite,
                color = if (trackLoopEnabled) colorPalette.iconButtonPlayer else colorPalette.textDisabled,
                onClick = {
                            trackLoopEnabled = !trackLoopEnabled
                            if (effectRotationEnabled) isRotated = !isRotated
                          },
                modifier = Modifier
                    .rotate(rotationAngle)
                    .weight(1f)
                    .size(24.dp)
            )

 */
            /*
            IconButton(
                icon = if (isCached) R.drawable.downloaded_square else R.drawable.download_square,
                color = if (isCached) colorPalette.iconButtonPlayer else colorPalette.textDisabled,
                onClick = {
                    trackLoopEnabled = !trackLoopEnabled
                    if (effectRotationEnabled) isRotated = !isRotated
                },
                modifier = Modifier
                    .rotate(rotationAngle)
                    .weight(1f)
                    .size(24.dp)
            )

 */
        }

        Spacer(
            modifier = Modifier
                .weight(0.8f)
        )

        if (!isLandscape) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
        ) {

            IconButton(
                icon = if (likedAt == null) R.drawable.heart_outline else R.drawable.heart,
                color = colorPalette.favoritesIcon,
                onClick = {
                    val currentMediaItem = binder.player.currentMediaItem
                    query {
                        if (Database.like(
                                mediaId,
                                if (likedAt == null) System.currentTimeMillis() else null
                            ) == 0
                        ) {
                            currentMediaItem
                                ?.takeIf { it.mediaId == mediaId }
                                ?.let {
                                    Database.insert(currentMediaItem, Song::toggleLike)
                                }
                        }
                    }
                    if (effectRotationEnabled) isRotated = !isRotated
                },
                modifier = Modifier
                    .rotate(rotationAngle)
                    //.weight(1f)
                    .size(24.dp)
            )

            IconButton(
                icon = R.drawable.repeat,
                color = if (trackLoopEnabled) colorPalette.iconButtonPlayer else colorPalette.textDisabled,
                onClick = {
                    trackLoopEnabled = !trackLoopEnabled
                    if (effectRotationEnabled) isRotated = !isRotated
                },
                modifier = Modifier
                    .rotate(rotationAngle)
                    //.weight(1f)
                    .size(24.dp)
            )



            IconButton(
                icon = if (isDownloaded) R.drawable.downloaded else R.drawable.download,
                color = if (isDownloaded) colorPalette.iconButtonPlayer else colorPalette.textDisabled,
                onClick = {
                    trackLoopEnabled = !trackLoopEnabled
                    if (effectRotationEnabled) isRotated = !isRotated
                },
                modifier = Modifier
                    .rotate(rotationAngle)
                    //.weight(1f)
                    .size(24.dp)
            )
/*
            IconButton(
                icon = R.drawable.shuffle,
                color = colorPalette.text,
                enabled = true,
                onClick = {
                    binder?.stopRadio()
                    binder?.player?.stop()
                    binder?.player?.shuffleQueue()
                    binder?.player?.play()
                },
                modifier = Modifier
                   .size(24.dp),
            )
*/

            IconButton(
                icon = R.drawable.ellipsis_horizontal,
                color = colorPalette.text,
                onClick = {
                    menuState.display {
                        PlayerMenu(
                            onDismiss = menuState::hide,
                            mediaItem =  binder.player.currentMediaItem ?: return@display,
                            binder = binder,
                            downloadbinder = downloadbinder
                        )
                    }
                },
                modifier = Modifier
                    //.padding(horizontal = 4.dp)
                    .size(24.dp)
            )


        }
    }
        Spacer(
            modifier = Modifier
                .weight(0.7f)
        )

        }


}

@ExperimentalAnimationApi
@UnstableApi
@Composable
private fun PlayerMenu(
    binder: PlayerService.Binder,
    downloadbinder: DownloaderService,
    mediaItem: MediaItem,
    onDismiss: () -> Unit
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