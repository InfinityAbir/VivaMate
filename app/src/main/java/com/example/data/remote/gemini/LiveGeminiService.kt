package com.example.data.remote.gemini

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.VivaEvaluationContext
import com.example.data.model.VivaEvaluationResult
import com.example.data.model.VivaQuestionContext
import com.example.data.model.VivaQuestionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class LiveGeminiService(
    private val fallbackService: GeminiService = MockGeminiService()
) : GeminiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val apiKey: String
        get() = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

    private fun isApiKeyValid(): Boolean {
        val key = apiKey.trim()
        return key.isNotBlank() && key != "MY_GEMINI_API_KEY" && !key.startsWith("placeholder", ignoreCase = true)
    }

    override suspend fun generateQuestion(context: VivaQuestionContext): Result<VivaQuestionResult> = withContext(Dispatchers.IO) {
        if (!isApiKeyValid()) {
            return@withContext fallbackService.generateQuestion(context)
        }

        try {
            val prompt = """
                You are a fair oral-exam examiner. Create exactly one clear question within the selected subject, topic, academic level, difficulty, and supplied syllabus.
                Use prior answers only to adjust difficulty and select a useful follow-up question. Do not assume facts not provided in the syllabus.
                Return valid JSON only with: question, questionType, expectedKeyPoints (array of strings), difficulty, estimatedAnswerTimeSeconds (int), examinerHint.

                Subject: ${context.subjectName}
                Topic: ${context.topicTitle}
                Academic Level: ${context.academicLevel}
                Exam Style: ${context.examStyle}
                Difficulty: ${context.difficulty}
                Language: ${context.language}
                Syllabus/Notes: ${context.syllabusOrNotes}
                Question Number: ${context.questionNumber} of ${context.totalQuestions}
                Previous Question: ${context.previousQuestion ?: "None"}
                Previous Answer: ${context.previousAnswer ?: "None"}
                Prior Performance: ${context.previousPerformanceSummary ?: "First question"}
                Weak Areas: ${context.weakAreas.joinToString()}
            """.trimIndent()

            val responseText = callGeminiApi(prompt)
            val json = extractJson(responseText)
            val questionObj = JSONObject(json)

            val expectedKeyPoints = mutableListOf<String>()
            val pointsArray = questionObj.optJSONArray("expectedKeyPoints")
            if (pointsArray != null) {
                for (i in 0 until pointsArray.length()) {
                    expectedKeyPoints.add(pointsArray.getString(i))
                }
            }

            Result.success(
                VivaQuestionResult(
                    question = questionObj.optString("question", "Explain the primary principles of ${context.topicTitle}."),
                    questionType = questionObj.optString("questionType", "concept"),
                    expectedKeyPoints = if (expectedKeyPoints.isNotEmpty()) expectedKeyPoints else listOf("Fundamental concept", "Operational mechanism", "Application"),
                    difficulty = questionObj.optString("difficulty", context.difficulty),
                    estimatedAnswerTimeSeconds = questionObj.optInt("estimatedAnswerTimeSeconds", 60),
                    examinerHint = questionObj.optString("examinerHint", "Be concise and structure your explanation logically.")
                )
            )
        } catch (e: Exception) {
            Log.w("LiveGeminiService", "Live question generation failed, using mock fallback: ${e.message}")
            fallbackService.generateQuestion(context)
        }
    }

    override suspend fun evaluateAnswer(context: VivaEvaluationContext): Result<VivaEvaluationResult> = withContext(Dispatchers.IO) {
        if (!isApiKeyValid()) {
            return@withContext fallbackService.evaluateAnswer(context)
        }

        try {
            val prompt = """
                You are a supportive oral-exam evaluator. Evaluate the student’s answer against the expected key points.
                Return valid JSON only with:
                - conceptualScore (0 to 100 integer)
                - communicationScore (0 to 100 integer)
                - strengths (array of strings)
                - missingPoints (array of strings)
                - misconceptions (array of strings)
                - conciseCorrection (string)
                - modelAnswer (string)
                - suggestedRevisionTopics (array of strings)
                - nextQuestionDifficulty ("Beginner", "Intermediate", or "Advanced")
                - needsClarification (boolean)
                - clarificationQuestion (string)

                Be encouraging but accurate. Never claim official exam authority.
                If the answer is incomplete or unclear, set needsClarification to true and ask one short follow-up question.

                Subject: ${context.subjectName}
                Topic: ${context.topicTitle}
                Academic Level: ${context.academicLevel}
                Question: ${context.questionText}
                Expected Key Points: ${context.expectedKeyPoints.joinToString()}
                Current Difficulty: ${context.currentDifficulty}
                Language: ${context.language}
                Student Answer: ${context.userAnswer}
            """.trimIndent()

            val responseText = callGeminiApi(prompt)
            val json = extractJson(responseText)
            val evalObj = JSONObject(json)

            fun optStringList(key: String): List<String> {
                val list = mutableListOf<String>()
                val arr = evalObj.optJSONArray(key) ?: return list
                for (i in 0 until arr.length()) {
                    list.add(arr.getString(i))
                }
                return list
            }

            Result.success(
                VivaEvaluationResult(
                    conceptualScore = evalObj.optInt("conceptualScore", 70),
                    communicationScore = evalObj.optInt("communicationScore", 75),
                    strengths = optStringList("strengths"),
                    missingPoints = optStringList("missingPoints"),
                    misconceptions = optStringList("misconceptions"),
                    conciseCorrection = evalObj.optString("conciseCorrection", "Focus on clear cause-and-effect explanations."),
                    modelAnswer = evalObj.optString("modelAnswer", "A strong answer highlights the foundational mechanism, followed by the operational trade-offs."),
                    suggestedRevisionTopics = optStringList("suggestedRevisionTopics"),
                    nextQuestionDifficulty = evalObj.optString("nextQuestionDifficulty", "Intermediate"),
                    needsClarification = evalObj.optBoolean("needsClarification", false),
                    clarificationQuestion = evalObj.optString("clarificationQuestion", "")
                )
            )
        } catch (e: Exception) {
            Log.w("LiveGeminiService", "Live answer evaluation failed, using mock fallback: ${e.message}")
            fallbackService.evaluateAnswer(context)
        }
    }

    override suspend fun explainTopic(topic: String, academicLevel: String): Result<String> = withContext(Dispatchers.IO) {
        if (!isApiKeyValid()) {
            return@withContext fallbackService.explainTopic(topic, academicLevel)
        }

        try {
            val prompt = "Provide a high-yield, 3-bullet oral viva revision summary for the academic topic '$topic' at $academicLevel level. Keep it concise, high-impact, and exam-focused."
            val responseText = callGeminiApi(prompt)
            Result.success(responseText)
        } catch (e: Exception) {
            fallbackService.explainTopic(topic, academicLevel)
        }
    }

    private fun callGeminiApi(prompt: String): String {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val requestJson = JSONObject().apply {
            val contentsArray = JSONArray().apply {
                val contentObj = JSONObject().apply {
                    val partsArray = JSONArray().apply {
                        val partObj = JSONObject().apply {
                            put("text", prompt)
                        }
                        put(partObj)
                    }
                    put("parts", partsArray)
                }
                put(contentObj)
            }
            put("contents", contentsArray)

            val genConfig = JSONObject().apply {
                put("temperature", 0.3)
                put("topP", 0.9)
            }
            put("generationConfig", genConfig)
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = requestJson.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("Gemini API call failed with code ${response.code}: ${response.message}")
            }
            val responseBody = response.body?.string() ?: throw IllegalStateException("Empty response from Gemini API")
            val root = JSONObject(responseBody)
            val candidates = root.optJSONArray("candidates") ?: throw IllegalStateException("No candidates in Gemini response")
            if (candidates.length() == 0) throw IllegalStateException("Empty candidates list")
            val content = candidates.getJSONObject(0).optJSONObject("content") ?: throw IllegalStateException("No content in candidate")
            val parts = content.optJSONArray("parts") ?: throw IllegalStateException("No parts in content")
            if (parts.length() == 0) throw IllegalStateException("Empty parts list")
            return parts.getJSONObject(0).optString("text", "")
        }
    }

    private fun extractJson(text: String): String {
        val trimmed = text.trim()
        val startIndex = trimmed.indexOf('{')
        val endIndex = trimmed.lastIndexOf('}')
        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            return trimmed.substring(startIndex, endIndex + 1)
        }
        return trimmed
    }
}
