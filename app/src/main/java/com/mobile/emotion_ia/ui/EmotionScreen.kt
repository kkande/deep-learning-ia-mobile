package com.mobile.emotion_ia.ui.emotion_screen

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.io.File

@Composable
fun EmotionScreen(viewModel: EmotionViewModel) {

    var text by remember { mutableStateOf("") }
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Sélecteur image (galerie)
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val file = uriToFile(context, it)
            viewModel.analyzeFace(file)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "EmotionIA",
            style = MaterialTheme.typography.headlineMedium
        )

        // ===== TEXTE =====
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Entrer un texte") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { viewModel.analyzeText(text) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Analyser le texte")
        }

        // ===== IMAGE =====
        Button(
            onClick = { imagePickerLauncher.launch("image/*") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Choisir une image visage")
        }

        // 🔥 BOUTON TEST (SANS GALERIE)
        Button(
            onClick = {
                val fakeFile = File(context.cacheDir, "test.jpg")
                viewModel.analyzeFace(fakeFile)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Test visage (rapide)")
        }

        // LOADING
        if (state.isLoading) {
            CircularProgressIndicator()
        }

        //  ERREUR
        state.error?.let { error ->
            Text(
                text = "Erreur : $error",
                color = MaterialTheme.colorScheme.error
            )
        }

        // RESULTAT TEXTE
        state.textResult?.let { result ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Résultat texte")
                    Text("Émotion : ${result.label}")
                    Text("Score : ${"%.2f".format(result.score * 100)} %")
                }
            }
        }

        // RESULTAT VISAGE
        state.faceResult?.let { face ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Résultat visage")
                    Text("Émotion : ${face.label}")
                    Text("Confiance : ${"%.2f".format(face.confidence * 100)} %")
                    Text("Temps : ${face.inference_time_ms} ms")
                }
            }
        }
    }
}

// Conversion URI -> File
fun uriToFile(context: Context, uri: Uri): File {
    val inputStream = context.contentResolver.openInputStream(uri)
    val file = File(context.cacheDir, "selected_face.jpg")

    inputStream.use { input ->
        file.outputStream().use { output ->
            input?.copyTo(output)
        }
    }

    return file
}