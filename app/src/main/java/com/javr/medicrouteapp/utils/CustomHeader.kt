package com.javr.medicrouteapp.utils

import android.content.Context
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Chunk
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.FontFactory
import com.itextpdf.text.Image
import com.itextpdf.text.Phrase
import com.itextpdf.text.pdf.ColumnText
import com.itextpdf.text.pdf.PdfPageEventHelper
import com.itextpdf.text.pdf.PdfWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CustomHeader (private val context: Context, private val titulo: String) : PdfPageEventHelper() {
    private val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    private var isFirstPage = true // Para controlar si es la primera página

    override fun onStartPage(writer: PdfWriter, document: Document) {
        if (isFirstPage) {
            try {
                val imagenId = context.resources.getIdentifier("logo_app", "drawable", context.packageName)
                val inputStream = context.resources.openRawResource(imagenId)
                val byteArray = inputStream.readBytes()
                val imagen = Image.getInstance(byteArray)
                imagen.scaleToFit(100f, 100f)
                imagen.setAbsolutePosition(20f, 740f)
                writer.directContent.addImage(imagen)

                val fuenteTitulo = FontFactory.getFont("arial", 13f, Font.BOLD, BaseColor.BLACK)

                val cb = writer.directContent
                cb.saveState()
                cb.beginText()

                // Agregar título (pasado por parámetro) en el centro del encabezado
                val centerX = (document.right() + document.left()) / 2
                val fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13f)
                val chunkTitulo = Chunk(titulo, fontTitulo)
                val phrase = Phrase(chunkTitulo)

                // Calcular la posición X para centrar el título en el encabezado
                val titleWidth = fontTitulo.getCalculatedBaseFont(false).getWidthPoint(titulo, 13f)
                val xPos = centerX - (titleWidth / 2)

                ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, phrase, xPos, 785f, 0f)

                val dateText = sdf.format(Date())
                val dateWidth = fuenteTitulo.getCalculatedBaseFont(false).getWidthPoint(dateText, 13f)
                cb.setFontAndSize(fuenteTitulo.getCalculatedBaseFont(false), 13f)
                cb.setTextMatrix(document.right() - dateWidth - 20f, 785f)
                cb.showText(dateText)

                cb.endText()
                cb.restoreState()

                isFirstPage = false // Marcar que ya no es la primera página
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}