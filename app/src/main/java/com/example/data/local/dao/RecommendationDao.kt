package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.RevisionRecommendation
import kotlinx.coroutines.flow.Flow

@Dao
interface RecommendationDao {
    @Query("SELECT * FROM revision_recommendations WHERE completed = 0 ORDER BY priority DESC, createdAt DESC")
    fun getPendingRecommendations(): Flow<List<RevisionRecommendation>>

    @Query("SELECT * FROM revision_recommendations ORDER BY createdAt DESC")
    fun getAllRecommendations(): Flow<List<RevisionRecommendation>>

    @Query("SELECT * FROM revision_recommendations WHERE sessionId = :sessionId")
    fun getRecommendationsForSession(sessionId: String): Flow<List<RevisionRecommendation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecommendations(recommendations: List<RevisionRecommendation>)

    @Update
    suspend fun updateRecommendation(recommendation: RevisionRecommendation)

    @Query("UPDATE revision_recommendations SET completed = :completed WHERE id = :id")
    suspend fun setRecommendationCompleted(id: String, completed: Boolean)

    @Query("DELETE FROM revision_recommendations WHERE id = :id")
    suspend fun deleteRecommendation(id: String)

    @Query("DELETE FROM revision_recommendations")
    suspend fun deleteAllRecommendations()
}
