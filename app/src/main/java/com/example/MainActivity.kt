package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.billing.MockBillingService
import com.example.data.local.AppDatabase
import com.example.data.local.preferences.PreferencesDataStore
import com.example.data.remote.gemini.GeminiServiceFactory
import com.example.data.repository.SettingsRepository
import com.example.data.repository.VivaRepository
import com.example.data.speech.AndroidSpeechToTextService
import com.example.ui.navigation.Screen
import com.example.ui.screens.analytics.HistoryAnalyticsScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.library.CreateEditSubjectScreen
import com.example.ui.screens.library.SubjectLibraryScreen
import com.example.ui.screens.onboarding.OnboardingScreen
import com.example.ui.screens.paywall.PaywallScreen
import com.example.ui.screens.privacy.PrivacyDeletionScreen
import com.example.ui.screens.revision.RevisionPlanScreen
import com.example.ui.screens.room.VivaRoomScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.setup.VivaSetupScreen
import com.example.ui.screens.summary.SessionSummaryScreen
import com.example.ui.screens.support.SupportHelpScreen
import com.example.ui.theme.VivaMateTheme
import com.example.ui.viewmodel.HomeViewModel
import com.example.ui.viewmodel.SessionSummaryViewModel
import com.example.ui.viewmodel.SettingsViewModel
import com.example.ui.viewmodel.SubjectViewModel
import com.example.ui.viewmodel.VivaRoomViewModel
import com.example.ui.viewmodel.VivaSetupViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getInstance(applicationContext)
        val dataStore = PreferencesDataStore(applicationContext)
        val vivaRepository = VivaRepository(database)
        val settingsRepository = SettingsRepository(dataStore)
        val geminiService = GeminiServiceFactory.create(BuildConfig.GEMINI_API_KEY)
        val speechService = AndroidSpeechToTextService(applicationContext)
        val billingService = MockBillingService()

        setContent {
            val userSettings by settingsRepository.userSettings.collectAsState(initial = com.example.data.model.UserSettings())
            val scope = rememberCoroutineScope()

            VivaMateTheme(themeSetting = userSettings.theme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    val startDest = if (userSettings.onboardingCompleted) Screen.Home.route else Screen.Onboarding.route

                    NavHost(
                        navController = navController,
                        startDestination = startDest
                    ) {
                        // 1. Onboarding
                        composable(Screen.Onboarding.route) {
                            OnboardingScreen(
                                currentTheme = userSettings.theme,
                                onSelectTheme = { theme -> scope.launch { settingsRepository.setTheme(theme) } },
                                onGetStarted = {
                                    scope.launch {
                                        settingsRepository.setOnboardingCompleted(true)
                                        navController.navigate(Screen.Home.route) {
                                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }

                        // 2. Home Dashboard
                        composable(Screen.Home.route) {
                            val homeViewModel = remember {
                                HomeViewModel(vivaRepository, settingsRepository)
                            }
                            val uiState by homeViewModel.uiState.collectAsState()

                            HomeScreen(
                                uiState = uiState,
                                onStartPractice = { navController.navigate(Screen.VivaSetup.createRoute()) },
                                onSelectSubject = { subjectId -> navController.navigate(Screen.VivaSetup.createRoute(subjectId = subjectId)) },
                                onViewSubjectLibrary = { navController.navigate(Screen.SubjectLibrary.route) },
                                onViewSessionSummary = { sessionId -> navController.navigate(Screen.SessionSummary.createRoute(sessionId)) },
                                onViewAnalytics = { navController.navigate(Screen.HistoryAnalytics.route) },
                                onViewRevisionPlan = { navController.navigate(Screen.RevisionPlan.route) },
                                onOpenSettings = { navController.navigate(Screen.Settings.route) },
                                onUpgradePremium = { navController.navigate(Screen.Paywall.route) }
                            )
                        }

                        // 3. Viva Setup
                        composable(
                            route = Screen.VivaSetup.route,
                            arguments = listOf(
                                navArgument("subjectId") {
                                    type = NavType.StringType
                                    nullable = true
                                    defaultValue = null
                                },
                                navArgument("topicId") {
                                    type = NavType.StringType
                                    nullable = true
                                    defaultValue = null
                                }
                            )
                        ) { backStackEntry ->
                            val setupViewModel = remember {
                                VivaSetupViewModel(vivaRepository, settingsRepository, geminiService)
                            }
                            val uiState by setupViewModel.uiState.collectAsState()

                            VivaSetupScreen(
                                uiState = uiState,
                                onSelectSubject = { setupViewModel.selectSubject(it) },
                                onSelectTopic = { setupViewModel.selectTopic(it) },
                                onSetAcademicLevel = { setupViewModel.setAcademicLevel(it) },
                                onSetExamStyle = { setupViewModel.setExamStyle(it) },
                                onSetDifficulty = { setupViewModel.setDifficulty(it) },
                                onSetQuestionCount = { setupViewModel.setQuestionCount(it) },
                                onSetLanguage = { setupViewModel.setLanguage(it) },
                                onSetAnswerMode = { setupViewModel.setAnswerMode(it) },
                                onSetSyllabusNotes = { setupViewModel.setSyllabusNotes(it) },
                                onStartSession = {
                                    setupViewModel.startSession { newSessionId ->
                                        navController.navigate(Screen.VivaRoom.createRoute(newSessionId)) {
                                            popUpTo(Screen.VivaSetup.route) { inclusive = true }
                                        }
                                    }
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 4. Viva Room
                        composable(
                            route = Screen.VivaRoom.route,
                            arguments = listOf(
                                navArgument("sessionId") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
                            val roomViewModel = remember(sessionId) {
                                VivaRoomViewModel(
                                    sessionId = sessionId,
                                    vivaRepository = vivaRepository,
                                    settingsRepository = settingsRepository,
                                    geminiService = geminiService,
                                    speechService = speechService
                                )
                            }
                            val uiState by roomViewModel.uiState.collectAsState()

                            VivaRoomScreen(
                                uiState = uiState,
                                onToggleRecord = { roomViewModel.toggleVoiceRecording() },
                                onUpdateTranscript = { roomViewModel.updateTranscript(it) },
                                onClearTranscript = { roomViewModel.clearTranscript() },
                                onSubmitAnswer = { roomViewModel.submitAnswer() },
                                onAdvanceNext = {
                                    roomViewModel.advanceToNextQuestion { finishedSessionId ->
                                        navController.navigate(Screen.SessionSummary.createRoute(finishedSessionId)) {
                                            popUpTo(Screen.VivaRoom.route) { inclusive = true }
                                        }
                                    }
                                },
                                onSkipQuestion = {
                                    roomViewModel.skipQuestion { finishedSessionId ->
                                        navController.navigate(Screen.SessionSummary.createRoute(finishedSessionId)) {
                                            popUpTo(Screen.VivaRoom.route) { inclusive = true }
                                        }
                                    }
                                },
                                onExplainTopic = { roomViewModel.explainTopic() },
                                onDismissExplanation = { roomViewModel.dismissExplanation() },
                                onDismissFeedback = { roomViewModel.dismissFeedbackSheet() },
                                onEndSession = {
                                    roomViewModel.finalizeSession { finishedSessionId ->
                                        navController.navigate(Screen.SessionSummary.createRoute(finishedSessionId)) {
                                            popUpTo(Screen.VivaRoom.route) { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }

                        // 5. Session Summary
                        composable(
                            route = Screen.SessionSummary.route,
                            arguments = listOf(
                                navArgument("sessionId") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
                            val summaryViewModel = remember(sessionId) {
                                SessionSummaryViewModel(sessionId, vivaRepository)
                            }
                            val uiState by summaryViewModel.uiState.collectAsState()

                            SessionSummaryScreen(
                                uiState = uiState,
                                onRetrySession = {
                                    val subId = uiState.session?.subjectId
                                    val topId = uiState.session?.topicId
                                    navController.navigate(Screen.VivaSetup.createRoute(subId, topId)) {
                                        popUpTo(Screen.SessionSummary.route) { inclusive = true }
                                    }
                                },
                                onPracticeWeakAreas = {
                                    val subId = uiState.session?.subjectId
                                    val topId = uiState.session?.topicId
                                    navController.navigate(Screen.VivaSetup.createRoute(subId, topId))
                                },
                                onDeleteTranscripts = {
                                    summaryViewModel.deleteTranscriptsForSession { }
                                },
                                onBack = {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Home.route) { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 6. Subject Library
                        composable(Screen.SubjectLibrary.route) {
                            val subjectViewModel = remember { SubjectViewModel(vivaRepository) }
                            val uiState by subjectViewModel.uiState.collectAsState()

                            SubjectLibraryScreen(
                                uiState = uiState,
                                onSelectSubject = { subjectViewModel.selectSubject(it) },
                                onAddNewSubject = { navController.navigate(Screen.CreateEditSubject.createRoute()) },
                                onEditSubject = { subjectId -> navController.navigate(Screen.CreateEditSubject.createRoute(subjectId)) },
                                onDeleteSubject = { subjectId -> subjectViewModel.deleteSubject(subjectId) },
                                onAddTopic = { subjectId, title, syllabus, importance ->
                                    subjectViewModel.addTopic(subjectId, title, syllabus, importance) {}
                                },
                                onPracticeTopic = { subjectId, topicId ->
                                    navController.navigate(Screen.VivaSetup.createRoute(subjectId, topicId))
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 7. Create / Edit Subject
                        composable(
                            route = Screen.CreateEditSubject.route,
                            arguments = listOf(
                                navArgument("subjectId") {
                                    type = NavType.StringType
                                    nullable = true
                                    defaultValue = null
                                }
                            )
                        ) { backStackEntry ->
                            val subjectId = backStackEntry.arguments?.getString("subjectId")
                            val subjectViewModel = remember { SubjectViewModel(vivaRepository) }

                            CreateEditSubjectScreen(
                                onSave = { name, desc, level, color ->
                                    subjectViewModel.saveSubject(name, desc, level, color, subjectId) {
                                        navController.popBackStack()
                                    }
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 8. History Analytics
                        composable(Screen.HistoryAnalytics.route) {
                            val allSessions by vivaRepository.getAllSessions().collectAsState(initial = emptyList())

                            HistoryAnalyticsScreen(
                                sessions = allSessions,
                                onSelectSession = { sessionId ->
                                    navController.navigate(Screen.SessionSummary.createRoute(sessionId))
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 9. Revision Plan
                        composable(Screen.RevisionPlan.route) {
                            val activeRecs by vivaRepository.getPendingRecommendations().collectAsState(initial = emptyList())

                            RevisionPlanScreen(
                                recommendations = activeRecs,
                                onToggleCompleted = { id, completed ->
                                    scope.launch { vivaRepository.setRecommendationCompleted(id, completed) }
                                },
                                onStartPracticeTarget = { topic ->
                                    navController.navigate(Screen.VivaSetup.createRoute())
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 10. Settings
                        composable(Screen.Settings.route) {
                            val settingsViewModel = remember {
                                SettingsViewModel(settingsRepository, vivaRepository, billingService)
                            }
                            val uiState by settingsViewModel.uiState.collectAsState()

                            SettingsScreen(
                                uiState = uiState,
                                onSetTheme = { settingsViewModel.setTheme(it) },
                                onSetLanguage = { settingsViewModel.setLanguage(it) },
                                onSetRetainTranscripts = { settingsViewModel.setRetainTranscripts(it) },
                                onSetRetainAudio = { settingsViewModel.setRetainAudio(it) },
                                onSetAnalyticsConsent = { settingsViewModel.setAnalyticsConsent(it) },
                                onSetReduceMotion = { settingsViewModel.setReduceMotion(it) },
                                onSetDailyReminder = { settingsViewModel.setDailyReminder(it) },
                                onOpenPrivacyDeletion = { navController.navigate(Screen.PrivacyDeletion.route) },
                                onOpenPaywall = { navController.navigate(Screen.Paywall.route) },
                                onOpenSupport = { navController.navigate(Screen.Support.route) },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 11. Privacy Deletion
                        composable(Screen.PrivacyDeletion.route) {
                            val settingsViewModel = remember {
                                SettingsViewModel(settingsRepository, vivaRepository, billingService)
                            }
                            PrivacyDeletionScreen(
                                onAnonymizeTranscripts = { onComplete -> settingsViewModel.anonymizeAllTranscripts(onComplete) },
                                onDeleteAllSessions = { onComplete -> settingsViewModel.deleteAllSessions(onComplete) },
                                onWipeAllData = { onComplete -> settingsViewModel.wipeAllData(onComplete) },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 12. Paywall
                        composable(Screen.Paywall.route) {
                            val settingsViewModel = remember {
                                SettingsViewModel(settingsRepository, vivaRepository, billingService)
                            }
                            val uiState by settingsViewModel.uiState.collectAsState()

                            PaywallScreen(
                                plans = uiState.plans,
                                isPremium = uiState.isPremium,
                                onPurchasePlan = { planId -> settingsViewModel.purchasePlan(planId) {} },
                                onRestorePurchases = { settingsViewModel.restorePurchases {} },
                                onToggleSimulatedPremium = { settingsViewModel.toggleSimulatedPremium() },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 13. Support & Help
                        composable(Screen.Support.route) {
                            SupportHelpScreen(onBack = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }
}
