package com.example.chesshand

import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Square
import com.github.bhlangonijr.chesslib.move.Move

class ChessEngine {
    val board = Board()
    var selectedSquare: Square? = null

    fun onSquareHovered(file: Int, rank: Int): Square {
        val squareName = "${('a' + file)}${8 - rank}".uppercase()
        return Square.fromValue(squareName)
    }

    fun selectOrMove(targetSquare: Square): Boolean {
        if (selectedSquare == null) {
            if (board.getPiece(targetSquare).pieceSide == board.sideToMove) {
                selectedSquare = targetSquare
            }
            return false
        } else {
            val move = Move(selectedSquare!!, targetSquare)
            val legalMoves = board.legalMoves()
            if (legalMoves.contains(move)) {
                board.doMove(move)
                selectedSquare = null
                return true
            } else {
                selectedSquare = null
                return false
            }
        }
    }
}
