package it.vfsfitvnm.vimusic.ui.screens.settings

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerAwareWindowInsets
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.internal
import it.vfsfitvnm.vimusic.path
import it.vfsfitvnm.vimusic.query
import it.vfsfitvnm.vimusic.service.MyDownloadService
import it.vfsfitvnm.vimusic.service.PlayerService
import it.vfsfitvnm.vimusic.ui.components.themed.ConfirmationDialog
import it.vfsfitvnm.vimusic.ui.components.themed.DefaultDialog
import it.vfsfitvnm.vimusic.ui.components.themed.HeaderWithIcon
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.shimmer
import it.vfsfitvnm.vimusic.utils.bold
import it.vfsfitvnm.vimusic.utils.intent
import it.vfsfitvnm.vimusic.utils.semiBold
import it.vfsfitvnm.vimusic.utils.toast
import kotlinx.coroutines.flow.distinctUntilChanged
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.system.exitProcess
@UnstableApi
@ExperimentalAnimationApi
@Composable
fun DatabaseSettings() {
    val context = LocalContext.current
    val (colorPalette, typography) = LocalAppearance.current

    val eventsCount by remember {
        Database.eventsCount().distinctUntilChanged()
    }.collectAsState(initial = 0)

    val backupLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/vnd.sqlite3")) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult

            query {
                Database.checkpoint()

                context.applicationContext.contentResolver.openOutputStream(uri)
                    ?.use { outputStream ->
                        FileInputStream(Database.internal.path).use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
            }
        }

    var exitAfterRestore by remember { mutableStateOf(false) }

    val restoreLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult

            query {
                Database.checkpoint()
                Database.internal.close()

                context.applicationContext.contentResolver.openInputStream(uri)
                    ?.use { inputStream ->
                        FileOutputStream(Database.internal.path).use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                context.stopService(context.intent<PlayerService>())
                context.stopService(context.intent<MyDownloadService>())

                exitAfterRestore = true
            }
        }

    if (exitAfterRestore)
        DefaultDialog(
            onDismiss = { exitAfterRestore = false },
            content = {
                BasicText(
                    text = stringResource(R.string.restore_completed),
                    style = typography.s.bold.copy(color = colorPalette.text),
                )
                Spacer(modifier = Modifier.height(20.dp))
                BasicText(
                    text = stringResource(R.string.click_to_close),
                    style = typography.xs.semiBold.copy(color = colorPalette.textSecondary),
                )
                Spacer(modifier = Modifier.height(10.dp))
                Image(
                    painter = painterResource(R.drawable.server),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(colorPalette.shimmer),
                    modifier = Modifier
                        .size(40.dp)
                        .clickable {
                            exitAfterRestore = false
                            exitProcess(0)
                        }
                )
            }

        )

    var isExporting by remember { mutableStateOf(false) }
    var isImporting by remember { mutableStateOf(false) }

    if (isExporting) {
        ConfirmationDialog(
            text = stringResource(R.string.export_the_database),
            onDismiss = { isExporting = false },
            onConfirm = {
                @SuppressLint("SimpleDateFormat")
                val dateFormat = SimpleDateFormat("yyyyMMddHHmmss")
                try {
                    backupLauncher.launch("rimusic_${dateFormat.format(Date())}.db")
                } catch (e: ActivityNotFoundException) {
                    context.toast("Couldn't find an application to create documents")
                }
            }
        )
    }
    if (isImporting) {
        ConfirmationDialog(
            text = stringResource(R.string.import_the_database),
            onDismiss = { isImporting = false },
            onConfirm = {
                try {
                    restoreLauncher.launch(
                        arrayOf(
                            "application/vnd.sqlite3",
                            "application/x-sqlite3",
                            "application/octet-stream"
                        )
                    )
                } catch (e: ActivityNotFoundException) {
                    context.toast("Couldn't find an application to open documents")
                }
            }
        )
    }

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
            title = stringResource(R.string.database),
            iconId = R.drawable.server,
            enabled = false,
            showIcon = true,
            modifier = Modifier,
            onClick = {}
        )

        SettingsEntryGroupText(title = stringResource(R.string.cleanup))

        SettingsEntry(
            title = stringResource(R.string.reset_quick_picks),
            text = if (eventsCount > 0) {
                stringResource(R.string.delete_playback_events, eventsCount)
            } else {
                stringResource(R.string.quick_picks_are_cleared)
            },
            isEnabled = eventsCount > 0,
            onClick = { query(Database::clearEvents) }
        )

        SettingsGroupSpacer()

        SettingsEntryGroupText(title = stringResource(R.string.backup))

        SettingsDescription(text = stringResource(R.string.personal_preference))

        SettingsEntry(
            title = stringResource(R.string.backup_1),
            text = stringResource(R.string.export_the_database),
            onClick = {
                isExporting = true
            }
        )


        SettingsGroupSpacer()

        SettingsEntryGroupText(title = stringResource(R.string.restore))

        ImportantSettingsDescription(text = stringResource(
            R.string.existing_data_will_be_overwritten,
            context.applicationInfo.nonLocalizedLabel
        ))

        SettingsEntry(
            title = stringResource(R.string.restore_1),
            text = stringResource(R.string.import_the_database),
            onClick = {
                isImporting = true
            }
        )
    }
}
