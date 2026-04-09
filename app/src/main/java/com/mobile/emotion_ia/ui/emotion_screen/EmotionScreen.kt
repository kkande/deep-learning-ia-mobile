package com.mobile.emotion_ia.ui.emotion_screen

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview as ComposePreview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobile.emotion_ia.data.model.EmotionData
import com.mobile.emotion_ia.data.model.TweetEmotion
import com.mobile.emotion_ia.ui.theme.EmotioniaTheme

// ---------------------------------------------------------------------------
// Colour tokens
// ---------------------------------------------------------------------------
private val BackgroundColor   = Color(0xFFF0F2F5)
private val CardColor         = Color.White
private val BlueAccent        = Color(0xFF4B8EF1)
private val NeutralBarColor   = Color(0xFF8E8E93)
private val SadBarColor       = Color(0xFF4B8EF1)
private val HappyBarColor     = Color(0xFFFFD60A)
private val AngryBarColor     = Color(0xFFFF3B30)
private val SurprisedBarColor = Color(0xFFAF52DE)
private val ResetButtonColor  = Color(0xFFFF3B30)
private val GreenAccent       = Color(0xFF34C759)

private val emotionTextColors = mapOf(
    "neutral"   to NeutralBarColor,
    "sad"       to SadBarColor,
    "happy"     to HappyBarColor,
    "angry"     to AngryBarColor,
    "surprised" to SurprisedBarColor
)

// ---------------------------------------------------------------------------
// Emotion helpers
// ---------------------------------------------------------------------------
private fun emotionEmoji(label: String): String = when (label.lowercase()) {
    "joy", "happy"                              -> "😄"
    "sadness", "sad", "grief"                  -> "😢"
    "anger", "angry", "annoyance"              -> "😠"
    "surprise"                                  -> "😮"
    "neutral"                                   -> "😐"
    "fear", "nervousness"                       -> "😨"
    "love", "caring", "desire"                 -> "❤️"
    "excitement"                                -> "🤩"
    "optimism"                                  -> "🌟"
    "gratitude"                                 -> "🙏"
    "amusement"                                 -> "😂"
    "confusion"                                 -> "😕"
    "curiosity"                                 -> "🤔"
    "disappointment", "remorse"                -> "😞"
    "disapproval", "disgust"                   -> "😤"
    "embarrassment"                             -> "😳"
    "pride", "approval"                         -> "👍"
    "realization"                               -> "💡"
    "relief"                                    -> "😌"
    else                                        -> "🤔"
}

private fun emotionColor(label: String): Color = when (label.lowercase()) {
    "joy", "happy", "amusement", "excitement"          -> HappyBarColor
    "sadness", "sad", "grief", "disappointment",
    "remorse"                                           -> SadBarColor
    "anger", "angry", "annoyance", "disapproval",
    "disgust"                                           -> AngryBarColor
    "surprise"                                          -> SurprisedBarColor
    "neutral"                                           -> NeutralBarColor
    "fear", "nervousness"                               -> Color(0xFFFF9500)
    "love", "caring", "desire", "gratitude"            -> Color(0xFFFF2D55)
    "optimism", "pride", "approval", "relief"          -> GreenAccent
    else                                                -> NeutralBarColor
}

// ---------------------------------------------------------------------------
// Root screen
// ---------------------------------------------------------------------------
@Composable
fun EmotionScreen(
    modifier: Modifier = Modifier,
    viewModel: EmotionViewModel = viewModel()
) {
    val emotionData    by viewModel.emotionData.collectAsState()
    val tweetInput     by viewModel.tweetInput.collectAsState()
    val analyzerState  by viewModel.analyzerState.collectAsState()
    val tweetHistory   by viewModel.tweetHistory.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ── Title ──────────────────────────────────────────────────────────
        Text(
            text = "AI Emotion Mirror",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp
            ),
            modifier = Modifier.fillMaxWidth(),
            color = Color.Black
        )

        // ── Camera Preview ─────────────────────────────────────────────────
        CameraPreviewCard(
            onCameraActiveChanged = { active -> viewModel.setCameraActive(active) }
        )

        // ── Emotion Bars ───────────────────────────────────────────────────
        EmotionBarsCard(emotionData)

        // ── Status Card ────────────────────────────────────────────────────
        StatusCard(emotionData)

        // ── Tweet Analyzer ─────────────────────────────────────────────────
        TweetAnalyzerCard(
            input = tweetInput,
            state = analyzerState,
            onInputChanged = viewModel::onTweetInputChanged,
            onAnalyze = viewModel::analyzeTweet,
            onDismissError = viewModel::dismissAnalyzerError
        )

        // ── Tweet History ──────────────────────────────────────────────────
        if (tweetHistory.isNotEmpty()) {
            TweetHistoryCard(history = tweetHistory)
        }

        // ── Action Icons Row ───────────────────────────────────────────────
        ActionIconsRow()

        // ── Statistics Card ────────────────────────────────────────────────
        StatisticsCard(emotionData, onReset = { viewModel.resetStats() })

        Spacer(Modifier.height(24.dp))
    }
}

