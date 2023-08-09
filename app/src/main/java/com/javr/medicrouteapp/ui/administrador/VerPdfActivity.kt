package com.javr.medicrouteapp.ui.administrador

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.javr.medicrouteapp.databinding.ActivityVerPdfBinding


import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import com.javr.medicrouteapp.R
import java.io.File

class VerPdfActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_PATH_PDF = "VerPdfActivity:PathPdf"
    }

    private lateinit var binding: ActivityVerPdfBinding
    private lateinit var pdfRenderer: PdfRenderer
    private lateinit var pdfDocument: PdfRenderer.Page

    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private lateinit var gestureDetector: GestureDetector
    private var scaleFactor = 1.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerPdfBinding.inflate(layoutInflater)
        setTheme(R.style.AppTheme_Light)
        setContentView(binding.root)

        val extraPathPdf = intent.getStringExtra(EXTRA_PATH_PDF)

        scaleGestureDetector = ScaleGestureDetector(this, ScaleListener())
        gestureDetector = GestureDetector(this, GestureListener())

        val fileDescriptor = getFileDescriptor(extraPathPdf!!)
        if (fileDescriptor != null) {
            pdfRenderer = PdfRenderer(fileDescriptor)
            pdfDocument = pdfRenderer.openPage(0)

            val bitmap = Bitmap.createBitmap(pdfDocument.width, pdfDocument.height, Bitmap.Config.ARGB_8888)
            pdfDocument.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
            binding.pdfImageView.setImageBitmap(bitmap)
        }
    }

    private fun getFileDescriptor(pdfFilePath: String): ParcelFileDescriptor? {
        val file = File(pdfFilePath)
        val uri = Uri.fromFile(file)
        return contentResolver.openFileDescriptor(uri, "r")
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            scaleGestureDetector.onTouchEvent(event)
            gestureDetector.onTouchEvent(event)
        }

        return true
    }

    inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleFactor *= detector?.scaleFactor ?: 1.0f
            scaleFactor = scaleFactor.coerceIn(0.1f, 10.0f) // Limit the scale factor

            binding.pdfImageView.scaleX = scaleFactor
            binding.pdfImageView.scaleY = scaleFactor
            return true
        }
    }

    inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(
            e1: MotionEvent,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            binding.pdfImageView.translationX -= distanceX
            binding.pdfImageView.translationY -= distanceY
            return true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        pdfDocument.close()
        pdfRenderer.close()
    }
}