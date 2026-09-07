package com.example.data.billing

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SubscriptionPlan(
    val id: String,
    val title: String,
    val usdPrice: String,
    val bdtPrice: String,
    val billingPeriod: String,
    val features: List<String>,
    val popularTag: Boolean = false
)

interface BillingService {
    val isPremium: StateFlow<Boolean>
    val plans: List<SubscriptionPlan>
    suspend fun purchasePlan(planId: String): Result<Boolean>
    suspend fun restorePurchases(): Result<Boolean>
    fun toggleSimulatedPremium()
}

class MockBillingService(initialPremium: Boolean = false) : BillingService {

    private val _isPremium = MutableStateFlow(initialPremium)
    override val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    override val plans: List<SubscriptionPlan> = listOf(
        SubscriptionPlan(
            id = "plan_monthly",
            title = "Monthly Student Pro",
            usdPrice = "$2.49",
            bdtPrice = "৳290",
            billingPeriod = "per month",
            features = listOf(
                "Unlimited AI Viva sessions",
                "Full syllabus & lecture notes examiner",
                "Personalized weak-topic revision plans",
                "Thesis Defense & Lab Examiner modes",
                "Speech transcription & voice analysis"
            ),
            popularTag = true
        ),
        SubscriptionPlan(
            id = "plan_semester",
            title = "Semester Exam Prep Pass",
            usdPrice = "$7.99",
            bdtPrice = "৳890",
            billingPeriod = "for 4 months",
            features = listOf(
                "All Monthly Pro features included",
                "Priority Gemini reasoning response",
                "Exportable PDF revision reports",
                "Multi-subject custom question banks",
                "Zero ads or evaluation limits"
            ),
            popularTag = false
        )
    )

    override suspend fun purchasePlan(planId: String): Result<Boolean> {
        _isPremium.value = true
        return Result.success(true)
    }

    override suspend fun restorePurchases(): Result<Boolean> {
        _isPremium.value = true
        return Result.success(true)
    }

    override fun toggleSimulatedPremium() {
        _isPremium.value = !_isPremium.value
    }
}
