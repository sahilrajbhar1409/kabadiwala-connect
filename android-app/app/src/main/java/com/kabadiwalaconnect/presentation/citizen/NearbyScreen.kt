package com.kabadiwalaconnect.presentation.citizen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.kabadiwalaconnect.ui.components.AppTopBar
import com.kabadiwalaconnect.ui.theme.*

@Composable
fun NearbyScreen(nav: NavHostController) {
    Scaffold(
        containerColor = Cream,
        topBar = { AppTopBar(nav, "Nearby collectors") }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth().height(220.dp),
                    shape = RoundedCornerShape(22.dp),
                    color = GreenLight
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🗺️", fontSize = 65.sp)
                            Text("Nearby collection network", fontWeight = FontWeight.Bold)
                            Text("Live map integration ready", color = TextMuted, fontSize = 13.sp)
                        }
                    }
                }
            }
            item { Text("Available near you", style = MaterialTheme.typography.titleLarge) }
            items(
                listOf(
                    "Green Scrap Centre" to "1.2 km",
                    "City Recycling Point" to "2.1 km",
                    "Eco Collectors" to "3.4 km"
                )
            ) { collector ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White
                ) {
                    Row(
                        modifier = Modifier.padding(17.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(48.dp).clip(CircleShape).background(GreenLight),
                            contentAlignment = Alignment.Center
                        ) { Text("♻") }
                        Spacer(Modifier.width(13.dp))
                        Column(Modifier.weight(1f)) {
                            Text(collector.first, fontWeight = FontWeight.Bold)
                            Text("★ 4.8 · ${collector.second}", color = TextMuted, fontSize = 13.sp)
                        }
                        Text("Available", color = Green, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
