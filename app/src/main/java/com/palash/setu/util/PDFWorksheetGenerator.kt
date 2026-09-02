package com.palash.setu.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
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
        competency: String,
        template: WorksheetTemplate
    ): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        drawPage(page.canvas, targetLanguage, competency, template)
        document.finishPage(page)

        val directory = File(context.filesDir, "worksheets").apply { mkdirs() }
        val file = File(directory, "worksheet_${UUID.randomUUID()}.pdf")
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()

        worksheetDao.insert(
            GeneratedWorksheet(
                id = UUID.randomUUID().toString(),
                userId = userId,
                title = "$competency - ${template.label}",
                targetLanguage = targetLanguage,
                nipunCompetencyCode = competency,
                pdfLocalFilePath = file.absolutePath
            )
        )
        return file
    }

    private fun drawPage(canvas: Canvas, language: String, competency: String, template: WorksheetTemplate) {
        val heading = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(0, 70, 120); textSize = 30f; typeface = Typeface.DEFAULT_BOLD }
        val body = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.DKGRAY; textSize = 20f }
        val line = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 2f }
        
        val scriptPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.BLACK; textSize = 26f; typeface = Typeface.DEFAULT_BOLD }
        val numberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(0, 100, 0); textSize = 44f; typeface = Typeface.DEFAULT_BOLD }
        val shapePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(255, 87, 34); style = Paint.Style.FILL }

        canvas.drawText("PALASH-Setu", 60f, 70f, heading)
        canvas.drawText(competency, 60f, 115f, body)
        canvas.drawText("Hindi  |  $language", 60f, 150f, body)
        canvas.drawLine(60f, 175f, PAGE_WIDTH - 60f, 175f, line)

        if (template == WorksheetTemplate.WORKSHEET) {
            val counts = listOf(3, 2, 5, 1, 4)
            val olChiki = listOf("ᱯᱮ", "ᱵᱟᱨ", "ᱢᱚᱬᱮ", "ᱢᱤᱫ", "ᱯᱩᱱ")
            val hindiPrompt = "गिनें:"
            val nativePrompt = "ᱞᱮᱠᱷᱟᱭ ᱯᱮ:"
            
            for (index in 0 until 5) {
                val count = counts[index]
                val top = 215f + index * 120f
                val boxWidth = 180f
                val boxHeight = 90f
                
                // Draw Question Box
                canvas.drawRect(60f, top, 60f + boxWidth, top + boxHeight, line)
                
                // Draw Visual Items in Box (Circles or Squares)
                val itemSize = 10f
                val spacing = 30f
                for (s in 0 until count) {
                    val sCol = s % 5
                    val sRow = s / 5
                    val x = 90f + sCol * spacing
                    val y = top + 30f + sRow * spacing
                    if (index % 2 == 0) {
                        canvas.drawCircle(x, y, itemSize, shapePaint)
                    } else {
                        canvas.drawRect(x - itemSize, y - itemSize, x + itemSize, y + itemSize, shapePaint)
                    }
                }

                // Render Hindi & Native Prompts
                canvas.drawText("$hindiPrompt $nativePrompt", 260f, top + 35f, body)
                
                // Draw Blank Fill-in Line and Answer Hint
                canvas.drawText("_______ ( ${olChiki[index]} / $count )", 260f, top + 75f, body)
                
                // Decorative row separator
                canvas.drawLine(60f, top + boxHeight + 15f, PAGE_WIDTH - 60f, top + boxHeight + 15f, line)
            }
        } else {
            val olChikiNumbers = listOf("ᱢᱤᱫ", "ᱵᱟᱨ", "ᱯᱮ", "ᱯᱩᱱ", "ᱢᱚᱬᱮ", "ᱛᱩᱨᱩᱭ")
            val hindiTrans = listOf("मिड", "बार", "पे", "पुन", "मोणे", "तुरुय")
            
            for (index in 0 until 6) {
                val column = index % 2
                val row = index / 2
                val left = 60f + column * 270f
                val top = 215f + row * 190f
                
                // Draw Card Box
                canvas.drawRect(left, top, left + 250f, top + 160f, line)
                
                // Render Number (1-6)
                canvas.drawText("${index + 1}", left + 20f, top + 55f, numberPaint)
                
                // Render Native Script (Ol Chiki)
                canvas.drawText(olChikiNumbers[index], left + 80f, top + 50f, scriptPaint)
                
                // Render Hindi Transliteration
                canvas.drawText(hindiTrans[index], left + 80f, top + 85f, body)
                
                // Draw Count Shapes (Stars/Dots)
                val dotRadius = 7f
                val spacing = 22f
                for (s in 0..index) {
                    val sColumn = s % 5
                    val sRow = s / 5
                    canvas.drawCircle(left + 25f + sColumn * spacing, top + 115f + sRow * spacing, dotRadius, shapePaint)
                }
            }
        }
    }

    companion object {
        private const val PAGE_WIDTH = 595
        private const val PAGE_HEIGHT = 842
    }
}

enum class WorksheetTemplate(val label: String) {
    WORKSHEET("Worksheet"),
    FLASHCARDS("Visual Flashcard Set")
}
