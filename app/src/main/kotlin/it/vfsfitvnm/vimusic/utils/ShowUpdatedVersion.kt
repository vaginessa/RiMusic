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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance

@SuppressLint("SuspiciousIndentation")
@ExperimentalComposeUiApi
@Composable
fun ShowUpdatedVersion(
    updatedVersion: String,
    onDismiss: () -> Unit,
    modifier: Modifier
    ) {
    //if (BuildConfig.VERSION_NAME != updatedVersion)

    val (colorPalette, typography) = LocalAppearance.current

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = modifier
                .padding(all = 48.dp)
                .background(
                    color = colorPalette.background4,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 24.dp, vertical = 16.dp),

        ){
            BasicText(
                text = "New version",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = typography.xs.semiBold.secondary,
            )
        }

}