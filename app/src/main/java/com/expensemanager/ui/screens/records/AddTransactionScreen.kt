package com.expensemanager.ui.screens.addtransaction

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Backspace
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
import com.expensemanager.data.local.entities.TransactionType
import com.expensemanager.utils.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    transactionId: Long? = null,
    onBack: () -> Unit,
    onSave: (amount: Double, type: TransactionType, category: String) -> Unit = { _, _, _ -> }
) {

    var amountText by remember { mutableStateOf("0") }
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var selectedCategory by remember { mutableStateOf("Groceries") }

    val categories = listOf(
        "Groceries", "Food", "Transport", "Home",
        "Fun", "Shopping", "Health", "More"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Record") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        },
        bottomBar = {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                IconButton(
                    onClick = { /* date picker later */ },
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(16.dp)
                        )
                ) {
                    Icon(Icons.Default.CalendarToday, null)
                }

                IconButton(
                    onClick = { /* notes later */ },
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(16.dp)
                        )
                ) {
                    Icon(Icons.Default.Edit, null)
                }

                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull() ?: 0.0
                        onSave(amount, selectedType, selectedCategory)
                        onBack()
                    },
                    modifier = Modifier
                        .height(56.dp)
                        .weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Save Record", fontSize = 18.sp)
                }
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // Transaction Type Tabs
            TabRow(selectedTabIndex = selectedType.ordinal) {

                TransactionType.values().forEachIndexed { index, type ->

                    Tab(
                        selected = selectedType.ordinal == index,
                        onClick = { selectedType = type },
                        text = { Text(type.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Amount Display
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    "Enter Amount",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    CurrencyFormatter.format(
                        amountText.toDoubleOrNull() ?: 0.0
                    ),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category Grid
            Text(
                "Category",
                modifier = Modifier.padding(start = 16.dp),
                fontWeight = FontWeight.SemiBold
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .height(160.dp)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                items(categories) { category ->

                    CategoryItem(
                        name = category,
                        selected = selectedCategory == category
                    ) {
                        selectedCategory = category
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Numeric Keypad
            NumericKeypad(
                onNumberClick = {

                    amountText =
                        if (amountText == "0") it
                        else amountText + it
                },
                onDeleteClick = {

                    amountText =
                        amountText.dropLast(1).ifEmpty { "0" }
                },
                onDotClick = {

                    if (!amountText.contains("."))
                        amountText += "."
                }
            )
        }
    }
}

@Composable
fun CategoryItem(
    name: String,
    selected: Boolean,
    onClick: () -> Unit
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
    ) {

        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (selected)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {

            Icon(
                Icons.Default.Category,
                null,
                tint =
                if (selected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            name,
            fontSize = 12.sp
        )
    }
}

@Composable
fun NumericKeypad(
    onNumberClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onDotClick: () -> Unit
) {

    val buttons = listOf(
        "1","2","3",
        "4","5","6",
        "7","8","9",
        ".","0","⌫"
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.height(240.dp),
        userScrollEnabled = false
    ) {

        items(buttons) { label ->

            Box(
                modifier = Modifier
                    .padding(12.dp)
                    .size(64.dp)
                    .clip(CircleShape)
                    .clickable {

                        when(label){

                            "⌫" -> onDeleteClick()
                            "." -> onDotClick()
                            else -> onNumberClick(label)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {

                Text(
                    label,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
