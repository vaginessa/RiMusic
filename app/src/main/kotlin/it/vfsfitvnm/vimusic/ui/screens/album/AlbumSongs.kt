package it.vfsfitvnm.vimusic.ui.screens.album

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.style.TextOverflow
import androidx.media3.common.MediaItem
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import it.vfsfitvnm.compose.persist.persist
import it.vfsfitvnm.compose.persist.persistList
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerAwareWindowInsets
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.PlaylistSortBy
import it.vfsfitvnm.vimusic.enums.SortOrder
import it.vfsfitvnm.vimusic.enums.UiType
import it.vfsfitvnm.vimusic.models.Album
import it.vfsfitvnm.vimusic.models.Info
import it.vfsfitvnm.vimusic.models.Playlist
import it.vfsfitvnm.vimusic.models.Song
import it.vfsfitvnm.vimusic.models.SongPlaylistMap
import it.vfsfitvnm.vimusic.query
import it.vfsfitvnm.vimusic.service.isLocal
import it.vfsfitvnm.vimusic.transaction
import it.vfsfitvnm.vimusic.ui.components.LocalMenuState
import it.vfsfitvnm.vimusic.ui.components.ShimmerHost
import it.vfsfitvnm.vimusic.ui.components.themed.ConfirmationDialog
import it.vfsfitvnm.vimusic.ui.components.themed.CustomDialog
import it.vfsfitvnm.vimusic.ui.components.themed.FloatingActionsContainerWithScrollToTop
import it.vfsfitvnm.vimusic.ui.components.themed.HeaderIconButton
import it.vfsfitvnm.vimusic.ui.components.themed.InputTextDialog
import it.vfsfitvnm.vimusic.ui.components.themed.LayoutWithAdaptiveThumbnail
import it.vfsfitvnm.vimusic.ui.components.themed.NonQueuedMediaItemMenu
import it.vfsfitvnm.vimusic.ui.components.themed.SelectorDialog
import it.vfsfitvnm.vimusic.ui.components.themed.TextFieldDialog
import it.vfsfitvnm.vimusic.ui.items.SongItem
import it.vfsfitvnm.vimusic.ui.items.SongItemPlaceholder
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.utils.UiTypeKey
import it.vfsfitvnm.vimusic.utils.asMediaItem
import it.vfsfitvnm.vimusic.utils.center
import it.vfsfitvnm.vimusic.utils.color
import it.vfsfitvnm.vimusic.utils.downloadedStateMedia
import it.vfsfitvnm.vimusic.utils.durationTextToMillis
import it.vfsfitvnm.vimusic.utils.enqueue
import it.vfsfitvnm.vimusic.utils.forcePlayAtIndex
import it.vfsfitvnm.vimusic.utils.forcePlayFromBeginning
import it.vfsfitvnm.vimusic.utils.formatAsTime
import it.vfsfitvnm.vimusic.utils.getDownloadState
import it.vfsfitvnm.vimusic.utils.isLandscape
import it.vfsfitvnm.vimusic.utils.manageDownload
import it.vfsfitvnm.vimusic.utils.medium
import it.vfsfitvnm.vimusic.utils.rememberPreference
import it.vfsfitvnm.vimusic.utils.semiBold
import it.vfsfitvnm.vimusic.utils.toast
import kotlinx.coroutines.Dispatchers
@ExperimentalTextApi
@SuppressLint("SuspiciousIndentation")
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@UnstableApi
@Composable
fun AlbumSongs(
    browseId: String,
    headerContent: @Composable (textButton: (@Composable () -> Unit)?) -> Unit,
    thumbnailContent: @Composable () -> Unit,
) {
    val (colorPalette, typography) = LocalAppearance.current
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current
    val uiType  by rememberPreference(UiTypeKey, UiType.RiMusic)

    var songs by persistList<Song>("album/$browseId/songs")
    var album by persist<Album?>("album/$browseId")

    LaunchedEffect(Unit) {
        Database.albumSongs(browseId).collect { songs = it }
    }
    LaunchedEffect(Unit) {
        Database.album(browseId).collect { album = it }
    }

    val playlistPreviews by remember {
        Database.playlistPreviews(PlaylistSortBy.Name, SortOrder.Ascending)
    }.collectAsState(initial = emptyList(), context = Dispatchers.IO)

    var showPlaylistSelectDialog by remember {
        mutableStateOf(false)
    }

    var showConfirmDeleteDownloadDialog by remember {
        mutableStateOf(false)
    }

    var showConfirmDownloadAllDialog by remember {
        mutableStateOf(false)
    }

    val thumbnailSizeDp = Dimensions.thumbnails.song

    val lazyListState = rememberLazyListState()

    val context = LocalContext.current
    var downloadState by remember {
        mutableStateOf(Download.STATE_STOPPED)
    }

    var listMediaItems = remember {
        mutableListOf<MediaItem>()
    }

    var selectItems by remember {
        mutableStateOf(false)
    }

    var showSelectDialog by remember {
        mutableStateOf(false)
    }

    var showAddPlaylistSelectDialog by remember {
        mutableStateOf(false)
    }

    var showSelectCustomizeAlbumDialog by remember {
        mutableStateOf(false)
    }
    var showDialogChangeAlbumTitle by remember {
        mutableStateOf(false)
    }
    var showDialogChangeAlbumAuthors by remember {
        mutableStateOf(false)
    }
    var showDialogChangeAlbumCover by remember {
        mutableStateOf(false)
    }
    var isCreatingNewPlaylist by rememberSaveable {
        mutableStateOf(false)
    }
    var totalPlayTimes = 0L
    songs.forEach {
        totalPlayTimes += it.durationText?.let { it1 ->
            durationTextToMillis(it1) }?.toLong() ?: 0
    }

    LayoutWithAdaptiveThumbnail(thumbnailContent = thumbnailContent) {
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
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        headerContent {

                            HeaderIconButton(
                                icon = R.drawable.downloaded,
                                color = colorPalette.text,
                                onClick = {
                                    showConfirmDownloadAllDialog = true
                                }
                            )

                            if (isCreatingNewPlaylist)
                                InputTextDialog(
                                    onDismiss = { isCreatingNewPlaylist = false },
                                    title = stringResource(R.string.new_playlist),
                                    value = "",
                                    placeholder = stringResource(R.string.new_playlist),
                                    setValue = {
                                        if (it.isNotEmpty()) {
                                            query {
                                                Database.insert(Playlist(name = it))
                                            }
                                            //context.toast("Song Saved $it")
                                        }
                                    }
                                )

                            if (showConfirmDownloadAllDialog) {
                                ConfirmationDialog(
                                    text = stringResource(R.string.do_you_really_want_to_download_all),
                                    onDismiss = { showConfirmDownloadAllDialog = false },
                                    onConfirm = {
                                        showConfirmDownloadAllDialog = false
                                        downloadState = Download.STATE_DOWNLOADING
                                        if (songs.isNotEmpty() == true)
                                            songs.forEach {
                                                binder?.cache?.removeResource(it.asMediaItem.mediaId)
                                                query {
                                                    Database.insert(
                                                        Song(
                                                            id = it.asMediaItem.mediaId,
                                                            title = it.asMediaItem.mediaMetadata.title.toString(),
                                                            artistsText = it.asMediaItem.mediaMetadata.artist.toString(),
                                                            thumbnailUrl = it.thumbnailUrl,
                                                            durationText = null
                                                        )
                                                    )
                                                }
                                                manageDownload(
                                                    context = context,
                                                    songId = it.asMediaItem.mediaId,
                                                    songTitle = it.asMediaItem.mediaMetadata.title.toString(),
                                                    downloadState = false
                                                )
                                            }
                                    }
                                )
                            }

                            HeaderIconButton(
                                icon = R.drawable.download,
                                color = colorPalette.text,
                                onClick = {
                                    showConfirmDeleteDownloadDialog = true
                                }
                            )

                            if (showConfirmDeleteDownloadDialog) {
                                ConfirmationDialog(
                                    text = stringResource(R.string.do_you_really_want_to_delete_download),
                                    onDismiss = { showConfirmDeleteDownloadDialog = false },
                                    onConfirm = {
                                        showConfirmDeleteDownloadDialog = false
                                        downloadState = Download.STATE_DOWNLOADING
                                        if (songs.isNotEmpty() == true)
                                            songs.forEach {
                                                binder?.cache?.removeResource(it.asMediaItem.mediaId)
                                                manageDownload(
                                                    context = context,
                                                    songId = it.asMediaItem.mediaId,
                                                    songTitle = it.asMediaItem.mediaMetadata.title.toString(),
                                                    downloadState = true
                                                )
                                            }
                                    }
                                )
                            }

                            HeaderIconButton(
                                icon = R.drawable.enqueue,
                                enabled = songs.isNotEmpty(),
                                color = if (songs.isNotEmpty()) colorPalette.text else colorPalette.textDisabled,
                                onClick = {
                                    if (!selectItems)
                                    showSelectDialog = true else {
                                        binder?.player?.enqueue(listMediaItems)
                                        listMediaItems.clear()
                                        selectItems = false
                                    }

                                }
                            )



                            HeaderIconButton(
                                icon = R.drawable.shuffle,
                                enabled = songs.isNotEmpty(),
                                color = if (songs.isNotEmpty()) colorPalette.text else colorPalette.textDisabled,
                                onClick = {
                                    if (songs.isNotEmpty()) {
                                        binder?.stopRadio()
                                        binder?.player?.forcePlayFromBeginning(
                                            songs.shuffled().map(Song::asMediaItem)
                                        )
                                    }
                                }
                            )

                            HeaderIconButton(
                                icon = R.drawable.add,
                                enabled = songs.isNotEmpty(),
                                color = if (songs.isNotEmpty()) colorPalette.text else colorPalette.textDisabled,
                                onClick = {
                                    if (!selectItems)
                                        showAddPlaylistSelectDialog = true  else
                                        showPlaylistSelectDialog = true
                                }
                            )

                            if (showAddPlaylistSelectDialog)
                                SelectorDialog(
                                    title = stringResource(R.string.playlists),
                                    onDismiss = { showAddPlaylistSelectDialog = false },
                                    values = listOf(
                                        Info("n", stringResource(R.string.new_playlist)),
                                        Info("a", stringResource(R.string.add_all_in_playlist)),
                                        Info("s", stringResource(R.string.add_selected_in_playlist))
                                    ),
                                    onValueSelected = {
                                        when (it) {
                                            "a" -> showPlaylistSelectDialog = true
                                            "n" -> isCreatingNewPlaylist = true
                                            else -> selectItems = true
                                        }
                                        showAddPlaylistSelectDialog = false
                                    }
                                )


                            if (showPlaylistSelectDialog) {

                                SelectorDialog(
                                    title = stringResource(R.string.playlists),
                                    onDismiss = { showPlaylistSelectDialog = false },
                                    showItemsIcon = true,
                                    values = playlistPreviews.map {
                                        Info(
                                            it.playlist.id.toString(),
                                            "${it.playlist.name} \n ${it.songCount} ${stringResource(R.string.songs)}"
                                        )
                                    },
                                    onValueSelected = {
                                        var position = 0
                                        query {
                                            position =
                                                Database.getSongMaxPositionToPlaylist(it.toLong())
                                            //Log.d("mediaItemMaxPos", position.toString())
                                        }
                                        if (position > 0) position++
                                        if (listMediaItems.isEmpty()) {
                                        songs.forEachIndexed { position, song ->
                                            //Log.d("mediaItemMaxPos", position.toString())
                                            transaction {
                                                Database.insert(song.asMediaItem)
                                                Database.insert(
                                                    SongPlaylistMap(
                                                        songId = song.asMediaItem.mediaId,
                                                        playlistId = it.toLong(),
                                                        position = position
                                                    )
                                                )
                                            }
                                            //Log.d("mediaItemPos", "add position $position")
                                        }
                                    } else {
                                            listMediaItems.forEachIndexed { position, song ->
                                                //Log.d("mediaItemMaxPos", position.toString())
                                                transaction {
                                                    Database.insert(song)
                                                    Database.insert(
                                                        SongPlaylistMap(
                                                            songId = song.mediaId,
                                                            playlistId = it.toLong(),
                                                            position = position
                                                        )
                                                    )
                                                }
                                                //Log.d("mediaItemPos", "add position $position")
                                            }
                                            listMediaItems.clear()
                                            selectItems = false
                                    }
                                        showPlaylistSelectDialog = false
                                    }
                                )
                            }

                            if (showSelectDialog)
                                SelectorDialog(
                                    title = stringResource(R.string.enqueue),
                                    onDismiss = { showSelectDialog = false },
                                    values = listOf(
                                        Info("a", stringResource(R.string.enqueue_all)),
                                        Info("s", stringResource(R.string.enqueue_selected))
                                    ),
                                    onValueSelected = {
                                        if (it == "a") {
                                            binder?.player?.enqueue(songs.map(Song::asMediaItem))
                                        } else selectItems = true

                                        showSelectDialog = false
                                    }
                                )

                            HeaderIconButton(
                                icon = R.drawable.pencil,
                                color = colorPalette.text,
                                onClick = {
                                    showSelectCustomizeAlbumDialog = true
                                }
                            )

                            if (showSelectCustomizeAlbumDialog)
                                SelectorDialog(
                                    title = stringResource(R.string.customize_album),
                                    onDismiss = { showSelectCustomizeAlbumDialog = false },
                                    values = listOf(
                                        Info("t", stringResource(R.string.update_title)),
                                        Info("a", stringResource(R.string.update_authors)),
                                        Info("c", stringResource(R.string.update_cover))
                                    ),
                                    onValueSelected = {
                                        when (it) {
                                            "t" -> showDialogChangeAlbumTitle = true
                                            "a" -> showDialogChangeAlbumAuthors = true
                                            "c" -> showDialogChangeAlbumCover = true
                                        }
                                        showSelectCustomizeAlbumDialog = false
                                    }
                                )

                            if (showDialogChangeAlbumTitle)
                                InputTextDialog(
                                    onDismiss = { showDialogChangeAlbumTitle = false },
                                    title = stringResource(R.string.update_title),
                                    value = album?.title.toString(),
                                    placeholder = stringResource(R.string.title),
                                    setValue = {
                                        if (it.isNotEmpty()) {
                                            query {
                                                Database.updateAlbumTitle(browseId, it)
                                            }
                                            //context.toast("Album Saved $it")
                                        }
                                    }
                                )
                            if (showDialogChangeAlbumAuthors)
                                InputTextDialog(
                                    onDismiss = { showDialogChangeAlbumAuthors = false },
                                    title = stringResource(R.string.update_authors),
                                    value = album?.authorsText.toString(),
                                    placeholder = stringResource(R.string.authors),
                                    setValue = {
                                        if (it.isNotEmpty()) {
                                            query {
                                                Database.updateAlbumAuthors(browseId, it)
                                            }
                                            //context.toast("Album Saved $it")
                                        }
                                    }
                                )

                            if (showDialogChangeAlbumCover)
                                InputTextDialog(
                                    onDismiss = { showDialogChangeAlbumCover = false },
                                    title = stringResource(R.string.update_cover),
                                    value = album?.thumbnailUrl.toString(),
                                    placeholder = stringResource(R.string.cover),
                                    setValue = {
                                        if (it.isNotEmpty()) {
                                            query {
                                                Database.updateAlbumCover(browseId, it)
                                            }
                                            //context.toast("Album Saved $it")
                                        }
                                    }
                                )

                        }

                        if (!isLandscape) {
                            thumbnailContent()
                        }

                        album?.title?.let {
                            BasicText(
                                text = it,
                                style = typography.xs.semiBold,
                                maxLines = 1
                            )
                        }
                        BasicText(
                            text = songs.size.toString() + " "
                                    +stringResource(R.string.songs)
                                    + " - " + formatAsTime(totalPlayTimes),
                            style = typography.xxs.medium,
                            maxLines = 1
                        )
                    }
                }

                itemsIndexed(
                    items = songs,
                    key = { _, song -> song.id }
                ) { index, song ->
                    val isLocal by remember { derivedStateOf { song.asMediaItem.isLocal } }
                    downloadState = getDownloadState(song.asMediaItem.mediaId)
                    val isDownloaded = if (!isLocal) downloadedStateMedia(song.asMediaItem.mediaId) else true
                    SongItem(
                        title = song.title,
                        isDownloaded = isDownloaded,
                        downloadState = downloadState,
                        onDownloadClick = {
                            binder?.cache?.removeResource(song.asMediaItem.mediaId)
                            query {
                                Database.insert(
                                    Song(
                                        id = song.asMediaItem.mediaId,
                                        title = song.asMediaItem.mediaMetadata.title.toString(),
                                        artistsText = song.asMediaItem.mediaMetadata.artist.toString(),
                                        thumbnailUrl = song.thumbnailUrl,
                                        durationText = null
                                    )
                                )
                            }
                            if (!isLocal)
                            manageDownload(
                                context = context,
                                songId = song.asMediaItem.mediaId,
                                songTitle = song.asMediaItem.mediaMetadata.title.toString(),
                                downloadState = isDownloaded
                            )
                        },
                        authors = song.artistsText,
                        duration = song.durationText,
                        thumbnailSizeDp = thumbnailSizeDp,
                        thumbnailContent = {
                            BasicText(
                                text = "${index + 1}",
                                style = typography.s.semiBold.center.color(colorPalette.textDisabled),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .width(thumbnailSizeDp)
                                    .align(Alignment.Center)
                            )
                        },
                        modifier = Modifier
                            .combinedClickable(
                                onLongClick = {
                                    menuState.display {
                                        NonQueuedMediaItemMenu(
                                            onDismiss = menuState::hide,
                                            mediaItem = song.asMediaItem,
                                        )
                                    }
                                },
                                onClick = {
                                    binder?.stopRadio()
                                    binder?.player?.forcePlayAtIndex(
                                        songs.map(Song::asMediaItem),
                                        index
                                    )
                                }
                            ),
                            trailingContent = {
                                val checkedState = remember { mutableStateOf(false) }
                                if (selectItems)
                                    Checkbox(
                                        checked = checkedState.value,
                                        onCheckedChange = {
                                            checkedState.value = it
                                            if (it) listMediaItems.add(song.asMediaItem) else
                                                listMediaItems.remove(song.asMediaItem)
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = colorPalette.accent,
                                            uncheckedColor = colorPalette.text
                                        )
                                    )
                            }
                    )
                }

                if (songs.isEmpty()) {
                    item(key = "loading") {
                        ShimmerHost(
                            modifier = Modifier
                                .fillParentMaxSize()
                        ) {
                            repeat(4) {
                                SongItemPlaceholder(thumbnailSizeDp = Dimensions.thumbnails.song)
                            }
                        }
                    }
                }
            }

            if(uiType == UiType.ViMusic)
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
}
