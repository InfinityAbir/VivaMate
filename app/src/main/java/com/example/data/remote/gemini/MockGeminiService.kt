package com.example.data.remote.gemini

import com.example.data.model.VivaEvaluationContext
import com.example.data.model.VivaEvaluationResult
import com.example.data.model.VivaQuestionContext
import com.example.data.model.VivaQuestionResult
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.min

class MockGeminiService : GeminiService {

    override suspend fun generateQuestion(context: VivaQuestionContext): Result<VivaQuestionResult> {
        delay(600) // Realistic network delay simulation
        val isBangla = context.language.equals("Bangla", ignoreCase = true) || context.language.equals("bn", ignoreCase = true)

        val questionPool = getSubjectQuestions(context.subjectName, context.topicTitle, isBangla)
        val selected = questionPool.getOrElse(context.questionNumber - 1) {
            // Dynamic fallback for higher question numbers
            if (isBangla) {
                VivaQuestionResult(
                    question = "${context.topicTitle}-এর বাস্তব প্রয়োগ এবং মূল চ্যালেঞ্জগুলো উদাহরণসহ ব্যাখ্যা করুন।",
                    questionType = "application",
                    expectedKeyPoints = listOf("মৌলিক ধারণা ও কার্যপদ্ধতি", "বাস্তব ব্যবহারের ক্ষেত্র", "সম্ভাব্য সমস্যা ও সমাধান"),
                    difficulty = context.difficulty,
                    estimatedAnswerTimeSeconds = 60,
                    examinerHint = "বাস্তব উদাহরণ ও প্রযুক্তিগত যুক্তি তুলে ধরুন।"
                )
            } else {
                VivaQuestionResult(
                    question = "Discuss the practical trade-offs and edge cases encountered when deploying ${context.topicTitle} in production systems.",
                    questionType = "practical_lab",
                    expectedKeyPoints = listOf("Scalability constraints", "Failure recovery strategies", "Resource trade-offs"),
                    difficulty = context.difficulty,
                    estimatedAnswerTimeSeconds = 60,
                    examinerHint = "Focus on real-world constraints and architectural decisions."
                )
            }
        }

        // Adapt difficulty if requested
        val effectiveDifficulty = when {
            context.difficulty == "Adaptive" && context.questionNumber == 1 -> "Beginner"
            context.difficulty == "Adaptive" && context.questionNumber in 2..3 -> "Intermediate"
            context.difficulty == "Adaptive" && context.questionNumber >= 4 -> "Advanced"
            else -> context.difficulty
        }

        return Result.success(selected.copy(difficulty = effectiveDifficulty))
    }

    override suspend fun evaluateAnswer(context: VivaEvaluationContext): Result<VivaEvaluationResult> {
        delay(800) // Realistic evaluation delay
        val isBangla = context.language.equals("Bangla", ignoreCase = true) || context.language.equals("bn", ignoreCase = true)

        val wordCount = context.userAnswer.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }.size
        val answerLower = context.userAnswer.lowercase()

        // Match key points
        var matchedPoints = 0
        for (point in context.expectedKeyPoints) {
            val wordsInPoint = point.lowercase().split("\\s+".toRegex()).filter { it.length > 3 }
            if (wordsInPoint.any { answerLower.contains(it) }) {
                matchedPoints++
            }
        }

        val conceptualScore = when {
            wordCount < 4 -> 25
            wordCount < 12 -> 45
            matchedPoints >= 2 -> min(95, 70 + (matchedPoints * 10))
            matchedPoints == 1 -> 65
            else -> min(78, 50 + (wordCount * 2))
        }

        val communicationScore = when {
            wordCount < 5 -> 30
            wordCount < 15 -> 60
            wordCount > 40 -> 92
            else -> min(90, 65 + (wordCount / 2))
        }

        val nextDifficulty = when {
            conceptualScore >= 80 -> "Advanced"
            conceptualScore >= 50 -> "Intermediate"
            else -> "Beginner"
        }

        val strengths = if (isBangla) {
            if (conceptualScore >= 60) {
                listOf(
                    "প্রশ্নের মূল প্রসঙ্গের সঠিক অবতারণা করেছেন।",
                    "বিষয়ভিত্তিক শব্দচয়ন এবং স্পষ্ট উপস্থাপনা ছিল।"
                )
            } else {
                listOf("চেষ্টা প্রশংসনীয়, উত্তর প্রাসঙ্গিক করার উদ্যোগ ভালো ছিল।")
            }
        } else {
            if (conceptualScore >= 60) {
                listOf(
                    "Demonstrated clear grasp of foundational terminology.",
                    "Articulated the core mechanism with good verbal clarity."
                )
            } else {
                listOf("Attempted to address the prompt directly.")
            }
        }

