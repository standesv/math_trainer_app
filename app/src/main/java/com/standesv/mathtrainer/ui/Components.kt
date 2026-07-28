package com.standesv.mathtrainer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** Bouton principal, volontairement tres large pour un doigt d'enfant. */
@Composable
fun BigButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    container: Color = MaterialTheme.colorScheme.primary,
    content: Color = Color.White,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(68.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = container,
            contentColor = content
        )
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
    }
}

/** Carte blanche arrondie, brique de base de tous les ecrans. */
@Composable
fun SoftCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 2.dp
    ) {
        Column(Modifier.padding(18.dp), content = content)
    }
}

/** Petite etiquette d'information (chrono, compteurs). */
@Composable
fun Pill(text: String, container: Color = MaterialTheme.colorScheme.surfaceVariant) {
    Box(
        Modifier
            .background(container, RoundedCornerShape(999.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}

/**
 * Pave numerique maison. Il remplace le clavier systeme : cibles bien plus
 * grandes, pas de risque de voir l'enfant ouvrir un autre clavier ou saisir
 * autre chose que des chiffres.
 */
@Composable
fun NumPad(
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onValidate: () -> Unit,
    onSign: (() -> Unit)? = null,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9")
    )

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { row ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { d ->
                    KeyButton(d, Modifier.weight(1f), enabled) { onDigit(d) }
                }
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (onSign != null) {
                KeyButton("±", Modifier.weight(1f), enabled, Amber) { onSign() }
            } else {
                Spacer(Modifier.weight(1f))
            }
            KeyButton("0", Modifier.weight(1f), enabled) { onDigit("0") }
            KeyButton("⌫", Modifier.weight(1f), enabled, RedErr) { onBackspace() }
        }

        BigButton(
            text = "Valider",
            onClick = onValidate,
            enabled = enabled,
            container = GreenOk
        )
    }
}

@Composable
private fun KeyButton(
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    container: Color = MaterialTheme.colorScheme.primaryContainer,
    onClick: () -> Unit
) {
    val isDigit = label.firstOrNull()?.isDigit() == true
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(64.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = container,
            contentColor = if (isDigit) BlueDark else Color.White
        )
    ) {
        Text(label, style = MaterialTheme.typography.headlineMedium)
    }
}
