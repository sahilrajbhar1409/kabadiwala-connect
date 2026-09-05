package com.kabadiwalaconnect.presentation.auth

import android.app.Activity
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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.kabadiwalaconnect.data.SessionState
import com.kabadiwalaconnect.data.auth.AuthenticatedUser
import com.kabadiwalaconnect.data.auth.FirebaseAuthRepository
import com.kabadiwalaconnect.data.model.UserRole
import com.kabadiwalaconnect.data.model.User
import com.kabadiwalaconnect.data.profile.FirebaseUserProfileRepository
import com.kabadiwalaconnect.navigation.Routes
import com.kabadiwalaconnect.ui.theme.*
import androidx.compose.ui.platform.LocalContext

@Composable
fun SplashScreen(nav: NavHostController) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1300)
        val firebaseUser = FirebaseAuthRepository().currentUser()
        if (firebaseUser == null) {
            nav.navigate(Routes.ONBOARDING) {
                popUpTo(Routes.SPLASH) { inclusive = true }
            }
            return@LaunchedEffect
        }

        FirebaseUserProfileRepository().getProfile(firebaseUser.uid) { result ->
            val role = result.getOrNull()?.role ?: SessionState.savedRole(context, firebaseUser.uid)
            val destination = role?.let { it.homeRoute() } ?: Routes.LOGIN
            if (role != null) SessionState.persistRole(context, firebaseUser.uid, role)
            nav.navigate(destination) { popUpTo(Routes.SPLASH) { inclusive = true } }
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
    val context = LocalContext.current
    val activity = context as? Activity
    val authRepository = remember { FirebaseAuthRepository() }
    val profileRepository = remember { FirebaseUserProfileRepository() }
    var phone by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf<String?>(null) }
    var authenticatedUser by remember { mutableStateOf<AuthenticatedUser?>(null) }
    var showRoleSelection by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun normalizedIndianPhone(): String? {
        val digits = phone.filter(Char::isDigit)
        return when {
            digits.length == 10 -> "+91$digits"
            digits.length == 12 && digits.startsWith("91") -> "+$digits"
            else -> null
        }
    }

    fun navigateForRole(role: UserRole) {
        SessionState.persistRole(context, authenticatedUser?.uid.orEmpty(), role)
        nav.navigate(role.homeRoute()) {
            popUpTo(Routes.LOGIN) { inclusive = true }
        }
    }

    fun resolveAuthenticatedUser(user: AuthenticatedUser) {
        authenticatedUser = user
        loading = true
        profileRepository.getProfile(user.uid) { result ->
            loading = false
            result.onSuccess { profile ->
                if (profile != null) {
                    SessionState.persistRole(context, user.uid, profile.role)
                    nav.navigate(profile.role.homeRoute()) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                } else {
                    showRoleSelection = true
                }
            }.onFailure {
                val savedRole = SessionState.savedRole(context, user.uid)
                if (savedRole == null) {
                    errorMessage = it.message ?: "Unable to load your profile."
                } else {
                    SessionState.persistRole(context, user.uid, savedRole)
                    nav.navigate(savedRole.homeRoute()) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            }
        }
    }

    val googleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        loading = false
        if (result.resultCode != Activity.RESULT_OK) {
            errorMessage = "Google sign-in was cancelled."
            return@rememberLauncherForActivityResult
        }
        val accountTask = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        runCatching { accountTask.result }
            .onSuccess { account ->
                val idToken = account.idToken
                if (idToken.isNullOrBlank()) {
                    errorMessage = "Google did not provide an ID token."
                } else {
                    loading = true
                    authRepository.signInWithGoogleIdToken(idToken) { authResult ->
                        loading = false
                        authResult.onSuccess(::resolveAuthenticatedUser)
                            .onFailure { errorMessage = it.message ?: "Google sign-in failed." }
                    }
                }
            }
            .onFailure { errorMessage = it.message ?: "Google sign-in failed." }
    }

    LaunchedEffect(Unit) {
        authRepository.currentUser()?.let(::resolveAuthenticatedUser)
    }

    if (showRoleSelection && authenticatedUser != null) {
        RoleSelectionDialog(
            onRoleSelected = { role ->
                authenticatedUser?.let { user ->
                    val now = System.currentTimeMillis().toString()
                    profileRepository.saveProfile(
                        User(
                            uid = user.uid,
                            name = user.displayName ?: "Kabadiwala Connect user",
                            phoneNumber = user.phoneNumber.orEmpty(),
                            email = user.email,
                            role = role,
                            createdAt = now,
                            updatedAt = now
                        )
                    ) { saveResult ->
                        saveResult.onSuccess {
                            showRoleSelection = false
                            navigateForRole(role)
                        }.onFailure {
                            errorMessage = it.message ?: "Unable to save your role."
                        }
                    }
                }
            }
        )
    }

    AuthContainer(
        title = "Welcome back 👋",
        subtitle = "Sign in to manage your recyclable waste."
    ) {
        Text("Mobile number", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = phone,
            onValueChange = {
                val digits = it.filter(Char::isDigit)
                if (digits.length <= 10) {
                    phone = digits
                } else if (digits.length <= 12 && digits.startsWith("91")) {
                    phone = digits.drop(2)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Text("+91", modifier = Modifier.padding(start = 8.dp)) },
            placeholder = { Text("Enter mobile number") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            shape = RoundedCornerShape(14.dp)
        )
        Spacer(Modifier.height(18.dp))
        Button(
            onClick = {
                val currentActivity = activity
                if (currentActivity == null) {
                    errorMessage = "Unable to start phone verification."
                    return@Button
                }
                val formattedPhone = normalizedIndianPhone()
                if (formattedPhone == null) {
                    errorMessage = "Enter a valid 10-digit Indian phone number."
                    return@Button
                }
                loading = true
                errorMessage = null
                authRepository.startPhoneVerification(
                    currentActivity,
                    formattedPhone,
                    onCodeSent = {
                        verificationId = it
                        loading = false
                    },
                    onVerificationCompleted = {
                        loading = false
                        resolveAuthenticatedUser(it)
                    },
                    onFailure = {
                        loading = false
                        errorMessage = it.message ?: "Phone verification failed."
                    }
                )
            },
            enabled = phone.length == 10 && !loading && verificationId == null,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(15.dp)
        ) {
            if (loading) CircularProgressIndicator(modifier = Modifier.size(20.dp))
            else Text("Continue")
        }
        verificationId?.let { id ->
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = otp,
                onValueChange = { if (it.length <= 6 && it.all(Char::isDigit)) otp = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Enter OTP") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = {
                    loading = true
                    authRepository.verifyPhoneCode(id, otp) { authResult ->
                        loading = false
                        authResult.onSuccess(::resolveAuthenticatedUser)
                            .onFailure { errorMessage = it.message ?: "Invalid OTP." }
                    }
                },
                enabled = otp.length == 6 && !loading,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(15.dp)
            ) {
                if (loading) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                else Text("Verify OTP")
            }
        }
        errorMessage?.let {
            Spacer(Modifier.height(10.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
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
                if (activity == null) {
                    errorMessage = "Google sign-in is not configured for this build."
                    return@OutlinedButton
                }
                loading = true
                runCatching { authRepository.googleSignInIntent(context) }
                    .onSuccess { googleLauncher.launch(it) }
                    .onFailure {
                        loading = false
                        errorMessage = it.message ?: "Google sign-in is not configured."
                    }
            },
            enabled = !loading,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(15.dp)
        ) {
            if (loading) CircularProgressIndicator(modifier = Modifier.size(20.dp))
            else Text("Continue with Google")
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
                nav.navigate(Routes.LOGIN) {
                    popUpTo(Routes.REGISTER) { inclusive = true }
                }
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
private fun RoleSelectionDialog(onRoleSelected: (UserRole) -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text("Choose your role") },
        text = { Text("Select how you use Kabadiwala Connect.") },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onRoleSelected(UserRole.CITIZEN) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Citizen") }
                Button(
                    onClick = { onRoleSelected(UserRole.COLLECTOR) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Collector / Kabadiwala") }
                Button(
                    onClick = { onRoleSelected(UserRole.RECYCLER) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Recycler") }
            }
        }
    )
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
