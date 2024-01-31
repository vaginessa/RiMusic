package it.vfsfitvnm.vimusic.ui.screens.statistics

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import it.vfsfitvnm.compose.persist.persistList
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerAwareWindowInsets
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.MaxStatisticsItems
import it.vfsfitvnm.vimusic.enums.PlayEventsType
import it.vfsfitvnm.vimusic.enums.StatisticsType
import it.vfsfitvnm.vimusic.enums.ThumbnailRoundness
import it.vfsfitvnm.vimusic.models.Album
import it.vfsfitvnm.vimusic.models.Artist
import it.vfsfitvnm.vimusic.models.PlaylistPreview
import it.vfsfitvnm.vimusic.models.Song
import it.vfsfitvnm.vimusic.ui.components.LocalMenuState
import it.vfsfitvnm.vimusic.ui.components.themed.HeaderInfo
import it.vfsfitvnm.vimusic.ui.components.themed.HeaderWithIcon
import it.vfsfitvnm.vimusic.ui.components.themed.NonQueuedMediaItemMenu
import it.vfsfitvnm.vimusic.ui.items.AlbumItem
import it.vfsfitvnm.vimusic.ui.items.ArtistItem
import it.vfsfitvnm.vimusic.ui.items.PlaylistItem
import it.vfsfitvnm.vimusic.ui.items.SongItem
import it.vfsfitvnm.vimusic.ui.screens.albumRoute
import it.vfsfitvnm.vimusic.ui.screens.artistRoute
import it.vfsfitvnm.vimusic.ui.screens.localPlaylistRoute
import it.vfsfitvnm.vimusic.ui.screens.settings.SettingsEntry
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.px
import it.vfsfitvnm.vimusic.ui.styling.shimmer
//import it.vfsfitvnm.vimusic.utils.SnapLayoutInfoProvider
import it.vfsfitvnm.vimusic.utils.UpdateYoutubeAlbum
import it.vfsfitvnm.vimusic.utils.UpdateYoutubeArtist
import it.vfsfitvnm.vimusic.utils.asMediaItem
import it.vfsfitvnm.vimusic.utils.downloadedStateMedia
import it.vfsfitvnm.vimusic.utils.durationTextToMillis
import it.vfsfitvnm.vimusic.utils.forcePlayAtIndex
import it.vfsfitvnm.vimusic.utils.formatAsTime
import it.vfsfitvnm.vimusic.utils.getDownloadState
import it.vfsfitvnm.vimusic.utils.isLandscape
import it.vfsfitvnm.vimusic.utils.manageDownload
import it.vfsfitvnm.vimusic.utils.maxStatisticsItemsKey
import it.vfsfitvnm.vimusic.utils.rememberPreference
import it.vfsfitvnm.vimusic.utils.secondary
import it.vfsfitvnm.vimusic.utils.semiBold
import it.vfsfitvnm.vimusic.utils.showStatsListeningTimeKey
import it.vfsfitvnm.vimusic.utils.thumbnailRoundnessKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.ZoneOffset

