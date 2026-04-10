package com.investledger.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.investledger.ui.theme.*

/**
 * 建仓对话框
 */
@Composable
fun OpenPositionDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: String, costPrice: Double, quantity: Double, note: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("股票") }
    var costPrice by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    
    val types = listOf("股票", "基金", "加密货币", "债券", "其他")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                "建仓",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 名称输入
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("投资名称/代码") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenPrimary,
                        unfocusedBorderColor = GrayBorder
                    )
                )
                
                // 类型选择
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = type,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("投资类型") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenPrimary,
                            unfocusedBorderColor = GrayBorder
                        )
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        types.forEach { selection ->
                            DropdownMenuItem(
                                text = { Text(selection) },
                                onClick = {
                                    type = selection
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                // 成本价输入
                OutlinedTextField(
                    value = costPrice,
                    onValueChange = { 
                        if (it.isEmpty() || it.matches(Regex("\\d+\\.?\\d*"))) {
                            costPrice = it
                        }
                    },
                    label = { Text("成本价") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenPrimary,
                        unfocusedBorderColor = GrayBorder
                    )
                )
                
                // 数量输入
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { 
                        if (it.isEmpty() || it.matches(Regex("\\d+\\.?\\d*"))) {
                            quantity = it
                        }
                    },
                    label = { Text("数量") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenPrimary,
                        unfocusedBorderColor = GrayBorder
                    )
                )
                
                // 备注（可选）
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("备注（可选）") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenPrimary,
                        unfocusedBorderColor = GrayBorder
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val cost = costPrice.toDoubleOrNull() ?: 0.0
                    val qty = quantity.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && cost > 0 && qty > 0) {
                        onConfirm(name, type, cost, qty, note)
                    }
                },
                enabled = name.isNotBlank() && costPrice.isNotBlank() && quantity.isNotBlank()
            ) {
                Text("确定", color = GreenPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}