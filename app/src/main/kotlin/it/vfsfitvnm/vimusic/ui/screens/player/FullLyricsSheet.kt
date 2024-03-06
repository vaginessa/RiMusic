package it.vfsfitvnm.vimusic.ui.screens.player


import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.C
import androidx.media3.common.util.Log
import coil.compose.AsyncImage
import com.valentinilk.shimmer.shimmer
import it.vfsfitvnm.innertube.Innertube
import it.vfsfitvnm.innertube.models.bodies.NextBody
import it.vfsfitvnm.innertube.requests.lyrics
import it.vfsfitvnm.kugou.KuGou
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.models.Lyrics
import it.vfsfitvnm.vimusic.models.Song
import it.vfsfitvnm.vimusic.query
import it.vfsfitvnm.vimusic.service.isLocal
import it.vfsfitvnm.vimusic.ui.components.BottomSheet
import it.vfsfitvnm.vimusic.ui.components.BottomSheetState
import it.vfsfitvnm.vimusic.ui.components.LocalMenuState
import it.vfsfitvnm.vimusic.ui.components.themed.IconButton
import it.vfsfitvnm.vimusic.ui.components.themed.InputTextDialog
import it.vfsfitvnm.vimusic.ui.components.themed.Menu
import it.vfsfitvnm.vimusic.ui.components.themed.MenuEntry
import it.vfsfitvnm.vimusic.ui.components.themed.TextPlaceholder
import it.vfsfitvnm.vimusic.ui.styling.DefaultDarkColorPalette
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.PureBlackColorPalette
import it.vfsfitvnm.vimusic.ui.styling.onOverlayShimmer
import it.vfsfitvnm.vimusic.ui.styling.px
import it.vfsfitvnm.vimusic.utils.SynchronizedLyrics
import it.vfsfitvnm.vimusic.utils.TextCopyToClipboard
import it.vfsfitvnm.vimusic.utils.center
import it.vfsfitvnm.vimusic.utils.color
import it.vfsfitvnm.vimusic.utils.forceSeekToNext
import it.vfsfitvnm.vimusic.utils.forceSeekToPrevious
import it.vfsfitvnm.vimusic.utils.getHttpClient
import it.vfsfitvnm.vimusic.utils.isShowingSynchronizedLyricsKey
import it.vfsfitvnm.vimusic.utils.languageDestination
import it.vfsfitvnm.vimusic.utils.medium
import it.vfsfitvnm.vimusic.utils.rememberPreference
import it.vfsfitvnm.vimusic.utils.secondary
import it.vfsfitvnm.vimusic.utils.shouldBePlaying
import it.vfsfitvnm.vimusic.utils.thumbnail
import it.vfsfitvnm.vimusic.utils.toast
import it.vfsfitvnm.vimusic.utils.verticalFadingEdge
import it.vfsfitvnm.vimusic.utils.windows
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import me.bush.translator.Language
import me.bush.translator.Translator

@ExperimentalTextApi
@SuppressLint("SuspiciousIndentation")
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@androidx.media3.common.util.UnstableApi
@Composable
fun FullLyricsSheet(
    //mediaId: String,
    backgroundColorProvider: () -> Color,
    layoutState: BottomSheetState,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
    onMaximize: () -> Unit,
    onRefresh: () -> Unit
) {
    val (colorPalette, typography, thumbnailShape) = LocalAppearance.current
    val (thumbnailSizeDp) = Dimensions.thumbnails.player.song.let {
        it to (it - 64.dp).px
    }
    val binder = LocalPlayerServiceBinder.current
    var shouldBePlaying by remember {
        mutableStateOf(false)
    }
    shouldBePlaying = binder?.player?.shouldBePlaying == true
    val shouldBePlayingTransition = updateTransition(shouldBePlaying, label = "shouldBePlaying")
    val playPauseRoundness by shouldBePlayingTransition.animateDp(
        transitionSpec = { tween(durationMillis = 100, easing = LinearEasing) },
        label = "playPauseRoundness",
        targetValueByState = { if (it == true) 24.dp else 12.dp }
    )
    val size = thumbnailSizeDp
    var mediaId by remember {
        mutableStateOf(binder?.player?.currentMediaItem?.mediaId ?: "")
    }

    BottomSheet(
        state = layoutState,
        modifier = modifier,
        collapsedContent = {}
    ) {
        val binder = LocalPlayerServiceBinder.current
        binder?.player ?: return@BottomSheet
        val player = binder.player

        Column(
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .background(Color.Black.copy(0.9f))
                .fillMaxHeight()
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .padding(vertical = 30.dp, horizontal = 4.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(Dimensions.collapsedPlayer)
                ) {
                    AsyncImage(
                        model = player.currentMediaItem?.mediaMetadata?.artworkUri.thumbnail(
                            Dimensions.thumbnails.song.px
                        ),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .clip(thumbnailShape)
                            .size(48.dp)
                    )
                }


                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                ) {
                    BasicText(
                        text = player.currentMediaItem?.mediaMetadata?.title?.toString() ?: "",
                        style = typography.s.medium.secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    BasicText(
                        text = player.currentMediaItem?.mediaMetadata?.artist?.toString() ?: "",
                        style = typography.xs.medium.secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )


                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .padding(horizontal = 4.dp)
            ) {


                //if (!player.currentMediaItem?.isLocal!!)
                    player.currentMediaItem?.mediaId?.let {
                        player.currentMediaItem!!::mediaMetadata.let { it1 ->
                            Lyrics(
                                enableClick = true,
                                mediaId = it,
                                isDisplayed = true,
                                onDismiss = {},
                                onMaximize = onMaximize,
                                ensureSongInserted = {
                                    /*
                                    query {
                                        Database.insert(player.currentMediaItem!!)
                                    }
                                     */
                                },
                                size = thumbnailSizeDp,
                                mediaMetadataProvider = it1,
                                durationProvider = player::getDuration,
                                trailingContent = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {

                                        IconButton(
                                            icon = R.drawable.play_skip_back,
                                            color = colorPalette.background2,
                                            onClick = {
                                                binder.player.forceSeekToPrevious()
                                                onRefresh()
                                            },
                                            modifier = Modifier
                                                .padding(horizontal = 2.dp, vertical = 8.dp)
                                                .size(24.dp)
                                        )

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(playPauseRoundness))
                                                .clickable {
                                                    //Log.d("mediaItem", "$shouldBePlaying")
                                                    shouldBePlaying = if (shouldBePlaying == true) {
                                                        binder.player.pause()
                                                        false
                                                    } else {
                                                        if (binder.player.playbackState == androidx.media3.common.Player.STATE_IDLE) {
                                                            binder.player.prepare()
                                                        }
                                                        binder.player.play()
                                                        true
                                                    }

                                                }
                                                .background(colorPalette.background2)
                                                .size(42.dp)
                                        ) {
                                            Image(
                                                painter = painterResource(if (shouldBePlaying == true) R.drawable.pause else R.drawable.play),
                                                contentDescription = null,
                                                colorFilter = ColorFilter.tint(colorPalette.iconButtonPlayer),
                                                modifier = Modifier

                                                    .align(Alignment.Center)
                                                    .size(24.dp)
                                            )
                                        }

                                        IconButton(
                                            icon = R.drawable.play_skip_forward,
                                            color = colorPalette.background2,
                                            onClick = {
                                                binder.player.forceSeekToNext()
                                                onRefresh()
                                            },
                                            modifier = Modifier
                                                .padding(horizontal = 2.dp, vertical = 8.dp)
                                                .size(24.dp)
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxHeight(0.9f)
                            )
                        }
                    }
            }
        }
    }
}
