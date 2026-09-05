package com.kabadiwalaconnect.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kabadiwalaconnect.ui.theme.*

@Composable
fun TrackingStep(title: String, time: String, completed: Boolean) {
    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape)
                .background(if (completed) Green else Border),
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (completed) "✓" else "•",
                color = if (completed) Color.White else TextMuted,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(time, color = TextMuted, fontSize = 12.sp)
        }
    }
}
