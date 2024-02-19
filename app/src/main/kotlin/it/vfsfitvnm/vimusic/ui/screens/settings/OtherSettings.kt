package it.vfsfitvnm.vimusic.ui.screens.settings

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SnapshotMutationPolicy
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerAwareWindowInsets
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.CheckUpdateState
import it.vfsfitvnm.vimusic.enums.MaxStatisticsItems
import it.vfsfitvnm.vimusic.query
import it.vfsfitvnm.vimusic.service.PlayerMediaBrowserService
import it.vfsfitvnm.vimusic.ui.components.themed.HeaderWithIcon
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.utils.checkUpdateStateKey
import it.vfsfitvnm.vimusic.utils.isAtLeastAndroid12
import it.vfsfitvnm.vimusic.utils.isAtLeastAndroid6
import it.vfsfitvnm.vimusic.utils.isIgnoringBatteryOptimizations
import it.vfsfitvnm.vimusic.utils.isInvincibilityEnabledKey
import it.vfsfitvnm.vimusic.utils.isKeepScreenOnEnabledKey
import it.vfsfitvnm.vimusic.utils.isProxyEnabledKey
import it.vfsfitvnm.vimusic.utils.isSwipeToActionEnabledKey
import it.vfsfitvnm.vimusic.utils.maxStatisticsItemsKey
import it.vfsfitvnm.vimusic.utils.pauseSearchHistoryKey
import it.vfsfitvnm.vimusic.utils.proxyHostnameKey
import it.vfsfitvnm.vimusic.utils.proxyModeKey
import it.vfsfitvnm.vimusic.utils.proxyPortKey
import it.vfsfitvnm.vimusic.utils.rememberPreference
import it.vfsfitvnm.vimusic.utils.showStatsListeningTimeKey
import it.vfsfitvnm.vimusic.utils.toast
import kotlinx.coroutines.flow.distinctUntilChanged
import java.net.Proxy

