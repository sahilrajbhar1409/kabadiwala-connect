package com.kabadiwalaconnect


import androidx.compose.runtime.LaunchedEffect
import android.speech.tts.TextToSpeech
import java.util.Locale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.res.stringResource
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavHostController


private val Green = Color(0xFF197A45)
private val GreenDark = Color(0xFF0E5A31)
private val GreenLight = Color(0xFFE8F5EC)
private val Cream = Color(0xFFF8FAF7)
private val TextDark = Color(0xFF17231B)
private val TextMuted = Color(0xFF68736B)
private val Border = Color(0xFFE0E6E1)
private val Orange = Color(0xFFE9892F)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KabadiwalaTheme {
                KabadiwalaApp()
            }
        }
    }
}

@Composable
fun KabadiwalaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Green,
            onPrimary = Color.White,
            secondary = GreenDark,
            background = Cream,
            surface = Color.White,
            onBackground = TextDark,
            onSurface = TextDark
        ),
        typography = Typography(
            headlineLarge = LocalTextStyle.current.copy(
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            ),
            headlineMedium = LocalTextStyle.current.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            ),
            titleLarge = LocalTextStyle.current.copy(
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            ),
            bodyLarge = LocalTextStyle.current.copy(
                fontSize = 16.sp
            )
        ),
        content = content
    )
}

@Composable
fun KabadiwalaApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") { SplashScreen(navController) }
        composable("onboarding") { OnboardingScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
        composable("home") { HomeScreen(navController) }
        composable("pickup") { PickupScreen(navController) }
        composable("tracking") { TrackingScreen(navController) }
        composable("history") { HistoryScreen(navController) }
        composable("profile") { ProfileScreen(navController) }
        composable("nearby") { NearbyScreen(navController) }
        composable("settings") { SettingsScreen(navController) }
        composable("safety") { SafetyScreen(navController) }
    }
}

@Composable
fun SplashScreen(nav: NavHostController) {
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1300)
        nav.navigate("onboarding") {
            popUpTo("splash") { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Green),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                modifier = Modifier.clickable {
                    nav.navigate("login")
                }
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
            Text(
                icons[page],
                fontSize = 110.sp
            )
        }

        Spacer(Modifier.height(35.dp))

        Text(
            titles[page],
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(12.dp))

        Text(
            descriptions[page],
            color = TextMuted,
            lineHeight = 23.sp
        )

        Spacer(Modifier.weight(1f))

        Row(
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
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
                if (page < 2) page++ else nav.navigate("login")
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

    AuthContainer(
        title = "Welcome back 👋",
        subtitle = "Sign in to manage your recyclable waste."
    ) {
        Text(
            "Mobile number",
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = {
                if (it.length <= 10) phone = it.filter { c -> c.isDigit() }
            },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Text("+91", modifier = Modifier.padding(start = 8.dp))
            },
            placeholder = { Text("Enter mobile number") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone
            ),
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(Modifier.height(18.dp))

        Button(
            onClick = { nav.navigate("home") },
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
            onClick = { nav.navigate("home") },
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
                modifier = Modifier.clickable {
                    nav.navigate("register")
                }
            )
        }
    }
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
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone
            ),
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(Modifier.height(22.dp))

        Button(
            onClick = { nav.navigate("home") },
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

        Text(
            subtitle,
            color = TextMuted,
            lineHeight = 22.sp
        )

        Spacer(Modifier.height(32.dp))

        content()
    }
}

