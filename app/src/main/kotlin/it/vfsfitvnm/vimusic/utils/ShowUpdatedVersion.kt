package it.vfsfitvnm.vimusic.utils

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import org.json.JSONException
import java.io.File

@SuppressLint("SuspiciousIndentation")
@Composable
fun ShowUpdatedVersion(
    updatedVersion: String,
    onDismiss: () -> Unit,
    modifier: Modifier
    ) {
    //if (BuildConfig.VERSION_NAME != updatedVersion)

    //val file = getFilesDir() //shows as unresolved reference
    val file = File(LocalContext.current.filesDir, "RiMusicUpdatedVersion.ver")
    val newVersion = file.readText()



        val (colorPalette, typography) = LocalAppearance.current


                BasicText(
                    text = "New version $newVersion",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = typography.xs.semiBold.secondary,
                )


}