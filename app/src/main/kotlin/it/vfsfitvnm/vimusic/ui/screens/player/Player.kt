package it.vfsfitvnm.vimusic.ui.screens.player

import android.content.ActivityNotFoundException
import android.content.Intent
import android.media.audiofx.AudioEffect
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.Log
import coil.compose.AsyncImage
import it.vfsfitvnm.innertube.models.NavigationEndpoint
import it.vfsfitvnm.compose.routing.OnGlobalRoute
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.models.Info
import it.vfsfitvnm.vimusic.service.PlayerService
import it.vfsfitvnm.vimusic.ui.components.BottomSheet
import it.vfsfitvnm.vimusic.ui.components.BottomSheetState
import it.vfsfitvnm.vimusic.ui.components.LocalMenuState
import it.vfsfitvnm.vimusic.ui.components.rememberBottomSheetState
import it.vfsfitvnm.vimusic.ui.components.themed.BaseMediaItemMenu
import it.vfsfitvnm.vimusic.ui.components.themed.IconButton
import it.vfsfitvnm.vimusic.ui.components.themed.MenuEntry
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.collapsedPlayerProgressBar
import it.vfsfitvnm.vimusic.ui.styling.px
import it.vfsfitvnm.vimusic.utils.DisposableListener
import it.vfsfitvnm.vimusic.utils.forceSeekToNext
import it.vfsfitvnm.vimusic.utils.isLandscape
import it.vfsfitvnm.vimusic.utils.positionAndDurationState
import it.vfsfitvnm.vimusic.utils.seamlessPlay
import it.vfsfitvnm.vimusic.utils.secondary
import it.vfsfitvnm.vimusic.utils.semiBold
import it.vfsfitvnm.vimusic.utils.shouldBePlaying
import it.vfsfitvnm.vimusic.utils.thumbnail
import it.vfsfitvnm.vimusic.utils.toast
import it.vfsfitvnm.vimusic.enums.DragAnchors
import it.vfsfitvnm.vimusic.ui.components.themed.HalfHeader
import it.vfsfitvnm.vimusic.ui.components.themed.Header
import it.vfsfitvnm.vimusic.utils.forceSeekToPrevious
import it.vfsfitvnm.vimusic.utils.medium
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.absoluteValue



