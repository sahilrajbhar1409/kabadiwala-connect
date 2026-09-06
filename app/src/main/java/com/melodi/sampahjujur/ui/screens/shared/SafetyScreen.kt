package com.melodi.sampahjujur.ui.screens.shared

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import java.util.Locale

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SafetyScreen(navController: NavHostController) {
    val context = LocalContext.current
    val title = "Safety guidance"
    val tips = listOf(
        "Wear gloves and keep recyclable materials dry.",
        "Do not handle damaged batteries or exposed wiring without professional help."
    )
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var ready by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        val engine = TextToSpeech(context) { status ->
            ready = status == TextToSpeech.SUCCESS
        }
        tts = engine
        onDispose {
            engine.stop()
            engine.shutdown()
            tts = null
            ready = false
        }
    }
    LaunchedEffect(ready) {
        if (ready) tts?.language = Locale.getDefault()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = navController::popBackStack) {
                        Text("<")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                enabled = ready,
                onClick = { tts?.speak(tips.joinToString(" "), TextToSpeech.QUEUE_FLUSH, null, "safety") }
            ) {
                Text("Read safety tips aloud")
            }
            tips.forEach { Text(it) }
        }
    }
}
