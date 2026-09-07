package com.example.ui.screens.room

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.ui.components.AnimatedProgressBar
import com.example.ui.components.FeedbackBottomSheet
import com.example.ui.components.PrimaryButton
import com.example.ui.components.QuestionCard
import com.example.ui.components.RecordingButton
import com.example.ui.components.SecondaryButton
import com.example.ui.components.TranscriptEditor
import com.example.ui.viewmodel.VivaRoomUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VivaRoomScreen(
    uiState: VivaRoomUiState,
    onToggleRecord: () -> Unit,
    onUpdateTranscript: (String) -> Unit,
    onClearTranscript: () -> Unit,
    onSubmitAnswer: () -> Unit,
    onAdvanceNext: () -> Unit,
    onSkipQuestion: () -> Unit,
    onExplainTopic: () -> Unit,
    onDismissExplanation: () -> Unit,
    onDismissFeedback: () -> Unit,
    onEndSession: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showEndDialog by remember { mutableStateOf(false) }
    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicPermission = isGranted
        if (isGranted) {
            onToggleRecord()
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.session?.topicTitle ?: "Viva Practice",
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1
                        )
                        Text(
                            text = "${uiState.session?.mode ?: "Oral Exam"} • ${uiState.session?.difficulty ?: "Adaptive"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { showEndDialog = true },
                        modifier = Modifier.testTag("close_session_button")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "End Session")
                    }
                },
                actions = {
                    IconButton(
                        onClick = onSkipQuestion,
                        enabled = !uiState.isEvaluating && !uiState.isGeneratingNextQuestion,
                        modifier = Modifier.testTag("skip_question_button")
                    ) {
                        Icon(imageVector = Icons.Default.SkipNext, contentDescription = "Skip Question")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Sequence progress bar
            AnimatedProgressBar(
                current = uiState.questionNumber,
                total = uiState.totalQuestions,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Error Message Banner
            if (uiState.errorMessage != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Text(
                        text = uiState.errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Question Card
            if (uiState.currentQuestion != null) {
                QuestionCard(
                    questionNumber = uiState.questionNumber,
                    totalQuestions = uiState.totalQuestions,
                    questionText = uiState.currentQuestion.text,
                    questionType = uiState.currentQuestion.questionType,
                    difficulty = uiState.currentQuestion.difficulty,
                    examinerHint = uiState.currentQuestion.examinerHint,
                    estimatedSeconds = uiState.timeRemainingSeconds
                )
            } else if (uiState.isGeneratingNextQuestion) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Examiner preparing question ${uiState.questionNumber}...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Voice Recording Controller
            RecordingButton(
                isRecording = uiState.isRecording,
                recordingTimeSeconds = uiState.recordingDurationSeconds,
                onToggleRecord = {
                    if (hasMicPermission) {
                        onToggleRecord()
                    } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                enabled = !uiState.isEvaluating && !uiState.isGeneratingNextQuestion
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Transcript / Typed Answer Area
            TranscriptEditor(
                transcript = uiState.transcript,
                onTranscriptChange = onUpdateTranscript,
                isVoiceDerived = uiState.isVoiceInput,
                onClear = onClearTranscript
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Submit Button / Loading Indicator
            if (uiState.isEvaluating) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Examiner analyzing conceptual depth...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                PrimaryButton(
                    text = "Submit Answer",
                    onClick = onSubmitAnswer,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.transcript.isNotBlank() && !uiState.isGeneratingNextQuestion,
                    icon = Icons.Default.Send,
                    testTag = "submit_answer_button"
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }

        // Feedback Bottom Sheet
        if (uiState.showFeedbackSheet && uiState.latestEvaluation != null) {
            FeedbackBottomSheet(
                evaluation = uiState.latestEvaluation,
                onContinue = onAdvanceNext,
                onExplainTopic = onExplainTopic,
                sheetState = sheetState,
                onDismiss = onDismissFeedback,
                isLastQuestion = uiState.questionNumber >= uiState.totalQuestions
            )
        }

        // Topic Explanation Dialog
        if (uiState.explanationText != null) {
            AlertDialog(
                onDismissRequest = onDismissExplanation,
                title = { Text("Examiner Concept Breakdown", style = MaterialTheme.typography.titleMedium) },
                text = {
                    Text(
                        text = uiState.explanationText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    TextButton(onClick = onDismissExplanation) {
                        Text("Understood")
                    }
                }
            )
        }

        // End Session Confirmation Dialog
        if (showEndDialog) {
            AlertDialog(
                onDismissRequest = { showEndDialog = false },
                title = { Text("End Oral Exam Session?", style = MaterialTheme.typography.titleMedium) },
                text = {
                    Text(
                        "Are you sure you want to conclude this session early? Your answered questions will be scored and saved to your revision history.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showEndDialog = false
                            onEndSession()
                        }
                    ) {
                        Text("End & View Scorecard")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEndDialog = false }) {
                        Text("Resume Viva")
                    }
                }
            )
        }
    }
}
