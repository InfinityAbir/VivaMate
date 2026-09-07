package com.example.ui.screens.privacy

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.ErrorRed
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyDeletionScreen(
    onAnonymizeTranscripts: (() -> Unit) -> Unit,
    onDeleteAllSessions: (() -> Unit) -> Unit,
    onWipeAllData: (() -> Unit) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var confirmDialogType by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val privacyUrl = stringResource(R.string.privacy_policy_url)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Privacy & Data Rights", style = MaterialTheme.typography.titleLarge) },
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

            // AI Processing Transparency Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = "How VivaMate Protects Your Privacy", style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Raw audio recordings are never stored on external servers.\n" +
                               "• Speech recognition is converted to text on your device or via secure OS speech API.\n" +
                               "• Oral exam questions & answers are sent to Google Gemini only for evaluation and feedback generation.\n" +
                               "• All session histories, transcripts, and revision recommendations are stored in your local on-device database.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(privacyUrl))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Policy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("View Official Privacy Policy")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(imageVector = Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(text = "Data Management Actions", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))

            // Action 1: Anonymize transcripts
            DataActionCard(
                icon = Icons.Default.DeleteOutline,
                title = "Clear All Spoken & Typed Transcripts",
                description = "Replaces all stored transcripts with '[Deleted by user]'. Keeps scores and revision topics intact.",
                buttonText = "Clear Transcripts",
                onClick = { confirmDialogType = "transcripts" }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Action 2: Delete all sessions
            DataActionCard(
                icon = Icons.Default.DeleteForever,
                title = "Delete Entire Session History",
                description = "Permanently deletes all past oral exam sessions, questions, scores, and evaluations.",
                buttonText = "Delete History",
                onClick = { confirmDialogType = "sessions" }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Action 3: Wipe all data
            DataActionCard(
                icon = Icons.Default.RestartAlt,
                title = "Wipe All Local App Data",
                description = "Resets subjects, sessions, topics, preferences, and restarts app to fresh state.",
                buttonText = "Reset Everything",
                isDestructive = true,
                onClick = { confirmDialogType = "wipe" }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Confirmation Dialog
        if (confirmDialogType != null) {
            val title = when (confirmDialogType) {
                "transcripts" -> "Clear All Transcripts?"
                "sessions" -> "Delete All Session History?"
                else -> "Wipe All App Data?"
            }
            val msg = when (confirmDialogType) {
                "transcripts" -> "This will permanently remove all text and speech transcripts from your local database."
                "sessions" -> "This will remove all session records, scores, and analytics. This action cannot be undone."
                else -> "This will completely reset VivaMate, clearing all subjects, sessions, and custom settings."
            }

            AlertDialog(
                onDismissRequest = { confirmDialogType = null },
                title = { Text(title) },
                text = { Text(msg) },
                confirmButton = {
                    Button(
                        onClick = {
                            val action = confirmDialogType
                            confirmDialogType = null
                            when (action) {
                                "transcripts" -> onAnonymizeTranscripts {
                                    scope.launch { snackbarHostState.showSnackbar("All transcripts cleared.") }
                                }
                                "sessions" -> onDeleteAllSessions {
                                    scope.launch { snackbarHostState.showSnackbar("All sessions deleted.") }
                                }
                                "wipe" -> onWipeAllData {
                                    scope.launch { snackbarHostState.showSnackbar("All local data wiped.") }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                    ) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { confirmDialogType = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun DataActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    buttonText: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isDestructive) ErrorRed else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = title, style = MaterialTheme.typography.titleSmall)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onClick,
                colors = if (isDestructive) ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed) else ButtonDefaults.outlinedButtonColors()
            ) {
                Text(buttonText)
            }
        }
    }
}
