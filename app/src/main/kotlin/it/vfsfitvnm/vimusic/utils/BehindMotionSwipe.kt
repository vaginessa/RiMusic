package it.vfsfitvnm.vimusic.utils

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.ui.items.DragAnchors
import it.vfsfitvnm.vimusic.ui.items.DraggableItem
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance


@ExperimentalFoundationApi
@Composable
fun BehindMotionSwipe(
    content: @Composable () -> Unit,
    leftActionsContent: @Composable () -> Unit,
    rightActionsContent: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val (colorPalette) = LocalAppearance.current

    val defaultActionSize = 80.dp

    val endActionSizePx = with(density) { (defaultActionSize * 2).toPx() }
    val startActionSizePx = with(density) { defaultActionSize.toPx() }

    val state = remember {
        AnchoredDraggableState(
            initialValue = DragAnchors.Center,
            anchors = DraggableAnchors {
                DragAnchors.Start at -startActionSizePx
                DragAnchors.Center at 0f
                DragAnchors.End at endActionSizePx
            },
            positionalThreshold = { distance: Float -> distance * 0.5f },
            velocityThreshold = { with(density) { 100.dp.toPx() } },
            animationSpec = tween(),
        )
    }

    DraggableItem(
        state = state,
        content = {
            content()
        },

        startAction = {
            leftActionsContent()
            /*
            LeftAction(
                icon = R.drawable.enqueue,
                backgroundColor = colorPalette.background4,
                onClick = {}
            )
             */
        },
        endAction = {
            rightActionsContent()
            /*
            RightActions(
                iconAction1 = R.drawable.pencil,
                backgroundColorAction1 = colorPalette.background4,
                onClickAction1 = {},
                iconAction2 = R.drawable.trash,
                backgroundColorAction2 = colorPalette.red,
                onClickAction2 = {}
            )
             */
        }
    )
}

@Composable
fun HelloWorldCard() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray), contentAlignment = Alignment.Center
    ) {
        Text(text = "Hello World!!")
    }
}