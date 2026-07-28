package com.standesv.mathtrainer.data

import kotlin.random.Random

/** Modes de jeu, repris a l'identique de la version web. */
enum class GameMode(val label: String, val hint: String) {
    MIX("Mix", "Additions et soustractions melangees"),
    ADD("+", "Additions uniquement"),
    SUB("−", "Soustractions uniquement"),
    TABLES("Tables", "Une table precise, en + ou en −")
}

enum class TablesOp { ADD, SUB }

/** Reglages persistes par profil. */
data class Settings(
    val totalQuestions: Int = 10,
    val maxValue: Int = 20,
    val allowNegative: Boolean = false,
    val tableNumber: Int = 7,
    val tablesOp: TablesOp = TablesOp.ADD,
    val progressEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val confettiEnabled: Boolean = true
) {
    companion object {
        const val MIN_QUESTIONS = 5
        const val MAX_QUESTIONS = 50
        const val MIN_LEVEL = 5
        const val MAX_LEVEL = 200
        const val MIN_TABLE = 1
        const val MAX_TABLE = 20
    }
}

/**
 * Progression : le niveau debloque demarre a 10 et monte par paliers de 10
 * jusqu'a 200. Regle de passage identique a l'originale : au moins
 * 10 questions dans la partie et 80 % de reussite.
 */
data class Progress(val unlockedMax: Int = LEVEL_START) {
    companion object {
        const val LEVEL_START = 10
        const val LEVEL_STEP = 10
        const val LEVEL_CAP = 200
        const val PASS_SCORE_MIN = 80
        const val PASS_MIN_QUESTIONS = 10
    }
}

/** Une question posee, et la reponse eventuelle de l'enfant. */
data class Question(
    val a: Int,
    val b: Int,
    val op: String,
    val answer: Int,
    val userAnswer: Int? = null,
    val correct: Boolean? = null,
    val skipped: Boolean = false
) {
    val statement: String get() = "$a $op $b"
}

/** Une partie terminee, telle qu'archivee dans l'historique. */
data class HistoryEntry(
    val timestamp: Long,
    val mode: String,
    val maxValue: Int,
    val total: Int,
    val ok: Int,
    val score: Int,
    val timeSec: Int,
    val avgSec: Double
) {
    val err: Int get() = total - ok

    companion object {
        const val HISTORY_MAX = 12
    }
}

/** Generation des questions. Toute la logique arithmetique vit ici. */
object QuestionGenerator {

    fun generate(mode: GameMode, settings: Settings, effectiveMax: Int): Question =
        if (mode == GameMode.TABLES) tables(settings, effectiveMax)
        else standard(mode, settings, effectiveMax)

    private fun pickOp(mode: GameMode): String = when (mode) {
        GameMode.ADD -> "+"
        GameMode.SUB -> "−"
        else -> if (Random.nextBoolean()) "+" else "−"
    }

    private fun standard(mode: GameMode, settings: Settings, max: Int): Question {
        val op = pickOp(mode)
        var a = Random.nextInt(0, max + 1)
        var b = Random.nextInt(0, max + 1)

        // Sans resultats negatifs autorises, on garantit a >= b.
        if (op == "−" && !settings.allowNegative && b > a) {
            val tmp = a; a = b; b = tmp
        }

        val answer = if (op == "+") a + b else a - b
        return Question(a = a, b = b, op = op, answer = answer)
    }

    private fun tables(settings: Settings, max: Int): Question {
        val t = settings.tableNumber.coerceIn(Settings.MIN_TABLE, Settings.MAX_TABLE)
        val n = Random.nextInt(0, max + 1)
        return if (settings.tablesOp == TablesOp.ADD) {
            Question(a = t, b = n, op = "+", answer = t + n)
        } else {
            val a = t + n
            Question(a = a, b = t, op = "−", answer = a - t)
        }
    }
}

fun clampInt(value: Int, min: Int, max: Int): Int = value.coerceIn(min, max)

fun formatMmSs(millis: Long): String {
    val totalSec = millis / 1000
    val m = totalSec / 60
    val s = totalSec % 60
    return "%02d:%02d".format(m, s)
}
