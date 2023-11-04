package it.vfsfitvnm.vimusic.ui.components.themed


import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.shimmer
import it.vfsfitvnm.vimusic.utils.medium
import it.vfsfitvnm.vimusic.utils.semiBold
import kotlin.random.Random

@Composable
fun Header(
    title: String,
    modifier: Modifier = Modifier,
    actionsContent: @Composable RowScope.() -> Unit = {},
) {
    val typography = LocalAppearance.current.typography

    Header(
        modifier = modifier,
        titleContent = {
            BasicText(
                text = title,
                style = typography.xxl.medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        actionsContent = actionsContent
    )
}

@Composable
fun Header(
    modifier: Modifier = Modifier,
    titleContent: @Composable () -> Unit,
    actionsContent: @Composable RowScope.() -> Unit,
) {
    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .height(Dimensions.mediumheaderHeight)
            .fillMaxWidth()
    ) {
        titleContent()

        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd),
            content = actionsContent,
        )
    }
}

@Composable
fun HeaderPlaceholder(
    modifier: Modifier = Modifier,
) {
    val (colorPalette, typography) = LocalAppearance.current

    Box(
        contentAlignment = Alignment.CenterEnd,
        modifier = modifier
            .padding(horizontal = 16.dp)
            .height(Dimensions.headerHeight)
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .background(colorPalette.shimmer)
                .fillMaxWidth(remember { 0.25f + Random.nextFloat() * 0.5f })
        ) {
            BasicText(
                text = "",
                style = typography.xxl.medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@SuppressLint("SuspiciousIndentation")
@Composable
fun HeaderWithIcon (
    title: String,
    modifier: Modifier,
    @DrawableRes iconId: Int,
    showIcon: Boolean = true,
    enabled: Boolean = true,
    onClick: () -> Unit
){
    Row (
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ){

            HalfHeader(
                title = title,
                modifier = Modifier
                    .fillMaxSize(0.9f)
            )

            if (showIcon)
            SecondaryButton(
                iconId = iconId,
                enabled = enabled,
                onClick = onClick,
            )



    }
}

@Composable
fun HalfHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionsContent: @Composable RowScope.() -> Unit = {},
) {
    val typography = LocalAppearance.current.typography
    val colorPalette = LocalAppearance.current.colorPalette


    HalfHeader(
        modifier = modifier,
        titleContent = {
            BasicText(
                text = title,
                style = typography.xxl.medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        actionsContent = actionsContent
    )
}

@Composable
fun HalfHeader(
    modifier: Modifier = Modifier,
    titleContent: @Composable () -> Unit,
    actionsContent: @Composable RowScope.() -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .padding(horizontal = 8.dp)
            .height(Dimensions.halfheaderHeight)
    ) {
        titleContent()

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .heightIn(min = 30.dp),
            content = actionsContent,
        )
    }
}

@Composable
fun HeaderInfo (
    title: String,
    icon: Painter,
    spacer: Int
) {
    val (colorPalette, typography, thumbnailShape) = LocalAppearance.current
    Image(
        painter = icon,
        contentDescription = null,
        colorFilter = ColorFilter.tint(colorPalette.shimmer),
        modifier = Modifier
            .size(14.dp)
    )
    BasicText(
        text = title,
        style = TextStyle(
            color = colorPalette.shimmer,
            fontStyle = typography.xxs.semiBold.fontStyle
        ),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )

    Spacer(
        modifier = Modifier
            .width(spacer.dp)
    )
}