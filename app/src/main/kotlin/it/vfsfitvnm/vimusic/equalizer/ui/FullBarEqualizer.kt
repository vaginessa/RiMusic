package it.vfsfitvnm.vimusic.equalizer.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged

import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.vimusic.equalizer.audio.VisualizerData
import it.vfsfitvnm.vimusic.equalizer.ui.ext.getHeightDp
import it.vfsfitvnm.vimusic.equalizer.ui.ext.getWidthDp
import kotlin.math.sin


@Composable
fun FullBarEqualizer(
    modifier: Modifier,
    barModifier: (Int, Modifier) -> Modifier,
    data: VisualizerData,
    barCount: Int
) {
    var size by remember { mutableStateOf(IntSize.Zero) }
    Row(modifier.onSizeChanged { size = it }) {
        val widthDp = size.getWidthDp()
        val heightDp = size.getHeightDp()
        //val padding = 1.dp
        val barWidthDp = widthDp / (barCount +2)

        data.resample(barCount).forEachIndexed { index, d ->
            val height by animateDpAsState(
                targetValue = heightDp * d / 128f
            )
            Box(
                barModifier(
                    index,
                    Modifier
                        .width(barWidthDp)
                        .height(height)
                        //.padding(start = if (index == 0) 0.dp else padding)
                        .align(Alignment.Bottom)
                )
            )
        }
    }
}

/*
@Preview
@Composable
fun FullBarEqualizerPreview() {
    val data = ByteArray(32) { ((sin(it.toDouble())+1) * 128).toInt().toByte() }
    FullBarEqualizer(
        Modifier
            .fillMaxSize()
            .background(Color.Magenta),
        barModifier = { i, m -> m.background(if (i == 0) Color.Blue else Color.Green) },
        VisualizerData(data),
        20
    )
}*/