package com.example.chesshand

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.detector.ObjectDetector

class PalmDetector(context: Context) {
    private var detector: ObjectDetector? = null

    init {
        val baseOptions = BaseOptions.builder().setNumThreads(2).build()
        val options = ObjectDetector.ObjectDetectorOptions.builder()
            .setBaseOptions(baseOptions)
            .setMaxResults(1)
            .setScoreThreshold(0.5f)
            .build()
        
        try {
            detector = ObjectDetector.createFromFileAndOptions(
                context,
                "palm_detection.tflite",
                options
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun detect(bitmap: Bitmap): RectF? {
        val tensorImage = TensorImage.fromBitmap(bitmap)
        val results = detector?.detect(tensorImage)
        return results?.firstOrNull()?.boundingBox
    }
}
