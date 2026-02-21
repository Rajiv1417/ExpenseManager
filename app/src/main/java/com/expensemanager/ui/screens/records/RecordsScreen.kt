package com.expensemanager.ui.screens.records

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun RecordsScreen(
    accountId: Long?,
    onBack: () -> Unit,
    onTransactionClick: (Long) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Records") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) {
        Text("Records for account: $accountId")
    }
}
