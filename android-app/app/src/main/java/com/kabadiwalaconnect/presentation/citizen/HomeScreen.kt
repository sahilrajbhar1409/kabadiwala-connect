package com.kabadiwalaconnect.presentation.citizen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
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
import com.kabadiwalaconnect.navigation.Routes
import com.kabadiwalaconnect.ui.components.*
import com.kabadiwalaconnect.ui.theme.*

@Composable
fun HomeScreen(nav: NavHostController) {
    Scaffold(
        containerColor = Cream,
        bottomBar = { BottomBar(nav, Routes.HOME) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 25.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Good morning 👋", color = TextMuted, fontSize = 14.sp)
                        Text("Sahil", style = MaterialTheme.typography.headlineMedium)
                    }
                    Box(
                        modifier = Modifier.size(46.dp).clip(CircleShape).background(GreenLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("S", color = Green, fontWeight = FontWeight.Bold)
                    }
                }
            }
            item {
                Surface(shape = RoundedCornerShape(22.dp), color = Green) {
                    Column(modifier = Modifier.padding(22.dp)) {
                        Text(
                            "Turn recyclables into value.",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Schedule a pickup from a nearby collector.",
                            color = Color.White.copy(alpha = .85f)
                        )
                        Spacer(Modifier.height(18.dp))
                        Button(
                            onClick = { nav.navigate(Routes.PICKUP) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Green
                            ),
                            shape = RoundedCornerShape(13.dp)
                        ) {
                            Text("Request pickup", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            item {
                Text("Quick actions", style = MaterialTheme.typography.titleLarge)
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickAction(
                        "♻", "Sell recyclables", "Get value", Modifier.width(155.dp)
                    ) { nav.navigate(Routes.PICKUP) }
                    QuickAction(
                        "📍", "Nearby collectors", "Find someone", Modifier.width(155.dp)
                    ) { nav.navigate(Routes.NEARBY) }
                    QuickAction(
                        "📋", "My history", "View activity", Modifier.width(155.dp)
                    ) { nav.navigate(Routes.HISTORY) }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Active pickup", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "View",
                        color = Green,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { nav.navigate(Routes.TRACKING) }
                    )
                }
            }
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth().clickable { nav.navigate(Routes.TRACKING) },
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White,
                    tonalElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(48.dp).clip(CircleShape).background(GreenLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🚚", fontSize = 23.sp)
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Pickup scheduled", fontWeight = FontWeight.Bold)
                            Text("Today · 5:30 PM", color = TextMuted, fontSize = 13.sp)
                        }
                        Text("Track →", color = Green, fontWeight = FontWeight.Bold)
                    }
                }
            }
            item {
                Text("Recent activity", style = MaterialTheme.typography.titleLarge)
            }
            item { ActivityRow("Paper", "8.5 kg", "170", "Yesterday") }
            item { ActivityRow("Plastic", "5.2 kg", "156", "28 Aug") }
            item { ActivityRow("Metal", "3.0 kg", "210", "24 Aug") }
        }
    }
}
