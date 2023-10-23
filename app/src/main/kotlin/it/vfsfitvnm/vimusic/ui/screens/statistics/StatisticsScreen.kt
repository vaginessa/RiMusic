package it.vfsfitvnm.vimusic.ui.screens.statistics

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import it.vfsfitvnm.compose.persist.PersistMapCleanup
import it.vfsfitvnm.compose.routing.RouteHandler
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.StatisticsType
import it.vfsfitvnm.vimusic.ui.components.themed.Scaffold
import it.vfsfitvnm.vimusic.ui.screens.globalRoutes

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun StatisticsScreen(
    statisticsType: StatisticsType
) {
    val saveableStateHolder = rememberSaveableStateHolder()

    val (tabIndex, onTabIndexChanged) = rememberSaveable {
        mutableStateOf(when (statisticsType) {
            StatisticsType.OneWeek -> 0
            StatisticsType.OneMonth -> 1
            StatisticsType.ThreeMonths -> 2
            StatisticsType.OneYear -> 3
            StatisticsType.All -> 4

        })
    }

    PersistMapCleanup(tagPrefix = "${statisticsType.name}/")

    RouteHandler(listenToGlobalEmitter = true) {
        globalRoutes()

        host {
            Scaffold(
                topIconButtonId = R.drawable.chevron_back,
                onTopIconButtonClick = pop,
                tabIndex = tabIndex,
                onTabChanged = onTabIndexChanged,
                tabColumnContent = { Item ->
                    Item(0, "One week", R.drawable.query_stats)
                    Item(1, "One month", R.drawable.query_stats)
                    Item(2, "Three months", R.drawable.query_stats)
                    Item(3, "One year", R.drawable.query_stats)
                    Item(4, "All", R.drawable.query_stats)
                }
            ) { currentTabIndex ->
                saveableStateHolder.SaveableStateProvider(key = currentTabIndex) {
                    when (currentTabIndex) {
                        0 -> StatisticsItems(statisticsType = StatisticsType.OneWeek)
                        1 -> StatisticsItems(statisticsType = StatisticsType.OneMonth)
                        2 -> StatisticsItems(statisticsType = StatisticsType.ThreeMonths)
                        3 -> StatisticsItems(statisticsType = StatisticsType.OneYear)
                        4 -> StatisticsItems(statisticsType = StatisticsType.All)
                    }
                }
            }
        }
    }
}
