package com.investledger.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 统计快照实体 - 用于缓存综合计算结果
 * 采用单例模式（id 固定为 1），避免多次插入
 */
@Entity(tableName = "statistics")
data class StatisticsSnapshot(
    @PrimaryKey val id: Int = 1,
    
    /** 总持仓成本 */
    val totalCost: Double = 0.0,
    
    /** 总收益（盈利 + 亏损） */
    val totalProfit: Double = 0.0,
    
    /** 盈利次数 */
    val winCount: Int = 0,
    
    /** 亏损次数 */
    val lossCount: Int = 0,
    
    /** 总盈利金额 */
    val totalWin: Double = 0.0,
    
    /** 总亏损金额 */
    val totalLoss: Double = 0.0,
    
    /** 总交易次数 */
    val transactionCount: Int = 0,
    
    /** 最后计算时间戳 */
    val lastCalculatedAt: Long = 0L,
    
    /** 版本号，用于乐观锁检测 */
    val version: Int = 0
) {
    /**
     * 计算胜率
     */
    val winRate: Double
        get() {
            val total = winCount + lossCount
            return if (total > 0) (winCount.toDouble() / total) * 100 else 0.0
        }
    
    /**
     * 创建空统计快照
     */
    companion object {
        fun empty() = StatisticsSnapshot()
    }
}