// ---------------------------------------------------------------------------
// Camera Preview
// ---------------------------------------------------------------------------
@Composable
private fun CameraPreviewCard(onCameraActiveChanged: (Boolean) -> Unit) {
    val context        = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF1C1C1E)),
        contentAlignment = Alignment.Center
    ) {
        if (hasCameraPermission) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }
                    val future = ProcessCameraProvider.getInstance(ctx)
                    future.addListener({
                        try {
                            val provider = future.get()
                            val preview = Preview.Builder().build().also {
                                it.surfaceProvider = previewView.surfaceProvider
                            }
                            provider.unbindAll()
                            provider.bindToLifecycle(
                                lifecycleOwner, CameraSelector.DEFAULT_FRONT_CAMERA, preview
                            )
                            onCameraActiveChanged(true)
                        } catch (e: Exception) {
                            Log.e("EmotionScreen", "Camera bind failed", e)
                            onCameraActiveChanged(false)
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Filled.PhotoCamera,
                    contentDescription = "Camera",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    colors = ButtonDefaults.buttonColors(containerColor = BlueAccent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Activer la Caméra", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Emotion Bars
// ---------------------------------------------------------------------------
@Composable
private fun EmotionBarsCard(data: EmotionData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            EmotionBarRow("Neutral",   data.neutral,   NeutralBarColor)
            EmotionBarRow("Sad",       data.sad,       SadBarColor)
            EmotionBarRow("Happy",     data.happy,     HappyBarColor)
            EmotionBarRow("Angry",     data.angry,     AngryBarColor)
            EmotionBarRow("Surprised", data.surprised, SurprisedBarColor)
        }
    }
}

@Composable
private fun EmotionBarRow(label: String, value: Float, barColor: Color) {
    val animated by animateFloatAsState(
        targetValue = value.coerceIn(0f, 1f),
        animationSpec = tween(600),
        label = "bar_$label"
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            modifier = Modifier.width(80.dp),
            fontSize = 14.sp,
            color = Color(0xFF636366)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(10.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFFE5E5EA))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = animated.coerceAtLeast(0.01f))
                    .clip(RoundedCornerShape(50))
                    .background(barColor)
            )
        }
        Text(
            text = "${(value * 100).toInt()}%",
            modifier = Modifier.width(40.dp).padding(start = 8.dp),
            fontSize = 13.sp,
            color = Color(0xFF636366)
        )
    }
}

// ---------------------------------------------------------------------------
// Status Card
// ---------------------------------------------------------------------------
@Composable
private fun StatusCard(data: EmotionData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            StatusRow("Status:", "${data.status} Score: ${String.format("%.2f", data.score)}")
            StatusRow(
                label = "Models:",
                value = if (data.modelsLoaded) "✓ Loaded" else "✗ Not loaded",
                valueColor = if (data.modelsLoaded) GreenAccent else AngryBarColor
            )
            StatusRow(
                label = "Camera:",
                value = if (data.cameraActive) "✓ Active" else "✗ Inactive",
                valueColor = if (data.cameraActive) GreenAccent else AngryBarColor
            )
        }
    }
}

