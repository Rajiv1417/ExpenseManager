package com.expensemanager.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.expensemanager.ui.screens.accounts.AccountsScreen
import com.expensemanager.ui.screens.addtransaction.AddTransactionScreen
import com.expensemanager.ui.screens.dashboard.DashboardScreen
import com.expensemanager.ui.screens.import_screen.ImportScreen
import com.expensemanager.ui.screens.settings.SettingsScreen

object Routes {
    const val DASHBOARD = "dashboard"
    const val ADD_TRANSACTION = "add_transaction?transactionId={transactionId}"
    const val ACCOUNTS = "accounts"
    const val IMPORT = "import"
    const val SETTINGS = "settings"

    fun addTransaction(transactionId: Long? = null) =
        "add_transaction?transactionId=${transactionId ?: -1}"
}

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.DASHBOARD
    ) {
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onAddTransaction = { navController.navigate(Routes.addTransaction()) },
                onTransactionClick = { id -> navController.navigate(Routes.addTransaction(id)) },
                onAccountsClick = { navController.navigate(Routes.ACCOUNTS) },
                onImportClick = { navController.navigate(Routes.IMPORT) },
                onSettingsClick = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(
            route = Routes.ADD_TRANSACTION,
            arguments = listOf(
                navArgument("transactionId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStack ->
            val transactionId = backStack.arguments?.getLong("transactionId")?.takeIf { it != -1L }
            AddTransactionScreen(
                transactionId = transactionId,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.ACCOUNTS) {
            AccountsScreen(
                onBack = { navController.popBackStack() },
                onAddTransaction = { accountId ->
                    navController.navigate(Routes.addTransaction())
                }
            )
        }
        composable(Routes.IMPORT) {
            ImportScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
