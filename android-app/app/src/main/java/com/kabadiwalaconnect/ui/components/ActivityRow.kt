package com.kabadiwalaconnect.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
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
fun ActivityRow(material: String, quantity: String, amount: String, date: String) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(42.dp).clip(CircleShape).background(GreenLight),
                contentAlignment = Alignment.Center
            ) {
                Text("♻")
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(material, fontWeight = FontWeight.Bold)
                Text("$quantity · $date", color = TextMuted, fontSize = 12.sp)
            }
            Text(amount, color = Green, fontWeight = FontWeight.Bold)
        }
    }
}
