package com.expensemanager.ui.screens.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.expensemanager.data.local.entities.AccountEntity
import com.expensemanager.utils.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    onBack: () -> Unit,
    onAddTransaction: (Long) -> Unit,
    viewModel: AccountsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddAccountDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Accounts") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddAccountDialog = true }) {
                        Icon(Icons.Default.Add, "Add Account")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                // Total Balance Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Net Worth", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            CurrencyFormatter.format(uiState.totalBalance),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            items(uiState.accounts) { account ->
                AccountCard(
                    account = account,
                    onAddTransaction = { onAddTransaction(account.id) }
                )
            }
        }
    }

    if (showAddAccountDialog) {
        AddAccountDialog(
            onAdd = { name, type, balance ->
                viewModel.addAccount(name, type, balance)
                showAddAccountDialog = false
            },
            onDismiss = { showAddAccountDialog = false }
        )
    }
}

@Composable
fun AccountCard(
    account: AccountEntity,
    onAddTransaction: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(account.color).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AccountBalance,
                    null,
                    tint = Color(account.color),
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(account.name, fontWeight = FontWeight.SemiBold)
                Text(account.type.name, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    CurrencyFormatter.format(account.balance),
                    fontWeight = FontWeight.Bold,
                    color = if (account.balance >= 0) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error
                )
                IconButton(onClick = onAddTransaction, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Add, "Add transaction", modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountDialog(
    onAdd: (String, com.expensemanager.data.local.entities.AccountType, Double) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var balance by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(com.expensemanager.data.local.entities.AccountType.BANK) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Account") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Account Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = balance,
                    onValueChange = { balance = it },
                    label = { Text("Opening Balance (₹)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Account Type", style = MaterialTheme.typography.labelMedium)
                com.expensemanager.data.local.entities.AccountType.values().forEach { type ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selectedType == type, onClick = { selectedType = type })
                        Text(type.name)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onAdd(name, selectedType, balance.toDoubleOrNull() ?: 0.0)
            }) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
