package com.palash.setu.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.palash.setu.data.dao.GeneratedWorksheetDao
import com.palash.setu.ui.theme.PalashBlue
import com.palash.setu.ui.theme.PalashGreen
import com.palash.setu.ui.theme.PalashMist
import com.palash.setu.util.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorksheetScreen(userId: String, targetLanguage: String, worksheetDao: GeneratedWorksheetDao) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    val viewModel: WorksheetViewModel = viewModel()

    val templates = listOf(
        WorksheetTemplateConfig("counting", "Animal Counting (1-10)", "FLN Math L1", "Grade 1 Foundational Numeracy counting objects."),
        WorksheetTemplateConfig("fruits", "Local Fruits & Words", "FLN Lang L2", "Vocabulary building with local Santhali fruits."),
        WorksheetTemplateConfig("tracing", "Ol Chiki Tracing", "FLN Lang L1", "Letter recognition and tracing for Ol Chiki script."),
        WorksheetTemplateConfig("actions", "Classroom Actions", "FLN Soc L2", "Bilingual flashcards for classroom dialogue.")
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Bilingual Worksheet Hub", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                "Select a Worksheet Template",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 12.dp)
            )
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(templates) { template ->
                    val isCurrentGenerating = viewModel.generatingWorksheetId == template.id
                    
                    TemplateCard(template, isCurrentGenerating) {
                        viewModel.generateWorksheet(
                            context = context,
                            userId = userId,
                            targetLanguage = targetLanguage,
                            worksheetDao = worksheetDao,
                            template = template,
                            onComplete = { message ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(message)
                                }
                            }
                        )
                    }
                }
            }
        }

        // Isolated Preview Dialog Trigger
        viewModel.previewPdfFile?.let { pdfFile ->
            val bitmap = viewModel.previewBitmap
            if (bitmap != null) {
                Dialog(
                    onDismissRequest = { viewModel.closePreview() },
                    properties = DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            TopAppBar(
                                title = { Text("Worksheet Preview", style = MaterialTheme.typography.titleMedium) },
                                navigationIcon = {
                                    IconButton(onClick = { viewModel.closePreview() }) {
                                        Icon(Icons.Default.Close, contentDescription = "Close")
                                    }
                                },
                                actions = {
                                    IconButton(onClick = { /* Print logic would go here */ }) {
                                        Icon(Icons.Default.Print, contentDescription = "Print")
                                    }
                                }
                            )
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .background(Color.LightGray)
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .verticalScroll(rememberScrollState())
                                        .background(Color.White)
                                        .padding(8.dp)
                                ) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "PDF Preview",
                                        modifier = Modifier.fillMaxWidth(),
                                        contentScale = ContentScale.FillWidth
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TemplateCard(config: WorksheetTemplateConfig, isBusy: Boolean, onGenerate: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .padding(bottom = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = config.iconVector,
                    contentDescription = null,
                    tint = PalashBlue,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Text(
                config.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Surface(
                color = PalashMist,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(
                    config.nipunTag,
                    style = MaterialTheme.typography.labelSmall,
                    color = PalashGreen,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    fontWeight = FontWeight.Bold
                )
            }
            
            Text(
                config.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                minLines = 2,
                lineHeight = 14.sp,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Button(
                onClick = onGenerate,
                enabled = !isBusy,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(vertical = 0.dp)
            ) {
                if (isBusy) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                } else {
                    Text("Generate PDF", fontSize = 12.sp)
                }
            }
        }
    }
}

// Extension to config to hold the icon vector for UI usage
private val WorksheetTemplateConfig.iconVector: ImageVector
    get() = when (this.id) {
        "counting" -> Icons.Default.Numbers
        "fruits" -> Icons.AutoMirrored.Filled.MenuBook
        "tracing" -> Icons.Default.Description
        else -> Icons.Default.RecordVoiceOver
    }
