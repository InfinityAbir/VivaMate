package com.example.ui.screens.library

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.Subject
import com.example.data.model.Topic
import com.example.ui.components.EmptyState
import com.example.ui.components.PrimaryButton
import com.example.ui.viewmodel.SubjectLibraryUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectLibraryScreen(
    uiState: SubjectLibraryUiState,
    onSelectSubject: (Subject) -> Unit,
    onAddNewSubject: () -> Unit,
    onEditSubject: (String) -> Unit,
    onDeleteSubject: (String) -> Unit,
    onAddTopic: (subjectId: String, title: String, syllabus: String, importance: String) -> Unit,
    onPracticeTopic: (subjectId: String, topicId: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddTopicDialog by remember { mutableStateOf(false) }
    var topicTitleInput by remember { mutableStateOf("") }
    var topicSyllabusInput by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Subject Library", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { if (uiState.selectedSubject != null) showAddTopicDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_add_topic")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Topic")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Select a subject to view topics and syllabus coverage:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Subject Selector Chips / Cards
            items(uiState.subjects) { subject ->
                val isSelected = uiState.selectedSubject?.id == subject.id
                Card(
                    onClick = { onSelectSubject(subject) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        1.5.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(Color(subject.color))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = subject.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (subject.description.isNotBlank()) {
                                Text(
                                    text = subject.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                        IconButton(onClick = { onEditSubject(subject.id) }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Subject", modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = { onDeleteSubject(subject.id) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Subject", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            // Selected Subject's Topics
            if (uiState.selectedSubject != null) {
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Topics in ${uiState.selectedSubject.name}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                if (uiState.topics.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("No topics in this subject yet.", style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(onClick = { showAddTopicDialog = true }) {
                                    Text("Add First Topic")
                                }
                            }
                        }
                    }
                } else {
                    items(uiState.topics) { topic ->
                        Card(
                            onClick = { onPracticeTopic(uiState.selectedSubject.id, topic.id) },
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
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = topic.title, style = MaterialTheme.typography.titleSmall)
                                    if (topic.syllabusText.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = topic.syllabusText,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2
                                        )
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = "Practice",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Topic Dialog
        if (showAddTopicDialog && uiState.selectedSubject != null) {
            AlertDialog(
                onDismissRequest = { showAddTopicDialog = false },
                title = { Text("Add New Topic") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = topicTitleInput,
                            onValueChange = { topicTitleInput = it },
                            label = { Text("Topic Title") },
                            placeholder = { Text("e.g. B-Tree Balancing") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = topicSyllabusInput,
                            onValueChange = { topicSyllabusInput = it },
                            label = { Text("Syllabus / Concept Notes") },
                            placeholder = { Text("Key theorems, algorithms, equations...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (topicTitleInput.isNotBlank()) {
                                onAddTopic(
                                    uiState.selectedSubject.id,
                                    topicTitleInput.trim(),
                                    topicSyllabusInput.trim(),
                                    "High"
                                )
                                topicTitleInput = ""
                                topicSyllabusInput = ""
                                showAddTopicDialog = false
                            }
                        }
                    ) {
                        Text("Add Topic")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddTopicDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
