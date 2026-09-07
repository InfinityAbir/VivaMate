package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.RevisionRecommendation
import com.example.data.model.Subject
import com.example.data.model.Topic
import com.example.data.model.VivaQuestion
import com.example.data.model.VivaResponse
import com.example.data.model.VivaSession
import kotlinx.coroutines.flow.Flow

class VivaRepository(private val database: AppDatabase) {

    // Subjects & Topics
    fun getActiveSubjects(): Flow<List<Subject>> = database.subjectDao().getActiveSubjects()
    fun getAllSubjects(): Flow<List<Subject>> = database.subjectDao().getAllSubjects()
    suspend fun getSubjectById(id: String): Subject? = database.subjectDao().getSubjectById(id)
    suspend fun insertSubject(subject: Subject) = database.subjectDao().insertSubject(subject)
    suspend fun updateSubject(subject: Subject) = database.subjectDao().updateSubject(subject)
    suspend fun deleteSubjectById(id: String) {
        database.subjectDao().deleteTopicsForSubject(id)
        database.subjectDao().deleteSubjectById(id)
    }

    fun getTopicsForSubject(subjectId: String): Flow<List<Topic>> = database.subjectDao().getTopicsForSubject(subjectId)
    suspend fun getTopicById(id: String): Topic? = database.subjectDao().getTopicById(id)
    suspend fun insertTopic(topic: Topic) = database.subjectDao().insertTopic(topic)
    suspend fun updateTopic(topic: Topic) = database.subjectDao().updateTopic(topic)
    suspend fun deleteTopic(topic: Topic) = database.subjectDao().deleteTopic(topic)

    // Sessions
    fun getAllSessions(): Flow<List<VivaSession>> = database.sessionDao().getAllSessions()
    fun getRecentSessions(limit: Int = 5): Flow<List<VivaSession>> = database.sessionDao().getRecentSessions(limit)
    suspend fun getSessionById(id: String): VivaSession? = database.sessionDao().getSessionById(id)
    suspend fun insertSession(session: VivaSession) = database.sessionDao().insertSession(session)
    suspend fun updateSession(session: VivaSession) = database.sessionDao().updateSession(session)
    suspend fun deleteSessionById(id: String) {
        database.questionDao().deleteQuestionsForSession(id)
        database.responseDao().deleteResponsesForSession(id)
        database.sessionDao().deleteSessionById(id)
    }
    suspend fun deleteAllSessions() {
        database.questionDao().deleteAllQuestions()
        database.responseDao().deleteAllResponses()
        database.sessionDao().deleteAllSessions()
    }

    fun getAverageOverallScore(): Flow<Double?> = database.sessionDao().getAverageOverallScore()
    fun getCompletedSessionsCount(): Flow<Int> = database.sessionDao().getCompletedSessionsCount()

    // Questions
    fun getQuestionsForSession(sessionId: String): Flow<List<VivaQuestion>> = database.questionDao().getQuestionsForSession(sessionId)
    suspend fun getQuestionsForSessionSync(sessionId: String): List<VivaQuestion> = database.questionDao().getQuestionsForSessionSync(sessionId)
    suspend fun insertQuestion(question: VivaQuestion) = database.questionDao().insertQuestion(question)

    // Responses
    fun getResponsesForSession(sessionId: String): Flow<List<VivaResponse>> = database.responseDao().getResponsesForSession(sessionId)
    suspend fun getResponsesForSessionSync(sessionId: String): List<VivaResponse> = database.responseDao().getResponsesForSessionSync(sessionId)
    suspend fun insertResponse(response: VivaResponse) = database.responseDao().insertResponse(response)
    suspend fun anonymizeTranscriptsForSession(sessionId: String) = database.responseDao().anonymizeTranscriptsForSession(sessionId)
    suspend fun anonymizeAllTranscripts() = database.responseDao().anonymizeAllTranscripts()

    // Recommendations
    fun getPendingRecommendations(): Flow<List<RevisionRecommendation>> = database.recommendationDao().getPendingRecommendations()
    fun getAllRecommendations(): Flow<List<RevisionRecommendation>> = database.recommendationDao().getAllRecommendations()
    fun getRecommendationsForSession(sessionId: String): Flow<List<RevisionRecommendation>> = database.recommendationDao().getRecommendationsForSession(sessionId)
    suspend fun insertRecommendations(recommendations: List<RevisionRecommendation>) = database.recommendationDao().insertRecommendations(recommendations)
    suspend fun setRecommendationCompleted(id: String, completed: Boolean) = database.recommendationDao().setRecommendationCompleted(id, completed)
    suspend fun deleteAllRecommendations() = database.recommendationDao().deleteAllRecommendations()

    // Privacy Wipe
    suspend fun wipeAllUserData() {
        database.clearAllTables()
    }
}
