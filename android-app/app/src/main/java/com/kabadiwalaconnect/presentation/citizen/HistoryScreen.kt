package com.kabadiwalaconnect.presentation.citizen

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.kabadiwalaconnect.navigation.Routes
import com.kabadiwalaconnect.ui.components.*
import com.kabadiwalaconnect.ui.theme.Cream

@Composable
fun HistoryScreen(nav: NavHostController) {
    var filter by remember { mutableStateOf("All") }

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
            items(
                listOf(
                    Triple("Paper", "8.5 kg", "170"),
                    Triple("Plastic", "5.2 kg", "156"),
                    Triple("Metal", "3.0 kg", "210"),
                    Triple("Cardboard", "11 kg", "132"),
                    Triple("E-waste", "2.4 kg", "480")
                )
            ) { item -> HistoryCard(item.first, item.second, item.third) }
        }
    }
}
