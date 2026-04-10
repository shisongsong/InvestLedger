package com.investledger.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * 交易记录数据访问对象
 */
@Dao
interface TransactionDao {
    
    /**
     * 获取所有交易记录（按时间倒序）
     */
    @Query("SELECT * FROM transactions ORDER BY createdAt DESC")
    fun getAllTransactions(): Flow<List<Transaction>>
    
    /**
     * 根据ID获取交易记录
     */
    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): Transaction?
    
    /**
     * 插入交易记录
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long
    
    /**
     * 删除交易记录
     */
    @Delete
    suspend fun deleteTransaction(transaction: Transaction)
    
    /**
     * 更新交易记录
     */
    @Update
    suspend fun updateTransaction(transaction: Transaction)
    
    /**
     * 获取交易次数
     */
    @Query("SELECT COUNT(*) FROM transactions")
    fun getTransactionCount(): Flow<Int>
    
    /**
     * 获取总收益
     */
    @Query("SELECT COALESCE(SUM(profit), 0.0) FROM transactions")
    fun getTotalProfit(): Flow<Double>
    
    /**
     * 获取盈利次数
     */
    @Query("SELECT COUNT(*) FROM transactions WHERE profit > 0")
    fun getWinCount(): Flow<Int>
    
    /**
     * 获取亏损次数
     */
    @Query("SELECT COUNT(*) FROM transactions WHERE profit < 0")
    fun getLossCount(): Flow<Int>
    
    /**
     * 获取总盈利
     */
    @Query("SELECT COALESCE(SUM(profit), 0.0) FROM transactions WHERE profit > 0")
    fun getTotalWin(): Flow<Double>
    
    /**
     * 获取总亏损
     */
    @Query("SELECT COALESCE(SUM(profit), 0.0) FROM transactions WHERE profit < 0")
    fun getTotalLoss(): Flow<Double>
}