package it.vfsfitvnm.vimusic.ui.screens.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.media.audiofx.AudioEffect
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.BuildCompat
import androidx.core.os.LocaleListCompat
import androidx.media3.common.util.UnstableApi
import it.vfsfitvnm.vimusic.LocalPlayerAwareWindowInsets
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.AudioQualityFormat
import it.vfsfitvnm.vimusic.enums.ExoPlayerMinTimeForEvent
import it.vfsfitvnm.vimusic.enums.Languages
import it.vfsfitvnm.vimusic.enums.MaxStatisticsItems
import it.vfsfitvnm.vimusic.enums.PlayEventsType
import it.vfsfitvnm.vimusic.enums.ThumbnailRoundness
import it.vfsfitvnm.vimusic.ui.components.themed.HeaderWithIcon
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.shimmer
import it.vfsfitvnm.vimusic.utils.SwipeToReveal
import it.vfsfitvnm.vimusic.utils.audioQualityFormatKey
import it.vfsfitvnm.vimusic.utils.closeWithBackButtonKey
import it.vfsfitvnm.vimusic.utils.closebackgroundPlayerKey
import it.vfsfitvnm.vimusic.utils.exoPlayerMinTimeForEventKey
import it.vfsfitvnm.vimusic.utils.isAtLeastAndroid6
import it.vfsfitvnm.vimusic.utils.isAvailableUpdate
import it.vfsfitvnm.vimusic.utils.languageAppKey
import it.vfsfitvnm.vimusic.utils.maxStatisticsItemsKey
import it.vfsfitvnm.vimusic.utils.persistentQueueKey
import it.vfsfitvnm.vimusic.utils.playEventsTypeKey
import it.vfsfitvnm.vimusic.utils.rememberPreference
import it.vfsfitvnm.vimusic.utils.resumePlaybackWhenDeviceConnectedKey
import it.vfsfitvnm.vimusic.utils.showStatsListeningTimeKey
import it.vfsfitvnm.vimusic.utils.skipSilenceKey
import it.vfsfitvnm.vimusic.utils.thumbnailRoundnessKey
import it.vfsfitvnm.vimusic.utils.toast
import it.vfsfitvnm.vimusic.utils.volumeNormalizationKey


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
    var audioQualityFormat by rememberPreference(audioQualityFormatKey, AudioQualityFormat.Auto)

    val activityResultLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { }

    val newVersion = isAvailableUpdate()
    var thumbnailRoundness by rememberPreference(
        thumbnailRoundnessKey,
        ThumbnailRoundness.Heavy
    )
    val uriHandler = LocalUriHandler.current

    var maxStatisticsItems by rememberPreference(
        maxStatisticsItemsKey,
        MaxStatisticsItems.`10`
    )

    var showStatsListeningTime by rememberPreference(showStatsListeningTimeKey,   true)

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
        if (newVersion != "") {
            //SettingsEntryGroupText(title = "Update available")
            SettingsEntry(
                title = "New version $newVersion",
                text = "Click here to open page",
                onClick = {
                    uriHandler.openUri("https://github.com/fast4x/RiMusic/releases/latest")
                    //uriHandler.openUri("https://github.com/fast4x/RiMusic/releases/tag/v0.6.9")
                },
                trailingContent = {
                    Image(
                        painter = painterResource(R.drawable.update),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(colorPalette.shimmer),
                        modifier = Modifier
                            .size(34.dp)
                    )
                },
                modifier = Modifier
                    .background(
                        color = colorPalette.background4,
                        shape = thumbnailRoundness.shape()
                    )

            )
        }

        SettingsGroupSpacer()
        SettingsEntryGroupText(title = stringResource(R.string.languages))

        SettingsDescription(text = stringResource(R.string.system_language)+": $systemLocale")

        EnumValueSelectorSettingsEntry(
            title = stringResource(R.string.app_language),
            selectedValue = languageApp,
            onValueSelected = {languageApp = it },
            valueText = {
                when (it){
                    Languages.System -> stringResource(R.string.system_language)
                    Languages.Arabic -> stringResource(R.string.arabic)
                    Languages.Bashkir -> stringResource(R.string.bashkir)
                    Languages.Catalan -> stringResource(R.string.catalan)
                    Languages.ChineseSimplified -> stringResource(R.string.chinese_simplified)
                    Languages.ChineseTraditional -> stringResource(R.string.chinese_traditional)
                    Languages.Czech -> stringResource(R.string.czech)
                    Languages.English -> stringResource(R.string.english)
                    Languages.Esperanto -> stringResource(R.string.esperanto)
                    Languages.French -> stringResource(R.string.french)
                    //Languages.FrenchEmo -> stringResource(R.string.french_emoticons_fran_ais)
                    Languages.German -> stringResource(R.string.german)
                    Languages.Greek -> stringResource(R.string.greek)
                    Languages.Hebrew -> stringResource(R.string.lang_hebrew)
                    Languages.Hungarian -> stringResource(R.string.hungarian)
                    Languages.Indonesian -> stringResource(R.string.indonesian)
                    Languages.Japanese -> stringResource(R.string.lang_japanese)
                    Languages.Korean -> stringResource(R.string.korean)
                    Languages.Italian -> stringResource(R.string.italian)
                    Languages.Odia -> stringResource(R.string.odia)
                    Languages.Persian -> stringResource(R.string.persian)
                    Languages.Polish -> stringResource(R.string.polish)
                    Languages.PortugueseBrazilian -> stringResource(R.string.portuguese_brazilian)
                    Languages.Portuguese -> stringResource(R.string.portuguese)
                    Languages.Romanian -> stringResource(R.string.romanian)
                    //Languages.RomanianEmo -> stringResource(R.string.romanian_emoticons_rom_n)
                    Languages.Russian -> stringResource(R.string.russian)
                    Languages.Spanish -> stringResource(R.string.spanish)
                    Languages.Turkish -> stringResource(R.string.turkish)
                    Languages.Ukrainian -> stringResource(R.string.lang_ukrainian)
                    Languages.Vietnamese -> "Vietnamese"
                }
            }
        )

        SettingsGroupSpacer()
        SettingsEntryGroupText(stringResource(R.string.player))

        EnumValueSelectorSettingsEntry(
            title = stringResource(R.string.audio_quality_format),
            selectedValue = audioQualityFormat,
            onValueSelected = { audioQualityFormat = it },
            valueText = {
                when (it) {
                    AudioQualityFormat.Auto -> stringResource(R.string.audio_quality_automatic)
                    AudioQualityFormat.High -> stringResource(R.string.audio_quality_format_high)
                    AudioQualityFormat.Medium -> stringResource(R.string.audio_quality_format_medium)
                    AudioQualityFormat.Low -> stringResource(R.string.audio_quality_format_low)
                }
            }
        )

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

        /*
        EnumValueSelectorSettingsEntry(
            title = stringResource(R.string.tips),
            selectedValue = playEventType,
            onValueSelected = { playEventType = it },
            valueText = {
                when (it) {
                    PlayEventsType.MostPlayed -> stringResource(R.string.by_most_played_song)
                    PlayEventsType.LastPlayed -> stringResource(R.string.by_last_played_song)
                }
            }
        )
        */

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
            title = stringResource(R.string.close_app_with_back_button),
            text = stringResource(R.string.when_you_use_the_back_button_from_the_home_page),
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


        SettingsGroupSpacer()
        SettingsEntryGroupText(stringResource(R.string.statistics))

        EnumValueSelectorSettingsEntry(
            title = stringResource(R.string.statistics_max_number_of_items),
            selectedValue = maxStatisticsItems,
            onValueSelected = { maxStatisticsItems = it },
            valueText = {
                it.number.toString()
            }
        )

        SwitchSettingEntry(
            title = "Listening time",
            text = "Shows the number of songs heard and their listening time",
            isChecked = showStatsListeningTime,
            onCheckedChange = {
                showStatsListeningTime = it
            }
        )

        SettingsGroupSpacer()

    }
}
