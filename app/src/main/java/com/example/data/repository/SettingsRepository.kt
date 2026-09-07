package com.example.data.repository

import com.example.data.local.preferences.PreferencesDataStore
import com.example.data.model.UserSettings
import kotlinx.coroutines.flow.Flow

class SettingsRepository(private val preferencesDataStore: PreferencesDataStore) {

    val userSettings: Flow<UserSettings> = preferencesDataStore.userSettings

    suspend fun setTheme(theme: String) = preferencesDataStore.setTheme(theme)
    suspend fun setLanguage(language: String) = preferencesDataStore.setLanguage(language)
    suspend fun setRetainTranscripts(retain: Boolean) = preferencesDataStore.setRetainTranscripts(retain)
    suspend fun setRetainAudio(retain: Boolean) = preferencesDataStore.setRetainAudio(retain)
    suspend fun setAnalyticsConsent(consent: Boolean) = preferencesDataStore.setAnalyticsConsent(consent)
    suspend fun setDailyReminder(enabled: Boolean) = preferencesDataStore.setDailyReminder(enabled)
    suspend fun setReduceMotion(enabled: Boolean) = preferencesDataStore.setReduceMotion(enabled)
    suspend fun setPremium(isPremium: Boolean) = preferencesDataStore.setPremium(isPremium)
    suspend fun incrementFreeSessionsUsed() = preferencesDataStore.incrementFreeSessionsUsed()
    suspend fun setOnboardingCompleted(completed: Boolean) = preferencesDataStore.setOnboardingCompleted(completed)
    suspend fun setUseMockAi(useMock: Boolean) = preferencesDataStore.setUseMockAi(useMock)
    suspend fun clearAllPreferences() = preferencesDataStore.clearAllPreferences()
}
