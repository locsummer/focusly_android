package com.focusly.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.focusly.app.ui.blocking.AppLimitsScreen
import com.focusly.app.ui.blocking.AppLimitsViewModel
import com.focusly.app.ui.blocking.AppPickerScreen
import com.focusly.app.ui.dashboard.DashboardScreen
import com.focusly.app.ui.focus.FocusTimerScreen
import com.focusly.app.ui.navigation.Screen
import com.focusly.app.ui.permissions.PermissionGuideScreen
import com.focusly.app.ui.statistics.StatisticsScreen
import com.focusly.app.ui.theme.FocuslyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FocuslyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainAppScaffold()
                }
            }
        }
    }
}

data class BottomNavItem(
    val screen: Screen,
    val icon: ImageVector
)

@Composable
fun MainAppScaffold() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        BottomNavItem(Screen.Dashboard, Icons.Default.Dashboard),
        BottomNavItem(Screen.Blocking, Icons.Default.Block),
        BottomNavItem(Screen.Focus, Icons.Default.HourglassEmpty),
        BottomNavItem(Screen.Statistics, Icons.Default.BarChart)
    )

    val limitsViewModel: AppLimitsViewModel = viewModel()
    var selectedAppForLimit by remember { mutableStateOf<Pair<String, String>?>(null) }
    var limitMinutesInput by remember { mutableIntStateOf(30) }

    val showBottomBar = currentRoute in bottomNavItems.map { it.screen.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(imageVector = item.icon, contentDescription = item.screen.title) },
                            label = { Text(item.screen.title) },
                            selected = currentRoute == item.screen.route,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onNavigateToPermissions = { navController.navigate(Screen.Permissions.route) },
                    onNavigateToBlocking = { navController.navigate(Screen.Blocking.route) }
                )
            }
            composable(Screen.Blocking.route) {
                AppLimitsScreen(
                    viewModel = limitsViewModel,
                    onNavigateToAppPicker = { navController.navigate(Screen.AppPicker.route) }
                )
            }
            composable(Screen.Focus.route) {
                FocusTimerScreen()
            }
            composable(Screen.Statistics.route) {
                StatisticsScreen()
            }
            composable(Screen.Permissions.route) {
                PermissionGuideScreen(
                    onAllGranted = {
                        navController.popBackStack()
                    }
                )
            }
            composable(Screen.AppPicker.route) {
                AppPickerScreen(
                    viewModel = limitsViewModel,
                    onBack = { navController.popBackStack() },
                    onAppSelected = { pkg, name ->
                        selectedAppForLimit = Pair(pkg, name)
                        navController.popBackStack()
                    }
                )
            }
        }

        // Dialog for setting allowance minutes on selected app
        selectedAppForLimit?.let { (pkg, name) ->
            AlertDialog(
                onDismissRequest = { selectedAppForLimit = null },
                title = { Text("Set Limit for $name") },
                text = {
                    OutlinedTextField(
                        value = limitMinutesInput.toString(),
                        onValueChange = { limitMinutesInput = it.toIntOrNull() ?: 30 },
                        label = { Text("Daily Limit (Minutes)") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        limitsViewModel.addLimit(pkg, name, limitMinutesInput)
                        selectedAppForLimit = null
                    }) {
                        Text("Save Limit")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedAppForLimit = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
