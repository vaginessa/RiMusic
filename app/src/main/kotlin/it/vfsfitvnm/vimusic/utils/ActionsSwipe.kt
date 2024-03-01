package it.vfsfitvnm.vimusic.utils


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.vfsfitvnm.compose.reordering.reorder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.ui.components.themed.IconButton
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance

@Composable
fun LeftAction(
    icon: Int,
    backgroundColor: Color,
    onClick: () -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.CenterStart
    ) {
        Action(
            modifier = Modifier
                .width(80.dp)
                .fillMaxHeight(),
            icon = ImageVector.vectorResource(id = icon),
            backgroundColor = backgroundColor,
            onClick = onClick

        )
    }
}

@Composable
fun RightActions(
    iconAction1: Int,
    backgroundColorAction1: Color,
    onClickAction1: () -> Unit,
    iconAction2: Int,
    backgroundColorAction2: Color,
    onClickAction2: () -> Unit,
    enableAction2: Boolean = true
) {
    val (colorPalette, typography, thumbnailShape) = LocalAppearance.current
    val rippleIndication = rememberRipple(bounded = false)

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.CenterEnd
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Action(
                modifier = Modifier
                    .width(80.dp)
                    .fillMaxHeight(),
                icon = ImageVector.vectorResource(id = iconAction1),
                backgroundColor = backgroundColorAction1,
                onClick = onClickAction1

            )
            Action(
                modifier = Modifier
                    .width(80.dp)
                    .fillMaxHeight(),
                icon = ImageVector.vectorResource(id = iconAction2),
                backgroundColor = backgroundColorAction2,
                onClick = onClickAction2,
                enabled = enableAction2

            )
        }
    }
 }

@Composable
fun Action(
    modifier: Modifier,
    icon: ImageVector,
    backgroundColor: Color,
    text: String = "",
    showText: Boolean = false,
    onClick: () -> Unit,
    enabled: Boolean = true
){
    val (colorPalette, typography, thumbnailShape) = LocalAppearance.current
    val rippleIndication = rememberRipple(bounded = false)
    
    Box(
        modifier = modifier
            .background(backgroundColor),
        contentAlignment = Alignment.Center

    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                modifier = Modifier
                    .padding(top = 20.dp, bottom = 4.dp)
                    .padding(horizontal = 25.dp)
                    .size(22.dp)
                    .clickable { if (enabled) onClick() },
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) colorPalette.accent else colorPalette.textDisabled
            )


            Text(
                text = "",
                color = backgroundColor,
                fontSize = 12.sp,
            )

        }
    }

}

@Composable
fun SaveAction(modifier: Modifier) {
    Box(
        modifier = modifier
            .background(Color.Green),
        contentAlignment = Alignment.Center

    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 4.dp)
                    .padding(horizontal = 20.dp)
                    .size(22.dp),
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = Color.White
            )

            Text(
                text = "Save",
                color = Color.White,
                fontSize = 12.sp,
            )
        }
    }

}


@Composable
fun EditAction(modifier: Modifier) {
    Box(
        modifier = modifier.background(Color.Blue),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 4.dp)
                    .padding(horizontal = 20.dp)
                    .size(22.dp),
                imageVector = Icons.Filled.Edit,
                contentDescription = null,
                tint = Color.White
            )

            Text(
                text = "Edit",
                color = Color.White,
                fontSize = 12.sp,
            )
        }
    }

}

@Composable
fun DeleteAction(modifier: Modifier) {
    Box(
        modifier = modifier.background(Color.Red),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 4.dp)
                    .padding(horizontal = 20.dp)
                    .size(22.dp),
                imageVector = Icons.Filled.Delete,
                contentDescription = null,
                tint = Color.White
            )

            Text(
                text = "Delete",
                color = Color.White,
                fontSize = 12.sp,
            )
        }
    }

}