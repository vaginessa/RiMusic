package it.vfsfitvnm.vimusic.equalizer.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.Path
import androidx.compose.ui.graphics.vector.PathNode
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.vimusic.equalizer.audio.VisualizerData
import it.vfsfitvnm.vimusic.equalizer.ui.Point
import it.vfsfitvnm.vimusic.equalizer.ui.circularProj
import it.vfsfitvnm.vimusic.equalizer.ui.computeDoubleSidedPoints


@Composable
fun DoubleSidedCircularPathEqualizer(
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
        if (resampled.isNotEmpty()) {
            val circularProj = computeDoubleSidedPoints(
                resampled,
                viewportWidth,
                viewportHeight,
                segmentCount,
                continuous = true // false if cubic, true if lineto
            ).map {
                val height by animateFloatAsState(targetValue = it.y())
                Point(it.x() to height)
            }
                .circularProj(
                    viewportWidth = viewportWidth,
                    viewportHeight = viewportHeight,
                    innerRadiusRatio = 0.5f,
                    outerRadiusRatio = 0.8f
                )

            /*val pathData = circularProj
            .map<Point, PathNode> { p ->
                PathNode.LineTo(p.x(), p.y())
            }*/
            val pathData = mutableListOf<PathNode>()
            val count = circularProj.size
            val halfCount = count / 2
            val offset = halfCount - 1
            pathData += PathNode.MoveTo(circularProj[offset].x(), circularProj[offset].y())
            for (i in 0..halfCount - 1) {
                val prevprev = circularProj[(i - 2 + halfCount) % halfCount]
                val prev = circularProj[(i - 1 + halfCount) % halfCount]
                val current = circularProj[i]
                val next = circularProj[(i + 1) % halfCount]
                val tangentPrev = current - prevprev
                val tangentCurrent = prev - next
                val control1 = prev + tangentPrev * 0.2f
                val control2 = current + tangentCurrent * 0.2f
                pathData += PathNode.CurveTo(
                    control1.x(), control1.y(),
                    control2.x(), control2.y(),
                    current.x(), current.y()
                )
            }

            pathData += PathNode.LineTo(circularProj[count - 1].x(), circularProj[count - 1].y())
            for (i in 0..halfCount - 1) {
                val prevprev = circularProj[((i - 2 + halfCount) % halfCount) + halfCount]
                val prev = circularProj[((i - 1 + halfCount) % halfCount) + halfCount]
                val current = circularProj[i + halfCount]
                val next = circularProj[((i + 1) % halfCount) + halfCount]
                val tangentPrev = current - prevprev
                val tangentCurrent = prev - next
                val control1 = prev + tangentPrev * 0.2f
                val control2 = current + tangentCurrent * 0.2f
                pathData += PathNode.CurveTo(
                    control1.x(), control1.y(),
                    control2.x(), control2.y(),
                    current.x(), current.y()
                )
            }

            val firstPoint = circularProj.first()
            val firstPath = PathNode.MoveTo(firstPoint.x(), firstPoint.y())
            val finalPathData = pathData.toMutableList()
            finalPathData.add(0, firstPath)

            val vectorPainter = rememberVectorPainter(
                defaultWidth = viewportWidth.dp,
                defaultHeight = viewportHeight.dp,
                viewportWidth = viewportWidth,
                viewportHeight = viewportHeight,
                autoMirror = false
            ) { _, _ ->
                Path(
                    fill = fillBrush,
                    //strokeLineWidth = 10f,
                    pathData = finalPathData
                )
            }
            Image(
                painter = vectorPainter,
                contentDescription = null
            )
        }
    }
}