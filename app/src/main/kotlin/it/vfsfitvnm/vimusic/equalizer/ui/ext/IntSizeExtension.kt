package it.vfsfitvnm.vimusic.equalizer.ui.ext

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize

@Composable
fun IntSize.getWidthDp(): Dp = LocalDensity.current.run { width.toDp() }

@Composable
fun IntSize.getHeightDp(): Dp = LocalDensity.current.run { height.toDp() }
