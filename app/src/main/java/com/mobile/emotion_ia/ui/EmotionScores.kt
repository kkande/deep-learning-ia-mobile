package com.mobile.emotion_ia.ui.emotion_screen

import androidx.compose.ui.graphics.Color
import com.mobile.emotion_ia.data.model.EmotionData
import com.mobile.emotion_ia.data.remote.FaceEmotionResponse

data class EmotionBarSpec(
    val key: String,
    val label: String,
    val color: Color,
)

val emotionBarSpecs: List<EmotionBarSpec> = listOf(
    EmotionBarSpec("neutral", "Neutral", Color(0xFF9E9E9E)),
    EmotionBarSpec("sad", "Sad", Color(0xFF2196F3)),
    EmotionBarSpec("happy", "Happy", Color(0xFFFFC107)),
    EmotionBarSpec("angry", "Angry", Color(0xFFF44336)),
    EmotionBarSpec("surprised", "Surprised", Color(0xFF9C27B0)),
)

fun normalizeEmotionKey(raw: String): String {
    val k = raw.lowercase().trim()
    return when (k) {
        "neutre", "neutral", "calm" -> "neutral"
        "sad", "triste", "sadness" -> "sad"
        "happy", "heureux", "happiness", "joy" -> "happy"
        "angry", "en colère", "anger", "mad" -> "angry"
        "surprised", "surpris", "surprise" -> "surprised"
        else -> k
    }
}

fun buildScoreFractions(
    face: FaceEmotionResponse?,
    text: EmotionData?,
): List<Pair<EmotionBarSpec, Float>> {
    val lookup = mutableMapOf<String, Double>()
    if (face != null) {
        face.scores.forEach { (key, value) ->
            lookup[normalizeEmotionKey(key)] = value
        }
    } else if (text != null) {
        lookup[normalizeEmotionKey(text.label)] = text.score.toDouble()
    }
    val maxV = lookup.values.maxOrNull() ?: 0.0
    val assumePercent = maxV > 1.5
    return emotionBarSpecs.map { spec ->
        val raw = lookup[spec.key] ?: 0.0
        val frac = when {
            assumePercent -> (raw / 100.0).coerceIn(0.0, 1.0)
            else -> raw.coerceIn(0.0, 1.0)
        }
        spec to frac.toFloat()
    }
}

fun statusPrimaryLine(face: FaceEmotionResponse?, text: EmotionData?): String = when {
    face != null -> {
        val conf = face.confidence
        val display = if (conf > 1.5) conf / 100.0 else conf
        val scoreStr = String.format(java.util.Locale.FRANCE, "%.2f", display)
        "Face detected! Score: $scoreStr"
    }
    text != null -> {
        val pct = (text.score.coerceIn(0f, 1f) * 100).toInt()
        "Text emotion: ${text.label} ($pct %)"
    }
    else -> "No analysis yet — capture a face or analyze text."
}

fun shareSummary(face: FaceEmotionResponse?, text: EmotionData?): String = when {
    face != null -> {
        val top = face.label
        val conf = face.confidence
        val frac = if (conf > 1.5) conf / 100.0 else conf
        "AI Emotion Mirror — Face: $top (confidence ${"%.0f".format(frac * 100)} %)"
    }
    text != null -> "AI Emotion Mirror — Text: ${text.label}"
    else -> "AI Emotion Mirror"
}
