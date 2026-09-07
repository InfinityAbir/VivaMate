package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Subject
import com.example.data.model.Topic
import com.example.data.repository.VivaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class SubjectLibraryUiState(
    val subjects: List<Subject> = emptyList(),
    val selectedSubject: Subject? = null,
    val topics: List<Topic> = emptyList(),
    val isLoading: Boolean = false
)

class SubjectViewModel(
    private val vivaRepository: VivaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubjectLibraryUiState())
    val uiState: StateFlow<SubjectLibraryUiState> = _uiState.asStateFlow()

    init {
        loadSubjects()
    }

    private fun loadSubjects() {
        viewModelScope.launch {
            vivaRepository.getActiveSubjects().collect { list ->
                _uiState.update { current ->
                    current.copy(
                        subjects = list,
                        selectedSubject = current.selectedSubject ?: list.firstOrNull()
                    )
                }
                val selected = _uiState.value.selectedSubject
                if (selected != null) {
                    loadTopics(selected.id)
                }
            }
        }
    }

    fun selectSubject(subject: Subject) {
        _uiState.update { it.copy(selectedSubject = subject) }
        loadTopics(subject.id)
    }

    private fun loadTopics(subjectId: String) {
        viewModelScope.launch {
            vivaRepository.getTopicsForSubject(subjectId).collect { topics ->
                _uiState.update { it.copy(topics = topics) }
            }
        }
    }

    fun saveSubject(name: String, description: String, level: String, color: Long, subjectId: String? = null, onDone: () -> Unit) {
        viewModelScope.launch {
            if (subjectId != null) {
                val existing = vivaRepository.getSubjectById(subjectId)
                if (existing != null) {
                    vivaRepository.updateSubject(
                        existing.copy(
                            name = name,
                            description = description,
                            level = level,
                            color = color
                        )
                    )
                }
            } else {
                val newSub = Subject(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    description = description,
                    level = level,
                    color = color
                )
                vivaRepository.insertSubject(newSub)
            }
            onDone()
        }
    }

    fun addTopic(subjectId: String, title: String, syllabusText: String, importance: String, onDone: () -> Unit) {
        viewModelScope.launch {
            val topic = Topic(
                subjectId = subjectId,
                title = title,
                syllabusText = syllabusText,
                importance = importance
            )
            vivaRepository.insertTopic(topic)
            onDone()
        }
    }

    fun deleteSubject(subjectId: String) {
        viewModelScope.launch {
            vivaRepository.deleteSubjectById(subjectId)
        }
    }
}
