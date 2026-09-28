package com.vector.escape.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.vector.escape.engine.MoveValidator
import com.vector.escape.model.Arrow
import com.vector.escape.model.BoardState

@Composable
fun VectorBoardComposable(
    board: BoardState,
    onArrowTapped: (Arrow) -> Unit,
    activeHintArrowId: String? = null,
    modifier: Modifier = Modifier
) {
    val legalMoves = remember(board) { MoveValidator.getLegalMoves(board).map { it.id }.toSet() }
    val transition = rememberInfiniteTransition(label = "vehicle_world")
    val pulse by transition.animateFloat(
        0.72f, 1f,
        infiniteRepeatable(tween(850, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "legal_pulse"
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(board) {
                detectTapGestures { offset ->
                    val cellW = size.width / board.cols
                    val cellH = size.height / board.rows
                    val col = (offset.x / cellW).toInt().coerceIn(0, board.cols - 1)
                    val row = (offset.y / cellH).toInt().coerceIn(0, board.rows - 1)
                    board.getArrowAt(row, col)?.let(onArrowTapped)
                }
            }
    ) {
        val cellW = size.width / board.cols
        val cellH = size.height / board.rows
        val gap = 5.dp.toPx()
        val radius = 14.dp.toPx()

        // Complete miniature level ground.
        drawRoundRect(
            brush = Brush.linearGradient(
                listOf(Color(0xFF314155), Color(0xFF172231), Color(0xFF080E17)),
                start = Offset.Zero,
                end = Offset(size.width, size.height)
            ),
            topLeft = Offset.Zero,
            size = size,
            cornerRadius = CornerRadius(28.dp.toPx(), 28.dp.toPx())
        )
        drawRoundRect(
            color = Color.Black.copy(alpha = 0.55f),
            topLeft = Offset(0f, 5.dp.toPx()),
            size = Size(size.width, size.height - 1.dp.toPx()),
            cornerRadius = CornerRadius(28.dp.toPx(), 28.dp.toPx())
        )
        drawRoundRect(
            brush = Brush.linearGradient(listOf(Color(0xFF26374A), Color(0xFF0E1723))),
            topLeft = Offset(2.dp.toPx(), 1.dp.toPx()),
            size = Size(size.width - 4.dp.toPx(), size.height - 6.dp.toPx()),
            cornerRadius = CornerRadius(26.dp.toPx(), 26.dp.toPx())
        )

        for (r in 0 until board.rows) {
            for (c in 0 until board.cols) {
                val x = c * cellW + gap
                val y = r * cellH + gap
                val w = cellW - gap * 2
                val h = cellH - gap * 2
                val arrow = board.getArrowAt(r, c)
                val legal = arrow != null && legalMoves.contains(arrow.id)
                val hint = arrow != null && arrow.id == activeHintArrowId

                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.48f),
                    topLeft = Offset(x, y + 4.dp.toPx()),
                    size = Size(w, h),
                    cornerRadius = CornerRadius(radius, radius)
                )
                drawRoundRect(
                    brush = Brush.linearGradient(listOf(Color(0xFF1D2B3C), Color(0xFF0C1521))),
                    topLeft = Offset(x, y),
                    size = Size(w, h),
                    cornerRadius = CornerRadius(radius, radius)
                )

                // Road markings make each tile part of the same level.
                drawLine(
                    color = Color.White.copy(alpha = if (arrow == null) 0.10f else 0.035f),
                    start = Offset(x + w * 0.08f, y + h * 0.52f),
                    end = Offset(x + w * 0.92f, y + h * 0.52f),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = Color(0xFF7890A8).copy(alpha = 0.08f),
                    start = Offset(x + w * 0.52f, y + h * 0.08f),
                    end = Offset(x + w * 0.52f, y + h * 0.92f),
                    strokeWidth = 1.dp.toPx()
                )

                // Decorative obstructions: scenery only, never part of the puzzle collision rules.
                if (arrow == null && (r * 17 + c * 29 + board.rows) % 7 == 0) {
                    drawBarrier(x + w * 0.17f, y + h * 0.31f, w * 0.66f, h * 0.20f)
                } else if (arrow == null && (r * 11 + c * 13) % 11 == 0) {
                    drawCone(Offset(x + w * 0.72f, y + h * 0.70f), minOf(w, h) * 0.15f)
                } else if (arrow == null && (r + c) % 13 == 0) {
                    drawBollard(Offset(x + w * 0.25f, y + h * 0.70f), minOf(w, h) * 0.11f)
                }

                val border = when {
                    hint -> Color(0xFF42F5B3)
                    legal -> Color(0xFF00E5FF).copy(alpha = 0.72f)
                    else -> Color(0xFF7B8FA4).copy(alpha = 0.18f)
                }
                drawRoundRect(
                    color = border,
                    topLeft = Offset(x, y),
                    size = Size(w, h),
                    cornerRadius = CornerRadius(radius, radius),
                    style = Stroke(if (hint) 2.dp.toPx() else 1.dp.toPx())
                )

                if (arrow != null) {
                    val center = Offset(x + w / 2f, y + h / 2f)
                    val isBus = (arrow.row * 31 + arrow.col * 17 + arrow.id.hashCode()) % 5 == 0
                    val vehicleColor = when {
                        hint -> Color(0xFF19D8A6)
                        legal -> Color(0xFF12DFF4)
                        isBus -> Color(0xFFF2B84B)
                        else -> Color(0xFF4D9CFF)
                    }

                    if (legal) {
                        drawCircle(
                            color = vehicleColor.copy(alpha = 0.12f * pulse),
                            radius = minOf(w, h) * 0.44f,
                            center = center
                        )
                    }

                    rotate(degrees = arrow.direction.angleDeg, pivot = center) {
                        drawVehicle(center, w, h, vehicleColor, isBus, legal)
                    }

                    if (hint) {
                        drawRoundRect(
                            color = Color(0xFF42F5B3).copy(alpha = 0.75f),
                            topLeft = Offset(x - 2.dp.toPx(), y - 2.dp.toPx()),
                            size = Size(w + 4.dp.toPx(), h + 4.dp.toPx()),
                            cornerRadius = CornerRadius(radius + 2.dp.toPx(), radius + 2.dp.toPx()),
                            style = Stroke(2.dp.toPx())
                        )
                    }
                }
            }
        }

        // Perimeter infrastructure: this is the level environment, not a flat grid.
        drawStreetLight(Offset(size.width * 0.045f, size.height * 0.12f), size.minDimension * 0.045f)
        drawStreetLight(Offset(size.width * 0.955f, size.height * 0.88f), size.minDimension * 0.045f)
        drawBarrier(8.dp.toPx(), size.height - 20.dp.toPx(), size.width * 0.24f, 8.dp.toPx())
        drawBarrier(size.width * 0.68f, 12.dp.toPx(), size.width * 0.24f, 8.dp.toPx())
        drawRoundRect(
            color = Color.White.copy(alpha = 0.035f),
            topLeft = Offset(2.dp.toPx(), 2.dp.toPx()),
            size = Size(size.width - 4.dp.toPx(), size.height * 0.18f),
            cornerRadius = CornerRadius(26.dp.toPx(), 26.dp.toPx())
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawVehicle(
    center: Offset,
    cellW: Float,
    cellH: Float,
    color: Color,
    isBus: Boolean,
    legal: Boolean
) {
    val length = cellH * if (isBus) 0.78f else 0.70f
    val width = cellW * if (isBus) 0.50f else 0.43f
    val left = center.x - width / 2f
    val top = center.y - length / 2f
    val radius = width * 0.16f

    drawRoundRect(
        color = Color.Black.copy(alpha = 0.62f),
        topLeft = Offset(left + 3.dp.toPx(), top + 5.dp.toPx()),
        size = Size(width, length),
        cornerRadius = CornerRadius(radius, radius)
    )
    drawRoundRect(
        brush = Brush.linearGradient(
            listOf(color, color.copy(alpha = 0.78f), Color(0xFF07101A)),
            start = Offset(left, top),
            end = Offset(left + width, top + length)
        ),
        topLeft = Offset(left, top),
        size = Size(width, length),
        cornerRadius = CornerRadius(radius, radius)
    )

    val cabinTop = top + length * 0.22f
    val cabinHeight = length * 0.40f
    drawRoundRect(
        color = Color(0xFF071522).copy(alpha = 0.96f),
        topLeft = Offset(left + width * 0.14f, cabinTop),
        size = Size(width * 0.72f, cabinHeight),
        cornerRadius = CornerRadius(width * 0.12f, width * 0.12f)
    )

    if (isBus) {
        for (i in 0..2) {
            drawRoundRect(
                color = Color(0xFF8AEAF4).copy(alpha = 0.72f),
                topLeft = Offset(left + width * (0.17f + i * 0.23f), cabinTop + cabinHeight * 0.16f),
                size = Size(width * 0.17f, cabinHeight * 0.55f),
                cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
            )
        }
    } else {
        drawRoundRect(
            color = Color(0xFF8AEAF4).copy(alpha = 0.78f),
            topLeft = Offset(left + width * 0.22f, cabinTop + cabinHeight * 0.16f),
            size = Size(width * 0.56f, cabinHeight * 0.55f),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
        )
    }

    val wheelR = width * 0.10f
    for (wy in listOf(top + length * 0.18f, top + length * 0.82f)) {
        drawCircle(Color(0xFF05080C), wheelR, Offset(left + width * 0.12f, wy))
        drawCircle(Color(0xFF687786), wheelR * 0.43f, Offset(left + width * 0.12f, wy))
        drawCircle(Color(0xFF05080C), wheelR, Offset(left + width * 0.88f, wy))
        drawCircle(Color(0xFF687786), wheelR * 0.43f, Offset(left + width * 0.88f, wy))
    }

    // Nose points UP before the vehicle is rotated to its vector direction.
    val nose = Path().apply {
        moveTo(center.x, top + length * 0.03f)
        lineTo(center.x - width * 0.25f, top + length * 0.18f)
        lineTo(center.x + width * 0.25f, top + length * 0.18f)
        close()
    }
    drawPath(nose, color.copy(alpha = 0.98f))
    drawCircle(Color.White.copy(alpha = 0.90f), width * 0.055f, Offset(center.x - width * 0.23f, top + length * 0.10f))
    drawCircle(Color.White.copy(alpha = 0.90f), width * 0.055f, Offset(center.x + width * 0.23f, top + length * 0.10f))

    if (legal) {
        drawRoundRect(
            color = Color.White.copy(alpha = 0.42f),
            topLeft = Offset(left + 1.dp.toPx(), top + 1.dp.toPx()),
            size = Size(width - 2.dp.toPx(), length * 0.20f),
            cornerRadius = CornerRadius(radius, radius),
            style = Stroke(1.dp.toPx())
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBarrier(x: Float, y: Float, w: Float, h: Float) {
    drawRoundRect(
        color = Color.Black.copy(alpha = 0.55f),
        topLeft = Offset(x + 2.dp.toPx(), y + 3.dp.toPx()),
        size = Size(w, h),
        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
    )
    drawRoundRect(
        brush = Brush.linearGradient(listOf(Color(0xFFF1B84B), Color(0xFFB5651A))),
        topLeft = Offset(x, y),
        size = Size(w, h),
        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
    )
    val stripe = w / 5f
    for (i in 0..4) {
        drawLine(
            color = Color(0xFF22272D),
            start = Offset(x + i * stripe, y),
            end = Offset(x + i * stripe + h, y + h),
            strokeWidth = maxOf(1.dp.toPx(), h * 0.18f)
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCone(center: Offset, radius: Float) {
    val cone = Path().apply {
        moveTo(center.x, center.y - radius * 1.6f)
        lineTo(center.x - radius, center.y + radius)
        lineTo(center.x + radius, center.y + radius)
        close()
    }
    drawPath(cone, Brush.linearGradient(listOf(Color(0xFFFFB43B), Color(0xFFD85B1A))))
    drawLine(
        color = Color.White.copy(alpha = 0.82f),
        start = Offset(center.x - radius * 0.52f, center.y),
        end = Offset(center.x + radius * 0.52f, center.y),
        strokeWidth = radius * 0.22f
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBollard(center: Offset, radius: Float) {
    drawCircle(Color.Black.copy(alpha = 0.45f), radius * 1.15f, Offset(center.x + 2.dp.toPx(), center.y + 3.dp.toPx()))
    drawRoundRect(
        color = Color(0xFFE7EEF5),
        topLeft = Offset(center.x - radius * 0.55f, center.y - radius),
        size = Size(radius * 1.1f, radius * 2f),
        cornerRadius = CornerRadius(radius * 0.25f, radius * 0.25f)
    )
    drawCircle(Color(0xFF00DDF5), radius * 0.22f, Offset(center.x, center.y - radius * 0.45f))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawStreetLight(base: Offset, radius: Float) {
    val poleHeight = radius * 4f
    drawLine(Color(0xFF0A0F15), base, Offset(base.x, base.y - poleHeight), strokeWidth = radius * 0.35f)
    drawCircle(Color(0xFF6FF8FF).copy(alpha = 0.20f), radius * 1.45f, Offset(base.x, base.y - poleHeight))
    drawCircle(Color(0xFFDFFBFF), radius * 0.42f, Offset(base.x, base.y - poleHeight))
}