@ExperimentalTextApi
@SuppressLint("SuspiciousIndentation")
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@UnstableApi
@Composable
fun StatisticsPage(
    statisticsType: StatisticsType
) {
    val onGoToArtist = artistRoute::global
    val onGoToAlbum = albumRoute::global
    //val onGoToPlaylist = playlistRoute::global
    val onGoToPlaylist = localPlaylistRoute::global

    val (colorPalette, typography) = LocalAppearance.current
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current
    val windowInsets = LocalPlayerAwareWindowInsets.current

    val songThumbnailSizeDp = Dimensions.thumbnails.song
    val songThumbnailSizePx = songThumbnailSizeDp.px
    val albumThumbnailSizeDp = 108.dp
    val albumThumbnailSizePx = albumThumbnailSizeDp.px
    val artistThumbnailSizeDp = 92.dp
    val artistThumbnailSizePx = artistThumbnailSizeDp.px
    val playlistThumbnailSizeDp = 108.dp
    val playlistThumbnailSizePx = playlistThumbnailSizeDp.px

    val scrollState = rememberScrollState()
    val quickPicksLazyGridState = rememberLazyGridState()

    val endPaddingValues = windowInsets.only(WindowInsetsSides.End).asPaddingValues()

    val sectionTextModifier = Modifier
        .padding(horizontal = 16.dp)
        .padding(top = 24.dp, bottom = 8.dp)
        .padding(endPaddingValues)

    var thumbnailRoundness by rememberPreference(
        thumbnailRoundnessKey,
        ThumbnailRoundness.Heavy
    )

    var showStatsListeningTime by rememberPreference(showStatsListeningTimeKey,   true)

    val context = LocalContext.current

    val thumbnailSizeDp = Dimensions.thumbnails.song
    val thumbnailSize = thumbnailSizeDp.px

    var songs by persistList<Song>("statistics/songs")
    var allSongs by persistList<Song>("statistics/allsongs")
    var artists by persistList<Artist>("statistics/artists")
    var albums by persistList<Album>("statistics/albums")
    var playlists by persistList<PlaylistPreview>("statistics/playlists")



    val now: Long = System.currentTimeMillis()
    //val now: Long = System.currentTimeMillis() / 1000
    val dateTime = LocalDateTime.now()
    //val today = dateTime.minusDays(1).toEpochSecond(ZoneOffset.UTC)
    val today = dateTime.minusHours(23).toEpochSecond(ZoneOffset.UTC) * 1000
    val lastWeek = dateTime.minusDays(7).toEpochSecond(ZoneOffset.UTC) * 1000
    val lastMonth = dateTime.minusDays(30).toEpochSecond(ZoneOffset.UTC) * 1000
    val last3Month = dateTime.minusDays(90).toEpochSecond(ZoneOffset.UTC) * 1000
    val last6Month = dateTime.minusDays(180).toEpochSecond(ZoneOffset.UTC) * 1000
    val lastYear = dateTime.minusDays(365).toEpochSecond(ZoneOffset.UTC) * 1000
    val last20Year = dateTime.minusYears(20).toEpochSecond(ZoneOffset.UTC) * 1000

    val from = when (statisticsType) {
        StatisticsType.Today -> today
        StatisticsType.OneWeek -> lastWeek
        StatisticsType.OneMonth -> lastMonth
        StatisticsType.ThreeMonths -> last3Month
        StatisticsType.SixMonths -> last6Month
        StatisticsType.OneYear -> lastYear
        StatisticsType.All -> last20Year
    }

    var maxStatisticsItems by rememberPreference(
        maxStatisticsItemsKey,
        MaxStatisticsItems.`10`
    )

    var totalPlayTimes = 0L
    allSongs.forEach {
        totalPlayTimes += it.durationText?.let { it1 ->
            durationTextToMillis(it1)
        }?.toLong() ?: 0
    }

    if (showStatsListeningTime) {
        LaunchedEffect(Unit) {
            Database.songsMostPlayedByPeriod(from, now).collect { allSongs = it }
        }
    }
    LaunchedEffect(Unit) {
        Database.artistsMostPlayedByPeriod(from, now, maxStatisticsItems.number.toInt()).collect { artists = it }
    }
    LaunchedEffect(Unit) {
        Database.albumsMostPlayedByPeriod(from, now, maxStatisticsItems.number.toInt()).collect { albums = it }
    }
    LaunchedEffect(Unit) {
        Database.playlistsMostPlayedByPeriod(from, now, maxStatisticsItems.number.toInt()).collect { playlists = it }
    }
    LaunchedEffect(Unit) {
        Database.songsMostPlayedByPeriod(from, now, maxStatisticsItems.number).collect { songs = it }
    }

    var downloadState by remember {
        mutableStateOf(Download.STATE_STOPPED)
    }


    BoxWithConstraints {
        val quickPicksLazyGridItemWidthFactor = if (isLandscape && maxWidth * 0.475f >= 320.dp) {
            0.475f
        } else {
            0.9f
        }
/*
        val snapLayoutInfoProvider = remember(quickPicksLazyGridState) {
            SnapLayoutInfoProvider(
                lazyGridState = quickPicksLazyGridState,
                positionInLayout = { layoutSize, itemSize ->
                    (layoutSize * quickPicksLazyGridItemWidthFactor / 2f - itemSize / 2f)
                }
            )
        }
*/
        val itemInHorizontalGridWidth = maxWidth * quickPicksLazyGridItemWidthFactor

        Column(
            modifier = Modifier
                .background(colorPalette.background0)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(
                    windowInsets
                        .only(WindowInsetsSides.Vertical)
                        .asPaddingValues()
                )
        ) {

            HeaderWithIcon(
                title = when (statisticsType) {
                    StatisticsType.Today -> stringResource(R.string.today)
                    StatisticsType.OneWeek -> stringResource(R.string._1_week)
                    StatisticsType.OneMonth -> stringResource(R.string._1_month)
                    StatisticsType.ThreeMonths -> stringResource(R.string._3_month)
                    StatisticsType.SixMonths -> stringResource(R.string._6_month)
                    StatisticsType.OneYear -> stringResource(R.string._1_year)
                    StatisticsType.All -> stringResource(R.string.all)
                },
                iconId = R.drawable.stats_chart,
                enabled = true,
                showIcon = true,
                modifier = Modifier,
                onClick = {}
            )

            if (showStatsListeningTime)
            SettingsEntry(
                title = "${allSongs.size} ${stringResource(R.string.statistics_songs_heard)}",
                text = "${formatAsTime(totalPlayTimes)} ${stringResource(R.string.statistics_of_time_taken)}",
                onClick = {},
                trailingContent = {
                    Image(
                        painter = painterResource(R.drawable.musical_notes),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(colorPalette.shimmer),
                        modifier = Modifier
                            .size(34.dp)
                    )
                },
                modifier = Modifier
                    .background(
                        color = colorPalette.background4,
                        shape = thumbnailRoundness.shape()
                    )

            )

            BasicText(
                text = "${maxStatisticsItems} ${stringResource(R.string.most_played_songs)}",
                style = typography.m.semiBold,
                modifier = sectionTextModifier
            )

                LazyHorizontalGrid(
                    state = quickPicksLazyGridState,
                    rows = GridCells.Fixed(2),
                    flingBehavior = ScrollableDefaults.flingBehavior(),
                    contentPadding = endPaddingValues,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((songThumbnailSizeDp + Dimensions.itemsVerticalPadding * 2) * 2)
                ) {

                    items(
                        count = songs.count(),
                        ) {
                        downloadState = getDownloadState(songs.get(it).asMediaItem.mediaId)
                        val isDownloaded = downloadedStateMedia(songs.get(it).asMediaItem.mediaId)
                        SongItem(
                            song = songs.get(it).asMediaItem,
                            isDownloaded = isDownloaded,
                            onDownloadClick = {
                                binder?.cache?.removeResource(songs.get(it).asMediaItem.mediaId)
                                manageDownload(
                                    context = context,
                                    songId = songs.get(it).asMediaItem.mediaId,
                                    songTitle = songs.get(it).asMediaItem.mediaMetadata.title.toString(),
                                    downloadState = isDownloaded
                                )
                            },
                            downloadState = downloadState,
                            thumbnailSizeDp = thumbnailSizeDp,
                            thumbnailSizePx = thumbnailSize,
                            modifier = Modifier
                                .combinedClickable(
                                    onLongClick = {
                                        menuState.display {

                                            //when (builtInPlaylist) {
                                            NonQueuedMediaItemMenu(
                                                mediaItem = songs.get(it).asMediaItem,
                                                onDismiss = menuState::hide
                                            )
                                            /*
                                                BuiltInPlaylist.Offline -> InHistoryMediaItemMenu(
                                                    song = song,
                                                    onDismiss = menuState::hide
                                                )
                                                */
                                            //}

                                        }
                                    },
                                    onClick = {
                                        binder?.stopRadio()
                                        binder?.player?.forcePlayAtIndex(
                                            songs.map(Song::asMediaItem),
                                            it
                                        )
                                    }
                                )
                                .animateItemPlacement()
                                .width(itemInHorizontalGridWidth)
                        )

                    }

                }

            BasicText(
                text = "${maxStatisticsItems} ${stringResource(R.string.most_listened_artists)}",
                style = typography.m.semiBold,
                modifier = sectionTextModifier
            )

            LazyRow(contentPadding = endPaddingValues) {
                items(
                    count = artists.count()
                ) {

                    if(artists[it].thumbnailUrl.toString() == "null")
                        UpdateYoutubeArtist(artists[it].id)

                    ArtistItem(
                        artist = artists[it],
                        thumbnailSizePx = artistThumbnailSizePx,
                        thumbnailSizeDp = artistThumbnailSizeDp,
                        alternative = true,
                        modifier = Modifier
                            .clickable(onClick = {
                                if (artists[it].id != "") {
                                    onGoToArtist(artists[it].id)
                                }
                            })
                    )
                }
            }


            BasicText(
                text = "${maxStatisticsItems} ${stringResource(R.string.most_albums_listened)}",
                style = typography.m.semiBold,
                modifier = sectionTextModifier
            )

            LazyRow(contentPadding = endPaddingValues) {
                items(
                    count = albums.count()
                ) {

                    if(albums[it].thumbnailUrl.toString() == "null")
                        UpdateYoutubeAlbum(albums[it].id)

                    AlbumItem(
                        album = albums[it],
                        thumbnailSizePx = albumThumbnailSizePx,
                        thumbnailSizeDp = albumThumbnailSizeDp,
                        alternative = true,
                        modifier = Modifier
                            .clickable(onClick = {
                                if (albums[it].id != "" )
                                onGoToAlbum(albums[it].id)
                            })
                    )
                }
            }



            BasicText(
                text = "${maxStatisticsItems} ${stringResource(R.string.most_played_playlists)}",
                style = typography.m.semiBold,
                modifier = sectionTextModifier
            )

            LazyRow(contentPadding = endPaddingValues) {
                items(
                    count = playlists.count()
                ) {

                    PlaylistItem(
                        playlist = playlists[it],
                        thumbnailSizePx = playlistThumbnailSizePx,
                        thumbnailSizeDp = playlistThumbnailSizeDp,
                        alternative = true,
                        modifier = Modifier
                            .clickable(onClick = {

                               // if (playlists[it].playlist.browseId != "" )
                                    onGoToPlaylist(playlists[it].playlist.id)
                                 //   onGoToPlaylist(
                                 //       playlists[it].playlist.browseId,
                                 //       null
                                 //   )

                            })
                    )
                }
            }


        }
    }
}
