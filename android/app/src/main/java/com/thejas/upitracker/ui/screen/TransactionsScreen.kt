package com.thejas.upitracker.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thejas.upitracker.data.model.Category
import com.thejas.upitracker.data.model.Transaction
import com.thejas.upitracker.ui.viewmodel.MainViewModel
import java.text.NumberFormat
import java.util.*

private fun fmtT(n: Double) = "₹" + NumberFormat.getNumberInstance(Locale("en","IN")).apply { maximumFractionDigits = 0 }.format(n)

@Composable
fun TransactionsScreen(vm: MainViewModel) {
    val transactions by vm.transactions.collectAsState()
    val categories   by vm.categories.collectAsState()
    var selectedCat  by remember { mutableStateOf<Long?>(null) }

    val filtered = if (selectedCat == null) transactions
                   else transactions.filter { it.categoryId == selectedCat }

    val totalDebit  = filtered.filter { it.type == "debit" }.sumOf { it.amount }
    val totalCredit = filtered.filter { it.type == "credit" }.sumOf { it.amount }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Summary row
        if (filtered.isNotEmpty()) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SummaryChip("Debited", fmtT(totalDebit), Color(0xFFF87171), Color(0xFF1A0505), Modifier.weight(1f))
                    SummaryChip("Credited", fmtT(totalCredit), Color(0xFF4ADE80), Color(0xFF051A05), Modifier.weight(1f))
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(vertical = 64.dp), contentAlignment = Alignment.Center) {
                    Text("No transactions", color = Color.Gray)
                }
            }
        }

        items(filtered, key = { it.id }) { tx ->
            val icon = categories.find { it.id == tx.categoryId }?.icon ?: "💳"
            TxCard(
                tx = tx,
                icon = icon,
                categories = categories,
                onDelete = { vm.deleteTransaction(tx) },
                onCategoryChange = { catId -> vm.updateTransaction(tx.copy(categoryId = catId)) }
            )
        }
    }
}

@Composable
private fun SummaryChip(label: String, value: String, textColor: Color, bgColor: Color, modifier: Modifier) {
    Card(colors = CardDefaults.cardColors(containerColor = bgColor), shape = RoundedCornerShape(12.dp), modifier = modifier) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, color = textColor, fontSize = 12.sp)
            Text(value, color = textColor.copy(alpha = 0.85f), fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
private fun TxCard(
    tx: Transaction,
    icon: String,
    categories: List<Category>,
    onDelete: () -> Unit,
    onCategoryChange: (Long?) -> Unit
) {
    var catExpanded by remember { mutableStateOf(false) }
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(icon, fontSize = 24.sp)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(tx.description ?: tx.vpa ?: "UPI Transfer", color = Color.White, fontSize = 14.sp, maxLines = 1)
                    Text("${tx.date} · ${tx.bank ?: tx.source}", color = Color.Gray, fontSize = 11.sp)
                    if (tx.refNo != null) Text("Ref: ${tx.refNo}", color = Color(0xFF6B7280), fontSize = 10.sp)
                }
                Text(
                    "${if (tx.type == "debit") "-" else "+"}${fmtT(tx.amount)}",
                    color = if (tx.type == "debit") Color(0xFFF87171) else Color(0xFF4ADE80),
                    fontWeight = FontWeight.SemiBold, fontSize = 14.sp
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { catExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        val cat = categories.find { it.id == tx.categoryId }
                        Text("${cat?.icon ?: "🔍"} ${cat?.name ?: "Uncategorized"}", fontSize = 11.sp, color = Color.White)
                    }
                    DropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                        DropdownMenuItem(text = { Text("Uncategorized") }, onClick = { onCategoryChange(null); catExpanded = false })
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text("${cat.icon} ${cat.name}") },
                                onClick = { onCategoryChange(cat.id); catExpanded = false }
                            )
                        }
                    }
                }
                TextButton(onClick = onDelete, colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)) {
                    Text("🗑️", fontSize = 16.sp)
                }
            }
        }
    }
}