@Composable
private fun StatusRow(label: String, value: String, valueColor: Color = Color(0xFF3C3C43)) {
    Row {
        Text(text = label, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF3C3C43))
        Spacer(Modifier.width(4.dp))
        Text(text = value, fontSize = 13.sp, color = valueColor, fontStyle = FontStyle.Italic)
    }
}

// ---------------------------------------------------------------------------
// Tweet Analyzer Card
// ---------------------------------------------------------------------------
@Composable
private fun TweetAnalyzerCard(
    input: String,
    state: TweetAnalyzerState,
    onInputChanged: (String) -> Unit,
    onAnalyze: () -> Unit,
    onDismissError: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Text(
                text = "Analyser un tweet",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = Color.Black
            )

            // Input + Send button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = onInputChanged,
                    placeholder = {
                        Text(
                            "Entrez un tweet ou un texte...",
                            fontSize = 13.sp,
                            color = Color(0xFF8E8E93)
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BlueAccent,
                        unfocusedBorderColor = Color(0xFFE5E5EA)
                    ),
                    enabled = state !is TweetAnalyzerState.Loading
                )
                FilledIconButton(
                    onClick = onAnalyze,
                    enabled = input.isNotBlank() && state !is TweetAnalyzerState.Loading,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = BlueAccent,
                        disabledContainerColor = Color(0xFFE5E5EA)
                    )
                ) {
                    Icon(Icons.Filled.Send, contentDescription = "Analyser", tint = Color.White)
                }
            }

            // Loading
            if (state is TweetAnalyzerState.Loading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = BlueAccent
                    )
                    Text("Analyse en cours...", fontSize = 13.sp, color = Color(0xFF636366))
                }
            }

            // Error
            AnimatedVisibility(
                visible = state is TweetAnalyzerState.Error,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                if (state is TweetAnalyzerState.Error) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(AngryBarColor.copy(alpha = 0.1f))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚠️ ${state.message}",
                            fontSize = 12.sp,
                            color = AngryBarColor,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = onDismissError) {
                            Text("OK", color = AngryBarColor, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Result
            AnimatedVisibility(
                visible = state is TweetAnalyzerState.Success,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                if (state is TweetAnalyzerState.Success) {
                    EmotionResultContent(result = state.result)
                }
            }
        }
    }
}

@Composable
private fun EmotionResultContent(result: TweetEmotion) {
    val dominantColor = emotionColor(result.label)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        HorizontalDivider(color = Color(0xFFE5E5EA))

        // Dominant emotion header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(dominantColor.copy(alpha = 0.1f))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = emotionEmoji(result.label), fontSize = 28.sp)
                Column {
                    Text(
                        text = result.label.replaceFirstChar { it.uppercase() },
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = dominantColor
                    )
                    Text(
                        text = "Confiance : ${(result.confidence * 100).toInt()}%",
                        fontSize = 12.sp,
                        color = Color(0xFF636366)
                    )
                }
            }
            Text(
                text = "${result.inferenceTimeMs.toInt()} ms",
                fontSize = 11.sp,
                color = Color(0xFF8E8E93)
            )
        }

        // Confidence bar
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Niveau de confiance", fontSize = 12.sp, color = Color(0xFF636366))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFFE5E5EA))
            ) {
                val animated by animateFloatAsState(
                    targetValue = result.confidence,
                    animationSpec = tween(800),
                    label = "confidence"
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animated)
                        .clip(RoundedCornerShape(50))
                        .background(dominantColor)
                )
            }
        }

        // Top 5 scores
        val top5 = result.scores.entries
            .sortedByDescending { it.value }
            .take(5)

        Text(
            text = "Top émotions détectées",
            fontSize = 12.sp,
            color = Color(0xFF636366),
            fontWeight = FontWeight.SemiBold
        )
        top5.forEach { (emotion, score) ->
            ScoreRow(
                label = "${emotionEmoji(emotion)} ${emotion.replaceFirstChar { it.uppercase() }}",
                value = score,
                color = emotionColor(emotion)
            )
        }
    }
}

