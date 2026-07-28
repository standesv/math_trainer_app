package com.standesv.mathtrainer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.standesv.mathtrainer.Screen
import com.standesv.mathtrainer.UiState
import com.standesv.mathtrainer.data.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ----------------------------------------------------------------- ACCUEIL

@Composable
fun HomeScreen(
    ui: UiState,
    onMode: (GameMode) -> Unit,
    onStart: () -> Unit,
    onSelectProfile: (String) -> Unit,
    onAddProfile: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SoftCard {
            Text("Qui joue ?", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProfileDropdown(
                    profiles = ui.profiles,
                    active = ui.activeProfile,
                    onSelect = onSelectProfile,
                    modifier = Modifier.weight(1f)
                )
                FilledTonalButton(
                    onClick = onAddProfile,
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(0.dp)
                ) { Text("+", style = MaterialTheme.typography.headlineMedium) }
            }
        }

        SoftCard {
            Text("Choisis ton jeu", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))

            val modes = GameMode.entries.toList()
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                modes.chunked(2).forEach { pair ->
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        pair.forEach { m ->
                            val selected = ui.mode == m
                            Button(
                                onClick = { onMode(m) },
                                modifier = Modifier.weight(1f).height(64.dp),
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selected) BlueMain
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (selected) Color.White else TextDark
                                )
                            ) {
                                Text(m.label, style = MaterialTheme.typography.headlineMedium)
                            }
                        }
                        if (pair.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(ui.mode.hint, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }

        SoftCard {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Pill("Niveau 0 → ${ui.effectiveMax}")
                Pill("${ui.settings.totalQuestions} questions")
            }
            if (ui.settings.progressEnabled) {
                Spacer(Modifier.height(10.dp))
                Text(
                    "Palier debloque : ${ui.progress.unlockedMax}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }

        BigButton("Demarrer", onStart, container = GreenOk)
        Spacer(Modifier.height(8.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileDropdown(
    profiles: List<String>,
    active: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = active,
            onValueChange = {},
            readOnly = true,
            textStyle = MaterialTheme.typography.titleMedium,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            profiles.forEach { p ->
                DropdownMenuItem(
                    text = { Text(p, style = MaterialTheme.typography.titleMedium) },
                    onClick = { onSelect(p); expanded = false }
                )
            }
        }
    }
}

// -------------------------------------------------------------------- JEU

@Composable
fun GameScreen(
    ui: UiState,
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onSign: () -> Unit,
    onValidate: () -> Unit,
    onSkip: () -> Unit,
    onStop: () -> Unit
) {
    val q = ui.current ?: return

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Pill("${ui.currentIndex + 1} / ${ui.total}")
            Pill("${ui.okCount} ✓   ${ui.errCount} ✗")
            Pill(formatMmSs(ui.elapsedMs))
        }

        LinearProgressIndicator(
            progress = { (ui.currentIndex + 1f) / ui.total.coerceAtLeast(1) },
            modifier = Modifier.fillMaxWidth().height(10.dp),
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )

        SoftCard {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${q.a} ${q.op} ${q.b} =",
                    style = MaterialTheme.typography.displayLarge,
                    color = TextDark
                )
                Spacer(Modifier.width(12.dp))
                Box(
                    Modifier
                        .widthIn(min = 96.dp)
                        .height(76.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(18.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        ui.input.ifEmpty { "?" },
                        style = MaterialTheme.typography.displayLarge,
                        color = if (ui.input.isEmpty()) Color.Gray else BlueMain
                    )
                }
            }

            if (ui.feedback != null) {
                Spacer(Modifier.height(10.dp))
                Text(
                    ui.feedback,
                    style = MaterialTheme.typography.titleLarge,
                    color = if (ui.feedbackOk) GreenOk else RedErr
                )
            }
        }

        NumPad(
            onDigit = onDigit,
            onBackspace = onBackspace,
            onValidate = onValidate,
            onSign = if (ui.settings.allowNegative) onSign else null,
            enabled = !ui.locked
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = onSkip,
                enabled = !ui.locked,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(18.dp)
            ) { Text("Passer", style = MaterialTheme.typography.titleMedium) }

            OutlinedButton(
                onClick = onStop,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(18.dp)
            ) { Text("Arreter", style = MaterialTheme.typography.titleMedium) }
        }
    }
}

// ---------------------------------------------------------------- RESULTAT

@Composable
fun ResultsScreen(ui: UiState, onReplay: () -> Unit, onHome: () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SoftCard {
            Text("Resultat", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(14.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Metric("Reussites", "${ui.okCount}", GreenOk)
                Metric("Erreurs", "${ui.total - ui.okCount}", RedErr)
                Metric("Score", "${ui.score}%", BlueMain)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "Temps ${formatMmSs(ui.elapsedMs)} — %.1f s par question"
                    .format(ui.elapsedMs / 1000.0 / ui.total.coerceAtLeast(1)),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }

        if (ui.levelUpTo != null) {
            SoftCard {
                Text("Nouveau palier !", style = MaterialTheme.typography.titleLarge, color = Amber)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Tu peux maintenant jouer jusqu'a ${ui.levelUpTo}.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        SoftCard {
            Text("Le corrige", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(10.dp))
            ui.questions.forEachIndexed { i, q ->
                val ok = q.correct == true
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "${i + 1}. ${q.statement} = ${q.userAnswer ?: "—"}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        if (ok) "✓" else "✗ ${q.answer}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (ok) GreenOk else RedErr
                    )
                }
            }
        }

        BigButton("Rejouer", onReplay, container = GreenOk)
        BigButton("Accueil", onHome, container = MaterialTheme.colorScheme.surfaceVariant, content = TextDark)
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun Metric(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineLarge, color = color)
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
    }
}

// ------------------------------------------------------------------ SCORES

@Composable
fun ScoresScreen(ui: UiState, onClear: () -> Unit) {
    val fmt = remember { SimpleDateFormat("dd/MM HH:mm", Locale.FRANCE) }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Scores de ${ui.activeProfile}",
            style = MaterialTheme.typography.headlineMedium
        )

        if (ui.history.isEmpty()) {
            SoftCard {
                Text("Aucun score pour l'instant", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Text("Joue une partie pour commencer.", color = Color.Gray)
            }
        } else {
            ui.history.forEach { h ->
                SoftCard {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(h.mode, style = MaterialTheme.typography.titleMedium)
                            Text(
                                "${fmt.format(Date(h.timestamp))} — niveau ${h.maxValue}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                        Text(
                            "${h.score}%",
                            style = MaterialTheme.typography.headlineMedium,
                            color = when {
                                h.score >= 80 -> GreenOk
                                h.score >= 50 -> Amber
                                else -> RedErr
                            }
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "${h.ok} bonnes / ${h.total} — ${h.timeSec}s — %.1f s par question"
                            .format(h.avgSec),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }

            BigButton("Effacer les scores", onClear, container = RedErr)
        }
        Spacer(Modifier.height(8.dp))
    }
}
