package it.vfsfitvnm.vimusic.equalizer.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.Path
import androidx.compose.ui.graphics.vector.PathNode
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.vimusic.equalizer.audio.VisualizerData


@Composable
fun DoubleSidedPathEqualizer(
    modifier: Modifier,
    data: VisualizerData,
    segmentCount: Int,
    fillBrush: Brush,
) {
    var size by remember { mutableStateOf(IntSize.Zero) }
    Row(modifier.onSizeChanged { size = it }) {
        val viewportWidth = size.width.toFloat()
        val viewportHeight = size.height.toFloat()

        val resampled = data.resample(segmentCount)
        val pathData = computeDoubleSidedPoints(resampled, viewportWidth, viewportHeight, segmentCount)
            .map { p ->
                val height by animateFloatAsState(targetValue = p.y())
                PathNode.LineTo(p.x(), height)
            }

        val vectorPainter = rememberVectorPainter(
            defaultWidth = viewportWidth.dp,
            defaultHeight = viewportHeight.dp,
            viewportWidth = viewportWidth,
            viewportHeight = viewportHeight,
            autoMirror = false
        ) { _, _ ->
            Path(
                fill = fillBrush,
                pathData = pathData
            )
        }
        Image(
            painter = vectorPainter,
            contentDescription = null
        )
    }
}