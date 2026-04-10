package com.investledger.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 交易记录实体
 */
@Entity(
    tableName = "transactions",
    indices = [Index(value = ["positionId"])]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /** 关联的持仓ID（清仓时） */
    val positionId: Long = 0,
    
    /** 投资名称 */
    val name: String = "",
    
    /** 投资类型 */
    val type: String = "",
    
    /** 成本价 */
    val costPrice: Double = 0.0,
    
    /** 卖出价 */
    val sellPrice: Double = 0.0,
    
    /** 数量 */
    val quantity: Double = 0.0,
    
    /** 收益 */
    val profit: Double = 0.0,
    
    /** 收益率 */
    val profitRate: Double = 0.0,
    
    /** 交易时间 */
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * 格式化收益
     */
    fun formatProfit(): String {
        return if (profit >= 0) {
            "+${String.format("%.2f", profit)}"
        } else {
            String.format("%.2f", profit)
        }
    }
    
    /**
     * 格式化收益率
     */
    fun formatProfitRate(): String {
        return if (profitRate >= 0) {
            "+${String.format("%.2f", profitRate)}%"
        } else {
            "${String.format("%.2f", profitRate)}%"
        }
    }
}