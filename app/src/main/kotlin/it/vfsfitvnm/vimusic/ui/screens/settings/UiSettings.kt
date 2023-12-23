package it.vfsfitvnm.vimusic.ui.screens.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.media.audiofx.AudioEffect
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.os.BuildCompat
import androidx.core.os.LocaleListCompat
import androidx.media3.common.util.UnstableApi
import it.vfsfitvnm.vimusic.LocalPlayerAwareWindowInsets
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.ColorPaletteMode
import it.vfsfitvnm.vimusic.enums.ColorPaletteName
import it.vfsfitvnm.vimusic.enums.ExoPlayerMinTimeForEvent
import it.vfsfitvnm.vimusic.enums.Languages
import it.vfsfitvnm.vimusic.enums.NavigationTab
import it.vfsfitvnm.vimusic.enums.PlayerPlayButtonType
import it.vfsfitvnm.vimusic.enums.PlayerThumbnailSize
import it.vfsfitvnm.vimusic.enums.PlayerTimelineType
import it.vfsfitvnm.vimusic.enums.PlayerVisualizerType
import it.vfsfitvnm.vimusic.enums.ThumbnailRoundness
import it.vfsfitvnm.vimusic.enums.UiType
import it.vfsfitvnm.vimusic.service.MyDownloadService
import it.vfsfitvnm.vimusic.service.PlayerService
import it.vfsfitvnm.vimusic.ui.components.themed.HeaderWithIcon
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.utils.UiTypeKey
import it.vfsfitvnm.vimusic.utils.applyFontPaddingKey
import it.vfsfitvnm.vimusic.utils.closeWithBackButtonKey
import it.vfsfitvnm.vimusic.utils.closebackgroundPlayerKey
import it.vfsfitvnm.vimusic.utils.colorPaletteModeKey
import it.vfsfitvnm.vimusic.utils.colorPaletteNameKey
import it.vfsfitvnm.vimusic.utils.disableIconButtonOnTopKey
import it.vfsfitvnm.vimusic.utils.disablePlayerHorizontalSwipeKey
import it.vfsfitvnm.vimusic.utils.exoPlayerMinTimeForEventKey
import it.vfsfitvnm.vimusic.utils.indexNavigationTabKey
import it.vfsfitvnm.vimusic.utils.intent
import it.vfsfitvnm.vimusic.utils.isAtLeastAndroid13
import it.vfsfitvnm.vimusic.utils.isAtLeastAndroid6
import it.vfsfitvnm.vimusic.utils.isShowingThumbnailInLockscreenKey
import it.vfsfitvnm.vimusic.utils.languageAppKey
import it.vfsfitvnm.vimusic.utils.lastPlayerPlayButtonTypeKey
import it.vfsfitvnm.vimusic.utils.lastPlayerThumbnailSizeKey
import it.vfsfitvnm.vimusic.utils.lastPlayerTimelineTypeKey
import it.vfsfitvnm.vimusic.utils.lastPlayerVisualizerTypeKey
import it.vfsfitvnm.vimusic.utils.persistentQueueKey
import it.vfsfitvnm.vimusic.utils.playerPlayButtonTypeKey
import it.vfsfitvnm.vimusic.utils.playerThumbnailSizeKey
import it.vfsfitvnm.vimusic.utils.playerTimelineTypeKey
import it.vfsfitvnm.vimusic.utils.playerVisualizerTypeKey
import it.vfsfitvnm.vimusic.utils.rememberPreference
import it.vfsfitvnm.vimusic.utils.resumePlaybackWhenDeviceConnectedKey
import it.vfsfitvnm.vimusic.utils.skipSilenceKey
import it.vfsfitvnm.vimusic.utils.thumbnailRoundnessKey
import it.vfsfitvnm.vimusic.utils.toast
import it.vfsfitvnm.vimusic.utils.useSystemFontKey
import it.vfsfitvnm.vimusic.utils.volumeNormalizationKey
import kotlin.system.exitProcess

