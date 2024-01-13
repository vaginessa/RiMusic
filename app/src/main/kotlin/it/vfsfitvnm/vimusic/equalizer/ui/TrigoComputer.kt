package it.vfsfitvnm.vimusic.equalizer.ui

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.PathNode
import it.vfsfitvnm.vimusic.equalizer.audio.VisualizerComputer.Companion.SAMPLING_INTERVAL
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

@JvmInline
value class Point(private val p: Pair<Float, Float>) {
    fun x() = p.first
    fun y() = p.second

    operator fun plus(other: Point) = Point(x() + other.x() to y() + other.y())
    operator fun minus(other: Point) = Point(x() - other.x() to y() - other.y())
    operator fun times(factor: Float) = Point(x() * factor to y() * factor)
    operator fun div(factor: Float) = times(1f / factor)

    override fun toString() = "[${x()}:${y()}]"
}

fun computeDoubleSidedPoints(
    resampled: IntArray,
    viewportWidth: Float,
    viewportHeight: Float,
    segmentCount: Int,
    continuous: Boolean = false,
): List<Point> {
    if (resampled.isEmpty()) return emptyList()
    val barWidth = viewportWidth / (segmentCount - if (continuous) 0 else 2)
    val top = mutableListOf<Point>()
    val bottom = mutableListOf<Point>()
    resampled.forEachIndexed { index, d ->
        val isTop = index % 2 == 0
        if (isTop) {
            val targetValue = viewportHeight * (0.5f - (d / 256f))
            top.add(Point(barWidth * (index + 0) to targetValue))
        } else {
            val targetValue = viewportHeight * (0.5f + (d / 256f))
            bottom.add(Point(barWidth * (index - 1) to targetValue))
        }
    }
    return top + bottom.reversed()//if (!continuous) {
    //top + bottom.reversed()
    /*} else {
        // Added 2 points to make it nicer for circular projection
        top + top.first() + bottom.first() + bottom.reversed()
    }*/
}

@Composable
fun computeStackedBarPoints(
    resampled: IntArray,
    viewportWidth: Float,
    viewportHeight: Float,
    barCount: Int,
    maxStackCount: Int,
    horizontalPadding: Float,
    verticalPadding: Float,
): List<Point> {
    val barWidth = (viewportWidth / barCount) - horizontalPadding
    val stackHeightWithPadding = viewportHeight / maxStackCount
    val stackHeight = stackHeightWithPadding - verticalPadding

    val nodes = mutableListOf<Point>()
    resampled.forEachIndexed { index, d ->
        //val barHeight by animateFloatAsState(targetValue = viewportHeight * (1 - (d / 128f)))
        //val stackCount  = (maxStackCount * (d / 128f)).roundToInt()
        val stackCount = animateIntAsState(
            (maxStackCount * (d / 128f)).roundToInt(),
            animationSpec = tween(durationMillis = SAMPLING_INTERVAL)
        )
        for (stackIndex in 0..stackCount.value) {
            nodes += Point(
                barWidth * index + horizontalPadding * index to
                        viewportHeight - stackIndex * stackHeight - verticalPadding * stackIndex
            )
            nodes += Point(
                barWidth * (index + 1) + horizontalPadding * index to
                        viewportHeight - stackIndex * stackHeight - verticalPadding * stackIndex
            )
            nodes += Point(
                barWidth * (index + 1) + horizontalPadding * index to
                        viewportHeight - (stackIndex + 1) * stackHeight - verticalPadding * stackIndex
            )
            nodes += Point(
                barWidth * index + horizontalPadding * index to
                        viewportHeight - (stackIndex + 1) * stackHeight - verticalPadding * stackIndex
            )
        }
    }
    return nodes
}

fun List<Point>.stackToNodes() = this.mapIndexed { index, point ->
    if (index % 4 == 0)
        PathNode.MoveTo(point.x(), point.y())
    else
        PathNode.LineTo(point.x(), point.y())
}

fun List<Point>.circularProj(
    viewportWidth: Float,
    viewportHeight: Float,
    // 0f = no inner radius, 1f = everything is applied on the external circle
    innerRadiusRatio: Float = 0.4f,
    outerRadiusRatio: Float = 1.414f,
    angleOffset: Float = 0f,
    stretchRadiusRatio: Float = 1f,// in [0;1], unrelated with other radius ratio, applied to move the effect neutral point
    stretchPow: Float = 1.6f // tunnel effect
) =
    circularProjection(
        points = this,
        viewportWidth = viewportWidth,
        viewportHeight = viewportHeight,
        innerRadiusRatio = innerRadiusRatio,
        outerRadiusRatio = outerRadiusRatio,
        angleOffset = angleOffset,
        stretchRadiusRatio = stretchRadiusRatio,
        stretchPow = stretchPow
    )

/**
 * Take points in a rectangle and project them on a disk,
 * use x as the angle, and y as the distance from center.
 */
fun circularProjection(
    points: List<Point>,
    viewportWidth: Float,
    viewportHeight: Float,
    // 0f = no inner radius, 1f = everything is applied on the external circle
    innerRadiusRatio: Float,
    outerRadiusRatio: Float,
    angleOffset: Float,
    stretchRadiusRatio: Float,// in [0;1], unrelated with other radius ratio, applied to move the effect neutral point
    stretchPow: Float // tunnel effect
): List<Point> {
    val circleRadius = min(viewportWidth, viewportHeight) / 2
    val center = Point(viewportWidth / 2 to viewportHeight / 2)
    val innerRadius = innerRadiusRatio * circleRadius
    val outerRadius = outerRadiusRatio * circleRadius
    return points.mapIndexed { i, p ->
        val angle = angleOffset + Math.PI.toFloat() * 2 * (p.x() / viewportWidth)
        val radiusRatio = 1 - (p.y() / viewportHeight)
        val stretchedRadiusRatio = (radiusRatio / stretchRadiusRatio).pow(stretchPow)
        val newRadius = lerp(innerRadius, outerRadius, stretchedRadiusRatio)
        val x = newRadius * cos(angle) + center.x()
        val y = newRadius * sin(angle) + center.y()
        Point(x to y)
    }
}

fun lerp(a: Float, b: Float, factor: Float): Float {
    return a + ((b - a) * factor)
}

fun List<Point>.circularStretch(viewportWidth: Float, viewportHeight: Float) =
    circularStretch(this, viewportWidth, viewportHeight)


fun circularStretch(
    points: List<Point>,
    viewportWidth: Float,
    viewportHeight: Float,
    baseRadiusRatio: Float = 0.5f,// points on this radius will not move
    stretchFactorOutside: Float = 1.1f,
): List<Point> {
    val centerX = viewportWidth / 2
    val centerY = viewportHeight / 2
    val baseDist = (min(viewportWidth, viewportHeight) / 2) * baseRadiusRatio
    return points.map {
        val diffX = it.x() - centerX
        val diffY = it.y() - centerY
        val radius = sqrt(diffX * diffX + diffY * diffY)
        if (radius - baseDist > 0) {
            it * 1.1f
            //radius * 1.1
        } else {
            it * 0.9f
            //radius * 0.9
        }
        //val stretchedRadius = radius*AERAR
    }
}