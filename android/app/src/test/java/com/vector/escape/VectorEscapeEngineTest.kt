package com.vector.escape

import com.vector.escape.engine.*
import com.vector.escape.model.*
import org.junit.Assert.*
import org.junit.Test

class VectorEscapeEngineTest {

    @Test
    fun test1_emptyBoard() {
        val board = BoardState(4, 4, emptyList())
        assertTrue("Empty board must be cleared", board.isCleared)
        assertEquals(0, MoveValidator.getLegalMoves(board).size)
        assertTrue("Empty board should be solvable", PuzzleSolver.isSolvable(board))
    }

    @Test
    fun test2_singleLegalArrow() {
        val arrow = Arrow("a1", 1, 1, Direction.UP)
        val board = BoardState(4, 4, listOf(arrow))
        assertTrue("Arrow pointing to free edge must be legal", MoveValidator.isMoveLegal(board, arrow))
        val legal = MoveValidator.getLegalMoves(board)
        assertEquals(1, legal.size)
        assertEquals("a1", legal[0].id)
        val solver = PuzzleSolver.solve(board)
        assertTrue(solver.isSolvable)
        assertEquals(1, solver.depth)
    }

    @Test
    fun test3_blockedArrow() {
        val blocker = Arrow("b", 1, 1, Direction.RIGHT)
        val blocked = Arrow("a", 2, 1, Direction.UP)
        val board = BoardState(4, 4, listOf(blocker, blocked))
        assertFalse("Arrow blocked by another arrow cannot be legal", MoveValidator.isMoveLegal(board, blocked))
        val foundBlocker = MoveValidator.getFirstBlockingArrow(board, blocked)
        assertNotNull(foundBlocker)
        assertEquals("b", foundBlocker?.id)
    }

    @Test
    fun test4_horizontalBlocking() {
        val left = Arrow("l", 2, 0, Direction.RIGHT)
        val right = Arrow("r", 2, 3, Direction.DOWN)
        val board = BoardState(5, 5, listOf(left, right))
        assertFalse(MoveValidator.isMoveLegal(board, left))
        assertTrue(MoveValidator.isMoveLegal(board, right))
    }

    @Test
    fun test5_verticalBlocking() {
        val top = Arrow("t", 0, 2, Direction.DOWN)
        val bottom = Arrow("b", 3, 2, Direction.RIGHT)
        val board = BoardState(5, 5, listOf(top, bottom))
        assertFalse(MoveValidator.isMoveLegal(board, top))
        assertTrue(MoveValidator.isMoveLegal(board, bottom))
    }

    @Test
    fun test6_multipleLegalMoves() {
        val a1 = Arrow("a1", 0, 0, Direction.UP)
        val a2 = Arrow("a2", 0, 3, Direction.RIGHT)
        val a3 = Arrow("a3", 3, 3, Direction.DOWN)
        val a4 = Arrow("a4", 3, 0, Direction.LEFT)
        val board = BoardState(4, 4, listOf(a1, a2, a3, a4))
        assertEquals(4, MoveValidator.getLegalMoves(board).size)
    }

    @Test
    fun test7_removingArrowUpdatesBoardAndExposesLegal() {
        val blocker = Arrow("b", 1, 2, Direction.RIGHT)
        val blocked = Arrow("a", 3, 2, Direction.UP)
        var board = BoardState(5, 5, listOf(blocker, blocked))
        assertFalse(MoveValidator.isMoveLegal(board, blocked))
        board = board.removeArrow("b")
        assertTrue(board.isCellEmpty(1, 2))
        assertTrue(MoveValidator.isMoveLegal(board, blocked))
    }

    @Test
    fun test8_completionDetection() {
        val a1 = Arrow("a1", 1, 1, Direction.UP)
        var board = BoardState(4, 4, listOf(a1))
        assertFalse(board.isCleared)
        board = board.removeArrow("a1")
        assertTrue(board.isCleared)
        assertEquals(0, board.remainingCount)
    }

    @Test
    fun test9_invalidMovePreservesOccupancy() {
        val blocker = Arrow("b", 1, 2, Direction.RIGHT)
        val blocked = Arrow("a", 3, 2, Direction.UP)
        val board = BoardState(5, 5, listOf(blocker, blocked))
        val keyBefore = board.toKey()
        val isLegal = MoveValidator.isMoveLegal(board, blocked)
        assertFalse(isLegal)
        assertEquals(keyBefore, board.toKey())
    }

