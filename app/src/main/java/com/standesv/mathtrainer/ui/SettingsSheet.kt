package com.standesv.mathtrainer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.standesv.mathtrainer.data.Progress
import com.standesv.mathtrainer.data.Settings
import com.standesv.mathtrainer.data.TablesOp

/**
 * Reglages. Aucune saisie clavier : uniquement des boutons + / − et des
 * interrupteurs, pour qu'un parent puisse ajuster vite et qu'un enfant ne
 * puisse pas saisir une valeur aberrante.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    settings: Settings,
    progress: Progress,
    onChange: ((Settings) -> Settings) -> Unit,
    onResetProgress: () -> Unit,
    onRenameProfile: () -> Unit,
    onDeleteProfile: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Reglages", style = MaterialTheme.typography.headlineMedium)

            Stepper(
                label = "Nombre de questions",
                value = settings.totalQuestions,
                step = 5,
                min = Settings.MIN_QUESTIONS,
                max = Settings.MAX_QUESTIONS
            ) { v -> onChange { it.copy(totalQuestions = v) } }

            Stepper(
                label = "Niveau (0 jusqu'a...)",
                value = settings.maxValue,
                step = 5,
                min = Settings.MIN_LEVEL,
                max = Settings.MAX_LEVEL
            ) { v -> onChange { it.copy(maxValue = v) } }

            if (settings.progressEnabled) {
                Text(
                    "La progression plafonne le niveau a ${progress.unlockedMax} pour l'instant.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            SwitchRow("Resultats negatifs autorises", settings.allowNegative) { v ->
                onChange { it.copy(allowNegative = v) }
            }

            HorizontalDivider()
            Text("Mode Tables", style = MaterialTheme.typography.titleLarge)

            Stepper(
                label = "Table",
                value = settings.tableNumber,
                step = 1,
                min = Settings.MIN_TABLE,
                max = Settings.MAX_TABLE
            ) { v -> onChange { it.copy(tableNumber = v) } }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(TablesOp.ADD to "Addition", TablesOp.SUB to "Soustraction").forEach { (op, lbl) ->
                    val sel = settings.tablesOp == op
                    Button(
                        onClick = { onChange { it.copy(tablesOp = op) } },
                        modifier = Modifier.weight(1f).height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (sel) BlueMain
                            else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (sel) Color.White else TextDark
                        )
                    ) { Text(lbl, style = MaterialTheme.typography.titleMedium) }
                }
            }

            HorizontalDivider()

            SwitchRow("Progression par paliers", settings.progressEnabled) { v ->
                onChange { it.copy(progressEnabled = v) }
            }
            SwitchRow("Sons", settings.soundEnabled) { v ->
                onChange { it.copy(soundEnabled = v) }
            }
            SwitchRow("Confettis", settings.confettiEnabled) { v ->
                onChange { it.copy(confettiEnabled = v) }
            }

            HorizontalDivider()

            OutlinedButton(
                onClick = onResetProgress,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp)
            ) { Text("Reinitialiser la progression") }

            OutlinedButton(
                onClick = onRenameProfile,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp)
            ) { Text("Renommer le profil") }

            OutlinedButton(
                onClick = onDeleteProfile,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = RedErr)
            ) { Text("Supprimer le profil") }

            BigButton("Terminer", onDismiss)
        }
    }
}

@Composable
private fun Stepper(
    label: String,
    value: Int,
    step: Int,
    min: Int,
    max: Int,
    onValue: (Int) -> Unit
) {
    Column {
        Text(label, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StepBtn("−", enabled = value > min) { onValue((value - step).coerceAtLeast(min)) }
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text("$value", style = MaterialTheme.typography.headlineMedium)
            }
            StepBtn("+", enabled = value < max) { onValue((value + step).coerceAtMost(max)) }
        }
    }
}

@Composable
private fun StepBtn(label: String, enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(58.dp),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(0.dp)
    ) { Text(label, style = MaterialTheme.typography.headlineMedium) }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}
