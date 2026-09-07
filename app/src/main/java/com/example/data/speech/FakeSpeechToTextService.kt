package com.example.data.speech

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FakeSpeechToTextService : SpeechToTextService {

    private val _state = MutableStateFlow<SpeechState>(SpeechState.Idle)
    override val state: StateFlow<SpeechState> = _state.asStateFlow()

    private var simulationJob: Job? = null

    override fun isAvailable(): Boolean = true

    override fun startListening(languageCode: String) {
        _state.value = SpeechState.Listening
        simulationJob?.cancel()
        simulationJob = CoroutineScope(Dispatchers.Default).launch {
            delay(1200)
            val isBn = languageCode.startsWith("bn", ignoreCase = true)
            if (isBn) {
                _state.value = SpeechState.PartialResult("এর মূল উদ্দেশ্য হলো...")
                delay(1000)
                _state.value = SpeechState.FinalResult("এর মূল উদ্দেশ্য হলো মেমোরি ব্যবস্থাপনাকে দ্রুত ও কার্যকর করা এবং অ্যালগরিদমিক জটিলতা কমানো।")
            } else {
                _state.value = SpeechState.PartialResult("The primary concept revolves around...")
                delay(1000)
                _state.value = SpeechState.FinalResult("The primary concept revolves around maintaining optimal computational invariants and minimizing latency across node operations.")
            }
        }
    }

    override fun stopListening() {
        simulationJob?.cancel()
        if (_state.value is SpeechState.PartialResult) {
            val partial = (_state.value as SpeechState.PartialResult).text
            _state.value = SpeechState.FinalResult(partial)
        } else if (_state.value is SpeechState.Listening) {
            _state.value = SpeechState.Idle
        }
    }

    override fun reset() {
        simulationJob?.cancel()
        _state.value = SpeechState.Idle
    }
}
