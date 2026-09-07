package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.RevisionRecommendation
import com.example.data.model.VivaQuestion
import com.example.data.model.VivaResponse
import com.example.data.model.VivaSession
import com.example.data.repository.VivaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class QuestionReviewItem(
    val question: VivaQuestion,
    val response: VivaResponse?
)

data class SessionSummaryUiState(
    val session: VivaSession? = null,
    val reviewItems: List<QuestionReviewItem> = emptyList(),
    val recommendations: List<RevisionRecommendation> = emptyList(),
    val strongPoints: List<String> = emptyList(),
    val weakPoints: List<String> = emptyList(),
    val isLoading: Boolean = true
)

class SessionSummaryViewModel(
    private val sessionId: String,
    private val vivaRepository: VivaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionSummaryUiState())
    val uiState: StateFlow<SessionSummaryUiState> = _uiState.asStateFlow()

    init {
        loadSummary()
    }

    private fun loadSummary() {
        viewModelScope.launch {
            val session = vivaRepository.getSessionById(sessionId)
            val questions = vivaRepository.getQuestionsForSessionSync(sessionId)
            val responses = vivaRepository.getResponsesForSessionSync(sessionId)

            val reviewItems = questions.map { q ->
                QuestionReviewItem(
                    question = q,
                    response = responses.find { it.questionId == q.id }
                )
            }

            val allStrengths = mutableListOf<String>()
            val allWeaknesses = mutableListOf<String>()
            responses.forEach { r ->
                try {
                    val strArr = org.json.JSONArray(r.strengthsJson)
                    for (i in 0 until strArr.length()) allStrengths.add(strArr.getString(i))
                    val missArr = org.json.JSONArray(r.missingPointsJson)
                    for (i in 0 until missArr.length()) allWeaknesses.add(missArr.getString(i))
                } catch (e: Exception) {
                    // Ignore
                }
            }

            vivaRepository.getRecommendationsForSession(sessionId).collect { recs ->
                _uiState.update {
                    it.copy(
                        session = session,
                        reviewItems = reviewItems,
                        recommendations = recs,
                        strongPoints = allStrengths.distinct().take(4),
                        weakPoints = allWeaknesses.distinct().take(4),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun deleteTranscriptsForSession(onSuccess: () -> Unit) {
        viewModelScope.launch {
            vivaRepository.anonymizeTranscriptsForSession(sessionId)
            loadSummary()
            onSuccess()
        }
    }
}
