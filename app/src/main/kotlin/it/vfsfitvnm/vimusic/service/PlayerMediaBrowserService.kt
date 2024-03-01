package it.vfsfitvnm.vimusic.service

import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.media.MediaBrowserServiceCompat
import androidx.media3.common.Player
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.cache.Cache
import androidx.media3.exoplayer.offline.Download
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.models.Album
import it.vfsfitvnm.vimusic.models.PlaylistPreview
import it.vfsfitvnm.vimusic.models.Song
import it.vfsfitvnm.vimusic.models.SongWithContentLength
import it.vfsfitvnm.vimusic.query
import it.vfsfitvnm.vimusic.transaction
import it.vfsfitvnm.vimusic.utils.asMediaItem
import it.vfsfitvnm.vimusic.utils.forcePlayAtIndex
import it.vfsfitvnm.vimusic.utils.forceSeekToNext
import it.vfsfitvnm.vimusic.utils.forceSeekToPrevious
import it.vfsfitvnm.vimusic.utils.intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class PlayerMediaBrowserService : MediaBrowserServiceCompat(), ServiceConnection {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val coroutineScopeDb = CoroutineScope(Dispatchers.Main)
    private var lastSongs = emptyList<Song>()

    private var bound = false

    override fun onDestroy() {
        if (bound) {
            unbindService(this)
        }
        super.onDestroy()
    }

    @UnstableApi
    override fun onServiceConnected(className: ComponentName, service: IBinder) {
        if (service is PlayerService.Binder) {
            bound = true
            sessionToken = service.mediaSession.sessionToken
            service.mediaSession.setCallback(SessionCallback(service.player, service.cache))
        }
    }

    override fun onServiceDisconnected(name: ComponentName) = Unit

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        bindService(intent<PlayerService>(), this, Context.BIND_AUTO_CREATE)
        return BrowserRoot(
            MediaId.root,
            bundleOf("android.media.browse.CONTENT_STYLE_BROWSABLE_HINT" to 1)
        )
        /*
        return if (clientUid == Process.myUid()
            || clientUid == Process.SYSTEM_UID
            || clientPackageName == "com.google.android.projection.gearhead"
        ) {
            bindService(intent<PlayerService>(), this, Context.BIND_AUTO_CREATE)
            BrowserRoot(
                MediaId.root,
                bundleOf("android.media.browse.CONTENT_STYLE_BROWSABLE_HINT" to 1)
            )
        } else {
            null
        }
         */
    }

     override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaItem>>) {
        runBlocking(Dispatchers.IO) {
            result.sendResult(
                when (parentId) {
                    MediaId.root -> mutableListOf(
                        songsBrowserMediaItem,
                        playlistsBrowserMediaItem,
                        albumsBrowserMediaItem
                    )

                    MediaId.songs -> Database
                        .songsByPlayTimeDesc()
                        .first()
                        .take(30)
                        .also { lastSongs = it }
                        .map { it.asBrowserMediaItem }
                        .toMutableList()
                        .apply {
                            if (isNotEmpty()) add(0, shuffleBrowserMediaItem)
                        }

                    MediaId.playlists -> Database
                        .playlistPreviewsByDateAddedDesc()
                        .first()
                        .map { it.asBrowserMediaItem }
                        .toMutableList()
                        .apply {
                            add(0, favoritesBrowserMediaItem)
                            add(1, offlineBrowserMediaItem)
                            add(2, downloadedBrowserMediaItem)
                        }

                    MediaId.albums -> Database
                        .albumsByRowIdDesc()
                        .first()
                        .map { it.asBrowserMediaItem }
                        .toMutableList()

                    else -> mutableListOf()
                }
            )
        }
    }

    private fun uriFor(@DrawableRes id: Int) = Uri.Builder()
        .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
        .authority(resources.getResourcePackageName(id))
        .appendPath(resources.getResourceTypeName(id))
        .appendPath(resources.getResourceEntryName(id))
        .build()


    private val shuffleBrowserMediaItem
        inline get() = MediaItem(
            MediaDescriptionCompat.Builder()
                .setMediaId(MediaId.shuffle)
                .setTitle((this as Context).resources.getString(R.string.shuffle))
                .setIconUri(uriFor(R.drawable.shuffle))
                .build(),
            MediaItem.FLAG_PLAYABLE
        )

    private val songsBrowserMediaItem
        inline get() = MediaItem(
            MediaDescriptionCompat.Builder()
                .setMediaId(MediaId.songs)
                .setTitle((this as Context).resources.getString(R.string.songs))
                .setIconUri(uriFor(R.drawable.musical_notes))
                .build(),
            MediaItem.FLAG_BROWSABLE
        )


    private val playlistsBrowserMediaItem
        inline get() = MediaItem(
            MediaDescriptionCompat.Builder()
                .setMediaId(MediaId.playlists)
                .setTitle((this as Context).resources.getString(R.string.playlists))
                .setIconUri(uriFor(R.drawable.playlist))
                .build(),
            MediaItem.FLAG_BROWSABLE
        )

    private val albumsBrowserMediaItem
        inline get() = MediaItem(
            MediaDescriptionCompat.Builder()
                .setMediaId(MediaId.albums)
                .setTitle((this as Context).resources.getString(R.string.albums))
                .setIconUri(uriFor(R.drawable.disc))
                .build(),
            MediaItem.FLAG_BROWSABLE
        )

    private val favoritesBrowserMediaItem
        inline get() = MediaItem(
            MediaDescriptionCompat.Builder()
                .setMediaId(MediaId.favorites)
                .setTitle((this as Context).resources.getString(R.string.favorites))
                .setIconUri(uriFor(R.drawable.heart))
                .build(),
            MediaItem.FLAG_PLAYABLE
        )

    private val offlineBrowserMediaItem
        inline get() = MediaItem(
            MediaDescriptionCompat.Builder()
                .setMediaId(MediaId.offline)
                .setTitle((this as Context).resources.getString(R.string.cached))
                .setIconUri(uriFor(R.drawable.airplane))
                .build(),
            MediaItem.FLAG_PLAYABLE
        )

    private val downloadedBrowserMediaItem
        inline get() = MediaItem(
            MediaDescriptionCompat.Builder()
                .setMediaId(MediaId.downloaded)
                .setTitle((this as Context).resources.getString(R.string.downloaded))
                .setIconUri(uriFor(R.drawable.downloaded))
                .build(),
            MediaItem.FLAG_PLAYABLE
        )

    private val Song.asBrowserMediaItem
        inline get() = MediaItem(
            MediaDescriptionCompat.Builder()
                .setMediaId(MediaId.forSong(id))
                .setTitle(title)
                .setSubtitle(artistsText)
                .setIconUri(thumbnailUrl?.toUri())
                .build(),
            MediaItem.FLAG_PLAYABLE
        )

    private val PlaylistPreview.asBrowserMediaItem
        inline get() = MediaItem(
            MediaDescriptionCompat.Builder()
                .setMediaId(MediaId.forPlaylist(playlist.id))
                .setTitle(playlist.name)
                .setSubtitle("$songCount songs")
                .setIconUri(uriFor(R.drawable.playlist))
                .build(),
            MediaItem.FLAG_PLAYABLE
        )

    private val Album.asBrowserMediaItem
        inline get() = MediaItem(
            MediaDescriptionCompat.Builder()
                .setMediaId(MediaId.forAlbum(id))
                .setTitle(title)
                .setSubtitle(authorsText)
                .setIconUri(thumbnailUrl?.toUri())
                .build(),
            MediaItem.FLAG_PLAYABLE
        )

    private inner class SessionCallback(private val player: Player, private val cache: Cache) :
        MediaSessionCompat.Callback() {
        override fun onPlay() = player.play()
        override fun onPause() = player.pause()
        override fun onSkipToPrevious() = player.forceSeekToPrevious()
        override fun onSkipToNext() = player.forceSeekToNext()
        override fun onSeekTo(pos: Long) = player.seekTo(pos)
        override fun onSkipToQueueItem(id: Long) = player.seekToDefaultPosition(id.toInt())

        @FlowPreview
        @ExperimentalCoroutinesApi
        @UnstableApi
        override fun onCustomAction(action: String?, extras: Bundle?) {
            super.onCustomAction(action, extras)
            if (action == "LIKE") {}
            if (action == "DOWNLOAD") {}
        }

        @UnstableApi
        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            val data = mediaId?.split('/') ?: return
            var index = 0

            coroutineScope.launch {
                val mediaItems = when (data.getOrNull(0)) {
                    MediaId.shuffle -> lastSongs

                    MediaId.songs ->  data
                        .getOrNull(1)
                        ?.let { songId ->
                            index = lastSongs.indexOfFirst { it.id == songId }
                            lastSongs
                        }

                    MediaId.favorites -> Database
                        .favorites()
                        .first()
                        .shuffled()

                    MediaId.offline -> Database
                        .songsWithContentLength()
                        .first()
                        .filter { song ->
                            song.contentLength?.let {
                                cache.isCached(song.song.id, 0, it)
                            } ?: false
                        }
                        .map(SongWithContentLength::song)
                        .shuffled()

                    MediaId.downloaded -> {
                        val downloads = DownloadUtil.downloads.value
                        Database.listAllSongs()
                             .filter {
                                    downloads[it.id]?.state == Download.STATE_COMPLETED
                             }
                    }

                    MediaId.playlists -> data
                        .getOrNull(1)
                        ?.toLongOrNull()
                        ?.let(Database::playlistWithSongs)
                        ?.first()
                        ?.songs
                        ?.shuffled()

                    MediaId.albums -> data
                        .getOrNull(1)
                        ?.let(Database::albumSongs)
                        ?.first()

                    else -> emptyList()
                }?.map(Song::asMediaItem) ?: return@launch

                withContext(Dispatchers.Main) {
                    player.forcePlayAtIndex(mediaItems, index.coerceIn(0, mediaItems.size))
                }
            }
        }
    }

    private object MediaId {
        const val root = "root"
        const val songs = "songs"
        const val playlists = "playlists"
        const val albums = "albums"

        const val favorites = "favorites"
        const val offline = "offline"
        const val shuffle = "shuffle"
        const val downloaded = "downloaded"

        fun forSong(id: String) = "songs/$id"
        fun forPlaylist(id: Long) = "playlists/$id"
        fun forAlbum(id: String) = "albums/$id"
    }
}
