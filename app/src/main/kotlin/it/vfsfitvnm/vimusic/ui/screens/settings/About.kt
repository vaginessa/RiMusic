package it.vfsfitvnm.vimusic.ui.screens.settings

//import it.vfsfitvnm.vimusic.BuildConfig
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.vimusic.BuildConfig
import it.vfsfitvnm.vimusic.LocalPlayerAwareWindowInsets
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.ThumbnailRoundness
import it.vfsfitvnm.vimusic.ui.components.themed.HeaderWithIcon
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.shimmer
import it.vfsfitvnm.vimusic.utils.isAvailableUpdate
import it.vfsfitvnm.vimusic.utils.rememberPreference
import it.vfsfitvnm.vimusic.utils.secondary
import it.vfsfitvnm.vimusic.utils.thumbnailRoundnessKey


@ExperimentalAnimationApi
@Composable
fun About() {
    val (colorPalette, typography) = LocalAppearance.current
    val uriHandler = LocalUriHandler.current

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
            title = stringResource(R.string.about),
            iconId = R.drawable.information,
            enabled = false,
            showIcon = true,
            modifier = Modifier,
            onClick = {}
        )
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
        ) {
            BasicText(
                text = "RiMusic v${BuildConfig.VERSION_NAME} by fast4x",
                style = typography.s.secondary,

                )
        }

        SettingsGroupSpacer()

        SettingsEntryGroupText(title = stringResource(R.string.social))

        SettingsEntry(
            title = "GitHub",
            text = stringResource(R.string.view_the_source_code),
            onClick = {
                uriHandler.openUri("https://github.com/fast4x/RiMusic")
            }
        )

        SettingsGroupSpacer()

        SettingsEntryGroupText(title = stringResource(R.string.troubleshooting))

        SettingsEntry(
            title = stringResource(R.string.report_an_issue),
            text = stringResource(R.string.you_will_be_redirected_to_github),
            onClick = {
                uriHandler.openUri("https://github.com/fast4x/RiMusic/issues/new?assignees=&labels=bug&template=bug_report.yaml")
            }
        )


        SettingsEntry(
            title = stringResource(R.string.request_a_feature_or_suggest_an_idea),
            text = stringResource(R.string.you_will_be_redirected_to_github),
            onClick = {
                uriHandler.openUri("https://github.com/fast4x/RiMusic/issues/new?assignees=&labels=feature_request&template=feature_request.yaml")
            }
        )

        SettingsGroupSpacer()

        SettingsEntryGroupText(title = stringResource(R.string.contributors))
        SettingsDescription(text = stringResource(R.string.in_alphabetical_order))

        SettingsTopDescription( text ="Translator:")
        SettingsTopDescription( text =
            "2010furs \n"+
                    "abfreeman \n"+
                    "Adam Kop \n"+
                    "Ahmad Al Juwaisri \n"+
                    "AntoniNowak \n" +
                    "Conk \n"+
                    "Corotyest \n" +
                    "Crayz310 \n"+
                    "CUMOON \n"+
                    "DanielSevillano \n"+
                    "EMC_Translator \n"+
                    "Fabian Urra \n"+
                    "fast4x \n"+
                    "Fausta Ahmad \n"+
                    "Get100percent \n"+
                    "Ikanakova \n"+
                    "JZITNIK-github \n"+
                    "Kjev666 \n"+
                    "Kptmx \n"+
                    "Lolozweipunktnull \n" +
                    "ManuelCoimbra) \n" +
                    "Marinkas \n"+
                    "Mid_Vur_Shaan \n" +
                    "Muha Aliss \n"+
                    "Ndvok \n"+
                    "NEVARLeVrai \n"+
                    "OrangeZXZ \n"+
                    "RegularWater \n"+
                    "Rikalaj \n" +
                    "Roklc \n"+
                    "Seryoga1984 \n" +
                    "SharkChan0622 \n"+
                    "Sharunkumar \n" +
                    "Shilave malay \n"+
                    "Siggi1984 \n"+
                    "Teaminh \n"+
                    "TeddysulaimanGL \n"+
                    "Th3-C0der \n" +
                    "TheCreeperDuck \n"+
                    "TsyQax \n"+
                    "VINULA2007 \n" +
                    "Vladimir \n" +
                    "Zan1456 \n" +
                    "ZeroZero00 \n"
        )

        SettingsTopDescription( text ="Developer / Designer:")
        SettingsTopDescription( text =
            "25huizengek1 \n"+
                "Craeckie \n"+
                "DanielSevillano \n"+
                "Fast4x \n"+
                "Ikanakova \n"+
                "JZITNIK-github \n" +
                "Locxter \n"+
                "Roklc \n"+
                "sharunkumar \n" +
                "SuhasDissa \n"
        )
    }
}
