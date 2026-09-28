package com.vector.escape.model

enum class Difficulty {
    EASY,
    MEDIUM,
    HARD,
    EXPERT
}

data class LevelDefinition(
    val levelNumber: Int,
    val title: String,
    val rows: Int,
    val cols: Int,
    val difficulty: Difficulty,
    val arrows: List<Arrow>,
    val parMoves: Int,
    val tutorialTip: String? = null
)
