package com.vector.escape.engine

import com.vector.escape.model.Arrow
import com.vector.escape.model.Difficulty
import com.vector.escape.model.Direction
import com.vector.escape.model.LevelDefinition

object CampaignLevels {
    private var cachedLevels: List<LevelDefinition>? = null

    fun getAll(): List<LevelDefinition> {
        if (cachedLevels != null) return cachedLevels!!

        val list = mutableListOf<LevelDefinition>()

        // 1-10 Handcrafted Tutorial & Progressive Levels
        list.add(
            LevelDefinition(
                levelNumber = 1,
                title = "First Opening",
                rows = 4,
                cols = 4,
                difficulty = Difficulty.EASY,
                arrows = listOf(
                    Arrow("l1_1", 1, 2, Direction.UP)
                ),
                parMoves = 1,
                tutorialTip = "Tap an arrow whose escape lane to the board edge is completely clear."
            )
        )

        list.add(
            LevelDefinition(
                levelNumber = 2,
                title = "Two Vectors",
                rows = 4,
                cols = 4,
                difficulty = Difficulty.EASY,
                arrows = listOf(
                    Arrow("l2_1", 2, 1, Direction.LEFT),
                    Arrow("l2_2", 1, 2, Direction.RIGHT)
                ),
                parMoves = 2,
                tutorialTip = "Two distinct vectors. Each escapes along its pointing axis."
            )
        )

        list.add(
            LevelDefinition(
                levelNumber = 3,
                title = "The Blocker",
                rows = 4,
                cols = 4,
                difficulty = Difficulty.EASY,
                arrows = listOf(
                    Arrow("l3_1", 2, 1, Direction.UP),
                    Arrow("l3_2", 1, 1, Direction.RIGHT)
                ),
                parMoves = 2,
                tutorialTip = "Vector A is blocked by Vector B. Clear Vector B first!"
            )
        )

        list.add(
            LevelDefinition(
                levelNumber = 4,
                title = "Simple Dependency",
                rows = 4,
                cols = 4,
                difficulty = Difficulty.EASY,
                arrows = listOf(
                    Arrow("l4_1", 3, 2, Direction.UP),
                    Arrow("l4_2", 2, 2, Direction.UP),
                    Arrow("l4_3", 1, 2, Direction.LEFT)
                ),
                parMoves = 3,
                tutorialTip = "A 3-step sequence: clear from the outer edge inward."
            )
        )

        list.add(
            LevelDefinition(
                levelNumber = 5,
                title = "The Crossing",
                rows = 5,
                cols = 5,
                difficulty = Difficulty.EASY,
                arrows = listOf(
                    Arrow("l5_1", 2, 2, Direction.UP),
                    Arrow("l5_2", 1, 2, Direction.LEFT),
                    Arrow("l5_3", 3, 2, Direction.DOWN),
                    Arrow("l5_4", 2, 3, Direction.RIGHT)
                ),
                parMoves = 4,
                tutorialTip = "Multiple valid opening vectors. Choose your sequence."
            )
        )

        list.add(
            LevelDefinition(
                levelNumber = 6,
                title = "Mixed Directions",
                rows = 5,
                cols = 5,
                difficulty = Difficulty.MEDIUM,
                arrows = listOf(
                    Arrow("l6_1", 1, 1, Direction.UP),
                    Arrow("l6_2", 3, 3, Direction.DOWN),
                    Arrow("l6_3", 1, 3, Direction.LEFT),
                    Arrow("l6_4", 3, 1, Direction.RIGHT),
                    Arrow("l6_5", 2, 2, Direction.UP),
                    Arrow("l6_6", 1, 2, Direction.UP)
                ),
                parMoves = 6,
                tutorialTip = "Four orthogonal directions interacting across the grid."
            )
        )

        list.add(
            LevelDefinition(
                levelNumber = 7,
                title = "Small Chain",
                rows = 5,
                cols = 5,
                difficulty = Difficulty.MEDIUM,
                arrows = listOf(
                    Arrow("l7_1", 4, 1, Direction.UP),
                    Arrow("l7_2", 3, 1, Direction.UP),
                    Arrow("l7_3", 2, 1, Direction.RIGHT),
                    Arrow("l7_4", 2, 3, Direction.DOWN),
                    Arrow("l7_5", 3, 3, Direction.RIGHT),
                    Arrow("l7_6", 1, 4, Direction.UP)
                ),
                parMoves = 6,
                tutorialTip = "Unravel the cascading dependency lane by lane."
            )
        )

        list.add(
            LevelDefinition(
                levelNumber = 8,
                title = "Cross-Dependency",
                rows = 5,
                cols = 5,
                difficulty = Difficulty.MEDIUM,
                arrows = listOf(
                    Arrow("l8_1", 2, 1, Direction.RIGHT),
                    Arrow("l8_2", 2, 2, Direction.DOWN),
                    Arrow("l8_3", 3, 2, Direction.RIGHT),
                    Arrow("l8_4", 1, 2, Direction.DOWN),
                    Arrow("l8_5", 2, 3, Direction.UP),
                    Arrow("l8_6", 4, 2, Direction.LEFT)
                ),
                parMoves = 6,
                tutorialTip = "Perpendicular paths cross at the center junction."
            )
        )

        list.add(
            LevelDefinition(
                levelNumber = 9,
                title = "Branching Order",
                rows = 5,
                cols = 5,
                difficulty = Difficulty.HARD,
                arrows = listOf(
                    Arrow("l9_1", 1, 1, Direction.LEFT),
                    Arrow("l9_2", 3, 3, Direction.RIGHT),
                    Arrow("l9_3", 2, 1, Direction.UP),
                    Arrow("l9_4", 1, 2, Direction.LEFT),
                    Arrow("l9_5", 2, 3, Direction.DOWN),
                    Arrow("l9_6", 3, 2, Direction.RIGHT),
                    Arrow("l9_7", 2, 2, Direction.UP),
                    Arrow("l9_8", 3, 1, Direction.UP)
                ),
                parMoves = 8,
                tutorialTip = "Analyze the consequences of each branch before launching."
            )
        )

        list.add(
            LevelDefinition(
                levelNumber = 10,
                title = "The Interlock",
                rows = 6,
                cols = 6,
                difficulty = Difficulty.HARD,
                arrows = listOf(
                    Arrow("l10_1", 0, 2, Direction.UP),
                    Arrow("l10_2", 1, 2, Direction.UP),
                    Arrow("l10_3", 2, 2, Direction.RIGHT),
                    Arrow("l10_4", 2, 4, Direction.DOWN),
                    Arrow("l10_5", 4, 4, Direction.LEFT),
                    Arrow("l10_6", 4, 3, Direction.DOWN),
                    Arrow("l10_7", 5, 3, Direction.DOWN),
                    Arrow("l10_8", 3, 1, Direction.LEFT),
                    Arrow("l10_9", 3, 3, Direction.LEFT)
                ),
                parMoves = 9,
                tutorialTip = "The master interlock. Precision clearance required."
            )
        )

        // 11-50 Procedural Deterministic Solvable Levels
        for (i in 11..50) {
            val (rows, cols, count, diff) = when {
                i <= 10 -> Quad(5, 5, 5 + (i - 5), Difficulty.EASY)
                i <= 20 -> Quad(6, 6, 10 + (i - 10) / 2, Difficulty.MEDIUM)
                i <= 30 -> Quad(6, 6, 15 + (i - 20) / 2, Difficulty.MEDIUM)
                i <= 40 -> Quad(6, 6, 20 + (i - 30) / 2, Difficulty.HARD)
                else -> Quad(6, 6, 24 + (i - 40) / 2, Difficulty.EXPERT)
            }

            val seed = ("lvl_seed_v2_" + i).hashCode().toLong()
            val board = PuzzleGenerator.generate(rows, cols, count, seed)

            list.add(
                LevelDefinition(
                    levelNumber = i,
                    title = "Sector $i",
                    rows = rows,
                    cols = cols,
                    difficulty = diff,
                    arrows = board.arrows,
                    parMoves = board.arrows.size
                )
            )
        }

        cachedLevels = list
        return list
    }

    fun getLevel(num: Int): LevelDefinition {
        val all = getAll()
        val index = (num - 1).coerceIn(0, all.size - 1)
        return all[index]
    }

    private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
