package com.example.data.speech

import kotlinx.coroutines.flow.StateFlow

sealed interface SpeechState {
    object Idle : SpeechState
    object Listening : SpeechState
    data class PartialResult(val text: String) : SpeechState
    data class FinalResult(val text: String) : SpeechState
    data class Error(val message: String) : SpeechState
}

interface SpeechToTextService {
    val state: StateFlow<SpeechState>
    fun isAvailable(): Boolean
    fun startListening(languageCode: String = "en-US")
    fun stopListening()
    fun reset()
}
