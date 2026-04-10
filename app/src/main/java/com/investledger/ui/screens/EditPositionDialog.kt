package com.investledger.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.investledger.data.Position
import com.investledger.ui.components.DateTimePicker
import com.investledger.ui.theme.*

/**
 * 编辑持仓对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPositionDialog(
    position: Position,
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: String, costPrice: Double, quantity: Double, note: String, createdAt: Long) -> Unit
) {
    var name by remember { mutableStateOf(position.name) }
    var type by remember { mutableStateOf(position.type) }
    var note by remember { mutableStateOf(position.note) }
    var costPrice by remember { mutableStateOf(position.costPrice.toString()) }
    var quantity by remember { mutableStateOf(position.quantity.toString()) }
    var expanded by remember { mutableStateOf(false) }
    
    // 日期时间
    var createdAt by remember { mutableStateOf(position.createdAt) }
    
    val types = listOf("股票", "基金", "加密货币", "债券", "其他")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                "编辑持仓",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
                
                // 日期时间选择
                DateTimePicker(
                    timestamp = createdAt,
                    onTimestampChange = { createdAt = it },
                    label = "购买日期"
                )
                
                // 成本价输入
                OutlinedTextField(
                    value = costPrice,
                    onValueChange = { 
                        if (it.isEmpty() || it.matches(Regex("\\d*\\.?\\d*"))) {
                            costPrice = it
                        }
                    },
                    label = { Text("成本价") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenPrimary,
                        unfocusedBorderColor = GrayBorder
                    )
                )
                
                // 数量输入
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { 
                        if (it.isEmpty() || it.matches(Regex("\\d*\\.?\\d*"))) {
                            quantity = it
                        }
                    },
                    label = { Text("数量") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
                    val parsedCostPrice = costPrice.toDoubleOrNull() ?: 0.0
                    val parsedQuantity = quantity.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && parsedCostPrice > 0 && parsedQuantity > 0) {
                        onConfirm(name, type, parsedCostPrice, parsedQuantity, note, createdAt)
                    }
                },
                enabled = name.isNotBlank() && costPrice.isNotBlank() && costPrice.toDoubleOrNull() != null && 
                         costPrice.toDoubleOrNull()!! > 0 && quantity.isNotBlank() && quantity.toDoubleOrNull() != null && 
                         quantity.toDoubleOrNull()!! > 0
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