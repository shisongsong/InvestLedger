package com.investledger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.investledger.data.Position
import com.investledger.data.Transaction
import com.investledger.ui.screens.*
import com.investledger.ui.theme.InvestLedgerTheme
import com.investledger.viewmodel.InvestViewModel
import com.investledger.viewmodel.InvestViewModelFactory

/**
 * 主活动
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            InvestLedgerTheme {
                val viewModel: InvestViewModel = viewModel(
                    factory = InvestViewModelFactory(applicationContext)
                )
                
                InvestLedgerApp(viewModel)
            }
        }
    }
}

/**
 * 应用主体
 */
@Composable
fun InvestLedgerApp(viewModel: InvestViewModel) {
    val navController = rememberNavController()
    
    // 导航项
    val items = listOf(
        Screen.Positions,
        Screen.Transactions,
        Screen.Statistics
    )
    
    // Dialog 状态
    var showOpenDialog by remember { mutableStateOf(false) }
    var showCloseDialog by remember { mutableStateOf(false) }
    var showReduceDialog by remember { mutableStateOf(false) }
    var showEditPositionDialog by remember { mutableStateOf(false) }
    var showEditTransactionDialog by remember { mutableStateOf(false) }
    var selectedPosition by remember { mutableStateOf<Position?>(null) }
    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Positions.route,
            modifier = Modifier.padding(padding)
        ) {
            // 持仓页面
            composable(Screen.Positions.route) {
                PositionListScreen(
                    viewModel = viewModel,
                    onAddPosition = { showOpenDialog = true },
                    onReducePosition = { position ->
                        selectedPosition = position
                        showReduceDialog = true
                    },
                    onClosePosition = { position ->
                        selectedPosition = position
                        showCloseDialog = true
                    },
                    onEditPosition = { position ->
                        selectedPosition = position
                        showEditPositionDialog = true
                    }
                )
            }
            
            // 交易记录页面
            composable(Screen.Transactions.route) {
                TransactionListScreen(
                    viewModel = viewModel,
                    onEditTransaction = { transaction ->
                        selectedTransaction = transaction
                        showEditTransactionDialog = true
                    }
                )
            }
            
            // 统计页面
            composable(Screen.Statistics.route) {
                StatisticsScreen(viewModel = viewModel)
            }
        }
    }
    
    // 建仓对话框
    if (showOpenDialog) {
        OpenPositionDialog(
            viewModel = viewModel,
            onDismiss = { showOpenDialog = false },
            onConfirm = { name, type, costPrice, quantity, note, createdAt ->
                viewModel.openPosition(name, type, costPrice, quantity, note, createdAt)
                showOpenDialog = false
            }
        )
    }
    
    // 减仓对话框
    val reducePos = selectedPosition
    if (showReduceDialog && reducePos != null) {
        ReducePositionDialog(
            position = reducePos,
            onDismiss = { 
                showReduceDialog = false
                selectedPosition = null
            },
            onConfirm = { sellPrice, sellQuantity ->
                viewModel.reducePosition(reducePos.id, sellPrice, sellQuantity)
                showReduceDialog = false
                selectedPosition = null
            }
        )
    }
    
    // 清仓对话框
    val closePos = selectedPosition
    if (showCloseDialog && closePos != null) {
        ClosePositionDialog(
            position = closePos,
            onDismiss = { 
                showCloseDialog = false
                selectedPosition = null
            },
            onConfirm = { sellPrice ->
                viewModel.closePosition(closePos.id, sellPrice)
                showCloseDialog = false
                selectedPosition = null
            }
        )
    }
    
    // 编辑持仓对话框
    val editPosition = selectedPosition
    if (showEditPositionDialog && editPosition != null) {
        EditPositionDialog(
            position = editPosition,
            onDismiss = { 
                showEditPositionDialog = false
                selectedPosition = null
            },
            onConfirm = { name, type, costPrice, quantity, note, createdAt ->
                viewModel.editPosition(editPosition.id, name, type, costPrice, quantity, note, createdAt)
                showEditPositionDialog = false
                selectedPosition = null
            }
        )
    }
    
    // 编辑交易记录对话框
    val editTransaction = selectedTransaction
    if (showEditTransactionDialog && editTransaction != null) {
        EditTransactionDialog(
            transaction = editTransaction,
            onDismiss = { 
                showEditTransactionDialog = false
                selectedTransaction = null
            },
            onConfirm = { name, type, costPrice, sellPrice, quantity, createdAt ->
                viewModel.editTransaction(
                    transactionId = editTransaction.id,
                    name = name,
                    type = type,
                    costPrice = costPrice,
                    sellPrice = sellPrice,
                    quantity = quantity,
                    createdAt = createdAt
                )
                showEditTransactionDialog = false
                selectedTransaction = null
            }
        )
    }
}

/**
 * 导航屏幕定义
 */
sealed class Screen(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    object Positions : Screen(
        route = "positions",
        label = "持仓",
        icon = Icons.Default.AccountBalance
    )
    
    object Transactions : Screen(
        route = "transactions",
        label = "记录",
        icon = Icons.Default.ReceiptLong
    )
    
    object Statistics : Screen(
        route = "statistics",
        label = "统计",
        icon = Icons.Default.TrendingUp
    )
}
