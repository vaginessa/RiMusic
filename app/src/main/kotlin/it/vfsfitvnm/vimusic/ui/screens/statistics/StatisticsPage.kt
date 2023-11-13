package it.vfsfitvnm.vimusic.ui.screens.statistics

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import it.vfsfitvnm.compose.persist.persistList
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerAwareWindowInsets
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.StatisticsType
import it.vfsfitvnm.vimusic.models.Album
import it.vfsfitvnm.vimusic.models.Artist
import it.vfsfitvnm.vimusic.models.PlaylistPreview
import it.vfsfitvnm.vimusic.models.Song
import it.vfsfitvnm.vimusic.ui.components.LocalMenuState
import it.vfsfitvnm.vimusic.ui.components.themed.HalfHeader
import it.vfsfitvnm.vimusic.ui.components.themed.HeaderWithIcon
import it.vfsfitvnm.vimusic.ui.components.themed.NonQueuedMediaItemMenu
import it.vfsfitvnm.vimusic.ui.items.AlbumItem
import it.vfsfitvnm.vimusic.ui.items.ArtistItem
import it.vfsfitvnm.vimusic.ui.items.PlaylistItem
import it.vfsfitvnm.vimusic.ui.items.SongItem
import it.vfsfitvnm.vimusic.ui.screens.albumRoute
import it.vfsfitvnm.vimusic.ui.screens.artistRoute
import it.vfsfitvnm.vimusic.ui.screens.playlistRoute
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.px
import it.vfsfitvnm.vimusic.utils.SnapLayoutInfoProvider
import it.vfsfitvnm.vimusic.utils.asMediaItem
import it.vfsfitvnm.vimusic.utils.downloadedStateMedia
import it.vfsfitvnm.vimusic.utils.forcePlayAtIndex
import it.vfsfitvnm.vimusic.utils.isLandscape
import it.vfsfitvnm.vimusic.utils.semiBold
import java.time.LocalDateTime
import java.time.ZoneOffset

@SuppressLint("SuspiciousIndentation")
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@UnstableApi
@Composable
fun StatisticsPage(
    statisticsType: StatisticsType
) {
    val onGoToArtist = artistRoute::global
    val onGoToAlbum = albumRoute::global
    val onGoToPlaylist = playlistRoute::global

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

    val context = LocalContext.current

    val thumbnailSizeDp = Dimensions.thumbnails.song
    val thumbnailSize = thumbnailSizeDp.px

    var songs by persistList<Song>("statistics/songs")
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

    var from = when (statisticsType) {
        StatisticsType.Today -> today
        StatisticsType.OneWeek -> lastWeek
        StatisticsType.OneMonth -> lastMonth
        StatisticsType.ThreeMonths -> last3Month
        StatisticsType.SixMonths -> last6Month
        StatisticsType.OneYear -> lastYear
        StatisticsType.All -> last20Year
    }

    LaunchedEffect(Unit) {
        Database.songsMostPlayedByPeriod(from, now, 6).collect { songs = it }
    }
    LaunchedEffect(Unit) {
        Database.artistsMostPlayedByPeriod(from, now, 6).collect { artists = it }
    }
    LaunchedEffect(Unit) {
        Database.albumsMostPlayedByPeriod(from, now, 6).collect { albums = it }
    }
    LaunchedEffect(Unit) {
        Database.playlistsMostPlayedByPeriod(from, now, 6).collect { playlists = it }
    }



    BoxWithConstraints {
        val quickPicksLazyGridItemWidthFactor = if (isLandscape && maxWidth * 0.475f >= 320.dp) {
            0.475f
        } else {
            0.9f
        }

        val snapLayoutInfoProvider = remember(quickPicksLazyGridState) {
            SnapLayoutInfoProvider(
                lazyGridState = quickPicksLazyGridState,
                positionInLayout = { layoutSize, itemSize ->
                    (layoutSize * quickPicksLazyGridItemWidthFactor / 2f - itemSize / 2f)
                }
            )
        }

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
                iconId = R.drawable.stats,
                enabled = true,
                showIcon = true,
                modifier = Modifier,
                onClick = {}
            )


            BasicText(
                text = stringResource(R.string.most_played_songs),
                style = typography.m.semiBold,
                modifier = sectionTextModifier
            )

                LazyHorizontalGrid(
                    state = quickPicksLazyGridState,
                    rows = GridCells.Fixed(2),
                    flingBehavior = rememberSnapFlingBehavior(snapLayoutInfoProvider),
                    contentPadding = endPaddingValues,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((songThumbnailSizeDp + Dimensions.itemsVerticalPadding * 2) * 2)
                ) {

                    items(
                        count = songs.count(),
                        ) {
                        SongItem(
                            song = songs.get(it).asMediaItem,
                            isDownloaded = downloadedStateMedia(songs.get(it).asMediaItem.mediaId),
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
                        )

                    }

                }

            BasicText(
                text = stringResource(R.string.most_listened_artists),
                style = typography.m.semiBold,
                modifier = sectionTextModifier
            )

            LazyRow(contentPadding = endPaddingValues) {
                items(
                    count = artists.count()
                ) {
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
                text = stringResource(R.string.most_albums_listened),
                style = typography.m.semiBold,
                modifier = sectionTextModifier
            )

            LazyRow(contentPadding = endPaddingValues) {
                items(
                    count = albums.count()
                ) {
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
                text = stringResource(R.string.most_played_playlists),
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
                                if (playlists[it].playlist.browseId != "" )
                                    //onGoToPlaylist(playlists[it].playlist.browseId)
                                    onGoToPlaylist(
                                        playlists[it].playlist.browseId,
                                        null
                                    )

                            })
                    )
                }
            }


        }
    }
}