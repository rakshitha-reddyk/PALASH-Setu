package com.palash.setu.ui.screens

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.speech.RecognizerIntent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.palash.setu.data.dao.FLNDictionaryDao
import com.palash.setu.ui.theme.*
import com.palash.setu.util.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveTranslatorScreen(targetLanguage: String, dictionaryDao: FLNDictionaryDao) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val networkObserver = remember { NetworkObserver(context) }
    val networkStatus by networkObserver.observe.collectAsState(initial = NetworkObserver.Status.Unavailable)
    val isOnline = networkStatus == NetworkObserver.Status.Available

    val tts = remember { TranslationTTS(context) }
    val engine = remember(dictionaryDao) { MockAudioTranslatorEngine(dictionaryDao) }
    val viewModel = remember { TranslatorViewModel(engine) }
    val uiState by viewModel.uiState.collectAsState()

    var isPlaying by remember { mutableStateOf(false) }
    var currentMediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    // Language Selection
    val languages = listOf("Santhali (Ol Chiki)", "Ho (Warang Chiti)", "Mundari (Bani Hisir)")
    var selectedLangName by remember { mutableStateOf(languages.find { it.contains(targetLanguage, ignoreCase = true) } ?: languages.first()) }
    var langExpanded by remember { mutableStateOf(false) }
    val currentLangCode = when {
        selectedLangName.contains("Santhali") -> "Santhali"
        selectedLangName.contains("Ho") -> "Ho"
        else -> "Mundari"
    }

    val speechLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.stopRecording()
        if (result.resultCode == RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            viewModel.onHindiVoiceInput(spokenText, currentLangCode)
        }
    }

    fun stopAllAudio() {
        tts.stop()
        currentMediaPlayer?.stop()
        currentMediaPlayer?.release()
        currentMediaPlayer = null
        isPlaying = false
    }

    fun playAudio(resId: Int?, fallbackText: String) {
        stopAllAudio()
        if (resId != null && resId != 0) {
            try {
                isPlaying = true
                val mp = MediaPlayer.create(context, resId)
                if (mp == null) {
                    isPlaying = false
                    tts.speak(fallbackText)
                    return
                }
                currentMediaPlayer = mp
                mp.setVolume(1.0f, 1.0f) // Forced max volume
                mp.setOnCompletionListener {
                    it.release()
                    if (currentMediaPlayer == it) {
                        currentMediaPlayer = null
                        isPlaying = false
                    }
                }
                mp.setOnErrorListener { player, _, _ ->
                    player.release()
                    if (currentMediaPlayer == player) {
                        currentMediaPlayer = null
                        isPlaying = false
                    }
                    true
                }
                mp.start()
            } catch (e: Exception) {
                Log.e("LiveTranslator", "MediaPlayer error: ${e.message}")
                isPlaying = false
                tts.speak(fallbackText)
            }
        } else {
            tts.speak(fallbackText)
        }
    }

    // Auto-play audio when translation result updates
    LaunchedEffect(uiState.result) {
        uiState.result?.let { translation ->
            val audioRes = getAudioResource(context, translation.sourceHindi)
            playAudio(audioRes, translation.devanagariText)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            tts.shutdown()
            currentMediaPlayer?.release()
        }
    }

    val isRecording = uiState.isRecording
    val result = uiState.result

    val pulse by rememberInfiniteTransition(label = "mic-pulse").animateFloat(
        initialValue = 1f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(650), RepeatMode.Reverse), label = "pulse"
    )

    Column(
        Modifier
            .fillMaxSize()
            .background(PalashBackground)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Live Translator", style = MaterialTheme.typography.headlineMedium)
                Text("Hindi  ->  $currentLangCode", color = PalashBlue, style = MaterialTheme.typography.labelLarge)
            }
            AssistChip(
                onClick = {},
                label = { Text(if (isOnline) "Online" else "Offline") },
                leadingIcon = { Text("●", color = if (isOnline) PalashGreen else Color.Gray) }
            )
        }

        // Target Language Selector
        ExposedDropdownMenuBox(
            expanded = langExpanded,
            onExpandedChange = { langExpanded = !langExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedLangName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Target Language") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = langExpanded) },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = langExpanded,
                onDismissRequest = { langExpanded = false }
            ) {
                languages.forEach { lang ->
                    DropdownMenuItem(
                        text = { Text(lang) },
                        onClick = {
                            selectedLangName = lang
                            langExpanded = false
                        }
                    )
                }
            }
        }

        // Translation Card Display
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            val labelStyle = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Column(Modifier.padding(18.dp)) {
                // Hindi Input Section
                Text("Hindi Input (Spoken)", style = labelStyle)
                Text(
                    if (uiState.hindiInput.isNotEmpty()) uiState.hindiInput else "Tap mic and speak in Hindi...",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Classroom Translation Section
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f)) {
                        Text("Classroom Translation ($currentLangCode)", style = labelStyle)
                        Text(
                            if (uiState.santhaliScript.isNotEmpty()) uiState.santhaliScript else "Native translation will appear here...",
                            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 22.sp),
                            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                        )
                    }
                    if (result != null) {
                        IconButton(onClick = {
                            val audioRes = getAudioResource(context, uiState.hindiInput)
                            playAudio(audioRes, uiState.santhaliPhonetic)
                        }) {
                            Icon(Icons.Default.VolumeUp, contentDescription = "Play Audio", tint = PalashBlue)
                        }
                    }
                }

                // Phonetic Guide Section
                Text("Phonetic Pronunciation Guide", style = labelStyle.copy(fontSize = 12.sp))
                Text(
                    if (uiState.santhaliPhonetic.isNotEmpty()) uiState.santhaliPhonetic else "Pronunciation guide will appear here",
                    style = MaterialTheme.typography.bodyLarge,
                    color = PalashBlue,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        Spacer(Modifier.weight(1f))

        if (isPlaying) {
            LinearProgressIndicator(Modifier.fillMaxWidth(), color = PalashGreen)
            Text("Playing translated audio...", color = PalashGreen)
        }

        // Main Mic Button
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Button(
                onClick = {
                    stopAllAudio()
                    try {
                        viewModel.startRecording()
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true) // FORCES OFFLINE SPEECH ENGINE
                            putExtra(RecognizerIntent.EXTRA_PROMPT, "Listening (Offline Mode)... Speak in Hindi")
                        }
                        speechLauncher.launch(intent)
                    } catch (e: Exception) {
                        viewModel.stopRecording()
                        Log.e("LiveTranslator", "Speech Intent failed: ${e.message}")
                    }
                },
                modifier = Modifier.size(116.dp).scale(if (isRecording) pulse else 1f),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = if (isRecording) Color.Red else PalashFlame)
            ) {
                Icon(Icons.Default.Mic, contentDescription = "Speak", modifier = Modifier.size(36.dp))
            }
        }
        Text(
            if (isRecording) "Listening... Speak now" else "Tap mic and speak in Hindi",
            Modifier.align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.bodyMedium
        )

        uiState.error?.let { 
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            )
        }
    }
}

private fun getAudioResource(context: Context, inputText: String): Int {
    val term = inputText.lowercase().trim()
    val name = when {
        term.contains("किताब") || term.contains("kitab") -> "puthi"
        term.contains("नमस्ते") || term.contains("namaste") -> "johar"
        term.contains("एक") || term.contains("ek") -> "mid"
        term.contains("दो") || term.contains("do") -> "bar"
        term.contains("बैठो") || term.contains("baitho") || term.contains("बैठo") -> "dul"
        term.contains("पानी") || term.contains("pani") -> "daag"
        term.contains("पढ़ो") || term.contains("padho") -> "padhaw"
        term.contains("लिखो") || term.contains("likho") -> "ol"
        else -> null
    } ?: return 0
    return try {
        context.resources.getIdentifier(name, "raw", context.packageName)
    } catch (e: Exception) {
        0
    }
}
