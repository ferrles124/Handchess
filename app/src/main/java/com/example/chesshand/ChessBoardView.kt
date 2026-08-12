package com.example.chesshand

import android.content.Context
import android.graphics.Camera
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import com.github.bhlangonijr.chesslib.Piece
import com.github.bhlangonijr.chesslib.Square

class ChessBoardView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    var engine: ChessEngine? = null

    private val camera3D = Camera()
    private val matrix3D = Matrix()

    private val lightPaint = Paint().apply { color = Color.parseColor("#EEEED2"); isAntiAlias = true }
    private val darkPaint = Paint().apply { color = Color.parseColor("#769656"); isAntiAlias = true }
    private val sideLightPaint = Paint().apply { color = Color.parseColor("#B1B18E"); isAntiAlias = true }
    private val sideDarkPaint = Paint().apply { color = Color.parseColor("#556B3E"); isAntiAlias = true }
    private val highlightPaint = Paint().apply { color = Color.parseColor("#F7F769"); isAntiAlias = true }
    private val cursorPaint = Paint().apply { color = Color.RED; style = Paint.Style.FILL; isAntiAlias = true }
    
    private val piecePaint = Paint().apply {
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    var cursorX: Float = -1f
    var cursorY: Float = -1f

    init {
        // Dokunmatik etkileşimleri Android seviyesinde tamamen kapat
        setOnTouchListener { _, _ -> true }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.save()

        // 3D Perspektif Matrisi (X ekseninde yatırma)
        val centerX = width / 2f
        val centerY = height / 2f
        camera3D.save()
        camera3D.rotateX(55f)
        camera3D.getMatrix(matrix3D)
        camera3D.restore()

        matrix3D.preTranslate(-centerX, -centerY)
        matrix3D.postTranslate(centerX, centerY)
        canvas.concat(matrix3D)

        val boardSize = width * 0.8f
        val tileSize = boardSize / 8f
        val startX = (width - boardSize) / 2f
        val startY = (height - boardSize) / 2f
        val depth = 20f // 3D Taş/Kare Kalınlığı

        piecePaint.textSize = tileSize * 0.75f

        for (row in 0 until 8) {
            for (col in 0 until 8) {
                val rankName = (8 - row).toString()
                val fileName = ('a' + col).toString()
                val squareName = (fileName + rankName).uppercase()

                val isSelected = engine?.selectedSquare?.value() == squareName
                val isLight = (row + col) % 2 == 0
                
                val tilePaint = if (isSelected) highlightPaint else (if (isLight) lightPaint else darkPaint)
                val sidePaint = if (isLight) sideLightPaint else sideDarkPaint

                val left = startX + (col * tileSize)
                val top = startY + (row * tileSize)
                val right = left + tileSize
                val bottom = top + tileSize

                // 3D Blok Yan Derinlik Çizimi
                val path = Path().apply {
                    moveTo(left, bottom)
                    lineTo(right, bottom)
                    lineTo(right, bottom + depth)
                    lineTo(left, bottom + depth)
                    close()
                }
                canvas.drawPath(path, sidePaint)

                // 3D Blok Üst Yüzey Çizimi
                canvas.drawRect(left, top, right, bottom, tilePaint)

                // 3D Taşları Çiz
                engine?.let {
                    try {
                        val square = Square.fromValue(squareName)
                        val piece = it.board.getPiece(square)
                        if (piece != Piece.NONE) {
                            val unicode = getPieceUnicode(piece)
                            val textY = top + (tileSize / 2f) - ((piecePaint.descent() + piecePaint.ascent()) / 2f)
                            canvas.drawText(unicode, left + (tileSize / 2f), textY - 10f, piecePaint)
                        }
                    } catch (e: Exception) {}
                }
            }
        }

        // 3D El İmleci Çizimi
        if (cursorX >= 0 && cursorY >= 0) {
            val cursor3DX = startX + (cursorX * boardSize)
            val cursor3DY = startY + (cursorY * boardSize)
            canvas.drawCircle(cursor3DX, cursor3DY, 20f, cursorPaint)
        }

        canvas.restore()
    }

    fun updateCursor(x: Float, y: Float) {
        this.cursorX = x
        this.cursorY = y
        invalidate()
    }

    private fun getPieceUnicode(piece: Piece): String {
        return when (piece) {
            Piece.WHITE_KING -> "♔"
            Piece.WHITE_QUEEN -> "♕"
            Piece.WHITE_ROOK -> "♖"
            Piece.WHITE_BISHOP -> "♗"
            Piece.WHITE_KNIGHT -> "♘"
            Piece.WHITE_PAWN -> "♙"
            Piece.BLACK_KING -> "♚"
            Piece.BLACK_QUEEN -> "♛"
            Piece.BLACK_ROOK -> "♜"
            Piece.BLACK_BISHOP -> "♝"
            Piece.BLACK_KNIGHT -> "♞"
            Piece.BLACK_PAWN -> "♟"
            else -> ""
        }
    }
}
