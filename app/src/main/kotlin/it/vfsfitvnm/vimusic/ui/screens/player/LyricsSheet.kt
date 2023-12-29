package it.vfsfitvnm.vimusic.ui.screens.player


import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.service.isLocal
import it.vfsfitvnm.vimusic.ui.components.BottomSheet
import it.vfsfitvnm.vimusic.ui.components.BottomSheetState
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.px
import it.vfsfitvnm.vimusic.utils.medium
import it.vfsfitvnm.vimusic.utils.secondary
import it.vfsfitvnm.vimusic.utils.thumbnail

@ExperimentalTextApi
@SuppressLint("SuspiciousIndentation")
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@androidx.media3.common.util.UnstableApi
@Composable
fun LyricsSheet(
    backgroundColorProvider: () -> Color,
    layoutState: BottomSheetState,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
    onMaximize: () -> Unit
) {
    val (colorPalette, typography, thumbnailShape) = LocalAppearance.current
    val (thumbnailSizeDp) = Dimensions.thumbnails.player.song.let {
        it to (it - 64.dp).px
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

                if (!player.currentMediaItem?.isLocal!!)
                    player.currentMediaItem?.mediaId?.let {
                        player.currentMediaItem!!::mediaMetadata.let { it1 ->
                            Lyrics(
                                mediaId = it,
                                isDisplayed = true,
                                onDismiss = {},
                                onMaximize = onMaximize,
                                ensureSongInserted = { Database.insert(player.currentMediaItem!!) },
                                size = thumbnailSizeDp,
                                mediaMetadataProvider = it1,
                                durationProvider = player::getDuration,
                                modifier = Modifier
                                    .fillMaxHeight(0.9f)
                            )
                        }
                    }
            }
        }
    }
}
