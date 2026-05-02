package com.mobile.emotion_ia.ui.emotion_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.emotion_ia.data.EmotionRepository
import com.mobile.emotion_ia.data.model.EmotionData
import com.mobile.emotion_ia.data.remote.FaceEmotionResponse
import com.mobile.emotion_ia.data.remote.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

data class UiState(
    val textResult: EmotionData? = null,
    val faceResult: FaceEmotionResponse? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class EmotionViewModel : ViewModel() {

    private val repository = EmotionRepository(
        RetrofitInstance.api
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    fun analyzeText(text: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                val result = repository.analyzeText(text)

                _uiState.value = _uiState.value.copy(
                    textResult = result,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun analyzeFace(file: File) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())

                val body = MultipartBody.Part.createFormData(
                    "file",
                    file.name,
                    requestFile
                )

                val result = repository.analyzeFace(body)

                _uiState.value = _uiState.value.copy(
                    faceResult = result,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
}