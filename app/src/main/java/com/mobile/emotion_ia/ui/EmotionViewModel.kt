package com.mobile.emotion_ia.ui.emotion_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.emotion_ia.data.EmotionRepository
import com.mobile.emotion_ia.data.model.EmotionData
import com.mobile.emotion_ia.data.remote.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class UiState(
    val result: EmotionData? = null
)

class EmotionViewModel : ViewModel() {

    private val repository = EmotionRepository(
        RetrofitInstance.api
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    fun analyzeText(text: String) {
        viewModelScope.launch {
            val result = repository.analyzeText(text)
            _uiState.value = UiState(result)
        }
    }
}