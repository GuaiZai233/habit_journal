package com.example.habbitjournal.navigation

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

private enum class RootTab(val route: String, val title: String) {
    HOME("home", "主页"),
    CALENDAR("calendar", "日历"),
    SETTINGS("settings", "设置"),
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
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = RootTab.HOME.route,
            modifier = Modifier.padding(padding),
        ) {
            composable(RootTab.HOME.route) {
                val vm: HomeViewModel = hiltViewModel()
                val uiState = vm.uiState.collectAsStateWithLifecycle()
                HomeScreen(uiState = uiState.value, onAddTodayLog = vm::addTodayLog)
            }
            composable(RootTab.CALENDAR.route) {
                val vm: CalendarViewModel = hiltViewModel()
                val uiState = vm.uiState.collectAsStateWithLifecycle()
                CalendarScreen(
                    uiState = uiState.value,
                    onPrevMonth = vm::previousMonth,
                    onNextMonth = vm::nextMonth,
                )
            }
            composable(RootTab.SETTINGS.route) {
                val vm: SettingsViewModel = hiltViewModel()
                val uiState = vm.uiState.collectAsStateWithLifecycle()
                SettingsScreen(
                    uiState = uiState.value,
                    onSaveServerUrl = vm::saveServerUrl,
                    onSaveGithubUrl = vm::saveGithubUrl,
                    onSyncNow = vm::syncNow,
                    onExportCsv = vm::exportCsv,
                )
            }
        }
    }
}