@Composable
fun HomeScreen(nav: NavHostController) {
    Scaffold(
        containerColor = Cream,
        bottomBar = { BottomBar(nav, "home") }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 20.dp,
                bottom = 25.dp
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Good morning 👋",
                            color = TextMuted,
                            fontSize = 14.sp
                        )
                        Text(
                            "Sahil",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(GreenLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("S", color = Green, fontWeight = FontWeight.Bold)
                    }
                }
            }

            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = Green
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp)
                    ) {
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
                            onClick = { nav.navigate("pickup") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Green
                            ),
                            shape = RoundedCornerShape(13.dp)
                        ) {
                            Text(
                                "Request pickup",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    "Quick actions",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickAction(
                        "♻",
                        "Sell recyclables",
                        "Get value",
                        Modifier.width(155.dp)
                    ) { nav.navigate("pickup") }

                    QuickAction(
                        "📍",
                        "Nearby collectors",
                        "Find someone",
                        Modifier.width(155.dp)
                    ) { nav.navigate("nearby") }

                    QuickAction(
                        "📋",
                        "My history",
                        "View activity",
                        Modifier.width(155.dp)
                    ) { nav.navigate("history") }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Active pickup",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        "View",
                        color = Green,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            nav.navigate("tracking")
                        }
                    )
                }
            }

            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { nav.navigate("tracking") },
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White,
                    tonalElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(GreenLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🚚", fontSize = 23.sp)
                        }

                        Spacer(Modifier.width(14.dp))

                        Column(Modifier.weight(1f)) {
                            Text(
                                "Pickup scheduled",
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Today · 5:30 PM",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }

                        Text(
                            "Track →",
                            color = Green,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            item {
                Text(
                    "Recent activity",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            item {
                ActivityRow("Paper", "8.5 kg", "170", "Yesterday")
            }

            item {
                ActivityRow("Plastic", "5.2 kg", "156", "28 Aug")
            }

            item {
                ActivityRow("Metal", "3.0 kg", "210", "24 Aug")
            }
        }
    }
}

@Composable
fun QuickAction(
    icon: String,
    title: String,
    subtitle: String,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(17.dp)
        ) {
            Text(icon, fontSize = 27.sp)
            Spacer(Modifier.height(12.dp))
            Text(title, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, color = TextMuted, fontSize = 12.sp)
        }
    }
}

