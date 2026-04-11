package com.investledger.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * 持仓数据访问对象
 */
@Dao
interface PositionDao {
    
    /**
     * 获取所有持仓（按创建时间倒序）
     */
    @Query("SELECT * FROM positions ORDER BY createdAt DESC")
    fun getAllPositions(): Flow<List<Position>>
    
    /**
     * 根据ID获取持仓
     */
    @Query("SELECT * FROM positions WHERE id = :id")
    suspend fun getPositionById(id: Long): Position?
    
    /**
     * 插入持仓
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosition(position: Position): Long
    
    /**
     * 更新持仓
     */
    @Update
    suspend fun updatePosition(position: Position)
    
    /**
     * 删除持仓
     */
    @Delete
    suspend fun deletePosition(position: Position)
    
    /**
     * 获取持仓数量
     */
    @Query("SELECT COUNT(*) FROM positions")
    fun getPositionCount(): Flow<Int>
    
    /**
     * 获取总成本
     */
    @Query("SELECT COALESCE(SUM(costPrice * quantity), 0.0) FROM positions")
    fun getTotalCost(): Flow<Double>
    
    /**
     * 获取所有不重复的投资名称和类型（用于自动补全）
     */
    @Query("SELECT DISTINCT name, type FROM positions ORDER BY createdAt DESC LIMIT 50")
    suspend fun getDistinctNames(): List<NameTypePair>
    
    /**
     * 根据名称查找持仓（忽略大小写，用于加仓检测）
     */
    @Query("SELECT * FROM positions WHERE LOWER(name) = LOWER(:name) ORDER BY createdAt DESC LIMIT 1")
    suspend fun getPositionByName(name: String): Position?
    
    /**
     * 更新持仓当前价格
     */
    @Query("UPDATE positions SET currentPrice = :price WHERE id = :id")
    suspend fun updateCurrentPrice(id: Long, price: Double)
}

/**
 * 名称和类型的组合，用于自动补全建议
 */
data class NameTypePair(
    val name: String,
    val type: String
)