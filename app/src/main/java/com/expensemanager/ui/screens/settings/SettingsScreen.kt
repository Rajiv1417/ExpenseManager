package com.expensemanager.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.expensemanager.utils.ExportHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item { SettingsGroupHeader("Appearance") }
            item {
                SettingsToggleItem(
                    icon = Icons.Default.DarkMode,
                    title = "Dark Mode",
                    subtitle = "Use dark theme",
                    checked = uiState.isDarkMode,
                    onToggle = { viewModel.setDarkMode(it) }
                )
            }

            item { SettingsGroupHeader("Export") }
            item {
                SettingsActionItem(
                    icon = Icons.Default.TableChart,
                    title = "Export as CSV",
                    subtitle = "Export all transactions",
                    onClick = {
                        viewModel.getTransactions { transactions ->
                            ExportHelper.exportToCsv(context, transactions)?.let { uri ->
                                ExportHelper.shareFile(context, uri, "text/csv")
                            }
                        }
                    }
                )
            }
            item {
                SettingsActionItem(
                    icon = Icons.Default.GridOn,
                    title = "Export as Excel",
                    subtitle = "Export all transactions",
                    onClick = {
                        viewModel.getTransactions { transactions ->
                            ExportHelper.exportToExcel(context, transactions)?.let { uri ->
                                ExportHelper.shareFile(context, uri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                            }
                        }
                    }
                )
            }
            item {
                SettingsActionItem(
                    icon = Icons.Default.PictureAsPdf,
                    title = "Export as PDF",
                    subtitle = "Export all transactions",
                    onClick = {
                        viewModel.getTransactions { transactions ->
                            ExportHelper.exportToPdf(context, transactions)?.let { uri ->
                                ExportHelper.shareFile(context, uri, "application/pdf")
                            }
                        }
                    }
                )
            }

            item { SettingsGroupHeader("Notifications") }
            item {
                SettingsToggleItem(
                    icon = Icons.Default.Notifications,
                    title = "SMS Auto-Detection",
                    subtitle = "Auto-detect transactions from bank SMS",
                    checked = uiState.smsDetectionEnabled,
                    onToggle = { viewModel.setSmsDetection(it) }
                )
            }
            item {
                SettingsToggleItem(
                    icon = Icons.Default.Repeat,
                    title = "Recurring Reminders",
                    subtitle = "Get reminders for recurring transactions",
                    checked = uiState.recurringRemindersEnabled,
                    onToggle = { viewModel.setRecurringReminders(it) }
                )
            }

            item { SettingsGroupHeader("About") }
            item {
                SettingsInfoItem(
                    icon = Icons.Default.Info,
                    title = "Version",
                    subtitle = "1.0.0"
                )
            }
        }
    }
}

@Composable
fun SettingsGroupHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp, start = 4.dp)
    )
}

@Composable
fun SettingsToggleItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Medium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = checked, onCheckedChange = onToggle)
        }
    }
}

@Composable
fun SettingsActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Medium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun SettingsInfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Medium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
