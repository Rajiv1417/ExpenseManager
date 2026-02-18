package com.expensemanager.ui.screens.addtransaction

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.expensemanager.data.local.entities.*
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

    LaunchedEffect(transactionId) {
        transactionId?.let { viewModel.loadTransaction(it) }
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onBack()
    }

    // Error snackbar
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditMode) "Edit Transaction" else "Add Transaction") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (uiState.isEditMode) {
                        IconButton(onClick = { viewModel.showRefundSheet(true) }) {
                            Icon(Icons.Default.Replay, "Add Refund/Cashback")
                        }
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
        ) {
            // ─── Transaction Type Tabs ───────────────────────────────────
            TypeSelector(
                selected = uiState.transactionType,
                onSelected = { viewModel.setTransactionType(it) },
                modifier = Modifier.padding(16.dp)
            )

            // ─── Amount Input ────────────────────────────────────────────
            AmountField(
                value = uiState.amount,
                onValueChange = { viewModel.setAmount(it) },
                transactionType = uiState.transactionType,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(16.dp))

            // ─── Account Selector ────────────────────────────────────────
            FormField(label = "Account") {
                AccountDropdown(
                    accounts = uiState.accounts,
                    selected = uiState.selectedAccount,
                    onSelected = { viewModel.setAccount(it) }
                )
            }

            // ─── To Account (Transfer only) ──────────────────────────────
            if (uiState.transactionType == TransactionType.TRANSFER) {
                FormField(label = "To Account") {
                    AccountDropdown(
                        accounts = uiState.accounts.filter { it.id != uiState.selectedAccount?.id },
                        selected = uiState.selectedToAccount,
                        onSelected = { viewModel.setToAccount(it) }
                    )
                }
            }

            // ─── Category Selector ───────────────────────────────────────
            if (uiState.transactionType != TransactionType.TRANSFER) {
                FormField(label = "Category") {
                    CategoryDropdown(
                        categories = uiState.categories,
                        selected = uiState.selectedCategory,
                        onSelected = { viewModel.setCategory(it) }
                    )
                }
            }

            // ─── Date & Time ─────────────────────────────────────────────
            FormField(label = "Date & Time") {
                DateTimeField(
                    dateTime = uiState.dateTime,
                    onChanged = { viewModel.setDateTime(it) }
                )
            }

            // ─── Payee / Payer ───────────────────────────────────────────
            FormField(label = if (uiState.transactionType == TransactionType.INCOME) "Payer" else "Payee") {
                OutlinedTextField(
                    value = uiState.payee,
                    onValueChange = { viewModel.setPayee(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Optional") },
                    singleLine = true
                )
            }

            // ─── Notes ───────────────────────────────────────────────────
            FormField(label = "Notes") {
                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = { viewModel.setNotes(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Optional") },
                    minLines = 2,
                    maxLines = 4
                )
            }

            // ─── Payment Type ─────────────────────────────────────────────
            FormField(label = "Payment Type") {
                PaymentTypeSelector(
                    selected = uiState.paymentType,
                    onSelected = { viewModel.setPaymentType(it) }
                )
            }

            // ─── Payment Status ──────────────────────────────────────────
            FormField(label = "Status") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PaymentStatus.values().forEach { s ->
                        FilterChip(
                            selected = uiState.status == s,
                            onClick = { viewModel.setStatus(s) },
                            label = { Text(s.name) }
                        )
                    }
                }
            }

            // ─── Labels ──────────────────────────────────────────────────
            FormField(label = "Labels") {
                LabelsInput(
                    labels = uiState.labels,
                    onAdd = { viewModel.addLabel(it) },
                    onRemove = { viewModel.removeLabel(it) }
                )
            }

            // ─── Recurring ───────────────────────────────────────────────
            FormField(label = "Recurring") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = uiState.isRecurring,
                        onCheckedChange = { viewModel.setRecurring(it) }
                    )
                    Spacer(Modifier.width(8.dp))
                    if (uiState.isRecurring) {
                        Text("Every ${uiState.recurringIntervalDays} days")
                        Spacer(Modifier.width(8.dp))
                        IconButton(onClick = {
                            if (uiState.recurringIntervalDays > 1)
                                viewModel.setRecurringInterval(uiState.recurringIntervalDays - 1)
                        }) { Icon(Icons.Default.Remove, null) }
                        IconButton(onClick = {
                            viewModel.setRecurringInterval(uiState.recurringIntervalDays + 1)
                        }) { Icon(Icons.Default.Add, null) }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ─── Save Button ─────────────────────────────────────────────
            Button(
                onClick = { viewModel.saveTransaction() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(56.dp),
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

    // ─── Refund Bottom Sheet ──────────────────────────────────────────────────
    if (uiState.showRefundSheet && uiState.editingTransactionId != null) {
        RefundBottomSheet(
            accounts = uiState.accounts,
            refundAmount = uiState.refundAmount,
            onAmountChange = { viewModel.setRefundAmount(it) },
            onAccountSelected = { viewModel.setRefundAccount(it.id) },
            onSave = { viewModel.saveRefund(uiState.editingTransactionId!!) },
            onDismiss = { viewModel.showRefundSheet(false) }
        )
    }
}

@Composable
fun TypeSelector(
    selected: TransactionType,
    onSelected: (TransactionType) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
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
                    elevation = ButtonDefaults.buttonElevation(if (isSelected) 4.dp else 0.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(type.name, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
    }
}

@Composable
fun AmountField(
    value: String,
    onValueChange: (String) -> Unit,
    transactionType: TransactionType,
    modifier: Modifier = Modifier
) {
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
        leadingIcon = {
            Text("₹", style = MaterialTheme.typography.titleLarge, color = color, modifier = Modifier.padding(start = 8.dp))
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        textStyle = MaterialTheme.typography.headlineMedium.copy(color = color, fontWeight = FontWeight.Bold),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = color,
            focusedLabelColor = color
        ),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun FormField(label: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDropdown(
    accounts: List<com.expensemanager.data.local.entities.AccountEntity>,
    selected: com.expensemanager.data.local.entities.AccountEntity?,
    onSelected: (com.expensemanager.data.local.entities.AccountEntity) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected?.name ?: "Select Account",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            accounts.forEach { account ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(12.dp).clip(CircleShape)
                                    .background(Color(account.color))
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(account.name)
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
    categories: List<com.expensemanager.data.local.entities.CategoryEntity>,
    selected: com.expensemanager.data.local.entities.CategoryEntity?,
    onSelected: (com.expensemanager.data.local.entities.CategoryEntity) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected?.name ?: "Select Category",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.name) },
                    onClick = { onSelected(category); expanded = false }
                )
            }
        }
    }
}

@Composable
fun DateTimeField(
    dateTime: LocalDateTime,
    onChanged: (LocalDateTime) -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")
    OutlinedTextField(
        value = dateTime.format(formatter),
        onValueChange = {},
        readOnly = true,
        trailingIcon = { Icon(Icons.Default.CalendarToday, null) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun PaymentTypeSelector(
    selected: PaymentType,
    onSelected: (PaymentType) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(PaymentType.values()) { type ->
            FilterChip(
                selected = selected == type,
                onClick = { onSelected(type) },
                label = { Text(type.name.replace("_", " ")) }
            )
        }
    }
}

@Composable
fun LabelsInput(
    labels: List<String>,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit
) {
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
            IconButton(
                onClick = {
                    if (labelText.isNotBlank()) {
                        onAdd(labelText.trim())
                        labelText = ""
                    }
                },
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
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
                            Icon(
                                Icons.Default.Close,
                                "Remove",
                                modifier = Modifier.size(16.dp).clickable { onRemove(label) }
                            )
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefundBottomSheet(
    accounts: List<com.expensemanager.data.local.entities.AccountEntity>,
    refundAmount: String,
    onAmountChange: (String) -> Unit,
    onAccountSelected: (com.expensemanager.data.local.entities.AccountEntity) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.padding(24.dp).navigationBarsPadding()) {
            Text(
                "Add Cashback / Refund",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
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
            AccountDropdown(
                accounts = accounts,
                selected = null,
                onSelected = onAccountSelected
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Link, null)
                Spacer(Modifier.width(8.dp))
                Text("Link Cashback/Refund")
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
