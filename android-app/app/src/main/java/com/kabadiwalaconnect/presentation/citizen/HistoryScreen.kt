package com.kabadiwalaconnect.presentation.citizen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.kabadiwalaconnect.data.model.CollectionRequest
import com.kabadiwalaconnect.data.model.CollectionRequestStatus
import com.kabadiwalaconnect.data.repository.CollectionRepositoryProvider
import com.kabadiwalaconnect.data.repository.PriceServiceProvider
import com.kabadiwalaconnect.navigation.Routes
import com.kabadiwalaconnect.ui.components.*
import com.kabadiwalaconnect.ui.theme.*
import java.util.Locale

@Composable
fun HistoryScreen(nav: NavHostController) {
    var filter by remember { mutableStateOf("All") }
    val repository = CollectionRepositoryProvider.instance
    val requests = repository.getCollectionRequests().asReversed()
    val filteredRequests = requests.filter { request ->
        when (filter) {
            "Completed" -> request.status == CollectionRequestStatus.COMPLETED
            "Pending" -> request.status != CollectionRequestStatus.COMPLETED &&
                request.status != CollectionRequestStatus.CANCELLED &&
                request.status != CollectionRequestStatus.REJECTED
            else -> true
        }
    }

    Scaffold(
        containerColor = Cream,
        topBar = { AppTopBar(nav, "My activity") },
        bottomBar = { BottomBar(nav, Routes.HISTORY) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Text("Recycling history", style = MaterialTheme.typography.headlineMedium) }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("All", "Completed", "Pending").forEach {
                        FilterChip(
                            selected = filter == it,
                            onClick = { filter = it },
                            label = { Text(it) }
                        )
                    }
                }
            }
            if (filteredRequests.isEmpty()) {
                item {
                    Text(
                        "Your pickup requests will appear here.",
                        color = TextMuted,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            } else {
                items(filteredRequests) { request ->
                    RequestHistoryCard(request) {
                        repository.getLots().find { it.requestId == request.id }?.let { lot ->
                            nav.navigate(Routes.tracking(lot.lotId)) {
                                launchSingleTop = true
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RequestHistoryCard(request: CollectionRequest, onClick: () -> Unit) {
    val material = PriceServiceProvider.instance.findMaterial(request.materialId)
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = Color.White
    ) {
        Column(Modifier.padding(17.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.weight(1f)) {
                    Text(material?.name ?: request.materialId, fontWeight = FontWeight.Bold)
                    Text(
                        "%.2f kg · %s".format(Locale.US, request.estimatedWeight, request.createdAt),
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }
                Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                    Text(
                        "₹%.2f".format(Locale.US, request.estimatedValue),
                        color = Green,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Text(request.status.name, color = Green, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = Border)
            Spacer(Modifier.height(10.dp))
            Text(request.pickupAddress, color = TextMuted, fontSize = 12.sp)
        }
    }
}
