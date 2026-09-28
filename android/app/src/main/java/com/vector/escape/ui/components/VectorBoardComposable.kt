package com.vector.escape.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.vector.escape.engine.MoveValidator
import com.vector.escape.model.Arrow
import com.vector.escape.model.BoardState
import com.vector.escape.model.Direction
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun VectorBoardComposable(
    board: BoardState,
    onArrowTapped: (Arrow) -> Unit,
    activeHintArrowId: String? = null,
    modifier: Modifier = Modifier
) {
    val legalMoves = remember(board) { MoveValidator.getLegalMoves(board).map { it.id }.toSet() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(16.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(board) {
                    detectTapGestures { offset ->
                        val cellW = size.width / board.cols
                        val cellH = size.height / board.rows
                        val col = (offset.x / cellW).toInt().coerceIn(0, board.cols - 1)
                        val row = (offset.y / cellH).toInt().coerceIn(0, board.rows - 1)
                        val arrow = board.getArrowAt(row, col)
                        if (arrow != null) {
                            onArrowTapped(arrow)
                        }
                    }
                }
        ) {
            val cellW = size.width / board.cols
            val cellH = size.height / board.rows
            val gap = 4.dp.toPx()

            // Draw board grid
            for (r in 0 until board.rows) {
                for (c in 0 until board.cols) {
                    val x = c * cellW + gap
                    val y = r * cellH + gap
                    val w = cellW - gap * 2
                    val h = cellH - gap * 2

                    val arrow = board.getArrowAt(r, c)
                    val isLegal = arrow != null && legalMoves.contains(arrow.id)
                    val isHint = arrow != null && arrow.id == activeHintArrowId

                    // Cell tile background
                    val tileColor = when {
                        isHint -> Color(0x3310B981)
                        arrow != null -> Color(0xFF121D33)
                        else -> Color(0x660B1220)
                    }

                    drawRoundRect(
                        color = tileColor,
                        topLeft = Offset(x, y),
                        size = Size(w, h),
                        cornerRadius = CornerRadius(14f, 14f)
                    )

                    // Cell border
                    val borderColor = when {
                        isHint -> Color(0xFF10B981)
                        isLegal -> Color(0x5500F0FF)
                        arrow != null -> Color(0x3300F0FF)
                        else -> Color(0x22334155)
                    }

                    drawRoundRect(
                        color = borderColor,
                        topLeft = Offset(x, y),
                        size = Size(w, h),
                        cornerRadius = CornerRadius(14f, 14f),
                        style = Stroke(width = if (isHint || isLegal) 2.dp.toPx() else 1.dp.toPx())
                    )

                    // Render Vector Arrow if present
                    if (arrow != null) {
                        val centerX = x + w / 2f
                        val centerY = y + h / 2f
                        val arrowColor = when {
                            isHint -> Color(0xFF10B981)
                            isLegal -> Color(0xFF00F0FF)
                            else -> Color(0xFF60A5FA)
                        }

                        val angleRad = (arrow.direction.angleDeg - 90f) * (PI / 180f).toFloat()
                        val shaftLen = minOf(w, h) * 0.28f

                        val dx = cos(angleRad) * shaftLen
                        val dy = sin(angleRad) * shaftLen

                        // Arrow shaft
                        drawLine(
                            color = arrowColor,
                            start = Offset(centerX - dx * 0.7f, centerY - dy * 0.7f),
                            end = Offset(centerX + dx * 0.7f, centerY + dy * 0.7f),
                            strokeWidth = 3.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )

                        // Arrowhead chevron
                        val tipX = centerX + dx * 0.75f
                        val tipY = centerY + dy * 0.75f
                        val headLen = minOf(w, h) * 0.16f
                        val wingAngle1 = angleRad + (140f * (PI / 180f)).toFloat()
                        val wingAngle2 = angleRad - (140f * (PI / 180f)).toFloat()

                        val wing1X = tipX + cos(wingAngle1) * headLen
                        val wing1Y = tipY + sin(wingAngle1) * headLen
                        val wing2X = tipX + cos(wingAngle2) * headLen
                        val wing2Y = tipY + sin(wingAngle2) * headLen

                        val path = Path().apply {
                            moveTo(wing1X, wing1Y)
                            lineTo(tipX, tipY)
                            lineTo(wing2X, wing2Y)
                        }

                        drawPath(
                            path = path,
                            color = arrowColor,
                            style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Glowing energy core
                        drawCircle(
                            color = arrowColor,
                            radius = 2.dp.toPx(),
                            center = Offset(centerX - dx * 0.5f, centerY - dy * 0.5f)
                        )
                    }
                }
            }
        }
    }
}