@Composable
fun ActivityRow(
    material: String,
    quantity: String,
    amount: String,
    date: String
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(GreenLight),
                contentAlignment = Alignment.Center
            ) {
                Text("♻")
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(material, fontWeight = FontWeight.Bold)
                Text("$quantity · $date", color = TextMuted, fontSize = 12.sp)
            }

            Text(
                amount,
                color = Green,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PickupScreen(nav: NavHostController) {
    var selectedMaterial by remember { mutableStateOf("Paper") }
    var quantity by remember { mutableStateOf("") }

    Scaffold(
        containerColor = Cream,
        topBar = {
            AppTopBar(nav, "Request pickup")
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Text(
                    "What do you want to recycle?",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf("Paper", "Plastic", "Metal", "E-waste").forEach {
                        FilterChip(
                            selected = selectedMaterial == it,
                            onClick = { selectedMaterial = it },
                            label = { Text(it) }
                        )
                    }
                }
            }

            item {
                Text(
                    "Estimated quantity",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            item {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter quantity in kg") },
                    trailingIcon = { Text("kg") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )
            }

            item {
                Text(
                    "Pickup address",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            item {
                Surface(
                    shape = RoundedCornerShape(17.dp),
                    color = Color.White
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(17.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Green
                        )

                        Spacer(Modifier.width(12.dp))

                        Column(Modifier.weight(1f)) {
                            Text("Home", fontWeight = FontWeight.Bold)
                            Text(
                                "Your saved pickup address",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }

                        Text(
                            "Change",
                            color = Green,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            item {
                Text(
                    "Preferred time",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf("Today", "Tomorrow", "Weekend").forEach {
                        OutlinedButton(
                            onClick = {},
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(13.dp)
                        ) {
                            Text(it)
                        }
                    }
                }
            }

            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = GreenLight
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp)
                    ) {
                        Text("💡")
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Keep recyclable materials separated and dry for better recovery.",
                            color = GreenDark,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = { nav.navigate("tracking") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(15.dp)
                ) {
                    Text(
                        "Confirm pickup",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun TrackingScreen(nav: NavHostController) {
    Scaffold(
        containerColor = Cream,
        topBar = {
            AppTopBar(nav, "Pickup tracking")
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                shape = RoundedCornerShape(24.dp),
                color = GreenLight
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📍", fontSize = 65.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Collector is on the way",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            "Map integration ready",
                            color = TextMuted
                        )
                    }
                }
            }

            Spacer(Modifier.height(25.dp))

            Text(
                "Pickup status",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(Modifier.height(18.dp))

            TrackingStep("Pickup requested", "5:02 PM", true)
            TrackingStep("Collector assigned", "5:06 PM", true)
            TrackingStep("On the way", "5:18 PM", true)
            TrackingStep("Materials collected", "Pending", false)

            Spacer(Modifier.weight(1f))

            OutlinedButton(
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Contact collector")
            }
        }
    }
}

@Composable
fun TrackingStep(
    title: String,
    time: String,
    completed: Boolean
) {
    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (completed) Green else Border),
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (completed) "✓" else "•",
                color = if (completed) Color.White else TextMuted,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.width(13.dp))

        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(time, color = TextMuted, fontSize = 12.sp)
        }
    }
}

@Composable
fun HistoryScreen(nav: NavHostController) {
    var filter by remember { mutableStateOf("All") }

    Scaffold(
        containerColor = Cream,
        topBar = {
            AppTopBar(nav, "My activity")
        },
        bottomBar = { BottomBar(nav, "history") }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Recycling history",
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
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
            ) { item ->
                HistoryCard(item.first, item.second, item.third)
            }
        }
    }
}

@Composable
fun HistoryCard(
    material: String,
    quantity: String,
    amount: String
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(17.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(material, fontWeight = FontWeight.Bold)
                    Text(
                        quantity,
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        amount,
                        color = Green,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Text(
                        "Completed",
                        color = Green,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            HorizontalDivider(color = Border)

            Spacer(Modifier.height(10.dp))

            Text(
                "Pickup completed · Payment recorded",
                color = TextMuted,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun NearbyScreen(nav: NavHostController) {
    Scaffold(
        containerColor = Cream,
        topBar = {
            AppTopBar(nav, "Nearby collectors")
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    shape = RoundedCornerShape(22.dp),
                    color = GreenLight
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🗺️", fontSize = 65.sp)
                            Text(
                                "Nearby collection network",
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Live map integration ready",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    "Available near you",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            items(
                listOf(
                    "Green Scrap Centre" to "1.2 km",
                    "City Recycling Point" to "2.1 km",
                    "Eco Collectors" to "3.4 km"
                )
            ) { collector ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White
                ) {
                    Row(
                        modifier = Modifier.padding(17.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(GreenLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("♻")
                        }

                        Spacer(Modifier.width(13.dp))

                        Column(Modifier.weight(1f)) {
                            Text(
                                collector.first,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "★ 4.8 · ${collector.second}",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }

                        Text(
                            "Available",
                            color = Green,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(nav: NavHostController) {
    Scaffold(
        containerColor = Cream,
        topBar = {
            AppTopBar(nav, "Profile")
        },
        bottomBar = { BottomBar(nav, "profile") }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(86.dp)
                            .clip(CircleShape)
                            .background(GreenLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "S",
                            color = Green,
                            fontSize = 35.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        "Sahil",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "+91 XXXXX XXXXX",
                        color = TextMuted
                    )
                }
            }

            item {
                ProfileOption(
                    Icons.Default.Person,
                    "Personal information"
                )
            }

            item {
                ProfileOption(
                    Icons.Default.LocationOn,
                    "Saved addresses"
                )
            }

            item {
                ProfileOption(
                    Icons.Default.Star,
                    "Rewards & impact"
                )
            }

            item {
                ProfileOption(
                    Icons.Default.Settings,
                    "Settings"
                ) {
                    nav.navigate("settings")
                }
            }

            item {
                OutlinedButton(
                    onClick = { nav.navigate("login") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Log out")
                }
            }
        }
    }
}

@Composable
fun ProfileOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(17.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(17.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = Green)

            Spacer(Modifier.width(15.dp))

            Text(
                title,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.SemiBold
            )

            Icon(
                Icons.Default.ChevronRight,
                null,
                tint = TextMuted
            )
        }
    }
}

@Composable
fun SettingsScreen(nav: NavHostController) {
    var notifications by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = Cream,
        topBar = {
            AppTopBar(nav, "Settings")
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
        ) {
            SettingToggle(
                "Notifications",
                "Pickup and activity updates",
                notifications
            ) {
                notifications = it
            }
SettingRow("Safety guidance", "Tips for handling e-waste") { nav.navigate("safety") }
            SettingRow("Language", "English")
            SettingRow("Privacy", "Manage your data")
            SettingRow("Help & support", "We're here to help")

            Spacer(Modifier.height(25.dp))

            Text(
                "About",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(Modifier.height(12.dp))

            Text(
                "Kabadiwala Connect",
                fontWeight = FontWeight.Bold
            )

            Text(
                "Connecting people, collectors and recyclable materials.",
                color = TextMuted,
                fontSize = 13.sp
            )
        }
    }
}
@Composable
fun SafetyScreen(nav: NavHostController) {
    val context = LocalContext.current

    val title = stringResource(id = R.string.safety_title)
    val tip1 = stringResource(id = R.string.safety_tip_1)
    val tip2 = stringResource(id = R.string.safety_tip_2)

    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var ttsReady by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        val ttsEngine = TextToSpeech(context) { status ->
            ttsReady = (status == TextToSpeech.SUCCESS)
        }
        tts = ttsEngine

        onDispose {
            ttsEngine.stop()
            ttsEngine.shutdown()
            tts = null
            ttsReady = false
        }
    }

    LaunchedEffect(ttsReady, tts) {
        if (!ttsReady) return@LaunchedEffect

        val deviceLang = Locale.getDefault().language
        val locale = when (deviceLang) {
            "hi" -> Locale("hi", "IN")
            "mr" -> Locale("mr", "IN")
            else -> Locale.ENGLISH
        }
        tts?.setLanguage(locale)
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


@Composable
fun SettingToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = TextMuted, fontSize = 12.sp)
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun SettingRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 17.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = TextMuted, fontSize = 12.sp)
        }

        Icon(
            Icons.Default.ChevronRight,
            null,
            tint = TextMuted
        )
    }
}

@Composable
fun AppTopBar(
    nav: NavHostController,
    title: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Cream)
            .padding(
                start = 18.dp,
                end = 18.dp,
                top = 18.dp,
                bottom = 10.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { nav.popBackStack() }) {
            Icon(Icons.Default.ArrowBack, null)
        }

        Text(
            title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun BottomBar(
    nav: NavHostController,
    selected: String
) {
    NavigationBar(
        containerColor = Color.White
    ) {
        NavigationBarItem(
            selected = selected == "home",
            onClick = { nav.navigate("home") },
            icon = { Icon(Icons.Default.Home, null) },
            label = { Text("Home") }
        )

        NavigationBarItem(
            selected = selected == "pickup",
            onClick = { nav.navigate("pickup") },
            icon = { Icon(Icons.Default.LocalShipping, null) },
            label = { Text("Pickup") }
        )

        NavigationBarItem(
            selected = selected == "history",
            onClick = { nav.navigate("history") },
            icon = { Icon(Icons.Default.History, null) },
            label = { Text("History") }
        )

        NavigationBarItem(
            selected = selected == "profile",
            onClick = { nav.navigate("profile") },
            icon = { Icon(Icons.Default.Person, null) },
            label = { Text("Profile") }
        )
    }
}
