package com.mobile.emotion_ia.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("predict-text")
    suspend fun predictEmotion(
        @Body request: EmotionRequest
    ): EmotionResponse
}