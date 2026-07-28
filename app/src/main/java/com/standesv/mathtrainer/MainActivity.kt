package com.standesv.mathtrainer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.ads.MobileAds
import com.standesv.mathtrainer.ui.*

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialisation du SDK publicitaire hors du thread principal.
        // Une eventuelle erreur reseau ne doit jamais empecher l'app de demarrer.
        Thread {
            runCatching { MobileAds.initialize(this) }
        }.start()

        setContent {
            MathsTrainerTheme {
                Surface(
                    Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) { AppRoot() }
            }
        }
    }
}

@Composable
fun AppRoot(vm: GameViewModel = viewModel()) {
    val ui by vm.ui.collectAsState()

    val sound = remember { SoundPlayer() }
    DisposableEffect(Unit) { onDispose { sound.release() } }

    var showSettings by remember { mutableStateOf(false) }
    var profileDialog by remember { mutableStateOf<ProfileDialog?>(null) }

    Scaffold(
        topBar = {
            Surface(color = BlueMain) {
                Row(
                    Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Maths Trainer",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { showSettings = true }) {
                        Text("Reglages", color = Color.White,
                            style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        },
        bottomBar = {
            Column {
                if (ui.screen != Screen.GAME) {
                    NavigationBar(containerColor = Color.White) {
                        NavItem("Accueil", ui.screen == Screen.HOME) { vm.go(Screen.HOME) }
                        NavItem("Jouer", ui.screen == Screen.GAME) { vm.startGame() }
                        NavItem("Scores", ui.screen == Screen.SCORES) { vm.go(Screen.SCORES) }
                    }
                }
                AdBanner(Modifier.navigationBarsPadding())
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (ui.screen) {
                Screen.HOME -> HomeScreen(
                    ui = ui,
                    onMode = { vm.setMode(it) },
                    onStart = { vm.startGame() },
                    onSelectProfile = { vm.selectProfile(it) },
                    onAddProfile = { profileDialog = ProfileDialog.Add }
                )

                Screen.GAME -> GameScreen(
                    ui = ui,
                    onDigit = { vm.onDigit(it) },
                    onBackspace = { vm.onBackspace() },
                    onSign = { vm.onToggleSign() },
                    onValidate = {
                        vm.validate { ok ->
                            if (ui.settings.soundEnabled) {
                                if (ok) sound.correct() else sound.wrong()
                            }
                        }
                    },
                    onSkip = { vm.skip() },
                    onStop = { vm.stopNow() }
                )

                Screen.RESULTS -> ResultsScreen(
                    ui = ui,
                    onReplay = { vm.startGame() },
                    onHome = { vm.go(Screen.HOME) }
                )

                Screen.SCORES -> ScoresScreen(ui = ui, onClear = { vm.clearHistory() })
            }

            ConfettiOverlay(ui.confettiTrigger, Modifier.fillMaxSize())
        }
    }

    if (showSettings) {
        SettingsSheet(
            settings = ui.settings,
            progress = ui.progress,
            onChange = { vm.updateSettings(it) },
            onResetProgress = { vm.resetProgress() },
            onRenameProfile = { profileDialog = ProfileDialog.Rename },
            onDeleteProfile = { vm.deleteProfile(); showSettings = false },
            onDismiss = { showSettings = false }
        )
    }

    profileDialog?.let { dialog ->
        TextPromptDialog(
            title = if (dialog == ProfileDialog.Add) "Nouveau profil" else "Renommer le profil",
            initial = if (dialog == ProfileDialog.Rename) ui.activeProfile else "",
            onConfirm = { name ->
                if (dialog == ProfileDialog.Add) vm.addProfile(name) else vm.renameProfile(name)
                profileDialog = null
            },
            onDismiss = { profileDialog = null }
        )
    }
}

private enum class ProfileDialog { Add, Rename }

@Composable
private fun RowScope.NavItem(label: String, selected: Boolean, onClick: () -> Unit) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {},
        label = { Text(label, style = MaterialTheme.typography.titleMedium) },
        alwaysShowLabel = true
    )
}

@Composable
private fun TextPromptDialog(
    title: String,
    initial: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var value by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                singleLine = true,
                textStyle = MaterialTheme.typography.titleMedium
            )
        },
        confirmButton = {
            TextButton(onClick = { if (value.isNotBlank()) onConfirm(value.trim()) }) {
                Text("Valider", style = MaterialTheme.typography.titleMedium)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", style = MaterialTheme.typography.titleMedium)
            }
        }
    )
}
