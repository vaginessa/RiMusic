package it.vfsfitvnm.vimusic.ui.components


import android.Manifest
import android.content.Context
import android.content.Intent
import android.media.audiofx.Visualizer
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.chibde.visualizer.LineVisualizer
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.ui.components.themed.SecondaryTextButton
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.utils.hasPermission
import it.vfsfitvnm.vimusic.utils.isCompositionLaunched


private val permission  = Manifest.permission.RECORD_AUDIO
private val permission1 =  Manifest.permission.MODIFY_AUDIO_SETTINGS

@UnstableApi
@Composable
fun Visualizer (
    type: String?
) {
    val context: Context = LocalContext.current
    val (colorPalette, typography) = LocalAppearance.current
    val binder = LocalPlayerServiceBinder.current

    var hasPermission by remember(isCompositionLaunched()) {
        mutableStateOf(context.applicationContext.hasPermission(permission))
    }
    var hasPermission1 by remember(isCompositionLaunched()) {
        mutableStateOf(context.applicationContext.hasPermission(permission1))
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { hasPermission = it }
    )

    if (hasPermission && hasPermission1) {

        var visualizer = binder?.player?.audioSessionId?.let { Visualizer(it) }
        if (visualizer != null) {
            visualizer.enabled= true

            var lineVisualizer = LineVisualizer(context)


            lineVisualizer.setColor(colorPalette.text.hashCode())
            lineVisualizer.setStrokeWidth(1)
            binder?.player?.let { lineVisualizer.setPlayer(it.audioSessionId) }

            

            //Log.d("mediaItemVisualizer",visualizer.enabled.toString() + " "+ lineVisualizer.visibility.toString())
        }





    } else {
        if (!hasPermission) {
            LaunchedEffect(Unit) { launcher.launch(permission) }

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BasicText(
                    text = stringResource(R.string.permission_declined_grant_media_permissions),
                    modifier = Modifier.fillMaxWidth(0.5f),
                    style = typography.s
                )
                SecondaryTextButton(
                    text = stringResource(R.string.open_settings),
                    onClick = {
                        context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            setData(Uri.fromParts("package", context.packageName, null))
                        })
                    }
                )
            }
        }
        if (!hasPermission1) {
            LaunchedEffect(Unit) { launcher.launch(permission1) }

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BasicText(
                    text = stringResource(R.string.permission_declined_grant_media_permissions),
                    modifier = Modifier.fillMaxWidth(0.5f),
                    style = typography.s
                )
                SecondaryTextButton(
                    text = stringResource(R.string.open_settings),
                    onClick = {
                        context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            setData(Uri.fromParts("package", context.packageName, null))
                        })
                    }
                )
            }
        }


    }


}