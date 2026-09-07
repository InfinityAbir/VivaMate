package com.example.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "vivamate_preferences")

class PreferencesDataStore(private val context: Context) {

    private object Keys {
        val LANGUAGE = stringPreferencesKey("language")
        val THEME = stringPreferencesKey("theme")
        val RETAIN_TRANSCRIPTS = booleanPreferencesKey("retain_transcripts")
        val RETAIN_AUDIO = booleanPreferencesKey("retain_audio")
        val ANALYTICS_CONSENT = booleanPreferencesKey("analytics_consent")
        val DAILY_REMINDER = booleanPreferencesKey("daily_reminder")
        val REDUCE_MOTION = booleanPreferencesKey("reduce_motion")
        val IS_PREMIUM = booleanPreferencesKey("is_premium")
        val FREE_SESSIONS_USED = intPreferencesKey("free_sessions_used")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val USE_MOCK_AI = booleanPreferencesKey("use_mock_ai")
    }

    val userSettings: Flow<UserSettings> = context.dataStore.data.map { pref ->
        UserSettings(
            language = pref[Keys.LANGUAGE] ?: "English",
            theme = pref[Keys.THEME] ?: "System",
            retainTranscripts = pref[Keys.RETAIN_TRANSCRIPTS] ?: true,
            retainAudio = pref[Keys.RETAIN_AUDIO] ?: false,
            analyticsConsent = pref[Keys.ANALYTICS_CONSENT] ?: true,
            dailyReminderEnabled = pref[Keys.DAILY_REMINDER] ?: false,
            reduceMotionEnabled = pref[Keys.REDUCE_MOTION] ?: false,
            isPremium = pref[Keys.IS_PREMIUM] ?: false,
            freeSessionsUsed = pref[Keys.FREE_SESSIONS_USED] ?: 0,
            onboardingCompleted = pref[Keys.ONBOARDING_COMPLETED] ?: false,
            useMockAi = pref[Keys.USE_MOCK_AI] ?: false
        )
    }

    suspend fun setTheme(theme: String) {
        context.dataStore.edit { it[Keys.THEME] = theme }
    }

    suspend fun setLanguage(language: String) {
        context.dataStore.edit { it[Keys.LANGUAGE] = language }
    }

    suspend fun setRetainTranscripts(retain: Boolean) {
        context.dataStore.edit { it[Keys.RETAIN_TRANSCRIPTS] = retain }
    }

    suspend fun setRetainAudio(retain: Boolean) {
        context.dataStore.edit { it[Keys.RETAIN_AUDIO] = retain }
    }

    suspend fun setAnalyticsConsent(consent: Boolean) {
        context.dataStore.edit { it[Keys.ANALYTICS_CONSENT] = consent }
    }

    suspend fun setDailyReminder(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DAILY_REMINDER] = enabled }
    }

    suspend fun setReduceMotion(enabled: Boolean) {
        context.dataStore.edit { it[Keys.REDUCE_MOTION] = enabled }
    }

    suspend fun setPremium(isPremium: Boolean) {
        context.dataStore.edit { it[Keys.IS_PREMIUM] = isPremium }
    }

    suspend fun incrementFreeSessionsUsed() {
        context.dataStore.edit {
            val current = it[Keys.FREE_SESSIONS_USED] ?: 0
            it[Keys.FREE_SESSIONS_USED] = current + 1
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setUseMockAi(useMock: Boolean) {
        context.dataStore.edit { it[Keys.USE_MOCK_AI] = useMock }
    }

    suspend fun clearAllPreferences() {
        context.dataStore.edit { it.clear() }
    }
}
