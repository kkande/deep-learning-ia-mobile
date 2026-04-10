package com.mobile.emotion_ia.ui.emotion_screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun EmotionScreen(viewModel: EmotionViewModel) {

    var text by remember { mutableStateOf("") }
    val state = viewModel.uiState.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Enter text") }
        )

        Button(onClick = {
            viewModel.analyzeText(text)
        }) {
            Text("Analyser")
        }

        state.value.result?.let {
            Text("Emotion: ${it.label}")
            Text("Score: ${it.score}")
        }
    }
}