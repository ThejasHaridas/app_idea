package com.thejas.upitracker.ui.screen

import android.graphics.Color.parseColor
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thejas.upitracker.ui.viewmodel.CategorySummary
import com.thejas.upitracker.ui.viewmodel.MainViewModel
import java.text.NumberFormat
import java.util.*

private val MONTH_NAMES = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
private fun fmt(n: Double) = "₹" + NumberFormat.getNumberInstance(Locale("en","IN")).apply { maximumFractionDigits = 0 }.format(n)

@Composable
fun DashboardScreen(vm: MainViewModel) {
    val summaries    by vm.categorySummaries.collectAsState()
    val transactions by vm.transactions.collectAsState()
    val month        by vm.month.collectAsState()
    val year         by vm.year.collectAsState()

    val totalSpent = summaries.sumOf { it.spent }
    val overLimit  = summaries.filter { it.limit != null && it.spent > it.limit!! }
    val recent     = transactions.take(5)
    val withSpend  = summaries.filter { it.spent > 0 }

    var monthExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Month picker
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { monthExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(MONTH_NAMES.getOrNull((month.toIntOrNull() ?: 1) - 1) ?: month, color = Color.White)
                    }
                    DropdownMenu(expanded = monthExpanded, onDismissRequest = { monthExpanded = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
                        MONTH_NAMES.forEachIndexed { i, m ->
                            DropdownMenuItem(text = { Text(m) }, onClick = {
                                vm.setMonthYear("${i + 1}", year)
                                monthExpanded = false
                            })
                        }
                    }
                }
                Text(year, color = Color.Gray, fontSize = 14.sp, modifier = Modifier.align(Alignment.CenterVertically))
            }
        }

        // Hero card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(Color(0xFF4F46E5), Color(0xFF7C3AED))), RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Text("Total Spent This Month", color = Color(0xFFBFDBFE), fontSize = 13.sp)
                    Text(fmt(totalSpent), color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                    if (overLimit.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Text("⚠️ ${overLimit.size} category over limit", color = Color(0xFFFDE68A), fontSize = 12.sp)
                    }
                }
            }
        }

        // Over-limit alerts
        items(overLimit) { s ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF450A0A)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(s.category.icon, fontSize = 26.sp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("${s.category.name} over limit!", color = Color(0xFFFCA5A5), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("${fmt(s.spent)} spent — limit ${fmt(s.limit!!)}", color = Color(0xFFF87171), fontSize = 12.sp)
                    }
                }
            }
        }

        // Category breakdown
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Category Breakdown", color = Color.Gray, fontSize = 13.sp)
                    if (withSpend.isEmpty()) {
                        Text("No transactions this month", color = Color.Gray, fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 12.dp))
                    } else {
                        withSpend.forEach { s -> CategoryBreakdownRow(s) }
                    }
                }
            }
        }

        // Recent transactions
        if (recent.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Recent Transactions", color = Color.Gray, fontSize = 13.sp)
                        recent.forEach { tx ->
                            val icon = summaries.find { it.category.id == tx.categoryId }?.category?.icon ?: "💳"
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(icon, fontSize = 22.sp)
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(tx.description ?: tx.vpa ?: "UPI Transfer", color = Color.White, fontSize = 13.sp, maxLines = 1)
                                    Text(tx.date, color = Color.Gray, fontSize = 11.sp)
                                }
                                Text(
                                    "${if (tx.type == "debit") "-" else "+"}${fmt(tx.amount)}",
                                    color = if (tx.type == "debit") Color(0xFFF87171) else Color(0xFF4ADE80),
                                    fontWeight = FontWeight.SemiBold, fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryBreakdownRow(s: CategorySummary) {
    val catColor = try { Color(parseColor(s.category.colorHex)) } catch (_: Exception) { Color.Gray }
    val progress = if (s.limit != null && s.limit > 0) (s.spent / s.limit).coerceIn(0.0, 1.0).toFloat() else 0f
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(s.category.icon, fontSize = 18.sp)
            Spacer(Modifier.width(8.dp))
            Text(s.category.name, color = Color.White, fontSize = 13.sp, modifier = Modifier.weight(1f))
            Text(fmt(s.spent), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            if (s.limit != null) Text(" / ${fmt(s.limit)}", color = Color.Gray, fontSize = 11.sp)
        }
        if (s.limit != null) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = if (s.spent > s.limit) Color(0xFFEF4444) else catColor,
                trackColor = Color(0xFF1F2937)
            )
        }
    }
}
