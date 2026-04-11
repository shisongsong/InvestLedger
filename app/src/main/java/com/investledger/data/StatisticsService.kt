package com.investledger.data

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * 统计服务 - 管理投资数据的综合计算
 * 
 * 设计模式：混合方案
 * - 小数据量：直接 SQL 聚合（当前方案）
 * - 大数据量：增量更新 + 定期校验
 * 
 * 核心机制：
 * 1. 增量更新：每次交易后立即更新统计快照（O(1) 性能）
 * 2. 定期校验：每 50 次交易或每天全量计算一次，修正累积误差
 * 3. 首次初始化：从现有数据全量计算
 */
class StatisticsService(
    private val statisticsDao: StatisticsDao,
    private val coroutineScope: CoroutineScope
) {
    // 统计快照的响应式流
    val statistics: StateFlow<StatisticsSnapshot> = statisticsDao.getStatistics()
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = StatisticsSnapshot.empty()
        )
    
    // 校验间隔（交易次数）
    private val validationInterval = 50
    
    /**
     * 增量更新：添加交易后调用
     * 时间复杂度：O(1)
     */
    fun onTransactionAdded(transaction: com.investledger.data.Transaction) {
        coroutineScope.launch {
            val current = statisticsDao.getStatisticsSync() ?: StatisticsSnapshot.empty()
            val updated = current.copy(
                totalProfit = current.totalProfit + transaction.profit,
                winCount = if (transaction.profit > 0) current.winCount + 1 else current.winCount,
                lossCount = if (transaction.profit < 0) current.lossCount + 1 else current.lossCount,
                totalWin = if (transaction.profit > 0) current.totalWin + transaction.profit else current.totalWin,
                totalLoss = if (transaction.profit < 0) current.totalLoss + transaction.profit else current.totalLoss,
                transactionCount = current.transactionCount + 1,
                lastCalculatedAt = System.currentTimeMillis(),
                version = current.version + 1
            )
            statisticsDao.updateStatistics(updated)
            
            // 定期校验
            if (updated.transactionCount % validationInterval == 0) {
                validateAndFix()
            }
        }
    }
    
    /**
     * 增量更新：删除交易后调用
     */
    fun onTransactionDeleted(transaction: com.investledger.data.Transaction) {
        coroutineScope.launch {
            val current = statisticsDao.getStatisticsSync() ?: return@launch
            val updated = current.copy(
                totalProfit = current.totalProfit - transaction.profit,
                winCount = if (transaction.profit > 0) maxOf(0, current.winCount - 1) else current.winCount,
                lossCount = if (transaction.profit < 0) maxOf(0, current.lossCount - 1) else current.lossCount,
                totalWin = if (transaction.profit > 0) current.totalWin - transaction.profit else current.totalWin,
                totalLoss = if (transaction.profit < 0) current.totalLoss - transaction.profit else current.totalLoss,
                transactionCount = maxOf(0, current.transactionCount - 1),
                lastCalculatedAt = System.currentTimeMillis(),
                version = current.version + 1
            )
            statisticsDao.updateStatistics(updated)
        }
    }
    
    /**
     * 增量更新：交易修改后调用（先删后加）
     */
    fun onTransactionModified(
        oldTransaction: com.investledger.data.Transaction,
        newTransaction: com.investledger.data.Transaction
    ) {
        coroutineScope.launch {
            val current = statisticsDao.getStatisticsSync() ?: return@launch
            
            // 移除旧数据
            var temp = current.copy(
                totalProfit = current.totalProfit - oldTransaction.profit,
                winCount = if (oldTransaction.profit > 0) maxOf(0, current.winCount - 1) else current.winCount,
                lossCount = if (oldTransaction.profit < 0) maxOf(0, current.lossCount - 1) else current.lossCount,
                totalWin = if (oldTransaction.profit > 0) current.totalWin - oldTransaction.profit else current.totalWin,
                totalLoss = if (oldTransaction.profit < 0) current.totalLoss - oldTransaction.profit else current.totalLoss
            )
            
            // 添加新数据
            val updated = temp.copy(
                totalProfit = temp.totalProfit + newTransaction.profit,
                winCount = if (newTransaction.profit > 0) temp.winCount + 1 else temp.winCount,
                lossCount = if (newTransaction.profit < 0) temp.lossCount + 1 else temp.lossCount,
                totalWin = if (newTransaction.profit > 0) temp.totalWin + newTransaction.profit else temp.totalWin,
                totalLoss = if (newTransaction.profit < 0) temp.totalLoss + newTransaction.profit else temp.totalLoss,
                lastCalculatedAt = System.currentTimeMillis(),
                version = current.version + 1
            )
            
            statisticsDao.updateStatistics(updated)
        }
    }
    
    /**
     * 全量重新计算（用于初始化和校验）
     * 时间复杂度：O(n)，n 为交易记录数
     */
    suspend fun recalculateAll() {
        val totalProfit = statisticsDao.calculateTotalProfit()
        val winCount = statisticsDao.calculateWinCount()
        val lossCount = statisticsDao.calculateLossCount()
        val totalWin = statisticsDao.calculateTotalWin()
        val totalLoss = statisticsDao.calculateTotalLoss()
        val transactionCount = statisticsDao.calculateTransactionCount()
        val totalCost = statisticsDao.calculateTotalCost()
        
        val snapshot = StatisticsSnapshot(
            totalCost = totalCost,
            totalProfit = totalProfit,
            winCount = winCount,
            lossCount = lossCount,
            totalWin = totalWin,
            totalLoss = totalLoss,
            transactionCount = transactionCount,
            lastCalculatedAt = System.currentTimeMillis(),
            version = 1
        )
        statisticsDao.updateStatistics(snapshot)
    }
    
    /**
     * 校验并修正统计偏差
     * 在以下情况调用：
     * 1. 定期校验（每 50 次交易）
     * 2. 应用启动时（可选）
     * 3. 用户手动触发
     */
    suspend fun validateAndFix() {
        val current = statisticsDao.getStatisticsSync() ?: return
        
        // 计算实际值
        val actualProfit = statisticsDao.calculateTotalProfit()
        val actualWinCount = statisticsDao.calculateWinCount()
        val actualLossCount = statisticsDao.calculateLossCount()
        
        // 检测偏差（浮点数精度容忍 0.01）
        val hasDeviation = abs(current.totalProfit - actualProfit) > 0.01 ||
                          current.winCount != actualWinCount ||
                          current.lossCount != actualLossCount
        
        if (hasDeviation) {
            // 数据不一致，全量重算
            recalculateAll()
        }
    }
    
    /**
     * 检查是否需要初始化（统计表为空）
     */
    suspend fun needsInitialization(): Boolean {
        return statisticsDao.getStatisticsSync() == null
    }
    
    /**
     * 初始化统计（应用首次启动时调用）
     */
    suspend fun initializeIfNeeded() {
        if (needsInitialization()) {
            recalculateAll()
        }
    }
}