@SuppressLint("BatteryLife")
@ExperimentalAnimationApi
@Composable
fun OtherSettings() {
    val context = LocalContext.current
    val (colorPalette) = LocalAppearance.current

    var isAndroidAutoEnabled by remember {
        val component = ComponentName(context, PlayerMediaBrowserService::class.java)
        val disabledFlag = PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        val enabledFlag = PackageManager.COMPONENT_ENABLED_STATE_ENABLED

        mutableStateOf(
            value = context.packageManager.getComponentEnabledSetting(component) == enabledFlag,
            policy = object : SnapshotMutationPolicy<Boolean> {
                override fun equivalent(a: Boolean, b: Boolean): Boolean {
                    context.packageManager.setComponentEnabledSetting(
                        component,
                        if (b) enabledFlag else disabledFlag,
                        PackageManager.DONT_KILL_APP
                    )
                    return a == b
                }
            }
        )
    }

    var isInvincibilityEnabled by rememberPreference(isInvincibilityEnabledKey, false)

    var isIgnoringBatteryOptimizations by remember {
        mutableStateOf(context.isIgnoringBatteryOptimizations)
    }

    val activityResultLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            isIgnoringBatteryOptimizations = context.isIgnoringBatteryOptimizations
        }

    var isProxyEnabled by rememberPreference(isProxyEnabledKey, false)
    var proxyHost by rememberPreference(proxyHostnameKey, "")
    var proxyPort by rememberPreference(proxyPortKey, 1080)
    var proxyMode by rememberPreference(proxyModeKey, Proxy.Type.HTTP)

    var isKeepScreenOnEnabled by rememberPreference(isKeepScreenOnEnabledKey, false)

    var checkUpdateState by rememberPreference(checkUpdateStateKey, CheckUpdateState.Disabled)

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
            title = stringResource(R.string.tab_miscellaneous),
            iconId = R.drawable.equalizer,
            enabled = false,
            showIcon = true,
            modifier = Modifier,
            onClick = {}
        )

        SettingsEntryGroupText(title = stringResource(R.string.check_update))
        EnumValueSelectorSettingsEntry(
            title = stringResource(R.string.enable_check_for_update),
            selectedValue = checkUpdateState,
            onValueSelected = { checkUpdateState = it },
            valueText = {
                when(it) {
                    CheckUpdateState.Disabled -> stringResource(R.string.vt_disabled)
                    CheckUpdateState.Enabled -> stringResource(R.string.enabled)
                    CheckUpdateState.Ask -> stringResource(R.string.ask)
                }

            }
        )
        SettingsDescription(text = stringResource(R.string.when_enabled_a_new_version_is_checked_and_notified_during_startup))

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
            title = stringResource(R.string.listening_time),
            text = stringResource(R.string.shows_the_number_of_songs_heard_and_their_listening_time),
            isChecked = showStatsListeningTime,
            onCheckedChange = {
                showStatsListeningTime = it
            }
        )

        SettingsGroupSpacer()
        SettingsEntryGroupText(title = stringResource(R.string.proxy))
        SettingsDescription(text = stringResource(R.string.restarting_rimusic_is_required))
        SwitchSettingEntry(
            title = stringResource(R.string.enable_proxy),
            text = "",
            isChecked = isProxyEnabled,
            onCheckedChange = { isProxyEnabled = it }
        )

        AnimatedVisibility(visible = isProxyEnabled) {
            Column {
                EnumValueSelectorSettingsEntry(title = stringResource(R.string.proxy_mode),
                    selectedValue = proxyMode,
                    onValueSelected = { proxyMode = it },
                    valueText = { it.name }
                )
                TextDialogSettingEntry(
                    title = stringResource(R.string.proxy_host),
                    text = proxyHost, //stringResource(R.string.set_proxy_hostname),
                    currentText = proxyHost,
                    onTextSave = { proxyHost = it })
                TextDialogSettingEntry(
                    title = stringResource(R.string.proxy_port),
                    text = proxyPort.toString(), //stringResource(R.string.set_proxy_port),
                    currentText = proxyPort.toString(),
                    onTextSave = { proxyPort = it.toIntOrNull() ?: 1080 })
            }
        }


        SettingsGroupSpacer()
        SettingsEntryGroupText(stringResource(R.string.on_device))
        StringListValueSelectorSettingsEntry(
            title = stringResource(R.string.blacklisted_folders),
            text = stringResource(R.string.edit_blacklist_for_on_device_songs),
            addTitle = stringResource(R.string.add_folder),
            addPlaceholder = "Android/media/com.whatsapp/WhatsApp/Media",
            conflictTitle = stringResource(R.string.this_folder_already_exists),
            removeTitle = stringResource(R.string.are_you_sure_you_want_to_remove_this_folder_from_the_blacklist),
            context = LocalContext.current
        )

        SettingsGroupSpacer()

        SettingsEntryGroupText(title = stringResource(R.string.android_auto))

        SettingsDescription(text = stringResource(R.string.enable_unknown_sources))

        SwitchSettingEntry(
            title = stringResource(R.string.android_auto_1),
            text = stringResource(R.string.enable_android_auto_support),
            isChecked = isAndroidAutoEnabled,
            onCheckedChange = { isAndroidAutoEnabled = it }
        )

        SettingsGroupSpacer()

        SettingsEntryGroupText(title = stringResource(R.string.service_lifetime))

        SwitchSettingEntry(
            title = stringResource(R.string.keep_screen_on),
            text = stringResource(R.string.prevents_screen_timeout),
            isChecked = isKeepScreenOnEnabled,
            onCheckedChange = { isKeepScreenOnEnabled = it }
        )

        ImportantSettingsDescription(text = stringResource(R.string.battery_optimizations_applied))

        if (isAtLeastAndroid12) {
            SettingsDescription(text = stringResource(R.string.is_android12))
        }

        val msgNoBatteryOptim = stringResource(R.string.not_find_battery_optimization_settings)

        SettingsEntry(
            title = stringResource(R.string.ignore_battery_optimizations),
            isEnabled = !isIgnoringBatteryOptimizations,
            text = if (isIgnoringBatteryOptimizations) {
                stringResource(R.string.already_unrestricted)
            } else {
                stringResource(R.string.disable_background_restrictions)
            },
            onClick = {
                if (!isAtLeastAndroid6) return@SettingsEntry

                try {
                    activityResultLauncher.launch(
                        Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                    )
                } catch (e: ActivityNotFoundException) {
                    try {
                        activityResultLauncher.launch(
                            Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                        )
                    } catch (e: ActivityNotFoundException) {
                        context.toast("$msgNoBatteryOptim RiMusic")
                    }
                }
            }
        )

        SwitchSettingEntry(
            title = stringResource(R.string.invincible_service),
            text = stringResource(R.string.turning_off_battery_optimizations_is_not_enough),
            isChecked = isInvincibilityEnabled,
            onCheckedChange = { isInvincibilityEnabled = it }
        )

    }
}
