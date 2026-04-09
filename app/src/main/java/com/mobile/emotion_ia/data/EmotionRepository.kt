package com.mobile.emotion_ia.data

import com.mobile.emotion_ia.data.model.EmotionData
import com.mobile.emotion_ia.data.model.TweetEmotion
import com.mobile.emotion_ia.data.remote.RetrofitClient
import com.mobile.emotion_ia.data.remote.api.EmotionApiService
import com.mobile.emotion_ia.data.remote.dto.PredictRequest
import kotlinx.coroutines.flow.Flow

interface EmotionRepository {
    fun getLiveEmotionData(): Flow<EmotionData>
    suspend fun resetStats()
    suspend fun predictEmotion(text: String): TweetEmotion
}

class EmotionRepositoryImpl : EmotionRepository {

    private val apiService: EmotionApiService by lazy {
        RetrofitClient.instance.create(EmotionApiService::class.java)
    }

    override fun getLiveEmotionData(): Flow<EmotionData> {
        TODO("Not yet implemented")
    }

    override suspend fun resetStats() {
        TODO("Not yet implemented")
    }

    override suspend fun predictEmotion(text: String): TweetEmotion {
        val response = apiService.predict(PredictRequest(text))
        return TweetEmotion(
            text = text,
            label = response.label,
            confidence = response.confidence,
            scores = response.scores,
            inferenceTimeMs = response.inferenceTimeMs
        )
    }
}