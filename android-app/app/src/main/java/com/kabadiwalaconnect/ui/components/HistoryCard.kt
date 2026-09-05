package com.kabadiwalaconnect.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kabadiwalaconnect.ui.theme.*

@Composable
fun HistoryCard(material: String, quantity: String, amount: String) {
    Surface(shape = RoundedCornerShape(18.dp), color = Color.White) {
        Column(modifier = Modifier.padding(17.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(material, fontWeight = FontWeight.Bold)
                    Text(quantity, color = TextMuted, fontSize = 13.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(amount, color = Green, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text("Completed", color = Green, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Border)
            Spacer(Modifier.height(10.dp))
            Text(
                "Pickup completed · Payment recorded",
                color = TextMuted,
                fontSize = 12.sp
            )
        }
    }
}
