package it.vfsfitvnm.vimusic.ui.screens.settings

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import it.vfsfitvnm.vimusic.LocalPlayerAwareWindowInsets
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.ColorPaletteMode
import it.vfsfitvnm.vimusic.enums.ColorPaletteName
import it.vfsfitvnm.vimusic.enums.FontType
import it.vfsfitvnm.vimusic.enums.HomeScreenTabs
import it.vfsfitvnm.vimusic.enums.PlayerPlayButtonType
import it.vfsfitvnm.vimusic.enums.PlayerThumbnailSize
import it.vfsfitvnm.vimusic.enums.PlayerTimelineType
import it.vfsfitvnm.vimusic.enums.PlayerVisualizerType
import it.vfsfitvnm.vimusic.enums.ThumbnailRoundness
import it.vfsfitvnm.vimusic.enums.UiType
import it.vfsfitvnm.vimusic.ui.components.themed.HeaderWithIcon
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.utils.UiTypeKey
import it.vfsfitvnm.vimusic.utils.applyFontPaddingKey
import it.vfsfitvnm.vimusic.utils.colorPaletteModeKey
import it.vfsfitvnm.vimusic.utils.colorPaletteNameKey
import it.vfsfitvnm.vimusic.utils.disableIconButtonOnTopKey
import it.vfsfitvnm.vimusic.utils.disablePlayerHorizontalSwipeKey
import it.vfsfitvnm.vimusic.utils.disableScrollingTextKey
import it.vfsfitvnm.vimusic.utils.effectRotationKey
import it.vfsfitvnm.vimusic.utils.fontTypeKey
import it.vfsfitvnm.vimusic.utils.indexNavigationTabKey
import it.vfsfitvnm.vimusic.utils.isAtLeastAndroid13
import it.vfsfitvnm.vimusic.utils.isShowingThumbnailInLockscreenKey
import it.vfsfitvnm.vimusic.utils.lastPlayerPlayButtonTypeKey
import it.vfsfitvnm.vimusic.utils.lastPlayerThumbnailSizeKey
import it.vfsfitvnm.vimusic.utils.lastPlayerTimelineTypeKey
import it.vfsfitvnm.vimusic.utils.lastPlayerVisualizerTypeKey
import it.vfsfitvnm.vimusic.utils.playerPlayButtonTypeKey
import it.vfsfitvnm.vimusic.utils.playerThumbnailSizeKey
import it.vfsfitvnm.vimusic.utils.playerTimelineTypeKey
import it.vfsfitvnm.vimusic.utils.playerVisualizerTypeKey
import it.vfsfitvnm.vimusic.utils.rememberPreference
import it.vfsfitvnm.vimusic.utils.showButtonPlayerAddToPlaylistKey
import it.vfsfitvnm.vimusic.utils.showButtonPlayerArrowKey
import it.vfsfitvnm.vimusic.utils.showButtonPlayerDownloadKey
import it.vfsfitvnm.vimusic.utils.showButtonPlayerLoopKey
import it.vfsfitvnm.vimusic.utils.showButtonPlayerLyricsKey
import it.vfsfitvnm.vimusic.utils.showButtonPlayerShuffleKey
import it.vfsfitvnm.vimusic.utils.showDownloadButtonBackgroundPlayerKey
import it.vfsfitvnm.vimusic.utils.showLikeButtonBackgroundPlayerKey
import it.vfsfitvnm.vimusic.utils.thumbnailRoundnessKey
import it.vfsfitvnm.vimusic.utils.thumbnailTapEnabledKey
import it.vfsfitvnm.vimusic.utils.useSystemFontKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.plus

