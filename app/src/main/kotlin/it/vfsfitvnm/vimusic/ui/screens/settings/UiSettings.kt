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
import androidx.core.os.LocaleListCompat
import androidx.media3.common.util.UnstableApi
import it.vfsfitvnm.vimusic.LocalPlayerAwareWindowInsets
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.ColorPaletteMode
import it.vfsfitvnm.vimusic.enums.ColorPaletteName
import it.vfsfitvnm.vimusic.enums.Languages
import it.vfsfitvnm.vimusic.enums.NavigationTab
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
import it.vfsfitvnm.vimusic.utils.indexNavigationTabKey
import it.vfsfitvnm.vimusic.utils.isAtLeastAndroid13
import it.vfsfitvnm.vimusic.utils.isShowingThumbnailInLockscreenKey
import it.vfsfitvnm.vimusic.utils.languageAppKey
import it.vfsfitvnm.vimusic.utils.playerThumbnailSizeKey
import it.vfsfitvnm.vimusic.utils.playerTimelineTypeKey
import it.vfsfitvnm.vimusic.utils.playerVisualizerTypeKey
import it.vfsfitvnm.vimusic.utils.rememberPreference
import it.vfsfitvnm.vimusic.utils.thumbnailRoundnessKey
import it.vfsfitvnm.vimusic.utils.useSystemFontKey

@ExperimentalAnimationApi
@UnstableApi
@Composable
fun UiSettings() {
    val (colorPalette) = LocalAppearance.current
    var uiType  by rememberPreference(UiTypeKey, UiType.RiMusic)
    var disablePlayerHorizontalSwipe by rememberPreference(disablePlayerHorizontalSwipeKey, false)
    var disableIconButtonOnTop by rememberPreference(disableIconButtonOnTopKey, false)
    var playerVisualizerType by rememberPreference(playerVisualizerTypeKey, PlayerVisualizerType.Disabled)
    var playerTimelineType by rememberPreference(playerTimelineTypeKey, PlayerTimelineType.Default)
    var playerThumbnailSize by rememberPreference(playerThumbnailSizeKey, PlayerThumbnailSize.Medium)

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
            title = stringResource(R.string.user_interface),
            iconId = R.drawable.app_icon,
            enabled = false,
            showIcon = true,
            modifier = Modifier,
            onClick = {}
        )

        SettingsEntryGroupText(stringResource(R.string.appearance))

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
                } else {
                    disablePlayerHorizontalSwipe = false
                    disableIconButtonOnTop = false
                    playerTimelineType = PlayerTimelineType.Wavy
                    playerVisualizerType = PlayerVisualizerType.Disabled
                    playerThumbnailSize = PlayerThumbnailSize.Medium
                }

            },
            valueText = {
                when(it) {
                    UiType.RiMusic -> UiType.RiMusic.name
                    UiType.ViMusic -> UiType.ViMusic.name
                }
            }
        )
/*
        SwitchSettingEntry(
            title = "Disable button on top",
            text = "The search/other button appears at the bottom of the screen",
            isChecked = disableIconButtonOnTop,
            onCheckedChange = { disableIconButtonOnTop = it },
            isEnabled =
                when (uiType) {
                    UiType.RiMusic -> false
                    UiType.ViMusic -> true
                }
        )
 */

        SettingsGroupSpacer()

        SettingsEntryGroupText(title = stringResource(R.string.player))

        SwitchSettingEntry(
            title = stringResource(R.string.disable_horizontal_swipe),
            text = stringResource(R.string.disable_song_switching_via_swipe),
            isChecked = disablePlayerHorizontalSwipe,
            onCheckedChange = { disablePlayerHorizontalSwipe = it }
        )

        EnumValueSelectorSettingsEntry(
            title = stringResource(R.string.player_thumbnail_size),
            selectedValue = playerThumbnailSize,
            onValueSelected = { playerThumbnailSize = it },
            valueText = {
                when (it) {
                    PlayerThumbnailSize.Small -> stringResource(R.string.small)
                    PlayerThumbnailSize.Medium -> stringResource(R.string.medium)
                    PlayerThumbnailSize.Big -> stringResource(R.string.big)
                    PlayerThumbnailSize.Biggest -> "Biggest"
                }
            }
        )

        EnumValueSelectorSettingsEntry(
            title = stringResource(R.string.timeline),
            selectedValue = playerTimelineType,
            onValueSelected = { playerTimelineType = it },
            valueText = {
                when (it) {
                    PlayerTimelineType.Default -> stringResource(R.string._default)
                    PlayerTimelineType.Wavy -> stringResource(R.string.wavy_timeline)
                }
            }
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

    }
}
