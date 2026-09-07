package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.RevisionRecommendation
import com.example.data.model.VivaEvaluationContext
import com.example.data.model.VivaEvaluationResult
import com.example.data.model.VivaQuestion
import com.example.data.model.VivaQuestionContext
import com.example.data.model.VivaResponse
import com.example.data.model.VivaSession
import com.example.data.remote.gemini.GeminiService
import com.example.data.repository.SettingsRepository
import com.example.data.repository.VivaRepository
import com.example.data.speech.SpeechState
import com.example.data.speech.SpeechToTextService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.util.UUID

data class VivaRoomUiState(
    val session: VivaSession? = null,
    val currentQuestion: VivaQuestion? = null,
    val questionNumber: Int = 1,
    val totalQuestions: Int = 5,
    val timeRemainingSeconds: Int = 60,
    val isTimerRunning: Boolean = false,
    val isRecording: Boolean = false,
    val recordingDurationSeconds: Int = 0,
    val transcript: String = "",
    val isVoiceInput: Boolean = false,
    val isEvaluating: Boolean = false,
    val latestEvaluation: VivaEvaluationResult? = null,
    val showFeedbackSheet: Boolean = false,
    val explanationText: String? = null,
    val isGeneratingNextQuestion: Boolean = false,
    val sessionFinished: Boolean = false,
    val errorMessage: String? = null
)

