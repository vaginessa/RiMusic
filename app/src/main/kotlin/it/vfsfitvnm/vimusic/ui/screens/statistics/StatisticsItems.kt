package it.vfsfitvnm.vimusic.ui.screens.statistics

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.compose.persist.persistList
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerAwareWindowInsets
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.SongSortBy
import it.vfsfitvnm.vimusic.enums.SortOrder
import it.vfsfitvnm.vimusic.enums.StatisticsType
import it.vfsfitvnm.vimusic.models.Song
import it.vfsfitvnm.vimusic.ui.components.LocalMenuState
import it.vfsfitvnm.vimusic.ui.components.themed.FloatingActionsContainerWithScrollToTop
import it.vfsfitvnm.vimusic.ui.components.themed.Header
import it.vfsfitvnm.vimusic.ui.components.themed.HeaderIconButton
import it.vfsfitvnm.vimusic.ui.components.themed.HeaderInfo
import it.vfsfitvnm.vimusic.ui.components.themed.SecondaryButton
import it.vfsfitvnm.vimusic.ui.items.SongItem
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.px
import it.vfsfitvnm.vimusic.utils.asMediaItem
import it.vfsfitvnm.vimusic.utils.enqueue
import it.vfsfitvnm.vimusic.utils.forcePlayAtIndex
import it.vfsfitvnm.vimusic.utils.forcePlayFromBeginning
import it.vfsfitvnm.vimusic.utils.rememberPreference
import it.vfsfitvnm.vimusic.utils.songSortByKey
import it.vfsfitvnm.vimusic.utils.songSortOrderKey
import java.time.LocalDateTime
import java.time.ZoneOffset


@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun StatisticsItems(statisticsType: StatisticsType) {
    val (colorPalette) = LocalAppearance.current
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current

    var songs by persistList<Song>("${statisticsType.name}/songs")

    var sortBy by rememberPreference(songSortByKey, SongSortBy.DateAdded)
    var sortOrder by rememberPreference(songSortOrderKey, SortOrder.Descending)

    val now: Long = System.currentTimeMillis()
    val dateTime = LocalDateTime.now()
    val today = dateTime.minusDays(1).toEpochSecond(ZoneOffset.UTC)
    val lastWeek = dateTime.minusDays(7).toEpochSecond(ZoneOffset.UTC)
    val lastMonth = dateTime.minusDays(30).toEpochSecond(ZoneOffset.UTC)
    val last3Month = dateTime.minusDays(90).toEpochSecond(ZoneOffset.UTC)
    val last6Month = dateTime.minusDays(180).toEpochSecond(ZoneOffset.UTC)
    val lastYear = dateTime.minusDays(365).toEpochSecond(ZoneOffset.UTC)



    //val instant = Instant.now().minus() minusMillis(86400000)
    //val millis = instant.toEpochMilli()
/*
    val day = 86400000

    val lastWeek = now - (day * 7)
    val lastMonth = now - (day * 30) // last 30 days
    val last3Month = now - (day * 90)
    val last6Month = now - (day * 180)
    val lastYear = now - (day * 365)
*/

    LaunchedEffect(Unit, sortBy, sortOrder) {
/*
        when (statisticsType) {
            StatisticsType.Today -> {
                Database
                    .songsMostPlayedByPeriod(today,now,2)
            }
            StatisticsType.OneWeek -> {
                Database
                    .songsMostPlayedByPeriod(lastWeek,now,2)
            }
            StatisticsType.OneMonth -> {
                Database
                    .songsMostPlayedByPeriod(lastMonth,now,2)
            }
            StatisticsType.ThreeMonths -> {
                Database
                    .songsMostPlayedByPeriod(last3Month,now,2)
            }
            StatisticsType.SixMonths -> {
                Database
                    .songsMostPlayedByPeriod(last6Month,now,2)
            }
            StatisticsType.OneYear -> {
                Database
                    .songsMostPlayedByPeriod(lastYear,now,2)
            }
            StatisticsType.All -> {
                Database
                    .songsMostPlayedByPeriod(lastYear*20,now,2)
            }

        /*
            BuiltInPlaylist.Offline -> Database
                .songsOffline(sortBy, sortOrder)
                .flowOn(Dispatchers.IO)
                .map { songs ->
                    songs.filter { song ->
                        song.contentLength?.let {
                            binder?.cache?.isCached(song.song.id, 0, song.contentLength)
                        } ?: false
                    }.map(SongWithContentLength::song)
                }
            */
        }.collect { songs = it }


*/
    }

    val thumbnailSizeDp = Dimensions.thumbnails.song
    val thumbnailSize = thumbnailSizeDp.px

    val sortOrderIconRotation by animateFloatAsState(
        targetValue = if (sortOrder == SortOrder.Ascending) 0f else 180f,
        animationSpec = tween(durationMillis = 400, easing = LinearEasing)
    )

    val lazyListState = rememberLazyListState()



    Box {
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
                Header(
                    title = when (statisticsType) {
                        StatisticsType.Today -> "Today"
                        StatisticsType.OneWeek -> "One week"
                        StatisticsType.OneMonth -> "One month"
                        StatisticsType.ThreeMonths -> "Three months"
                        StatisticsType.SixMonths -> "Six months"
                        StatisticsType.OneYear -> "One year"
                        StatisticsType.All -> "All"
                    },
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                ) {
                    HeaderInfo(
                        title = "Most Played",
                        icon = painterResource(R.drawable.musical_notes),
                        spacer = 0
                    )
                }
            }

            itemsIndexed(
                items = songs,
                key = { _, song -> song.id },
                contentType = { _, song -> song },
            ) { index, song ->
                SongItem(
                    song = song,
                    thumbnailSizeDp = thumbnailSizeDp,
                    thumbnailSizePx = thumbnailSize,
                    modifier = Modifier
                        .combinedClickable(
                            onLongClick = {
                                menuState.display {
                                    /*
                                    when (builtInPlaylist) {
                                        BuiltInPlaylist.Favorites -> NonQueuedMediaItemMenu(
                                            mediaItem = song.asMediaItem,
                                            onDismiss = menuState::hide
                                        )

                                        BuiltInPlaylist.Offline -> InHistoryMediaItemMenu(
                                            song = song,
                                            onDismiss = menuState::hide
                                        )
                                    }
                                    */
                                }
                            },
                            onClick = {
                                binder?.stopRadio()
                                binder?.player?.forcePlayAtIndex(
                                    songs.map(Song::asMediaItem),
                                    index
                                )
                            }
                        )
                        .animateItemPlacement()
                )
            }
        }

        FloatingActionsContainerWithScrollToTop(
            lazyListState = lazyListState,
            iconId = R.drawable.shuffle,
            onClick = {
                if (songs.isNotEmpty()) {
                    binder?.stopRadio()
                    binder?.player?.forcePlayFromBeginning(
                        songs.shuffled().map(Song::asMediaItem)
                    )
                }
            }
        )
    }
}
