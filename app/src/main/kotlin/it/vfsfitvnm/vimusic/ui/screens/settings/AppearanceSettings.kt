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
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import it.vfsfitvnm.vimusic.LocalPlayerAwareWindowInsets
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.ColorPaletteMode
import it.vfsfitvnm.vimusic.enums.ColorPaletteName
import it.vfsfitvnm.vimusic.enums.Languages
import it.vfsfitvnm.vimusic.enums.NavigationTab
import it.vfsfitvnm.vimusic.enums.ThumbnailRoundness
import it.vfsfitvnm.vimusic.service.MyDownloadService
import it.vfsfitvnm.vimusic.service.PlayerService
import it.vfsfitvnm.vimusic.ui.components.themed.HeaderWithIcon
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.utils.applyFontPaddingKey
import it.vfsfitvnm.vimusic.utils.colorPaletteModeKey
import it.vfsfitvnm.vimusic.utils.colorPaletteNameKey
import it.vfsfitvnm.vimusic.utils.indexNavigationTabKey
import it.vfsfitvnm.vimusic.utils.intent
import it.vfsfitvnm.vimusic.utils.isAtLeastAndroid13
import it.vfsfitvnm.vimusic.utils.isShowingThumbnailInLockscreenKey
import it.vfsfitvnm.vimusic.utils.languageAppKey
import it.vfsfitvnm.vimusic.utils.rememberPreference
import it.vfsfitvnm.vimusic.utils.thumbnailRoundnessKey
import it.vfsfitvnm.vimusic.utils.useSystemFontKey

@ExperimentalAnimationApi
@UnstableApi
@Composable
fun AppearanceSettings() {
    val (colorPalette) = LocalAppearance.current
    var languageApp  by rememberPreference(languageAppKey, Languages.English)
    val systemLocale = LocaleListCompat.getDefault().get(0).toString()
        languageApp.code = systemLocale

    //Log.d("LanguageSystem",systemLocale.toString() +"  "+ languageApp.name)

    var colorPaletteName by rememberPreference(colorPaletteNameKey, ColorPaletteName.PureBlack)
    var colorPaletteMode by rememberPreference(colorPaletteModeKey, ColorPaletteMode.System)
    var thumbnailRoundness by rememberPreference(
        thumbnailRoundnessKey,
        ThumbnailRoundness.Heavy
    )
    var useSystemFont by rememberPreference(useSystemFontKey, false)
    var applyFontPadding by rememberPreference(applyFontPaddingKey, false)
    var isShowingThumbnailInLockscreen by rememberPreference(
        isShowingThumbnailInLockscreenKey,
        false
    )
    var navTabIndex by rememberPreference(
        indexNavigationTabKey,
        NavigationTab.Default
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

        SettingsEntryGroupText(title = stringResource(R.string.languages))

        EnumValueSelectorSettingsEntry(
            title = stringResource(R.string.app_language),
            selectedValue = languageApp,
            onValueSelected = {languageApp = it },
            valueText = {
                when (it){
                    Languages.System -> stringResource(R.string.system_language)
                    Languages.English -> stringResource(R.string.english)
                    Languages.Italian -> stringResource(R.string.italian)
                    Languages.Czech -> stringResource(R.string.czech)
                    Languages.German -> stringResource(R.string.german)
                    Languages.Spanish -> stringResource(R.string.spanish)
                    Languages.French -> stringResource(R.string.french)
                    Languages.FrenchEmo -> stringResource(R.string.french_emoticons_fran_ais)
                    Languages.Romanian -> stringResource(R.string.romanian)
                    Languages.RomanianEmo -> stringResource(R.string.romanian_emoticons_rom_n)
                    Languages.Russian -> stringResource(R.string.russian)
                    Languages.Turkish -> stringResource(R.string.turkish)
                    Languages.Polish -> stringResource(R.string.polish)
                    Languages.PortugueseBrazilian -> "Portuguese, Brazilian"
                    Languages.Portuguese -> "Portuguese"
                    Languages.Indonesian -> "Indonesian"
                    Languages.Odia -> "Odia"
                }
            }
        )



        /*
        SettingsEntryGroupText(title = "Home")

        EnumValueSelectorSettingsEntry(
            title = "Tab",
            selectedValue = navTabIndex,
            onValueSelected = { navTabIndex = it }
        )
        */


        SettingsEntryGroupText(title = stringResource(R.string.colors))

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

        SettingsGroupSpacer()

        SettingsEntryGroupText(title = stringResource(R.string.shapes))

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


        SettingsGroupSpacer()

        SettingsEntryGroupText(title = stringResource(R.string.text))

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
