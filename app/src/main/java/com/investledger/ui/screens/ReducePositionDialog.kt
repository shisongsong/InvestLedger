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
import com.investledger.ui.theme.*

/**
 * 减仓对话框 - 支持两种卖出计算方式
 * 1. 按卖出价+卖出数量
 * 2. 按卖出金额+卖出数量（自动计算卖出价）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReducePositionDialog(
    position: Position,
    onDismiss: () -> Unit,
    onConfirm: (sellPrice: Double, sellQuantity: Double) -> Unit
) {
    // 输入模式：0=卖出价+数量, 1=卖出金额+数量
    var inputMode by remember { mutableStateOf(0) }
    
    var sellPrice by remember { mutableStateOf("") }
    var sellQuantity by remember { mutableStateOf("") }
    var sellAmount by remember { mutableStateOf("") }
    
    // 计算最终卖出价
    val finalSellPrice = when (inputMode) {
        0 -> sellPrice.toDoubleOrNull() ?: 0.0
        1 -> {
            val amount = sellAmount.toDoubleOrNull() ?: 0.0
            val qty = sellQuantity.toDoubleOrNull() ?: 0.0
            if (qty > 0) amount / qty else 0.0
        }
        else -> 0.0
    }
    
    // 计算最终卖出数量
    val finalSellQuantity = sellQuantity.toDoubleOrNull() ?: 0.0
    
    // 计算收益
    val profit = if (finalSellPrice > 0 && finalSellQuantity > 0) {
        (finalSellPrice - position.costPrice) * finalSellQuantity
    } else 0.0
    
    val profitRate = if (finalSellPrice > 0 && position.costPrice != 0.0) {
        ((finalSellPrice - position.costPrice) / position.costPrice) * 100
    } else 0.0
    
    // 计算卖出总金额
    val totalSellAmount = if (finalSellPrice > 0 && finalSellQuantity > 0) {
        finalSellPrice * finalSellQuantity
    } else 0.0
    
    // 检查是否超过持仓数量
    val isOverQuantity = finalSellQuantity > position.quantity
    val remainingQuantity = position.quantity - finalSellQuantity
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                "减仓",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
                            "${position.type} | 成本价 ${position.formatPrice()} | 持仓 ${position.formatQuantity()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = GrayText
                        )
                    }
                }
                
                // 输入模式选择
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SegmentedButton(
                        selected = inputMode == 0,
                        onClick = { inputMode = 0 },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) {
                        Text("卖出价")
                    }
                    SegmentedButton(
                        selected = inputMode == 1,
                        onClick = { inputMode = 1 },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) {
                        Text("卖出金额")
                    }
                }
                
                // 卖出数量（两种模式都需要）
                OutlinedTextField(
                    value = sellQuantity,
                    onValueChange = { 
                        if (it.isEmpty() || it.matches(Regex("\\d*\\.?\\d*"))) {
                            sellQuantity = it
                        }
                    },
                    label = { Text("卖出数量") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isOverQuantity) RedLoss else GreenPrimary,
                        unfocusedBorderColor = if (isOverQuantity) RedLoss else GrayBorder
                    ),
                    isError = isOverQuantity,
                    supportingText = {
                        if (isOverQuantity) {
                            Text("卖出数量不能超过持仓数量", color = RedLoss)
                        } else if (finalSellQuantity > 0) {
                            Text("剩余: ${String.format("%.2f", remainingQuantity)}")
                        }
                    }
                )
                
                // 根据模式显示不同的输入
                when (inputMode) {
                    0 -> {
                        // 模式1：卖出价
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
                    }
                    1 -> {
                        // 模式2：卖出金额
                        OutlinedTextField(
                            value = sellAmount,
                            onValueChange = { 
                                if (it.isEmpty() || it.matches(Regex("\\d*\\.?\\d*"))) {
                                    sellAmount = it
                                }
                            },
                            label = { Text("卖出金额") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GreenPrimary,
                                unfocusedBorderColor = GrayBorder
                            )
                        )
                        
                        // 显示自动计算的卖出价
                        if (finalSellPrice > 0) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = GreenLight
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "自动计算卖出价",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = GrayText
                                    )
                                    Text(
                                        String.format("%.4f", finalSellPrice),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = GreenPrimary
                                    )
                                }
                            }
                        }
                    }
                }
                
                // 收益预览
                if (finalSellPrice > 0 && finalSellQuantity > 0 && !isOverQuantity) {
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
                            // 卖出总金额
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "卖出总额",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GrayText
                                )
                                Text(
                                    String.format("%.2f", totalSellAmount),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (profit >= 0) GreenPrimary else RedLoss
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // 收益
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        "预估收益",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GrayText
                                    )
                                    Text(
                                        if (profit >= 0) 
                                            String.format("+%.2f", profit)
                                        else 
                                            String.format("%.2f", profit),
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (profit >= 0) GreenPrimary else RedLoss
                                    )
                                }
                                
                                Text(
                                    if (profitRate >= 0)
                                        String.format("+%.2f%%", profitRate)
                                    else
                                        String.format("%.2f%%", profitRate),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (profit >= 0) GreenPrimary else RedLoss
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (finalSellPrice > 0 && finalSellQuantity > 0 && !isOverQuantity) {
                        onConfirm(finalSellPrice, finalSellQuantity)
                    }
                },
                enabled = finalSellPrice > 0 && finalSellQuantity > 0 && !isOverQuantity
            ) {
                Text("确定减仓", color = GreenPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
