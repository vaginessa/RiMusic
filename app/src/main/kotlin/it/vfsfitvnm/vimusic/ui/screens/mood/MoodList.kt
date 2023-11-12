package it.vfsfitvnm.vimusic.ui.screens.mood

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.shimmer
import it.vfsfitvnm.compose.persist.persist
import it.vfsfitvnm.innertube.Innertube
import it.vfsfitvnm.innertube.models.bodies.BrowseBody
import it.vfsfitvnm.innertube.requests.BrowseResult
import it.vfsfitvnm.innertube.requests.browse
import it.vfsfitvnm.vimusic.LocalPlayerAwareWindowInsets
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.models.Mood
import it.vfsfitvnm.vimusic.ui.components.ShimmerHost
import it.vfsfitvnm.vimusic.ui.components.themed.Header
import it.vfsfitvnm.vimusic.ui.components.themed.HeaderPlaceholder
import it.vfsfitvnm.vimusic.ui.components.themed.HeaderWithIcon
import it.vfsfitvnm.vimusic.ui.components.themed.TextPlaceholder
import it.vfsfitvnm.vimusic.ui.items.AlbumItem
import it.vfsfitvnm.vimusic.ui.items.AlbumItemPlaceholder
import it.vfsfitvnm.vimusic.ui.items.ArtistItem
import it.vfsfitvnm.vimusic.ui.items.PlaylistItem
import it.vfsfitvnm.vimusic.ui.screens.albumRoute
import it.vfsfitvnm.vimusic.ui.screens.artistRoute
import it.vfsfitvnm.vimusic.ui.screens.playlistRoute
import it.vfsfitvnm.vimusic.ui.screens.playlistRoute1
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.px
import it.vfsfitvnm.vimusic.utils.center
import it.vfsfitvnm.vimusic.utils.secondary
import it.vfsfitvnm.vimusic.utils.semiBold

internal const val defaultBrowseId = "FEmusic_moods_and_genres_category"

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun MoodList(mood: Mood) {
    val (colorPalette, typography) = LocalAppearance.current
    val windowInsets = LocalPlayerAwareWindowInsets.current

    val browseId = mood.browseId ?: defaultBrowseId
    var moodPage by persist<Result<BrowseResult>>("playlist/$browseId${mood.params?.let { "/$it" } ?: ""}")

    LaunchedEffect(Unit) {
        moodPage = Innertube.browse(BrowseBody(browseId = browseId, params = mood.params))
    }

    val thumbnailSizeDp = Dimensions.thumbnails.album
    val thumbnailSizePx = thumbnailSizeDp.px

    val lazyListState = rememberLazyListState()

    val endPaddingValues = windowInsets.only(WindowInsetsSides.End).asPaddingValues()

    val sectionTextModifier = Modifier
        .padding(horizontal = 16.dp)
        .padding(top = 24.dp, bottom = 8.dp)
        .padding(endPaddingValues)

    Column {
        moodPage?.getOrNull()?.let { moodResult ->
            LazyColumn(
                state = lazyListState,
                contentPadding = LocalPlayerAwareWindowInsets.current
                    .only(WindowInsetsSides.Vertical + WindowInsetsSides.End).asPaddingValues(),
                modifier = Modifier
                    .background(colorPalette.background0)
                    .fillMaxSize()
            ) {
                item(
                    key = "header",
                    contentType = 0
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        //Header(title = mood.name)
                        HeaderWithIcon(
                            title = mood.name,
                            iconId = R.drawable.globe,
                            enabled = true,
                            showIcon = true,
                            modifier = Modifier,
                            onClick = {}
                        )
                    }
                }

                moodResult.items.forEach { item ->
                    item {
                        BasicText(
                            text = item.title,
                            style = typography.m.semiBold,
                            modifier = sectionTextModifier
                        )
                    }
                    item {
                        LazyRow {
                            items(items = item.items, key = { it.key }) { childItem ->
                                if (childItem.key == defaultBrowseId) return@items
                                when (childItem) {
                                    is Innertube.AlbumItem -> AlbumItem(
                                        album = childItem,
                                        thumbnailSizePx = thumbnailSizePx,
                                        thumbnailSizeDp = thumbnailSizeDp,
                                        alternative = true,
                                        modifier = Modifier.clickable {
                                            childItem.info?.endpoint?.browseId?.let {
                                                albumRoute.global(
                                                    it
                                                )
                                            }
                                        }
                                    )

                                    is Innertube.ArtistItem -> ArtistItem(
                                        artist = childItem,
                                        thumbnailSizePx = thumbnailSizePx,
                                        thumbnailSizeDp = thumbnailSizeDp,
                                        alternative = true,
                                        modifier = Modifier.clickable {
                                            childItem.info?.endpoint?.browseId?.let {
                                                artistRoute.global(
                                                    it
                                                )
                                            }
                                        }
                                    )

                                    is Innertube.PlaylistItem -> PlaylistItem(
                                        playlist = childItem,
                                        thumbnailSizePx = thumbnailSizePx,
                                        thumbnailSizeDp = thumbnailSizeDp,
                                        alternative = true,
                                        modifier = Modifier.clickable {
                                            childItem.info?.endpoint?.browseId?.let {
                                                playlistRoute1.global(
                                                    it,
                                                    null,
                                                    1
                                                )
                                            }
                                        }
                                    )

                                    else -> {}
                                }
                            }
                        }
                    }
                }
            }
        } ?: moodPage?.exceptionOrNull()?.let {
            BasicText(
                text = "An error has occurred",
                style = typography.s.secondary.center,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(all = 16.dp)
            )
        } ?: ShimmerHost {
            HeaderPlaceholder(modifier = Modifier.shimmer())
            repeat(4) {
                TextPlaceholder(modifier = sectionTextModifier)
                Row {
                    repeat(6) {
                        AlbumItemPlaceholder(
                            thumbnailSizeDp = thumbnailSizeDp,
                            alternative = true
                        )
                    }
                }
            }
        }
    }
}