@androidx.annotation.OptIn(androidx.core.os.BuildCompat.PrereleaseSdkCheck::class)
@ExperimentalAnimationApi
@UnstableApi
@Composable
fun  UiSettings() {
    val (colorPalette) = LocalAppearance.current
    val binder = LocalPlayerServiceBinder.current
    val context = LocalContext.current

    var languageApp  by rememberPreference(languageAppKey, Languages.English)
    val systemLocale = LocaleListCompat.getDefault().get(0).toString()
    languageApp.code = systemLocale

    //Log.d("LanguageSystem",systemLocale.toString() +"  "+ languageApp.name)

    var exoPlayerMinTimeForEvent by rememberPreference(
        exoPlayerMinTimeForEventKey,
        ExoPlayerMinTimeForEvent.`20s`
    )
    var persistentQueue by rememberPreference(persistentQueueKey, false)
    var closebackgroundPlayer by rememberPreference(closebackgroundPlayerKey, false)
    var closeWithBackButton by rememberPreference(closeWithBackButtonKey, false)
    var resumePlaybackWhenDeviceConnected by rememberPreference(
        resumePlaybackWhenDeviceConnectedKey,
        false
    )

    var skipSilence by rememberPreference(skipSilenceKey, false)
    var volumeNormalization by rememberPreference(volumeNormalizationKey, false)

    val activityResultLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { }

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

        SettingsGroupSpacer()
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
                    Languages.PortugueseBrazilian -> stringResource(R.string.portuguese_brazilian)
                    Languages.Portuguese -> stringResource(R.string.portuguese)
                    Languages.Indonesian -> stringResource(R.string.indonesian)
                    Languages.Odia -> stringResource(R.string.odia)
                    Languages.Bashkir -> stringResource(R.string.bashkir)
                    Languages.Esperanto -> stringResource(R.string.esperanto)
                }
            }
        )

        SettingsGroupSpacer()
        SettingsEntryGroupText(stringResource(R.string.player))


        EnumValueSelectorSettingsEntry(
            title = stringResource(R.string.min_listening_time),
            selectedValue = exoPlayerMinTimeForEvent,
            onValueSelected = { exoPlayerMinTimeForEvent = it },
            valueText = {
                when (it) {
                    ExoPlayerMinTimeForEvent.`10s` -> "10s"
                    ExoPlayerMinTimeForEvent.`15s` -> "15s"
                    ExoPlayerMinTimeForEvent.`20s` -> "20s"
                    ExoPlayerMinTimeForEvent.`30s` -> "30s"
                    ExoPlayerMinTimeForEvent.`40s` -> "40s"
                    ExoPlayerMinTimeForEvent.`60s` -> "60s"
                }
            }
        )
        SettingsDescription(text = stringResource(R.string.is_min_list_time_for_tips_or_quick_pics))

        SwitchSettingEntry(
            title = stringResource(R.string.persistent_queue),
            text = stringResource(R.string.save_and_restore_playing_songs),
            isChecked = persistentQueue,
            onCheckedChange = {
                persistentQueue = it
            }
        )


        if (isAtLeastAndroid6) {
            SwitchSettingEntry(
                title = stringResource(R.string.resume_playback),
                text = stringResource(R.string.when_device_is_connected),
                isChecked = resumePlaybackWhenDeviceConnected,
                onCheckedChange = {
                    resumePlaybackWhenDeviceConnected = it
                }
            )
        }

        SwitchSettingEntry(
            isEnabled = if (BuildCompat.isAtLeastT()) true else false,
            title = "Close app with Back Button",
            text = "When you use the back button from the home page",
            isChecked = closeWithBackButton,
            onCheckedChange = {
                closeWithBackButton = it
            }
        )
        SettingsDescription(text = stringResource(R.string.restarting_rimusic_is_required))

        SwitchSettingEntry(
            title = stringResource(R.string.close_background_player),
            text = stringResource(R.string.when_app_swipe_out_from_task_manager),
            isChecked = closebackgroundPlayer,
            onCheckedChange = {
                closebackgroundPlayer = it
            }
        )

        SwitchSettingEntry(
            title = stringResource(R.string.skip_silence),
            text = stringResource(R.string.skip_silent_parts_during_playback),
            isChecked = skipSilence,
            onCheckedChange = {
                skipSilence = it
            }
        )

        SwitchSettingEntry(
            title = stringResource(R.string.loudness_normalization),
            text = stringResource(R.string.autoadjust_the_volume),
            isChecked = volumeNormalization,
            onCheckedChange = {
                volumeNormalization = it
            }
        )

        SettingsEntry(
            title = stringResource(R.string.equalizer),
            text = stringResource(R.string.interact_with_the_system_equalizer),
            onClick = {
                val intent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
                    putExtra(AudioEffect.EXTRA_AUDIO_SESSION, binder?.player?.audioSessionId)
                    putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
                    putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                }

                try {
                    activityResultLauncher.launch(intent)
                } catch (e: ActivityNotFoundException) {
                    context.toast("Couldn't find an application to equalize audio")
                }
            }
        )




    }
}
