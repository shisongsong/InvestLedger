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
                var showOpenDialog by remember { mutableStateOf(false) }
                var showCloseDialog by remember { mutableStateOf(false) }
                var showReduceDialog by remember { mutableStateOf(false) }
                var selectedPosition by remember { mutableStateOf<Position?>(null) }
                
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
                    }
                )
                
                // 建仓对话框
                if (showOpenDialog) {
                    OpenPositionDialog(
                        onDismiss = { showOpenDialog = false },
                        onConfirm = { name, type, costPrice, quantity, note ->
                            viewModel.openPosition(name, type, costPrice, quantity, note)
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
            }
            
            // 交易记录页面
            composable(Screen.Transactions.route) {
                TransactionListScreen(viewModel = viewModel)
            }
            
            // 统计页面
            composable(Screen.Statistics.route) {
                StatisticsScreen(viewModel = viewModel)
            }
        }
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
