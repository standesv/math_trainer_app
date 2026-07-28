package com.standesv.mathtrainer.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.dataStore by preferencesDataStore(name = "maths_trainer")

/**
 * Persistance. Chaque profil possede ses propres reglages, sa progression et
 * son historique, exactement comme la version web les separait par cle.
 */
class Store(private val context: Context) {

    private object Keys {
        val PROFILES = stringPreferencesKey("profiles")
        val ACTIVE = stringPreferencesKey("active_profile")
        fun history(p: String) = stringPreferencesKey("history::$p")
        fun unlocked(p: String) = intPreferencesKey("unlocked::$p")
        fun totalQuestions(p: String) = intPreferencesKey("total_questions::$p")
        fun maxValue(p: String) = intPreferencesKey("max_value::$p")
        fun allowNegative(p: String) = booleanPreferencesKey("allow_negative::$p")
        fun tableNumber(p: String) = intPreferencesKey("table_number::$p")
        fun tablesOp(p: String) = stringPreferencesKey("tables_op::$p")
        fun progressEnabled(p: String) = booleanPreferencesKey("progress_enabled::$p")
        fun soundEnabled(p: String) = booleanPreferencesKey("sound_enabled::$p")
        fun confettiEnabled(p: String) = booleanPreferencesKey("confetti_enabled::$p")
    }

    companion object {
        const val DEFAULT_PROFILE = "Enfant"
    }

    val data: Flow<Preferences> = context.dataStore.data

    // ---------- Profils ----------

