package com.mobile.emotion_ia.data.model

data class TweetEmotion(
    val text: String,
    val label: String,
    val confidence: Float,
    val scores: Map<String, Float>,
    val inferenceTimeMs: Float
)