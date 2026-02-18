package com.expensemanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expensemanager.data.local.entities.TransactionEntity
import com.expensemanager.data.local.entities.TransactionType
import com.expensemanager.ui.theme.ExpenseColor
import com.expensemanager.ui.theme.IncomeColor
import com.expensemanager.ui.theme.TransferColor
import com.expensemanager.utils.CurrencyFormatter
import java.time.format.DateTimeFormatter

@Composable
fun TransactionItem(
    transaction: TransactionEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    linkedRefundAmount: Double? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(transaction.typeColor().copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = transaction.typeIcon(),
                        contentDescription = null,
                        tint = transaction.typeColor(),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                // Info
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = transaction.payee ?: transaction.notes ?: transaction.type.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (transaction.isAutoDetected) {
                            Spacer(Modifier.width(4.dp))
                            SmsTag()
                        }
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        transaction.dateTime.format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (transaction.labels.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            transaction.labels.take(3).forEach { label ->
                                LabelChip(label)
                            }
                        }
                    }
                }

                // Amount
                Column(horizontalAlignment = Alignment.End) {
                    val sign = when (transaction.type) {
                        TransactionType.EXPENSE -> "-"
                        TransactionType.INCOME -> "+"
                        TransactionType.TRANSFER -> "→"
                    }
                    // If there's a linked refund, show original with strikethrough
                    if (transaction.refundAmount != null) {
                        Text(
                            text = "$sign${CurrencyFormatter.format(transaction.amount)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textDecoration = TextDecoration.LineThrough,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "$sign${CurrencyFormatter.format(transaction.amount - (transaction.refundAmount ?: 0.0))}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = transaction.typeColor()
                        )
                    } else {
                        Text(
                            text = "$sign${CurrencyFormatter.format(transaction.amount)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = transaction.typeColor()
                        )
                    }
                    if (transaction.refundAmount != null) {
                        Text(
                            text = "Refund ₹${CurrencyFormatter.format(transaction.refundAmount!!)}",
                            fontSize = 11.sp,
                            color = IncomeColor
                        )
                    }
                    Text(
                        text = transaction.status.name,
                        fontSize = 11.sp,
                        color = if (transaction.status.name == "PENDING")
                            MaterialTheme.colorScheme.tertiary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Refund/Cashback linked badge — like the screenshot
            if (transaction.linkedRefundTransactionId != null && transaction.refundAmount != null) {
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(IncomeColor.copy(alpha = 0.08f))
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Replay,
                        null,
                        tint = IncomeColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (transaction.isPartialRefund) "Partial refund" else "Full refund",
                        style = MaterialTheme.typography.labelSmall,
                        color = IncomeColor
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        "Cashback ₹${CurrencyFormatter.format(transaction.refundAmount!!)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = IncomeColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun SmsTag() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text("SMS", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
    }
}

@Composable
private fun LabelChip(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

private fun TransactionEntity.typeColor(): Color = when (type) {
    TransactionType.EXPENSE -> ExpenseColor
    TransactionType.INCOME -> IncomeColor
    TransactionType.TRANSFER -> TransferColor
}

private fun TransactionEntity.typeIcon() = when (type) {
    TransactionType.EXPENSE -> Icons.Default.ArrowUpward
    TransactionType.INCOME -> Icons.Default.ArrowDownward
    TransactionType.TRANSFER -> Icons.Default.CompareArrows
}
