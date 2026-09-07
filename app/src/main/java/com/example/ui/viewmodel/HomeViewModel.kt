package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.RevisionRecommendation
import com.example.data.model.Subject
import com.example.data.model.UserSettings
import com.example.data.model.VivaSession
import com.example.data.repository.SettingsRepository
import com.example.data.repository.VivaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val subjects: List<Subject> = emptyList(),
    val recentSessions: List<VivaSession> = emptyList(),
    val averageScore: Double = 0.0,
    val completedSessionsCount: Int = 0,
    val topRecommendation: RevisionRecommendation? = null,
    val userSettings: UserSettings = UserSettings(),
    val practiceStreakDays: Int = 3,
    val isLoading: Boolean = false
)

class HomeViewModel(
    private val vivaRepository: VivaRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        combine(
            vivaRepository.getActiveSubjects(),
            vivaRepository.getRecentSessions(5),
            vivaRepository.getAverageOverallScore()
        ) { subjects, sessions, avgScore ->
            Triple(subjects, sessions, avgScore)
        },
        combine(
            vivaRepository.getCompletedSessionsCount(),
            vivaRepository.getPendingRecommendations(),
            settingsRepository.userSettings
        ) { count, recommendations, settings ->
            Triple(count, recommendations, settings)
        }
    ) { (subjects, sessions, avgScore), (count, recommendations, settings) ->
        HomeUiState(
            subjects = subjects,
            recentSessions = sessions,
            averageScore = avgScore ?: 0.0,
            completedSessionsCount = count,
            topRecommendation = recommendations.firstOrNull(),
            userSettings = settings,
            practiceStreakDays = calculateStreak(sessions),
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true)
    )

    private fun calculateStreak(sessions: List<VivaSession>): Int {
        if (sessions.isEmpty()) return 0
        // Simple streak estimation based on completed sessions
        return (sessions.count { it.status == "Completed" }).coerceIn(1, 14)
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            settingsRepository.setOnboardingCompleted(true)
        }
    }
}
