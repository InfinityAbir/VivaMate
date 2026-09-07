package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.VivaSession
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM viva_sessions ORDER BY startedAt DESC")
    fun getAllSessions(): Flow<List<VivaSession>>

    @Query("SELECT * FROM viva_sessions ORDER BY startedAt DESC LIMIT :limit")
    fun getRecentSessions(limit: Int = 5): Flow<List<VivaSession>>

    @Query("SELECT * FROM viva_sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionById(id: String): VivaSession?

    @Query("SELECT * FROM viva_sessions WHERE subjectId = :subjectId ORDER BY startedAt DESC")
    fun getSessionsForSubject(subjectId: String): Flow<List<VivaSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: VivaSession)

    @Update
    suspend fun updateSession(session: VivaSession)

    @Query("DELETE FROM viva_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: String)

    @Query("DELETE FROM viva_sessions")
    suspend fun deleteAllSessions()

    @Query("SELECT AVG(overallScore) FROM viva_sessions WHERE status = 'Completed'")
    fun getAverageOverallScore(): Flow<Double?>

    @Query("SELECT COUNT(*) FROM viva_sessions WHERE status = 'Completed'")
    fun getCompletedSessionsCount(): Flow<Int>
}
