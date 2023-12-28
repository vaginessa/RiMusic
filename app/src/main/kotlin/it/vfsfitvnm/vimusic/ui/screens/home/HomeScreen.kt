package it.vfsfitvnm.vimusic.ui.screens.home

import android.util.Log
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import it.vfsfitvnm.compose.persist.PersistMapCleanup
import it.vfsfitvnm.compose.routing.RouteHandler
import it.vfsfitvnm.compose.routing.defaultStacking
import it.vfsfitvnm.compose.routing.defaultStill
import it.vfsfitvnm.compose.routing.defaultUnstacking
import it.vfsfitvnm.compose.routing.isStacking
import it.vfsfitvnm.compose.routing.isUnknown
import it.vfsfitvnm.compose.routing.isUnstacking
import it.vfsfitvnm.vimusic.BuildConfig
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.StatisticsType
import it.vfsfitvnm.vimusic.models.SearchQuery
import it.vfsfitvnm.vimusic.models.toUiMood
import it.vfsfitvnm.vimusic.query
import it.vfsfitvnm.vimusic.ui.components.themed.DefaultDialog
import it.vfsfitvnm.vimusic.ui.components.themed.GenericDialog
import it.vfsfitvnm.vimusic.ui.components.themed.Scaffold
import it.vfsfitvnm.vimusic.ui.screens.albumRoute
import it.vfsfitvnm.vimusic.ui.screens.artistRoute
import it.vfsfitvnm.vimusic.ui.screens.builtInPlaylistRoute
import it.vfsfitvnm.vimusic.ui.screens.builtinplaylist.BuiltInPlaylistScreen
import it.vfsfitvnm.vimusic.ui.screens.deviceListSongRoute
import it.vfsfitvnm.vimusic.ui.screens.globalRoutes
import it.vfsfitvnm.vimusic.ui.screens.localPlaylistRoute
import it.vfsfitvnm.vimusic.ui.screens.localplaylist.LocalPlaylistScreen
import it.vfsfitvnm.vimusic.ui.screens.moodRoute
import it.vfsfitvnm.vimusic.ui.screens.playlist.PlaylistScreen
import it.vfsfitvnm.vimusic.ui.screens.playlistRoute
import it.vfsfitvnm.vimusic.ui.screens.search.SearchScreen
import it.vfsfitvnm.vimusic.ui.screens.searchResultRoute
import it.vfsfitvnm.vimusic.ui.screens.searchRoute
import it.vfsfitvnm.vimusic.ui.screens.searchresult.SearchResultScreen
import it.vfsfitvnm.vimusic.ui.screens.settings.SettingsScreen
import it.vfsfitvnm.vimusic.ui.screens.settingsRoute
import it.vfsfitvnm.vimusic.ui.screens.statisticsTypeRoute
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.shimmer
import it.vfsfitvnm.vimusic.utils.bold
import it.vfsfitvnm.vimusic.utils.homeScreenTabIndexKey
import it.vfsfitvnm.vimusic.utils.isAvailableUpdate
import it.vfsfitvnm.vimusic.utils.pauseSearchHistoryKey
import it.vfsfitvnm.vimusic.utils.preferences
import it.vfsfitvnm.vimusic.utils.rememberPreference
import it.vfsfitvnm.vimusic.utils.semiBold


