package com.palash.setu.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.palash.setu.data.dao.FLNDictionaryDao
import com.palash.setu.ui.theme.*
import com.palash.setu.util.*

private enum class VoiceModelState {
    Checking,
    Downloading,
    Ready,
    Unavailable,
    Error
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveTranslatorScreen(targetLanguage: String, dictionaryDao: FLNDictionaryDao) {
    val context = LocalContext.current
    val networkObserver = remember { NetworkObserver(context) }
    val networkStatus by networkObserver.observe.collectAsState(initial = NetworkObserver.Status.Unavailable)
    val isOnline = networkStatus == NetworkObserver.Status.Available

    val tts = remember { TranslationTTS(context) }
    val engine = remember(dictionaryDao) { MockAudioTranslatorEngine(dictionaryDao) }
    val viewModel = remember { TranslatorViewModel(engine) }
    val uiState by viewModel.uiState.collectAsState()

    var isPlaying by remember { mutableStateOf(false) }
    var currentMediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    val languages = listOf("Santhali (Ol Chiki)", "Ho (Warang Chiti)", "Mundari (Bani Hisir)")
    var selectedLangName by remember { mutableStateOf(languages.find { it.contains(targetLanguage, ignoreCase = true) } ?: languages.first()) }
    var langExpanded by remember { mutableStateOf(false) }
    val currentLangCode = when {
        selectedLangName.contains("Santhali") -> "Santhali"
        selectedLangName.contains("Ho") -> "Ho"
        else -> "Mundari"
    }

    var liveTranscript by remember { mutableStateOf("") }
    var typedHindi by remember { mutableStateOf("") }
    var isTranslationInputFocused by remember { mutableStateOf(false) }
    var isTranslating by remember { mutableStateOf(false) }
    var amplitude by remember { mutableFloatStateOf(0f) }
    var voiceModelState by remember { mutableStateOf(VoiceModelState.Checking) }
    var voiceStatusAcked by remember { mutableStateOf(false) }
    var downloadPercent by remember { mutableStateOf<Int?>(null) }
    val langCodeRef = rememberUpdatedState(currentLangCode)

    fun stopAllAudio() {
        tts.stop()
        currentMediaPlayer?.stop()
        currentMediaPlayer?.release()
        currentMediaPlayer = null
        isPlaying = false
    }

    val recognizer = remember {
        object : OnnxStt.Listener {
            override fun onAmplitude(value: Float) {
                amplitude = ((value + 60f) / 60f).coerceIn(0f, 1f)
            }

            override fun onResult(text: String) {
                amplitude = 0f
                isTranslating = false
                viewModel.onHindiVoiceInput(text, langCodeRef.value)
            }

            override fun onListeningEnd() {
                isTranslating = true
            }

            override fun onError(message: String) {
                amplitude = 0f
                isTranslating = false
                viewModel.setError(message)
            }
        }
    }

    fun startModelDownload() {
        voiceModelState = VoiceModelState.Downloading
        OnnxStt.downloadModel(
            context,
            onProgress = { downloadPercent = it },
            onSuccess = {
                downloadPercent = null
                voiceModelState = VoiceModelState.Ready
                voiceStatusAcked = true
                OnnxStt.prewarm(context) { message -> viewModel.setError(message) }
            },
            onError = { message ->
                downloadPercent = null
                voiceModelState = VoiceModelState.Error
                viewModel.setError(message)
            }
        )
    }

    LaunchedEffect(Unit) {
        if (OnnxStt.isModelInstalled(context)) {
            OnnxStt.prewarm(context) { message -> viewModel.setError(message) }
            voiceModelState = VoiceModelState.Ready
            voiceStatusAcked = true
        } else {
            voiceModelState = VoiceModelState.Unavailable
        }
    }

    fun startListening() {
        stopAllAudio()
        OnnxStt.start(context, recognizer)
        liveTranscript = ""
        amplitude = 0f
        viewModel.startRecording()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startListening()
        } else {
            viewModel.setError(
                "Microphone permission is needed for voice input. " +
                    "You can type the Hindi text below instead."
            )
        }
    }

