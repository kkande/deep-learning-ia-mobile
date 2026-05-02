package com.mobile.emotion_ia.data.remote

import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {

    @POST("predict-text")
    suspend fun predictEmotion(
        @Body request: EmotionRequest
    ): EmotionResponse

    @Multipart
    @POST("predict-face")
    suspend fun predictFace(
        @Part file: MultipartBody.Part
    ): FaceEmotionResponse
}