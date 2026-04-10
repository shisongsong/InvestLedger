package com.investledger.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.investledger.data.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 投资账本 ViewModel
 */
class InvestViewModel(private val database: AppDatabase) : ViewModel() {
    
    private val positionDao = database.positionDao()
    private val transactionDao = database.transactionDao()
    
    // 持仓列表
    val positions: StateFlow<List<Position>> = positionDao.getAllPositions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // 交易记录
    val transactions: StateFlow<List<Transaction>> = transactionDao.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // 统计数据
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
    
    /**
     * 建仓（买入）
     */
    fun openPosition(
        name: String,
        type: String,
        costPrice: Double,
        quantity: Double,
        note: String = "",
        createdAt: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
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
                transactionDao.updateTransaction(updatedTransaction)
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
            }
        }
    }
    
    /**
     * 删除持仓
     */
    fun deletePosition(position: Position) {
        viewModelScope.launch {
            positionDao.deletePosition(position)
        }
    }
    
    /**
     * 删除交易记录
     */
    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
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
}