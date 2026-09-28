package com.vector.escape.engine

import com.vector.escape.model.Arrow
import com.vector.escape.model.BoardState

object MoveValidator {
    fun isMoveLegal(board: BoardState, arrow: Arrow): Boolean {
        val (dr, dc) = arrow.direction.vector
        var currR = arrow.row + dr
        var currC = arrow.col + dc

        while (board.isValidPosition(currR, currC)) {
            if (!board.isCellEmpty(currR, currC)) {
                return false
            }
            currR += dr
            currC += dc
        }
        return true
    }

    fun getLegalMoves(board: BoardState): List<Arrow> {
        return board.arrows.filter { isMoveLegal(board, it) }
    }

    fun getFirstBlockingArrow(board: BoardState, arrow: Arrow): Arrow? {
        val (dr, dc) = arrow.direction.vector
        var currR = arrow.row + dr
        var currC = arrow.col + dc

        while (board.isValidPosition(currR, currC)) {
            val blocker = board.getArrowAt(currR, currC)
            if (blocker != null) {
                return blocker
            }
            currR += dr
            currC += dc
        }
        return null
    }

    fun getPathCells(board: BoardState, arrow: Arrow): List<Pair<Int, Int>> {
        val cells = mutableListOf<Pair<Int, Int>>()
        val (dr, dc) = arrow.direction.vector
        var currR = arrow.row + dr
        var currC = arrow.col + dc

        while (board.isValidPosition(currR, currC)) {
            cells.add(Pair(currR, currC))
            if (!board.isCellEmpty(currR, currC)) {
                break
            }
            currR += dr
            currC += dc
        }
        return cells
    }
}
