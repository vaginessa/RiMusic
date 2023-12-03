package it.vfsfitvnm.vimusic.ui.screens.home

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.compose.persist.persistList
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerAwareWindowInsets
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.BuiltInPlaylist
import it.vfsfitvnm.vimusic.enums.PlaylistSortBy
import it.vfsfitvnm.vimusic.enums.SortOrder
import it.vfsfitvnm.vimusic.enums.StatisticsType
import it.vfsfitvnm.vimusic.enums.UiType
import it.vfsfitvnm.vimusic.models.Playlist
import it.vfsfitvnm.vimusic.models.PlaylistPreview
import it.vfsfitvnm.vimusic.query
import it.vfsfitvnm.vimusic.ui.components.themed.FloatingActionsContainerWithScrollToTop
import it.vfsfitvnm.vimusic.ui.components.themed.HalfHeader
import it.vfsfitvnm.vimusic.ui.components.themed.HeaderWithIcon
import it.vfsfitvnm.vimusic.ui.components.themed.SecondaryButton
import it.vfsfitvnm.vimusic.ui.components.themed.TextFieldDialog
import it.vfsfitvnm.vimusic.ui.items.PlaylistItem
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.favoritesIcon
import it.vfsfitvnm.vimusic.ui.styling.px
import it.vfsfitvnm.vimusic.utils.UiTypeKey
import it.vfsfitvnm.vimusic.utils.playlistSortByKey
import it.vfsfitvnm.vimusic.utils.playlistSortOrderKey
import it.vfsfitvnm.vimusic.utils.rememberPreference

