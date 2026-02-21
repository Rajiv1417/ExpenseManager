package com.expensemanager.ui.screens.accountdetail

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun AccountDetailScreen(
    accountId: Long,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) {
        Text("Account ID: $accountId")
    }
}
