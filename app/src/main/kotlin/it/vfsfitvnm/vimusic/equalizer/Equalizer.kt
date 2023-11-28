package it.vfsfitvnm.vimusic.equalizer

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.PlayerVisualizerType
import it.vfsfitvnm.vimusic.equalizer.audio.VisualizerComputer
import it.vfsfitvnm.vimusic.equalizer.audio.VisualizerData
import it.vfsfitvnm.vimusic.equalizer.ui.CircularStackedBarEqualizer
import it.vfsfitvnm.vimusic.equalizer.ui.DoubleSidedCircularPathEqualizer
import it.vfsfitvnm.vimusic.equalizer.ui.DoubleSidedPathEqualizer
import it.vfsfitvnm.vimusic.equalizer.ui.FancyTubularStackedBarEqualizer
import it.vfsfitvnm.vimusic.equalizer.ui.FullBarEqualizer
import it.vfsfitvnm.vimusic.equalizer.ui.OneSidedPathEqualizer
import it.vfsfitvnm.vimusic.equalizer.ui.StackedBarEqualizer
import it.vfsfitvnm.vimusic.equalizer.ui.ext.repeat
import it.vfsfitvnm.vimusic.ui.components.themed.IconButton
import it.vfsfitvnm.vimusic.ui.components.themed.SecondaryTextButton
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.onOverlay
import it.vfsfitvnm.vimusic.ui.styling.overlay
import it.vfsfitvnm.vimusic.utils.hasPermission
import it.vfsfitvnm.vimusic.utils.isCompositionLaunched
import it.vfsfitvnm.vimusic.utils.playerVisualizerTypeKey
import it.vfsfitvnm.vimusic.utils.semiBold

@UnstableApi
@Composable
fun Equalizer(
    showInPage: Boolean? = true,
    playerVisualizerType: PlayerVisualizerType = PlayerVisualizerType.Disabled
) {
        val visualizerData = remember { mutableStateOf(VisualizerData()) }

        if (showInPage == true)
            Content(
                //isPlaying,
                //setPlaying,
                visualizerData
             )
        else
                ContentType(
                playerVisualizerType,
                visualizerData
            )

}

@UnstableApi
@Composable
fun Content(
    visualizerData: MutableState<VisualizerData>
) {

    val binder = LocalPlayerServiceBinder.current

    VisualizerComputer.setupPermissions( LocalContext.current as Activity )
    val audioComputer = VisualizerComputer()

    binder?.player?.audioSessionId?.let {
        audioComputer.start(audioSessionId = it, onData = { data ->
        visualizerData.value = data
    })
    }

    LazyColumn (
        modifier = Modifier
            .fillMaxSize()
    ) {

        val someColors =
            listOf(Color.Blue, Color.Green, Color.Yellow, Color.Magenta, Color.Red, Color.Cyan)
        val displayAllItems = false
        val selectItemIndex = 1

        if (displayAllItems || (selectItemIndex == 0))
            item {
                FancyTubularStackedBarEqualizer(
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .padding(all = 2.dp),
                    data = visualizerData.value,
                    barCount = 48,
                    maxStackCount = 16,
                )
            }

        if (displayAllItems || (selectItemIndex == 1))
            item {
                CircularStackedBarEqualizer(
                    Modifier
                        .fillMaxWidth()
                        //.height(300.dp)
                        .aspectRatio(1f),
                       // .background(Color(0xff111111)),
                    data = visualizerData.value,
                    barCount = 48,
                    maxStackCount = 16
                )
            }

        if (displayAllItems || (selectItemIndex == 2))
            item {
                StackedBarEqualizer(
                    Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(vertical = 4.dp)
                        .background(Color(0x50000000)),
                    data = visualizerData.value,
                    barCount = 64
                )
            }

        if (displayAllItems || (selectItemIndex == 3))
            item {
                FullBarEqualizer(
                    Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .padding(vertical = 4.dp)
                        .background(Color(0x50000000)),
                    barModifier = { i, m -> m.background(someColors[i % someColors.size]) },
                    data = visualizerData.value,
                    barCount = 64
                )
            }

        if (displayAllItems || (selectItemIndex == 4))
            item {
                OneSidedPathEqualizer(
                    Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .padding(vertical = 4.dp)
                        .background(Color(0x60000000)),
                    data = visualizerData.value,
                    segmentCount = 32,
                    fillBrush = Brush.linearGradient(
                        start = Offset.Zero,
                        end = Offset.Infinite,
                        colors = listOf(
                            Color.Red,
                            Color.Yellow,
                            Color.Green,
                            Color.Cyan,
                            Color.Blue,
                            Color.Magenta,
                        ).repeat(3)
                    )
                )
            }

        if (displayAllItems || (selectItemIndex == 5))
            item {
                DoubleSidedPathEqualizer(
                    Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .padding(vertical = 4.dp)
                        .background(Color(0x70000000)),
                    data = visualizerData.value,
                    segmentCount = 128,
                    fillBrush = Brush.linearGradient(
                        start = Offset.Zero,
                        end = Offset(0f, Float.POSITIVE_INFINITY),
                        colors = listOf(Color.White, Color.Red, Color.White)
                    )
                )
            }

        if (displayAllItems || (selectItemIndex == 6))
            item {
                DoubleSidedCircularPathEqualizer(
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .padding(vertical = 4.dp)
                        .background(Color(0xE0000000)),
                    data = visualizerData.value,
                    segmentCount = 128,
                    fillBrush = Brush.radialGradient(
                        listOf(
                            Color.Red,
                            Color.Red,
                            Color.Yellow,
                            Color.Green
                        )
                    )
                )
            }
    }
}

