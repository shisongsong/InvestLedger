package com.investledger.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
    val positions by viewModel.positions.collectAsState()
    val totalCost by viewModel.totalCost.collectAsState()
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "持仓",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                actions = {
                    IconButton(onClick = onAddPosition) {
                        Icon(Icons.Default.Add, contentDescription = "建仓")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = White
                )
            )
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
            if (positions.isNotEmpty()) {
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
                        Text(
                            "${positions.size} 个持仓",
                            style = MaterialTheme.typography.bodySmall,
                            color = GrayText
                        )
                    }
                }
            }
            
            // 持仓列表
            if (positions.isEmpty()) {
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
                            "暂无持仓",
                            style = MaterialTheme.typography.titleMedium,
                            color = GrayText
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "点击右下角按钮开始建仓",
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
                        items = positions,
                        key = { it.id }
                    ) { position ->
                        PositionCard(
                            position = position,
                            onEdit = { onEditPosition(position) },
                            onReduce = { onReducePosition(position) },
                            onClose = { onClosePosition(position) },
                            onDelete = { viewModel.deletePosition(position) }
                        )
                    }
                }
            }
        }
    }
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
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    
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
                
                IconButton(onClick = { showEditDialog = true }) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "编辑",
                        tint = GrayLight
                    )
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
    
    // 编辑对话框 - 这里是添加的对话框，调用传入的 onEdit 回调
    if (showEditDialog) {
        EditPositionDialog(
            position = position,
            onDismiss = { showEditDialog = false },
            onConfirm = { name, type, costPrice, quantity, note, createdAt ->
                // 调用传入的编辑函数，这个函数会被主界面传进来的回调处理
                onEdit()
                showEditDialog = false
            }
        )
    }
}
