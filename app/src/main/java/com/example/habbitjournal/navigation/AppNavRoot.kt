package com.example.habbitjournal.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.habbitjournal.feature.calendar.CalendarScreen
import com.example.habbitjournal.feature.calendar.CalendarViewModel
import com.example.habbitjournal.feature.home.HomeScreen
import com.example.habbitjournal.feature.home.HomeViewModel
import com.example.habbitjournal.feature.settings.SettingsScreen
import com.example.habbitjournal.feature.settings.SettingsViewModel
import kotlin.math.abs

private enum class RootTab(val route: String, val title: String) {
    HOME("home", "主页"),
    CALENDAR("calendar", "日历"),
    SETTINGS("settings", "设置"),
}

private fun routeIndex(route: String?): Int {
    return RootTab.entries.indexOfFirst { it.route == route }.takeIf { it >= 0 } ?: 0
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.slideDirection(): AnimatedContentTransitionScope.SlideDirection {
    val initialIndex = routeIndex(initialState.destination.route)
    val targetIndex = routeIndex(targetState.destination.route)
    return if (targetIndex >= initialIndex) {
        AnimatedContentTransitionScope.SlideDirection.Left
    } else {
        AnimatedContentTransitionScope.SlideDirection.Right
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.tabDistance(): Int {
    val initialIndex = routeIndex(initialState.destination.route)
    val targetIndex = routeIndex(targetState.destination.route)
    return abs(targetIndex - initialIndex).coerceAtLeast(1)
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.tabEnterTransition(): EnterTransition {
    val distance = tabDistance()
    val direction = slideDirection()
    val duration = 260 + (distance - 1) * 140
    return slideInHorizontally(
        animationSpec = tween(duration),
        initialOffsetX = { fullWidth ->
            val delta = fullWidth * distance
            if (direction == AnimatedContentTransitionScope.SlideDirection.Left) delta else -delta
        },
    )
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.tabExitTransition(): ExitTransition {
    val distance = tabDistance()
    val direction = slideDirection()
    val duration = 260 + (distance - 1) * 140
    return slideOutHorizontally(
        animationSpec = tween(duration),
        targetOffsetX = { fullWidth ->
            val delta = fullWidth * distance
            if (direction == AnimatedContentTransitionScope.SlideDirection.Left) -delta else delta
        },
    )
}

@Composable
fun AppNavRoot() {
    val navController = rememberNavController()
    val tabs = RootTab.entries
    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            NavigationBar {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        label = { Text(tab.title) },
                        icon = { Text(tab.title.take(1)) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = RootTab.HOME.route,
            modifier = Modifier.padding(padding),
        ) {
            composable(
                route = RootTab.HOME.route,
                enterTransition = { tabEnterTransition() },
                exitTransition = { tabExitTransition() },
                popEnterTransition = { tabEnterTransition() },
                popExitTransition = { tabExitTransition() },
            ) {
                val vm: HomeViewModel = hiltViewModel()
                val uiState = vm.uiState.collectAsStateWithLifecycle()
                HomeScreen(uiState = uiState.value, onAddTodayLog = vm::addTodayLog)
            }
            composable(
                route = RootTab.CALENDAR.route,
                enterTransition = { tabEnterTransition() },
                exitTransition = { tabExitTransition() },
                popEnterTransition = { tabEnterTransition() },
                popExitTransition = { tabExitTransition() },
            ) {
                val vm: CalendarViewModel = hiltViewModel()
                val uiState = vm.uiState.collectAsStateWithLifecycle()
                CalendarScreen(
                    uiState = uiState.value,
                    onPrevMonth = vm::previousMonth,
                    onNextMonth = vm::nextMonth,
                )
            }
            composable(
                route = RootTab.SETTINGS.route,
                enterTransition = { tabEnterTransition() },
                exitTransition = { tabExitTransition() },
                popEnterTransition = { tabEnterTransition() },
                popExitTransition = { tabExitTransition() },
            ) {
                val vm: SettingsViewModel = hiltViewModel()
                val uiState = vm.uiState.collectAsStateWithLifecycle()
                SettingsScreen(
                    uiState = uiState.value,
                    onSaveServerUrl = vm::saveServerUrl,                    onSyncNow = vm::syncNow,
                    onExportCsv = vm::exportCsv,
                )
            }
        }
    }
}


