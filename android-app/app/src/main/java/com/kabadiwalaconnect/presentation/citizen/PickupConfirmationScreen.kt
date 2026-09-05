package com.kabadiwalaconnect.presentation.citizen

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
import androidx.navigation.NavHostController
import com.kabadiwalaconnect.data.repository.CollectionRepositoryProvider
import com.kabadiwalaconnect.data.repository.PriceServiceProvider
import com.kabadiwalaconnect.navigation.Routes
import com.kabadiwalaconnect.ui.components.AppTopBar
import com.kabadiwalaconnect.ui.theme.*
import java.util.Locale

@Composable
fun PickupConfirmationScreen(nav: NavHostController, lotId: String?) {
    val lot = lotId?.let { CollectionRepositoryProvider.instance.getLot(it) }
    val material = lot?.let { PriceServiceProvider.instance.findMaterial(it.materialId) }

    Scaffold(
        containerColor = Cream,
        topBar = { AppTopBar(nav, "Pickup requested") }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(30.dp))
            Surface(
                modifier = Modifier.size(92.dp),
                shape = RoundedCornerShape(46.dp),
                color = GreenLight
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("✓", color = Green, fontSize = 48.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(22.dp))
            Text("Your pickup is requested", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            Text(
                "A collector will be matched with your request shortly.",
                color = TextMuted
            )
            Spacer(Modifier.height(22.dp))
            Surface(shape = RoundedCornerShape(20.dp), color = Color.White) {
                Column(
                    Modifier.fillMaxWidth().padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ConfirmationRow("Lot ID", lot?.lotId ?: "Unavailable")
                    ConfirmationRow("Material", material?.name ?: "Unavailable")
                    ConfirmationRow(
                        "Estimated value",
                        lot?.let { "₹%.2f".format(Locale.US, it.estimatedValue) } ?: "Unavailable"
                    )
                    ConfirmationRow("Status", "REQUESTED")
                }
            }
            Spacer(Modifier.weight(1f))
            Button(
                onClick = {
                    lot?.lotId?.let { id ->
                        nav.navigate(Routes.tracking(id)) {
                            launchSingleTop = true
                            popUpTo(Routes.HOME)
                        }
                    }
                },
                enabled = lot != null,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(15.dp)
            ) {
                Text("Track pickup", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = {
                    nav.navigate(Routes.HISTORY) {
                        launchSingleTop = true
                        popUpTo(Routes.HOME)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(15.dp)
            ) {
                Text("View history")
            }
        }
    }
}

@Composable
private fun ConfirmationRow(label: String, value: String) {
    Column {
        Text(label, color = TextMuted, fontSize = 12.sp)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}
