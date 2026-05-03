package com.mobile.emotion_ia.data.remote.api

import com.mobile.emotion_ia.data.remote.dto.PredictRequest
import com.mobile.emotion_ia.data.remote.dto.PredictResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface EmotionApiService {

    @POST("predict")
    suspend fun predict(@Body request: PredictRequest): PredictResponse
}