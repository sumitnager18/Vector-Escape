package com.vector.escape.engine

import com.vector.escape.model.*
import kotlin.random.Random

object PuzzleGenerator {
    fun generate(
        rows: Int,
        cols: Int,
        arrowCount: Int,
        seed: Long,
        maxRetries: Int = 25
    ): BoardState {
        val random = Random(seed)
        val target = arrowCount.coerceAtMost(rows * cols - 1)

        for (attempt in 1..maxRetries) {
            var currentBoard = BoardState(rows, cols, emptyList())
            val placed = mutableListOf<Arrow>()
            var idCounter = 1

            for (step in 0 until target) {
                val candidates = mutableListOf<Triple<Int, Int, Direction>>()
                for (r in 0 until rows) {
                    for (c in 0 until cols) {
                        if (currentBoard.isCellEmpty(r, c)) {
                            for (dir in Direction.values()) {
                                val temp = Arrow("temp", r, c, dir)
                                if (MoveValidator.isMoveLegal(currentBoard, temp)) {
                                    candidates.add(Triple(r, c, dir))
                                }
                            }
                        }
                    }
                }

                if (candidates.isEmpty()) break
                val chosen = candidates[random.nextInt(candidates.size)]
                val arrow = Arrow("a_\${idCounter++}", chosen.first, chosen.second, chosen.third)
                placed.add(arrow)
                currentBoard = currentBoard.addArrow(arrow)
            }

            if (placed.size == target) {
                val candidateBoard = BoardState(rows, cols, placed)
                val solver = PuzzleSolver.solve(candidateBoard)
                if (solver.isSolvable && solver.depth == target && solver.minInitialLegalMoves >= 1) {
                    return candidateBoard
                }
            }
        }

        return generateFallback(rows, cols, target)
    }

    private fun generateFallback(rows: Int, cols: Int, count: Int): BoardState {
        val safeArrows = mutableListOf<Arrow>()
        var id = 1
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (safeArrows.size >= count) break
                val distUp = r
                val distDown = rows - 1 - r
                val distLeft = c
                val distRight = cols - 1 - c
                val minDist = minOf(distUp, distDown, distLeft, distRight)

                val dir = when (minDist) {
                    distUp -> Direction.UP
                    distDown -> Direction.DOWN
                    distLeft -> Direction.LEFT
                    else -> Direction.RIGHT
                }
                safeArrows.add(Arrow("fb_\${id++}", r, c, dir))
            }
        }
        return BoardState(rows, cols, safeArrows)
    }
}
