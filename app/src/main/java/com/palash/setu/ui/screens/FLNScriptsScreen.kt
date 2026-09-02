package com.palash.setu.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private data class LessonStep(val hindi: String, val native: String, val pronunciation: String)

private val lessonData = mapOf(
    "Grade 1 Math - Counting 1-10" to listOf(
        LessonStep("बच्चों, अपनी गणित की किताब निकालिए।", "ᱜᱤᱫᱽᱨᱟᱹ, ᱟᱯᱮᱭᱟᱜ ᱞᱮᱠᱷᱟ ᱯᱩᱛᱷᱤ ᱩᱰᱩᱠ ᱯᱮ᱾", "गिद्रạ, आपेयाक् लेखा पुथी उडुक पे।"),
        LessonStep("मेरे बाद एक से दस तक दोहराएं।", "ᱤᱧ ᱛᱟᱭᱚᱢ ᱢᱤᱫ ᱠᱷᱚᱱ ᱜᱮᱞ ᱫᱷᱟᱹᱵᱤᱡ ᱞᱟᱹᱭ ᱯᱮ᱾", "इञ् तायोम मिद खोन गेल धा़बिज ला़य पे।"),
        LessonStep("कंकड़ों का उपयोग करके गिनें।", "ᱜᱤᱴᱤᱞ ᱵᱮᱵᱷᱟᱨ ᱠᱟᱛᱮ ᱞᱮᱠᱷᱟᱭ ᱯᱮ᱾", "गिटिल बेव्हार काते लेखाय पे।")
    ),
    "Grade 1 Phonics - Greetings" to listOf(
        LessonStep("नमस्ते बच्चों, आप कैसे हैं?", "ᱡᱚᱦᱟᱨ ᱜᱤᱫᱽᱨᱟᱹ, ᱪᱮᱫ ᱞᱮᱠᱟ ᱢᱮᱱᱟᱜ ᱯᱮᱭᱟ?", "जोहार गिद्रạ, चेद लेका मेनाक पेया?"),
        LessonStep("अपना नाम बताएं।", "ᱟᱯᱮᱭᱟᱜ ᱧᱩᱛᱩᱢ ᱞᱟᱹᱭ ᱯᱮ᱾", "आपेयाक् ञुतुम ला़य पे।"),
        LessonStep("आज हम 'अ' ध्वनि सीखेंगे।", "ᱛᱮᱦᱮᱧ ᱵᱚᱱ 'ᱚ' ᱥᱟᱰᱮ ᱵᱚᱱ ᱪᱮᱫᱚᱜ-ᱟ᱾", "तेहेञ बोन 'ओ' साडे बोन चेदोक्-आ।")
    ),
    "Grade 2 Shapes" to listOf(
        LessonStep("इस गोल आकृति को देखें।", "ᱱᱚᱣᱟ ᱜᱩᱞᱟᱹᱭ ᱪᱤᱛᱟᱹᱨ ᱧᱮᱞ ᱯᱮ᱾", "नोवा गुला़य चितạर ञेल पे।"),
        LessonStep("इसे 'वृत्त' कहते हैं।", "ᱱᱚᱣᱟ ᱫᱚ 'ᱜᱩᱞᱟᱹᱭ' ᱵᱚᱱ ᱢᱮᱛᱟᱜ-ᱟ᱾", "नोवा दो 'गुला़य' बोन मेताक्-आ।"),
        LessonStep("अपने आसपास गोल चीजें ढूंढें।", "ᱟᱯᱮ ᱟᱰᱮ ᱯᱟᱥᱮ ᱜᱩᱞᱟᱹᱭ ᱡᱤᱱᱤᱥ ᱯᱟᱱᱛᱮ ᱯᱮ᱾", "आपे आडे पासे गुला़य जिनिस पानते पे।")
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FLNScriptsScreen(targetLanguage: String) {
    val topics = lessonData.keys.toList()
    var selectedTopic by remember { mutableStateOf(topics.first()) }
    var expanded by remember { mutableStateOf(false) }
    val steps = lessonData[selectedTopic] ?: emptyList()

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("FLN Activity Script", style = MaterialTheme.typography.headlineMedium)
        Text("Hindi to $targetLanguage", color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(bottom = 16.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
        ) {
            OutlinedTextField(
                value = selectedTopic,
                onValueChange = {},
                readOnly = true,
                label = { Text("Select Topic") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                topics.forEach { topic ->
                    DropdownMenuItem(
                        text = { Text(topic) },
                        onClick = {
                            selectedTopic = topic
                            expanded = false
                        }
                    )
                }
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(steps) { step ->
                Card(
                    Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(step.hindi, style = MaterialTheme.typography.titleLarge)
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                        Text(step.native, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                        Text(step.pronunciation, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }
    }
}
