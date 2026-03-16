package com.mobile.emotion_ia.ui.emotion_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.emotion_ia.data.model.EmotionData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EmotionViewModel : ViewModel() {

    private val _emotionData = MutableStateFlow(
        EmotionData(
            neutral = 0.68f,
            sad = 0.28f,
            happy = 0.04f,
            angry = 0.00f,
            surprised = 0.00f,
            status = "Face detected!",
            score = 0.87f,
            modelsLoaded = true,
            cameraActive = true,
            totalInteractions = 1,
            averageEmotions = mapOf(
                "neutral" to 0.86f,
                "sad" to 0.13f,
                "happy" to 0.008f,
                "angry" to 0.001f,
                "surprised" to 0.001f
            ),
            lastVisit = "01:55:42",
            duration = "0:00:19",
            activeTime = "0:00:05"
        )
    )
    val emotionData: StateFlow<EmotionData> = _emotionData.asStateFlow()

    fun resetStats() {
        viewModelScope.launch {
            _emotionData.value = _emotionData.value.copy(
                totalInteractions = 0,
                averageEmotions = mapOf(
                    "neutral" to 0f,
                    "sad" to 0f,
                    "happy" to 0f,
                    "angry" to 0f,
                    "surprised" to 0f
                ),
                lastVisit = "--:--:--",
                duration = "0:00:00",
                activeTime = "0:00:00"
            )
        }
    }
}