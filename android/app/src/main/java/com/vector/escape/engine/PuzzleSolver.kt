package com.vector.escape.engine

import com.vector.escape.model.Arrow
import com.vector.escape.model.BoardState

data class SolverResult(
    val isSolvable: Boolean,
    val solutionOrder: List<String>,
    val depth: Int,
    val statesVisited: Int,
    val minInitialLegalMoves: Int
)

object PuzzleSolver {
    fun solve(board: BoardState, maxStates: Int = 10000): SolverResult {
        val visited = mutableSetOf<String>()
        var states = 0
        val initialLegal = MoveValidator.getLegalMoves(board).size

        if (board.isCleared) {
            return SolverResult(true, emptyList(), 0, 1, 0)
        }

        if (initialLegal == 0) {
            return SolverResult(false, emptyList(), 0, 1, 0)
        }

        fun search(current: BoardState, path: List<String>): List<String>? {
            states++
            if (states > maxStates) return null
            if (current.isCleared) return path

            val key = current.toKey()
            if (visited.contains(key)) return null
            visited.add(key)

            val legal = MoveValidator.getLegalMoves(current)
            if (legal.isEmpty()) return null

            for (move in legal) {
                val nextBoard = current.removeArrow(move.id)
                val res = search(nextBoard, path + move.id)
                if (res != null) return res
            }
            return null
        }

        val solution = search(board, emptyList())
        return SolverResult(
            isSolvable = solution != null,
            solutionOrder = solution ?: emptyList(),
            depth = solution?.size ?: 0,
            statesVisited = states,
            minInitialLegalMoves = initialLegal
        )
    }

    fun isSolvable(board: BoardState): Boolean {
        return solve(board).isSolvable
    }
}
