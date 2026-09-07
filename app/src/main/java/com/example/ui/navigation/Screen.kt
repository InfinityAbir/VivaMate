package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object SubjectLibrary : Screen("subject_library")
    object CreateEditSubject : Screen("create_edit_subject?subjectId={subjectId}") {
        fun createRoute(subjectId: String? = null) = if (subjectId != null) "create_edit_subject?subjectId=$subjectId" else "create_edit_subject"
    }
    object VivaSetup : Screen("viva_setup?subjectId={subjectId}&topicId={topicId}") {
        fun createRoute(subjectId: String? = null, topicId: String? = null): String {
            return when {
                subjectId != null && topicId != null -> "viva_setup?subjectId=$subjectId&topicId=$topicId"
                subjectId != null -> "viva_setup?subjectId=$subjectId"
                else -> "viva_setup"
            }
        }
    }
    object VivaRoom : Screen("viva_room/{sessionId}") {
        fun createRoute(sessionId: String) = "viva_room/$sessionId"
    }
    object SessionSummary : Screen("session_summary/{sessionId}") {
        fun createRoute(sessionId: String) = "session_summary/$sessionId"
    }
    object HistoryAnalytics : Screen("history_analytics")
    object RevisionPlan : Screen("revision_plan")
    object Settings : Screen("settings")
    object PrivacyDeletion : Screen("privacy_deletion")
    object Paywall : Screen("paywall")
    object Support : Screen("support")
}
