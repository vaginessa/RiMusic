package it.vfsfitvnm.vimusic.equalizer

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
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
import it.vfsfitvnm.vimusic.utils.hasPermission
import it.vfsfitvnm.vimusic.utils.isCompositionLaunched
import it.vfsfitvnm.vimusic.utils.semiBold

@UnstableApi
@Composable
fun Equalizer(
    showInPage: Boolean? = true,
    showType: Int? = 0
) {

    val permission  = Manifest.permission.RECORD_AUDIO
    val permission1 =  Manifest.permission.MODIFY_AUDIO_SETTINGS
    val context: Context = LocalContext.current
    val (colorPalette, typography) = LocalAppearance.current

    var hasPermission by remember(isCompositionLaunched()) {
        mutableStateOf(context.applicationContext.hasPermission(permission))
    }
    var hasPermission1 by remember(isCompositionLaunched()) {
        mutableStateOf(context.applicationContext.hasPermission(permission1))
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { hasPermission = it }
    )

    if (hasPermission && hasPermission1) {

        val visualizerData = remember { mutableStateOf(VisualizerData()) }
        val (isPlaying, setPlaying) = remember { mutableStateOf(false) }

        if (showInPage == true)
            Content(
                //isPlaying,
                //setPlaying,
                visualizerData
             )
        else
                ContentType(
                showType,
                visualizerData
            )


    } else {
        if (!hasPermission) {
            LaunchedEffect(Unit) { launcher.launch(permission) }

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BasicText(
                    text = stringResource(R.string.permission_declined_please_grant_media_permissions_in_the_settings_of_your_device),
                    modifier = Modifier.fillMaxWidth(0.5f),
                    style = typography.s
                )
                SecondaryTextButton(
                    text = stringResource(R.string.open_settings),
                    onClick = {
                        context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            setData(Uri.fromParts("package", context.packageName, null))
                        })
                    }
                )
            }
        }
        if (!hasPermission1) {
            LaunchedEffect(Unit) { launcher.launch(permission1) }

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BasicText(
                    text = stringResource(R.string.permission_declined_please_grant_media_permissions_in_the_settings_of_your_device),
                    modifier = Modifier.fillMaxWidth(0.5f),
                    style = typography.s
                )
                SecondaryTextButton(
                    text = stringResource(R.string.open_settings),
                    onClick = {
                        context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            setData(Uri.fromParts("package", context.packageName, null))
                        })
                    }
                )
            }
        }


    }

}

@UnstableApi
@Composable
fun Content(
    //isPlaying: Boolean,
    //setPlaying: (Boolean) -> Unit,
    visualizerData: MutableState<VisualizerData>
) {

    val (colorPalette, typography, thumbnailShape) = LocalAppearance.current
    val binder = LocalPlayerServiceBinder.current

    //VisualizerComputer.setupPermissions()
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
        /*
        item {
            BasicText(
                text = if (isPlaying) "stop" else "play",
                style = typography.xxs.semiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            IconButton(
                onClick = {setPlaying(!isPlaying)},
                icon = R.drawable.play,
                color = colorPalette.accent,
                modifier = Modifier
                    .padding(2.dp),
            )
        }

         */

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
    showType: Int?,
    visualizerData: MutableState<VisualizerData>
) {

    //val (colorPalette, typography, thumbnailShape) = LocalAppearance.current
    val binder = LocalPlayerServiceBinder.current

    //VisualizerComputer.setupPermissions()
    val audioComputer = VisualizerComputer()

    binder?.player?.audioSessionId?.let {
        audioComputer.start(audioSessionId = it, onData = { data ->
            visualizerData.value = data
        })
    }

        val someColors =
            listOf(Color.Blue, Color.Green, Color.Yellow, Color.Magenta, Color.Red, Color.Cyan)


        if (showType == 0)
                FancyTubularStackedBarEqualizer(
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .padding(all = 2.dp),
                    data = visualizerData.value,
                    barCount = 48,
                    maxStackCount = 16,
                )


    if (showType == 1)
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


    if (showType == 2)
                StackedBarEqualizer(
                    Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(vertical = 4.dp)
                        .background(Color(0x50000000)),
                    data = visualizerData.value,
                    barCount = 64
                )


    if (showType == 3)
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


    if (showType == 4)
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


    if (showType == 5)
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


    if (showType == 6)
    DoubleSidedCircularPathEqualizer(
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .padding(vertical = 4.dp),
                        //.background(Color(0xE0000000)),
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
