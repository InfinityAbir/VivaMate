package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.VivaResponse
import kotlinx.coroutines.flow.Flow

@Dao
interface ResponseDao {
    @Query("SELECT * FROM viva_responses WHERE sessionId = :sessionId ORDER BY submittedAt ASC")
    fun getResponsesForSession(sessionId: String): Flow<List<VivaResponse>>

    @Query("SELECT * FROM viva_responses WHERE sessionId = :sessionId ORDER BY submittedAt ASC")
    suspend fun getResponsesForSessionSync(sessionId: String): List<VivaResponse>

    @Query("SELECT * FROM viva_responses WHERE questionId = :questionId LIMIT 1")
    suspend fun getResponseForQuestion(questionId: String): VivaResponse?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResponse(response: VivaResponse)

    @Query("DELETE FROM viva_responses WHERE sessionId = :sessionId")
    suspend fun deleteResponsesForSession(sessionId: String)

    @Query("DELETE FROM viva_responses")
    suspend fun deleteAllResponses()

    @Query("UPDATE viva_responses SET transcript = '[Deleted by user]' WHERE sessionId = :sessionId")
    suspend fun anonymizeTranscriptsForSession(sessionId: String)

    @Query("UPDATE viva_responses SET transcript = '[Deleted by user]'")
    suspend fun anonymizeAllTranscripts()
}
