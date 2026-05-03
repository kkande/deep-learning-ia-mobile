package com.mobile.emotion_ia.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PredictResponse(
    @SerializedName("label") val label: String,
    @SerializedName("scores") val scores: Map<String, Float>,
    @SerializedName("confidence") val confidence: Float,
    @SerializedName("inference_time_ms") val inferenceTimeMs: Float
)