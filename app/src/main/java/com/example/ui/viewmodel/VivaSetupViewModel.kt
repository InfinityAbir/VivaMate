package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Subject
import com.example.data.model.Topic
import com.example.data.model.VivaQuestion
import com.example.data.model.VivaQuestionContext
import com.example.data.model.VivaSession
import com.example.data.remote.gemini.GeminiService
import com.example.data.repository.SettingsRepository
import com.example.data.repository.VivaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.util.UUID

data class VivaSetupUiState(
    val subjects: List<Subject> = emptyList(),
    val topics: List<Topic> = emptyList(),
    val selectedSubject: Subject? = null,
    val selectedTopic: Topic? = null,
    val academicLevel: String = "Undergraduate",
    val examStyle: String = "Viva", // Viva, Lab Viva, Thesis Defense, Interview, Rapid Practice
    val difficulty: String = "Adaptive", // Beginner, Intermediate, Advanced, Adaptive
    val questionCount: Int = 5,
    val language: String = "English",
    val answerMode: String = "Both", // Voice First, Typed Only, Both
    val syllabusNotes: String = "",
    val isCreatingSession: Boolean = false,
    val errorMessage: String? = null
)

class VivaSetupViewModel(
    private val vivaRepository: VivaRepository,
    private val settingsRepository: SettingsRepository,
    private val geminiService: GeminiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(VivaSetupUiState())
    val uiState: StateFlow<VivaSetupUiState> = _uiState.asStateFlow()

    init {
        loadSubjectsAndSettings()
    }

    private fun loadSubjectsAndSettings() {
        viewModelScope.launch {
            vivaRepository.getActiveSubjects().collect { list ->
                _uiState.update { current ->
                    val defaultSubject = current.selectedSubject ?: list.firstOrNull()
                    current.copy(
                        subjects = list,
                        selectedSubject = defaultSubject
                    )
                }
                if (_uiState.value.selectedSubject != null) {
                    loadTopicsForSubject(_uiState.value.selectedSubject!!.id)
                }
            }
        }
        viewModelScope.launch {
            settingsRepository.userSettings.firstOrNull()?.let { settings ->
                _uiState.update { it.copy(language = settings.language) }
            }
        }
    }

    fun selectSubject(subject: Subject) {
        _uiState.update { it.copy(selectedSubject = subject, selectedTopic = null) }
        loadTopicsForSubject(subject.id)
    }

    fun selectTopic(topic: Topic) {
        _uiState.update {
            it.copy(
                selectedTopic = topic,
                syllabusNotes = if (it.syllabusNotes.isBlank()) topic.syllabusText else it.syllabusNotes
            )
        }
    }

    fun setAcademicLevel(level: String) {
        _uiState.update { it.copy(academicLevel = level) }
    }

    fun setExamStyle(style: String) {
        _uiState.update { it.copy(examStyle = style) }
    }

    fun setDifficulty(diff: String) {
        _uiState.update { it.copy(difficulty = diff) }
    }

    fun setQuestionCount(count: Int) {
        _uiState.update { it.copy(questionCount = count) }
    }

    fun setLanguage(lang: String) {
        _uiState.update { it.copy(language = lang) }
    }

    fun setAnswerMode(mode: String) {
        _uiState.update { it.copy(answerMode = mode) }
    }

    fun setSyllabusNotes(notes: String) {
        _uiState.update { it.copy(syllabusNotes = notes) }
    }

    private fun loadTopicsForSubject(subjectId: String) {
        viewModelScope.launch {
            vivaRepository.getTopicsForSubject(subjectId).collect { topicsList ->
                _uiState.update { current ->
                    current.copy(
                        topics = topicsList,
                        selectedTopic = current.selectedTopic ?: topicsList.firstOrNull(),
                        syllabusNotes = if (current.syllabusNotes.isBlank()) topicsList.firstOrNull()?.syllabusText ?: "" else current.syllabusNotes
                    )
                }
            }
        }
    }

    fun startSession(onSuccess: (String) -> Unit) {
        val state = _uiState.value
        val subject = state.selectedSubject ?: return
        val topic = state.selectedTopic ?: return

        _uiState.update { it.copy(isCreatingSession = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val sessionId = UUID.randomUUID().toString()
                val newSession = VivaSession(
                    id = sessionId,
                    subjectId = subject.id,
                    topicId = topic.id,
                    subjectName = subject.name,
                    topicTitle = topic.title,
                    startedAt = System.currentTimeMillis(),
                    mode = state.examStyle,
                    difficulty = state.difficulty,
                    language = state.language,
                    totalQuestions = state.questionCount,
                    status = "In Progress"
                )

                vivaRepository.insertSession(newSession)

                // Generate Question 1
                val context = VivaQuestionContext(
                    subjectName = subject.name,
                    topicTitle = topic.title,
                    academicLevel = state.academicLevel,
                    examStyle = state.examStyle,
                    difficulty = state.difficulty,
                    language = state.language,
                    syllabusOrNotes = state.syllabusNotes.ifBlank { topic.syllabusText },
                    questionNumber = 1,
                    totalQuestions = state.questionCount
                )

                val questionResult = geminiService.generateQuestion(context).getOrThrow()
                val q1 = VivaQuestion(
                    id = UUID.randomUUID().toString(),
                    sessionId = sessionId,
                    sequence = 1,
                    text = questionResult.question,
                    questionType = questionResult.questionType,
                    expectedKeyPointsJson = JSONArray(questionResult.expectedKeyPoints).toString(),
                    difficulty = questionResult.difficulty,
                    estimatedAnswerTimeSeconds = questionResult.estimatedAnswerTimeSeconds,
                    examinerHint = questionResult.examinerHint
                )
                vivaRepository.insertQuestion(q1)

                settingsRepository.incrementFreeSessionsUsed()
                _uiState.update { it.copy(isCreatingSession = false) }
                onSuccess(sessionId)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isCreatingSession = false,
                        errorMessage = "Could not initialize session: ${e.message}"
                    )
                }
            }
        }
    }
}
