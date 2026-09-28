package com.vector.escape.model

data class DirectionVector(val dr: Int, val dc: Int, val angleDeg: Float)

enum class Direction(val dr: Int, val dc: Int, val angleDeg: Float) {
    UP(-1, 0, 0f),
    RIGHT(0, 1, 90f),
    DOWN(1, 0, 180f),
    LEFT(0, -1, 270f);

    val vector: DirectionVector
        get() = DirectionVector(dr, dc, angleDeg)
}
