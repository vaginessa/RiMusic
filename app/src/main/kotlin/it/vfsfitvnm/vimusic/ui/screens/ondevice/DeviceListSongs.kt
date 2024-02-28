package it.vfsfitvnm.vimusic.ui.screens.ondevice

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import coil.compose.AsyncImage
import it.vfsfitvnm.compose.persist.persistList
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerAwareWindowInsets
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.AlbumSortBy
import it.vfsfitvnm.vimusic.enums.BuiltInPlaylist
import it.vfsfitvnm.vimusic.enums.DeviceLists
import it.vfsfitvnm.vimusic.enums.OnDeviceSongSortBy
import it.vfsfitvnm.vimusic.enums.SongSortBy
import it.vfsfitvnm.vimusic.enums.SortOrder
import it.vfsfitvnm.vimusic.enums.ThumbnailRoundness
import it.vfsfitvnm.vimusic.enums.UiType
import it.vfsfitvnm.vimusic.models.Song
import it.vfsfitvnm.vimusic.models.SongPlaylistMap
import it.vfsfitvnm.vimusic.service.LOCAL_KEY_PREFIX
import it.vfsfitvnm.vimusic.transaction
import it.vfsfitvnm.vimusic.ui.components.LocalMenuState
import it.vfsfitvnm.vimusic.ui.components.themed.FloatingActionsContainerWithScrollToTop
import it.vfsfitvnm.vimusic.ui.components.themed.HeaderIconButton
import it.vfsfitvnm.vimusic.ui.components.themed.HeaderWithIcon
import it.vfsfitvnm.vimusic.ui.components.themed.IconInfo
import it.vfsfitvnm.vimusic.ui.components.themed.InHistoryMediaItemMenu
import it.vfsfitvnm.vimusic.ui.components.themed.NowPlayingShow
import it.vfsfitvnm.vimusic.ui.components.themed.PlaylistsItemMenu
import it.vfsfitvnm.vimusic.ui.components.themed.SortMenu
import it.vfsfitvnm.vimusic.ui.items.PlaylistItem
import it.vfsfitvnm.vimusic.ui.items.SongItem
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.favoritesIcon
import it.vfsfitvnm.vimusic.ui.styling.onOverlay
import it.vfsfitvnm.vimusic.ui.styling.overlay
import it.vfsfitvnm.vimusic.ui.styling.px
import it.vfsfitvnm.vimusic.utils.OnDeviceBlacklist
import it.vfsfitvnm.vimusic.utils.UiTypeKey
import it.vfsfitvnm.vimusic.utils.asMediaItem
import it.vfsfitvnm.vimusic.utils.center
import it.vfsfitvnm.vimusic.utils.color
import it.vfsfitvnm.vimusic.utils.durationTextToMillis
import it.vfsfitvnm.vimusic.utils.enqueue
import it.vfsfitvnm.vimusic.utils.forcePlayAtIndex
import it.vfsfitvnm.vimusic.utils.forcePlayFromBeginning
import it.vfsfitvnm.vimusic.utils.formatAsTime
import it.vfsfitvnm.vimusic.utils.isAtLeastAndroid10
import it.vfsfitvnm.vimusic.utils.isAtLeastAndroid13
import it.vfsfitvnm.vimusic.utils.rememberPreference
import it.vfsfitvnm.vimusic.utils.secondary
import it.vfsfitvnm.vimusic.utils.semiBold
import it.vfsfitvnm.vimusic.utils.onDeviceSongSortByKey
import it.vfsfitvnm.vimusic.utils.songSortOrderKey
import it.vfsfitvnm.vimusic.utils.thumbnail
import it.vfsfitvnm.vimusic.utils.thumbnailRoundnessKey
import it.vfsfitvnm.vimusic.utils.toast
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@ExperimentalTextApi
@SuppressLint("SuspiciousIndentation")
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@UnstableApi
@Composable
fun DeviceListSongs(
    deviceLists: DeviceLists,
    onSearchClick: () -> Unit
) {
    val (colorPalette,typography) = LocalAppearance.current
    val binder = LocalPlayerServiceBinder.current
    val uiType  by rememberPreference(UiTypeKey, UiType.RiMusic)
    val menuState = LocalMenuState.current

    var sortBy by rememberPreference(onDeviceSongSortByKey, OnDeviceSongSortBy.DateAdded)
    var sortOrder by rememberPreference(songSortOrderKey, SortOrder.Descending)

    var songs by remember(sortBy, sortOrder) {
        mutableStateOf<List<Song>>(emptyList())
    }
    var filteredSongs = songs
    val context = LocalContext.current
    LaunchedEffect(sortBy, sortOrder) {
        context.musicFilesAsFlow(sortBy, sortOrder, context).collect { songs = it }
    }

    var filter: String? by rememberSaveable { mutableStateOf(null) }

    var filterCharSequence: CharSequence
    filterCharSequence = filter.toString()
    //Log.d("mediaItemFilter", "<${filter}>  <${filterCharSequence}>")
    if (!filter.isNullOrBlank())
    filteredSongs = songs
        .filter {
            it.title?.contains(filterCharSequence,true) ?: false
            || it.artistsText?.contains(filterCharSequence,true) ?: false
        }

    var searching by rememberSaveable { mutableStateOf(false) }

    val thumbnailSizeDp = Dimensions.thumbnails.song
    val thumbnailSize = thumbnailSizeDp.px

    val sortOrderIconRotation by animateFloatAsState(
        targetValue = if (sortOrder == SortOrder.Ascending) 0f else 180f,
        animationSpec = tween(durationMillis = 400, easing = LinearEasing), label = ""
    )

    var thumbnailRoundness by rememberPreference(
        thumbnailRoundnessKey,
        ThumbnailRoundness.Heavy
    )

    val lazyListState = rememberLazyListState()

    var totalPlayTimes = 0L
    filteredSongs.forEach {
        totalPlayTimes += it.durationText?.let { it1 ->
            durationTextToMillis(it1)
        }?.toLong() ?: 0
    }

    val playlistThumbnailSizeDp = Dimensions.thumbnails.playlist
    val playlistThumbnailSizePx = playlistThumbnailSizeDp.px

    val thumbnails = songs
        .takeWhile { it.thumbnailUrl?.isNotEmpty() ?: false }
        .take(4)
        .map { it.thumbnailUrl.thumbnail(playlistThumbnailSizePx / 2) }

    var listMediaItems = remember {
        mutableListOf<MediaItem>()
    }

    var selectItems by remember {
        mutableStateOf(false)
    }

    var position by remember {
        mutableIntStateOf(0)
    }
    var scrollToNowPlaying by remember {
        mutableStateOf(false)
    }

    var nowPlayingItem by remember {
        mutableStateOf(-1)
    }

    val activity = LocalContext.current as Activity
    if (ContextCompat.checkSelfPermission(
            activity,
            if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO
            else Manifest.permission.READ_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        LocalContext.current.toast("On device require read media permission, grant please.")
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO
            else Manifest.permission.READ_EXTERNAL_STORAGE), 41
        )
    } else {

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
                    title = stringResource(R.string.on_device),
                    iconId = R.drawable.search,
                    enabled = true,
                    showIcon = true,
                    modifier = Modifier,
                    onClick = onSearchClick
                )

                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        //.background(colorPalette.background4)
                        .fillMaxSize(0.99F)
                        .background(
                            color = colorPalette.background4,
                            shape = thumbnailRoundness.shape()
                        )
                ) {
                    if (filteredSongs.isEmpty())
                        PlaylistItem(
                            icon = R.drawable.musical_notes,
                            colorTint = colorPalette.favoritesIcon,
                            name = stringResource(R.string.on_device),
                            songCount = null,
                            thumbnailSizeDp = playlistThumbnailSizeDp,
                            alternative = false,
                            modifier = Modifier
                                .padding(top = 14.dp)
                        )

                    if (filteredSongs.isNotEmpty())
                        PlaylistItem(
                            thumbnailContent = {
                                if (thumbnails.toSet().size == 1) {
                                    AsyncImage(
                                        model = thumbnails.first().thumbnail(playlistThumbnailSizePx),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = it
                                    )
                                } else {
                                    Box(
                                        modifier = it
                                            .fillMaxSize()
                                    ) {
                                        listOf(
                                            Alignment.TopStart,
                                            Alignment.TopEnd,
                                            Alignment.BottomStart,
                                            Alignment.BottomEnd
                                        ).forEachIndexed { index, alignment ->
                                            AsyncImage(
                                                model = thumbnails.getOrNull(index),
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .align(alignment)
                                                    .size(playlistThumbnailSizeDp / 2)
                                            )
                                        }
                                    }
                                }
                            },
                            songCount = null,
                            name = "",
                            channelName = null,
                            thumbnailSizeDp = playlistThumbnailSizeDp,
                            alternative = true,
                            showName = false,
                            modifier = Modifier
                                .padding(top = 14.dp)
                        )


                    Column (
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier
                            .fillMaxHeight()
                        //.border(BorderStroke(1.dp, Color.White))
                    ) {
                        Spacer(modifier = Modifier.height(10.dp))
                        IconInfo(
                            title = filteredSongs.size.toString(),
                            icon = painterResource(R.drawable.musical_notes)
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        IconInfo(
                            title = formatAsTime(totalPlayTimes),
                            icon = painterResource(R.drawable.time)
                        )
                        Spacer(modifier = Modifier.height(30.dp))
                    }



                }

                Spacer(modifier = Modifier.height(10.dp))

                Row (
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                    ){

                    HeaderIconButton(
                        icon = R.drawable.locate,
                        enabled = filteredSongs.isNotEmpty(),
                        color = if (filteredSongs.isNotEmpty()) colorPalette.text else colorPalette.textDisabled,
                        onClick = {
                            nowPlayingItem = -1
                            scrollToNowPlaying = false
                            filteredSongs
                                .forEachIndexed{ index, song ->
                                    if (song.asMediaItem.mediaId == binder?.player?.currentMediaItem?.mediaId)
                                        nowPlayingItem = index
                                }

                            if (nowPlayingItem > -1)
                                scrollToNowPlaying = true
                        }
                    )
                    LaunchedEffect(scrollToNowPlaying) {
                        if (scrollToNowPlaying)
                            lazyListState.scrollToItem(nowPlayingItem,1)
                        scrollToNowPlaying = false
                    }

                    HeaderIconButton(
                        onClick = { searching = !searching },
                        icon = R.drawable.search_circle,
                        color = colorPalette.text,
                        iconSize = 24.dp
                    )

                    /*
                    HeaderIconButton(
                        icon = R.drawable.enqueue,
                        enabled = filteredSongs.isNotEmpty(),
                        color = if (filteredSongs.isNotEmpty()) colorPalette.text else colorPalette.textDisabled,
                        onClick = {
                            binder?.player?.enqueue(filteredSongs.map(Song::asMediaItem))
                        }
                    )
                     */

                    HeaderIconButton(
                        icon = R.drawable.shuffle,
                        enabled = filteredSongs.isNotEmpty(),
                        color = if (filteredSongs.isNotEmpty()) colorPalette.text else colorPalette.textDisabled,
                        onClick = {
                            if (filteredSongs.isNotEmpty()) {
                                binder?.stopRadio()
                                binder?.player?.forcePlayFromBeginning(
                                    songs.shuffled().map(Song::asMediaItem)
                                )
                            }
                        }
                    )

                    HeaderIconButton(
                        icon = R.drawable.ellipsis_horizontal,
                        color = if (filteredSongs.isNotEmpty() == true) colorPalette.text else colorPalette.textDisabled,
                        enabled = filteredSongs.isNotEmpty() == true,
                        modifier = Modifier
                            .padding(end = 4.dp),
                        onClick = {
                            menuState.display {
                                PlaylistsItemMenu(
                                    onDismiss = menuState::hide,
                                    onSelect = { selectItems = true },
                                    onUncheck = {
                                        selectItems = false
                                        listMediaItems.clear()
                                    },
                                    onEnqueue = {
                                        if (listMediaItems.isEmpty()) {
                                            binder?.player?.enqueue(filteredSongs.map(Song::asMediaItem))
                                        } else {
                                            binder?.player?.enqueue(listMediaItems)
                                            listMediaItems.clear()
                                            selectItems = false
                                        }
                                    },
                                    onAddToPlaylist = { playlistPreview ->
                                        position =
                                            playlistPreview.songCount.minus(1) ?: 0
                                        //Log.d("mediaItem", " maxPos in Playlist $it ${position}")
                                        if (position > 0) position++ else position = 0
                                        //Log.d("mediaItem", "next initial pos ${position}")
                                        if (listMediaItems.isEmpty()) {
                                            filteredSongs.forEachIndexed { index, song ->
                                                transaction {
                                                    Database.insert(song.asMediaItem)
                                                    Database.insert(
                                                        SongPlaylistMap(
                                                            songId = song.asMediaItem.mediaId,
                                                            playlistId = playlistPreview.playlist.id,
                                                            position = position + index
                                                        )
                                                    )
                                                }
                                                //Log.d("mediaItemPos", "added position ${position + index}")
                                            }
                                        } else {
                                            listMediaItems.forEachIndexed { index, song ->
                                                //Log.d("mediaItemMaxPos", position.toString())
                                                transaction {
                                                    Database.insert(song)
                                                    Database.insert(
                                                        SongPlaylistMap(
                                                            songId = song.mediaId,
                                                            playlistId = playlistPreview.playlist.id,
                                                            position = position + index
                                                        )
                                                    )
                                                }
                                                //Log.d("mediaItemPos", "add position $position")
                                            }
                                            listMediaItems.clear()
                                            selectItems = false
                                        }
                                    },
                                )
                            }
                        }
                    )

                    Spacer(
                        modifier = Modifier
                            .weight(1f)
                    )

                    BasicText(
                        text = when (sortBy) {
                            OnDeviceSongSortBy.Title -> stringResource(R.string.sort_title)
                            OnDeviceSongSortBy.DateAdded -> stringResource(R.string.sort_date_added)
                            OnDeviceSongSortBy.Artist -> stringResource(R.string.sort_artist)
                        },
                        style = typography.xs.semiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .clickable {
                                menuState.display{
                                    SortMenu(
                                        title = stringResource(R.string.sorting_order),
                                        onDismiss = menuState::hide,
                                        onTitle = { sortBy = OnDeviceSongSortBy.Title },
                                        onDateAdded = { sortBy = OnDeviceSongSortBy.DateAdded },
                                        onArtist = { sortBy = OnDeviceSongSortBy.Artist },
                                    )
                                }

                            }
                    )

                    /*
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

                     */

                            /*
                            Spacer(
                                modifier = Modifier
                                    .width(2.dp)
                            )
                            */
                            HeaderIconButton(
                                icon = R.drawable.arrow_up,
                                color = colorPalette.text,
                                onClick = { sortOrder = !sortOrder },
                                modifier = Modifier
                                    .graphicsLayer { rotationZ = sortOrderIconRotation }
                            )

                }

                Row (
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier
                        .padding(all = 10.dp)
                        .fillMaxWidth()
                ) {
                    AnimatedVisibility(visible = searching) {
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
                                        .padding(horizontal = 10.dp)
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
                                .height(30.dp)
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
                    }
                }

            }

            itemsIndexed(
                items = filteredSongs,
                key = { _, song -> song.id },
                contentType = { _, song -> song },
            ) { index, song ->
/*
                Log.d("mediaItemUri",
                ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        song.id.substringAfter(LOCAL_KEY_PREFIX).toLong()
                ).toString()
                )

*/

                SongItem(
                    song = song,
                    isDownloaded = true,
                    onDownloadClick = {
                        // not necessary
                    },
                    downloadState = Download.STATE_COMPLETED,
                    thumbnailSizeDp = thumbnailSizeDp,
                    thumbnailSizePx = thumbnailSize,
                    onThumbnailContent = {
                        if (nowPlayingItem > -1)
                            NowPlayingShow(song.asMediaItem.mediaId)
                    },
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
                    },
                    modifier = Modifier
                        .combinedClickable(
                            onLongClick = {
                                menuState.display {
                                    when (deviceLists) {
                                        DeviceLists.LocalSongs -> InHistoryMediaItemMenu(
                                            song = song,
                                            onDismiss = menuState::hide
                                        )
                                    }
                                }
                            },
                            onClick = {
                                binder?.stopRadio()
                                binder?.player?.forcePlayAtIndex(
                                    filteredSongs.map(Song::asMediaItem),
                                    index
                                )
                            }
                        )
                        .animateItemPlacement()
                )
            }
        }

        if(uiType == UiType.ViMusic)
        FloatingActionsContainerWithScrollToTop(
            lazyListState = lazyListState,
            iconId = R.drawable.shuffle,
            onClick = {
                if (filteredSongs.isNotEmpty()) {
                    binder?.stopRadio()
                    binder?.player?.forcePlayFromBeginning(
                        filteredSongs.shuffled().map(Song::asMediaItem)
                    )
                }
            }
        )

    }


    }




}

