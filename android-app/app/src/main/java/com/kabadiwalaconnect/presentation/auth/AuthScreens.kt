package com.kabadiwalaconnect.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.kabadiwalaconnect.data.SessionState
import com.kabadiwalaconnect.data.model.UserRole
import com.kabadiwalaconnect.navigation.Routes
import com.kabadiwalaconnect.ui.theme.*

@Composable
fun SplashScreen(nav: NavHostController) {
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1300)
        nav.navigate(Routes.ONBOARDING) {
            popUpTo(Routes.SPLASH) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Green),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text("♻", fontSize = 52.sp)
            }

            Spacer(Modifier.height(20.dp))
            Text(
                "KABADIWALA",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            )
            Text(
                "CONNECT",
                color = Color.White.copy(alpha = .9f),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 5.sp
            )
            Spacer(Modifier.height(14.dp))
            Text(
                "Waste to value. Together.",
                color = Color.White.copy(alpha = .8f),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun OnboardingScreen(nav: NavHostController) {
    var page by remember { mutableIntStateOf(0) }

    val titles = listOf(
        "Give your waste a second life",
        "Connect with local collectors",
        "Track every pickup"
    )
    val descriptions = listOf(
        "Sell recyclable materials responsibly and keep valuable waste in the recycling chain.",
        "Find trusted kabadiwalas and nearby collection options from one simple app.",
        "Know when your pickup is scheduled, collected and completed."
    )
    val icons = listOf("♻", "📍", "✓")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .padding(24.dp)
    ) {
        Spacer(Modifier.height(30.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                "Skip",
                color = TextMuted,
                modifier = Modifier.clickable { nav.navigate(Routes.LOGIN) }
            )
        }
        Spacer(Modifier.height(50.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(270.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(GreenLight),
            contentAlignment = Alignment.Center
        ) {
            Text(icons[page], fontSize = 110.sp)
        }
        Spacer(Modifier.height(35.dp))
        Text(titles[page], style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        Text(descriptions[page], color = TextMuted, lineHeight = 23.sp)
        Spacer(Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .height(7.dp)
                        .width(if (it == page) 28.dp else 7.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (it == page) Green else Border)
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                if (page < 2) page++ else nav.navigate(Routes.LOGIN)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                if (page < 2) "Continue" else "Get Started",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun LoginScreen(nav: NavHostController) {
    var phone by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.CITIZEN) }

    AuthContainer(
        title = "Welcome back 👋",
        subtitle = "Sign in to manage your recyclable waste."
    ) {
        Text("Mobile number", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = phone,
            onValueChange = {
                if (it.length <= 10) phone = it.filter { c -> c.isDigit() }
            },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Text("+91", modifier = Modifier.padding(start = 8.dp)) },
            placeholder = { Text("Enter mobile number") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            shape = RoundedCornerShape(14.dp)
        )
        Spacer(Modifier.height(18.dp))
        Text("Sign in as", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = selectedRole == UserRole.CITIZEN,
                onClick = { selectedRole = UserRole.CITIZEN },
                label = { Text("Citizen") }
            )
            FilterChip(
                selected = selectedRole == UserRole.COLLECTOR,
                onClick = { selectedRole = UserRole.COLLECTOR },
                label = { Text("Collector") }
            )
            FilterChip(
                selected = selectedRole == UserRole.RECYCLER,
                onClick = { selectedRole = UserRole.RECYCLER },
                label = { Text("Recycler") }
            )
        }
        Spacer(Modifier.height(18.dp))
        Button(
            onClick = {
                SessionState.signInAs(selectedRole)
                nav.navigate(selectedRole.homeRoute())
            },
            enabled = phone.length >= 10,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(15.dp)
        ) {
            Text("Continue")
        }
        Spacer(Modifier.height(25.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(Modifier.weight(1f))
            Text("  OR  ", color = TextMuted)
            HorizontalDivider(Modifier.weight(1f))
        }
        Spacer(Modifier.height(20.dp))
        OutlinedButton(
            onClick = {
                SessionState.signInAs(selectedRole)
                nav.navigate(selectedRole.homeRoute())
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(15.dp)
        ) {
            Text("Continue with Google")
        }

        Spacer(Modifier.height(25.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text("New here? ", color = TextMuted)
            Text(
                "Create account",
                color = Green,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { nav.navigate(Routes.REGISTER) }
            )
        }
    }
}

private fun UserRole.homeRoute(): String = when (this) {
    UserRole.COLLECTOR -> Routes.COLLECTOR_DASHBOARD
    UserRole.RECYCLER -> Routes.RECYCLER_DASHBOARD
    else -> Routes.HOME
}

@Composable
fun RegisterScreen(nav: NavHostController) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    AuthContainer(
        title = "Create your account",
        subtitle = "Start making recycling easier in your neighbourhood."
    ) {
        Text("Full name", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(7.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Enter your name") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )
        Spacer(Modifier.height(15.dp))
        Text("Mobile number", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(7.dp))
        OutlinedTextField(
            value = phone,
            onValueChange = {
                if (it.length <= 10) phone = it.filter { c -> c.isDigit() }
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("10-digit mobile number") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            shape = RoundedCornerShape(14.dp)
        )
        Spacer(Modifier.height(22.dp))
        Button(
            onClick = {
                SessionState.signInAs(UserRole.CITIZEN)
                nav.navigate(Routes.HOME)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(15.dp)
        ) {
            Text("Create account")
        }
    }
}

@Composable
fun AuthContainer(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .padding(24.dp)
    ) {
        Spacer(Modifier.height(55.dp))
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(CircleShape)
                .background(GreenLight),
            contentAlignment = Alignment.Center
        ) {
            Text("♻", fontSize = 30.sp)
        }
        Spacer(Modifier.height(25.dp))
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text(subtitle, color = TextMuted, lineHeight = 22.sp)
        Spacer(Modifier.height(32.dp))
        content()
    }
}
