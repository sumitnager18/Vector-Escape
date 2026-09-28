package com.vector.escape.model

class BoardState(
    val rows: Int,
    val cols: Int,
    arrowsList: List<Arrow>
) {
    val arrows: List<Arrow> = arrowsList.toList()
    private val grid: Array<Array<Arrow?>> = Array(rows) { Array(cols) { null } }

    init {
        for (arrow in arrows) {
            if (isValidPosition(arrow.row, arrow.col)) {
                grid[arrow.row][arrow.col] = arrow
            }
        }
    }

    fun isValidPosition(row: Int, col: Int): Boolean {
        return row in 0 until rows && col in 0 until cols
    }

    fun isCellEmpty(row: Int, col: Int): Boolean {
        if (!isValidPosition(row, col)) return true
        return grid[row][col] == null
    }

    fun getArrowAt(row: Int, col: Int): Arrow? {
        if (!isValidPosition(row, col)) return null
        return grid[row][col]
    }

    fun getArrowById(id: String): Arrow? {
        return arrows.find { it.id == id }
    }

    val remainingCount: Int
        get() = arrows.size

    val isCleared: Boolean
        get() = arrows.isEmpty()

    fun removeArrow(arrowId: String): BoardState {
        val next = arrows.filter { it.id != arrowId }
        return BoardState(rows, cols, next)
    }

    fun addArrow(arrow: Arrow): BoardState {
        val filtered = arrows.filter { !(it.row == arrow.row && it.col == arrow.col) }
        return BoardState(rows, cols, filtered + arrow)
    }

    fun toKey(): String {
        return arrows.map { "\${it.row},\${it.col},\${it.direction}" }.sorted().joinToString("|")
    }
}
