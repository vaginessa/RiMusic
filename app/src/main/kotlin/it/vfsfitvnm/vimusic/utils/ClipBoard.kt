package it.vfsfitvnm.vimusic.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import it.vfsfitvnm.vimusic.R

@Composable
fun TextCopyToClipboard(textCopied:String) {
    val context = LocalContext.current
    val clipboardManager = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
    clipboardManager.setPrimaryClip(ClipData.newPlainText   ("", textCopied))
    // Only show a toast for Android 12 and lower.
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2)
        context.toast(stringResource(R.string.value_copied))
}