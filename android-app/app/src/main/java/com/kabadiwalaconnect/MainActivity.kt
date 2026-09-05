package com.kabadiwalaconnect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.kabadiwalaconnect.navigation.AppNavigation
import com.kabadiwalaconnect.ui.theme.KabadiwalaConnectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KabadiwalaConnectTheme {
                AppNavigation()
            }
        }
    }
}
