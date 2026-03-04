package com.expensemanager.ui.screens.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.expensemanager.data.local.entities.AccountEntity
import com.expensemanager.data.local.entities.AccountType
import com.expensemanager.data.local.entities.AccountWithBalance
import com.expensemanager.utils.CurrencyFormatter

@Composable
fun AccountsScreen(
    onBack: () -> Unit,
    onAddTransaction: (Long) -> Unit,
    viewModel: AccountsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddAccountDialog by remember { mutableStateOf(false) }
    var editingAccount by remember { mutableStateOf<AccountEntity?>(null) }
    var deletingAccount by remember { mutableStateOf<AccountEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Accounts") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
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
                    onAddTransaction = { onAddTransaction(account.account.id) },
                    onEdit = { editingAccount = account.account },
                    onDelete = { deletingAccount = account.account }
                )
            }
        }
    }

    if (showAddAccountDialog) {
        AccountDialog(
            title = "Add Account",
            initialAccount = null,
            defaultColor = viewModel.getSuggestedColor(),
            onSubmit = { name, type, balance, color, symbol ->
                viewModel.addAccount(name, type, balance, color, symbol)
                showAddAccountDialog = false
            },
            onDismiss = { showAddAccountDialog = false }
        )
    }

    editingAccount?.let { account ->
        AccountDialog(
            title = "Edit Account",
            initialAccount = account,
            defaultColor = account.color,
            onSubmit = { name, type, balance, color, symbol ->
                viewModel.updateAccount(account, name, type, balance, color, symbol)
                editingAccount = null
            },
            onDismiss = { editingAccount = null }
        )
    }

    deletingAccount?.let { account ->
        AlertDialog(
            onDismissRequest = { deletingAccount = null },
            title = { Text("Delete ${account.name}?") },
            text = { Text("This will remove the account. Transactions linked to it may block deletion depending on constraints.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAccount(account)
                    deletingAccount = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { deletingAccount = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun AccountCard(
    account: AccountWithBalance,
    onAddTransaction: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
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
                    .background(Color(account.account.color).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = account.account.accountNumber?.take(2)?.uppercase()
                        ?: account.account.name.take(1).uppercase(),
                    color = Color(account.account.color),
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(account.account.name, fontWeight = FontWeight.SemiBold)
                Text(
                    account.account.type.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    CurrencyFormatter.format(account.balance),
                    fontWeight = FontWeight.Bold,
                    color = if (account.balance >= 0) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error
                )
                Row {
                    IconButton(onClick = onAddTransaction, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Add, "Add transaction", modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, "Edit account", modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, "Delete account", modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AccountDialog(
    title: String,
    initialAccount: AccountEntity?,
    defaultColor: Long,
    onSubmit: (String, AccountType, Double, Long, String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialAccount?.name ?: "") }
    var balance by remember { mutableStateOf(initialAccount?.initialValue?.toString() ?: "") }
    var symbol by remember { mutableStateOf(initialAccount?.accountNumber ?: "") }
    var selectedType by remember { mutableStateOf(initialAccount?.type ?: AccountType.BANK) }
    var selectedColor by remember { mutableStateOf(initialAccount?.color ?: defaultColor) }

    val palette = listOf(0xFF43A047, 0xFF1A6EDD, 0xFFE53935, 0xFF8E24AA, 0xFF00B67B, 0xFFFF8F00)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
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
                OutlinedTextField(
                    value = symbol,
                    onValueChange = { symbol = it.take(4) },
                    label = { Text("Symbol / tag (e.g. SB)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Account Type", style = MaterialTheme.typography.labelMedium)
                AccountType.values().forEach { type ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selectedType == type, onClick = { selectedType = type })
                        Text(type.name)
                    }
                }
                Text("Identifier Color", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    palette.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(color))
                                .padding(2.dp)
                        ) {
                            RadioButton(
                                selected = selectedColor == color,
                                onClick = { selectedColor = color }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSubmit(name, selectedType, balance.toDoubleOrNull() ?: 0.0, selectedColor, symbol)
            }) { Text(if (initialAccount == null) "Add" else "Update") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