    @Test
    fun test10_hearts() {
        val state = GameState(1, "Test", Difficulty.EASY, BoardState(4, 4, emptyList()), BoardState(4, 4, emptyList()))
        assertEquals(3, state.hearts)
        val mistakeState = state.copy(hearts = state.hearts - 1, mistakes = state.mistakes + 1)
        assertEquals(2, mistakeState.hearts)
    }

    @Test
    fun test11_undo() {
        val a1 = Arrow("a1", 0, 1, Direction.UP)
        val a2 = Arrow("a2", 2, 2, Direction.DOWN)
        val board = BoardState(4, 4, listOf(a1, a2))
        val stack = listOf(UndoStep(board, 1, a1))
        val nextBoard = board.removeArrow("a1")
        assertEquals(1, nextBoard.remainingCount)
        val restored = stack.last().board
        assertEquals(2, restored.remainingCount)
    }

    @Test
    fun test12_restart() {
        val a1 = Arrow("a1", 0, 1, Direction.UP)
        val initial = BoardState(4, 4, listOf(a1))
        val afterMove = initial.removeArrow("a1")
        assertEquals(0, afterMove.remainingCount)
        val restarted = initial
        assertEquals(1, restarted.remainingCount)
    }

    @Test
    fun test13_hintCorrectness() {
        val blocker = Arrow("b", 1, 2, Direction.RIGHT)
        val blocked = Arrow("a", 3, 2, Direction.UP)
        val board = BoardState(5, 5, listOf(blocker, blocked))
        val legal = MoveValidator.getLegalMoves(board)
        assertTrue(legal.isNotEmpty())
        val hint = legal[0]
        assertTrue(MoveValidator.isMoveLegal(board, hint))
        assertEquals("b", hint.id)
    }

    @Test
    fun test14_proceduralGeneration() {
        val board = PuzzleGenerator.generate(6, 6, 8, 12345L)
        assertEquals(8, board.remainingCount)
        assertTrue(MoveValidator.getLegalMoves(board).isNotEmpty())
    }

    @Test
    fun test15_solvabilityValidation() {
        val board = PuzzleGenerator.generate(5, 5, 7, 98765L)
        val solver = PuzzleSolver.solve(board)
        assertTrue("Generated puzzle must be solvable", solver.isSolvable)
        assertEquals(7, solver.depth)
    }

    @Test
    fun test16_deterministicSeed() {
        val b1 = PuzzleGenerator.generate(5, 5, 6, 424242L)
        val b2 = PuzzleGenerator.generate(5, 5, 6, 424242L)
        assertEquals(b1.toKey(), b2.toKey())
    }

    @Test
    fun test17_dailyChallengeDeterminism() {
        val d1 = DailyVectorGenerator.generateForDate("2026-09-26")
        val d2 = DailyVectorGenerator.generateForDate("2026-09-26")
        assertEquals(d1.arrows.size, d2.arrows.size)
        assertEquals(BoardState(d1.rows, d1.cols, d1.arrows).toKey(), BoardState(d2.rows, d2.cols, d2.arrows).toKey())
    }

    @Test
    fun test18_starCalculation() {
        assertEquals(3, GameState.calculateStars(0))
        assertEquals(2, GameState.calculateStars(1))
        assertEquals(1, GameState.calculateStars(2))
    }

    @Test
    fun test19_handcrafted10LevelVerticalSlice() {
        for (lvl in 1..10) {
            val def = CampaignLevels.getLevel(lvl)
            val board = BoardState(def.rows, def.cols, def.arrows)
            assertTrue("Level $lvl (${def.title}) must have legal opening moves", MoveValidator.getLegalMoves(board).isNotEmpty())
            val solverRes = PuzzleSolver.solve(board)
            assertTrue("Level $lvl (${def.title}) must be solvable", solverRes.isSolvable)
            assertEquals("Level $lvl depth matches arrow count", def.arrows.size, solverRes.depth)
        }
    }

    @Test
    fun test20_campaignIntegrity() {
        val all = CampaignLevels.getAll()
        assertEquals(50, all.size)
        for (def in all) {
            val board = BoardState(def.rows, def.cols, def.arrows)
            assertTrue("Sector ${def.levelNumber} must have legal moves", MoveValidator.getLegalMoves(board).isNotEmpty())
        }
    }
}
