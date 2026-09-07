package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val level: String = "Undergraduate", // Undergraduate, Postgraduate, High School, Diploma, Professional
    val color: Long = 0xFF4F46E5, // Default Indigo
    val createdAt: Long = System.currentTimeMillis(),
    val archived: Boolean = false
)

@Entity(tableName = "topics")
data class Topic(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val subjectId: String,
    val title: String,
    val syllabusText: String = "",
    val importance: String = "High", // High, Medium, Standard
    val targetConfidence: Int = 80
)

@Entity(tableName = "viva_sessions")
data class VivaSession(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val subjectId: String,
    val topicId: String,
    val subjectName: String,
    val topicTitle: String,
    val startedAt: Long = System.currentTimeMillis(),
    val endedAt: Long? = null,
    val mode: String = "Viva", // Viva, Lab Viva, Thesis Defense, Interview, Rapid Practice
    val difficulty: String = "Adaptive", // Beginner, Intermediate, Advanced, Adaptive
    val language: String = "English",
    val overallScore: Int = 0,
    val conceptualScore: Int = 0,
    val communicationScore: Int = 0,
    val status: String = "In Progress", // In Progress, Completed, Abandoned
    val totalQuestions: Int = 5,
    val answeredQuestions: Int = 0
)

@Entity(tableName = "viva_questions")
data class VivaQuestion(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val sessionId: String,
    val sequence: Int,
    val text: String,
    val questionType: String = "concept", // definition, concept, comparison, application, troubleshooting, practical_lab, follow_up
    val expectedKeyPointsJson: String = "[]",
    val difficulty: String = "Intermediate",
    val estimatedAnswerTimeSeconds: Int = 60,
    val examinerHint: String = "",
    val generatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "viva_responses")
data class VivaResponse(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val questionId: String,
    val sessionId: String,
    val answerMode: String = "Voice", // Voice, Text
    val transcript: String = "",
    val submittedAt: Long = System.currentTimeMillis(),
    val conceptualScore: Int = 0,
    val communicationScore: Int = 0,
    val strengthsJson: String = "[]",
    val missingPointsJson: String = "[]",
    val misconceptionsJson: String = "[]",
    val correction: String = "",
    val modelAnswer: String = "",
    val suggestedRevisionTopicsJson: String = "[]",
    val nextQuestionDifficulty: String = "Intermediate",
    val clarificationQuestion: String? = null
)

@Entity(tableName = "revision_recommendations")
data class RevisionRecommendation(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val sessionId: String,
    val topic: String,
    val priority: String = "High", // High, Medium, Low
    val explanation: String = "",
    val completed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class VivaQuestionContext(
    val subjectName: String,
    val topicTitle: String,
    val academicLevel: String,
    val examStyle: String,
    val difficulty: String,
    val language: String,
    val syllabusOrNotes: String,
    val questionNumber: Int,
    val totalQuestions: Int,
    val previousQuestion: String? = null,
    val previousAnswer: String? = null,
    val previousPerformanceSummary: String? = null,
    val weakAreas: List<String> = emptyList()
)

data class VivaQuestionResult(
    val question: String,
    val questionType: String,
    val expectedKeyPoints: List<String>,
    val difficulty: String,
    val estimatedAnswerTimeSeconds: Int = 60,
    val examinerHint: String = ""
)

data class VivaEvaluationContext(
    val subjectName: String,
    val topicTitle: String,
    val academicLevel: String,
    val questionText: String,
    val questionType: String,
    val expectedKeyPoints: List<String>,
    val userAnswer: String,
    val currentDifficulty: String,
    val language: String = "English"
)

data class VivaEvaluationResult(
    val conceptualScore: Int,
    val communicationScore: Int,
    val strengths: List<String>,
    val missingPoints: List<String>,
    val misconceptions: List<String>,
    val conciseCorrection: String,
    val modelAnswer: String,
    val suggestedRevisionTopics: List<String>,
    val nextQuestionDifficulty: String,
    val needsClarification: Boolean = false,
    val clarificationQuestion: String = ""
)

data class UserSettings(
    val language: String = "English",
    val theme: String = "System", // System, Light, Dark
    val retainTranscripts: Boolean = true,
    val retainAudio: Boolean = false,
    val analyticsConsent: Boolean = true,
    val dailyReminderEnabled: Boolean = false,
    val reduceMotionEnabled: Boolean = false,
    val isPremium: Boolean = false,
    val freeSessionsUsed: Int = 0,
    val onboardingCompleted: Boolean = false,
    val useMockAi: Boolean = false
)
