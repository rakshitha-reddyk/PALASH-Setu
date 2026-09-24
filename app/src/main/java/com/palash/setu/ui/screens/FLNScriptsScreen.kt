package com.palash.setu.ui.screens

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.palash.setu.ui.theme.*
import java.util.Locale

data class ScriptCardData(
    val character: String,
    val name: String,
    val phonetic: String,
    val audioResName: String
)

data class LessonPrompt(
    val title: String,
    val hindi: String,
    val native: String,
    val pronunciation: String,
    val activity: String,
    val audioResName: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FLNScriptsScreen(targetLanguage: String = "Santhali") {
    val context = LocalContext.current
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var currentMediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(Unit) {
        val tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsEngine?.language = Locale("hi", "IN")
            }
        }
        ttsEngine = tts
        onDispose {
            currentMediaPlayer?.stop()
            currentMediaPlayer?.release()
            tts.stop()
            tts.shutdown()
        }
    }

    fun playSound(audioResName: String, phoneticText: String) {
        try {
            currentMediaPlayer?.stop()
            currentMediaPlayer?.release()
            currentMediaPlayer = null
        } catch (e: Exception) {
            Log.e("FLNScripts", "Error resetting MediaPlayer: ${e.message}")
        }

        val resId = context.resources.getIdentifier(audioResName, "raw", context.packageName)
        if (resId != 0) {
            try {
                val mp = MediaPlayer.create(context, resId)
                if (mp != null) {
                    mp.setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    mp.setVolume(1.0f, 1.0f)
                    currentMediaPlayer = mp
                    mp.setOnCompletionListener {
                        it.release()
                        if (currentMediaPlayer == it) currentMediaPlayer = null
                    }
                    mp.start()
                    return
                }
            } catch (e: Exception) {
                Log.e("FLNScripts", "Raw Audio Playback Error: ${e.message}")
            }
        }
        ttsEngine?.speak(phoneticText, TextToSpeech.QUEUE_FLUSH, null, "FLNSoundBoardTTS")
    }

    val lessonPrompts = listOf(
        LessonPrompt(
            title = "Cultural Story: Baha Festival",
            hindi = "नमस्ते बच्चों! चलिए आज 'Baha' (फूल) उत्सव के बारे में बात करते हैं। यह वसंत ऋतु का स्वागत है।",
            native = "ᱡᱚᱦᱟᱨ ᱜᱤᱫᱽᱨᱟᱹ! ᱫᱮᱞᱟ ᱛᱮᱦᱮᱧ ᱵᱟᱦᱟ ᱯᱚᱨᱚᱵᱽ ᱵᱟᱵᱚᱛ ᱵᱚᱱ ᱜᱟᱞᱢᱟᱨᱟᱣ-ᱟ᱾",
            pronunciation = "जोहार गिद्रạ! देला तेहेञ बाहा परोब बाबत बोन गालमाराव-आ।",
            activity = "Activity: Draw a Sal flower (Sarjom) on your slate and count its petals.",
            audioResName = "baha_story"
        ),
        LessonPrompt(
            title = "Numeracy Activity: Tumdak Beats",
            hindi = "अपने 'Tumdak' (ढोल) को बजाएं और जितनी बार आवाज आए, उतनी बार गिनती करें।",
            native = "ᱟᱯᱮᱭᱟᱜ ᱛᱩᱢᱫᱟᱜ ᱨᱩᱭ ᱯᱮ ᱟᱨ ᱞᱮᱠᱷᱟᱭ ᱯᱮ᱾",
            pronunciation = "आपेयाक् तुमदाक रुय पे आर लेखाय पे।",
            activity = "Activity: Group repetition. One student beats the drum, others shout the numbers (Mid, Bar, Pe...).",
            audioResName = "tumdak_activity"
        ),
        LessonPrompt(
            title = "Environment: Forest Trees",
            hindi = "जंगल के साल (Sarejom) के पेड़ों को गिनें। हमारे जंगल हमारी जान हैं।",
            native = "ᱵᱤᱨ ᱨᱮᱱᱟᱜ ᱥᱟᱨᱡᱚᱢ ᱫᱟᱨᱮ ᱞᱮᱠᱷᱟᱭ ᱯᱮ᱾",
            pronunciation = "बीर रेनाक सारजोम दारे लेखाय पे।",
            activity = "Activity: Matching game. Match character cards with pictures of forest items.",
            audioResName = "forest_env"
        )
    )

    val fullAlphabets = listOf(
        ScriptCardData("ᱚ", "LA", "La", "ol_a"), ScriptCardData("ᱛ", "AT", "At", "ol_at"),
        ScriptCardData("ᱜ", "AG", "Ag", "ol_ag"), ScriptCardData("ᱝ", "ANG", "Ang", "ol_ang"),
        ScriptCardData("ᱞ", "AL", "Al", "ol_al"), ScriptCardData("ᱟ", "LAA", "Laa", "ol_laa"),
        ScriptCardData("ᱠ", "AAK", "Aak", "ol_aak"), ScriptCardData("ᱡ", "AJ", "Aj", "ol_aj"),
        ScriptCardData("ᱢ", "AM", "Am", "ol_am"), ScriptCardData("ᱣ", "AW", "Aw", "ol_aw"),
        ScriptCardData("ᱤ", "LI", "Li", "ol_li"), ScriptCardData("ᱥ", "IS", "Is", "ol_is"),
        ScriptCardData("ᱦ", "IH", "Ih", "ol_ih"), ScriptCardData("ᱧ", "INY", "Iny", "ol_iny"),
        ScriptCardData("ᱨ", "IR", "Ir", "ol_ir"), ScriptCardData("ᱩ", "LU", "Lu", "ol_lu"),
        ScriptCardData("ᱪ", "UC", "Uc", "ol_uc"), ScriptCardData("ᱫ", "UD", "Ud", "ol_ud"),
        ScriptCardData("ᱬ", "UNN", "Unn", "ol_unn"), ScriptCardData("ᱭ", "UY", "Uy", "ol_uy"),
        ScriptCardData("ᱮ", "LE", "Le", "ol_le"), ScriptCardData("ᱯ", "EP", "Ep", "ol_ep"),
        ScriptCardData("ᱴ", "EDD", "Edd", "ol_edd"), ScriptCardData("ᱱ", "EN", "En", "ol_en"),
        ScriptCardData("ᱲ", "ERR", "Err", "ol_err"), ScriptCardData("ᱳ", "LO", "Lo", "ol_lo"),
        ScriptCardData("ᱴ", "OTT", "Ott", "ol_ott"), ScriptCardData("ᱵ", "OB", "Ob", "ol_ob"),
        ScriptCardData("ᱶ", "OV", "Ov", "ol_ov"), ScriptCardData("ᱷ", "OH", "Oh", "ol_oh")
    )

    val numerals = listOf(
        ScriptCardData("᱑", "Mid", "1", "mid"), ScriptCardData("᱒", "Bar", "2", "bar"),
        ScriptCardData("᱓", "Pe", "3", "pe"), ScriptCardData("４", "Pone", "4", "pone"),
        ScriptCardData("᱕", "Mone", "5", "mone"), ScriptCardData("６", "Turui", "6", "turui"),
        ScriptCardData("７", "Eaye", "7", "eaye"), ScriptCardData("８", "Iral", "8", "iral"),
        ScriptCardData("９", "Arey", "9", "arey"), ScriptCardData("᱑᱐", "Gel", "10", "gel")
    )

    var alphabetsExpanded by remember { mutableStateOf(true) }
    var numeralsExpanded by remember { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxSize().background(PalashBackground)) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.White,
            contentColor = PalashBlue
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = { Text("Lesson Scripts & Activities", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = { Text("Phonics & Sound Board", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (selectedTabIndex == 0) {
                Text("Bilingual FLN Lesson Guide", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                
                lessonPrompts.forEach { prompt ->
                    LessonPromptCard(prompt) { playSound(prompt.audioResName, prompt.pronunciation) }
                }
            } else {
                Text("Interactive Phonics Tools", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

                ExpandableHeader(
                    title = "Alphabets ( 30 Ol Chiki Letters )",
                    isExpanded = alphabetsExpanded,
                    onToggle = { alphabetsExpanded = !alphabetsExpanded }
                )
                AnimatedVisibility(visible = alphabetsExpanded) {
                    ScriptCardsGrid(fullAlphabets) { item -> playSound(item.audioResName, item.phonetic) }
                }

                ExpandableHeader(
                    title = "Numerals ( Santhali Numbers 1 to 10 )",
                    isExpanded = numeralsExpanded,
                    onToggle = { numeralsExpanded = !numeralsExpanded }
                )
                AnimatedVisibility(visible = numeralsExpanded) {
                    ScriptCardsGrid(numerals) { item -> playSound(item.audioResName, item.name) }
                }
            }
        }
    }
}

@Composable
private fun LessonPromptCard(prompt: LessonPrompt, onPlay: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(prompt.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = PalashBlue)
                IconButton(onClick = onPlay) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Listen", tint = PalashGreen)
                }
            }
            Text(prompt.hindi, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            Text(prompt.native, style = MaterialTheme.typography.headlineSmall, color = PalashBlue)
            Text(prompt.pronunciation, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            Spacer(Modifier.height(4.dp))
            Surface(
                color = PalashMist,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(prompt.activity, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ExpandableHeader(title: String, isExpanded: Boolean, onToggle: () -> Unit) {
    val rotation by animateFloatAsState(if (isExpanded) 180f else 0f, label = "rotate")
    Card(
        onClick = onToggle,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Icon(Icons.Default.ExpandMore, contentDescription = null, modifier = Modifier.rotate(rotation))
        }
    }
}

@Composable
private fun ScriptCardsGrid(items: List<ScriptCardData>, onItemClick: (ScriptCardData) -> Unit) {
    val rows = items.chunked(3)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { rowItems ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowItems.forEach { item ->
                    Box(Modifier.weight(1f)) {
                        ScriptTile(item, onClick = { onItemClick(item) })
                    }
                }
                if (rowItems.size < 3) repeat(3 - rowItems.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun ScriptTile(item: ScriptCardData, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().height(95.dp)
    ) {
        Column(Modifier.fillMaxSize().padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(item.character, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = PalashBlue)
            Text("${item.name} (${item.phonetic})", fontSize = 10.sp, color = Color.DarkGray, textAlign = TextAlign.Center)
            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = PalashGreen, modifier = Modifier.size(12.dp))
        }
    }
}