@ExperimentalAnimationApi
@UnstableApi
@Composable
fun AppearanceSettings() {
    val (colorPalette) = LocalAppearance.current
    val context = LocalContext.current
    val coroutineScope = CoroutineScope(Dispatchers.IO) + Job()

    var colorPaletteName by rememberPreference(colorPaletteNameKey, ColorPaletteName.ModernBlack)
    var colorPaletteMode by rememberPreference(colorPaletteModeKey, ColorPaletteMode.System)
    var thumbnailRoundness by rememberPreference(
        thumbnailRoundnessKey,
        ThumbnailRoundness.Heavy
    )
    var useSystemFont by rememberPreference(useSystemFontKey, false)
    var applyFontPadding by rememberPreference(applyFontPaddingKey, false)
    var isShowingThumbnailInLockscreen by rememberPreference(
        isShowingThumbnailInLockscreenKey,
        true
    )

    var playerPlayButtonType by rememberPreference(playerPlayButtonTypeKey, PlayerPlayButtonType.Rectangular)

    var lastPlayerVisualizerType by rememberPreference(lastPlayerVisualizerTypeKey, PlayerVisualizerType.Disabled)
    var lastPlayerTimelineType by rememberPreference(lastPlayerTimelineTypeKey, PlayerTimelineType.Default)
    var lastPlayerThumbnailSize by rememberPreference(lastPlayerThumbnailSizeKey, PlayerThumbnailSize.Medium)
    var lastPlayerPlayButtonType by rememberPreference(lastPlayerPlayButtonTypeKey, PlayerPlayButtonType.Rectangular)
    var uiType  by rememberPreference(UiTypeKey, UiType.RiMusic)
    var disablePlayerHorizontalSwipe by rememberPreference(disablePlayerHorizontalSwipeKey, false)
    var disableIconButtonOnTop by rememberPreference(disableIconButtonOnTopKey, false)
    var disableScrollingText by rememberPreference(disableScrollingTextKey, false)
    var showLikeButtonBackgroundPlayer by rememberPreference(showLikeButtonBackgroundPlayerKey, true)
    var showDownloadButtonBackgroundPlayer by rememberPreference(showDownloadButtonBackgroundPlayerKey, true)
    var playerVisualizerType by rememberPreference(playerVisualizerTypeKey, PlayerVisualizerType.Disabled)
    var playerTimelineType by rememberPreference(playerTimelineTypeKey, PlayerTimelineType.Default)
    var playerThumbnailSize by rememberPreference(playerThumbnailSizeKey, PlayerThumbnailSize.Medium)

    var effectRotationEnabled by rememberPreference(effectRotationKey, true)

    var thumbnailTapEnabled by rememberPreference(thumbnailTapEnabledKey, false)
    var fontType by rememberPreference(fontTypeKey, FontType.Rubik)

    var showButtonPlayerAddToPlaylist by rememberPreference(showButtonPlayerAddToPlaylistKey, true)
    var showButtonPlayerArrow by rememberPreference(showButtonPlayerArrowKey, false)
    var showButtonPlayerDownload by rememberPreference(showButtonPlayerDownloadKey, true)
    var showButtonPlayerLoop by rememberPreference(showButtonPlayerLoopKey, true)
    var showButtonPlayerLyrics by rememberPreference(showButtonPlayerLyricsKey, true)
    var showButtonPlayerShuffle by rememberPreference(showButtonPlayerShuffleKey, true)

    var indexNavigationTab by rememberPreference(
        indexNavigationTabKey,
        HomeScreenTabs.QuickPics
    )


    Column(
        modifier = Modifier
            .background(colorPalette.background0)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                LocalPlayerAwareWindowInsets.current
                    .only(WindowInsetsSides.Vertical + WindowInsetsSides.End)
                    .asPaddingValues()
            )
    ) {
        HeaderWithIcon(
            title = stringResource(R.string.appearance),
            iconId = R.drawable.color_palette,
            enabled = false,
            showIcon = true,
            modifier = Modifier,
            onClick = {}
        )

        SettingsGroupSpacer()
        SettingsEntryGroupText(stringResource(R.string.user_interface))

        EnumValueSelectorSettingsEntry(
            title = stringResource(R.string.interface_in_use),
            selectedValue = uiType,
            onValueSelected = {
                uiType = it
                if (uiType == UiType.ViMusic) {
                    disablePlayerHorizontalSwipe = true
                    disableIconButtonOnTop = true
                    playerTimelineType = PlayerTimelineType.Default
                    playerVisualizerType = PlayerVisualizerType.Disabled
                    playerThumbnailSize = PlayerThumbnailSize.Medium
                    thumbnailTapEnabled = true
                } else {
                    disablePlayerHorizontalSwipe = false
                    disableIconButtonOnTop = false
                    playerTimelineType = lastPlayerTimelineType
                    playerVisualizerType = lastPlayerVisualizerType
                    playerThumbnailSize = lastPlayerThumbnailSize
                    playerPlayButtonType = lastPlayerPlayButtonType
                }

            },
            valueText = {
                when(it) {
                    UiType.RiMusic -> UiType.RiMusic.name
                    UiType.ViMusic -> UiType.ViMusic.name
                }
            }
        )

        EnumValueSelectorSettingsEntry(
            title = stringResource(R.string.theme),
            selectedValue = colorPaletteName,
            onValueSelected = { colorPaletteName = it },
            valueText = {
                when (it) {
                    ColorPaletteName.Default -> stringResource(R.string._default)
                    ColorPaletteName.Dynamic -> stringResource(R.string.dynamic)
                    ColorPaletteName.PureBlack -> ColorPaletteName.PureBlack.name
                    ColorPaletteName.ModernBlack -> ColorPaletteName.ModernBlack.name
                }
            }
        )

        EnumValueSelectorSettingsEntry(
            title = stringResource(R.string.theme_mode),
            selectedValue = colorPaletteMode,
            isEnabled = when (colorPaletteName) {
                ColorPaletteName.PureBlack -> false
                ColorPaletteName.ModernBlack -> false
                else -> { true }
            },
            onValueSelected = { colorPaletteMode = it },
            valueText = {
                when (it) {
                    ColorPaletteMode.Dark -> stringResource(R.string.dark)
                    ColorPaletteMode.Light -> stringResource(R.string._light)
                    ColorPaletteMode.System -> stringResource(R.string.system)
                }
            }
        )


        EnumValueSelectorSettingsEntry(
            title = stringResource(R.string.default_page),
            selectedValue = indexNavigationTab,
            onValueSelected = {indexNavigationTab = it},
            valueText = {
                when (it) {
                    HomeScreenTabs.Default -> stringResource(R.string._default)
                    HomeScreenTabs.QuickPics -> stringResource(R.string.quick_picks)
                    HomeScreenTabs.Songs -> stringResource(R.string.songs)
                    HomeScreenTabs.Albums -> stringResource(R.string.albums)
                    HomeScreenTabs.Artists -> stringResource(R.string.artists)
                    HomeScreenTabs.Library -> stringResource(R.string.library)
                    HomeScreenTabs.Discovery -> stringResource(R.string.discovery)
                }
            }
        )

        SettingsGroupSpacer()
        SettingsEntryGroupText(title = stringResource(R.string.player))

        EnumValueSelectorSettingsEntry(
            title = stringResource(R.string.player_thumbnail_size),
            selectedValue = playerThumbnailSize,
            onValueSelected = { playerThumbnailSize = it },
            valueText = {
                when (it) {
                    PlayerThumbnailSize.Small -> stringResource(R.string.small)
                    PlayerThumbnailSize.Medium -> stringResource(R.string.medium)
                    PlayerThumbnailSize.Big -> stringResource(R.string.big)
                    PlayerThumbnailSize.Biggest -> stringResource(R.string.biggest)
                }
            }
        )

        EnumValueSelectorSettingsEntry(
            title = stringResource(R.string.thumbnail_roundness),
            selectedValue = thumbnailRoundness,
            onValueSelected = { thumbnailRoundness = it },
            trailingContent = {
                Spacer(
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = colorPalette.accent,
                            shape = thumbnailRoundness.shape()
                        )
                        .background(
                            color = colorPalette.background1,
                            shape = thumbnailRoundness.shape()
                        )
                        .size(36.dp)
                )
            },
            valueText = {
                when (it) {
                    ThumbnailRoundness.None -> stringResource(R.string.none)
                    ThumbnailRoundness.Light -> stringResource(R.string.light)
                    ThumbnailRoundness.Heavy -> stringResource(R.string.heavy)
                    ThumbnailRoundness.Medium -> stringResource(R.string.medium)
                }
            }
        )

        SwitchSettingEntry(
            title = stringResource(R.string.disable_scrolling_text),
            text = stringResource(R.string.scrolling_text_is_used_for_long_texts),
            isChecked = disableScrollingText,
            onCheckedChange = { disableScrollingText = it }
        )

        SwitchSettingEntry(
            title = stringResource(R.string.disable_horizontal_swipe),
            text = stringResource(R.string.disable_song_switching_via_swipe),
            isChecked = disablePlayerHorizontalSwipe,
            onCheckedChange = { disablePlayerHorizontalSwipe = it }
        )

        EnumValueSelectorSettingsEntry(
            title = stringResource(R.string.timeline),
            selectedValue = playerTimelineType,
            onValueSelected = { playerTimelineType = it },
            valueText = {
                when (it) {
                    PlayerTimelineType.Default -> stringResource(R.string._default)
                    PlayerTimelineType.Wavy -> stringResource(R.string.wavy_timeline)
                    PlayerTimelineType.BodiedBar -> stringResource(R.string.bodied_bar)
                    PlayerTimelineType.PinBar -> stringResource(R.string.pin_bar)
                }
            }
        )

        EnumValueSelectorSettingsEntry(
            title = stringResource(R.string.play_button),
            selectedValue = playerPlayButtonType,
            onValueSelected = {
                playerPlayButtonType = it
                lastPlayerPlayButtonType = it
            },
            valueText = {
                when (it) {
                    PlayerPlayButtonType.Default -> stringResource(R.string._default)
                    PlayerPlayButtonType.Rectangular -> stringResource(R.string.rectangular)
                    PlayerPlayButtonType.Square -> stringResource(R.string.square)
                    PlayerPlayButtonType.CircularRibbed -> stringResource(R.string.circular_ribbed)
                }
            },
            isEnabled = uiType != UiType.ViMusic
        )


        SwitchSettingEntry(
            title = stringResource(R.string.player_rotating_buttons),
            text = stringResource(R.string.player_enable_rotation_buttons),
            isChecked = effectRotationEnabled,
            onCheckedChange = { effectRotationEnabled = it }
        )

        EnumValueSelectorSettingsEntry(
            title = stringResource(R.string.visualizer),
            selectedValue = playerVisualizerType,
            onValueSelected = { playerVisualizerType = it },
            valueText = {
                when (it) {
                    PlayerVisualizerType.Fancy -> stringResource(R.string.vt_fancy)
                    PlayerVisualizerType.Circular -> stringResource(R.string.vt_circular)
                    PlayerVisualizerType.Disabled -> stringResource(R.string.vt_disabled)
                    PlayerVisualizerType.Stacked -> stringResource(R.string.vt_stacked)
                    PlayerVisualizerType.Oneside -> stringResource(R.string.vt_one_side)
                    PlayerVisualizerType.Doubleside -> stringResource(R.string.vt_double_side)
                    PlayerVisualizerType.DoublesideCircular -> stringResource(R.string.vt_double_side_circular)
                    PlayerVisualizerType.Full -> stringResource(R.string.vt_full)
                }
            }
        )
        ImportantSettingsDescription(text = stringResource(R.string.visualizer_require_mic_permission))

        SwitchSettingEntry(
            title = stringResource(R.string.toggle_lyrics),
            text = stringResource(R.string.by_tapping_on_the_thumbnail),
            isChecked = thumbnailTapEnabled,
            onCheckedChange = { thumbnailTapEnabled = it }
        )

        SettingsGroupSpacer()
        SettingsEntryGroupText(title = stringResource(R.string.action_bar_button))

        SwitchSettingEntry(
            title = stringResource(R.string.action_bar_show_download_button),
            text = "",
            isChecked = showButtonPlayerDownload,
            onCheckedChange = { showButtonPlayerDownload = it }
        )

        SwitchSettingEntry(
            title = stringResource(R.string.action_bar_show_add_to_playlist_button),
            text = "",
            isChecked = showButtonPlayerAddToPlaylist,
            onCheckedChange = { showButtonPlayerAddToPlaylist = it }
        )

        SwitchSettingEntry(
            title = stringResource(R.string.action_bar_show_loop_button),
            text = "",
            isChecked = showButtonPlayerLoop,
            onCheckedChange = { showButtonPlayerLoop = it }
        )

        SwitchSettingEntry(
            title = stringResource(R.string.action_bar_show_shuffle_button),
            text = "",
            isChecked = showButtonPlayerShuffle,
            onCheckedChange = { showButtonPlayerShuffle = it }
        )

        SwitchSettingEntry(
            title = stringResource(R.string.action_bar_show_lyrics_button),
            text = "",
            isChecked = showButtonPlayerLyrics,
            onCheckedChange = { showButtonPlayerLyrics = it }
        )

        SwitchSettingEntry(
            title = stringResource(R.string.action_bar_show_arrow_button),
            text = "",
            isChecked = showButtonPlayerArrow,
            onCheckedChange = { showButtonPlayerArrow = it }
        )

        SettingsGroupSpacer()
        SettingsEntryGroupText(title = stringResource(R.string.background_player))

        SwitchSettingEntry(
            title = stringResource(R.string.show_favorite_button),
            text = stringResource(R.string.show_favorite_button_in_lock_screen_and_notification_area),
            isChecked = showLikeButtonBackgroundPlayer,
            onCheckedChange = { showLikeButtonBackgroundPlayer = it }
        )
        ImportantSettingsDescription(text = stringResource(R.string.restarting_rimusic_is_required))
        SwitchSettingEntry(
            title = stringResource(R.string.show_download_button),
            text = stringResource(R.string.show_download_button_in_lock_screen_and_notification_area),
            isChecked = showDownloadButtonBackgroundPlayer,
            onCheckedChange = { showDownloadButtonBackgroundPlayer = it }
        )

        ImportantSettingsDescription(text = stringResource(R.string.restarting_rimusic_is_required))

        SettingsGroupSpacer()
        SettingsEntryGroupText(title = stringResource(R.string.text))

        EnumValueSelectorSettingsEntry(
            title = "Use font type",
            selectedValue = fontType,
            onValueSelected = { fontType = it },
            valueText = {
                when (it) {
                    FontType.Rubik -> FontType.Rubik.name
                    FontType.Poppins -> FontType.Poppins.name
                }
            }
        )

        SwitchSettingEntry(
            title = stringResource(R.string.use_system_font),
            text = stringResource(R.string.use_font_by_the_system),
            isChecked = useSystemFont,
            onCheckedChange = { useSystemFont = it }
        )

        SwitchSettingEntry(
            title = stringResource(R.string.apply_font_padding),
            text = stringResource(R.string.add_spacing_around_texts),
            isChecked = applyFontPadding,
            onCheckedChange = { applyFontPadding = it }
        )

        if (!isAtLeastAndroid13) {
            SettingsGroupSpacer()

            SettingsEntryGroupText(title = stringResource(R.string.lockscreen))

            SwitchSettingEntry(
                title = stringResource(R.string.show_song_cover),
                text = stringResource(R.string.use_song_cover_on_lockscreen),
                isChecked = isShowingThumbnailInLockscreen,
                onCheckedChange = { isShowingThumbnailInLockscreen = it }
            )
        }
    }
}
