package com.mobile.emotion_ia.ui.emotion_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.emotion_ia.data.EmotionRepositoryImpl
import com.mobile.emotion_ia.data.model.EmotionData
import com.mobile.emotion_ia.data.model.TweetEmotion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class TweetAnalyzerState {
    object Idle : TweetAnalyzerState()
    object Loading : TweetAnalyzerState()
    data class Success(val result: TweetEmotion) : TweetAnalyzerState()
    data class Error(val message: String) : TweetAnalyzerState()
}

class EmotionViewModel : ViewModel() {

    private val repository = EmotionRepositoryImpl()

    // ── Camera / Emotion bars state ────────────────────────────────────────
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

    // ── Tweet analyzer state ───────────────────────────────────────────────
    private val _tweetInput = MutableStateFlow("")
    val tweetInput: StateFlow<String> = _tweetInput.asStateFlow()

    private val _analyzerState = MutableStateFlow<TweetAnalyzerState>(TweetAnalyzerState.Idle)
    val analyzerState: StateFlow<TweetAnalyzerState> = _analyzerState.asStateFlow()

    private val _tweetHistory = MutableStateFlow<List<TweetEmotion>>(emptyList())
    val tweetHistory: StateFlow<List<TweetEmotion>> = _tweetHistory.asStateFlow()

    // ── Actions ────────────────────────────────────────────────────────────

    fun onTweetInputChanged(value: String) {
        _tweetInput.value = value
    }

    fun analyzeTweet() {
        val text = _tweetInput.value.trim()
        if (text.isBlank()) return

        viewModelScope.launch {
            _analyzerState.value = TweetAnalyzerState.Loading
            try {
                val result = repository.predictEmotion(text)
                _analyzerState.value = TweetAnalyzerState.Success(result)
                _tweetHistory.value = (listOf(result) + _tweetHistory.value).take(10)
                _tweetInput.value = ""
            } catch (e: Exception) {
                val message = when {
                    e.message?.contains("Unable to resolve host") == true ->
                        "Serveur injoignable. Vérifiez l'URL de l'API."
                    e.message?.contains("timeout") == true ->
                        "Délai d'attente dépassé. Réessayez."
                    else -> e.message ?: "Erreur inconnue"
                }
                _analyzerState.value = TweetAnalyzerState.Error(message)
            }
        }
    }

    fun dismissAnalyzerError() {
        _analyzerState.value = TweetAnalyzerState.Idle
    }

    fun setCameraActive(active: Boolean) {
        _emotionData.value = _emotionData.value.copy(cameraActive = active)
    }

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
            _tweetHistory.value = emptyList()
            _analyzerState.value = TweetAnalyzerState.Idle
        }
    }
}