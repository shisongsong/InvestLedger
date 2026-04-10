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
    val name: String,
    
    /** 投资类型：股票、基金、加密货币等 */
    val type: String,
    
    /** 成本价 */
    val costPrice: Double,
    
    /** 持有数量 */
    val quantity: Double,
    
    /** 创建时间 */
    val createdAt: Long = System.currentTimeMillis(),
    
    /** 备注 */
    val note: String = ""
) {
    /**
     * 计算总成本
     */
    val totalCost: Double
        get() = costPrice * quantity
    
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
}