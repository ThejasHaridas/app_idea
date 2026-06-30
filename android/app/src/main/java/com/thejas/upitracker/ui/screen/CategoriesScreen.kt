package com.thejas.upitracker.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thejas.upitracker.data.model.Category
import com.thejas.upitracker.ui.viewmodel.MainViewModel
import org.json.JSONArray
import java.text.NumberFormat
import java.util.*

private fun fmtC(n: Double) = "₹" + NumberFormat.getNumberInstance(Locale("en","IN")).apply { maximumFractionDigits = 0 }.format(n)

@Composable
fun CategoriesScreen(vm: MainViewModel) {
    val categories by vm.categories.collectAsState()
    val limits     by vm.limits.collectAsState()
    val limMap     = limits.associate { it.categoryId to it.monthlyLimit }
    val drafts     = remember { mutableStateMapOf<Long, String>() }

    var showAdd    by remember { mutableStateOf(false) }
    var newName    by remember { mutableStateOf("") }
    var newIcon    by remember { mutableStateOf("💳") }
    var newKw      by remember { mutableStateOf("") }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Categories & Limits", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                TextButton(onClick = { showAdd = !showAdd },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF818CF8))) { Text("+ New") }
            }
        }

        if (showAdd) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("New Category", color = Color.White, fontWeight = FontWeight.Medium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = newIcon, onValueChange = { newIcon = it },
                                modifier = Modifier.width(64.dp), singleLine = true, label = { Text("Icon") })
                            OutlinedTextField(value = newName, onValueChange = { newName = it },
                                modifier = Modifier.weight(1f), singleLine = true, label = { Text("Name") })
                        }
                        OutlinedTextField(value = newKw, onValueChange = { newKw = it },
                            modifier = Modifier.fillMaxWidth(), singleLine = true,
                            label = { Text("Keywords (comma-separated)") })
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = {
                                if (newName.isNotBlank()) {
                                    val kws = newKw.split(",").map { it.trim() }.filter { it.isNotBlank() }
                                    val kwJson = "[${kws.joinToString(",") { "\"$it\"" }}]"
                                    vm.addCategory(Category(name = newName, icon = newIcon, keywords = kwJson))
                                    newName = ""; newIcon = "💳"; newKw = ""; showAdd = false
                                }
                            }, modifier = Modifier.weight(1f)) { Text("Add") }
                            OutlinedButton(onClick = { showAdd = false }, modifier = Modifier.weight(1f)) { Text("Cancel") }
                        }
                    }
                }
            }
        }

        items(categories, key = { it.id }) { cat ->
            val lim   = limMap[cat.id]
            val draft = drafts[cat.id]
            val displayVal = draft ?: (if (lim != null) lim.toLong().toString() else "")

            val kws = try {
                val arr = JSONArray(cat.keywords)
                (0 until arr.length()).map { arr.getString(it) }.take(3).joinToString(", ")
            } catch (_: Exception) { "" }

            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(14.dp)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(cat.icon, fontSize = 26.sp)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(cat.name, color = Color.White, fontWeight = FontWeight.Medium)
                            if (kws.isNotEmpty()) Text(kws, color = Color.Gray, fontSize = 11.sp)
                        }
                        IconButton(onClick = { vm.deleteCategory(cat) }) { Text("🗑️", fontSize = 18.sp) }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Monthly ₹", color = Color.Gray, fontSize = 12.sp)
                        OutlinedTextField(
                            value = displayVal,
                            onValueChange = { drafts[cat.id] = it },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            placeholder = { Text("No limit", color = Color.Gray) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        if (draft != null) {
                            TextButton(onClick = {
                                val v = draft.toDoubleOrNull()
                                if (v != null && v > 0) vm.setLimit(cat.id, v)
                                else if (draft.isEmpty()) vm.removeLimit(cat.id)
                                drafts.remove(cat.id)
                            }) { Text("Save", color = Color(0xFF818CF8)) }
                        }
                        if (lim != null && draft == null) {
                            TextButton(onClick = { vm.removeLimit(cat.id) }) {
                                Text("Remove", color = Color.Gray, fontSize = 11.sp)
                            }
                        }
                    }
                    if (lim != null) Text("Limit: ${fmtC(lim)}/month", color = Color.Gray, fontSize = 11.sp)
                }
            }
        }
    }
}
