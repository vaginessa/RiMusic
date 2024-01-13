package it.vfsfitvnm.vimusic.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SnapshotMutationPolicy
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
const val lastPlayerThumbnailSizeKey = "lastPlayerThumbnailSize"
const val lastPlayerPlayButtonTypeKey = "lastPlayerPlayButtonType"
const val lastPlayerTimelineTypeKey = "lastPlayerTimelineType"
const val lastPlayerVisualizerTypeKey = "lastPlayerVisualizerType"
const val playerPlayButtonTypeKey = "playerPlayButtonType"
const val playerTimelineTypeKey = "playerTimelineType"
const val playerVisualizerTypeKey = "playerVisualizerType"
const val thumbnailTapEnabledKey = "thumbnailTapEnabled"
const val wavedPlayerTimelineKey = "wavedPlayerTimeline"
const val languageAppKey = "languageApp"
const val indexNavigationTabKey = "indexNavigationTab"
const val effectRotationKey = "effectRotation"
const val playerThumbnailSizeKey = "playerThumbnailSize"
const val colorPaletteNameKey = "colorPaletteName"
const val colorPaletteModeKey = "colorPaletteMode"
const val thumbnailRoundnessKey = "thumbnailRoundness"
const val coilDiskCacheMaxSizeKey = "coilDiskCacheMaxSize"
const val exoPlayerDiskCacheMaxSizeKey = "exoPlayerDiskCacheMaxSize"
const val exoPlayerDiskDownloadCacheMaxSizeKey = "exoPlayerDiskDownloadCacheMaxSize"
const val exoPlayerMinTimeForEventKey = "exoPlayerMinTimeForEvent"
const val exoPlayerAlternateCacheLocationKey = "exoPlayerAlternateCacheLocation"
const val isInvincibilityEnabledKey = "isInvincibilityEnabled"
const val useSystemFontKey = "useSystemFont"
const val applyFontPaddingKey = "applyFontPadding"
const val songSortOrderKey = "songSortOrder"
const val songSortByKey = "songSortBy"
const val playlistSortOrderKey = "playlistSortOrder"
const val playlistSortByKey = "playlistSortBy"
const val albumSortOrderKey = "albumSortOrder"
const val albumSortByKey = "albumSortBy"
const val artistSortOrderKey = "artistSortOrder"
const val artistSortByKey = "artistSortBy"
const val trackLoopEnabledKey = "trackLoopEnabled"
const val queueLoopEnabledKey = "queueLoopEnabled"
const val reorderInQueueEnabledKey = "reorderInQueueEnabled"
const val skipSilenceKey = "skipSilence"
const val volumeNormalizationKey = "volumeNormalization"
const val resumePlaybackWhenDeviceConnectedKey = "resumePlaybackWhenDeviceConnected"
const val persistentQueueKey = "persistentQueue"
const val closebackgroundPlayerKey = "closebackgroundPlayer"
const val closeWithBackButtonKey = "closeWithBackButton"
const val isShowingSynchronizedLyricsKey = "isShowingSynchronizedLyrics"
const val isShowingThumbnailInLockscreenKey = "isShowingThumbnailInLockscreen"
const val homeScreenTabIndexKey = "homeScreenTabIndex"
const val searchResultScreenTabIndexKey = "searchResultScreenTabIndex"
const val artistScreenTabIndexKey = "artistScreenTabIndex"
const val pauseSearchHistoryKey = "pauseSearchHistory"
const val UiTypeKey = "UiType"
const val disablePlayerHorizontalSwipeKey = "disablePlayerHorizontalSwipe"
const val disableIconButtonOnTopKey = "disableIconButtonOnTop"
const val exoPlayerCustomCacheKey = "exoPlayerCustomCache"
const val disableScrollingTextKey = "disableScrollingText"
const val audioQualityFormatKey = "audioQualityFormat"
const val showLikeButtonBackgroundPlayerKey = "showLikeButtonBackgroundPlayer"
const val showDownloadButtonBackgroundPlayerKey = "showDownloadButtonBackgroundPlayer"
const val playEventsTypeKey = "playEventsType"
const val fontTypeKey = "fontType"
const val playlistSongSortByKey = "playlistSongSortBy"
const val showRelatedAlbumsKey = "showRelatedAlbums"
const val showSimilarArtistsKey = "showSimilarArtists"
const val showNewAlbumsArtistsKey = "showNewAlbumsArtists"
const val showPlaylistMightLikeKey = "showPlaylistMightLike"


inline fun <reified T : Enum<T>> SharedPreferences.getEnum(
    key: String,
    defaultValue: T
): T =
    getString(key, null)?.let {
        try {
            enumValueOf<T>(it)
        } catch (e: IllegalArgumentException) {
            null
        }
    } ?: defaultValue

inline fun <reified T : Enum<T>> SharedPreferences.Editor.putEnum(
    key: String,
    value: T
): SharedPreferences.Editor =
    putString(key, value.name)

val Context.preferences: SharedPreferences
    get() = getSharedPreferences("preferences", Context.MODE_PRIVATE)

@Composable
fun rememberPreference(key: String, defaultValue: Boolean): MutableState<Boolean> {
    val context = LocalContext.current
    return remember {
        mutableStatePreferenceOf(context.preferences.getBoolean(key, defaultValue)) {
            context.preferences.edit { putBoolean(key, it) }
        }
    }
}

@Composable
fun rememberPreference(key: String, defaultValue: Int): MutableState<Int> {
    val context = LocalContext.current
    return remember {
        mutableStatePreferenceOf(context.preferences.getInt(key, defaultValue)) {
            context.preferences.edit { putInt(key, it) }
        }
    }
}

@Composable
fun rememberPreference(key: String, defaultValue: String): MutableState<String> {
    val context = LocalContext.current
    return remember {
        mutableStatePreferenceOf(context.preferences.getString(key, null) ?: defaultValue) {
            context.preferences.edit { putString(key, it) }
        }
    }
}

@Composable
inline fun <reified T : Enum<T>> rememberPreference(key: String, defaultValue: T): MutableState<T> {
    val context = LocalContext.current
    return remember {
        mutableStatePreferenceOf(context.preferences.getEnum(key, defaultValue)) {
            context.preferences.edit { putEnum(key, it) }
        }
    }
}

inline fun <T> mutableStatePreferenceOf(
    value: T,
    crossinline onStructuralInequality: (newValue: T) -> Unit
) =
    mutableStateOf(
        value = value,
        policy = object : SnapshotMutationPolicy<T> {
            override fun equivalent(a: T, b: T): Boolean {
                val areEquals = a == b
                if (!areEquals) onStructuralInequality(b)
                return areEquals
            }
        })
