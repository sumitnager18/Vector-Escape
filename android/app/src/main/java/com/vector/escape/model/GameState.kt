package com.vector.escape.model

enum class GameStatus {
    PLAYING,
    CLEARED,
    FAILED
}

data class UndoStep(
    val board: BoardState,
    val flow: Int,
    val arrow: Arrow
)

data class GameState(
    val levelNumber: Int,
    val levelTitle: String,
    val difficulty: Difficulty,
    val board: BoardState,
    val initialBoard: BoardState,
    val hearts: Int = 3,
    val maxHearts: Int = 3,
    val moves: Int = 0,
    val mistakes: Int = 0,
    val flow: Int = 1,
    val maxFlow: Int = 1,
    val status: GameStatus = GameStatus.PLAYING,
    val stars: Int = 0,
    val undoStack: List<UndoStep> = emptyList()
) {
    companion object {
        fun calculateStars(mistakes: Int): Int {
            return when {
                mistakes == 0 -> 3
                mistakes <= 1 -> 2
                else -> 1
            }
        }
    }
}
