package com.mobile.emotion_ia.ui.emotion_screen

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import java.io.File

@Composable
fun EmotionScreen(viewModel: EmotionViewModel) {
    var textInput by remember { mutableStateOf("") }
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var permissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionGranted = granted
    }

    LaunchedEffect(Unit) {
        if (!permissionGranted) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var previewActive by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val file = uriToFile(context, it)
            viewModel.analyzeFace(file)
        }
    }

    val emotionRows = remember(state.faceResult, state.textResult) {
        buildScoreFractions(state.faceResult, state.textResult)
    }

    val bg = Color(0xFFECECEC)
    val cardWhite = Color.White
    val statusGreen = Color(0xFF2E7D32)

    fun captureAndAnalyze() {
        val capture = imageCapture ?: return
        val file = File(context.cacheDir, "face_${System.currentTimeMillis()}.jpg")
        val opts = ImageCapture.OutputFileOptions.Builder(file).build()
        capture.takePicture(
            opts,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    viewModel.analyzeFace(file)
                }

                override fun onError(exc: ImageCaptureException) {
                    /* no-op; errors surface via API state if needed */
                }
            }
        )
    }

    fun openShare() {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(
                Intent.EXTRA_TEXT,
                shareSummary(state.faceResult, state.textResult)
            )
        }
        context.startActivity(Intent.createChooser(send, null))
    }

    fun openMail() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_SUBJECT, "AI Emotion Mirror")
            putExtra(
                Intent.EXTRA_TEXT,
                shareSummary(state.faceResult, state.textResult)
            )
        }
        context.startActivity(Intent.createChooser(intent, null))
    }

    Scaffold(
        containerColor = bg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "AI Emotion Mirror",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Saisir un texte…") },
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(12.dp)
            )

            TextButton(
                onClick = { viewModel.analyzeText(textInput) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Analyser le texte")
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    if (permissionGranted) {
                        CameraPreview(
                            modifier = Modifier.fillMaxSize(),
                            cameraPermissionGranted = true,
                            onImageCaptureReady = { imageCapture = it },
                            onBound = { bound ->
                                previewActive = bound
                                if (!bound) imageCapture = null
                            }
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFFD0D0D0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "Caméra non autorisée",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                TextButton(onClick = {
                                    permissionLauncher.launch(Manifest.permission.CAMERA)
                                }) {
                                    Text("Autoriser l’accès")
                                }
                            }
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Choisir une image visage")
            }

            if (state.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            state.error?.let { err ->
                Text(
                    text = "Erreur : $err",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    emotionRows.forEach { (spec, fraction) ->
                        EmotionProgressRow(spec = spec, fraction = fraction)
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            "Status: ",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            statusPrimaryLine(state.faceResult, state.textResult),
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = FontStyle.Italic,
                            color = Color.DarkGray
                        )
                    }
                    StatusLine(
                        label = "Models:",
                        ok = true,
                        value = "Loaded",
                        okColor = statusGreen
                    )
                    StatusLine(
                        label = "Camera:",
                        ok = permissionGranted && previewActive,
                        value = if (permissionGranted && previewActive) "Active" else "Inactive",
                        okColor = statusGreen
                    )
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { openMail() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = "Email",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    VerticalDivider(
                        modifier = Modifier.height(32.dp),
                        color = Color.LightGray.copy(alpha = 0.7f)
                    )
                    IconButton(
                        onClick = { openShare() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Partager",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    VerticalDivider(
                        modifier = Modifier.height(32.dp),
                        color = Color.LightGray.copy(alpha = 0.7f)
                    )
                    IconButton(
                        onClick = { captureAndAnalyze() },
                        modifier = Modifier.weight(1f),
                        enabled = imageCapture != null && permissionGranted
                    ) {
                        Icon(
                            Icons.Default.PhotoCamera,
                            contentDescription = "Capturer",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmotionProgressRow(
    spec: EmotionBarSpec,
    fraction: Float,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = spec.label,
            modifier = Modifier.width(88.dp),
            style = MaterialTheme.typography.bodyMedium
        )
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .padding(horizontal = 8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = spec.color,
            trackColor = spec.color.copy(alpha = 0.22f),
        )
        Text(
            text = "${(fraction * 100).coerceIn(0f, 100f).toInt()}%",
            modifier = Modifier.width(42.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun StatusLine(
    label: String,
    ok: Boolean,
    value: String,
    okColor: Color,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        Icon(
            imageVector = Icons.Outlined.CheckCircle,
            contentDescription = null,
            tint = if (ok) okColor else Color.Gray,
            modifier = Modifier.padding(start = 6.dp, end = 4.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = if (ok) okColor else Color.Gray,
            fontWeight = FontWeight.Medium
        )
    }
}

fun uriToFile(context: Context, uri: Uri): File {
    val inputStream = context.contentResolver.openInputStream(uri)
    val file = File(context.cacheDir, "selected_face_${System.currentTimeMillis()}.jpg")
    inputStream.use { input ->
        file.outputStream().use { output ->
            input?.copyTo(output)
        }
    }
    return file
}
