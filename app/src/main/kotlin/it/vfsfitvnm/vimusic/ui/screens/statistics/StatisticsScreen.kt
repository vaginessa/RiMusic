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
            StatisticsType.Today -> 0
            StatisticsType.OneWeek -> 1
            StatisticsType.OneMonth -> 2
            StatisticsType.ThreeMonths -> 3
            StatisticsType.SixMonths -> 4
            StatisticsType.OneYear -> 5
            StatisticsType.All -> 6

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
                    Item(0, "Today", R.drawable.query_stats)
                    Item(1, "1 week", R.drawable.query_stats)
                    Item(2, "1 month", R.drawable.query_stats)
                    Item(3, "3 months", R.drawable.query_stats)
                    Item(4, "6 months", R.drawable.query_stats)
                    Item(5, "1 year", R.drawable.query_stats)
                    Item(6, "All", R.drawable.query_stats)
                }
            ) { currentTabIndex ->
                saveableStateHolder.SaveableStateProvider(key = currentTabIndex) {
                    when (currentTabIndex) {
                        0 -> StatisticsPage(statisticsType = StatisticsType.Today)
                        1 -> StatisticsPage(statisticsType = StatisticsType.OneWeek)
                        2 -> StatisticsPage(statisticsType = StatisticsType.OneMonth)
                        3 -> StatisticsPage(statisticsType = StatisticsType.ThreeMonths)
                        4 -> StatisticsPage(statisticsType = StatisticsType.SixMonths)
                        5 -> StatisticsPage(statisticsType = StatisticsType.OneYear)
                        6 -> StatisticsPage(statisticsType = StatisticsType.All)
                    }
                }
            }
        }
    }
}
