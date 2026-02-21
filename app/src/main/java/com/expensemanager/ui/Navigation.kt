package com.expensemanager.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.expensemanager.ui.screens.accountdetail.AccountDetailScreen
import com.expensemanager.ui.screens.accounts.AccountEditScreen
import com.expensemanager.ui.screens.accounts.AccountsScreen
import com.expensemanager.ui.screens.addtransaction.AddTransactionScreen
import com.expensemanager.ui.screens.dashboard.DashboardScreen
import com.expensemanager.ui.screens.import_screen.ImportScreen
import com.expensemanager.ui.screens.records.RecordsScreen
import com.expensemanager.ui.screens.settings.SettingsScreen

object Routes {
    const val DASHBOARD = "dashboard"
    const val ADD_TRANSACTION = "add_transaction?transactionId={transactionId}"
    const val ACCOUNTS = "accounts"
    const val ACCOUNT_EDIT = "account_edit?accountId={accountId}"
    const val ACCOUNT_DETAIL = "account_detail/{accountId}"
    const val RECORDS = "records?accountIds={accountIds}"
    const val IMPORT = "import"
    const val SETTINGS = "settings"

    fun addTransaction(transactionId: Long? = null) =
        "add_transaction?transactionId=${transactionId ?: -1}"

    fun accountEdit(accountId: Long? = null) =
        "account_edit?accountId=${accountId ?: -1}"

    fun accountDetail(accountId: Long) = "account_detail/$accountId"

    fun records(accountIds: List<Long>) =
        "records?accountIds=${accountIds.joinToString(",")}"
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
                onAccountDetailClick = { accountId -> navController.navigate(Routes.accountDetail(accountId)) },
                onRecordsClick = { accountIds -> navController.navigate(Routes.records(accountIds)) },
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
                onAddAccount = { navController.navigate(Routes.accountEdit()) },
                onAccountClick = { id -> navController.navigate(Routes.accountEdit(id)) }
            )
        }
        composable(
            route = Routes.ACCOUNT_EDIT,
            arguments = listOf(
                navArgument("accountId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStack ->
            val accountId = backStack.arguments?.getLong("accountId")?.takeIf { it != -1L }
            AccountEditScreen(
                accountId = accountId,
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Routes.ACCOUNT_DETAIL,
            arguments = listOf(
                navArgument("accountId") { type = NavType.LongType }
            )
        ) { backStack ->
            val accountId = backStack.arguments?.getLong("accountId") ?: return@composable
            AccountDetailScreen(
                accountId = accountId,
                onBack = { navController.popBackStack() },
                onTransactionClick = { id -> navController.navigate(Routes.addTransaction(id)) },
                onEditAccount = { navController.navigate(Routes.accountEdit(accountId)) },
                onOpenRecords = { navController.navigate(Routes.records(listOf(accountId))) }
            )
        }
        composable(
            route = Routes.RECORDS,
            arguments = listOf(
                navArgument("accountIds") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStack ->
            val accountIds = backStack.arguments?.getString("accountIds")
                ?.split(",")
                ?.mapNotNull { it.toLongOrNull() }
                ?.filter { it > 0 }
                ?: emptyList()
            RecordsScreen(
                accountIds = accountIds,
                onBack = { navController.popBackStack() },
                onTransactionClick = { id -> navController.navigate(Routes.addTransaction(id)) }
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
