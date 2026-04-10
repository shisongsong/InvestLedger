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
import com.investledger.data.Transaction
import com.investledger.ui.components.DateTimePicker
import com.investledger.ui.theme.*

/**
 * 编辑交易记录对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionDialog(
    transaction: Transaction,
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: String, costPrice: Double, sellPrice: Double, quantity: Double, createdAt: Long) -> Unit
) {
    var name by remember { mutableStateOf(transaction.name) }
    var type by remember { mutableStateOf(transaction.type) }
    var costPrice by remember { mutableStateOf(transaction.costPrice.toString()) }
    var sellPrice by remember { mutableStateOf(transaction.sellPrice.toString()) }
    var quantity by remember { mutableStateOf(transaction.quantity.toString()) }
    var expanded by remember { mutableStateOf(false) }
    
    // 日期时间
    var createdAt by remember { mutableStateOf(transaction.createdAt) }
    
    val types = listOf("股票", "基金", "加密货币", "债券", "其他")
    
    // 更新收益计算
    val profit = sellPrice.toDoubleOrNull()?.let { sp ->
        val cp = costPrice.toDoubleOrNull() ?: 0.0
        val qty = quantity.toDoubleOrNull() ?: 0.0
        (sp - cp) * qty
    } ?: 0.0
    
    val profitRate = sellPrice.toDoubleOrNull()?.let { sp ->
        val cp = costPrice.toDoubleOrNull() ?: 0.0
        if (cp != 0.0) {
            ((sp - cp) / cp) * 100
        } else 0.0
    } ?: 0.0
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                "编辑交易记录",
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
                    label = "卖出日期"
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
                
                // 卖出价输入
                OutlinedTextField(
                    value = sellPrice,
                    onValueChange = { 
                        if (it.isEmpty() || it.matches(Regex("\\d*\\.?\\d*"))) {
                            sellPrice = it
                        }
                    },
                    label = { Text("卖出价") },
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
                
                // 显示计算的收益
                if (costPrice.isNotEmpty() && sellPrice.isNotEmpty() && quantity.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (profit >= 0) GreenLight else RedLight
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Text(
                                "预览收益",
                                style = MaterialTheme.typography.labelSmall,
                                color = GrayText
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        if (profit >= 0) 
                                            String.format("+%.2f", profit)
                                        else 
                                            String.format("%.2f", profit),
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (profit >= 0) GreenPrimary else RedLoss
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        if (profitRate >= 0)
                                            String.format("+%.2f%%", profitRate)
                                        else
                                            String.format("%.2f%%", profitRate),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = if (profit >= 0) GreenPrimary else RedLoss
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val parsedCostPrice = costPrice.toDoubleOrNull() ?: 0.0
                    val parsedSellPrice = sellPrice.toDoubleOrNull() ?: 0.0
                    val parsedQuantity = quantity.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && parsedCostPrice > 0 && parsedSellPrice > 0 && parsedQuantity > 0) {
                        onConfirm(name, type, parsedCostPrice, parsedSellPrice, parsedQuantity, createdAt)
                    }
                },
                enabled = name.isNotBlank() && costPrice.isNotBlank() && costPrice.toDoubleOrNull() != null && 
                         costPrice.toDoubleOrNull()!! > 0 && sellPrice.isNotBlank() && sellPrice.toDoubleOrNull() != null && 
                         sellPrice.toDoubleOrNull()!! > 0 && quantity.isNotBlank() && quantity.toDoubleOrNull() != null && 
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