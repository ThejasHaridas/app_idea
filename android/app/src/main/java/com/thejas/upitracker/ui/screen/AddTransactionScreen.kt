package com.thejas.upitracker.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thejas.upitracker.data.model.Transaction
import com.thejas.upitracker.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AddTransactionScreen(vm: MainViewModel, onAdded: () -> Unit) {
    val categories by vm.categories.collectAsState()

    var amount      by remember { mutableStateOf("") }
    var type        by remember { mutableStateOf("debit") }
    var description by remember { mutableStateOf("") }
    var vpa         by remember { mutableStateOf("") }
    var date        by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())) }
    var categoryId  by remember { mutableStateOf<Long?>(null) }
    var catExpanded by remember { mutableStateOf(false) }
    var error       by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Add Transaction", color = Color.White, style = MaterialTheme.typography.titleLarge)

        // Debit / Credit toggle
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("debit" to "↑ Debit (Sent)", "credit" to "↓ Credit (Received)").forEach { (t, label) ->
                Button(
                    onClick = { type = t },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when { type == t && t == "debit" -> Color(0xFF991B1B); type == t -> Color(0xFF166534); else -> MaterialTheme.colorScheme.surfaceVariant },
                        contentColor = if (type == t) Color.White else Color.Gray
                    )
                ) { Text(label, fontSize = 12.sp) }
            }
        }

        OutlinedTextField(
            value = amount, onValueChange = { amount = it },
            label = { Text("Amount (₹)") }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = date, onValueChange = { date = it },
            label = { Text("Date (yyyy-MM-dd)") }, singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = description, onValueChange = { description = it },
            label = { Text("Description") }, singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = vpa, onValueChange = { vpa = it },
            label = { Text("UPI VPA / Merchant") }, singleLine = true,
            placeholder = { Text("merchant@upi") },
            modifier = Modifier.fillMaxWidth()
        )

        // Category picker
        Box(Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = { catExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                val cat = categories.find { it.id == categoryId }
                Text("${cat?.icon ?: "🔍"} ${cat?.name ?: "Auto-detect category"}", color = Color.White)
            }
            DropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                DropdownMenuItem(text = { Text("Auto-detect") }, onClick = { categoryId = null; catExpanded = false })
                categories.forEach { cat ->
                    DropdownMenuItem(
                        text = { Text("${cat.icon} ${cat.name}") },
                        onClick = { categoryId = cat.id; catExpanded = false }
                    )
                }
            }
        }

        if (error.isNotEmpty()) Text(error, color = Color(0xFFF87171), fontSize = 13.sp)

        Button(
            onClick = {
                val amt = amount.toDoubleOrNull()
                if (amt == null || amt <= 0) { error = "Enter a valid amount"; return@Button }
                if (date.isBlank()) { error = "Date is required"; return@Button }
                error = ""
                vm.addTransaction(Transaction(
                    amount = amt, type = type,
                    description = description.ifBlank { null },
                    vpa = vpa.ifBlank { null },
                    date = date, categoryId = categoryId
                ))
                onAdded()
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
        ) { Text("Add Transaction", fontSize = 16.sp) }
    }
}
