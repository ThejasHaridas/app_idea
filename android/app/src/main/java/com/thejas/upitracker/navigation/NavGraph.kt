package com.thejas.upitracker.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.thejas.upitracker.ui.screen.*
import com.thejas.upitracker.ui.viewmodel.MainViewModel

data class Screen(val route: String, val label: String, val icon: String)

val SCREENS = listOf(
    Screen("dashboard",    "Dashboard", "📊"),
    Screen("transactions", "History",   "📋"),
    Screen("add",          "Add",       "➕"),
    Screen("categories",   "Limits",    "🎯")
)

@Composable
fun NavGraph(navController: NavHostController, viewModel: MainViewModel) {
    NavHost(navController, startDestination = "dashboard") {
        composable("dashboard")    { DashboardScreen(viewModel) }
        composable("transactions") { TransactionsScreen(viewModel) }
        composable("add")          { AddTransactionScreen(viewModel) { navController.navigate("transactions") } }
        composable("categories")   { CategoriesScreen(viewModel) }
    }
}
