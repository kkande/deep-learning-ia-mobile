package com.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobile.emotion_ia.ui.emotion_screen.EmotionScreen
import com.mobile.emotion_ia.ui.emotion_screen.EmotionViewModel
import com.mobile.ui.theme.Ia_emotionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Ia_emotionTheme {
                val vm: EmotionViewModel = viewModel()
                EmotionScreen(viewModel = vm)
            }
        }
    }
}