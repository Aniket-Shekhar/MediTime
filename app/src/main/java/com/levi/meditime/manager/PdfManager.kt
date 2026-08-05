package com.levi.meditime.manager

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.levi.meditime.domain.model.Medicine
import com.levi.meditime.domain.model.History
import com.levi.meditime.domain.model.DoseStatus
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance

class PdfManager(private val context: Context) {

    fun generateFullReport(
        userName: String,
        adherence: Int,
        history: List<History>,
        medicines: List<Medicine>,
        fileName: String
    ): File? {
        val trace = FirebasePerformance.getInstance().newTrace("pdf_generate_report")
        trace.start()
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        var page = document.startPage(pageInfo)
        var canvas: Canvas = page.canvas
        val paint = Paint()
        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

        // Header
        paint.textSize = 24f
        paint.isFakeBoldText = true
        paint.color = Color.BLACK
        canvas.drawText("MediTime Health Report", 50f, 60f, paint)
        
        paint.textSize = 12f
        paint.isFakeBoldText = false
        paint.color = Color.GRAY
        canvas.drawText("Generated on: ${dateFormat.format(Date())}", 50f, 80f, paint)
        canvas.drawText("Patient: $userName", 50f, 100f, paint)

        // Adherence Section
        paint.color = Color.BLACK
        paint.textSize = 16f
        paint.isFakeBoldText = true
        canvas.drawText("Overall Adherence: $adherence%", 50f, 140f, paint)

        // Draw simple bar for adherence
        paint.color = Color.LTGRAY
        canvas.drawRect(50f, 150f, 350f, 170f, paint)
        paint.color = if (adherence > 80) Color.GREEN else if (adherence > 50) Color.YELLOW else Color.RED
        canvas.drawRect(50f, 150f, 50f + (adherence * 3f), 170f, paint)

        // Medicines List
        var y = 210f
        paint.color = Color.BLACK
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("Current Medicines", 50f, y, paint)
        y += 20f
        
        paint.textSize = 10f
        paint.isFakeBoldText = false
        medicines.forEach { med ->
            if (y > 800) {
                document.finishPage(page)
                page = document.startPage(pageInfo)
                canvas = page.canvas
                y = 50f
            }
            canvas.drawText("${med.name} - ${med.dosage} (${med.currentStock} remaining)", 60f, y, paint)
            y += 15f
        }

        // History Section
        y += 20f
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("Recent Dose History", 50f, y, paint)
        y += 20f
        
        paint.textSize = 10f
        paint.isFakeBoldText = false
        history.take(20).forEach { item ->
            if (y > 800) {
                document.finishPage(page)
                page = document.startPage(pageInfo)
                canvas = page.canvas
                y = 50f
            }
            val statusColor = if (item.status == DoseStatus.TAKEN) Color.GREEN else Color.RED
            val medName = medicines.find { it.id == item.medicineId }?.name ?: "Unknown Medicine"
            val displayTime = item.actionTime ?: item.scheduledTime
            
            paint.color = Color.BLACK
            canvas.drawText("${dateFormat.format(Date(displayTime))} - $medName", 60f, y, paint)
            paint.color = statusColor
            canvas.drawText(item.status.name, 450f, y, paint)
            y += 15f
        }

        document.finishPage(page)

        val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "MediTime")
        if (!dir.exists()) dir.mkdirs()
        
        val file = File(dir, "$fileName.pdf")
        return try {
            document.writeTo(FileOutputStream(file))
            document.close()
            file
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            document.close()
            null
        } finally {
            trace.stop()
        }
    }

    fun generateReport(medicines: List<Medicine>, fileName: String): File? {
        return generateFullReport("User", 0, emptyList(), medicines, fileName)
    }
}
