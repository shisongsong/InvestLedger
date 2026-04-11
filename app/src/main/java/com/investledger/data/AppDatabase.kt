package com.investledger.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * 应用数据库
 */
@Database(
    entities = [Position::class, Transaction::class, StatisticsSnapshot::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun positionDao(): PositionDao
    abstract fun transactionDao(): TransactionDao
    abstract fun statisticsDao(): StatisticsDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        /**
         * 数据库迁移 2 -> 3：添加统计快照表
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 创建统计快照表
                database.execSQL("""
                    CREATE TABLE statistics (
                        id INTEGER PRIMARY KEY NOT NULL DEFAULT 1,
                        totalCost REAL NOT NULL DEFAULT 0.0,
                        totalProfit REAL NOT NULL DEFAULT 0.0,
                        winCount INTEGER NOT NULL DEFAULT 0,
                        lossCount INTEGER NOT NULL DEFAULT 0,
                        totalWin REAL NOT NULL DEFAULT 0.0,
                        totalLoss REAL NOT NULL DEFAULT 0.0,
                        transactionCount INTEGER NOT NULL DEFAULT 0,
                        lastCalculatedAt INTEGER NOT NULL DEFAULT 0,
                        version INTEGER NOT NULL DEFAULT 0
                    )
                """)
                
                // 插入初始记录（单例）
                database.execSQL("INSERT INTO statistics (id) VALUES (1)")
            }
        }
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "invest_ledger_db"
                )
                    .addMigrations(MIGRATION_2_3)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