@SuppressLint("SuspiciousIndentation")
@UnstableApi
@Composable
fun ContentType(
    visualizerType: PlayerVisualizerType,
    visualizerData: MutableState<VisualizerData>
) {

    val (colorPalette, typography, thumbnailShape) = LocalAppearance.current
    val binder = LocalPlayerServiceBinder.current

    VisualizerComputer.setupPermissions( LocalContext.current as Activity )
    val audioComputer = VisualizerComputer()

    binder?.player?.audioSessionId?.let {

        audioComputer.start(audioSessionId = it, onData = { data ->
            visualizerData.value = data
        })
    }

        val someColors =
            listOf(Color.Blue, Color.Green, Color.Yellow, Color.Magenta, Color.Red, Color.Cyan)


        if (visualizerType == PlayerVisualizerType.Fancy)
                FancyTubularStackedBarEqualizer(
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .padding(all = 2.dp)
                        .background(colorPalette.overlay),
                    data = visualizerData.value,
                    barCount = 48,
                    maxStackCount = 16,
                )

    if (visualizerType == PlayerVisualizerType.Circular)
                CircularStackedBarEqualizer(
                    Modifier
                        .fillMaxWidth()
                        //.height(300.dp)
                        .aspectRatio(1f)
                        .background(colorPalette.overlay),
                    data = visualizerData.value,
                    barCount = 48,
                    maxStackCount = 16
                )


    if (visualizerType == PlayerVisualizerType.Stacked)
                StackedBarEqualizer(
                    Modifier
                        .fillMaxWidth()
                        //.height(300.dp)
                        .aspectRatio(1f)
                        .padding(vertical = 4.dp)
                        .background(colorPalette.overlay),
                    data = visualizerData.value,
                    barCount = 64
                )


    if (visualizerType == PlayerVisualizerType.Full)
                FullBarEqualizer(
                    Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .aspectRatio(1f)
                        .padding(vertical = 4.dp)
                        .background(colorPalette.overlay),
                    barModifier = { i, m -> m.background(someColors[i % someColors.size]) },
                    data = visualizerData.value,
                    barCount = 64
                )


    if (visualizerType == PlayerVisualizerType.Oneside)
                OneSidedPathEqualizer(
                    Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .aspectRatio(1f)
                        .padding(vertical = 4.dp)
                        .background(colorPalette.overlay),
                    data = visualizerData.value,
                    segmentCount = 32,
                    fillBrush = Brush.linearGradient(
                        start = Offset.Zero,
                        end = Offset.Infinite,
                        colors = listOf(
                            Color.Red,
                            Color.Yellow,
                            Color.Green,
                            Color.Cyan,
                            Color.Blue,
                            Color.Magenta,
                        ).repeat(3)
                    )
                )


    if (visualizerType == PlayerVisualizerType.Doubleside)
                DoubleSidedPathEqualizer(
                    Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .aspectRatio(1f)
                        .padding(vertical = 4.dp)
                        .background(colorPalette.overlay),
                    data = visualizerData.value,
                    segmentCount = 128,
                    fillBrush = Brush.linearGradient(
                        start = Offset.Zero,
                        end = Offset(0f, Float.POSITIVE_INFINITY),
                        colors = listOf(Color.White, Color.Red, Color.White)
                    )
                )


    if (visualizerType == PlayerVisualizerType.DoublesideCircular)
    DoubleSidedCircularPathEqualizer(
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .padding(vertical = 4.dp)
                        .background(colorPalette.overlay),
                    data = visualizerData.value,
                    segmentCount = 128,
                    fillBrush = Brush.radialGradient(
                        listOf(
                            Color.Red,
                            Color.Red,
                            Color.Yellow,
                            Color.Green
                        )
                    )
                )

    }
