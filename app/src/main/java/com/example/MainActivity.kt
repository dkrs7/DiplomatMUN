package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DebateChamberScreen
import com.example.ui.screens.EvaluationScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.RopGuideScreen
import com.example.ui.screens.SessionSetupScreen
import com.example.ui.theme.DiplomatTheme
import com.example.viewmodel.DiplomatViewModel

enum class AppScreen {
    AUTH,
    DASHBOARD,
    SETUP,
    CHAMBER,
    EVALUATION,
    HISTORY,
    ROP_GUIDE
}

class MainActivity : ComponentActivity() {
    private val viewModel: DiplomatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DiplomatTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DiplomatApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun DiplomatApp(viewModel: DiplomatViewModel) {
    val activeUser by viewModel.activeUser.collectAsState()
    var currentScreen by remember { mutableStateOf(AppScreen.AUTH) }

    // Request Audio Permission for Voice Input
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    // Automatically transition to dashboard if user profile is already saved
    LaunchedEffect(activeUser) {
        if (activeUser != null && currentScreen == AppScreen.AUTH) {
            currentScreen = AppScreen.DASHBOARD
        } else if (activeUser == null) {
            currentScreen = AppScreen.AUTH
        }
    }

    when (currentScreen) {
        AppScreen.AUTH -> {
            AuthScreen(
                onLoginSuccess = { name, email, age, countryCode ->
                    viewModel.registerOrLoginUser(name, email, age, countryCode)
                    currentScreen = AppScreen.DASHBOARD
                }
            )
        }

        AppScreen.DASHBOARD -> {
            DashboardScreen(
                user = activeUser,
                onStartNewSession = { currentScreen = AppScreen.SETUP },
                onViewHistory = { currentScreen = AppScreen.HISTORY },
                onViewRopGuide = { currentScreen = AppScreen.ROP_GUIDE },
                onLogout = {
                    viewModel.logout()
                    currentScreen = AppScreen.AUTH
                }
            )
        }

        AppScreen.SETUP -> {
            BackHandler { currentScreen = AppScreen.DASHBOARD }
            SessionSetupScreen(
                initialCountryCode = activeUser?.preferredCountryCode ?: "USA",
                onBack = { currentScreen = AppScreen.DASHBOARD },
                onStartDebate = { committee, topic, country, speechTime ->
                    viewModel.setupConference(committee, topic, country, speechTime)
                    currentScreen = AppScreen.CHAMBER
                }
            )
        }

        AppScreen.CHAMBER -> {
            BackHandler {
                viewModel.concludeDebateAndEvaluate()
                currentScreen = AppScreen.EVALUATION
            }
            DebateChamberScreen(
                viewModel = viewModel,
                onAdjournDebate = { currentScreen = AppScreen.EVALUATION }
            )
        }

        AppScreen.EVALUATION -> {
            BackHandler { currentScreen = AppScreen.DASHBOARD }
            EvaluationScreen(
                viewModel = viewModel,
                onReturnHome = { currentScreen = AppScreen.DASHBOARD },
                onNewDebate = { currentScreen = AppScreen.SETUP }
            )
        }

        AppScreen.HISTORY -> {
            BackHandler { currentScreen = AppScreen.DASHBOARD }
            HistoryScreen(
                viewModel = viewModel,
                onBack = { currentScreen = AppScreen.DASHBOARD },
                onSelectSession = { session ->
                    val eval = viewModel.parseEvaluation(session.evaluationJson)
                    if (eval != null) {
                        currentScreen = AppScreen.EVALUATION
                    }
                }
            )
        }

        AppScreen.ROP_GUIDE -> {
            BackHandler { currentScreen = AppScreen.DASHBOARD }
            RopGuideScreen(
                onBack = { currentScreen = AppScreen.DASHBOARD }
            )
        }
    }
}
