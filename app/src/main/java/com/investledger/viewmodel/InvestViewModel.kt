package com.investledger.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import android.net.Uri
import com.investledger.data.*
import com.investledger.data.CsvService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 投资账本 ViewModel
 */
class InvestViewModel(
    private val database: AppDatabase,
    private val statisticsService: StatisticsService,
    context: Context
) : ViewModel() {
    
    private val csvService = CsvService(context)
    
    private val positionDao = database.positionDao()
    private val transactionDao = database.transactionDao()
    
    // 持仓列表
    val positions: StateFlow<List<Position>> = positionDao.getAllPositions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // 交易记录
    val transactions: StateFlow<List<Transaction>> = transactionDao.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // 月度统计
    val monthlyStats: StateFlow<List<com.investledger.data.MonthlyStat>> = transactionDao.getMonthlyStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // 统计快照（新的优化方案）
    val statistics: StateFlow<StatisticsSnapshot> = statisticsService.statistics
    
    // 保留旧的统计查询作为后备（兼容旧代码）
    val totalCost: StateFlow<Double> = positionDao.getTotalCost()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    
    val totalProfit: StateFlow<Double> = transactionDao.getTotalProfit()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    
    val winCount: StateFlow<Int> = transactionDao.getWinCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    
    val lossCount: StateFlow<Int> = transactionDao.getLossCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    
    val totalWin: StateFlow<Double> = transactionDao.getTotalWin()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    
    val totalLoss: StateFlow<Double> = transactionDao.getTotalLoss()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    
    val positionCount: StateFlow<Int> = positionDao.getPositionCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    
    val transactionCount: StateFlow<Int> = transactionDao.getTransactionCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    
    init {
        // 初始化统计服务
        viewModelScope.launch {
            statisticsService.initializeIfNeeded()
        }
    }
    
    /**
     * 检查是否存在同名持仓
     */
    suspend fun checkExistingPosition(name: String): Position? {
        return positionDao.getPositionByName(name)
    }

    /**
     * 建仓（买入）
     * @param mergeWithExisting 是否合并到已有持仓（加仓）
     */
    fun openPosition(
        name: String,
        type: String,
        costPrice: Double,
        quantity: Double,
        note: String = "",
        createdAt: Long = System.currentTimeMillis(),
        mergeWithExisting: Boolean = false
    ) {
        viewModelScope.launch {
            if (mergeWithExisting) {
                val existing = positionDao.getPositionByName(name)
                if (existing != null) {
                    val newQuantity = existing.quantity + quantity
                    val newTotalCost = (existing.costPrice * existing.quantity) + (costPrice * quantity)
                    val newCostPrice = if (newQuantity > 0) newTotalCost / newQuantity else costPrice
                    
                    // 合并备注
                    val mergedNote = when {
                        existing.note.isBlank() -> note
                        note.isBlank() -> existing.note
                        else -> "${existing.note} | $note"
                    }
                    
                    val updated = existing.copy(
                        quantity = newQuantity,
                        costPrice = newCostPrice,
                        note = mergedNote,
                        createdAt = existing.createdAt // 保持首次建仓时间
                    )
                    positionDao.updatePosition(updated)
                } else {
                    // 如果未找到，则新建
                    val position = Position(name = name, type = type, costPrice = costPrice, quantity = quantity, createdAt = createdAt, note = note)
                    positionDao.insertPosition(position)
                }
            } else {
                val position = Position(
                    name = name,
                    type = type,
                    costPrice = costPrice,
                    quantity = quantity,
                    createdAt = createdAt,
                    note = note
                )
                positionDao.insertPosition(position)
            }
            
            // 更新统计
            statisticsService.recalculateAll()
        }
    }
    
    /**
     * 减仓（部分卖出）
     */
    fun reducePosition(positionId: Long, sellPrice: Double, sellQuantity: Double) {
        viewModelScope.launch {
            val position = positionDao.getPositionById(positionId)
            if (position != null && sellQuantity > 0 && sellQuantity <= position.quantity) {
                val profit = (sellPrice - position.costPrice) * sellQuantity
                val profitRate = if (position.costPrice != 0.0) {
                    ((sellPrice - position.costPrice) / position.costPrice) * 100
                } else 0.0
                
                // 创建交易记录
                val transaction = Transaction(
                    positionId = position.id,
                    name = position.name,
                    type = position.type,
                    costPrice = position.costPrice,
                    sellPrice = sellPrice,
                    quantity = sellQuantity,
                    profit = profit,
                    profitRate = profitRate
                )
                transactionDao.insertTransaction(transaction)
                
                // 更新持仓数量
                val newQuantity = position.quantity - sellQuantity
                if (newQuantity <= 0) {
                    // 全部卖出，删除持仓
                    positionDao.deletePosition(position)
                } else {
                    // 部分卖出，更新持仓（保持成本价不变）
                    val updatedPosition = position.copy(quantity = newQuantity)
                    positionDao.updatePosition(updatedPosition)
                }
                
                // 增量更新统计
                statisticsService.onTransactionAdded(transaction)
            }
        }
    }
    
    /**
     * 编辑持仓
     */
    fun editPosition(positionId: Long, name: String, type: String, costPrice: Double, quantity: Double, note: String, createdAt: Long) {
        viewModelScope.launch {
            val position = positionDao.getPositionById(positionId)
            if (position != null) {
                val updatedPosition = position.copy(
                    name = name,
                    type = type,
                    costPrice = costPrice,
                    quantity = quantity,
                    note = note,
                    createdAt = createdAt
                )
                positionDao.updatePosition(updatedPosition)
                
                // 重新计算统计（持仓成本变化）
                statisticsService.recalculateAll()
            }
        }
    }
    
    /**
     * 编辑交易记录
     */
    fun editTransaction(
        transactionId: Long,
        name: String,
        type: String,
        costPrice: Double,
        sellPrice: Double,
        quantity: Double,
        createdAt: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            val transaction = transactionDao.getTransactionById(transactionId)
            if (transaction != null) {
                val profit = (sellPrice - costPrice) * quantity
                val profitRate = if (costPrice != 0.0) {
                    ((sellPrice - costPrice) / costPrice) * 100
                } else 0.0
                
                val updatedTransaction = transaction.copy(
                    name = name,
                    type = type,
                    costPrice = costPrice,
                    sellPrice = sellPrice,
                    quantity = quantity,
                    profit = profit,
                    profitRate = profitRate,
                    createdAt = createdAt
                )
                
                // 先删除旧的统计影响
                statisticsService.onTransactionDeleted(transaction)
                
                transactionDao.updateTransaction(updatedTransaction)
                
                // 再添加新的统计影响
                statisticsService.onTransactionAdded(updatedTransaction)
            }
        }
    }
    
    /**
     * 清仓（卖出）
     */
    fun closePosition(positionId: Long, sellPrice: Double) {
        viewModelScope.launch {
            val position = positionDao.getPositionById(positionId)
            if (position != null) {
                val profit = (sellPrice - position.costPrice) * position.quantity
                val profitRate = if (position.costPrice != 0.0) {
                    ((sellPrice - position.costPrice) / position.costPrice) * 100
                } else 0.0
                
                val transaction = Transaction(
                    positionId = position.id,
                    name = position.name,
                    type = position.type,
                    costPrice = position.costPrice,
                    sellPrice = sellPrice,
                    quantity = position.quantity,
                    profit = profit,
                    profitRate = profitRate
                )
                
                transactionDao.insertTransaction(transaction)
                positionDao.deletePosition(position)
                
                // 增量更新统计
                statisticsService.onTransactionAdded(transaction)
            }
        }
    }
    
    /**
     * 更新持仓当前价格
     */
    fun updateCurrentPrice(positionId: Long, price: Double) {
        viewModelScope.launch {
            positionDao.updateCurrentPrice(positionId, price)
        }
    }
    
    /**
     * 删除持仓
     */
    fun deletePosition(position: Position) {
        viewModelScope.launch {
            positionDao.deletePosition(position)
            
            // 重新计算统计
            statisticsService.recalculateAll()
        }
    }
    
    /**
     * 删除交易记录
     */
    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            // 先删除旧的统计影响
            statisticsService.onTransactionDeleted(transaction)
            
            transactionDao.deleteTransaction(transaction)
        }
    }
    
    /**
     * 计算胜率
     */
    fun calculateWinRate(): Double {
        val total = winCount.value + lossCount.value
        return if (total > 0) {
            (winCount.value.toDouble() / total) * 100
        } else {
            0.0
        }
    }
    
    // ========== 导出/导入 ==========
    
    /**
     * 导出数据到 CSV
     */
    fun exportData(uri: Uri): kotlinx.coroutines.flow.Flow<Result<Unit>> {
        return kotlinx.coroutines.flow.flow {
            val positionsList = positions.value
            val transactionsList = transactions.value
            emit(csvService.exportData(positionsList, transactionsList, uri))
        }
    }
    
    /**
     * 从 CSV 导入数据
     */
    fun importData(uri: Uri): kotlinx.coroutines.flow.Flow<Result<Unit>> {
        return kotlinx.coroutines.flow {
            val result = csvService.importData(uri)
            if (result.isSuccess) {
                val importResult = result.getOrNull()!!
                // 清空现有数据（可选：也可以不删除，直接追加）
                // 这里采用追加模式
                
                importResult.positions.forEach { pos ->
                    positionDao.insertPosition(pos.copy(id = 0)) // Reset ID for new insert
                }
                importResult.transactions.forEach { tx ->
                    transactionDao.insertTransaction(tx.copy(id = 0))
                }
                
                // 重新计算统计
                statisticsService.recalculateAll()
                emit(Result.success(Unit))
            } else {
                emit(Result.failure(result.exceptionOrNull() ?: Exception("Import failed")))
            }
        }
    }
    
    /**
     * 获取导出文件名
     */
    fun getExportFileName(): String = csvService.generateExportFileName()
    
    /**
     * 获取投资名称建议（用于自动补全）
     */
    suspend fun getNameSuggestions(query: String): List<NameTypePair> {
        val allPairs = positionDao.getDistinctNames()
        if (query.isBlank()) return allPairs.take(10) // 空白时返回最近10个
        
        val lowerQuery = query.lowercase()
        return allPairs.filter { pair ->
            pair.name.lowercase().contains(lowerQuery) ||
            pair.type.lowercase().contains(lowerQuery)
        }.take(10) // 最多显示10个建议
    }
}