@SuppressLint("SuspiciousIndentation")
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Composable
fun HomeStatistics(
    onStatisticsType: (StatisticsType) -> Unit,
    onBuiltInPlaylist: (BuiltInPlaylist) -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onSearchClick: () -> Unit,
) {
    val (colorPalette, typography, thumbnailShape) = LocalAppearance.current
    val uiType  by rememberPreference(UiTypeKey, UiType.RiMusic)

    var isCreatingANewPlaylist by rememberSaveable {
        mutableStateOf(false)
    }

    if (isCreatingANewPlaylist) {
        TextFieldDialog(
            hintText = stringResource(R.string.enter_the_playlist_name),
            onDismiss = {
                isCreatingANewPlaylist = false
            },
            onDone = { text ->
                query {
                    Database.insert(Playlist(name = text))
                }
            }
        )
    }

    var sortBy by rememberPreference(playlistSortByKey, PlaylistSortBy.DateAdded)
    var sortOrder by rememberPreference(playlistSortOrderKey, SortOrder.Descending)

    var items by persistList<PlaylistPreview>("home/playlists")

    LaunchedEffect(sortBy, sortOrder) {
        Database.playlistPreviews(sortBy, sortOrder).collect { items = it }
    }
/*
    val sortOrderIconRotation by animateFloatAsState(
        targetValue = if (sortOrder == SortOrder.Ascending) 0f else 180f,
        animationSpec = tween(durationMillis = 400, easing = LinearEasing)
    )
*/
    val thumbnailSizeDp = 108.dp
    val thumbnailSizePx = thumbnailSizeDp.px

    val lazyGridState = rememberLazyGridState()

    Box {
        LazyVerticalGrid(
            state = lazyGridState,
            columns = GridCells.Adaptive(Dimensions.thumbnails.song * 2 + Dimensions.itemsVerticalPadding * 2),
            contentPadding = LocalPlayerAwareWindowInsets.current
                .only(WindowInsetsSides.Vertical + WindowInsetsSides.End).asPaddingValues(),
            verticalArrangement = Arrangement.spacedBy(Dimensions.itemsVerticalPadding * 2),
            horizontalArrangement = Arrangement.spacedBy(
                space = Dimensions.itemsVerticalPadding * 2,
                alignment = Alignment.CenterHorizontally
            ),
            modifier = Modifier
                .fillMaxSize()
                .background(colorPalette.background0)
        ) {
            item(key = "header", contentType = 0, span = { GridItemSpan(maxLineSpan) }) {

                HeaderWithIcon(
                    title = stringResource(R.string.statistics),
                    iconId = R.drawable.search,
                    enabled = true,
                    showIcon = true,
                    modifier = Modifier,
                    onClick = onSearchClick
                )

            }

            item(key = "today") {
                PlaylistItem(
                    icon = R.drawable.query_stats,
                    colorTint = colorPalette.favoritesIcon,
                    name = stringResource(R.string.today),
                    songCount = null,
                    thumbnailSizeDp = thumbnailSizeDp,
                    alternative = true,
                    modifier = Modifier
                        .clip(thumbnailShape)
                        .clickable(onClick = { onStatisticsType(StatisticsType.Today) })
                        .animateItemPlacement()

                )
            }

            item(key = "oneweek") {
                PlaylistItem(
                    icon = R.drawable.query_stats,
                    colorTint = colorPalette.favoritesIcon,
                    name = stringResource(R.string._1_week),
                    songCount = null,
                    thumbnailSizeDp = thumbnailSizeDp,
                    alternative = true,
                    modifier = Modifier
                        .clip(thumbnailShape)
                        .clickable(onClick = { onStatisticsType(StatisticsType.OneWeek) })
                        .animateItemPlacement()

                )
            }

            item(key = "onemonth") {
                PlaylistItem(
                    icon = R.drawable.query_stats,
                    colorTint = colorPalette.favoritesIcon,
                    name = stringResource(R.string._1_month),
                    songCount = null,
                    thumbnailSizeDp = thumbnailSizeDp,
                    alternative = true,
                    modifier = Modifier
                        .clip(thumbnailShape)
                        .clickable(onClick = { onStatisticsType(StatisticsType.OneMonth) })
                        .animateItemPlacement()
                )
            }

            item(key = "threemonths") {
                PlaylistItem(
                    icon = R.drawable.query_stats,
                    colorTint = colorPalette.favoritesIcon,
                    name = stringResource(R.string._3_month),
                    songCount = null,
                    thumbnailSizeDp = thumbnailSizeDp,
                    alternative = true,
                    modifier = Modifier
                        .clip(thumbnailShape)
                        .clickable(onClick = { onStatisticsType(StatisticsType.ThreeMonths) })
                        .animateItemPlacement()
                )
            }

            item(key = "sixmonths") {
                PlaylistItem(
                    icon = R.drawable.query_stats,
                    colorTint = colorPalette.favoritesIcon,
                    name = stringResource(R.string._6_month),
                    songCount = null,
                    thumbnailSizeDp = thumbnailSizeDp,
                    alternative = true,
                    modifier = Modifier
                        .clip(thumbnailShape)
                        .clickable(onClick = { onStatisticsType(StatisticsType.SixMonths) })
                        .animateItemPlacement()
                )
            }

            item(key = "oneyear") {
                PlaylistItem(
                    icon = R.drawable.query_stats,
                    colorTint = colorPalette.favoritesIcon,
                    name = stringResource(R.string._1_year),
                    songCount = null,
                    thumbnailSizeDp = thumbnailSizeDp,
                    alternative = true,
                    modifier = Modifier
                        .clip(thumbnailShape)
                        .clickable(onClick = { onStatisticsType(StatisticsType.OneYear) })
                        .animateItemPlacement()
                )
            }

            item(key = "all") {
                PlaylistItem(
                    icon = R.drawable.query_stats,
                    colorTint = colorPalette.favoritesIcon,
                    name = stringResource(R.string.all),
                    songCount = null,
                    thumbnailSizeDp = thumbnailSizeDp,
                    alternative = true,
                    modifier = Modifier
                        .clip(thumbnailShape)
                        .clickable(onClick = { onStatisticsType(StatisticsType.All) })
                        .animateItemPlacement()
                )
            }

        }
        if(uiType == UiType.ViMusic)
        FloatingActionsContainerWithScrollToTop(
            lazyGridState = lazyGridState,
            iconId = R.drawable.search,
            onClick = onSearchClick
        )


    }
}
