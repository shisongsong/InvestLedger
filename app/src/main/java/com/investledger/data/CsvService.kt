package com.investledger.data

import android.content.Context
import android.net.Uri
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * CSV 导出/导入服务
 */
class CsvService(private val context: Context) {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    
    // ========== 导出功能 ==========
    
    /**
     * 导出持仓和交易记录到 CSV
     */
    suspend fun exportData(
        positions: List<Position>,
        transactions: List<Transaction>,
        uri: Uri
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    // 写入持仓数据
                    writer.appendLine("=== Positions ===")
                    writer.appendLine("ID,Name,Type,CostPrice,Quantity,CreatedAt,Note")
                    positions.forEach { pos ->
                        val createdAtStr = dateFormat.format(Date(pos.createdAt))
                        writer.appendLine(
                            "${pos.id},${escapeCsv(pos.name)},${escapeCsv(pos.type)},${pos.costPrice},${pos.quantity},$createdAtStr,${escapeCsv(pos.note)}"
                        )
                    }
                    
                    writer.appendLine()
                    
                    // 写入交易数据
                    writer.appendLine("=== Transactions ===")
                    writer.appendLine("ID,PositionId,Name,Type,CostPrice,SellPrice,Quantity,Profit,ProfitRate,CreatedAt")
                    transactions.forEach { tx ->
                        val createdAtStr = dateFormat.format(Date(tx.createdAt))
                        writer.appendLine(
                            "${tx.id},${tx.positionId},${escapeCsv(tx.name)},${escapeCsv(tx.type)},${tx.costPrice},${tx.sellPrice},${tx.quantity},${tx.profit},${tx.profitRate},$createdAtStr"
                        )
                    }
                    
                    writer.appendLine()
                    writer.appendLine("=== Export Info ===")
                    writer.appendLine("Positions Count,${positions.size}")
                    writer.appendLine("Transactions Count,${transactions.size}")
                    writer.appendLine("Export Date,${dateFormat.format(Date())}")
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 从 CSV 导入数据
     */
    suspend fun importData(uri: Uri): Result<ImportResult> = withContext(Dispatchers.IO) {
        try {
            val positions = mutableListOf<Position>()
            val transactions = mutableListOf<Transaction>()
            
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var section = ""
                    var headerRead = false
                    
                    reader.forEachLine { line ->
                        when {
                            line.startsWith("=== Positions ===") -> {
                                section = "positions"
                                headerRead = false
                            }
                            line.startsWith("=== Transactions ===") -> {
                                section = "transactions"
                                headerRead = false
                            }
                            line.startsWith("=== Export Info ===") -> section = "info"
                            line.isBlank() -> {}
                            section == "positions" -> {
                                if (!headerRead) {
                                    headerRead = true
                                } else {
                                    val parts = parseCsvLine(line)
                                    if (parts.size >= 7) {
                                        positions.add(
                                            Position(
                                                id = parts[0].toLongOrNull() ?: 0,
                                                name = parts[1],
                                                type = parts[2],
                                                costPrice = parts[3].toDoubleOrNull() ?: 0.0,
                                                quantity = parts[4].toDoubleOrNull() ?: 0.0,
                                                createdAt = parseDate(parts[5]),
                                                note = parts[6]
                                            )
                                        )
                                    }
                                }
                            }
                            section == "transactions" -> {
                                if (!headerRead) {
                                    headerRead = true
                                } else {
                                    val parts = parseCsvLine(line)
                                    if (parts.size >= 10) {
                                        transactions.add(
                                            Transaction(
                                                id = parts[0].toLongOrNull() ?: 0,
                                                positionId = parts[1].toLongOrNull() ?: 0,
                                                name = parts[2],
                                                type = parts[3],
                                                costPrice = parts[4].toDoubleOrNull() ?: 0.0,
                                                sellPrice = parts[5].toDoubleOrNull() ?: 0.0,
                                                quantity = parts[6].toDoubleOrNull() ?: 0.0,
                                                profit = parts[7].toDoubleOrNull() ?: 0.0,
                                                profitRate = parts[8].toDoubleOrNull() ?: 0.0,
                                                createdAt = parseDate(parts[9])
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            Result.success(ImportResult(positions, transactions))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 生成导出文件名
     */
    fun generateExportFileName(): String {
        val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return "InvestLedger_Export_$dateStr.csv"
    }
    
    // ========== 辅助方法 ==========
    
    private fun escapeCsv(value: String): String {
        if (value.contains(",") || value.contains(""") || value.contains("\n")) {
            return """ + value.replace(""", """") + """
        }
        return value
    }
    
    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        
        for (char in line) {
            when {
                char == '"' -> inQuotes = !inQuotes
                char == ',' && !inQuotes -> {
                    result.add(sb.toString().trim())
                    sb.clear()
                }
                else -> sb.append(char)
            }
        }
        result.add(sb.toString().trim())
        return result
    }
    
    private fun parseDate(dateStr: String): Long {
        return try {
            dateFormat.parse(dateStr)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
    
    data class ImportResult(
        val positions: List<Position>,
        val transactions: List<Transaction>
    )
}
