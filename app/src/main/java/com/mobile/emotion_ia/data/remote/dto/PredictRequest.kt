package com.mobile.emotion_ia.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PredictRequest(
    @SerializedName("text") val text: String
)