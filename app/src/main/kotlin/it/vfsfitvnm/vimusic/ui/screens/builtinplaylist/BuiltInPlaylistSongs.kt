package it.vfsfitvnm.vimusic.ui.screens.builtinplaylist

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
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
import it.vfsfitvnm.compose.persist.persistList
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerAwareWindowInsets
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.BuiltInPlaylist
import it.vfsfitvnm.vimusic.enums.SongSortBy
import it.vfsfitvnm.vimusic.enums.SortOrder
import it.vfsfitvnm.vimusic.enums.ThumbnailRoundness
import it.vfsfitvnm.vimusic.models.Song
import it.vfsfitvnm.vimusic.models.SongWithContentLength
import it.vfsfitvnm.vimusic.query
import it.vfsfitvnm.vimusic.service.DownloadUtil
import it.vfsfitvnm.vimusic.service.isLocal
import it.vfsfitvnm.vimusic.ui.components.LocalMenuState
import it.vfsfitvnm.vimusic.ui.components.themed.ConfirmationDialog
import it.vfsfitvnm.vimusic.ui.components.themed.HeaderIconButton
import it.vfsfitvnm.vimusic.ui.components.themed.HeaderInfo
import it.vfsfitvnm.vimusic.ui.components.themed.HeaderWithIcon
import it.vfsfitvnm.vimusic.ui.components.themed.InHistoryMediaItemMenu
import it.vfsfitvnm.vimusic.ui.components.themed.NonQueuedMediaItemMenu
import it.vfsfitvnm.vimusic.ui.items.SongItem
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.onOverlay
import it.vfsfitvnm.vimusic.ui.styling.overlay
import it.vfsfitvnm.vimusic.ui.styling.px
import it.vfsfitvnm.vimusic.utils.asMediaItem
import it.vfsfitvnm.vimusic.utils.center
import it.vfsfitvnm.vimusic.utils.color
import it.vfsfitvnm.vimusic.utils.downloadedStateMedia
import it.vfsfitvnm.vimusic.utils.enqueue
import it.vfsfitvnm.vimusic.utils.forcePlayAtIndex
import it.vfsfitvnm.vimusic.utils.forcePlayFromBeginning
import it.vfsfitvnm.vimusic.utils.getDownloadState
import it.vfsfitvnm.vimusic.utils.manageDownload
import it.vfsfitvnm.vimusic.utils.rememberPreference
import it.vfsfitvnm.vimusic.utils.secondary
import it.vfsfitvnm.vimusic.utils.semiBold
import it.vfsfitvnm.vimusic.utils.songSortByKey
import it.vfsfitvnm.vimusic.utils.songSortOrderKey
import it.vfsfitvnm.vimusic.utils.thumbnailRoundnessKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