@ExperimentalTextApi
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@UnstableApi
@Composable
fun HomeScreen(
    onPlaylistUrl: (String) -> Unit
) {
    val (colorPalette, typography) = LocalAppearance.current
    var newVersion = ""
    var showNewversionDialog by remember { mutableStateOf(true) }
    val uriHandler = LocalUriHandler.current

    val saveableStateHolder = rememberSaveableStateHolder()
    //var setDefaultTab = remember { mutableStateOf(true) }
    //val context = LocalContext.current

    PersistMapCleanup("home/")

    RouteHandler(
        listenToGlobalEmitter = true,
        transitionSpec = {
            when {
                isStacking -> defaultStacking
                isUnstacking -> defaultUnstacking
                isUnknown -> when {
                    initialState.route == searchRoute && targetState.route == searchResultRoute -> defaultStacking
                    initialState.route == searchResultRoute && targetState.route == searchRoute -> defaultUnstacking
                    else -> defaultStill
                }

                else -> defaultStill
            }
        }
    ) {
        globalRoutes()

        settingsRoute {
            SettingsScreen()
        }

        localPlaylistRoute { playlistId ->
            LocalPlaylistScreen(
                playlistId = playlistId ?: error("playlistId cannot be null")
            )
        }

        builtInPlaylistRoute { builtInPlaylist ->
            BuiltInPlaylistScreen(
                builtInPlaylist = builtInPlaylist
            )
        }

        playlistRoute { browseId, params ->
            PlaylistScreen(
                browseId = browseId ?: "",
                params = params
            )
        }

        searchResultRoute { query ->
            SearchResultScreen(
                query = query,
                onSearchAgain = {
                    searchRoute(query)
                }
            )
        }

        searchRoute { initialTextInput ->
            val context = LocalContext.current

            SearchScreen(
                initialTextInput = initialTextInput,
                onSearch = { query ->
                    pop()
                    searchResultRoute(query)

                    if (!context.preferences.getBoolean(pauseSearchHistoryKey, false)) {
                        query {
                            Database.insert(SearchQuery(query = query))
                        }
                    }
                },
                onViewPlaylist = onPlaylistUrl
            )
        }

        host {

            var (tabIndex, onTabChanged) = rememberPreference(
                homeScreenTabIndexKey,
                defaultValue = 0
            )

/*
            var (navTabIndex) = rememberPreference(
                indexNavigationTabKey,
                NavigationTab.Default
            )
*/


            //setDefaultTab.value = navTabIndex.index < 100

            //countCall.value = countCall.value.inc()
            //Log.d("routeHome","DefaultTab ${setDefaultTab.value} tabIndex ${tabIndex} navTabIndex ${navTabIndex.index}")
/*
            if (setDefaultTab.value == true) {
                Log.d("routeHomeIfCheck","setHome ${setDefaultTab} tabIndex ${tabIndex} navTabIndex ${navTabIndex.index}")
                tabIndex = navTabIndex.index
                setDefaultTab.value = false
            }
*/

            //Log.d("updatedversion","newVersion $newVersion version ${BuildConfig.VERSION_NAME}")


            Scaffold(
                topIconButtonId = R.drawable.settings, //if (newVersion == "") R.drawable.settings else R.drawable.direct_download,
                onTopIconButtonClick = { settingsRoute() },
                topIconButton2Id = R.drawable.stats_chart,
                onTopIconButton2Click = { statisticsTypeRoute(StatisticsType.Today) },
                showButton2 = true,
                tabIndex = tabIndex,
                onTabChanged = onTabChanged,
                tabColumnContent = { Item ->
                    Item(0, stringResource(R.string.quick_picks), R.drawable.sparkles)
                    Item(1, stringResource(R.string.songs), R.drawable.musical_notes)
                    Item(2, stringResource(R.string.artists), R.drawable.person)
                    Item(3, stringResource(R.string.albums), R.drawable.disc)
                    Item(4, stringResource(R.string.library), R.drawable.library)
                    Item(5, stringResource(R.string.discovery), R.drawable.megaphone)
                    //Item(6, "Equalizer", R.drawable.musical_notes)
                    //Item(6, "Settings", R.drawable.equalizer)
                }
            ) { currentTabIndex ->
                saveableStateHolder.SaveableStateProvider(key = currentTabIndex) {
                    when (currentTabIndex) {
                        0 -> QuickPicks(
                            onAlbumClick = { albumRoute(it) },
                            onArtistClick = { artistRoute(it) },
                            onPlaylistClick = { playlistRoute(it) },
                            onSearchClick = { searchRoute("") }
                        )

                        1 -> HomeSongs(
                            onSearchClick = { searchRoute("") }
                        )

                        2 -> HomeArtistList(
                            onArtistClick = { artistRoute(it.id) },
                            onSearchClick = { searchRoute("") }
                        )

                        3 -> HomeAlbums(
                            onAlbumClick = { albumRoute(it.id) },
                            onSearchClick = { searchRoute("") }
                        )

                        4 -> HomePlaylists(
                            onBuiltInPlaylist = { builtInPlaylistRoute(it) },
                            onPlaylistClick = { localPlaylistRoute(it.id) },
                            onSearchClick = { searchRoute("") },
                            onDeviceListSongsClick = { deviceListSongRoute("") }
                        )

                        5 -> HomeDiscovery(
                            onMoodClick = { mood -> moodRoute(mood.toUiMood()) },
                            onNewReleaseAlbumClick = { albumRoute(it) },
                            onSearchClick = { searchRoute("") }
                        )

                        //6 -> HomeEqualizer( )
                        /*
                        5 -> HomeStatistics(
                            onStatisticsType = { statisticsTypeRoute(it)},
                            onBuiltInPlaylist = { builtInPlaylistRoute(it) },
                            onPlaylistClick = { localPlaylistRoute(it.id) },
                            onSearchClick = { searchRoute("") }
                        )
                         */

                        //6 -> settingsRoute()
                    }
                }
            }
        }
    }

    newVersion =  isAvailableUpdate()

if (showNewversionDialog)
    DefaultDialog(
        onDismiss = { showNewversionDialog = false },
        content = {
            BasicText(
                text = "New version available $newVersion",
                style = typography.s.bold.copy(color = colorPalette.text),
            )
            Spacer(modifier = Modifier.height(20.dp))
            BasicText(
                text = "Click icon to open the release page.",
                style = typography.xs.semiBold.copy(color = colorPalette.textSecondary),
            )
            Spacer(modifier = Modifier.height(10.dp))
            Image(
                painter = painterResource(R.drawable.direct_download),
                contentDescription = null,
                colorFilter = ColorFilter.tint(colorPalette.shimmer),
                modifier = Modifier
                    .size(40.dp)
                    .clickable {
                        showNewversionDialog = false
                        uriHandler.openUri("https://github.com/fast4x/RiMusic/releases")
                    }
            )
        }

    )

}
