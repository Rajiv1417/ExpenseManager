package com.expensemanager.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AccountPreviewCard(
    name: String,
    balance: String,
    currency: String,
    color: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                name.ifBlank { "Account Name" },
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )

            Text(
                "$currency ${balance.ifBlank { "0.00" }}",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
        }
    }
}
