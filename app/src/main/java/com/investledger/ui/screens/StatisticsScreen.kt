package com.investledger.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.investledger.ui.theme.*
import com.investledger.viewmodel.InvestViewModel

/**
 * 统计屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: InvestViewModel
) {
    val totalProfit by viewModel.totalProfit.collectAsState()
    val winCount by viewModel.winCount.collectAsState()
    val lossCount by viewModel.lossCount.collectAsState()
    val totalWin by viewModel.totalWin.collectAsState()
    val totalLoss by viewModel.totalLoss.collectAsState()
    val transactionCount by viewModel.transactionCount.collectAsState()
    val positionCount by viewModel.positionCount.collectAsState()
    
    val winRate = viewModel.calculateWinRate()
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "收益统计",
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 总收益卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (totalProfit >= 0) GreenLight else RedLight
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.TrendingUp,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = if (totalProfit >= 0) GreenPrimary else RedLoss
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "累计收益",
                        style = MaterialTheme.typography.labelMedium,
                        color = GrayText
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        if (totalProfit >= 0)
                            String.format("+%.2f", totalProfit)
                        else
                            String.format("%.2f", totalProfit),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (totalProfit >= 0) GreenPrimary else RedLoss
                    )
                }
            }
            
            // 交易统计
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "交易统计",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(
                            label = "总交易",
                            value = transactionCount.toString(),
                            icon = Icons.Default.ReceiptLong,
                            color = GrayText
                        )
                        StatItem(
                            label = "持仓数",
                            value = positionCount.toString(),
                            icon = Icons.Default.AccountBalance,
                            color = GrayText
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(
                            label = "盈利次数",
                            value = winCount.toString(),
                            icon = Icons.Default.ArrowUpward,
                            color = GreenPrimary
                        )
                        StatItem(
                            label = "亏损次数",
                            value = lossCount.toString(),
                            icon = Icons.Default.ArrowDownward,
                            color = RedLoss
                        )
                    }
                }
            }
            
            // 收益详情
            if (transactionCount > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "收益详情",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // 胜率
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "胜率",
                                style = MaterialTheme.typography.bodyMedium,
                                color = GrayText
                            )
                            Text(
                                String.format("%.2f%%", winRate),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (winRate >= 50) GreenPrimary else RedLoss
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // 总盈利
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "总盈利",
                                style = MaterialTheme.typography.bodyMedium,
                                color = GrayText
                            )
                            Text(
                                String.format("+%.2f", totalWin),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = GreenPrimary
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // 总亏损
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "总亏损",
                                style = MaterialTheme.typography.bodyMedium,
                                color = GrayText
                            )
                            Text(
                                String.format("%.2f", totalLoss),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = RedLoss
                            )
                        }
                    }
                }
            }
            
            // 空状态提示
            if (transactionCount == 0 && positionCount == 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = GrayLight
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "暂无数据",
                            style = MaterialTheme.typography.titleMedium,
                            color = GrayText
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "开始建仓并清仓后，这里会显示收益统计",
                            style = MaterialTheme.typography.bodySmall,
                            color = GrayLight
                        )
                    }
                }
            }
        }
    }
}

/**
 * 统计项组件
 */
@Composable
fun StatItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = color
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = GrayText
        )
    }
}