private val mediaScope = CoroutineScope(Dispatchers.IO + CoroutineName("MediaStore worker"))
fun Context.musicFilesAsFlow(sortBy: OnDeviceSongSortBy, order: SortOrder, context: Context): StateFlow<List<Song>> = flow {
    var version: String? = null

    while (currentCoroutineContext().isActive) {
        val newVersion = MediaStore.getVersion(applicationContext)
        if (version != newVersion) {
            version = newVersion
            val collection =
                if (isAtLeastAndroid10) MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
                else MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.RELATIVE_PATH,
                MediaStore.Audio.Media.TITLE
            )

            val sortOrderSQL = when (order) {
                SortOrder.Ascending -> "ASC"
                SortOrder.Descending -> "DESC"
            }

            val sortBySQL = when (sortBy) {
                OnDeviceSongSortBy.Title -> "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE $sortOrderSQL"
                OnDeviceSongSortBy.DateAdded -> "${MediaStore.Audio.Media.DATE_ADDED} $sortOrderSQL"
                OnDeviceSongSortBy.Artist -> "${MediaStore.Audio.Media.ARTIST} COLLATE NOCASE $sortOrderSQL"
            }

            val albumUriBase = Uri.parse("content://media/external/audio/albumart")

            contentResolver.query(collection, projection, null, null, sortBySQL)
                ?.use { cursor ->
                    val idIdx = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
                    val nameIdx = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)
                    val durationIdx = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
                    val artistIdx = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                    val albumIdIdx = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
                    val relativePathIdx = cursor.getColumnIndex(MediaStore.Audio.Media.RELATIVE_PATH)
                    val titleIdx = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                    val blacklist = OnDeviceBlacklist(context = context)


                    buildList {
                        while (cursor.moveToNext()) {
                            val id = cursor.getLong(idIdx)
                            val name = cursor.getString(nameIdx).substringBeforeLast(".")
                            val trackName = cursor.getString(titleIdx)
                            val duration = cursor.getInt(durationIdx)
                            val artist = cursor.getString(artistIdx)
                            val albumId = cursor.getLong(albumIdIdx)
                            val relativePath = cursor.getString(relativePathIdx)
                            val exclude = blacklist.contains(relativePath)

                            if (!exclude) {
                                val albumUri = ContentUris.withAppendedId(albumUriBase, albumId)
                                val durationText =
                                    duration.milliseconds.toComponents { minutes, seconds, _ ->
                                        "$minutes:${seconds.toString().padStart(2, '0')}"
                                    }
                                add(
                                    Song(
                                        id = "$LOCAL_KEY_PREFIX$id",
                                        title = trackName ?: name,
                                        artistsText = artist,
                                        durationText = durationText,
                                        thumbnailUrl = albumUri.toString()
                                    )
                                )
                            }
                        }
                    }
                }?.let { emit(it) }
        }
        delay(5.seconds)
    }
}.distinctUntilChanged()
    .onEach { songs -> transaction { songs.forEach(Database::insert) } }
    .stateIn(mediaScope, SharingStarted.Eagerly, listOf())