        val missingPoints = if (isBangla) {
            if (conceptualScore < 85) {
                listOf(
                    "বাস্তব প্রয়োগের ক্ষেত্র এবং সীমাবদ্ধতা আরও বিশদ করা যেত।",
                    "মূল টার্মিনোলজি ও গাণিতিক/যৌক্তিক বিশ্লেষণ যুক্ত করলে পূর্ণাঙ্গ হতো।"
                )
            } else {
                listOf("বিকল্প আর্কিটেকচারাল সমাধান আরও সংক্ষেপে উল্লেখ করা যেতো।")
            }
        } else {
            if (conceptualScore < 85) {
                listOf(
                    "Did not explicitly articulate the operational boundary conditions.",
                    "Omitted key comparison with alternative architectures or failure modes."
                )
            } else {
                listOf("Could briefly mention minor latency or resource constraints.")
            }
        }

        val misconceptions = if (conceptualScore < 50) {
            if (isBangla) {
                listOf("তত্ত্বগত ধারণার সাথে বাস্তব কার্যপ্রণালীর মিশ্রণ ঘটেছে।")
            } else {
                listOf("Confused theoretical ideal behavior with actual implementation constraints.")
            }
        } else emptyList()

        val conciseCorrection = if (isBangla) {
            "সংক্ষেপে: মূল নীতি পরিষ্কার রেখে ধাপে ধাপে কারণ ও ফলাফল ব্যাখ্যা করুন।"
        } else {
            "Tip: State the fundamental rule first, then follow up with the causal mechanism and direct outcome."
        }

        val modelAnswer = if (isBangla) {
            "আদর্শ উত্তর: প্রথমে সুনির্দিষ্ট সংজ্ঞা দিন, এরপর অভ্যন্তরীণ আর্কিটেকচার এবং বাস্তব প্রয়োগে কীভাবে দক্ষতা বৃদ্ধি করে তা ২টি মূল উদাহরণ দিয়ে সমাপ্ত করুন।"
        } else {
            "Model Answer: Start with the concise definition: clearly identify the role, explain the step-by-step state transition or data flow, and conclude with the trade-offs (e.g. time vs space, accuracy vs latency)."
        }

        val suggestedRevision = listOf(
            context.topicTitle,
            "Edge Cases & Real-world Trade-offs",
            "Comparative Performance Analysis"
        )

        val needsClarification = wordCount in 4..10
        val clarificationQuestion = if (needsClarification) {
            if (isBangla) {
                "আপনি কি একটু বিস্তারিত বলবেন কীভাবে এই উপাদানটি সিস্টেমের মেমোরি বা গতিতে প্রভাব ফেলে?"
            } else {
                "Could you briefly elaborate on how this affects throughput or memory overhead?"
            }
        } else ""

