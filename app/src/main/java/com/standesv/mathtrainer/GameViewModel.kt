package com.standesv.mathtrainer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.standesv.mathtrainer.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class Screen { HOME, GAME, RESULTS, SCORES }

data class UiState(
    val screen: Screen = Screen.HOME,
    val profiles: List<String> = listOf(Store.DEFAULT_PROFILE),
    val activeProfile: String = Store.DEFAULT_PROFILE,
    val settings: Settings = Settings(),
    val progress: Progress = Progress(),
    val history: List<HistoryEntry> = emptyList(),
    val mode: GameMode = GameMode.MIX,

    // Partie en cours
    val questions: List<Question> = emptyList(),
    val currentIndex: Int = 0,
    val okCount: Int = 0,
    val input: String = "",
    val feedback: String? = null,
    val feedbackOk: Boolean = false,
    val locked: Boolean = false,
    val elapsedMs: Long = 0L,

    // Effets
    val confettiTrigger: Int = 0,
    val levelUpTo: Int? = null
) {
    val current: Question? get() = questions.getOrNull(currentIndex)
    val total: Int get() = settings.totalQuestions
    val errCount: Int get() = answeredCount - okCount
    val answeredCount: Int get() = questions.count { it.correct != null || it.skipped }
    val score: Int get() = if (total > 0) Math.round(okCount * 100f / total) else 0
    val avgSecPerQuestion: Double
        get() = if (answeredCount > 0) elapsedMs / 1000.0 / answeredCount else 0.0

    /** Niveau reellement applique : plafonne par la progression si activee. */
    val effectiveMax: Int
        get() = if (settings.progressEnabled) minOf(settings.maxValue, progress.unlockedMax)
        else settings.maxValue
}

class GameViewModel(app: Application) : AndroidViewModel(app) {

