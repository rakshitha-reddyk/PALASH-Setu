package com.palash.setu.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.palash.setu.data.dao.GeneratedWorksheetDao
import com.palash.setu.data.entity.GeneratedWorksheet
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class PDFWorksheetGenerator(
    private val context: Context,
    private val worksheetDao: GeneratedWorksheetDao
) {
    suspend fun generate(
        userId: String,
        targetLanguage: String,
        template: WorksheetTemplateConfig
    ): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        
        drawHeader(page.canvas, template)
        drawContent(page.canvas, targetLanguage, template)
        
        document.finishPage(page)

        val directory = File(context.cacheDir, "worksheets").apply { mkdirs() }
        val file = File(directory, "PALASH_${template.id}_${UUID.randomUUID().toString().take(6)}.pdf")
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()

        worksheetDao.insert(
            GeneratedWorksheet(
                id = UUID.randomUUID().toString(),
                userId = userId,
                title = template.title,
                targetLanguage = targetLanguage,
                nipunCompetencyCode = template.nipunTag,
                pdfLocalFilePath = file.absolutePath
            )
        )
        return file
    }

    private fun drawHeader(canvas: Canvas, config: WorksheetTemplateConfig) {
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { 
            color = Color.rgb(0, 102, 204)
            textSize = 24f
            typeface = Typeface.DEFAULT_BOLD 
        }
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { 
            color = Color.DKGRAY
            textSize = 14f 
        }
        val tagPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { 
            color = Color.rgb(0, 135, 90)
            textSize = 12f
            typeface = Typeface.MONOSPACE
        }

        canvas.drawText("PALASH MTB-MLE Bilingual Worksheet", 50f, 50f, titlePaint)
        canvas.drawText("School: __________________________  Date: __________", 50f, 80f, subPaint)
        canvas.drawText("Competency: ${config.title} [${config.nipunTag}]", 50f, 105f, tagPaint)
        canvas.drawLine(50f, 120f, PAGE_WIDTH - 50f, 120f, Paint().apply { strokeWidth = 1f; color = Color.LTGRAY })
    }

    private fun drawContent(canvas: Canvas, language: String, config: WorksheetTemplateConfig) {
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 18f; color = Color.BLACK }
        val smallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 14f; color = Color.GRAY }
        val nativePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 22f; color = Color.rgb(0, 70, 120); typeface = Typeface.DEFAULT_BOLD }
        val shapePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(217, 56, 30); style = Paint.Style.STROKE; strokeWidth = 2f }
        val dashPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(150, 150, 150)
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
            pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
        }
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(255, 235, 230); style = Paint.Style.FILL }

        when (config.id) {
            "counting" -> {
                val items = listOf("एक", "दो", "तीन", "चार", "पाँच")
                val native = listOf("᱑ (Mid)", "᱒ (Bar)", "３ (Pe)", "４ (Pun)", "᱕ (Mone)")
                for (i in 0 until 5) {
                    val top = 160f + (i * 125f)
                    canvas.drawRect(60f, top, 180f, top + 90f, fillPaint)
                    canvas.drawRect(60f, top, 180f, top + 90f, shapePaint)
                    
                    for (c in 0..i) {
                        canvas.drawCircle(85f + (c % 3 * 25f), top + 30f + (c / 3 * 30f), 8f, Paint().apply { color = Color.RED; style = Paint.Style.FILL })
                    }

                    canvas.drawText("${items[i]} / ${native[i]}", 210f, top + 35f, bodyPaint)
                    canvas.drawText("Count: ________________", 210f, top + 75f, bodyPaint)
                    canvas.drawLine(50f, top + 105f, PAGE_WIDTH - 50f, top + 105f, Paint().apply { color = Color.LTGRAY; strokeWidth = 0.5f })
                }
            }
            "fruits" -> {
                val hindi = listOf("सेब", "केला", "आम", "अमरूद")
                val native = listOf("ᱥᱮᱣ", "ᱠᱟᱭᱨᱟ", "ᱩᱞ", "ᱛᱟᱢᱨᱚᱥ")
                val phonetic = listOf("Sew", "Kaira", "Ul", "Tamros")
                
                for (i in 0 until 4) {
                    val top = 160f + (i * 155f)
                    
                    // Row Box
                    canvas.drawRect(50f, top, PAGE_WIDTH - 50f, top + 130f, dashPaint)
                    
                    // Hindi Term
                    canvas.drawText("Hindi:", 70f, top + 40f, smallPaint)
                    canvas.drawText(hindi[i], 70f, top + 75f, bodyPaint.apply { textSize = 24f })
                    
                    // Native Script
                    canvas.drawText("Santhali ($language):", 250f, top + 40f, smallPaint)
                    canvas.drawText(native[i], 250f, top + 80f, nativePaint.apply { textSize = 32f })
                    canvas.drawText("(${phonetic[i]})", 250f, top + 110f, smallPaint)
                    
                    // Activity
                    canvas.drawText("Activity: Trace the word below", 70f, top + 115f, smallPaint)
                    canvas.drawLine(70f, top + 125f, 200f, top + 125f, dashPaint)
                }
            }
            "tracing" -> {
                val letters = listOf("ᱚ", "ᱛ", "ᱜ", "ᱝ", "ᱞ", "ᱟ")
                for (i in 0 until letters.size) {
                    val top = 160f + (i * 105f)
                    canvas.drawText(letters[i], 70f, top + 55f, nativePaint.apply { textSize = 45f })
                    canvas.drawText("Trace -->  . . . . . . . . . . . . . .", 160f, top + 45f, Paint().apply { color = Color.LTGRAY; textSize = 25f })
                    canvas.drawLine(160f, top + 65f, PAGE_WIDTH - 60f, top + 65f, dashPaint)
                }
            }
            "actions" -> {
                val actions = listOf(
                    "बैठो" to "ᱫᱩᱲᱩᱵ (Durub)",
                    "किताब खोलो" to "ᱯᱩᱛᱷᱤ ᱡᱷᱤᱡᱽ ᱢᱮ (Puthi jhij me)",
                    "सुनो" to "ᱟᱸᱡᱚᱢ ᱢᱮ (Anjom me)",
                    "लिखो" to "ᱚᱞ ᱢᱮ (Ol me)"
                )
                
                for (i in 0 until 4) {
                    val row = i / 2
                    val col = i % 2
                    val left = 50f + (col * 255f)
                    val top = 160f + (row * 300f)
                    val cardWidth = 240f
                    val cardHeight = 280f
                    
                    // Flashcard Box with Dash border for cutting
                    canvas.drawRect(left, top, left + cardWidth, top + cardHeight, dashPaint)
                    
                    // Content
                    canvas.drawText("Hindi:", left + 20f, top + 40f, smallPaint)
                    canvas.drawText(actions[i].first, left + 20f, top + 80f, bodyPaint.apply { textSize = 22f; typeface = Typeface.DEFAULT_BOLD })
                    
                    canvas.drawLine(left + 20f, top + 110f, left + cardWidth - 20f, top + 110f, Paint().apply { color = Color.LTGRAY })
                    
                    canvas.drawText("Santhali:", left + 20f, top + 150f, smallPaint)
                    val scriptPart = actions[i].second.substringBefore(" (")
                    val phoneticPart = "(${actions[i].second.substringAfter(" (")}"
                    
                    canvas.drawText(scriptPart, left + 20f, top + 200f, nativePaint.apply { textSize = 28f })
                    canvas.drawText(phoneticPart, left + 20f, top + 240f, bodyPaint.apply { textSize = 16f; color = Color.DKGRAY })
                }
            }
        }
    }

    companion object {
        private const val PAGE_WIDTH = 595
        private const val PAGE_HEIGHT = 842
    }
}

data class WorksheetTemplateConfig(
    val id: String,
    val title: String,
    val nipunTag: String,
    val description: String
)
