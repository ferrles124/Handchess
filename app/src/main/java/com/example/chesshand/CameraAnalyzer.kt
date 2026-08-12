package com.example.chesshand

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy

class CameraAnalyzer(
    context: Context,
    private val onPalmDetected: (x: Float, y: Float, boundingArea: Float) -> Unit
) : ImageAnalysis.Analyzer {

    private val palmDetector = PalmDetector(context)

    override fun analyze(imageProxy: ImageProxy) {
        val bitmap = imageProxy.toBitmap()
        val matrix = Matrix().apply {
            postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
            postScale(-1f, 1f) // Ön kamera için aynalama
        }
        
        val rotatedBitmap = Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
        )

        val rect = palmDetector.detect(rotatedBitmap)
        if (rect != null) {
            val centerX = rect.centerX() / rotatedBitmap.width
            val centerY = rect.centerY() / rotatedBitmap.height
            val area = (rect.width() * rect.height()) / (rotatedBitmap.width * rotatedBitmap.height)
            onPalmDetected(centerX, centerY, area)
        }

        imageProxy.close()
    }
}
