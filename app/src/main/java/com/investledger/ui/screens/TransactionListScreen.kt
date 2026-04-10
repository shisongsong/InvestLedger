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
import com.investledger.data.Transaction
import com.investledger.ui.theme.*
import com.investledger.viewmodel.InvestViewModel
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * 交易记录屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    viewModel: InvestViewModel,
    onEditTransaction: (Transaction) -> Unit
) {
    val transactions by viewModel.transactions.collectAsState()
    val totalProfit by viewModel.totalProfit.collectAsState()
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "交易记录",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = White
                )
            )
        },
        containerColor = SurfaceLight
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 总收益卡片
            if (transactions.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (totalProfit >= 0) GreenLight else RedLight
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "累计收益",
                            style = MaterialTheme.typography.labelMedium,
                            color = GrayText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            if (totalProfit >= 0)
                                String.format("+%.2f", totalProfit)
                            else
                                String.format("%.2f", totalProfit),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (totalProfit >= 0) GreenPrimary else RedLoss
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "${transactions.size} 笔交易",
                            style = MaterialTheme.typography.bodySmall,
                            color = GrayText
                        )
                    }
                }
            }
            
            // 交易记录列表
            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.CENTER
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.ReceiptLong,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = GrayLight
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "暂无交易记录",
                            style = MaterialTheme.typography.titleMedium,
                            color = GrayText
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "清仓后会自动生成交易记录",
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
                        items = transactions,
                        key = { it.id }
                    ) { transaction ->
                        TransactionCard(
                            transaction = transaction,
                            onEdit = { onEditTransaction(transaction) },
                            onDelete = { viewModel.deleteTransaction(transaction) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 交易记录卡片
 */
@Composable
fun TransactionCard(
    transaction: Transaction,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (transaction.profit >= 0) GreenLight else RedLight
        ),
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
                        transaction.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${transaction.type} | ${SimpleDateFormat("MM/dd", Locale.getDefault()).format(java.util.Date(transaction.createdAt))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = GrayText
                    )
                }
                
                Row {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "编辑",
                            tint = GrayLight
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "删除",
                            tint = GrayLight
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
                        String.format("%.2f", transaction.costPrice),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Column {
                    Text(
                        "卖出价",
                        style = MaterialTheme.typography.labelSmall,
                        color = GrayText
                    )
                    Text(
                        String.format("%.2f", transaction.sellPrice),
                        style = MaterialTheme.typography.titleSmall,
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
                        if (transaction.quantity == transaction.quantity.toLong().toDouble())
                            transaction.quantity.toLong().toString()
                        else
                            String.format("%.2f", transaction.quantity),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 收益显示
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "收益",
                        style = MaterialTheme.typography.labelSmall,
                        color = GrayText
                    )
                    Text(
                        transaction.formatProfit(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (transaction.profit >= 0) GreenPrimary else RedLoss
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "收益率",
                        style = MaterialTheme.typography.labelSmall,
                        color = GrayText
                    )
                    Text(
                        transaction.formatProfitRate(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (transaction.profit >= 0) GreenPrimary else RedLoss
                    )
                }
            }
        }
    }
    
    // 删除确认对话框
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除此交易记录吗？") },
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