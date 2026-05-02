package com.mobile.emotion_ia.data

import com.mobile.emotion_ia.data.model.EmotionData
import com.mobile.emotion_ia.data.remote.ApiService
import com.mobile.emotion_ia.data.remote.EmotionRequest
import com.mobile.emotion_ia.data.remote.FaceEmotionResponse
import okhttp3.MultipartBody

class EmotionRepository(
    private val apiService: ApiService
) {

    suspend fun analyzeText(text: String): EmotionData {
        val response = apiService.predictEmotion(EmotionRequest(text))
        return EmotionData(
            label = response.label,
            score = response.confidence.toFloat()
        )
    }

    suspend fun analyzeFace(file: MultipartBody.Part): FaceEmotionResponse {
        return apiService.predictFace(file)
    }
}