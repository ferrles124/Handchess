package com.example.chesshand

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class ChessBoardView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val lightPaint = Paint().apply { color = Color.LTGRAY }
    private val darkPaint = Paint().apply { color = Color.DKGRAY }
    private val cursorPaint = Paint().apply { color = Color.RED; style = Paint.Style.FILL }
    
    var cursorX: Float = -1f
    var cursorY: Float = -1f

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val tileSize = width / 8f

        for (row in 0 until 8) {
            for (col in 0 until 8) {
                val paint = if ((row + col) % 2 == 0) lightPaint else darkPaint
                canvas.drawRect(
                    col * tileSize,
                    row * tileSize,
                    (col + 1) * tileSize,
                    (row + 1) * tileSize,
                    paint
                )
            }
        }

        if (cursorX >= 0 && cursorY >= 0) {
            canvas.drawCircle(cursorX * width, cursorY * height, 30f, cursorPaint)
        }
    }

    fun updateCursor(x: Float, y: Float) {
        this.cursorX = x
        this.cursorY = y
        invalidate()
    }
}