    private val store = Store(app)
    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui.asStateFlow()

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            store.data.collect { prefs ->
                val profiles = store.profilesFrom(prefs)
                val active = store.activeFrom(prefs)
                _ui.value = _ui.value.copy(
                    profiles = profiles,
                    activeProfile = active,
                    settings = store.settingsFrom(prefs, active),
                    progress = store.progressFrom(prefs, active),
                    history = store.historyFrom(prefs, active)
                )
            }
        }
    }

    // ---------- Navigation ----------

    fun go(screen: Screen) {
        if (screen == Screen.GAME && _ui.value.questions.isEmpty()) {
            startGame(); return
        }
        _ui.value = _ui.value.copy(screen = screen)
    }

    fun setMode(mode: GameMode) {
        _ui.value = _ui.value.copy(mode = mode)
    }

    // ---------- Profils ----------

    fun selectProfile(name: String) = viewModelScope.launch { store.setActive(name) }
    fun addProfile(name: String) = viewModelScope.launch { store.addProfile(name) }
    fun renameProfile(new: String) =
        viewModelScope.launch { store.renameProfile(_ui.value.activeProfile, new) }

    fun deleteProfile() =
        viewModelScope.launch { store.deleteProfile(_ui.value.activeProfile) }

    // ---------- Reglages ----------

    fun updateSettings(block: (Settings) -> Settings) {
        val updated = block(_ui.value.settings)
        _ui.value = _ui.value.copy(settings = updated)
        viewModelScope.launch { store.saveSettings(_ui.value.activeProfile, updated) }
    }

    fun resetProgress() =
        viewModelScope.launch { store.resetProgress(_ui.value.activeProfile) }

    fun clearHistory() =
        viewModelScope.launch { store.clearHistory(_ui.value.activeProfile) }

    // ---------- Partie ----------

    fun startGame() {
        val s = _ui.value
        val max = s.effectiveMax
        val questions = List(s.settings.totalQuestions) {
            QuestionGenerator.generate(s.mode, s.settings, max)
        }
        _ui.value = s.copy(
            screen = Screen.GAME,
            questions = questions,
            currentIndex = 0,
            okCount = 0,
            input = "",
            feedback = null,
            locked = false,
            elapsedMs = 0L,
            levelUpTo = null
        )
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        val startedAt = System.currentTimeMillis()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(250)
                _ui.value = _ui.value.copy(elapsedMs = System.currentTimeMillis() - startedAt)
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    fun onDigit(d: String) {
        val s = _ui.value
        if (s.locked) return
        if (s.input.length >= 4) return
        _ui.value = s.copy(input = s.input + d)
    }

    fun onBackspace() {
        val s = _ui.value
        if (s.locked) return
        _ui.value = s.copy(input = s.input.dropLast(1))
    }

    fun onToggleSign() {
        val s = _ui.value
        if (s.locked || !s.settings.allowNegative) return
        val next = if (s.input.startsWith("-")) s.input.drop(1) else "-" + s.input
        _ui.value = s.copy(input = next)
    }

    fun validate(onSound: (Boolean) -> Unit) {
        val s = _ui.value
        if (s.locked) return
        val q = s.current ?: return
        val user = s.input.toIntOrNull() ?: return

        val correct = user == q.answer
        val updated = s.questions.toMutableList()
        updated[s.currentIndex] = q.copy(userAnswer = user, correct = correct)

        onSound(correct)

        _ui.value = s.copy(
            questions = updated,
            okCount = if (correct) s.okCount + 1 else s.okCount,
            feedback = if (correct) "Bravo !" else "La reponse etait ${q.answer}",
            feedbackOk = correct,
            locked = true,
            confettiTrigger = if (correct && s.settings.confettiEnabled)
                s.confettiTrigger + 1 else s.confettiTrigger
        )

        viewModelScope.launch {
            delay(if (correct) 650 else 1400)
            advance()
        }
    }

    fun skip() {
        val s = _ui.value
        if (s.locked) return
        val q = s.current ?: return
        val updated = s.questions.toMutableList()
        updated[s.currentIndex] = q.copy(skipped = true, correct = false)
        _ui.value = s.copy(
            questions = updated,
            feedback = "Passe - la reponse etait ${q.answer}",
            feedbackOk = false,
            locked = true
        )
        viewModelScope.launch {
            delay(1200)
            advance()
        }
    }

    private fun advance() {
        val s = _ui.value
        if (s.currentIndex >= s.total - 1) {
            finish()
        } else {
            _ui.value = s.copy(
                currentIndex = s.currentIndex + 1,
                input = "",
                feedback = null,
                locked = false
            )
        }
    }

    fun stopNow() {
        stopTimer()
        _ui.value = _ui.value.copy(screen = Screen.HOME, questions = emptyList(), locked = false)
    }

    private fun finish() {
        stopTimer()
        val s = _ui.value

        val timeSec = (s.elapsedMs / 1000).toInt()
        val avg = if (s.total > 0) s.elapsedMs / 1000.0 / s.total else 0.0
        val modeLabel = if (s.mode == GameMode.TABLES)
            "Table de ${s.settings.tableNumber} (${s.settings.tablesOp.symbol})"
        else s.mode.label

        viewModelScope.launch {
            store.addHistory(
                s.activeProfile,
                HistoryEntry(
                    timestamp = System.currentTimeMillis(),
                    mode = modeLabel,
                    maxValue = s.effectiveMax,
                    total = s.total,
                    ok = s.okCount,
                    score = s.score,
                    timeSec = timeSec,
                    avgSec = Math.round(avg * 10) / 10.0
                )
            )
        }

        // Deblocage du palier suivant, memes regles que la version web.
        var levelUp: Int? = null
        if (s.settings.progressEnabled &&
            s.total >= Progress.PASS_MIN_QUESTIONS &&
            s.score >= Progress.PASS_SCORE_MIN
        ) {
            val next = minOf(s.progress.unlockedMax + Progress.LEVEL_STEP, Progress.LEVEL_CAP)
            if (next > s.progress.unlockedMax) {
                levelUp = next
                viewModelScope.launch { store.saveProgress(s.activeProfile, Progress(next)) }
            }
        }

        _ui.value = s.copy(screen = Screen.RESULTS, locked = false, levelUpTo = levelUp)
    }

    override fun onCleared() {
        stopTimer()
        super.onCleared()
    }
}
