package it.vfsfitvnm.vimusic.ui.screens.playlist

import android.annotation.SuppressLint
import android.content.Intent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import com.valentinilk.shimmer.shimmer
import it.vfsfitvnm.compose.persist.persist
import it.vfsfitvnm.innertube.Innertube
import it.vfsfitvnm.innertube.models.bodies.BrowseBody
import it.vfsfitvnm.innertube.requests.playlistPage
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerAwareWindowInsets
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.PlaylistSortBy
import it.vfsfitvnm.vimusic.enums.SortOrder
import it.vfsfitvnm.vimusic.enums.ThumbnailRoundness
import it.vfsfitvnm.vimusic.enums.UiType
import it.vfsfitvnm.vimusic.models.Info
import it.vfsfitvnm.vimusic.models.Playlist
import it.vfsfitvnm.vimusic.models.Song
import it.vfsfitvnm.vimusic.models.SongPlaylistMap
import it.vfsfitvnm.vimusic.query
import it.vfsfitvnm.vimusic.service.isLocal
import it.vfsfitvnm.vimusic.transaction
import it.vfsfitvnm.vimusic.ui.components.LocalMenuState
import it.vfsfitvnm.vimusic.ui.components.ShimmerHost
import it.vfsfitvnm.vimusic.ui.components.themed.FloatingActionsContainerWithScrollToTop
import it.vfsfitvnm.vimusic.ui.components.themed.HeaderIconButton
import it.vfsfitvnm.vimusic.ui.components.themed.HeaderInfo
import it.vfsfitvnm.vimusic.ui.components.themed.HeaderPlaceholder
import it.vfsfitvnm.vimusic.ui.components.themed.HeaderWithIcon
import it.vfsfitvnm.vimusic.ui.components.themed.LayoutWithAdaptiveThumbnail
import it.vfsfitvnm.vimusic.ui.components.themed.NonQueuedMediaItemMenu
import it.vfsfitvnm.vimusic.ui.components.themed.SelectorDialog
import it.vfsfitvnm.vimusic.ui.components.themed.TextFieldDialog
import it.vfsfitvnm.vimusic.ui.components.themed.adaptiveThumbnailContent
import it.vfsfitvnm.vimusic.ui.items.SongItem
import it.vfsfitvnm.vimusic.ui.items.SongItemPlaceholder
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.px
import it.vfsfitvnm.vimusic.utils.UiTypeKey
import it.vfsfitvnm.vimusic.utils.asMediaItem
import it.vfsfitvnm.vimusic.utils.completed
import it.vfsfitvnm.vimusic.utils.downloadedStateMedia
import it.vfsfitvnm.vimusic.utils.enqueue
import it.vfsfitvnm.vimusic.utils.forcePlayAtIndex
import it.vfsfitvnm.vimusic.utils.forcePlayFromBeginning
import it.vfsfitvnm.vimusic.utils.getDownloadState
import it.vfsfitvnm.vimusic.utils.isLandscape
import it.vfsfitvnm.vimusic.utils.manageDownload
import it.vfsfitvnm.vimusic.utils.rememberPreference
import it.vfsfitvnm.vimusic.utils.secondary
import it.vfsfitvnm.vimusic.utils.semiBold
import it.vfsfitvnm.vimusic.utils.thumbnailRoundnessKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@ExperimentalTextApi
@SuppressLint("SuspiciousIndentation")
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@UnstableApi
@Composable
fun PlaylistSongList(
    browseId: String,
    params: String?
) {
    val (colorPalette, typography) = LocalAppearance.current
    val binder = LocalPlayerServiceBinder.current
    val context = LocalContext.current
    val menuState = LocalMenuState.current
    val uiType  by rememberPreference(UiTypeKey, UiType.RiMusic)

    var playlistPage by persist<Innertube.PlaylistOrAlbumPage?>("playlist/$browseId/playlistPage")

    var filter: String? by rememberSaveable { mutableStateOf(null) }

    LaunchedEffect(Unit, filter) {
        //if (playlistPage != null && playlistPage?.songsPage?.continuation == null) return@LaunchedEffect

        playlistPage = withContext(Dispatchers.IO) {
            Innertube
                .playlistPage(BrowseBody(browseId = browseId, params = params))
                ?.completed()
                ?.getOrNull()
        }
        //Log.d("mediaPlaylist", "${playlistPage?.title} songs ${playlistPage?.songsPage?.items?.size} continuation ${playlistPage?.songsPage?.continuation}")

/*
                playlistPage = withContext(Dispatchers.IO) {
                    Innertube.playlistPage(BrowseBody(browseId = browseId, params = params))
                        ?.completed(maxDepth = maxDepth ?: Int.MAX_VALUE)?.getOrNull()
                }

 */
/*
        playlistPage = withContext(Dispatchers.IO) {
            Innertube.playlistPage(BrowseBody(browseId = browseId))?.completed()?.getOrNull()
        }
*/
    }

    var filterCharSequence: CharSequence
    filterCharSequence = filter.toString()
    //Log.d("mediaItemFilter", "<${filter}>  <${filterCharSequence}>")
    if (!filter.isNullOrBlank())
        playlistPage?.songsPage?.items =
        playlistPage?.songsPage?.items?.filter {songItem ->
                songItem.asMediaItem.mediaMetadata.title?.contains(filterCharSequence,true) ?: false
                        || songItem.asMediaItem.mediaMetadata.artist?.contains(filterCharSequence,true) ?: false
            }

    val songThumbnailSizeDp = Dimensions.thumbnails.song
    val songThumbnailSizePx = songThumbnailSizeDp.px

    var isImportingPlaylist by rememberSaveable {
        mutableStateOf(false)
    }

    var downloadState by remember {
        mutableStateOf(Download.STATE_STOPPED)
    }

    var thumbnailRoundness by rememberPreference(
        thumbnailRoundnessKey,
        ThumbnailRoundness.Heavy
    )

    var showAddPlaylistSelectDialog by remember {
        mutableStateOf(false)
    }

    val playlistPreviews by remember {
        Database.playlistPreviews(PlaylistSortBy.Name, SortOrder.Ascending)
    }.collectAsState(initial = emptyList(), context = Dispatchers.IO)

    var showPlaylistSelectDialog by remember {
        mutableStateOf(false)
    }

    if (isImportingPlaylist) {
        TextFieldDialog(
            hintText = stringResource(R.string.enter_the_playlist_name),
            initialTextInput = playlistPage?.title ?: "",
            onDismiss = { isImportingPlaylist = false },
            onDone = { text ->
                query {
                    transaction {
                        val playlistId = Database.insert(Playlist(name = text, browseId = browseId))

                        playlistPage?.songsPage?.items
                            ?.map(Innertube.SongItem::asMediaItem)
                            ?.onEach(Database::insert)
                            ?.mapIndexed { index, mediaItem ->
                                SongPlaylistMap(
                                    songId = mediaItem.mediaId,
                                    playlistId = playlistId,
                                    position = index
                                )
                            }?.let(Database::insertSongPlaylistMaps)
                    }
                }
            }
        )
    }

    val headerContent: @Composable () -> Unit = {
        if (playlistPage == null) {
            HeaderPlaceholder(
                modifier = Modifier
                    .shimmer()
            )
        } else {

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                HeaderWithIcon(
                    title = playlistPage?.title ?: "Unknown",
                    iconId = R.drawable.playlist,
                    enabled = true,
                    showIcon = true,
                    modifier = Modifier
                        .padding(bottom = 8.dp),
                    onClick = {}
                ) //{

            }

            //Header(title = playlistPage?.title ?: "Unknown") {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                HeaderInfo(
                    title = "${playlistPage?.songsPage?.items?.size}",
                    icon = painterResource(R.drawable.musical_notes),
                    spacer = 0
                )

                Spacer(
                    modifier = Modifier
                        .weight(1f)
                )

                HeaderIconButton(
                    icon = R.drawable.downloaded,
                    color = colorPalette.text,
                    onClick = {
                        downloadState = Download.STATE_DOWNLOADING
                        if (playlistPage?.songsPage?.items?.isNotEmpty() == true)
                            playlistPage?.songsPage?.items?.forEach {
                                binder?.cache?.removeResource(it.asMediaItem.mediaId)
                                query {
                                    Database.insert(
                                        Song(
                                            id = it.asMediaItem.mediaId,
                                            title = it.asMediaItem.mediaMetadata.title.toString(),
                                            artistsText = it.asMediaItem.mediaMetadata.artist.toString(),
                                            thumbnailUrl = it.thumbnail?.url,
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

                HeaderIconButton(
                    icon = R.drawable.download,
                    color = colorPalette.text,
                    onClick = {
                        downloadState = Download.STATE_DOWNLOADING
                        if (playlistPage?.songsPage?.items?.isNotEmpty() == true)
                            playlistPage?.songsPage?.items?.forEach {
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



                HeaderIconButton(
                    icon = R.drawable.enqueue,
                    enabled = playlistPage?.songsPage?.items?.isNotEmpty() == true,
                    color =  if (playlistPage?.songsPage?.items?.isNotEmpty() == true) colorPalette.text else colorPalette.textDisabled,
                    onClick = {
                        playlistPage?.songsPage?.items?.map(Innertube.SongItem::asMediaItem)?.let { mediaItems ->
                            binder?.player?.enqueue(mediaItems)
                        }
                    }
                )

                HeaderIconButton(
                    icon = R.drawable.shuffle,
                    enabled = playlistPage?.songsPage?.items?.isNotEmpty() == true,
                    color = if (playlistPage?.songsPage?.items?.isNotEmpty() ==true) colorPalette.text else colorPalette.textDisabled,
                    onClick = {
                        if (playlistPage?.songsPage?.items?.isNotEmpty() == true) {
                            binder?.stopRadio()
                            playlistPage?.songsPage?.items?.shuffled()?.map(Innertube.SongItem::asMediaItem)
                                ?.let {
                                    binder?.player?.forcePlayFromBeginning(
                                        it
                                    )
                                }
                        }
                    }
                )

                HeaderIconButton(
                    icon = R.drawable.add,
                    color = colorPalette.text,
                    onClick = {
                        showAddPlaylistSelectDialog = true
                    }
                )

                if (showAddPlaylistSelectDialog)
                    SelectorDialog(
                        title = stringResource(R.string.add_in_playlist),
                        onDismiss = { showAddPlaylistSelectDialog = false },
                        values = listOf(
                            Info("a", stringResource(R.string.import_playlist)),
                            Info("s", stringResource(R.string.add_all_in_playlist))
                        ),
                        onValueSelected = {
                            if (it == "a") {
                                isImportingPlaylist = true
                            } else showPlaylistSelectDialog = true

                            showAddPlaylistSelectDialog = false
                        }
                    )

                if (showPlaylistSelectDialog) {

                    SelectorDialog(
                        title = stringResource(R.string.playlists),
                        onDismiss = { showPlaylistSelectDialog = false },
                        values = playlistPreviews.map {
                            Info(
                                it.playlist.id.toString(),
                                "${it.playlist.name} (${it.songCount})"
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

                                playlistPage!!.songsPage?.items?.forEachIndexed { position, song ->
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

                            showPlaylistSelectDialog = false
                        }
                    )
                }

                HeaderIconButton(
                    icon = R.drawable.share_social,
                    color = colorPalette.text,
                    onClick = {
                        (playlistPage?.url ?: "https://music.youtube.com/playlist?list=${browseId.removePrefix("VL")}").let { url ->
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, url)
                            }

                            context.startActivity(Intent.createChooser(sendIntent, null))
                        }
                    }
                )
            }

            /*        */
            Row (
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier
                    .padding(all = 10.dp)
                    .fillMaxWidth()
            ) {
                var searching by rememberSaveable { mutableStateOf(false) }

                if (searching) {
                    val focusRequester = remember { FocusRequester() }
                    val focusManager = LocalFocusManager.current
                    val keyboardController = LocalSoftwareKeyboardController.current

                    LaunchedEffect(searching) {
                        focusRequester.requestFocus()
                    }

                    BasicTextField(
                        value = filter ?: "",
                        onValueChange = { filter = it },
                        textStyle = typography.xs.semiBold,
                        singleLine = true,
                        maxLines = 1,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (filter.isNullOrBlank()) filter = ""
                            focusManager.clearFocus()
                        }),
                        cursorBrush = SolidColor(colorPalette.text),
                        decorationBox = { innerTextField ->
                            Box(
                                contentAlignment = Alignment.CenterStart,
                                modifier = Modifier.weight(1f)
                            ) {
                                androidx.compose.animation.AnimatedVisibility(
                                    visible = filter?.isEmpty() ?: true,
                                    enter = fadeIn(tween(100)),
                                    exit = fadeOut(tween(100)),
                                ) {
                                    BasicText(
                                        text = stringResource(R.string.search),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = typography.xs.semiBold.secondary.copy(color = colorPalette.textDisabled)
                                    )
                                }

                                innerTextField()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                colorPalette.background4,
                                shape = thumbnailRoundness.shape()
                            )
                            .focusRequester(focusRequester)
                            .onFocusChanged {
                                if (!it.hasFocus) {
                                    keyboardController?.hide()
                                    if (filter?.isBlank() == true) {
                                        filter = null
                                        searching = false
                                    }
                                }
                            }
                    )
                } else {
                    HeaderIconButton(
                        onClick = { searching = true },
                        icon = R.drawable.search_circle,
                        color = colorPalette.text,
                        iconSize = 24.dp
                    )
                }
            }
            /*        */
        }
    }

    val thumbnailContent = adaptiveThumbnailContent(playlistPage == null, playlistPage?.thumbnail?.url)

    val lazyListState = rememberLazyListState()

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
                        headerContent()
                        if (!isLandscape) thumbnailContent()
                    }
                }

                itemsIndexed(items = playlistPage?.songsPage?.items ?: emptyList()) { index, song ->
                    val isLocal by remember { derivedStateOf { song.asMediaItem.isLocal } }
                    downloadState = getDownloadState(song.asMediaItem.mediaId)
                    val isDownloaded = if (!isLocal) downloadedStateMedia(song.asMediaItem.mediaId) else true
                    SongItem(
                        song = song,
                        isDownloaded = isDownloaded,
                        onDownloadClick = {
                            binder?.cache?.removeResource(song.asMediaItem.mediaId)
                            query {
                                Database.insert(
                                    Song(
                                        id = song.asMediaItem.mediaId,
                                        title = song.asMediaItem.mediaMetadata.title.toString(),
                                        artistsText = song.asMediaItem.mediaMetadata.artist.toString(),
                                        thumbnailUrl = song.thumbnail?.url,
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
                        downloadState = downloadState,
                        thumbnailSizePx = songThumbnailSizePx,
                        thumbnailSizeDp = songThumbnailSizeDp,
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
                                    playlistPage?.songsPage?.items?.map(Innertube.SongItem::asMediaItem)?.let { mediaItems ->
                                        binder?.stopRadio()
                                        binder?.player?.forcePlayAtIndex(mediaItems, index)
                                    }
                                }
                            )
                    )
                }

                if (playlistPage == null) {
                    item(key = "loading") {
                        ShimmerHost(
                            modifier = Modifier
                                .fillParentMaxSize()
                        ) {
                            repeat(4) {
                                SongItemPlaceholder(thumbnailSizeDp = songThumbnailSizeDp)
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
                    playlistPage?.songsPage?.items?.let { songs ->
                        if (songs.isNotEmpty()) {
                            binder?.stopRadio()
                            binder?.player?.forcePlayFromBeginning(
                                songs.shuffled().map(Innertube.SongItem::asMediaItem)
                            )
                        }
                    }
                }
            )


        }
    }
}
