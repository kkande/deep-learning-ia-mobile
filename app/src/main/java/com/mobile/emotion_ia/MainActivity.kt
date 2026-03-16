package com.mobile.emotion_ia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.mobile.emotion_ia.ui.emotion_screen.EmotionScreen
import com.mobile.emotion_ia.ui.theme.EmotioniaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EmotioniaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
                    EmotionScreen(modifier = Modifier.padding(paddingValues))
                }
            }
        }
    }
}