package com.kabadiwalaconnect.presentation.citizen

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.kabadiwalaconnect.R
import com.kabadiwalaconnect.ui.components.AppTopBar
import com.kabadiwalaconnect.ui.theme.Cream
import java.util.Locale

@Composable
fun SafetyScreen(nav: NavHostController) {
    val context = LocalContext.current

    val title = stringResource(id = R.string.safety_title)
    val tip1 = stringResource(id = R.string.safety_tip_1)
    val tip2 = stringResource(id = R.string.safety_tip_2)

    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var ttsReady by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        val engine = TextToSpeech(context) { status ->
            ttsReady = (status == TextToSpeech.SUCCESS)
        }
        tts = engine

        onDispose {
            engine.stop()
            engine.shutdown()
            tts = null
            ttsReady = false
        }
    }

    LaunchedEffect(ttsReady, tts) {
        if (!ttsReady) return@LaunchedEffect

        val deviceLang = Locale.getDefault().language
        val locale = when (deviceLang) {
            "hi" -> Locale.Builder().setLanguage("hi").setRegion("IN").build()
            "mr" -> Locale.Builder().setLanguage("mr").setRegion("IN").build()
            else -> Locale.ENGLISH
        }


        tts?.language = locale
    }

    Scaffold(
        containerColor = Cream,
        topBar = { AppTopBar(nav, title) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                enabled = ttsReady,
                onClick = {
                    val message = "$title. $tip1 $tip2"
                    tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, "safety")
                }
            ) {
                Text("🔊 Speak")
            }

            Text(tip1)
            Text(tip2)
        }
    }
}