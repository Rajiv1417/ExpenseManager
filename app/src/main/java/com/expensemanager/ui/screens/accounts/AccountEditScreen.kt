package com.expensemanager.ui.screens.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.expensemanager.data.local.entities.AccountType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountEditScreen(
    accountId: Long?,
    onBack: () -> Unit,
    viewModel: AccountsViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(AccountType.CASH) }
    var currency by remember { mutableStateOf("INR") }
    var color by remember { mutableStateOf(0xFF66DD22L) }
    var isActive by remember { mutableStateOf(true) }

    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(accountId) {
        if (accountId != null) {
            viewModel.getAccountById(accountId)?.let { account ->
                name = account.name
                type = account.type
                currency = account.currency
                color = account.color
                isActive = account.isActive
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (accountId == null) "Add account" else "Edit account") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.Close, "Close") }
                },
                actions = {
                    if (accountId != null) {
                        IconButton(onClick = {
                            viewModel.deleteAccount(accountId)
                            onBack()
                        }) {
                            Icon(Icons.Default.Delete, "Delete")
                        }
                    }
                    IconButton(onClick = {
                        if (name.isNotBlank()) {
                            viewModel.saveAccount(accountId, name, type, currency, color, isActive)
                            onBack()
                        }
                    }) {
                        Icon(Icons.Default.Check, "Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Account name") },
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = type.name.replace('_', ' '),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    AccountType.values().forEach { accountType ->
                        DropdownMenuItem(
                            text = { Text(accountType.name.replace('_', ' ')) },
                            onClick = {
                                type = accountType
                                expanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = currency,
                onValueChange = { currency = it.uppercase() },
                label = { Text("Currency") },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Color")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(0xFF66DD22L, 0xFF02A9F4L, 0xFFFAB801L, 0xFF9C27B0L, 0xFF009688L).forEach { option ->
                    ColorSwatch(color = option, selected = color == option, onClick = { color = option })
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("Archive", modifier = Modifier.weight(1f))
                Switch(checked = !isActive, onCheckedChange = { isActive = !it })
            }
        }
    }
}

@Composable
private fun ColorSwatch(color: Long, selected: Boolean, onClick: () -> Unit) {
    androidx.compose.material3.Surface(
        modifier = Modifier
            .width(48.dp)
            .height(32.dp),
        shape = RoundedCornerShape(8.dp),
        onClick = onClick,
        tonalElevation = if (selected) 6.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(color)),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selected) Text("✓", color = Color.White)
        }
    }
}
