package com.palash.setu.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.palash.setu.data.dao.GeneratedWorksheetDao
import com.palash.setu.ui.theme.PalashGreen
import com.palash.setu.util.*
import kotlinx.coroutines.launch
import java.io.File
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider

import androidx.compose.material3.ExperimentalMaterial3Api
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorksheetScreen(userId: String, targetLanguage: String, worksheetDao: GeneratedWorksheetDao) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val competencies = listOf("Grade 1 Math - Counting 1-10", "Grade 1 Phonics", "Grade 2 Shapes")
    var competency by remember { mutableStateOf(competencies.first()) }
    var template by remember { mutableStateOf(WorksheetTemplate.WORKSHEET) }
    var competencyExpanded by remember { mutableStateOf(false) }
    var templateExpanded by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<String?>(null) }
    var generatedFile by remember { mutableStateOf<File?>(null) }

    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Bilingual Worksheets", style = MaterialTheme.typography.headlineMedium)
        Text("Create a printable FLN activity in Hindi and $targetLanguage.")
        ExposedDropdownMenuBox(competencyExpanded, { competencyExpanded = !competencyExpanded }) {
            OutlinedTextField(competency, {}, Modifier.fillMaxWidth().menuAnchor(), readOnly = true, label = { Text("NIPUN competency") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(competencyExpanded) })
            ExposedDropdownMenu(competencyExpanded, { competencyExpanded = false }) { competencies.forEach { item -> DropdownMenuItem({ Text(item) }, { competency = item; competencyExpanded = false }) } }
        }
        ExposedDropdownMenuBox(templateExpanded, { templateExpanded = !templateExpanded }) {
            OutlinedTextField(template.label, {}, Modifier.fillMaxWidth().menuAnchor(), readOnly = true, label = { Text("Output format") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(templateExpanded) })
            ExposedDropdownMenu(templateExpanded, { templateExpanded = false }) { WorksheetTemplate.entries.forEach { item -> DropdownMenuItem({ Text(item.label) }, { template = item; templateExpanded = false }) } }
        }
        Button(onClick = {
            status = "Generating locally..."
            scope.launch {
                val file = PDFWorksheetGenerator(context, worksheetDao).generate(userId, targetLanguage, competency, template)
                generatedFile = file
                status = "PDF saved offline"
            }
        }, modifier = Modifier.fillMaxWidth()) { Text("Generate Bilingual PDF") }
        
        status?.let { 
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(it, color = PalashGreen)
                if (it == "PDF saved offline" && generatedFile != null) {
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = { openPdf(context, generatedFile!!) }) {
                        Text("Open PDF")
                    }
                }
            }
        }
    }
}

private fun openPdf(context: Context, file: File) {
    try {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Open PDF"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