@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun Player(
    layoutState: BottomSheetState,
    modifier: Modifier = Modifier,
) {
    //val context = LocalContext.current

    val menuState = LocalMenuState.current

    val (colorPalette, typography, thumbnailShape) = LocalAppearance.current
    val binder = LocalPlayerServiceBinder.current

    binder?.player ?: return

    var nullableMediaItem by remember {
        mutableStateOf(binder.player.currentMediaItem, neverEqualPolicy())
    }

    var shouldBePlaying by remember {
        mutableStateOf(binder.player.shouldBePlaying)
    }

    /*
    val state = remember {
        AnchoredDraggableState(
            // 2
            initialValue = DragAnchors.Start,
            // 3
            positionalThreshold = { distance: Float -> distance * 0.5f },
            // 4
            velocityThreshold = { with(density) { 100.dp.toPx() } },
            // 5
            animationSpec = tween(),
        ).apply {
            // 6
            updateAnchors(
                // 7
                DraggableAnchors {
                    DragAnchors.Start at 0f
                    DragAnchors.End at 400f
                }
            )
        }
    }
    */

    binder.player.DisposableListener {
        object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                nullableMediaItem = mediaItem
            }

            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                shouldBePlaying = binder.player.shouldBePlaying
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                shouldBePlaying = binder.player.shouldBePlaying
            }
        }
    }

    val mediaItem = nullableMediaItem ?: return

    val positionAndDuration by binder.player.positionAndDurationState()

    val windowInsets = WindowInsets.systemBars

    val horizontalBottomPaddingValues = windowInsets
        .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom).asPaddingValues()

    var albumInfo by remember {
        mutableStateOf(mediaItem.mediaMetadata.extras?.getString("albumId")?.let { albumId ->
            Info(albumId, null)
        })
    }

    var artistsInfo by remember {
        mutableStateOf(
            mediaItem.mediaMetadata.extras?.getStringArrayList("artistNames")?.let { artistNames ->
                mediaItem.mediaMetadata.extras?.getStringArrayList("artistIds")?.let { artistIds ->
                    artistNames.zip(artistIds).map { (authorName, authorId) ->
                        Info(authorId, authorName)
                    }
                }
            }
        )
    }



    LaunchedEffect(mediaItem.mediaId) {
        withContext(Dispatchers.IO) {
            //if (albumInfo == null)
                albumInfo = Database.songAlbumInfo(mediaItem.mediaId)
            //if (artistsInfo == null)
                artistsInfo = Database.songArtistInfo(mediaItem.mediaId)

        }
    }



    val ExistIdsExtras = mediaItem.mediaMetadata.extras?.getStringArrayList("artistIds")?.size.toString()
    val ExistAlbumIdExtras = mediaItem.mediaMetadata.extras?.getString("albumId")?.toString()

    var albumId   = albumInfo?.id
    if (albumId == null) albumId = ExistAlbumIdExtras
    var albumTitle = albumInfo?.name

    var artistIds = arrayListOf<String>()
    artistsInfo?.forEach { (id) -> artistIds = arrayListOf(id) }
    if (ExistIdsExtras.equals(0).not()) mediaItem.mediaMetadata.extras?.getStringArrayList("artistIds")?.toCollection(artistIds)

    /*
    //Log.d("mediaItem_pl_mediaId",mediaItem.mediaId)
    Log.d("mediaItem_pl","--- START LOG ARTIST ---")
    Log.d("mediaItem_pl_extraArt?",ExistIdsExtras.toString())
    Log.d("mediaItem_pl_extrasArt",mediaItem.mediaMetadata.extras?.getStringArrayList("artistIds").toString())
    Log.d("mediaItem_pl_artinfo",artistsInfo.toString())
    Log.d("mediaItem_pl_artId",artistIds.toString())
    Log.d("mediaItem_pl","--- START LOG ALBUM ---")
    Log.d("mediaItem_pl_extraAlb?",ExistAlbumIdExtras.toString())
    //Log.d("mediaItem_pl_extras",mediaItem.mediaMetadata.extras.toString())
    Log.d("mediaItem_pl_albinfo",albumInfo.toString())
    Log.d("mediaItem_pl_albId",albumId.toString())
    Log.d("mediaItem_pl","--- END LOG ---")
    */

    OnGlobalRoute {
        layoutState.collapseSoft()
    }

    BottomSheet(
        state = layoutState,
        modifier = modifier,
        onDismiss = {
            binder.stopRadio()
            binder.player.clearMediaItems()
        },
        collapsedContent = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
                modifier = Modifier
                    .background(colorPalette.background1)
                    .fillMaxSize()
                    .padding(horizontalBottomPaddingValues)
                    .drawBehind {
                        val progress =
                            positionAndDuration.first.toFloat() / positionAndDuration.second.absoluteValue

                        drawLine(
                            color = colorPalette.collapsedPlayerProgressBar,
                            start = Offset(x = 0f, y = 1.dp.toPx()),
                            end = Offset(x = size.width * progress, y = 1.dp.toPx()),
                            strokeWidth = 2.dp.toPx()
                        )
                    }
            ) {
                Spacer(
                    modifier = Modifier
                        .width(2.dp)
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(Dimensions.collapsedPlayer)
                ) {
                    AsyncImage(
                        model = mediaItem.mediaMetadata.artworkUri.thumbnail(Dimensions.thumbnails.song.px),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .clip(thumbnailShape)
                            .size(48.dp)
                    )
                }

                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .height(Dimensions.collapsedPlayer)
                        .weight(1f)
                ) {
                    /* minimized player */
                    BasicText(
                        text = mediaItem.mediaMetadata.title?.toString() ?: "",
                        style = typography.xxs.semiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    BasicText(
                        text = mediaItem.mediaMetadata.artist?.toString() ?: "",
                        style = typography.xxs.semiBold.secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(
                    modifier = Modifier
                        .width(2.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .height(Dimensions.collapsedPlayer)
                ) {
                    IconButton(
                        icon = R.drawable.play_skip_back,
                        color = colorPalette.text,
                        onClick = binder.player::forceSeekToPrevious,
                        modifier = Modifier
                            .padding(horizontal = 2.dp, vertical = 8.dp)
                            .size(18.dp)
                    )

                    IconButton(
                        icon = if (shouldBePlaying) R.drawable.pause else R.drawable.play,
                        color = colorPalette.text,
                        onClick = {
                            if (shouldBePlaying) {
                                binder.player.pause()
                            } else {
                                if (binder.player.playbackState == Player.STATE_IDLE) {
                                    binder.player.prepare()
                                }
                                binder.player.play()
                            }
                        },
                        modifier = Modifier
                            .padding(horizontal = 2.dp, vertical = 8.dp)
                            .size(18.dp)
                    )

                    IconButton(
                        icon = R.drawable.play_skip_forward,
                        color = colorPalette.text,
                        onClick = binder.player::forceSeekToNext,
                        modifier = Modifier
                            .padding(horizontal = 2.dp, vertical = 8.dp)
                            .size(18.dp)
                    )
                }

                Spacer(
                    modifier = Modifier
                        .width(2.dp)
                )
            }
        }
    ) {
        var isShowingLyrics by rememberSaveable {
            mutableStateOf(false)
        }

        var isShowingStatsForNerds by rememberSaveable {
            mutableStateOf(false)
        }

        val playerBottomSheetState = rememberBottomSheetState(
            64.dp + horizontalBottomPaddingValues.calculateBottomPadding(),
            layoutState.expandedBound
        )

        val containerModifier = Modifier
            .background(colorPalette.background1)
            .padding(
                windowInsets
                    .only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
                    .asPaddingValues()
            )
            .padding(bottom = playerBottomSheetState.collapsedBound)

        val thumbnailContent: @Composable (modifier: Modifier) -> Unit = { modifier ->
            Thumbnail(
                isShowingLyrics = isShowingLyrics,
                onShowLyrics = { isShowingLyrics = it },
                isShowingStatsForNerds = isShowingStatsForNerds,
                onShowStatsForNerds = { isShowingStatsForNerds = it },
                modifier = modifier
                    .nestedScroll(layoutState.preUpPostDownNestedScrollConnection)
                    .border(BorderStroke(1.dp, colorPalette.textDisabled))
            )
        }

        val controlsContent: @Composable (modifier: Modifier) -> Unit = { modifier ->
            Controls(
                mediaId = mediaItem.mediaId,
                title = mediaItem.mediaMetadata.title?.toString(),
                artist = mediaItem.mediaMetadata.artist?.toString(),
                artistIds = artistIds,
                albumId = albumId,
                shouldBePlaying = shouldBePlaying,
                position = positionAndDuration.first,
                duration = positionAndDuration.second,
                modifier = modifier
            )
        }

        if (isLandscape) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = containerModifier
                    .padding(top = 32.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(0.66f)
                        .padding(bottom = 16.dp)
                ) {

                    thumbnailContent(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                    )
                }

                controlsContent(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .fillMaxHeight()
                        .weight(1f)
                )
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = containerModifier
                   // .padding(top = 54.dp)
            ) {
                HalfHeader(
                    title = stringResource(R.string.now_playing)
                )
/*
                BasicText(
                    text = stringResource(R.string.now_playing),
                    style = typography.l.medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = modifier
                        .padding(vertical = 15.dp)
                )

 */
                Box(
                    contentAlignment = Alignment.Center,
                    //modifier = Modifier
                        //.weight(1.25f)
                ) {
                    thumbnailContent(
                        modifier = Modifier
                            .padding(horizontal = 32.dp, vertical = 8.dp)
                    )
                }

                controlsContent(
                    modifier = Modifier
                        .padding(vertical = 24.dp)
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
        }


        Queue(
            layoutState = playerBottomSheetState,
            content = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(horizontal = 8.dp)
                        .fillMaxHeight()
                ) {
                    IconButton(
                        icon = R.drawable.ellipsis_horizontal,
                        color = colorPalette.text,
                        onClick = {
                            menuState.display {
                                PlayerMenu(
                                    onDismiss = menuState::hide,
                                    mediaItem = mediaItem,
                                    binder = binder
                                )
                            }
                        },
                        modifier = Modifier
                            .padding(horizontal = 4.dp, vertical = 8.dp)
                            .size(20.dp)
                    )

                    Spacer(
                        modifier = Modifier
                            .width(4.dp)
                    )
                }
            },
            backgroundColorProvider = { colorPalette.background2 },
            modifier = Modifier
                .align(Alignment.BottomCenter)
        )
    }
}

@ExperimentalAnimationApi
@Composable
private fun PlayerMenu(
    binder: PlayerService.Binder,
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
