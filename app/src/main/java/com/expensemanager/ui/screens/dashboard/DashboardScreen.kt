@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.expensemanager.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.expensemanager.data.local.entities.AccountEntity
import com.expensemanager.data.local.entities.TransactionEntity
import com.expensemanager.ui.components.TransactionItem
import com.expensemanager.utils.CurrencyFormatter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun DashboardScreen(
    onAddTransaction: () -> Unit,
    onTransactionClick: (Long) -> Unit,
    onAccountsClick: () -> Unit,
    onAccountDetailClick: (Long) -> Unit,
    onRecordsClick: (List<Long>) -> Unit,
    onImportClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedAccountIds by remember { mutableStateOf<Set<Long>>(emptySet()) }

    LaunchedEffect(uiState.accounts) {
        val activeIds = uiState.accounts.filter { it.isActive }.map { it.id }.toSet()
        selectedAccountIds = if (selectedAccountIds.isEmpty()) activeIds else selectedAccountIds.intersect(activeIds)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Home", style = MaterialTheme.typography.titleLarge)
                        Text(
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM yyyy")),
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
                    IconButton(onClick = onImportClick) { Icon(Icons.Default.FileUpload, "Import") }
                    IconButton(onClick = onSettingsClick) { Icon(Icons.Default.Settings, "Settings") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTransaction, shape = CircleShape) {
                Icon(Icons.Default.Add, "Add Transaction")
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(500)) + slideInVertically(tween(500))
                ) {
                    BalanceOverviewCard(
                        totalBalance = uiState.totalBalance,
                        monthlyIncome = uiState.monthlyIncome,
                        monthlyExpense = uiState.monthlyExpense,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            item {
                AccountsSection(
                    accounts = uiState.accounts,
                    selectedAccountIds = selectedAccountIds,
                    onToggleAccount = { id ->
                        selectedAccountIds = if (selectedAccountIds.contains(id)) {
                            selectedAccountIds - id
                        } else {
                            selectedAccountIds + id
                        }
                    },
                    onSelectAll = {
                        selectedAccountIds = uiState.accounts.filter { it.isActive }.map { it.id }.toSet()
                    },
                    onOpenAccountsSettings = onAccountsClick,
                    onOpenAccountDetail = {
                        selectedAccountIds.singleOrNull()?.let(onAccountDetailClick)
                    },
                    onOpenRecords = { onRecordsClick(selectedAccountIds.toList()) }
                )
                Spacer(Modifier.height(8.dp))
            }

            item {
                SectionHeader(title = "Spending Overview", onSeeAll = null)
                TabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    listOf("Daily", "Monthly", "Yearly").forEachIndexed { idx, label ->
                        Tab(selected = selectedTab == idx, onClick = { selectedTab = idx }, text = { Text(label) })
                    }
                }
                Spacer(Modifier.height(8.dp))
                SpendingBarChart(
                    data = uiState.dailyExpenses,
                    modifier = Modifier.fillMaxWidth().height(180.dp).padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(16.dp))
            }

            item { SectionHeader(title = "Recent Transactions", onSeeAll = null) }
            if (uiState.recentTransactions.isEmpty()) {
                item { EmptyTransactionsPlaceholder(onAddTransaction) }
            } else {
                items(uiState.recentTransactions) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        onClick = { onTransactionClick(transaction.id) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountsSection(
    accounts: List<AccountEntity>,
    selectedAccountIds: Set<Long>,
    onToggleAccount: (Long) -> Unit,
    onSelectAll: () -> Unit,
    onOpenAccountsSettings: () -> Unit,
    onOpenAccountDetail: () -> Unit,
    onOpenRecords: () -> Unit
) {
    Card(
        modifier = Modifier.padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("List of accounts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onOpenAccountsSettings) {
                    Icon(Icons.Default.Settings, contentDescription = "Account settings")
                }
            }
            accounts.chunked(3).forEach { rowAccounts ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowAccounts.forEach { account ->
                        val selected = selectedAccountIds.contains(account.id)
                        AccountGridItem(
                            modifier = Modifier.weight(1f),
                            account = account,
                            selected = selected,
                            onClick = { onToggleAccount(account.id) }
                        )
                    }
                    repeat(3 - rowAccounts.size) { Spacer(Modifier.weight(1f)) }
                }
                Spacer(Modifier.height(8.dp))
            }
            TextButton(onClick = onSelectAll, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("Select all non-excluded")
            }

            if (selectedAccountIds.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilledTonalButton(
                        modifier = Modifier.weight(1f),
                        onClick = onOpenAccountDetail,
                        enabled = selectedAccountIds.size == 1
                    ) { Text("ACCOUNT DETAIL") }
                    FilledTonalButton(modifier = Modifier.weight(1f), onClick = onOpenRecords) { Text("RECORDS") }
                }
            }
        }
    }
}

@Composable
private fun AccountGridItem(
    modifier: Modifier,
    account: AccountEntity,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Color(account.color) else Color(0xFFB0B0B0)
        )
    ) {
        Column(Modifier.padding(10.dp)) {
            Text(account.name, maxLines = 1, color = Color.White, style = MaterialTheme.typography.bodySmall)
            Text(
                CurrencyFormatter.format(account.balance),
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun BalanceOverviewCard(totalBalance: Double, monthlyIncome: Double, monthlyExpense: Double, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Box(
            modifier = Modifier.fillMaxWidth().background(
                Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)),
                shape = RoundedCornerShape(24.dp)
            ).padding(24.dp)
        ) {
            Column {
                Text("Total Balance", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
                Spacer(Modifier.height(4.dp))
                Text(CurrencyFormatter.format(totalBalance), style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
                Spacer(Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    BalanceStat("Income", monthlyIncome, Color(0xFFB2F0E8))
                    Divider(modifier = Modifier.height(40.dp).width(1.dp), color = Color.White.copy(alpha = 0.3f))
                    BalanceStat("Expense", monthlyExpense, Color(0xFFFFCDD2))
                }
            }
        }
    }
}

@Composable
private fun BalanceStat(label: String, amount: Double, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(color.copy(alpha = 0.3f)))
        Spacer(Modifier.width(8.dp))
        Column {
            Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
            Text(CurrencyFormatter.formatCompact(amount), color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }
    }
}

@Composable
fun SpendingBarChart(data: List<Pair<String, Double>>, modifier: Modifier = Modifier) {
    if (data.isEmpty()) {
        Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No spending data this period") }
        }
        return
    }
    val maxValue = data.maxOf { it.second }
    Card(modifier = modifier, elevation = CardDefaults.cardElevation(2.dp)) {
        Row(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            data.takeLast(14).forEach { (date, amount) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom, modifier = Modifier.weight(1f).fillMaxSize()) {
                    val fraction = if (maxValue > 0) (amount / maxValue).toFloat() else 0f
                    Box(
                        modifier = Modifier.fillMaxWidth(0.6f).weight(fraction.coerceIn(0.05f, 1f)).clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)).background(MaterialTheme.colorScheme.primary)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(text = date.takeLast(2), fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, onSeeAll: (() -> Unit)?) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        if (onSeeAll != null) {
            TextButton(onClick = onSeeAll) { Text("See All") }
        }
    }
}

@Composable
fun EmptyTransactionsPlaceholder(onAdd: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Outlined.ReceiptLong, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        Spacer(Modifier.height(16.dp))
        Text("No transactions yet", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text("Add your first transaction.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(16.dp))
        FilledTonalButton(onClick = onAdd) {
            Icon(Icons.Default.Add, null)
            Spacer(Modifier.width(8.dp))
            Text("Add Transaction")
        }
    }
}