@Composable
private fun ScoreRow(label: String, value: Float, color: Color) {
    val animated by animateFloatAsState(
        targetValue = value.coerceIn(0f, 1f),
        animationSpec = tween(600),
        label = "score_$label"
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            modifier = Modifier.width(160.dp),
            fontSize = 13.sp,
            color = Color(0xFF3C3C43),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFFE5E5EA))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = animated.coerceAtLeast(0.01f))
                    .clip(RoundedCornerShape(50))
                    .background(color)
            )
        }
        Text(
            text = "${(value * 100).toInt()}%",
            modifier = Modifier.width(36.dp).padding(start = 6.dp),
            fontSize = 12.sp,
            color = Color(0xFF636366)
        )
    }
}

// ---------------------------------------------------------------------------
// Tweet History Card
// ---------------------------------------------------------------------------
@Composable
private fun TweetHistoryCard(history: List<TweetEmotion>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Historique des analyses",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = Color.Black
            )

            history.forEachIndexed { index, tweet ->
                if (index > 0) HorizontalDivider(color = Color(0xFFE5E5EA), thickness = 0.5.dp)
                TweetHistoryItem(tweet = tweet)
            }
        }
    }
}

@Composable
private fun TweetHistoryItem(tweet: TweetEmotion) {
    val color = emotionColor(tweet.label)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Emotion pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(color.copy(alpha = 0.12f))
                .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${emotionEmoji(tweet.label)} ${tweet.label.replaceFirstChar { it.uppercase() }}",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }

        // Tweet text
        Text(
            text = tweet.text,
            fontSize = 13.sp,
            color = Color(0xFF3C3C43),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        // Confidence
        Text(
            text = "${(tweet.confidence * 100).toInt()}%",
            fontSize = 12.sp,
            color = Color(0xFF8E8E93),
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ---------------------------------------------------------------------------
// Action Icons Row
// ---------------------------------------------------------------------------
@Composable
private fun ActionIconsRow() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ActionIcon(icon = Icons.Filled.MailOutline, label = "Email")
            VerticalDivider()
            ActionIcon(icon = Icons.Filled.Share,       label = "Share")
            VerticalDivider()
            ActionIcon(icon = Icons.Filled.PhotoCamera, label = "Camera")
        }
    }
}

@Composable
private fun ActionIcon(icon: ImageVector, label: String) {
    IconButton(onClick = { }) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = BlueAccent,
            modifier = Modifier.size(26.dp)
        )
    }
}

@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(30.dp)
            .background(Color(0xFFE5E5EA))
    )
}

// ---------------------------------------------------------------------------
// Statistics Card
// ---------------------------------------------------------------------------
@Composable
private fun StatisticsCard(data: EmotionData, onReset: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Statistics",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.Black,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            StatSection(title = "Total Interactions") {
                Text(text = data.totalInteractions.toString(), fontSize = 15.sp, color = Color(0xFF3C3C43))
            }

            StatSection(title = "Average Emotions") {
                val orderedEmotions = listOf("neutral", "sad", "happy", "angry", "surprised")
                orderedEmotions.forEach { key ->
                    val pct = data.averageEmotions[key] ?: 0f
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = key,
                            fontSize = 14.sp,
                            color = emotionTextColors[key] ?: NeutralBarColor,
                            fontStyle = FontStyle.Italic
                        )
                        Text(
                            text = ": ${String.format("%.1f", pct * 100)}%",
                            fontSize = 14.sp,
                            color = emotionTextColors[key] ?: NeutralBarColor,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
            }

            StatSection(title = "Session Info") {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Last Visit: ${data.lastVisit}",  fontSize = 13.sp, color = Color(0xFF3C3C43))
                    Text("Duration:   ${data.duration}",   fontSize = 13.sp, color = Color(0xFF3C3C43))
                    Text("Active Time: ${data.activeTime}", fontSize = 13.sp, color = Color(0xFF3C3C43))
                }
            }

            Button(
                onClick = onReset,
                colors = ButtonDefaults.buttonColors(containerColor = ResetButtonColor),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.wrapContentWidth()
            ) {
                Text(text = "Reset Stats", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun StatSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = BlueAccent)
        content()
    }
}

// ---------------------------------------------------------------------------
// Preview
// ---------------------------------------------------------------------------
@ComposePreview(showBackground = true, showSystemUi = true)
@Composable
fun EmotionScreenPreview() {
    EmotioniaTheme {
        EmotionScreen()
    }
}