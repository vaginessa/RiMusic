package it.vfsfitvnm.vimusic.ui.components.themed

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.shimmer
import it.vfsfitvnm.vimusic.utils.semiBold

@Composable
fun IconInfo (
    title: String,
    icon: Painter,
    spacer: Dp = 4.dp,
    iconSize: Dp = 20.dp
) {
    val (colorPalette, typography, thumbnailShape) = LocalAppearance.current
    Row (
        modifier = Modifier.fillMaxWidth()
    ) {
        Image(
            painter = icon,
            contentDescription = null,
            colorFilter = ColorFilter.tint(colorPalette.text),
            modifier = Modifier
                .size(iconSize)
        )
        Spacer(modifier = Modifier.padding(horizontal = 2.dp))
        BasicText(
            text = title,
            style = TextStyle(
                color = colorPalette.text,
                fontStyle = typography.l.fontStyle
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

    }
}