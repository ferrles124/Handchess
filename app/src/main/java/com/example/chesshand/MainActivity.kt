package com.example.chesshand

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.chesshand.databinding.ActivityMainBinding
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    
    private val chessEngine = ChessEngine()
    private var hoverCol = -1
    private var hoverRow = -1
    private var hoverStartTime = 0L
    private val DWELL_THRESHOLD = 1500L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.chessBoardView.engine = chessEngine

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .apply {
                    setAnalyzer(
                        cameraExecutor,
                        CameraAnalyzer(this@MainActivity) { x, y, _ ->
                            runOnUiThread {
                                binding.chessBoardView.updateCursor(x, y)
                                processHandInteraction(x, y)
                            }
                        }
                    )
                }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun processHandInteraction(x: Float, y: Float) {
        val col = (x * 8).toInt().coerceIn(0, 7)
        val row = (y * 8).toInt().coerceIn(0, 7)

        if (col == hoverCol && row == hoverRow) {
            if (System.currentTimeMillis() - hoverStartTime > DWELL_THRESHOLD) {
                val square = chessEngine.onSquareHovered(col, row)
                chessEngine.selectOrMove(square)
                binding.chessBoardView.invalidate()
                hoverStartTime = System.currentTimeMillis() + 1000L
            }
        } else {
            hoverCol = col
            hoverRow = row
            hoverStartTime = System.currentTimeMillis()
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
