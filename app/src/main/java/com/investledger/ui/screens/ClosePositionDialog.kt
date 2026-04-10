package com.investledger.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.investledger.data.Position
import com.investledger.ui.theme.*

/**
 * 清仓对话框
 */
@Composable
fun ClosePositionDialog(
    position: Position,
    onDismiss: () -> Unit,
    onConfirm: (sellPrice: Double) -> Unit
) {
    var sellPrice by remember { mutableStateOf("") }
    
    val profit = sellPrice.toDoubleOrNull()?.let { sp ->
        (sp - position.costPrice) * position.quantity
    } ?: 0.0
    
    val profitRate = sellPrice.toDoubleOrNull()?.let { sp ->
        ((sp - position.costPrice) / position.costPrice) * 100
    } ?: 0.0
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                "清仓",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 持仓信息
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            position.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "${position.type} | 成本价 ${position.formatPrice()} | 数量 ${position.formatQuantity()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = GrayText
                        )
                    }
                }
                
                // 卖出价输入
                OutlinedTextField(
                    value = sellPrice,
                    onValueChange = { 
                        if (it.isEmpty() || it.matches(Regex("\\d+\\.?\\d*"))) {
                            sellPrice = it
                        }
                    },
                    label = { Text("卖出价") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenPrimary,
                        unfocusedBorderColor = GrayBorder
                    )
                )
                
                // 收益预览
                if (sellPrice.isNotBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (profit >= 0) GreenLight else RedLight
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                "预估收益",
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
                    val sp = sellPrice.toDoubleOrNull() ?: 0.0
                    if (sp > 0) {
                        onConfirm(sp)
                    }
                },
                enabled = sellPrice.isNotBlank()
            ) {
                Text("确定清仓", color = GreenPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}