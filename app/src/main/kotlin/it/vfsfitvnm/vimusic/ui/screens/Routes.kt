package it.vfsfitvnm.vimusic.ui.screens

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import it.vfsfitvnm.compose.routing.Route0
import it.vfsfitvnm.compose.routing.Route1
import it.vfsfitvnm.compose.routing.Route2
import it.vfsfitvnm.compose.routing.Route3
import it.vfsfitvnm.compose.routing.RouteHandlerScope
import it.vfsfitvnm.innertube.Innertube
import it.vfsfitvnm.vimusic.enums.BuiltInPlaylist
import it.vfsfitvnm.vimusic.enums.DeviceLists
import it.vfsfitvnm.vimusic.enums.StatisticsType
import it.vfsfitvnm.vimusic.models.Mood
import it.vfsfitvnm.vimusic.ui.screens.album.AlbumScreen
import it.vfsfitvnm.vimusic.ui.screens.artist.ArtistScreen
import it.vfsfitvnm.vimusic.ui.screens.home.HomeScreen
import it.vfsfitvnm.vimusic.ui.screens.playlist.PlaylistScreen
import it.vfsfitvnm.vimusic.ui.screens.home.QuickPicks
import it.vfsfitvnm.vimusic.ui.screens.localplaylist.LocalPlaylistScreen
import it.vfsfitvnm.vimusic.ui.screens.mood.MoodScreen
import it.vfsfitvnm.vimusic.ui.screens.ondevice.DeviceListSongs
import it.vfsfitvnm.vimusic.ui.screens.ondevice.DeviceListSongsScreen
import it.vfsfitvnm.vimusic.ui.screens.statistics.StatisticsScreen

val quickpicksRoute = Route1<String?>("quickpicksRoute")
val albumRoute = Route1<String?>("albumRoute")
val artistRoute = Route1<String?>("artistRoute")
val builtInPlaylistRoute = Route1<BuiltInPlaylist>("builtInPlaylistRoute")
val deviceListSongRoute = Route1<String>("deviceListSongRoute")
val statisticsTypeRoute = Route1<StatisticsType>("statisticsTypeRoute")
val localPlaylistRoute = Route1<Long?>("localPlaylistRoute")
val searchResultRoute = Route1<String>("searchResultRoute")
val searchRoute = Route1<String>("searchRoute")
val settingsRoute = Route0("settingsRoute")
val homeRoute = Route0("homeRoute")
val moodRoute = Route1<Mood>("moodRoute")
//val playlistRoute = Route1<String?>("playlistRoute")
//val playlistRoute = Route3<String?, String?, Int?>("playlistRoute")
val playlistRoute = Route2<String?, String?>("playlistRoute")

@ExperimentalMaterialApi
@ExperimentalTextApi
@SuppressLint("ComposableNaming")
@Suppress("NOTHING_TO_INLINE")
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@UnstableApi
@Composable
inline fun RouteHandlerScope.globalRoutes() {
    albumRoute { browseId ->
        AlbumScreen(
            browseId = browseId ?: error("browseId cannot be null")
        )
    }

    artistRoute { browseId ->
        ArtistScreen(
            browseId = browseId ?: error("browseId cannot be null")
        )
    }

/*
    playlistRoute { browseId ->
        PlaylistScreen(
        )
    }
 */

    localPlaylistRoute { playlistId ->
        LocalPlaylistScreen(
            playlistId = playlistId ?: error("playlistId cannot be null")
        )
    }


    playlistRoute { browseId, params ->
        PlaylistScreen(
            browseId = browseId ?: error("browseId cannot be null"),
            params = params
        )
    }

    statisticsTypeRoute { browseId ->
        StatisticsScreen(
            statisticsType = browseId ?: error("browseId cannot be null")
        )
    }

    homeRoute {
        HomeScreen(onPlaylistUrl = {pop})
    }

    moodRoute { mood ->
        MoodScreen(mood = mood)
    }


    deviceListSongRoute { browseId ->
        DeviceListSongsScreen(
            deviceLists = DeviceLists.LocalSongs
        )
    }

    quickpicksRoute { browseId ->

    }

}