    fun onMicTap() {
        if (voiceModelState != VoiceModelState.Ready && !uiState.isRecording) return
        if (uiState.isRecording) {
            OnnxStt.stopListening()
            amplitude = 0f
            return
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            startListening()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
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
                mp.setVolume(1.0f, 1.0f)
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

    LaunchedEffect(uiState.result) {
        uiState.result?.let { translation ->
            isTranslating = false
            val audioRes = getAudioResource(context, translation.sourceHindi)
            playAudio(audioRes, translation.devanagariText)
        }
    }

    LaunchedEffect(currentLangCode) {
        if (uiState.hindiInput.isNotBlank()) {
            viewModel.onHindiVoiceInput(uiState.hindiInput, currentLangCode)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            OnnxStt.cancel()
            OnnxStt.destroy()
            tts.shutdown()
            currentMediaPlayer?.release()
        }
    }

    val isRecording = uiState.isRecording
    val result = uiState.result
    val micEnabled = voiceModelState == VoiceModelState.Ready
    val showVoiceStatus =
        voiceModelState != VoiceModelState.Ready || !voiceStatusAcked
    val hideMicGroup = isTranslationInputFocused &&
        WindowInsets.ime.getBottom(LocalDensity.current) > 0

    val micContainer by animateColorAsState(
        targetValue = if (isRecording) PalashGreen else PalashFlame,
        label = "mic-container"
    )
    val micScale by animateFloatAsState(
        targetValue = 1f + (if (isRecording) amplitude * 0.08f else 0f),
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "mic-scale"
    )

    val statusText = when {
        isRecording && liveTranscript.isNotBlank() -> "Listening…"
        isRecording -> "Listening… speak in Hindi"
        isTranslating -> "Translating…"
        isPlaying -> "Speaking…"
        voiceModelState == VoiceModelState.Unavailable -> "Voice input isn't available on this device"
        result != null -> "Tap the mic to translate again"
        else -> "Tap the mic and speak in Hindi"
    }

    // Layout is split into three zones: a fixed top (header, language picker,
    // transcript card), a flexible middle that centers the mic, and a fixed
    // bottom (error/pack prompts + typed input). Only the middle zone shrinks
    // when the keyboard opens, so the input row below it always keeps its
    // full height and stays visible, and the mic (fixed 144.dp, CircleShape)
    // never gets squashed into an oval.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .navigationBarsPadding()
            .imePadding()
    ) {
        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Live Translator", style = MaterialTheme.typography.headlineSmall)
                Text(
                    "Hindi → $currentLangCode",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = if (isOnline) "● Online" else "● Offline",
                style = MaterialTheme.typography.labelMedium,
                color = if (isOnline) PalashGreen else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(20.dp))

        ExposedDropdownMenuBox(
            expanded = langExpanded,
            onExpandedChange = { langExpanded = !langExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedLangName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Target language") },
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

        Spacer(Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "You said",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                AnimatedContent(
                    targetState = if (isRecording) liveTranscript else uiState.hindiInput,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "transcript"
                ) { text ->
                    Text(
                        text = text.ifBlank { "Your words will appear here…" },
                        style = MaterialTheme.typography.headlineSmall,
                        color = if (text.isBlank()) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "Translation · $currentLangCode",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        AnimatedContent(
                            targetState = uiState.santhaliScript,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "translation"
                        ) { script ->
                            Text(
                                text = script.ifBlank { "Translation will appear here…" },
                                style = MaterialTheme.typography.titleLarge,
                                color = if (script.isBlank()) {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                        if (uiState.santhaliPhonetic.isNotBlank()) {
                            Text(
                                text = uiState.santhaliPhonetic,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    AnimatedVisibility(visible = result != null) {
                        IconButton(onClick = {
                            val audioRes = getAudioResource(context, uiState.hindiInput)
                            playAudio(audioRes, uiState.santhaliPhonetic)
                        }) {
                            Icon(
                                Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Replay translation",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // Flexible zone: takes whatever vertical space is left between the
        // card and the pinned bottom group, and centers the mic inside it.
        // This is the piece that shrinks when the keyboard opens, so the
        // fixed bottom group below never has to give up its own height.
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visible = !hideMicGroup,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { onMicTap() },
                        enabled = micEnabled,
                        modifier = Modifier.size(144.dp).scale(micScale),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = micContainer),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        AnimatedContent(
                            targetState = isRecording,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "mic-icon"
                        ) { recording ->
                            Icon(
                                imageVector = if (recording) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = if (recording) "Stop listening" else "Start speaking",
                                modifier = Modifier.size(56.dp)
                            )
                        }
                    }

                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                AnimatedVisibility(
                    visible = showVoiceStatus,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = when (voiceModelState) {
                                VoiceModelState.Checking -> "Checking offline voice model…"
                                VoiceModelState.Downloading -> downloadPercent?.let { "Downloading… $it%" }
                                    ?: "Downloading offline voice model…"
                                VoiceModelState.Ready -> "Ready"
                                VoiceModelState.Unavailable -> "Offline voice model not installed"
                                VoiceModelState.Error -> "Model download failed"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        if (voiceModelState == VoiceModelState.Unavailable || voiceModelState == VoiceModelState.Error) {
                            TextButton(onClick = { startModelDownload() }) {
                                Text(if (voiceModelState == VoiceModelState.Error) "Retry download" else "Download model")
                            }
                        }
                    }
                }
            }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AnimatedVisibility(
                visible = uiState.error != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                uiState.error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            OutlinedTextField(
                value = typedHindi,
                onValueChange = { typedHindi = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .onFocusChanged { isTranslationInputFocused = it.isFocused },
                label = { Text("Type in Hindi instead") },
                placeholder = { Text("e.g. किताब") },
                singleLine = true,
                trailingIcon = {
                    TextButton(
                        onClick = {
                            if (typedHindi.isNotBlank()) {
                                stopAllAudio()
                                viewModel.onHindiVoiceInput(typedHindi.trim(), currentLangCode)
                                typedHindi = ""
                            }
                        }
                    ) { Text("Translate") }
                }
            )

            Spacer(Modifier.height(16.dp))
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