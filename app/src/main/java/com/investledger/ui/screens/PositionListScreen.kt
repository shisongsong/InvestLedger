package com.investledger.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.investledger.data.Position
import com.investledger.ui.theme.*
import com.investledger.viewmodel.InvestViewModel
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * 持仓列表屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PositionListScreen(
    viewModel: InvestViewModel,
    onAddPosition: () -> Unit,
    onReducePosition: (Position) -> Unit,
    onClosePosition: (Position) -> Unit,
    onEditPosition: (Position) -> Unit
) {
    val allPositions by viewModel.positions.collectAsState()
    val totalCost by viewModel.totalCost.collectAsState()
    var showPriceDialog by remember { mutableStateOf<Position?>(null) }
    
    // 搜索状态
    var searchQuery by remember { mutableStateOf("") }
    val filteredPositions = remember(allPositions, searchQuery) {
        if (searchQuery.isBlank()) {
            allPositions
        } else {
            val lowerQuery = searchQuery.lowercase()
            allPositions.filter { 
                it.name.lowercase().contains(lowerQuery) || 
                it.type.lowercase().contains(lowerQuery) ||
                it.note.lowercase().contains(lowerQuery)
            }
        }
    }
    
    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = { 
                        Text(
                            "持仓",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    },
                    actions = {
                        IconButton(onClick = { /* Toggle search */ }) {
                            Icon(Icons.Outlined.Search, contentDescription = "搜索")
                        }
                        IconButton(onClick = onAddPosition) {
                            Icon(Icons.Default.Add, contentDescription = "建仓")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = White
                    )
                )
                
                // 搜索栏
                if (searchQuery.isNotEmpty() || allPositions.size > 3) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        placeholder = { Text("搜索名称/类型/备注") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "清除")
                                }
                            }
                        },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddPosition,
                containerColor = GreenPrimary,
                contentColor = White
            ) {
                Icon(Icons.Default.Add, contentDescription = "建仓")
            }
        },
        containerColor = SurfaceLight
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 总成本卡片
            if (filteredPositions.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = GreenLight),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "总成本",
                            style = MaterialTheme.typography.labelMedium,
                            color = GrayText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            String.format("%.2f", totalCost),
                            style = MaterialTheme.typography.headlineMedium,
                            color = GreenPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val displayText = if (searchQuery.isNotBlank()) {
                            "${filteredPositions.size}/${allPositions.size} 个持仓"
                        } else {
                            "${allPositions.size} 个持仓"
                        }
                        Text(
                            displayText,
                            style = MaterialTheme.typography.bodySmall,
                            color = GrayText
                        )
                    }
                }
            }
            
            // 持仓列表
            if (filteredPositions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.AccountBalance,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = GrayLight
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            if (searchQuery.isNotBlank()) "无匹配结果" else "暂无持仓",
                            style = MaterialTheme.typography.titleMedium,
                            color = GrayText
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            if (searchQuery.isNotBlank()) "换个关键词试试" else "点击右下角按钮开始建仓",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GrayLight
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = filteredPositions,
                        key = { it.id }
                    ) { position ->
                        PositionCard(
                            position = position,
                            onEdit = { onEditPosition(position) },
                            onReduce = { onReducePosition(position) },
                            onClose = { onClosePosition(position) },
                            onDelete = { viewModel.deletePosition(position) },
                            onUpdatePrice = { showPriceDialog = position }
                        )
                    }
                }
            }
        }
    }
    
    // 设置现价对话框
    val pricePosition = showPriceDialog
    if (pricePosition != null) {
        CurrentPriceDialog(
            position = pricePosition,
            onDismiss = { showPriceDialog = null },
            onConfirm = { price ->
                viewModel.updateCurrentPrice(pricePosition.id, price)
                showPriceDialog = null
            }
        )
    }
}

/**
 * 设置现价对话框
 */
@Composable
fun CurrentPriceDialog(
    position: Position,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var priceText by remember { mutableStateOf(position.currentPrice.takeIf { it > 0 }?.let { String.format("%.2f", it) } ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("设置现价 - ${position.name}") },
        text = {
            Column {
                Text("请输入${position.name}的当前价格", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { 
                        if (it.isEmpty() || it.matches(Regex("\\d*\\.?\\d*"))) {
                            priceText = it
                        }
                    },
                    label = { Text("当前价格") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val price = priceText.toDoubleOrNull() ?: 0.0
                    if (price > 0) onConfirm(price)
                },
                enabled = (priceText.toDoubleOrNull() ?: 0.0) > 0
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
 * 持仓卡片
 */
@Composable
fun PositionCard(
    position: Position,
    onEdit: () -> Unit,
    onReduce: () -> Unit,
    onClose: () -> Unit,
    onDelete: () -> Unit,
    onUpdatePrice: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    // 计算浮盈浮亏
    val floatingProfit = position.floatingProfit
    val floatingRate = position.floatingProfitRate
    val hasCurrentPrice = position.currentPrice > 0
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        position.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${position.type} | ${SimpleDateFormat("MM/dd", Locale.getDefault()).format(java.util.Date(position.createdAt))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = GrayText
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onUpdatePrice) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "设置现价",
                            tint = if (hasCurrentPrice) GreenPrimary else GrayLight
                        )
                    }
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "编辑",
                            tint = GrayLight
                        )
                    }
                }
            }
            
            // 浮动盈亏显示
            if (hasCurrentPrice) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "现价",
                            style = MaterialTheme.typography.labelSmall,
                            color = GrayText
                        )
                        Text(
                            position.formatCurrentPrice(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "浮动盈亏",
                            style = MaterialTheme.typography.labelSmall,
                            color = GrayText
                        )
                        Text(
                            if (floatingProfit >= 0) "+${String.format("%.2f", floatingProfit)}" else String.format("%.2f", floatingProfit),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (floatingProfit >= 0) GreenPrimary else RedLoss
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "盈亏比例",
                            style = MaterialTheme.typography.labelSmall,
                            color = GrayText
                        )
                        Text(
                            if (floatingRate >= 0) "+${String.format("%.2f%%", floatingRate)}" else String.format("%.2f%%", floatingRate),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (floatingRate >= 0) GreenPrimary else RedLoss
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column {
                    Text(
                        "成本价",
                        style = MaterialTheme.typography.labelSmall,
                        color = GrayText
                    )
                    Text(
                        position.formatPrice(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Column {
                    Text(
                        "数量",
                        style = MaterialTheme.typography.labelSmall,
                        color = GrayText
                    )
                    Text(
                        position.formatQuantity(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Column {
                    Text(
                        "总成本",
                        style = MaterialTheme.typography.labelSmall,
                        color = GrayText
                    )
                    Text(
                        String.format("%.2f", position.totalCost),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GreenPrimary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onReduce,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenSecondary,
                        contentColor = White
                    )
                ) {
                    Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("减仓")
                }
                
                Button(
                    onClick = onClose,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RedLoss,
                        contentColor = White
                    )
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("清仓")
                }
            }
        }
    }
    
    // 删除确认对话框
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除此持仓记录吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text("删除", color = RedLoss)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}
