package com.expensemanager.ui.screens.accountdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.expensemanager.ui.components.TransactionItem
import com.expensemanager.utils.CurrencyFormatter

@Composable
fun AccountDetailScreen(
    accountId: Long,
    onBack: () -> Unit,
    onTransactionClick: (Long) -> Unit,
    onEditAccount: () -> Unit,
    onOpenRecords: () -> Unit,
    viewModel: AccountDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(accountId) {
        viewModel.load(accountId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account Detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                },
                actions = {
                    IconButton(onClick = onEditAccount) { Icon(Icons.Default.Edit, "Edit") }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Column(modifier = Modifier.fillMaxSize().padding(padding), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = Color(uiState.account?.color ?: 0xFF4CAF50))) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text(uiState.account?.name ?: "", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                        Text(uiState.account?.type?.name ?: "", color = Color.White.copy(alpha = 0.8f))
                        Spacer(Modifier.height(12.dp))
                        Text("Today", color = Color.White.copy(alpha = 0.75f))
                        Text(
                            CurrencyFormatter.format(uiState.account?.balance ?: 0.0),
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            item {
                FilledTonalButton(onClick = onOpenRecords, modifier = Modifier.fillMaxWidth()) {
                    Text("Open Records")
                }
            }

            item { Text("Last records overview", style = MaterialTheme.typography.titleMedium) }

            items(uiState.transactions.take(20)) { transaction ->
                TransactionItem(
                    transaction = transaction,
                    onClick = { onTransactionClick(transaction.id) }
                )
            }
        }
    }
}