@ExperimentalTextApi
@SuppressLint("SuspiciousIndentation", "StateFlowValueCalledInComposition")
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@UnstableApi
@Composable
fun BuiltInPlaylistSongs(
    builtInPlaylist: BuiltInPlaylist,
    onSearchClick: () -> Unit
) {
    val context = LocalContext.current
    val (colorPalette, typography, thumbnailShape) = LocalAppearance.current
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current

    var songs by persistList<Song>("${builtInPlaylist.name}/songs")

    var sortBy by rememberPreference(songSortByKey, SongSortBy.DateAdded)
    var sortOrder by rememberPreference(songSortOrderKey, SortOrder.Descending)

    var filter: String? by rememberSaveable { mutableStateOf(null) }

    var downloadState by remember {
        mutableStateOf(Download.STATE_STOPPED)
    }

    var showConfirmDeleteDownloadDialog by remember {
        mutableStateOf(false)
    }

     LaunchedEffect(Unit, sortBy, sortOrder, filter) {
        when (builtInPlaylist) {

            BuiltInPlaylist.Downloaded -> {
                DownloadUtil.getDownloadManager(context)
                DownloadUtil.getDownloads()
                DownloadUtil.downloads.value.keys.toList().let { Database.getSongsList(it) }
            }

            BuiltInPlaylist.Favorites -> Database
                .songsFavorites(sortBy, sortOrder)

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
        }?.collect { songs = it }
    }

    var filterCharSequence: CharSequence
    filterCharSequence = filter.toString()
    //Log.d("mediaItemFilter", "<${filter}>  <${filterCharSequence}>")
    if (!filter.isNullOrBlank())
    songs = songs
        .filter {
            it.title?.contains(filterCharSequence,true) ?: false
            || it.artistsText?.contains(filterCharSequence,true) ?: false
        }

    val thumbnailSizeDp = Dimensions.thumbnails.song
    val thumbnailSize = thumbnailSizeDp.px

    val sortOrderIconRotation by animateFloatAsState(
        targetValue = if (sortOrder == SortOrder.Ascending) 0f else 180f,
        animationSpec = tween(durationMillis = 400, easing = LinearEasing)
    )

    var thumbnailRoundness by rememberPreference(
        thumbnailRoundnessKey,
        ThumbnailRoundness.Heavy
    )

    val lazyListState = rememberLazyListState()

    /*
    var totalPlayTimes = 0L
    songs.forEach {
        totalPlayTimes += if (it.durationText?.length == 4) {
            durationToMillis("0" + it.durationText)
        } else {
            durationToMillis(it.durationText.toString())
        }
    }
    */

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

                HeaderWithIcon(
                    title = when (builtInPlaylist) {
                        BuiltInPlaylist.Favorites -> stringResource(R.string.favorites)
                        BuiltInPlaylist.Downloaded -> stringResource(R.string.downloaded)
                        BuiltInPlaylist.Offline -> stringResource(R.string.cached)
                    },
                    iconId = R.drawable.search,
                    enabled = true,
                    showIcon = true,
                    modifier = Modifier,
                    onClick = onSearchClick
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    HeaderInfo(
                        //title = "${songs.size} (${formatAsDuration(totalPlayTimes).dropLast(3)})",
                        title = "${songs.size}",
                        icon = painterResource(R.drawable.musical_notes),
                        spacer = 0
                    )

                    Spacer(
                        modifier = Modifier
                            .weight(1f)
                    )
                    if (builtInPlaylist == BuiltInPlaylist.Favorites) {
                        HeaderIconButton(
                            icon = R.drawable.downloaded,
                            color = colorPalette.text,
                            onClick = {
                                downloadState = Download.STATE_DOWNLOADING
                                if (songs.isNotEmpty() == true)
                                    songs.forEach {
                                        binder?.cache?.removeResource(it.asMediaItem.mediaId)
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
                    if (builtInPlaylist == BuiltInPlaylist.Favorites || builtInPlaylist == BuiltInPlaylist.Downloaded) {
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

                    }

                    HeaderIconButton(
                        icon = R.drawable.enqueue,
                        enabled = songs.isNotEmpty(),
                        color = if (songs.isNotEmpty()) colorPalette.text else colorPalette.textDisabled,
                        onClick = {
                            binder?.player?.enqueue(songs.map(Song::asMediaItem))
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

                if (builtInPlaylist != BuiltInPlaylist.Downloaded) {
                    HeaderIconButton(
                        icon = R.drawable.trending,
                        color = if (sortBy == SongSortBy.PlayTime) colorPalette.text else colorPalette.textDisabled,
                        onClick = { sortBy = SongSortBy.PlayTime }
                    )

                    HeaderIconButton(
                        icon = R.drawable.text,
                        color = if (sortBy == SongSortBy.Title) colorPalette.text else colorPalette.textDisabled,
                        onClick = { sortBy = SongSortBy.Title }
                    )

                    HeaderIconButton(
                        icon = R.drawable.time,
                        color = if (sortBy == SongSortBy.DateAdded) colorPalette.text else colorPalette.textDisabled,
                        onClick = { sortBy = SongSortBy.DateAdded }
                    )

                    Spacer(
                        modifier = Modifier
                            .width(2.dp)
                    )

                    HeaderIconButton(
                        icon = R.drawable.arrow_up,
                        color = colorPalette.text,
                        onClick = { sortOrder = !sortOrder },
                        modifier = Modifier
                            .graphicsLayer { rotationZ = sortOrderIconRotation }
                    )

                }

                }

                /*        */
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier
                        //.requiredHeight(30.dp)
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

            /*
            if (builtInPlaylist == BuiltInPlaylist.Downloaded)
            item(
                key = "warning",
                contentType = 0
            ) {
                BasicText(
                    text = "Please be patient, I’m working on it.",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = typography.m.semiBold.secondary.copy(color = colorPalette.textDisabled)
                )
            }
             */


            itemsIndexed(
                items = songs,
                key = { _, song -> song.id },
                contentType = { _, song -> song },
            ) { index, song ->
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
                    downloadState = downloadState,
                    thumbnailSizeDp = thumbnailSizeDp,
                    thumbnailSizePx = thumbnailSize,
                    onThumbnailContent = if (sortBy == SongSortBy.PlayTime) ({
                        BasicText(
                            text = song.formattedTotalPlayTime,
                            style = typography.xxs.semiBold.center.color(colorPalette.onOverlay),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, colorPalette.overlay)
                                    ),
                                    shape = thumbnailShape
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .align(Alignment.BottomCenter)
                        )
                    }) else null,
                    modifier = Modifier
                        .combinedClickable(
                            onLongClick = {
                                menuState.display {
                                    when (builtInPlaylist) {
                                        BuiltInPlaylist.Favorites, BuiltInPlaylist.Downloaded -> NonQueuedMediaItemMenu(
                                            mediaItem = song.asMediaItem,
                                            onDismiss = menuState::hide
                                        )

                                        BuiltInPlaylist.Offline -> InHistoryMediaItemMenu(
                                            song = song,
                                            onDismiss = menuState::hide
                                        )
                                    }
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
        }

    /*
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
*/

}
