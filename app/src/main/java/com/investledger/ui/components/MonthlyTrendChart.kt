package com.investledger.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.investledger.data.MonthlyStat
import java.text.SimpleDateFormat
import java.util.*

/**
 * 月度收益趋势图
 */
@Composable
fun MonthlyTrendChart(
    monthlyStats: List<MonthlyStat>,
    modifier: Modifier = Modifier,
    chartHeight: androidx.compose.ui.unit.Dp = 200.dp
) {
    if (monthlyStats.isEmpty()) {
        Box(modifier = modifier.height(chartHeight), contentAlignment = Alignment.Center) {
            Text("暂无数据", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    val profits = monthlyStats.map { it.profit }
    val maxProfit = profits.maxOrNull() ?: 0.0
    val minProfit = profits.minOrNull() ?: 0.0
    val absMax = maxOf(maxProfit.absoluteValue, minProfit.absoluteValue, 1.0)

    val greenProfit = Color(0xFF4CAF50)
    val redLoss = Color(0xFFF44336)
    val gridColor = Color(0xFFE0E0E0)
    val axisLabelColor = Color(0xFF9E9E9E)

    Column(modifier = modifier) {
        // 图表区域
        Canvas(modifier = Modifier.fillMaxWidth().height(chartHeight)) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val barWidth = canvasWidth / monthlyStats.size
            val halfBarWidth = barWidth * 0.3f
            val midY = canvasHeight / 2f
            val scale = (canvasHeight / 2f) / absMax.toFloat()

            // 画网格
            drawLine(gridColor, Offset(0f, midY), Offset(canvasWidth, midY), strokeWidth = 1f)
            
            // 画柱状图
            monthlyStats.forEachIndexed { index, stat ->
                val profit = stat.profit.toFloat()
                val x = index * barWidth + (barWidth - halfBarWidth * 2) / 2f
                
                if (profit >= 0) {
                    val barHeight = profit * scale
                    drawRect(
                        color = greenProfit,
                        topLeft = Offset(x, midY - barHeight),
                        size = Size(halfBarWidth * 2, barHeight)
                    )
                } else {
                    val barHeight = profit.absoluteValue * scale
                    drawRect(
                        color = redLoss,
                        topLeft = Offset(x, midY),
                        size = Size(halfBarWidth * 2, barHeight)
                    )
                }
            }
        }

        // 月份标签
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            monthlyStats.forEach { stat ->
                val label = "${stat.month}月"
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = axisLabelColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 图例
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text("盈利", color = greenProfit, style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.width(16.dp))
            Text("亏损", color = redLoss, style = MaterialTheme.typography.labelSmall)
        }
    }
}
