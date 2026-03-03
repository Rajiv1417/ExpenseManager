package com.expensemanager.ui
import com.expensemanager.ui.screens.accountdetail.AccountDetailScreen
import com.expensemanager.ui.screens.records.RecordsScreen
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

    const val ADD_TRANSACTION =
        "add_transaction?transactionId={transactionId}"

    const val ACCOUNTS = "accounts"

    const val IMPORT = "import"

    const val SETTINGS = "settings"

    const val ACCOUNT_DETAILS =
        "account_details/{accountId}"

    const val ACCOUNT_RECORDS =
        "account_records/{accountId}"

    const val ADD_ACCOUNT = "add_account"

    const val EDIT_ACCOUNT = "edit_account/{accountId}"

    fun editAccount(accountId: Long): String {
        return "edit_account/$accountId"


    fun addTransaction(transactionId: Long? = null) =
        "add_transaction?transactionId=${transactionId ?: -1}"

    fun accountDetails(accountId: Long) =
        "account_details/$accountId"

    fun accountRecords(accountId: Long) =
        "account_records/$accountId"
}
@Composable
fun AppNavGraph(navController: NavHostController) {

    NavHost(
        navController = navController,
        startDestination = Routes.DASHBOARD
    ) {

        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onAddTransaction = {
                    navController.navigate(Routes.addTransaction())
                },
                onTransactionClick = { id ->
                    navController.navigate(Routes.addTransaction(id))
                },
                onAccountsClick = {
                    navController.navigate(Routes.ACCOUNTS)
                },
                onImportClick = {
                    navController.navigate(Routes.IMPORT)
                },
                onSettingsClick = {
                    navController.navigate(Routes.SETTINGS)
                }
            )
        }

        composable(Routes.ACCOUNTS) {
            AccountsScreen(
                onBack = { navController.popBackStack() },
                onAddAccount = {
                    navController.navigate(Routes.ADD_ACCOUNT)
                },
                onAccountClick = { accountId ->
                    navController.navigate(
                        Routes.editAccount(accountId)
                    )
                }
            )
        }

        composable(Routes.ADD_ACCOUNT) {
            AddAccountScreen(
                onBack = { navController.popBackStack() },
                onSaved = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Routes.EDIT_ACCOUNT,
            arguments = listOf(
                navArgument("accountId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->

            val accountId =
                backStackEntry.arguments?.getLong("accountId")
                    ?: return@composable

            EditAccountScreen(
                accountId = accountId,
                onBack = { navController.popBackStack() }
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