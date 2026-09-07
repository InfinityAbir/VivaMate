package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.VivaQuestion
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {
    @Query("SELECT * FROM viva_questions WHERE sessionId = :sessionId ORDER BY sequence ASC")
    fun getQuestionsForSession(sessionId: String): Flow<List<VivaQuestion>>

    @Query("SELECT * FROM viva_questions WHERE sessionId = :sessionId ORDER BY sequence ASC")
    suspend fun getQuestionsForSessionSync(sessionId: String): List<VivaQuestion>

    @Query("SELECT * FROM viva_questions WHERE id = :id LIMIT 1")
    suspend fun getQuestionById(id: String): VivaQuestion?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: VivaQuestion)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<VivaQuestion>)

    @Query("DELETE FROM viva_questions WHERE sessionId = :sessionId")
    suspend fun deleteQuestionsForSession(sessionId: String)

    @Query("DELETE FROM viva_questions")
    suspend fun deleteAllQuestions()
}
