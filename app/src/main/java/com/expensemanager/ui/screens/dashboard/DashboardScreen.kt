package com.expensemanager.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.expensemanager.data.local.entities.AccountWithBalance
import com.expensemanager.utils.CurrencyFormatter

private val PrimaryMint = Color(0xFF3DDAAE)
private val Jade = Color(0xFF00B67B)
private val OrangeAccent = Color(0xFFFF712B)
private val BackgroundLight = Color(0xFFF4F7F9)
private val TextMain = Color(0xFF1A2C38)
private val TextMuted = Color(0xFF6E8CA0)

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

    Scaffold(
        containerColor = BackgroundLight,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTransaction,
                containerColor = Color.Transparent,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            brush = Brush.horizontalGradient(listOf(PrimaryMint, Jade)),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }
        },
        bottomBar = {
            LiquidBottomNav(
                onHomeClick = {},
                onRecordsClick = onImportClick,
                onAccountsClick = onAccountsClick,
                onSettingsClick = onSettingsClick
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AmbientBackground()

            Column(modifier = Modifier.fillMaxSize()) {
                DashboardHeader()
                NetWorthCard(
                    totalBalance = uiState.totalBalance,
                    liquidAmount = uiState.accounts.filter { it.balance >= 0 }.sumOf { it.balance },
                    investAmount = uiState.accounts.filter { it.balance < 0 }.sumOf { kotlin.math.abs(it.balance) }
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 110.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.accounts, key = { it.id }) { account ->
                        DashboardAccountCard(
                            account = account,
                            onClick = { onAccountDetailsClick(account.id) },
                            onLongInfo = { onAccountRecordsClick(account.id) }
                        )
                    }

                    item {
                        AddAccountPlaceholder(onClick = onAccountsClick)
                    }
                }
            }
        }
    }
}

@Composable
private fun AmbientBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(PrimaryMint.copy(alpha = 0.20f), Color.Transparent),
                    radius = 760f
                )
            )
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(OrangeAccent.copy(alpha = 0.10f), Color.Transparent),
                    radius = 860f,
                    center = androidx.compose.ui.geometry.Offset(1200f, 2200f)
                )
            )
    )
}

@Composable
private fun DashboardHeader() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.82f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("My Wallets", color = TextMain, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Manage your sources", color = TextMuted, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = {}) {
                Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = TextMain)
            }
        }
    }
}

@Composable
private fun NetWorthCard(
    totalBalance: Double,
    liquidAmount: Double,
    investAmount: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(TextMain, Color(0xFF2C4A5F))))
                .padding(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.TopEnd)
                    .background(Color.White.copy(alpha = 0.08f), CircleShape)
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text("Total Net Worth", color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall)
                        Text(
                            CurrencyFormatter.format(totalBalance),
                            color = Color.White,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.12f))
                            .padding(8.dp)
                    ) {
                        Icon(Icons.Default.Home, contentDescription = null, tint = PrimaryMint)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SummaryPill(label = "Liquid", value = CurrencyFormatter.format(liquidAmount), dotColor = PrimaryMint)
                    SummaryPill(label = "Invest", value = CurrencyFormatter.format(investAmount), dotColor = OrangeAccent)
                }
            }
        }
    }
}

@Composable
private fun SummaryPill(label: String, value: String, dotColor: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Text("$label: $value", color = Color.White, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun DashboardAccountCard(
    account: AccountWithBalance,
    onClick: () -> Unit,
    onLongInfo: () -> Unit
) {
    val (start, end, icon) = accountVisuals(account)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(4f / 3f)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(listOf(start, end)))
                .padding(14.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Text(
                        text = account.type.name,
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Column {
                    Text(
                        CurrencyFormatter.format(account.balance),
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            account.name,
                            color = Color.White.copy(alpha = 0.72f),
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "i",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .clip(CircleShape)
                                .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                                .clickable { onLongInfo() }
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun accountVisuals(account: AccountWithBalance): Triple<Color, Color, androidx.compose.ui.graphics.vector.ImageVector> {
    return when {
        account.balance < 0 -> Triple(Color(0xFF1A2C38), Color(0xFF2C3E50), Icons.Default.CreditCard)
        account.type.name.contains("SAV", ignoreCase = true) -> Triple(Color(0xFFFF712B), Color(0xFFFF4B6E), Icons.Default.Savings)
        account.type.name.contains("CRYPTO", ignoreCase = true) -> Triple(Color(0xFF4F46E5), Color(0xFF7C3AED), Icons.Default.CurrencyBitcoin)
        else -> Triple(Color(0xFF10221B), Color(0xFF00B67B), Icons.Default.AccountBalance)
    }
}

@Composable
private fun AddAccountPlaceholder(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(4f / 3f)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.4f))
            .border(2.dp, PrimaryMint.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(PrimaryMint.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = PrimaryMint)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Add Account", color = TextMuted, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun LiquidBottomNav(
    onHomeClick: () -> Unit,
    onRecordsClick: () -> Unit,
    onAccountsClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.82f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            BottomNavItem("Home", Icons.Default.Home, false, onHomeClick)
            BottomNavItem("Records", Icons.Default.ReceiptLong, false, onRecordsClick)
            Spacer(modifier = Modifier.size(52.dp))
            BottomNavItem("Accounts", Icons.Default.CreditCard, true, onAccountsClick)
            BottomNavItem("Settings", Icons.Default.Settings, false, onSettingsClick)
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = if (isActive) TextMain else TextMuted,
            modifier = Modifier.size(24.dp)
        )
        Text(
            label,
            color = if (isActive) TextMain else TextMuted,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
        )
    }
}
