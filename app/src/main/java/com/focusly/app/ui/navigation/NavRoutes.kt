package com.focusly.app.ui.navigation

sealed class Screen(val route: String, val title: String) {
    object Dashboard : Screen("dashboard", "Dashboard")
    object Blocking : Screen("blocking", "Blocking")
    object Focus : Screen("focus", "Focus Timer")
    object Statistics : Screen("statistics", "Reports")
    object Permissions : Screen("permissions", "Permissions")
    object AppPicker : Screen("app_picker", "Select Apps")
}
