package com.expensemanager.ui

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.expensemanager.ui.screens.accountdetail.AccountDetailScreen
import com.expensemanager.ui.screens.accounts.AccountsScreen
import com.expensemanager.ui.screens.addtransaction.AddTransactionScreen
import com.expensemanager.ui.screens.addtransaction.AddTransactionViewModel
import com.expensemanager.ui.screens.dashboard.DashboardScreen
import com.expensemanager.ui.screens.import_screen.ImportScreen
import com.expensemanager.ui.screens.records.RecordsScreen
import com.expensemanager.ui.screens.settings.SettingsScreen

object Routes {
    const val DASHBOARD       = "dashboard"
    const val ADD_TRANSACTION = "add_transaction?transactionId={transactionId}"
    const val ACCOUNTS        = "accounts"
    const val ACCOUNT_DETAILS = "account_details/{accountId}"
    const val ACCOUNT_RECORDS = "account_records/{accountId}"
    const val IMPORT          = "import"
    const val SETTINGS        = "settings"

    fun addTransaction(transactionId: Long? = null) =
        "add_transaction?transactionId=${transactionId ?: -1}"

    fun accountDetails(accountId: Long) = "account_details/$accountId"
    fun accountRecords(accountId: Long) = "account_records/$accountId"
}

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Routes.DASHBOARD) {

        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onAddTransaction      = { navController.navigate(Routes.addTransaction()) },
                onTransactionClick    = { id -> navController.navigate(Routes.addTransaction(id)) },
                onAccountsClick       = { navController.navigate(Routes.ACCOUNTS) },
                onAccountDetailsClick = { id -> navController.navigate(Routes.accountDetails(id)) },
                onAccountRecordsClick = { id -> navController.navigate(Routes.accountRecords(id)) },
                onImportClick         = { navController.navigate(Routes.IMPORT) },
                onSettingsClick       = { navController.navigate(Routes.SETTINGS) }
            )
        }

        composable(
            route     = Routes.ADD_TRANSACTION,
            arguments = listOf(navArgument("transactionId") {
                type         = NavType.LongType
                defaultValue = -1L
            })
        ) { backStack ->
            val transactionId = backStack.arguments?.getLong("transactionId")?.takeIf { it != -1L }
            val viewModel: AddTransactionViewModel = hiltViewModel()
            AddTransactionScreen(
                transactionId = transactionId,
                onBack        = { navController.popBackStack() },
                viewModel     = viewModel
            )
        }

        composable(Routes.ACCOUNTS) {
            AccountsScreen(
                onBack           = { navController.popBackStack() },
                onAddTransaction = { navController.navigate(Routes.addTransaction()) }
            )
        }

        composable(
            route     = Routes.ACCOUNT_DETAILS,
            arguments = listOf(navArgument("accountId") { type = NavType.LongType })
        ) { backStack ->
            val accountId = backStack.arguments?.getLong("accountId") ?: return@composable
            AccountDetailScreen(accountId = accountId, onBack = { navController.popBackStack() })
        }

        composable(
            route     = Routes.ACCOUNT_RECORDS,
            arguments = listOf(navArgument("accountId") { type = NavType.LongType })
        ) { backStack ->
            val accountId = backStack.arguments?.getLong("accountId") ?: return@composable
            RecordsScreen(
                accountId          = accountId,
                onBack             = { navController.popBackStack() },
                onTransactionClick = { id -> navController.navigate(Routes.addTransaction(id)) }
            )
        }

        composable(Routes.IMPORT) {
            ImportScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