    fun profilesFrom(prefs: Preferences): List<String> {
        val raw = prefs[Keys.PROFILES] ?: return listOf(DEFAULT_PROFILE)
        return runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { arr.getString(it) }
                .filter { it.isNotBlank() }
                .ifEmpty { listOf(DEFAULT_PROFILE) }
        }.getOrElse { listOf(DEFAULT_PROFILE) }
    }

    fun activeFrom(prefs: Preferences): String {
        val profiles = profilesFrom(prefs)
        val active = prefs[Keys.ACTIVE]
        return if (active != null && active in profiles) active else profiles.first()
    }

    suspend fun saveProfiles(profiles: List<String>) {
        val arr = JSONArray().also { a -> profiles.forEach { a.put(it) } }
        context.dataStore.edit { it[Keys.PROFILES] = arr.toString() }
    }

    suspend fun setActive(profile: String) {
        context.dataStore.edit { it[Keys.ACTIVE] = profile }
    }

    suspend fun addProfile(name: String) {
        val clean = name.trim()
        if (clean.isEmpty()) return
        context.dataStore.edit { prefs ->
            val current = profilesFrom(prefs).toMutableList()
            if (clean !in current) current.add(clean)
            prefs[Keys.PROFILES] = JSONArray().also { a -> current.forEach { a.put(it) } }.toString()
            prefs[Keys.ACTIVE] = clean
        }
    }

    suspend fun renameProfile(old: String, new: String) {
        val clean = new.trim()
        if (clean.isEmpty() || clean == old) return
        context.dataStore.edit { prefs ->
            val current = profilesFrom(prefs).map { if (it == old) clean else it }
            prefs[Keys.PROFILES] = JSONArray().also { a -> current.forEach { a.put(it) } }.toString()
            prefs[Keys.ACTIVE] = clean
            // Les donnees suivent le renommage.
            prefs[Keys.history(clean)] = prefs[Keys.history(old)] ?: "[]"
            prefs[Keys.unlocked(clean)] = prefs[Keys.unlocked(old)] ?: Progress.LEVEL_START
            prefs.remove(Keys.history(old))
            prefs.remove(Keys.unlocked(old))
        }
    }

    suspend fun deleteProfile(profile: String) {
        context.dataStore.edit { prefs ->
            val remaining = profilesFrom(prefs).filter { it != profile }
                .ifEmpty { listOf(DEFAULT_PROFILE) }
            prefs[Keys.PROFILES] = JSONArray().also { a -> remaining.forEach { a.put(it) } }.toString()
            prefs[Keys.ACTIVE] = remaining.first()
            prefs.remove(Keys.history(profile))
            prefs.remove(Keys.unlocked(profile))
        }
    }

    // ---------- Reglages ----------

    fun settingsFrom(prefs: Preferences, profile: String): Settings = Settings(
        totalQuestions = prefs[Keys.totalQuestions(profile)] ?: 10,
        maxValue = prefs[Keys.maxValue(profile)] ?: 20,
        allowNegative = prefs[Keys.allowNegative(profile)] ?: false,
        tableNumber = prefs[Keys.tableNumber(profile)] ?: 7,
        tablesOp = if (prefs[Keys.tablesOp(profile)] == "SUB") TablesOp.SUB else TablesOp.ADD,
        progressEnabled = prefs[Keys.progressEnabled(profile)] ?: true,
        soundEnabled = prefs[Keys.soundEnabled(profile)] ?: true,
        confettiEnabled = prefs[Keys.confettiEnabled(profile)] ?: true
    )

    suspend fun saveSettings(profile: String, s: Settings) {
        context.dataStore.edit { p ->
            p[Keys.totalQuestions(profile)] =
                clampInt(s.totalQuestions, Settings.MIN_QUESTIONS, Settings.MAX_QUESTIONS)
            p[Keys.maxValue(profile)] = clampInt(s.maxValue, Settings.MIN_LEVEL, Settings.MAX_LEVEL)
            p[Keys.allowNegative(profile)] = s.allowNegative
            p[Keys.tableNumber(profile)] =
                clampInt(s.tableNumber, Settings.MIN_TABLE, Settings.MAX_TABLE)
            p[Keys.tablesOp(profile)] = s.tablesOp.name
            p[Keys.progressEnabled(profile)] = s.progressEnabled
            p[Keys.soundEnabled(profile)] = s.soundEnabled
            p[Keys.confettiEnabled(profile)] = s.confettiEnabled
        }
    }

    // ---------- Progression ----------

    fun progressFrom(prefs: Preferences, profile: String): Progress =
        Progress(
            clampInt(
                prefs[Keys.unlocked(profile)] ?: Progress.LEVEL_START,
                Progress.LEVEL_STEP,
                Progress.LEVEL_CAP
            )
        )

    suspend fun saveProgress(profile: String, progress: Progress) {
        context.dataStore.edit { it[Keys.unlocked(profile)] = progress.unlockedMax }
    }

    suspend fun resetProgress(profile: String) {
        context.dataStore.edit { it[Keys.unlocked(profile)] = Progress.LEVEL_START }
    }

    // ---------- Historique ----------

    fun historyFrom(prefs: Preferences, profile: String): List<HistoryEntry> {
        val raw = prefs[Keys.history(profile)] ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                HistoryEntry(
                    timestamp = o.getLong("ts"),
                    mode = o.getString("mode"),
                    maxValue = o.getInt("maxValue"),
                    total = o.getInt("total"),
                    ok = o.getInt("ok"),
                    score = o.getInt("score"),
                    timeSec = o.getInt("timeSec"),
                    avgSec = o.getDouble("avgSec")
                )
            }
        }.getOrElse { emptyList() }
    }

    suspend fun addHistory(profile: String, entry: HistoryEntry) {
        context.dataStore.edit { prefs ->
            val current = historyFrom(prefs, profile).toMutableList()
            current.add(0, entry)
            val trimmed = current.take(HistoryEntry.HISTORY_MAX)
            val arr = JSONArray()
            trimmed.forEach { e ->
                arr.put(
                    JSONObject()
                        .put("ts", e.timestamp)
                        .put("mode", e.mode)
                        .put("maxValue", e.maxValue)
                        .put("total", e.total)
                        .put("ok", e.ok)
                        .put("score", e.score)
                        .put("timeSec", e.timeSec)
                        .put("avgSec", e.avgSec)
                )
            }
            prefs[Keys.history(profile)] = arr.toString()
        }
    }

    suspend fun clearHistory(profile: String) {
        context.dataStore.edit { it.remove(Keys.history(profile)) }
    }
}
