package com.vector.escape.engine

import com.vector.escape.model.Difficulty
import com.vector.escape.model.LevelDefinition
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DailyVectorGenerator {
    fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    fun generateForDate(dateStr: String): LevelDefinition {
        val seed = dateStr.hashCode().toLong()
        val parts = dateStr.split("-")
        val day = parts.getOrNull(2)?.toIntOrNull() ?: 1
        val mod = day % 4

        val (diff, count, rows, cols) = when (mod) {
            0 -> Quad(Difficulty.EASY, 10, 5, 5)
            1 -> Quad(Difficulty.MEDIUM, 14, 6, 6)
            2 -> Quad(Difficulty.HARD, 20, 6, 6)
            else -> Quad(Difficulty.EXPERT, 24, 6, 6)
        }

        val board = PuzzleGenerator.generate(rows, cols, count, seed)
        return LevelDefinition(
            levelNumber = 0,
            title = "Daily Vector: $dateStr",
            rows = rows,
            cols = cols,
            difficulty = diff,
            arrows = board.arrows,
            parMoves = board.arrows.size
        )
    }

    private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