class VivaRoomViewModel(
    private val sessionId: String,
    private val vivaRepository: VivaRepository,
    private val settingsRepository: SettingsRepository,
    private val geminiService: GeminiService,
    private val speechService: SpeechToTextService
) : ViewModel() {

    private val _uiState = MutableStateFlow(VivaRoomUiState())
    val uiState: StateFlow<VivaRoomUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var recordingTimerJob: Job? = null
    private val sessionResponses = mutableListOf<VivaResponse>()

    init {
        loadSessionData()
        observeSpeechState()
    }

    private fun loadSessionData() {
        viewModelScope.launch {
            val session = vivaRepository.getSessionById(sessionId)
            if (session != null) {
                val questions = vivaRepository.getQuestionsForSessionSync(sessionId)
                val currentQ = questions.lastOrNull()
                _uiState.update {
                    it.copy(
                        session = session,
                        currentQuestion = currentQ,
                        questionNumber = currentQ?.sequence ?: 1,
                        totalQuestions = session.totalQuestions,
                        timeRemainingSeconds = currentQ?.estimatedAnswerTimeSeconds ?: 60
                    )
                }
                startQuestionTimer(currentQ?.estimatedAnswerTimeSeconds ?: 60)
            }
        }
    }

    private fun observeSpeechState() {
        viewModelScope.launch {
            speechService.state.collect { state ->
                when (state) {
                    is SpeechState.Listening -> {
                        _uiState.update { it.copy(isRecording = true, isVoiceInput = true) }
                    }
                    is SpeechState.PartialResult -> {
                        _uiState.update { it.copy(transcript = state.text) }
                    }
                    is SpeechState.FinalResult -> {
                        _uiState.update {
                            it.copy(
                                transcript = if (it.transcript.isBlank()) state.text else it.transcript + " " + state.text,
                                isRecording = false
                            )
                        }
                        stopRecordingTimer()
                    }
                    is SpeechState.Error -> {
                        _uiState.update {
                            it.copy(
                                isRecording = false,
                                errorMessage = state.message
                            )
                        }
                        stopRecordingTimer()
                    }
                    is SpeechState.Idle -> {
                        _uiState.update { it.copy(isRecording = false) }
                        stopRecordingTimer()
                    }
                }
            }
        }
    }

    fun toggleVoiceRecording() {
        if (_uiState.value.isRecording) {
            speechService.stopListening()
            stopRecordingTimer()
        } else {
            val lang = _uiState.value.session?.language ?: "English"
            val code = if (lang.equals("Bangla", ignoreCase = true)) "bn-BD" else "en-US"
            speechService.startListening(code)
            startRecordingTimer()
        }
    }

    private fun startRecordingTimer() {
        recordingTimerJob?.cancel()
        _uiState.update { it.copy(recordingDurationSeconds = 0) }
        recordingTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.update { it.copy(recordingDurationSeconds = it.recordingDurationSeconds + 1) }
            }
        }
    }

    private fun stopRecordingTimer() {
        recordingTimerJob?.cancel()
    }

    private fun startQuestionTimer(seconds: Int) {
        timerJob?.cancel()
        _uiState.update { it.copy(timeRemainingSeconds = seconds, isTimerRunning = true) }
        timerJob = viewModelScope.launch {
            while (_uiState.value.timeRemainingSeconds > 0) {
                delay(1000)
                _uiState.update { it.copy(timeRemainingSeconds = it.timeRemainingSeconds - 1) }
            }
            _uiState.update { it.copy(isTimerRunning = false) }
        }
    }

    fun updateTranscript(newText: String) {
        _uiState.update { it.copy(transcript = newText) }
    }

    fun clearTranscript() {
        _uiState.update { it.copy(transcript = "") }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun submitAnswer() {
        val state = _uiState.value
        val question = state.currentQuestion ?: return
        val session = state.session ?: return
        val answerText = state.transcript.trim()

        if (answerText.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please speak or type your answer before submitting.") }
            return
        }

        // Stop recording if active
        if (state.isRecording) {
            speechService.stopListening()
            stopRecordingTimer()
        }
        timerJob?.cancel()

        _uiState.update { it.copy(isEvaluating = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val keyPoints = try {
                    val arr = JSONArray(question.expectedKeyPointsJson)
                    (0 until arr.length()).map { arr.getString(it) }
                } catch (e: Exception) {
                    listOf("Core concepts", "Application")
                }

                val evalContext = VivaEvaluationContext(
                    subjectName = session.subjectName,
                    topicTitle = session.topicTitle,
                    academicLevel = "Undergraduate",
                    questionText = question.text,
                    questionType = question.questionType,
                    expectedKeyPoints = keyPoints,
                    userAnswer = answerText,
                    currentDifficulty = question.difficulty,
                    language = session.language
                )

                val evalResult = geminiService.evaluateAnswer(evalContext).getOrThrow()

                // Save VivaResponse
                val response = VivaResponse(
                    id = UUID.randomUUID().toString(),
                    questionId = question.id,
                    sessionId = session.id,
                    answerMode = if (state.isVoiceInput) "Voice" else "Text",
                    transcript = answerText,
                    submittedAt = System.currentTimeMillis(),
                    conceptualScore = evalResult.conceptualScore,
                    communicationScore = evalResult.communicationScore,
                    strengthsJson = JSONArray(evalResult.strengths).toString(),
                    missingPointsJson = JSONArray(evalResult.missingPoints).toString(),
                    misconceptionsJson = JSONArray(evalResult.misconceptions).toString(),
                    correction = evalResult.conciseCorrection,
                    modelAnswer = evalResult.modelAnswer,
                    suggestedRevisionTopicsJson = JSONArray(evalResult.suggestedRevisionTopics).toString(),
                    nextQuestionDifficulty = evalResult.nextQuestionDifficulty,
                    clarificationQuestion = if (evalResult.needsClarification) evalResult.clarificationQuestion else null
                )

                vivaRepository.insertResponse(response)
                sessionResponses.add(response)

                _uiState.update {
                    it.copy(
                        isEvaluating = false,
                        latestEvaluation = evalResult,
                        showFeedbackSheet = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isEvaluating = false,
                        errorMessage = "Evaluation error: ${e.message}"
                    )
                }
            }
        }
    }

    fun dismissFeedbackSheet() {
        _uiState.update { it.copy(showFeedbackSheet = false) }
    }

    fun advanceToNextQuestion(onFinished: (String) -> Unit) {
        val state = _uiState.value
        val session = state.session ?: return
        val currentSeq = state.questionNumber

        _uiState.update { it.copy(showFeedbackSheet = false) }

        if (currentSeq >= state.totalQuestions) {
            finalizeSession(onFinished)
        } else {
            generateNextQuestion(currentSeq + 1)
        }
    }

    private fun generateNextQuestion(nextSeq: Int) {
        val state = _uiState.value
        val session = state.session ?: return
        val currentQ = state.currentQuestion
        val lastEval = state.latestEvaluation

        _uiState.update {
            it.copy(
                isGeneratingNextQuestion = true,
                transcript = "",
                isVoiceInput = false,
                latestEvaluation = null
            )
        }

        viewModelScope.launch {
            try {
                val nextDifficulty = lastEval?.nextQuestionDifficulty ?: currentQ?.difficulty ?: "Intermediate"
                val qContext = VivaQuestionContext(
                    subjectName = session.subjectName,
                    topicTitle = session.topicTitle,
                    academicLevel = "Undergraduate",
                    examStyle = session.mode,
                    difficulty = nextDifficulty,
                    language = session.language,
                    syllabusOrNotes = "",
                    questionNumber = nextSeq,
                    totalQuestions = state.totalQuestions,
                    previousQuestion = currentQ?.text,
                    previousAnswer = state.transcript,
                    previousPerformanceSummary = "Last conceptual score: ${lastEval?.conceptualScore ?: 70}"
                )

                val result = geminiService.generateQuestion(qContext).getOrThrow()
                val newQ = VivaQuestion(
                    id = UUID.randomUUID().toString(),
                    sessionId = session.id,
                    sequence = nextSeq,
                    text = result.question,
                    questionType = result.questionType,
                    expectedKeyPointsJson = JSONArray(result.expectedKeyPoints).toString(),
                    difficulty = result.difficulty,
                    estimatedAnswerTimeSeconds = result.estimatedAnswerTimeSeconds,
                    examinerHint = result.examinerHint
                )

                vivaRepository.insertQuestion(newQ)

                _uiState.update {
                    it.copy(
                        isGeneratingNextQuestion = false,
                        currentQuestion = newQ,
                        questionNumber = nextSeq,
                        timeRemainingSeconds = newQ.estimatedAnswerTimeSeconds
                    )
                }
                startQuestionTimer(newQ.estimatedAnswerTimeSeconds)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isGeneratingNextQuestion = false,
                        errorMessage = "Could not generate next question: ${e.message}"
                    )
                }
            }
        }
    }

    fun skipQuestion(onFinished: (String) -> Unit) {
        val state = _uiState.value
        if (state.questionNumber >= state.totalQuestions) {
            finalizeSession(onFinished)
        } else {
            generateNextQuestion(state.questionNumber + 1)
        }
    }

    fun explainTopic() {
        val session = _uiState.value.session ?: return
        viewModelScope.launch {
            val res = geminiService.explainTopic(session.topicTitle, "Undergraduate")
            _uiState.update { it.copy(explanationText = res.getOrNull()) }
        }
    }

    fun dismissExplanation() {
        _uiState.update { it.copy(explanationText = null) }
    }

    fun finalizeSession(onFinished: (String) -> Unit) {
        val session = _uiState.value.session ?: return
        viewModelScope.launch {
            val responses = vivaRepository.getResponsesForSessionSync(session.id)
            val avgConceptual = if (responses.isNotEmpty()) responses.map { it.conceptualScore }.average().toInt() else 0
            val avgCommunication = if (responses.isNotEmpty()) responses.map { it.communicationScore }.average().toInt() else 0
            val overall = ((avgConceptual * 0.6) + (avgCommunication * 0.4)).toInt()

            val updatedSession = session.copy(
                endedAt = System.currentTimeMillis(),
                status = "Completed",
                overallScore = overall,
                conceptualScore = avgConceptual,
                communicationScore = avgCommunication,
                answeredQuestions = responses.size
            )
            vivaRepository.updateSession(updatedSession)

            // Extract Revision Recommendations from responses
            val recommendations = mutableListOf<RevisionRecommendation>()
            responses.forEach { resp ->
                try {
                    val missing = JSONArray(resp.missingPointsJson)
                    for (i in 0 until missing.length()) {
                        recommendations.add(
                            RevisionRecommendation(
                                sessionId = session.id,
                                topic = missing.getString(i),
                                priority = if (resp.conceptualScore < 60) "High" else "Medium",
                                explanation = "Identified as missing in Question: ${resp.correction}"
                            )
                        )
                    }
                } catch (e: Exception) {
                    // Ignore
                }
            }
            if (recommendations.isNotEmpty()) {
                vivaRepository.insertRecommendations(recommendations.take(5))
            }

            _uiState.update { it.copy(sessionFinished = true) }
            onFinished(session.id)
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        recordingTimerJob?.cancel()
        speechService.reset()
    }
}
