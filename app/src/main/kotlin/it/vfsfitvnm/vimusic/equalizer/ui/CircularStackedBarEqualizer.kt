package it.vfsfitvnm.vimusic.equalizer.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.Path
import androidx.compose.ui.graphics.vector.PathNode
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.vimusic.equalizer.audio.VisualizerData
import it.vfsfitvnm.vimusic.equalizer.ui.circularProj
import it.vfsfitvnm.vimusic.equalizer.ui.computeStackedBarPoints


@Composable
fun CircularStackedBarEqualizer(
    modifier: Modifier,
    data: VisualizerData,
    barCount: Int,
    maxStackCount: Int = 32
) {
    var size by remember { mutableStateOf(IntSize.Zero) }
    Row(modifier.onSizeChanged { size = it }) {
        val viewportWidth = size.width.toFloat()
        val viewportHeight = size.height.toFloat()
        val horizontalPadding = LocalDensity.current.run { 2.dp.toPx() }
        val verticalPadding = LocalDensity.current.run { 4.dp.toPx() }

        val stackedBar = computeStackedBarPoints(
            resampled = data.resample(barCount),
            viewportWidth = viewportWidth,
            viewportHeight = viewportHeight,
            barCount = barCount,
            maxStackCount = maxStackCount,
            horizontalPadding = horizontalPadding,
            verticalPadding = verticalPadding,
        )
        val points = stackedBar.circularProj(
            viewportWidth = viewportWidth,
            viewportHeight = viewportHeight,
            innerRadiusRatio = 0.1f,
            outerRadiusRatio = 1.0f,
            stretchPow = 1f
        )

        val nodes = points
            .mapIndexed { index, point ->
                if (index % 4 == 0)
                    PathNode.MoveTo(point.x(), point.y())
                else
                    PathNode.LineTo(point.x(), point.y())
            }

        val vectorPainter = rememberVectorPainter(
            defaultWidth = viewportWidth.dp,
            defaultHeight = viewportHeight.dp,
            viewportWidth = viewportWidth,
            viewportHeight = viewportHeight,
            autoMirror = false
        ) { vw, vh ->
            Path(
                fill = Brush.radialGradient(
                    listOf(
                        Color(0xffb3e5fc),
                        Color(0xffb3e5fc),
                        Color(0xffffffff),
                        Color(0xff9575cd),
                    )
                ),
                pathData = nodes
            )
        }
        Image(
            painter = vectorPainter,
            contentDescription = null
        )
    }
}

/*
@Preview
@Composable
fun EqualizerPreview() {
    val data = IntArray(32) { ((sin(it.toDouble())+1) * 128).toInt() }
    BarEqualizer(
        Modifier
            .fillMaxSize()
            .background(Color.Magenta),
        VisualizerData(data)
    )
}*/