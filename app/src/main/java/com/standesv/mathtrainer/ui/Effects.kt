package com.standesv.mathtrainer.ui

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Sons courts de retour. ToneGenerator evite d'embarquer des fichiers audio
 * et reste fiable sur tous les appareils.
 */
class SoundPlayer {
    private var tone: ToneGenerator? = null

    private fun generator(): ToneGenerator? {
        if (tone == null) {
            tone = runCatching { ToneGenerator(AudioManager.STREAM_MUSIC, 80) }.getOrNull()
        }
        return tone
    }

    fun correct() {
        runCatching { generator()?.startTone(ToneGenerator.TONE_PROP_BEEP, 150) }
    }

    fun wrong() {
        runCatching { generator()?.startTone(ToneGenerator.TONE_SUP_ERROR, 300) }
    }

    fun release() {
        runCatching { tone?.release() }
        tone = null
    }
}

private data class Particle(
    val angle: Float,
    val speed: Float,
    val color: Color,
    val size: Float,
    val spin: Float
)

private val confettiColors = listOf(
    Color(0xFFFF6B6B), Color(0xFFFFD93D), Color(0xFF6BCB77),
    Color(0xFF4D96FF), Color(0xFFB983FF), Color(0xFFFF9F45)
)

/**
 * Explosion de confettis declenchee a chaque incrementation de [trigger].
 * Purement decoratif : aucun impact sur le deroulement de la partie.
 */
@Composable
fun ConfettiOverlay(trigger: Int, modifier: Modifier = Modifier) {
    if (trigger <= 0) return

    val particles = remember(trigger) {
        List(46) {
            Particle(
                angle = Random.nextFloat() * 2f * Math.PI.toFloat(),
                speed = 240f + Random.nextFloat() * 520f,
                color = confettiColors.random(),
                size = 10f + Random.nextFloat() * 14f,
                spin = Random.nextFloat() * 2f - 1f
            )
        }
    }

    var launched by remember(trigger) { mutableStateOf(false) }
    val t by animateFloatAsState(
        targetValue = if (launched) 1f else 0f,
        animationSpec = tween(durationMillis = 1300),
        label = "confetti"
    )
    LaunchedEffect(trigger) { launched = true }

    Canvas(modifier = modifier) {
        if (t <= 0f || t >= 1f) return@Canvas
        val originX = size.width / 2f
        val originY = size.height * 0.34f
        val gravity = 900f

        particles.forEach { p ->
            val x = originX + cos(p.angle) * p.speed * t
            val y = originY + sin(p.angle) * p.speed * t + 0.5f * gravity * t * t
            drawCircle(
                color = p.color.copy(alpha = (1f - t).coerceIn(0f, 1f)),
                radius = p.size * (1f - t * 0.35f),
                center = Offset(x, y)
            )
        }
    }
}
