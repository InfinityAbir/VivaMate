package com.example.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.components.PremiumFeatureCard
import com.example.ui.components.ThemeSelector
import com.example.ui.viewmodel.SettingsUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onSetTheme: (String) -> Unit,
    onSetLanguage: (String) -> Unit,
    onSetRetainTranscripts: (Boolean) -> Unit,
    onSetRetainAudio: (Boolean) -> Unit,
    onSetAnalyticsConsent: (Boolean) -> Unit,
    onSetReduceMotion: (Boolean) -> Unit,
    onSetDailyReminder: (Boolean) -> Unit,
    onOpenPrivacyDeletion: () -> Unit,
    onOpenPaywall: () -> Unit,
    onOpenSupport: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Settings & Preferences", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Premium Upgrade banner if not premium
            if (!uiState.isPremium) {
                PremiumFeatureCard(
                    title = "VivaMate Pro",
                    subtitle = "Unlimited sessions, thesis defense, syllabus notes analysis.",
                    usdPrice = "$2.49",
                    bdtPrice = "৳290",
                    onUpgradeClick = onOpenPaywall
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Theme Selector
            ThemeSelector(
                selectedTheme = uiState.settings.theme,
                onSelectTheme = onSetTheme
            )

            Spacer(modifier = Modifier.height(22.dp))

            // Language Selector
            Text("Language / ভাষা", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val languages = listOf("English", "Bangla")
                languages.forEach { lang ->
                    val isSelected = uiState.settings.language.equals(lang, ignoreCase = true)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSetLanguage(lang) }
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 12.dp)) {
                            Text(
                                text = if (lang == "Bangla") "বাংলা (Bangla)" else "English",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Privacy & Retention Controls
            Text("Audio & Transcript Privacy", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            SettingSwitchRow(
                title = "Retain Typed & Spoken Transcripts",
                subtitle = "Stores text answers on this device for your progress review.",
                checked = uiState.settings.retainTranscripts,
                onCheckedChange = onSetRetainTranscripts
            )

            SettingSwitchRow(
                title = "Save Raw Audio Files",
                subtitle = "Disabled by default. Speech is transcribed immediately and audio is discarded.",
                checked = uiState.settings.retainAudio,
                onCheckedChange = onSetRetainAudio
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Accessibility & Preferences
            Text("Preferences", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            SettingSwitchRow(
                title = "Reduce Motion",
                subtitle = "Minimizes pulses and transitions for accessibility.",
                checked = uiState.settings.reduceMotionEnabled,
                onCheckedChange = onSetReduceMotion
            )

            SettingSwitchRow(
                title = "Daily Practice Reminders",
                subtitle = "Gentle daily notification to practice one oral exam question.",
                checked = uiState.settings.dailyReminderEnabled,
                onCheckedChange = onSetDailyReminder
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Navigation Links: Privacy Deletion, Subscription, Support
            SettingsNavRow(
                icon = Icons.Default.Lock,
                title = "Privacy & Data Deletion",
                subtitle = "Clear transcripts, delete sessions, reset local data",
                onClick = onOpenPrivacyDeletion
            )

            SettingsNavRow(
                icon = Icons.Default.Star,
                title = "Subscription & Pricing (BDT / USD)",
                subtitle = "Manage plan, restore purchases, low student pricing",
                onClick = onOpenPaywall
            )

            SettingsNavRow(
                icon = Icons.Default.HelpOutline,
                title = "Help, FAQ & Report Feedback",
                subtitle = "Academic disclaimer, model guidelines, support contact",
                onClick = onOpenSupport
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Academic Disclaimer Footer
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "VivaMate is an AI study coaching companion. It does not replace teacher evaluations, official university criteria, or formal grading.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingsNavRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleSmall)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        }
    }
}
