package com.mobile.emotion_ia.data

import com.mobile.emotion_ia.data.model.EmotionData
import kotlinx.coroutines.flow.Flow

interface EmotionRepository {
    fun getLiveEmotionData(): Flow<EmotionData>
    suspend fun resetStats()
    // Add other data operations as needed (e.g., saving settings)
}

class EmotionRepositoryImpl : EmotionRepository {
    override fun getLiveEmotionData(): Flow<EmotionData> {
        // This is where you would fetch data from a camera, a machine learning model, or a local database.
        // For now, it's a placeholder.
        TODO("Not yet implemented")
    }

    override suspend fun resetStats() {
        TODO("Not yet implemented")
    }
}