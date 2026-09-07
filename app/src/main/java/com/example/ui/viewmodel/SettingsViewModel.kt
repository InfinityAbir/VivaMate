package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.billing.BillingService
import com.example.data.billing.SubscriptionPlan
import com.example.data.model.UserSettings
import com.example.data.repository.SettingsRepository
import com.example.data.repository.VivaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val settings: UserSettings = UserSettings(),
    val isPremium: Boolean = false,
    val plans: List<SubscriptionPlan> = emptyList(),
    val isPerformingDataAction: Boolean = false,
    val message: String? = null
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val vivaRepository: VivaRepository,
    private val billingService: BillingService
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.userSettings,
        billingService.isPremium
    ) { settings, isPremium ->
        SettingsUiState(
            settings = settings.copy(isPremium = isPremium),
            isPremium = isPremium,
            plans = billingService.plans
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState(plans = billingService.plans)
    )

    fun setTheme(theme: String) {
        viewModelScope.launch {
            settingsRepository.setTheme(theme)
        }
    }

    fun setLanguage(language: String) {
        viewModelScope.launch {
            settingsRepository.setLanguage(language)
        }
    }

    fun setRetainTranscripts(retain: Boolean) {
        viewModelScope.launch {
            settingsRepository.setRetainTranscripts(retain)
        }
    }

    fun setRetainAudio(retain: Boolean) {
        viewModelScope.launch {
            settingsRepository.setRetainAudio(retain)
        }
    }

    fun setAnalyticsConsent(consent: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAnalyticsConsent(consent)
        }
    }

    fun setReduceMotion(reduce: Boolean) {
        viewModelScope.launch {
            settingsRepository.setReduceMotion(reduce)
        }
    }

    fun setDailyReminder(reminder: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDailyReminder(reminder)
        }
    }

    fun anonymizeAllTranscripts(onComplete: () -> Unit) {
        viewModelScope.launch {
            vivaRepository.anonymizeAllTranscripts()
            onComplete()
        }
    }

    fun deleteAllSessions(onComplete: () -> Unit) {
        viewModelScope.launch {
            vivaRepository.deleteAllSessions()
            onComplete()
        }
    }

    fun wipeAllData(onComplete: () -> Unit) {
        viewModelScope.launch {
            vivaRepository.wipeAllUserData()
            settingsRepository.clearAllPreferences()
            onComplete()
        }
    }

    fun purchasePlan(planId: String, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            val res = billingService.purchasePlan(planId)
            onDone(res.isSuccess)
        }
    }

    fun restorePurchases(onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            val res = billingService.restorePurchases()
            onDone(res.isSuccess)
        }
    }

    fun toggleSimulatedPremium() {
        billingService.toggleSimulatedPremium()
    }
}
