package com.kabadiwalaconnect.presentation.citizen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.kabadiwalaconnect.navigation.Routes
import com.kabadiwalaconnect.ui.components.*
import com.kabadiwalaconnect.ui.theme.*

@Composable
fun ProfileScreen(nav: NavHostController) {
    Scaffold(
        containerColor = Cream,
        topBar = { AppTopBar(nav, "Profile") },
        bottomBar = { BottomBar(nav, Routes.PROFILE) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier.size(86.dp).clip(CircleShape).background(GreenLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("S", color = Green, fontSize = 35.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("Sahil", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("+91 XXXXX XXXXX", color = TextMuted)
                }
            }
            item { ProfileOption(Icons.Default.Person, "Personal information") }
            item { ProfileOption(Icons.Default.LocationOn, "Saved addresses") }
            item { ProfileOption(Icons.Default.Star, "Rewards & impact") }
            item {
                ProfileOption(Icons.Default.Settings, "Settings") {
                    nav.navigate(Routes.SETTINGS)
                }
            }
            item {
                OutlinedButton(
                    onClick = { nav.navigate(Routes.LOGIN) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) { Text("Log out") }
            }
        }
    }
}
