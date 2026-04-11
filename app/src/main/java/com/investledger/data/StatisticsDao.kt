package com.investledger.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * 统计数据访问对象
 * 提供统计快照的 CRUD 操作和全量计算查询
 */
@Dao
interface StatisticsDao {
    
    /**
     * 获取统计快照（响应式 Flow）
     */
    @Query("SELECT * FROM statistics WHERE id = 1")
    fun getStatistics(): Flow<StatisticsSnapshot>
    
    /**
     * 获取统计快照（挂起函数，用于同步操作）
     */
    @Query("SELECT * FROM statistics WHERE id = 1")
    suspend fun getStatisticsSync(): StatisticsSnapshot?
    
    /**
     * 更新统计快照（单例 upsert）
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateStatistics(stats: StatisticsSnapshot)
    
    // ========== 全量计算查询（用于初始化和校验） ==========
    
    /**
     * 计算总收益
     */
    @Query("SELECT COALESCE(SUM(profit), 0.0) FROM transactions")
    suspend fun calculateTotalProfit(): Double
    
    /**
     * 计算盈利次数
     */
    @Query("SELECT COUNT(*) FROM transactions WHERE profit > 0")
    suspend fun calculateWinCount(): Int
    
    /**
     * 计算亏损次数
     */
    @Query("SELECT COUNT(*) FROM transactions WHERE profit < 0")
    suspend fun calculateLossCount(): Int
    
    /**
     * 计算总盈利
     */
    @Query("SELECT COALESCE(SUM(profit), 0.0) FROM transactions WHERE profit > 0")
    suspend fun calculateTotalWin(): Double
    
    /**
     * 计算总亏损
     */
    @Query("SELECT COALESCE(SUM(profit), 0.0) FROM transactions WHERE profit < 0")
    suspend fun calculateTotalLoss(): Double
    
    /**
     * 计算交易总数
     */
    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun calculateTransactionCount(): Int
    
    /**
     * 计算总持仓成本
     */
    @Query("SELECT COALESCE(SUM(costPrice * quantity), 0.0) FROM positions")
    suspend fun calculateTotalCost(): Double
}
