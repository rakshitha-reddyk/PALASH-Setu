package com.palash.setu.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.palash.setu.util.AudioTranslatorEngine
import com.palash.setu.util.TranslationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TranslatorUiState(
    val hindiInput: String = "",
    val santhaliScript: String = "",
    val santhaliPhonetic: String = "",
    val result: TranslationResult? = null,
    val isRecording: Boolean = false,
    val error: String? = null
)

class TranslatorViewModel(
    private val engine: AudioTranslatorEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(TranslatorUiState())
    val uiState: StateFlow<TranslatorUiState> = _uiState.asStateFlow()

    fun onHindiVoiceInput(spokenText: String?, currentLangCode: String) {
        if (spokenText.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(
                isRecording = false,
                error = "Offline speech failed. Please ensure Hindi language pack is downloaded in Android Settings (Settings -> Google -> Voice -> Offline Speech Recognition)"
            )
            return
        }

        viewModelScope.launch {
            // Set input immediately
            _uiState.value = _uiState.value.copy(
                hindiInput = spokenText,
                isRecording = false
            )
            
            val translation = engine.translate(spokenText, currentLangCode)
            
            // Map result to specific UI fields as required
            _uiState.value = _uiState.value.copy(
                santhaliScript = translation.nativeText,
                santhaliPhonetic = translation.devanagariText,
                result = translation
            )
        }
    }

    fun startRecording() {
        _uiState.value = _uiState.value.copy(isRecording = true, error = null)
    }

    fun stopRecording() {
        _uiState.value = _uiState.value.copy(isRecording = false)
    }

    fun setError(message: String) {
        _uiState.value = _uiState.value.copy(isRecording = false, error = message)
    }
}
