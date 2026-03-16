package com.mobile.emotion_ia.data.model

data class EmotionData(
    val neutral: Float = 0f,
    val sad: Float = 0f,
    val happy: Float = 0f,
    val angry: Float = 0f,
    val surprised: Float = 0f,
    val status: String = "No face detected",
    val score: Float = 0f,
    val modelsLoaded: Boolean = false,
    val cameraActive: Boolean = false,
    val totalInteractions: Int = 0,
    val averageEmotions: Map<String, Float> = emptyMap(),
    val lastVisit: String = "",
    val duration: String = "",
    val activeTime: String = ""
)