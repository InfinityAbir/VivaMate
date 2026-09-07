package com.example.data.remote.gemini

import com.example.data.model.VivaEvaluationContext
import com.example.data.model.VivaEvaluationResult
import com.example.data.model.VivaQuestionContext
import com.example.data.model.VivaQuestionResult

interface GeminiService {
    suspend fun generateQuestion(context: VivaQuestionContext): Result<VivaQuestionResult>
    suspend fun evaluateAnswer(context: VivaEvaluationContext): Result<VivaEvaluationResult>
    suspend fun explainTopic(topic: String, academicLevel: String): Result<String>
}

object GeminiServiceFactory {
    fun create(apiKey: String): GeminiService {
        return LiveGeminiService()
    }
}
