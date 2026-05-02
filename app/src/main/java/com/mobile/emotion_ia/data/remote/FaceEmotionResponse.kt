package com.mobile.emotion_ia.data.remote

data class FaceEmotionResponse(
    val label: String,
    val scores: Map<String, Double>,
    val confidence: Double,
    val inference_time_ms: Double
)