package it.vfsfitvnm.vimusic.ui.components.themed

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance

@ExperimentalAnimationApi
@Composable
fun Scaffold(
    topIconButtonId: Int,
    onTopIconButtonClick: () -> Unit,
    topIconButton2Id: Int,
    onTopIconButton2Click: () -> Unit,
    showButton2: Boolean,
    tabIndex: Int,
    onTabChanged: (Int) -> Unit,
    tabColumnContent: @Composable ColumnScope.(@Composable (Int, String, Int) -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.(Int) -> Unit
) {
    val (colorPalette) = LocalAppearance.current

    Row(
        modifier = modifier
            .background(colorPalette.background0)
            .fillMaxSize()
    ) {

        NavigationRail(
            topIconButtonId = topIconButtonId,
            onTopIconButtonClick = onTopIconButtonClick,
            topIconButton2Id = topIconButton2Id,
            onTopIconButton2Click = onTopIconButton2Click,
            showButton2 = showButton2,
            tabIndex = tabIndex,
            onTabIndexChanged = onTabChanged,
            content = tabColumnContent
        )

        AnimatedContent(
            targetState = tabIndex,
            transitionSpec = {
                val slideDirection = when (targetState > initialState) {
                    true -> AnimatedContentScope.SlideDirection.Right
                    false -> AnimatedContentScope.SlideDirection.Left
                }

                val animationSpec = spring(
                    dampingRatio = 0.9f,
                    stiffness = Spring.StiffnessLow,
                    visibilityThreshold = IntOffset.VisibilityThreshold
                )

                slideIntoContainer(slideDirection, animationSpec) with
                        slideOutOfContainer(slideDirection, animationSpec)
            },
            content = content
        )
    }
}

@ExperimentalAnimationApi
@Composable
fun Scaffold3(
    topIconButtonId: Int,
    onTopIconButtonClick: () -> Unit,
    topIconButton2Id: Int,
    onTopIconButton2Click: () -> Unit,
    showButton2: Boolean,
    topIconButton3Id: Int,
    onTopIconButton3Click: () -> Unit,
    showButton3: Boolean,

    tabIndex: Int,
    onTabChanged: (Int) -> Unit,
    tabColumnContent: @Composable ColumnScope.(@Composable (Int, String, Int) -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.(Int) -> Unit
) {
    val (colorPalette) = LocalAppearance.current

    Row(
        modifier = modifier
            .background(colorPalette.background0)
            .fillMaxSize()
    ) {

        NavigationRail3(
            topIconButtonId = topIconButtonId,
            onTopIconButtonClick = onTopIconButtonClick,
            topIconButton2Id = topIconButton2Id,
            onTopIconButton2Click = onTopIconButton2Click,
            showButton2 = showButton2,
            topIconButton3Id = topIconButton3Id,
            onTopIconButton3Click = onTopIconButton3Click,
            showButton3 = showButton3,
            tabIndex = tabIndex,
            onTabIndexChanged = onTabChanged,
            content = tabColumnContent
        )

        AnimatedContent(
            targetState = tabIndex,
            transitionSpec = {
                val slideDirection = when (targetState > initialState) {
                    true -> AnimatedContentScope.SlideDirection.Right
                    false -> AnimatedContentScope.SlideDirection.Left
                }

                val animationSpec = spring(
                    dampingRatio = 0.9f,
                    stiffness = Spring.StiffnessLow,
                    visibilityThreshold = IntOffset.VisibilityThreshold
                )

                slideIntoContainer(slideDirection, animationSpec) with
                        slideOutOfContainer(slideDirection, animationSpec)
            },
            content = content
        )
    }
}