package com.palash.setu.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.palash.setu.util.PDFWorksheetGenerator
import com.palash.setu.util.WorksheetTemplateConfig
import com.palash.setu.data.dao.GeneratedWorksheetDao
import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class WorksheetViewModel : ViewModel() {
    var generatingWorksheetId by mutableStateOf<String?>(null)
        private set
    
    var previewBitmap by mutableStateOf<Bitmap?>(null)
    var previewPdfFile by mutableStateOf<File?>(null)
    
    fun generateWorksheet(
        context: Context,
        userId: String,
        targetLanguage: String,
        worksheetDao: GeneratedWorksheetDao,
        template: WorksheetTemplateConfig,
        onComplete: (String) -> Unit
    ) {
        generatingWorksheetId = template.id
        
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val generator = PDFWorksheetGenerator(context, worksheetDao)
                val pdfFile = generator.generate(userId, targetLanguage, template)
                
                val bitmap = renderPdfToBitmap(pdfFile)
                
                withContext(Dispatchers.Main) {
                    previewPdfFile = pdfFile
                    previewBitmap = bitmap
                    generatingWorksheetId = null
                    onComplete("Bilingual Worksheet saved to Cache")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    generatingWorksheetId = null
                    onComplete("Error: ${e.message}")
                }
            }
        }
    }

    private fun renderPdfToBitmap(file: File): Bitmap? {
        return try {
            val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(fileDescriptor)
            val page = renderer.openPage(0)
            
            // High resolution for clear text
            val bitmap = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            
            page.close()
            renderer.close()
            fileDescriptor.close()
            bitmap
        } catch (e: Exception) {
            null
        }
    }
    
    fun closePreview() {
        previewBitmap = null
        previewPdfFile = null
    }
}
