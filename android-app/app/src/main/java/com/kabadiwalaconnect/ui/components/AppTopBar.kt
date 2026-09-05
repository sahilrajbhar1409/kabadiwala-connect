package com.kabadiwalaconnect.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.kabadiwalaconnect.ui.theme.Cream

@Composable
fun AppTopBar(nav: NavHostController, title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Cream)
            .padding(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 10.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        IconButton(onClick = { nav.popBackStack() }) {
            Icon(Icons.Default.ArrowBack, null)
        }
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}
