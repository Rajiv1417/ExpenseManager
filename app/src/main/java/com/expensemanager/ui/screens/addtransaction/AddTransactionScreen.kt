package com.expensemanager.ui.screens.addtransaction

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.expensemanager.data.local.entities.AccountWithBalance
import com.expensemanager.data.local.entities.CategoryEntity
import com.expensemanager.data.local.entities.PaymentStatus
import com.expensemanager.data.local.entities.PaymentType
import com.expensemanager.data.local.entities.TransactionType
import com.expensemanager.ui.theme.ExpenseColor
import com.expensemanager.ui.theme.IncomeColor
import com.expensemanager.ui.theme.TransferColor
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    transactionId: Long? = null,
    onBack: () -> Unit,
    viewModel: AddTransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(transactionId) { transactionId?.let { viewModel.loadTransaction(it) } }
    LaunchedEffect(uiState.isSaved) { if (uiState.isSaved) onBack() }
    uiState.error?.let { LaunchedEffect(it) { viewModel.clearError() } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditMode) "Edit Transaction" else "Add Transaction") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                },
                actions = {
                    if (uiState.isEditMode) {
                        IconButton(onClick = { viewModel.showRefundSheet(true) }) { Icon(Icons.Default.Replay, "Add Refund/Cashback") }
                        IconButton(onClick = { viewModel.deleteCurrentTransaction() }) { Icon(Icons.Default.Delete, "Delete transaction") }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
        ) {
            TypeSelector(uiState.transactionType, { viewModel.setTransactionType(it) }, Modifier.padding(16.dp))
            AmountField(uiState.amount, { viewModel.setAmount(it) }, uiState.transactionType, Modifier.padding(horizontal = 16.dp))
            Spacer(Modifier.height(16.dp))

            FormField("Account") {
                AccountDropdown(uiState.accounts, uiState.selectedAccount) { viewModel.setAccount(it) }
            }

            if (uiState.transactionType == TransactionType.TRANSFER) {
                FormField("To Account") {
                    AccountDropdown(
                        accounts = uiState.accounts.filter { it.account.id != uiState.selectedAccount?.account?.id },
                        selected = uiState.selectedToAccount,
                        onSelected = { viewModel.setToAccount(it) }
                    )
                }
            }

            if (uiState.transactionType != TransactionType.TRANSFER) {
                FormField("Category") {
                    CategoryDropdown(
                        categories = uiState.categories,
                        selected = uiState.selectedCategory,
                        onSelected = { viewModel.setCategory(it) },
                        onAddCategory = { viewModel.addCategory(it) }
                    )
                }
            }

            FormField("Date & Time") {
                DateTimeField(uiState.dateTime) { viewModel.setDateTime(it) }
            }

            if (uiState.dateTime.isAfter(LocalDateTime.now())) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Future transaction will be saved as PENDING (scheduled) and excluded from balances until cleared.")
                }
            }

            FormField(if (uiState.transactionType == TransactionType.INCOME) "Payer" else "Payee") {
                OutlinedTextField(uiState.payee, { viewModel.setPayee(it) }, Modifier.fillMaxWidth(), placeholder = { Text("Optional") }, singleLine = true)
            }

            FormField("Notes") {
                OutlinedTextField(uiState.notes, { viewModel.setNotes(it) }, Modifier.fillMaxWidth(), placeholder = { Text("Optional") }, minLines = 2, maxLines = 4)
            }

            FormField("Payment Type") {
                PaymentTypeSelector(uiState.paymentType) { viewModel.setPaymentType(it) }
            }

            FormField("Status") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (s in PaymentStatus.values()) {
                        FilterChip(selected = uiState.status == s, onClick = { viewModel.setStatus(s) }, label = { Text(s.name) })
                    }
                }
            }

            FormField("Labels") {
                LabelsInput(uiState.labels, onAdd = { viewModel.addLabel(it) }, onRemove = { viewModel.removeLabel(it) })
            }

            FormField("Recurring") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.Switch(checked = uiState.isRecurring, onCheckedChange = { viewModel.setRecurring(it) })
                    Spacer(Modifier.width(8.dp))
                    if (uiState.isRecurring) {
                        Text("Every ${uiState.recurringIntervalDays} days")
                        Spacer(Modifier.width(8.dp))
                        IconButton(onClick = { if (uiState.recurringIntervalDays > 1) viewModel.setRecurringInterval(uiState.recurringIntervalDays - 1) }) { Icon(Icons.Default.Remove, null) }
                        IconButton(onClick = { viewModel.setRecurringInterval(uiState.recurringIntervalDays + 1) }) { Icon(Icons.Default.Add, null) }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { viewModel.saveTransaction() },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(56.dp),
                enabled = !uiState.isLoading,
                shape = RoundedCornerShape(16.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Icon(Icons.Default.Save, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (uiState.isEditMode) "Update" else "Save Transaction", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (uiState.showRefundSheet && uiState.editingTransactionId != null) {
        RefundBottomSheet(
            accounts = uiState.accounts,
            refundAmount = uiState.refundAmount,
            onAmountChange = { viewModel.setRefundAmount(it) },
            onAccountSelected = { viewModel.setRefundAccount(it.account.id) },
            onSave = { viewModel.saveRefund(uiState.editingTransactionId!!) },
            onDismiss = { viewModel.showRefundSheet(false) }
        )
    }
}

@Composable
fun TypeSelector(selected: TransactionType, onSelected: (TransactionType) -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(16.dp)) {
        Row(modifier = Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TransactionType.values().forEach { type ->
                val isSelected = selected == type
                val color = when (type) {
                    TransactionType.EXPENSE -> ExpenseColor
                    TransactionType.INCOME -> IncomeColor
                    TransactionType.TRANSFER -> TransferColor
                }
                Button(
                    onClick = { onSelected(type) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) color else Color.Transparent,
                        contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(type.name, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
    }
}

@Composable
fun AmountField(value: String, onValueChange: (String) -> Unit, transactionType: TransactionType, modifier: Modifier = Modifier) {
    val color = when (transactionType) {
        TransactionType.EXPENSE -> ExpenseColor
        TransactionType.INCOME -> IncomeColor
        TransactionType.TRANSFER -> TransferColor
    }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text("Amount (₹)") },
        leadingIcon = { Text("₹", style = MaterialTheme.typography.titleLarge, color = color, modifier = Modifier.padding(start = 8.dp)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        textStyle = MaterialTheme.typography.headlineMedium.copy(color = color, fontWeight = FontWeight.Bold),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun FormField(label: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp))
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDropdown(accounts: List<AccountWithBalance>, selected: AccountWithBalance?, onSelected: (AccountWithBalance) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected?.account?.name ?: "Select Account",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
            shape = RoundedCornerShape(12.dp)
        )
        androidx.compose.material3.ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            for (account in accounts) {
                androidx.compose.material3.DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(12.dp).clip(CircleShape).background(Color(account.account.color)))
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(account.account.name)
                                Text("₹${account.balance}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    },
                    onClick = { onSelected(account); expanded = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropdown(
    categories: List<CategoryEntity>,
    selected: CategoryEntity?,
    onSelected: (CategoryEntity) -> Unit,
    onAddCategory: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected?.name ?: "Select Category",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
            shape = RoundedCornerShape(12.dp)
        )
        androidx.compose.material3.ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            for (category in categories) {
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(category.name) },
                    onClick = { onSelected(category); expanded = false }
                )
            }
            androidx.compose.material3.DropdownMenuItem(
                text = { Text("+ Add category") },
                onClick = { expanded = false; showAddDialog = true }
            )
        }
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Category") },
            text = {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Category name") })
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    onAddCategory(name)
                    showAddDialog = false
                }) { Text("Add") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun DateTimeField(dateTime: LocalDateTime, onChanged: (LocalDateTime) -> Unit) {
    val context = LocalContext.current
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")

    OutlinedTextField(
        value = dateTime.format(formatter),
        onValueChange = {},
        readOnly = true,
        trailingIcon = {
            Row {
                Icon(Icons.Default.CalendarToday, null)
                Spacer(Modifier.width(4.dp))
                Icon(Icons.Default.Timer, null)
            }
        },
        modifier = Modifier.fillMaxWidth().clickable {
            DatePickerDialog(
                context,
                { _, year, month, day ->
                    val pickedDate = dateTime.withYear(year).withMonth(month + 1).withDayOfMonth(day)
                    TimePickerDialog(
                        context,
                        { _, hour, minute -> onChanged(pickedDate.withHour(hour).withMinute(minute)) },
                        dateTime.hour,
                        dateTime.minute,
                        false
                    ).show()
                },
                dateTime.year,
                dateTime.monthValue - 1,
                dateTime.dayOfMonth
            ).show()
        },
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun PaymentTypeSelector(selected: PaymentType, onSelected: (PaymentType) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(PaymentType.values()) { type ->
            FilterChip(selected = selected == type, onClick = { onSelected(type) }, label = { Text(type.name.replace("_", " ")) })
        }
    }
}

@Composable
fun LabelsInput(labels: List<String>, onAdd: (String) -> Unit, onRemove: (String) -> Unit) {
    var labelText by remember { mutableStateOf("") }
    Column {
        Row {
            OutlinedTextField(
                value = labelText,
                onValueChange = { labelText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Add label") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = {
                if (labelText.isNotBlank()) {
                    onAdd(labelText.trim())
                    labelText = ""
                }
            }, modifier = Modifier.align(Alignment.CenterVertically)) {
                Icon(Icons.Default.Add, "Add label")
            }
        }
        if (labels.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(labels) { label ->
                    InputChip(
                        selected = true,
                        onClick = {},
                        label = { Text(label) },
                        trailingIcon = {
                            Icon(Icons.Default.Close, "Remove", modifier = Modifier.size(16.dp).clickable { onRemove(label) })
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RefundBottomSheet(
    accounts: List<AccountWithBalance>,
    refundAmount: String,
    onAmountChange: (String) -> Unit,
    onAccountSelected: (AccountWithBalance) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = androidx.compose.material3.rememberModalBottomSheetState()
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.padding(24.dp).navigationBarsPadding()) {
            Text("Add Cashback / Refund", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                "Link a cashback or refund to this transaction. It will appear as a separate income entry in your chosen account.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = refundAmount,
                onValueChange = onAmountChange,
                label = { Text("Cashback/Refund Amount (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(12.dp))
            AccountDropdown(accounts = accounts, selected = null, onSelected = onAccountSelected)
            Spacer(Modifier.height(24.dp))
            Button(onClick = onSave, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Default.Link, null)
                Spacer(Modifier.width(8.dp))
                Text("Link Cashback/Refund")
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
