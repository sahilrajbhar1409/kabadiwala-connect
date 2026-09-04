package com.melodi.sampahjujur.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.melodi.sampahjujur.model.PriceHistoryRecord
import com.melodi.sampahjujur.ui.theme.PrimaryGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Jetpack Compose Canvas Line Chart displaying material price history over time.
 */
@Composable
fun PriceHistoryChart(
    history: List<PriceHistoryRecord>,
    modifier: Modifier = Modifier
) {
    if (history.isEmpty()) return

    val sortedHistory = history.sortedBy { it.timestamp }
    val prices = sortedHistory.map { it.buyingPrice }
    val minPrice = (prices.minOrNull() ?: 0.0) * 0.9
    val maxPrice = (prices.maxOrNull() ?: 100.0) * 1.1

    val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFF9F9F9), shape = RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Price History (₹/kg)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = "${sortedHistory.size} Records",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            val width = size.width
            val height = size.height
            val spacing = width / (prices.size - 1).coerceAtLeast(1)

            val points = prices.mapIndexed { index, price ->
                val x = index * spacing
                val priceRatio = (price - minPrice) / (maxPrice - minPrice).coerceAtLeast(1.0)
                val y = height - (priceRatio * height).toFloat()
                Offset(x, y)
            }

            // Draw horizontal grid lines
            val gridLines = 3
            for (i in 0..gridLines) {
                val y = height * (i / gridLines.toFloat())
                drawLine(
                    color = Color.LightGray.copy(alpha = 0.4f),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Draw connecting path line
            val path = Path().apply {
                if (points.isNotEmpty()) {
                    moveTo(points[0].x, points[0].y)
                    for (i in 1 until points.size) {
                        lineTo(points[i].x, points[i].y)
                    }
                }
            }

            drawPath(
                path = path,
                color = PrimaryGreen,
                style = Stroke(width = 3.dp.toPx())
            )

            // Draw data points & values
            points.forEachIndexed { index, point ->
                drawCircle(
                    color = PrimaryGreen,
                    radius = 5.dp.toPx(),
                    center = point
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.5.dp.toPx(),
                    center = point
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // X-Axis Labels (Dates)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            sortedHistory.forEach { record ->
                Text(
                    text = dateFormat.format(Date(record.timestamp)),
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
