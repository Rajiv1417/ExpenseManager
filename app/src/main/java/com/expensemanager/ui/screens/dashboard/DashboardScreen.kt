package com.expensemanager.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.expensemanager.ui.components.TransactionItem
import com.expensemanager.utils.CurrencyFormatter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onAddTransaction: () -> Unit,
    onTransactionClick: (Long) -> Unit,
    onAccountsClick: () -> Unit,
    onAccountDetailsClick: (Long) -> Unit,
    onAccountRecordsClick: (Long) -> Unit,
    onImportClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {

    val uiState by viewModel.uiState.collectAsState()

    var selectedAccountId by remember { mutableStateOf<Long?>(null) }
    var showAccountActions by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {

            TopAppBar(

                title = {
                    Column {
                        Text(
                            "Expense Manager",
                            style = MaterialTheme.typography.titleLarge
                        )

                        Text(
                            LocalDateTime.now().format(
                                DateTimeFormatter.ofPattern("MMMM yyyy")
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),

                actions = {

                    IconButton(onClick = onImportClick) {
                        Icon(Icons.Default.FileUpload, "Import")
                    }

                    IconButton(onClick = onAccountsClick) {
                        Icon(Icons.Default.AccountBalance, "Accounts")
                    }

                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                }
            )
        },

        floatingActionButton = {

            FloatingActionButton(
                onClick = onAddTransaction,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, "Add Transaction")
            }
        }

    ) { padding ->

        if (uiState.isLoading) {

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            item {

                BalanceOverviewCard(
                    totalBalance = uiState.totalBalance,
                    monthlyIncome = uiState.monthlyIncome,
                    monthlyExpense = uiState.monthlyExpense,
                    modifier = Modifier.padding(16.dp)
                )
            }

            item {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Text(
                        "Accounts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = onAccountsClick) {

                        Icon(Icons.Default.Settings, "Manage Accounts")
                    }
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    items(uiState.accounts) { account ->

                        AccountChip(
                            name = account.name,
                            balance = account.balance,
                            color = Color(account.color),
                            isSelected = selectedAccountId == account.id,
                            onClick = {

                                selectedAccountId = account.id
                                showAccountActions = true
                            }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
            }

            item {

                Text(
                    "Recent Transactions",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            items(uiState.recentTransactions) { transaction ->

                TransactionItem(
                    transaction = transaction,
                    onClick = {
                        onTransactionClick(transaction.id)
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }

    // Bottom sheet OUTSIDE Scaffold (correct placement)

    if (showAccountActions && selectedAccountId != null) {

        ModalBottomSheet(
            onDismissRequest = {
                showAccountActions = false
            }
        ) {

            ListItem(
                headlineContent = { Text("Account Details") },
                leadingContent = {
                    Icon(Icons.Default.AccountBalance, null)
                },
                modifier = Modifier.clickable {

                    showAccountActions = false

                    onAccountDetailsClick(selectedAccountId!!)
                }
            )

            ListItem(
                headlineContent = { Text("View Records") },
                leadingContent = {
                    Icon(Icons.Default.ReceiptLong, null)
                },
                modifier = Modifier.clickable {

                    showAccountActions = false

                    onAccountRecordsClick(selectedAccountId!!)
                }
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun AccountChip(
    name: String,
    balance: Double,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .width(160.dp)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick),

        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Icon(
                Icons.Default.AccountBalance,
                contentDescription = null,
                tint = color
            )

            Spacer(Modifier.height(8.dp))

            Text(name)

            Text(
                CurrencyFormatter.format(balance),
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun BalanceOverviewCard(
    totalBalance: Double,
    monthlyIncome: Double,
    monthlyExpense: Double,
    modifier: Modifier = Modifier
) {

    Card(
        modifier = modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text("Total Balance")

            Text(
                CurrencyFormatter.format(totalBalance),
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(Modifier.height(8.dp))

            Text("Income: ${CurrencyFormatter.format(monthlyIncome)}")

            Text("Expense: ${CurrencyFormatter.format(monthlyExpense)}")
        }
    }
}
