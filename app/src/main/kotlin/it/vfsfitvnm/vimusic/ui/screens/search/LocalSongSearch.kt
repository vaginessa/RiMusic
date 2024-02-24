package it.vfsfitvnm.vimusic.ui.screens.search

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import it.vfsfitvnm.compose.persist.persistList
import it.vfsfitvnm.innertube.models.NavigationEndpoint
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerAwareWindowInsets
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.ThumbnailRoundness
import it.vfsfitvnm.vimusic.models.Song
import it.vfsfitvnm.vimusic.query
import it.vfsfitvnm.vimusic.service.isLocal
import it.vfsfitvnm.vimusic.ui.components.LocalMenuState
import it.vfsfitvnm.vimusic.ui.components.themed.FloatingActionsContainerWithScrollToTop
import it.vfsfitvnm.vimusic.ui.components.themed.Header
import it.vfsfitvnm.vimusic.ui.components.themed.HeaderWithIcon
import it.vfsfitvnm.vimusic.ui.components.themed.InHistoryMediaItemMenu
import it.vfsfitvnm.vimusic.ui.components.themed.SecondaryTextButton
import it.vfsfitvnm.vimusic.ui.items.SongItem
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.px
import it.vfsfitvnm.vimusic.utils.align
import it.vfsfitvnm.vimusic.utils.asMediaItem
import it.vfsfitvnm.vimusic.utils.downloadedStateMedia
import it.vfsfitvnm.vimusic.utils.forcePlay
import it.vfsfitvnm.vimusic.utils.getDownloadState
import it.vfsfitvnm.vimusic.utils.manageDownload
import it.vfsfitvnm.vimusic.utils.medium
import it.vfsfitvnm.vimusic.utils.rememberPreference
import it.vfsfitvnm.vimusic.utils.thumbnailRoundnessKey
import kotlinx.coroutines.delay

@ExperimentalTextApi
@SuppressLint("SuspiciousIndentation")
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@UnstableApi
@Composable
fun LocalSongSearch(
    textFieldValue: TextFieldValue,
    onTextFieldValueChanged: (TextFieldValue) -> Unit,
    decorationBox: @Composable (@Composable () -> Unit) -> Unit
) {
    val (colorPalette, typography) = LocalAppearance.current
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current

    var items by persistList<Song>("search/local/songs")

    LaunchedEffect(textFieldValue.text) {
        if (textFieldValue.text.length > 1) {
            Database.search("%${textFieldValue.text}%").collect { items = it }
        }
    }

    val thumbnailSizeDp = Dimensions.thumbnails.song
    val thumbnailSizePx = thumbnailSizeDp.px

    val lazyListState = rememberLazyListState()

    var downloadState by remember {
        mutableStateOf(Download.STATE_STOPPED)
    }
    val context = LocalContext.current

    var thumbnailRoundness by rememberPreference(
        thumbnailRoundnessKey,
        ThumbnailRoundness.Heavy
    )

    val focusRequester = remember {
        FocusRequester()
    }

    Box {
        LazyColumn(
            state = lazyListState,
            contentPadding = LocalPlayerAwareWindowInsets.current
                .only(WindowInsetsSides.Vertical + WindowInsetsSides.End).asPaddingValues(),
            modifier = Modifier
                .fillMaxSize()
        ) {
            item(
                key = "header",
                contentType = 0
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    HeaderWithIcon(
                        title = "${stringResource(R.string.search)} ${stringResource(R.string.library)}",
                        iconId = R.drawable.library,
                        enabled = true,
                        showIcon = true,
                        modifier = Modifier
                            .padding(bottom = 8.dp),
                        onClick = {}
                    )

                }
                Header(
                    titleContent = {
                        BasicTextField(
                            value = textFieldValue,
                            onValueChange = onTextFieldValueChanged,
                            textStyle = typography.l.medium.align(TextAlign.Start),
                            singleLine = true,
                            maxLines = 1,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            cursorBrush = SolidColor(colorPalette.text),
                            decorationBox = decorationBox,
                            modifier = Modifier
                                .background(
                                    colorPalette.background1,
                                    shape = thumbnailRoundness.shape()
                                )
                                .focusRequester(focusRequester)
                                .fillMaxWidth()
                        )
                    },
                    actionsContent = {
                        if (textFieldValue.text.isNotEmpty()) {
                            SecondaryTextButton(
                                text = stringResource(R.string.clear),
                                onClick = { onTextFieldValueChanged(TextFieldValue()) }
                            )
                        }
                    },
                    /*
                    modifier = Modifier
                        .drawBehind {

                            val strokeWidth = 1 * density
                            val y = size.height - strokeWidth / 2

                            drawLine(
                                color = colorPalette.textDisabled,
                                start = Offset(x = 0f, y = y/2),
                                end = Offset(x = size.maxDimension, y = y/2),
                                strokeWidth = 2.dp.toPx()
                            )
                        }
                     */
                )
            }

            items(
                items = items,
                key = Song::id,
            ) { song ->
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
                    thumbnailSizePx = thumbnailSizePx,
                    thumbnailSizeDp = thumbnailSizeDp,
                    modifier = Modifier
                        .combinedClickable(
                            onLongClick = {
                                menuState.display {
                                    InHistoryMediaItemMenu(
                                        song = song,
                                        onDismiss = menuState::hide
                                    )
                                }
                            },
                            onClick = {
                                val mediaItem = song.asMediaItem
                                binder?.stopRadio()
                                binder?.player?.forcePlay(mediaItem)
                                binder?.setupRadio(
                                    NavigationEndpoint.Endpoint.Watch(videoId = mediaItem.mediaId)
                                )
                            }
                        )
                        .animateItemPlacement()
                )
            }
        }

        FloatingActionsContainerWithScrollToTop(lazyListState = lazyListState)
    }
    LaunchedEffect(Unit) {
        delay(300)
        focusRequester.requestFocus()
    }
}
