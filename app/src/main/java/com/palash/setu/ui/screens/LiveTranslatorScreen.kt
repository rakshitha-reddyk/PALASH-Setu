package com.palash.setu.ui.screens

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
import androidx.compose.ui.unit.dp
import com.palash.setu.data.dao.FLNDictionaryDao
import com.palash.setu.ui.theme.*
import com.palash.setu.util.*
import kotlinx.coroutines.launch

@Composable
fun LiveTranslatorScreen(targetLanguage: String, dictionaryDao: FLNDictionaryDao) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val networkObserver = remember { NetworkObserver(context) }
    val networkStatus by networkObserver.observe.collectAsState(initial = NetworkObserver.Status.Unavailable)
    val isOnline = networkStatus == NetworkObserver.Status.Available

    val engine = remember(dictionaryDao) { MockAudioTranslatorEngine(dictionaryDao) }
    var isRecording by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<TranslationResult?>(null) }
    val pulse by rememberInfiniteTransition(label = "mic-pulse").animateFloat(
        initialValue = 1f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(650), RepeatMode.Reverse), label = "pulse"
    )

    Column(Modifier.fillMaxSize().background(PalashBackground).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Live Translator", style = MaterialTheme.typography.headlineMedium)
                Text("Hindi  ->  $targetLanguage", color = PalashBlue, style = MaterialTheme.typography.labelLarge)
            }
            AssistChip(
                onClick = {}, 
                label = { Text(if (isOnline) "Online" else "Offline") }, 
                leadingIcon = { Text("●", color = if (isOnline) PalashGreen else Color.Gray) }
            )
        }
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Students' voice", style = MaterialTheme.typography.titleLarge)
                Text(result?.nativeText ?: "Your translated words will appear here", style = MaterialTheme.typography.headlineMedium)
                HorizontalDivider()
                Text("Teacher pronunciation", style = MaterialTheme.typography.labelLarge, color = PalashBlue)
                Text(result?.devanagariText ?: "Tap the mic and say a command", style = MaterialTheme.typography.bodyLarge)
            }
        }
        Spacer(Modifier.weight(1f))
        if (isPlaying) {
            LinearProgressIndicator(Modifier.fillMaxWidth(), color = PalashGreen)
            Text("Playing translated audio  ·  under 1.8 seconds", color = PalashGreen)
        }
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Button(
                onClick = {
                    if (isRecording) return@Button
                    isRecording = true
                    scope.launch {
                        result = engine.translate("बैठो", targetLanguage)
                        isRecording = false
                        isPlaying = true
                        kotlinx.coroutines.delay(1200)
                        isPlaying = false
                    }
                },
                modifier = Modifier.size(116.dp).scale(if (isRecording) pulse else 1f),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = PalashFlame)
            ) { Icon(Icons.Default.Mic, contentDescription = "Speak") }
        }
        Text(if (isRecording) "Listening..." else "Tap and speak in Hindi", Modifier.align(Alignment.CenterHorizontally), style = MaterialTheme.typography.bodyMedium)
    }
}
