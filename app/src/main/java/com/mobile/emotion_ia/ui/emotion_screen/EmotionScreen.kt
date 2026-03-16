package com.mobile.emotion_ia.ui.emotion_screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobile.emotion_ia.data.model.EmotionData
import com.mobile.emotion_ia.ui.theme.EmotioniaTheme

// ---------------------------------------------------------------------------
// Colour tokens matching the reference screenshots
// ---------------------------------------------------------------------------
private val BackgroundColor      = Color(0xFFF0F2F5)
private val CardColor            = Color.White
private val BlueAccent           = Color(0xFF4B8EF1)
private val NeutralBarColor      = Color(0xFF8E8E93)
private val SadBarColor          = Color(0xFF4B8EF1)
private val HappyBarColor        = Color(0xFFFFD60A)
private val AngryBarColor        = Color(0xFFFF3B30)
private val SurprisedBarColor    = Color(0xFFAF52DE)
private val ResetButtonColor     = Color(0xFFFF3B30)

// Colour used in the Statistics "Average Emotions" rows
private val emotionTextColors = mapOf(
    "neutral"   to NeutralBarColor,
    "sad"       to SadBarColor,
    "happy"     to HappyBarColor,
    "angry"     to AngryBarColor,
    "surprised" to SurprisedBarColor
)

@Composable
fun EmotionScreen(
    modifier: Modifier = Modifier,
    viewModel: EmotionViewModel = viewModel()
) {
    val emotionData by viewModel.emotionData.collectAsState()

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

        // ── Camera Preview Placeholder ─────────────────────────────────────
        CameraPreviewCard()

        // ── Emotion Bars ──────────────────────────────────────────────────
        EmotionBarsCard(emotionData)

        // ── Status Card ───────────────────────────────────────────────────
        StatusCard(emotionData)

        // ── Action Icons Row ──────────────────────────────────────────────
        ActionIconsRow()

        // ── Statistics Card ───────────────────────────────────────────────
        StatisticsCard(emotionData, onReset = { viewModel.resetStats() })

        Spacer(Modifier.height(24.dp))
    }
}

// ─── Camera Preview Placeholder ────────────────────────────────────────────

@Composable
private fun CameraPreviewCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFD1D1D6)),
        contentAlignment = Alignment.Center
    ) {
        // Replace this with CameraX PreviewView via AndroidView in the real camera integration
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.PhotoCamera,
                contentDescription = "Camera",
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Camera Preview",
                color = Color.White.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ─── Emotion Bars ──────────────────────────────────────────────────────────

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
    val animatedValue by animateFloatAsState(
        targetValue = value.coerceIn(0f, 1f),
        animationSpec = tween(600),
        label = "emotion_bar_$label"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Label
        Text(
            text = label,
            modifier = Modifier.width(80.dp),
            fontSize = 14.sp,
            color = Color(0xFF636366)
        )

        // Progress bar track
        Box(
            modifier = Modifier
                .weight(1f)
                .height(10.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFFE5E5EA))
        ) {
            // Filled portion
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = animatedValue.coerceAtLeast(0.01f))
                    .clip(RoundedCornerShape(50))
                    .background(barColor)
            )
        }

        // Percentage
        Text(
            text = "${(value * 100).toInt()}%",
            modifier = Modifier
                .width(40.dp)
                .padding(start = 8.dp),
            fontSize = 13.sp,
            color = Color(0xFF636366)
        )
    }
}

// ─── Status Card ──────────────────────────────────────────────────────────

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
            StatusRow(
                label = "Status:",
                value = "${data.status} Score: ${String.format("%.2f", data.score)}"
            )
            StatusRow(
                label = "Models:",
                value = if (data.modelsLoaded) "✓ Loaded" else "✗ Not loaded",
                valueColor = if (data.modelsLoaded) Color(0xFF34C759) else Color(0xFFFF3B30)
            )
            StatusRow(
                label = "Camera:",
                value = if (data.cameraActive) "✓ Active" else "✗ Inactive",
                valueColor = if (data.cameraActive) Color(0xFF34C759) else Color(0xFFFF3B30)
            )
        }
    }
}

@Composable
private fun StatusRow(
    label: String,
    value: String,
    valueColor: Color = Color(0xFF3C3C43)
) {
    Row {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = Color(0xFF3C3C43)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            color = valueColor,
            fontStyle = FontStyle.Italic
        )
    }
}

// ─── Action Icons Row ─────────────────────────────────────────────────────

@Composable
private fun ActionIconsRow() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
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

// ─── Statistics Card ──────────────────────────────────────────────────────

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
            // Header
            Text(
                text = "Statistics",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.Black,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            // Total Interactions
            StatSection(title = "Total Interactions") {
                Text(
                    text = data.totalInteractions.toString(),
                    fontSize = 15.sp,
                    color = Color(0xFF3C3C43)
                )
            }

            // Average Emotions
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

            // Session Info
            StatSection(title = "Session Info") {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Last Visit: ${data.lastVisit}",
                        fontSize = 13.sp,
                        color = Color(0xFF3C3C43)
                    )
                    Text(
                        text = "Duration:   ${data.duration}",
                        fontSize = 13.sp,
                        color = Color(0xFF3C3C43)
                    )
                    Text(
                        text = "Active Time: ${data.activeTime}",
                        fontSize = 13.sp,
                        color = Color(0xFF3C3C43)
                    )
                }
            }

            // Reset button
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
        Text(
            text = title,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            color = BlueAccent
        )
        content()
    }
}

// ─── Preview ──────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun EmotionScreenPreview() {
    EmotioniaTheme {
        EmotionScreen()
    }
}