        return Result.success(
            VivaEvaluationResult(
                conceptualScore = conceptualScore,
                communicationScore = communicationScore,
                strengths = strengths,
                missingPoints = missingPoints,
                misconceptions = misconceptions,
                conciseCorrection = conciseCorrection,
                modelAnswer = modelAnswer,
                suggestedRevisionTopics = suggestedRevision,
                nextQuestionDifficulty = nextDifficulty,
                needsClarification = needsClarification,
                clarificationQuestion = clarificationQuestion
            )
        )
    }

    override suspend fun explainTopic(topic: String, academicLevel: String): Result<String> {
        delay(400)
        return Result.success(
            "Quick Revision on '$topic' ($academicLevel):\n\n" +
            "1. Core Principle: Focus on the fundamental definitions and structural relations.\n" +
            "2. Critical Trade-offs: Examiners typically look for why one method is chosen over another in actual implementations.\n" +
            "3. Common Traps: Avoid confusing theoretical guarantees with hardware/environment realities.\n" +
            "4. Oral Exam Strategy: State your conclusion in the first 10 seconds, then support it with two solid technical facts."
        )
    }

    private fun getSubjectQuestions(subject: String, topic: String, isBangla: Boolean): List<VivaQuestionResult> {
        if (isBangla) {
            return listOf(
                VivaQuestionResult(
                    question = "$topic-এর মৌলিক সংজ্ঞা এবং মূল উদ্দেশ্য কী?",
                    questionType = "definition",
                    expectedKeyPoints = listOf("সংজ্ঞা", "মূল উদ্দেশ্য", "প্রধান সুবিধা"),
                    difficulty = "Beginner",
                    estimatedAnswerTimeSeconds = 45,
                    examinerHint = "সংক্ষেপে দুই বাক্যে উত্তর দিন।"
                ),
                VivaQuestionResult(
                    question = "এটি কীভাবে কাজ করে? এর অভ্যন্তরীণ মেকানিজম বা অ্যালগরিদম ধাপগুলো ব্যাখ্যা করুন।",
                    questionType = "concept",
                    expectedKeyPoints = listOf("ধাপসমূহ", "মেমোরি কাঠামো", "ইনপুট-আউটপুট সম্পর্ক"),
                    difficulty = "Intermediate",
                    estimatedAnswerTimeSeconds = 60,
                    examinerHint = "ধাপে ধাপে ফ্লো বর্ণনা করুন।"
                ),
                VivaQuestionResult(
                    question = "এর বিকল্প কৌশলের সাথে এর প্রধান পার্থক্য বা তুলনামূলক সুবিধা কী?",
                    questionType = "comparison",
                    expectedKeyPoints = listOf("গতি ও মেমোরি জটিলতা", "ব্যবহারিক পার্থক্য", "উপযুক্ত ক্ষেত্র"),
                    difficulty = "Intermediate",
                    estimatedAnswerTimeSeconds = 60,
                    examinerHint = "টাইম ও স্পেস কমপ্লেক্সিটি উল্লেখ করুন।"
                ),
                VivaQuestionResult(
                    question = "ল্যাব বা প্রোডাকশন পরিবেশে কোনো এরর বা ফেইলিউর হলে কীভাবে ডিবাগ করবেন?",
                    questionType = "troubleshooting",
                    expectedKeyPoints = listOf("ফল্ট ডিটেকশন", "লগিং", "রিসার্চ ও রিকভারি"),
                    difficulty = "Advanced",
                    estimatedAnswerTimeSeconds = 75,
                    examinerHint = "বাস্তব ডিবাগিং মেথডোলজি তুলে ধরুন।"
                ),
                VivaQuestionResult(
                    question = "$topic-এর অন্যতম সীমাবদ্ধতা কী এবং আধুনিক গবেষণায় কীভাবে এর সমাধান করা হচ্ছে?",
                    questionType = "follow_up",
                    expectedKeyPoints = listOf("সীমাবদ্ধতা", "বটলাইনেক", "ভবিষ্যত দৃষ্টিভঙ্গি"),
                    difficulty = "Advanced",
                    estimatedAnswerTimeSeconds = 60,
                    examinerHint = "উন্নত গবেষণা বা ইন্ডাস্ট্রি স্ট্যান্ডার্ড উল্লেখ করুন।"
                )
            )
        }

        // English subjects
        return listOf(
            VivaQuestionResult(
                question = "How would you concisely define $topic, and what primary problem does it solve?",
                questionType = "definition",
                expectedKeyPoints = listOf("Formal definition", "Core problem statement", "Fundamental objective"),
                difficulty = "Beginner",
                estimatedAnswerTimeSeconds = 45,
                examinerHint = "Aim for a punchy 2-3 sentence overview."
            ),
            VivaQuestionResult(
                question = "Walk me through the underlying mechanism or algorithm of $topic step by step.",
                questionType = "concept",
                expectedKeyPoints = listOf("Step-by-step state changes", "Data invariants", "Input validation"),
                difficulty = "Intermediate",
                estimatedAnswerTimeSeconds = 60,
                examinerHint = "Trace a typical operational cycle."
            ),
            VivaQuestionResult(
                question = "How does $topic compare against its primary alternative in terms of time, space, or resource overhead?",
                questionType = "comparison",
                expectedKeyPoints = listOf("Time/space complexity", "Resource trade-offs", "When to select which approach"),
                difficulty = "Intermediate",
                estimatedAnswerTimeSeconds = 60,
                examinerHint = "Mention Big-O notation or physical engineering boundaries."
            ),
            VivaQuestionResult(
                question = "In a laboratory or production environment, what is the most common failure mode for $topic, and how would you diagnose it?",
                questionType = "troubleshooting",
                expectedKeyPoints = listOf("Symptom identification", "Diagnostic telemetry/metrics", "Corrective recovery steps"),
                difficulty = "Advanced",
                estimatedAnswerTimeSeconds = 75,
                examinerHint = "Think like a senior engineer or lab director."
            ),
            VivaQuestionResult(
                question = "What are the fundamental theoretical limitations of $topic when pushed to extreme scale or high concurrency?",
                questionType = "follow_up",
                expectedKeyPoints = listOf("Bottlenecks", "Amdahl's law / physics boundaries", "Modern mitigations"),
                difficulty = "Advanced",
                estimatedAnswerTimeSeconds = 60,
                examinerHint = "Tie together theory and edge-case limits."
            )
        )
    }
}
