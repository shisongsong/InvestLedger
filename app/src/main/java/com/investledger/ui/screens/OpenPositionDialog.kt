package com.investledger.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.investledger.data.NameTypePair
import com.investledger.data.Position
import com.investledger.ui.components.DateTimePicker
import com.investledger.ui.theme.*
import com.investledger.viewmodel.InvestViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 建仓对话框 - 支持两种计算方式、日期时间设置和自动补全
 * 1. 按成本价+数量
 * 2. 按金额+数量（自动计算成本价）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenPositionDialog(
    viewModel: InvestViewModel,
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: String, costPrice: Double, quantity: Double, note: String, createdAt: Long, mergeWithExisting: Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("股票") }
    var note by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    // 自动补全相关状态
    var nameSuggestions by remember { mutableStateOf<List<NameTypePair>>(emptyList()) }
    var showSuggestions by remember { mutableStateOf(false) }
    var searchJob by remember { mutableStateOf<Job?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // 加仓确认状态
    var showMergeDialog by remember { mutableStateOf(false) }
    var existingPositionForMerge by remember { mutableStateOf<Position?>(null) }

    // 输入模式：0=成本价+数量, 1=金额+数量
    var inputMode by remember { mutableStateOf(0) }

    // 模式1：成本价和数量
    var costPrice by remember { mutableStateOf("") }
    var quantityMode1 by remember { mutableStateOf("") }

    // 模式2：金额和数量
    var totalAmount by remember { mutableStateOf("") }
    var quantityMode2 by remember { mutableStateOf("") }

    // 日期时间
    var createdAt by remember { mutableStateOf(System.currentTimeMillis()) }

    val types = listOf("股票", "基金", "加密货币", "债券", "其他")

    // 计算实时总额显示
    val displayTotal = when (inputMode) {
        0 -> {
            val cost = costPrice.toDoubleOrNull() ?: 0.0
            val qty = quantityMode1.toDoubleOrNull() ?: 0.0
            if (cost > 0 && qty > 0) cost * qty else 0.0
        }
        1 -> {
            totalAmount.toDoubleOrNull() ?: 0.0
        }
        else -> 0.0
    }

    // 计算实际成本价（用于保存）
    val finalCostPrice = when (inputMode) {
        0 -> costPrice.toDoubleOrNull() ?: 0.0
        1 -> {
            val amount = totalAmount.toDoubleOrNull() ?: 0.0
            val qty = quantityMode2.toDoubleOrNull() ?: 0.0
            if (qty > 0) amount / qty else 0.0
        }
        else -> 0.0
    }

    // 计算实际数量
    val finalQuantity = when (inputMode) {
        0 -> quantityMode1.toDoubleOrNull() ?: 0.0
        1 -> quantityMode2.toDoubleOrNull() ?: 0.0
        else -> 0.0
    }

    // 更新建议列表（带防抖和协程取消）
    fun updateSuggestions(query: String) {
        searchJob?.cancel()  // 取消上一次的查询
        searchJob = coroutineScope.launch {
            delay(300)  // 300ms 防抖，避免频繁查询
            nameSuggestions = viewModel.getNameSuggestions(query)
            showSuggestions = nameSuggestions.isNotEmpty()
        }
    }

    // 选择建议项
    fun selectSuggestion(suggestion: NameTypePair) {
        name = suggestion.name
        type = suggestion.type
        showSuggestions = false
        focusManager.clearFocus()  // 收起键盘
    }

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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 名称输入（带自动补全）
                Box(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { newName ->
                                name = newName
                                if (newName.isNotBlank()) {
                                    updateSuggestions(newName)
                                } else {
                                    showSuggestions = false
                                }
                            },
                            label = { Text("投资名称/代码") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GreenPrimary,
                                unfocusedBorderColor = GrayBorder
                            ),
                            trailingIcon = if (name.isNotEmpty()) {
                                {
                                    IconButton(onClick = {
                                        name = ""
                                        showSuggestions = false
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "清空",
                                            tint = GrayText
                                        )
                                    }
                                }
                            } else {
                                null
                            }
                        )

                        // 下拉建议列表
                        if (showSuggestions && nameSuggestions.isNotEmpty()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 200.dp)
                                ) {
                                    items(nameSuggestions) { suggestion ->
                                        SuggestionItem(
                                            suggestion = suggestion,
                                            onClick = { selectSuggestion(suggestion) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
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

                // 输入模式选择
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SegmentedButton(
                        selected = inputMode == 0,
                        onClick = { inputMode = 0 },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) {
                        Text("成本价+数量")
                    }
                    SegmentedButton(
                        selected = inputMode == 1,
                        onClick = { inputMode = 1 },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) {
                        Text("金额+数量")
                    }
                }

                // 根据模式显示不同的输入
                when (inputMode) {
                    0 -> {
                        // 模式1：成本价 + 数量
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

                        OutlinedTextField(
                            value = quantityMode1,
                            onValueChange = {
                                if (it.isEmpty() || it.matches(Regex("\\d*\\.?\\d*"))) {
                                    quantityMode1 = it
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
                    }
                    1 -> {
                        // 模式2：金额 + 数量
                        OutlinedTextField(
                            value = totalAmount,
                            onValueChange = {
                                if (it.isEmpty() || it.matches(Regex("\\d*\\.?\\d*"))) {
                                    totalAmount = it
                                }
                            },
                            label = { Text("金额") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GreenPrimary,
                                unfocusedBorderColor = GrayBorder
                            )
                        )

                        OutlinedTextField(
                            value = quantityMode2,
                            onValueChange = {
                                if (it.isEmpty() || it.matches(Regex("\\d*\\.?\\d*"))) {
                                    quantityMode2 = it
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

                        // 显示自动计算的成本价
                        if (finalCostPrice > 0) {
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
                                        "自动计算成本价",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = GrayText
                                    )
                                    Text(
                                        String.format("%.4f", finalCostPrice),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = GreenPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                // 实时显示总金额
                if (displayTotal > 0) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "总金额",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                String.format("%.2f", displayTotal),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
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
                    if (name.isNotBlank() && finalCostPrice > 0 && finalQuantity > 0) {
                        onConfirm(name, type, finalCostPrice, finalQuantity, note, createdAt, false)
                    }
                },
                enabled = name.isNotBlank() && finalCostPrice > 0 && finalQuantity > 0
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

/**
 * 建议项组件
 */
@Composable
private fun SuggestionItem(
    suggestion: NameTypePair,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = suggestion.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = suggestion.type,
            style = MaterialTheme.typography.labelSmall,
            color = GrayText
        )
    }
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 0.5.dp,
        color = GrayBorder
    )
}
