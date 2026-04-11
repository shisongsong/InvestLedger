package com.investledger.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 持仓实体
 */
@Entity(tableName = "positions")
data class Position(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /** 投资名称/代码 */
    val name: String = "",
    
    /** 投资类型：股票、基金、加密货币等 */
    val type: String = "",
    
    /** 成本价 */
    val costPrice: Double = 0.0,
    
    /** 持有数量 */
    val quantity: Double = 0.0,
    
    /** 创建时间 */
    val createdAt: Long = System.currentTimeMillis(),
    
    /** 备注 */
    val note: String = "",
    
    /** 当前价格（手动设置，用于计算浮盈浮亏） */
    val currentPrice: Double = 0.0
) {
    /**
     * 计算总成本
     */
    val totalCost: Double
        get() = if (costPrice != 0.0 && quantity != 0.0) costPrice * quantity else 0.0
    
    /**
     * 计算当前市值
     */
    val marketValue: Double
        get() = if (currentPrice > 0 && quantity > 0) currentPrice * quantity else 0.0
    
    /**
     * 计算浮动盈亏
     */
    val floatingProfit: Double
        get() = if (currentPrice > 0) (currentPrice - costPrice) * quantity else 0.0
    
    /**
     * 计算浮动盈亏比例
     */
    val floatingProfitRate: Double
        get() = if (costPrice > 0 && currentPrice > 0) ((currentPrice - costPrice) / costPrice) * 100 else 0.0
    
    /**
     * 格式化显示
     */
    fun formatQuantity(): String {
        return if (quantity == quantity.toLong().toDouble()) {
            quantity.toLong().toString()
        } else {
            String.format("%.2f", quantity)
        }
    }
    
    fun formatPrice(): String = String.format("%.2f", costPrice)
    fun formatCurrentPrice(): String = if (currentPrice > 0) String.format("%.2f", currentPrice) else "--"
}
