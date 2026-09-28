package com.vector.escape.engine

import com.vector.escape.model.BoardState
import com.vector.escape.model.Difficulty

data class DifficultyMetrics(
    val arrowCount: Int,
    val density: Float,
    val initialLegalMoves: Int,
    val dependencyDepth: Int,
    val difficulty: Difficulty
)

object DifficultyAnalyzer {
    fun analyze(board: BoardState): DifficultyMetrics {
        val totalCells = board.rows * board.cols
        val count = board.remainingCount
        val density = if (totalCells > 0) count.toFloat() / totalCells else 0f
        val initialLegal = MoveValidator.getLegalMoves(board).size
        val solver = PuzzleSolver.solve(board)

        val diff = when {
            count >= 24 || (density >= 0.55f && solver.depth >= 15) -> Difficulty.EXPERT
            count >= 16 || solver.depth >= 10 -> Difficulty.HARD
            count >= 8 || solver.depth >= 5 -> Difficulty.MEDIUM
            else -> Difficulty.EASY
        }

        return DifficultyMetrics(
            arrowCount = count,
            density = density,
            initialLegalMoves = initialLegal,
            dependencyDepth = solver.depth,
            difficulty = diff
        )
    }
}
