package com.vector.escape.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
    exitingArrowId: String? = null,
    exitProgress: Float = 0f,
    interactionEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val legalMoves = remember(board) { MoveValidator.getLegalMoves(board).map { it.id }.toSet() }
    val transition = rememberInfiniteTransition(label = "world_lights")
    val pulse by transition.animateFloat(
        0.72f, 1f,
        infiniteRepeatable(
            tween(900, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "legal_pulse"
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(board, interactionEnabled) {
                if (!interactionEnabled) return@pointerInput
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
        val margin = 7.dp.toPx()
        val radius = 16.dp.toPx()

        // A complete toy-like parking/road world rather than isolated tiles.
        drawRoundRect(
            brush = Brush.linearGradient(
                listOf(
                    Color(0xFF8B949E),
                    Color(0xFF68727D),
                    Color(0xFF3E4853)
                ),
                start = Offset.Zero,
                end = Offset(size.width, size.height)
            ),
            topLeft = Offset.Zero,
            size = size,
            cornerRadius = CornerRadius(28.dp.toPx(), 28.dp.toPx())
        )

        // Raised curb / road island.
        drawRoundRect(
            color = Color(0xFF1D242C),
            topLeft = Offset(6.dp.toPx(), 6.dp.toPx()),
            size = Size(size.width - 12.dp.toPx(), size.height - 12.dp.toPx()),
            cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx())
        )
        drawRoundRect(
            brush = Brush.linearGradient(
                listOf(Color(0xFFF6C33A), Color(0xFF121820), Color(0xFFF6C33A)),
                start = Offset.Zero,
                end = Offset(size.width, size.height)
            ),
            topLeft = Offset(8.dp.toPx(), 8.dp.toPx()),
            size = Size(size.width - 16.dp.toPx(), size.height - 16.dp.toPx()),
            cornerRadius = CornerRadius(22.dp.toPx(), 22.dp.toPx()),
            style = Stroke(4.dp.toPx())
        )

        // Parking bays and road lanes.
        for (r in 0 until board.rows) {
            for (c in 0 until board.cols) {
                val x = c * cellW + margin
                val y = r * cellH + margin
                val w = cellW - margin * 2
                val h = cellH - margin * 2

                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.28f),
                    topLeft = Offset(x + 2.dp.toPx(), y + 5.dp.toPx()),
                    size = Size(w, h),
                    cornerRadius = CornerRadius(radius, radius)
                )
                drawRoundRect(
                    brush = Brush.linearGradient(
                        listOf(Color(0xFF465563), Color(0xFF293642), Color(0xFF1B252F)),
                        start = Offset(x, y),
                        end = Offset(x + w, y + h)
                    ),
                    topLeft = Offset(x, y),
                    size = Size(w, h),
                    cornerRadius = CornerRadius(radius, radius)
                )

                // White parking-space markings.
                drawLine(
                    color = Color.White.copy(alpha = 0.23f),
                    start = Offset(x + w * 0.10f, y + h * 0.82f),
                    end = Offset(x + w * 0.90f, y + h * 0.82f),
                    strokeWidth = 1.5.dp.toPx()
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.13f),
                    start = Offset(x + w * 0.50f, y + h * 0.08f),
                    end = Offset(x + w * 0.50f, y + h * 0.82f),
                    strokeWidth = 1.dp.toPx()
                )

                // Functional-looking lane guides: each vehicle is visually aligned
                // to a clear escape corridor.
                drawLine(
                    color = Color(0xFFB8C2CC).copy(alpha = 0.08f),
                    start = Offset(x + w * 0.08f, y + h * 0.50f),
                    end = Offset(x + w * 0.92f, y + h * 0.50f),
                    strokeWidth = 1.dp.toPx()
                )

                // Roadside objects are integrated into the parking world.
                if (arrowAt(board, r, c) == null) {
                    val pattern = (r * 19 + c * 31 + board.rows * 7) % 9
                    when (pattern) {
                        0 -> drawParkingBlock(Offset(x + w * 0.16f, y + h * 0.17f), w * 0.66f, h * 0.10f)
                        3 -> drawCone(Offset(x + w * 0.77f, y + h * 0.68f), minOf(w, h) * 0.11f)
                        6 -> drawBollard(Offset(x + w * 0.23f, y + h * 0.65f), minOf(w, h) * 0.09f)
                    }
                }

                val arrow = arrowAt(board, r, c)
                if (arrow != null) {
                    val legal = legalMoves.contains(arrow.id)
                    val hint = arrow.id == activeHintArrowId
                    val isExiting = arrow.id == exitingArrowId

                    val vehicleCenter = Offset(
                        x + w / 2f + if (isExiting) exitDx(arrow, cellW, cellH, exitProgress) else 0f,
                        y + h / 2f + if (isExiting) exitDy(arrow, cellW, cellH, exitProgress) else 0f
                    )

                    val vehicleColor = vehiclePalette(arrow.id)
                    if (legal && !isExiting) {
                        drawCircle(
                            color = vehicleColor.copy(alpha = 0.15f * pulse),
                            radius = minOf(w, h) * 0.49f,
                            center = vehicleCenter
                        )
                    }

                    if (isExiting) {
                        drawExitTrail(
                            center = vehicleCenter,
                            direction = arrow.direction,
                            length = minOf(w, h) * (0.85f + exitProgress * 1.4f),
                            alpha = 0.32f * (1f - exitProgress * 0.35f)
                        )
                    }

                    rotate(
                        degrees = arrow.direction.angleDeg,
                        pivot = vehicleCenter
                    ) {
                        drawToyVehicle(
                            center = vehicleCenter,
                            cellW = w,
                            cellH = h,
                            color = vehicleColor,
                            bus = isBus(arrow),
                            pulse = if (legal || hint) pulse else 1f,
                            exitProgress = if (isExiting) exitProgress else 0f
                        )
                    }

                    val borderColor = when {
                        hint -> Color(0xFF42F5B3)
                        legal -> Color(0xFF00E5FF).copy(alpha = 0.78f)
                        else -> Color.White.copy(alpha = 0.12f)
                    }
                    drawRoundRect(
                        color = borderColor,
                        topLeft = Offset(x, y),
                        size = Size(w, h),
                        cornerRadius = CornerRadius(radius, radius),
                        style = Stroke(if (hint) 2.5.dp.toPx() else 1.dp.toPx())
                    )
                }
            }
        }

        // Exit gates make the destination visually obvious.
        drawExitGate(Offset(size.width / 2f, 2.dp.toPx()), horizontal = true)
        drawExitGate(Offset(size.width / 2f, size.height - 2.dp.toPx()), horizontal = true)
        drawExitGate(Offset(2.dp.toPx(), size.height / 2f), horizontal = false)
        drawExitGate(Offset(size.width - 2.dp.toPx(), size.height / 2f), horizontal = false)

        // Soft highlight across the world gives it a toy-rendered finish.
        drawRoundRect(
            color = Color.White.copy(alpha = 0.055f),
            topLeft = Offset(12.dp.toPx(), 12.dp.toPx()),
            size = Size(size.width - 24.dp.toPx(), size.height * 0.20f),
            cornerRadius = CornerRadius(20.dp.toPx(), 20.dp.toPx())
        )
    }
}

private fun arrowAt(board: BoardState, r: Int, c: Int): Arrow? = board.getArrowAt(r, c)

private fun isBus(arrow: Arrow): Boolean {
    return (arrow.id.hashCode().ushr(2) % 5) == 0
}

private fun vehiclePalette(id: String): Color {
    return when (id.hashCode().ushr(1) % 6) {
        0 -> Color(0xFF23D5FF)
        1 -> Color(0xFFFF4F64)
        2 -> Color(0xFFFFB62E)
        3 -> Color(0xFF9B5CFF)
        4 -> Color(0xFF41E35A)
        else -> Color(0xFFFF6BC5)
    }
}

private fun exitDx(arrow: Arrow, cellW: Float, cellH: Float, p: Float): Float {
    return when (arrow.direction) {
        com.vector.escape.model.Direction.LEFT -> -cellW * (0.55f + p * 1.35f)
        com.vector.escape.model.Direction.RIGHT -> cellW * (0.55f + p * 1.35f)
        else -> 0f
    }
}

private fun exitDy(arrow: Arrow, cellW: Float, cellH: Float, p: Float): Float {
    return when (arrow.direction) {
        com.vector.escape.model.Direction.UP -> -cellH * (0.55f + p * 1.35f)
        com.vector.escape.model.Direction.DOWN -> cellH * (0.55f + p * 1.35f)
        else -> 0f
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawToyVehicle(
    center: Offset,
    cellW: Float,
    cellH: Float,
    color: Color,
    bus: Boolean,
    pulse: Float,
    exitProgress: Float
) {
    val bodyW = cellW * if (bus) 0.60f else 0.57f
    val bodyH = cellH * if (bus) 0.88f else 0.78f
    val left = center.x - bodyW / 2f
    val top = center.y - bodyH / 2f
    val r = bodyW * 0.15f
    val fade = 1f - exitProgress * 0.18f

    // Ground shadow.
    drawRoundRect(
        color = Color.Black.copy(alpha = 0.48f * fade),
        topLeft = Offset(left + 5.dp.toPx(), top + 8.dp.toPx()),
        size = Size(bodyW, bodyH),
        cornerRadius = CornerRadius(r, r)
    )

    // Lower chassis / 3D extrusion.
    drawRoundRect(
        color = Color(0xFF10161D).copy(alpha = 0.92f),
        topLeft = Offset(left - 1.dp.toPx(), top + 6.dp.toPx()),
        size = Size(bodyW + 2.dp.toPx(), bodyH),
        cornerRadius = CornerRadius(r, r)
    )

    // Main glossy body.
    drawRoundRect(
        brush = Brush.linearGradient(
            listOf(
                color.copy(alpha = fade),
                color.copy(alpha = 0.82f * fade),
                Color(0xFF26313C).copy(alpha = fade)
            ),
            start = Offset(left, top),
            end = Offset(left + bodyW, top + bodyH)
        ),
        topLeft = Offset(left, top),
        size = Size(bodyW, bodyH),
        cornerRadius = CornerRadius(r, r)
    )

    // Hood and roof create the readable 3D toy-car silhouette.
    val hoodH = bodyH * 0.18f
    drawRoundRect(
        color = Color.White.copy(alpha = 0.11f * fade),
        topLeft = Offset(left + bodyW * 0.13f, top + bodyH * 0.08f),
        size = Size(bodyW * 0.74f, hoodH),
        cornerRadius = CornerRadius(r * 0.6f, r * 0.6f)
    )

    val roofTop = top + bodyH * 0.27f
    val roofH = bodyH * 0.38f
    val roofW = bodyW * if (bus) 0.82f else 0.70f
    drawRoundRect(
        color = Color(0xFF0C1822).copy(alpha = 0.98f),
        topLeft = Offset(center.x - roofW / 2f, roofTop),
        size = Size(roofW, roofH),
        cornerRadius = CornerRadius(r * 0.75f, r * 0.75f)
    )

    // Windows: bright enough to read as a real vehicle at phone scale.
    if (bus) {
        for (i in 0..2) {
            val wx = center.x - roofW * 0.38f + i * roofW * 0.29f
            drawRoundRect(
                color = Color(0xFFB9F5FF).copy(alpha = 0.72f),
                topLeft = Offset(wx, roofTop + roofH * 0.16f),
                size = Size(roofW * 0.21f, roofH * 0.57f),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
        }
    } else {
        drawRoundRect(
            color = Color(0xFFB9F5FF).copy(alpha = 0.78f),
            topLeft = Offset(center.x - roofW * 0.36f, roofTop + roofH * 0.14f),
            size = Size(roofW * 0.72f, roofH * 0.60f),
            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
        )
        drawLine(
            color = Color.White.copy(alpha = 0.38f),
            start = Offset(center.x, roofTop + roofH * 0.15f),
            end = Offset(center.x, roofTop + roofH * 0.73f),
            strokeWidth = 1.2.dp.toPx()
        )
    }

    // Side mirrors.
    drawCircle(color.copy(alpha = 0.95f), bodyW * 0.055f, Offset(left - bodyW * 0.01f, center.y - bodyH * 0.12f))
    drawCircle(color.copy(alpha = 0.95f), bodyW * 0.055f, Offset(left + bodyW * 1.01f, center.y - bodyH * 0.12f))

    // Wheels with rims.
    val wheelR = bodyW * 0.105f
    for (wy in listOf(top + bodyH * 0.22f, top + bodyH * 0.78f)) {
        drawCircle(Color(0xFF080B0F), wheelR, Offset(left + bodyW * 0.07f, wy))
        drawCircle(Color(0xFFB7C0C8), wheelR * 0.45f, Offset(left + bodyW * 0.07f, wy))
        drawCircle(Color(0xFF080B0F), wheelR, Offset(left + bodyW * 0.93f, wy))
        drawCircle(Color(0xFFB7C0C8), wheelR * 0.45f, Offset(left + bodyW * 0.93f, wy))
    }

    // Front bumper, lights and rear light strip.
    drawRoundRect(
        color = Color.White.copy(alpha = 0.92f),
        topLeft = Offset(left + bodyW * 0.16f, top + bodyH * 0.045f),
        size = Size(bodyW * 0.24f, bodyH * 0.065f),
        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
    )
    drawRoundRect(
        color = Color.White.copy(alpha = 0.92f),
        topLeft = Offset(left + bodyW * 0.60f, top + bodyH * 0.045f),
        size = Size(bodyW * 0.24f, bodyH * 0.065f),
        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
    )
    drawRoundRect(
        color = Color(0xFFFF324A).copy(alpha = 0.85f),
        topLeft = Offset(left + bodyW * 0.22f, top + bodyH * 0.89f),
        size = Size(bodyW * 0.56f, bodyH * 0.045f),
        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
    )

    // Subtle animated energy under the legal vehicle.
    if (pulse > 0.8f) {
        drawCircle(
            color = Color.White.copy(alpha = 0.12f * pulse),
            radius = bodyW * 0.12f,
            center = Offset(center.x, top + bodyH * 0.12f)
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawExitTrail(
    center: Offset,
    direction: com.vector.escape.model.Direction,
    length: Float,
    alpha: Float
) {
    val dx = direction.dc.toFloat()
    val dy = direction.dr.toFloat()
    val start = Offset(center.x - dx * length * 0.45f, center.y - dy * length * 0.45f)
    val end = Offset(center.x - dx * length, center.y - dy * length)
    drawLine(
        brush = Brush.linearGradient(
            listOf(Color(0xFF6CF6FF).copy(alpha = alpha), Color.Transparent)
        ),
        start = start,
        end = end,
        strokeWidth = 6.dp.toPx(),
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color.White.copy(alpha = alpha * 0.55f),
        start = start,
        end = end,
        strokeWidth = 1.5.dp.toPx(),
        cap = StrokeCap.Round
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawParkingBlock(
    center: Offset,
    width: Float,
    height: Float
) {
    val left = center.x - width / 2f
    val top = center.y - height / 2f
    drawRoundRect(
        color = Color.Black.copy(alpha = 0.35f),
        topLeft = Offset(left + 3.dp.toPx(), top + 4.dp.toPx()),
        size = Size(width, height),
        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
    )
    drawRoundRect(
        brush = Brush.horizontalGradient(listOf(Color(0xFFFFD33D), Color(0xFFEB8E1A))),
        topLeft = Offset(left, top),
        size = Size(width, height),
        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
    )
    for (i in 0..5) {
        drawLine(
            color = Color(0xFF252A30),
            start = Offset(left + i * width / 5f, top),
            end = Offset(left + i * width / 5f + height, top + height),
            strokeWidth = maxOf(1.dp.toPx(), height * 0.18f)
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCone(center: Offset, radius: Float) {
    val cone = Path().apply {
        moveTo(center.x, center.y - radius * 1.7f)
        lineTo(center.x - radius, center.y + radius)
        lineTo(center.x + radius, center.y + radius)
        close()
    }
    drawPath(cone, Brush.verticalGradient(listOf(Color(0xFFFFD43B), Color(0xFFF06A1A))))
    drawLine(
        color = Color.White.copy(alpha = 0.9f),
        start = Offset(center.x - radius * 0.52f, center.y),
        end = Offset(center.x + radius * 0.52f, center.y),
        strokeWidth = radius * 0.22f
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBollard(center: Offset, radius: Float) {
    drawCircle(Color.Black.copy(alpha = 0.38f), radius * 1.15f, Offset(center.x + 2.dp.toPx(), center.y + 3.dp.toPx()))
    drawRoundRect(
        color = Color(0xFFE9EEF4),
        topLeft = Offset(center.x - radius * 0.55f, center.y - radius),
        size = Size(radius * 1.1f, radius * 2f),
        cornerRadius = CornerRadius(radius * 0.25f, radius * 0.25f)
    )
    drawCircle(Color(0xFFFFC43A), radius * 0.22f, Offset(center.x, center.y - radius * 0.45f))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawExitGate(
    center: Offset,
    horizontal: Boolean
) {
    val length = 44.dp.toPx()
    val width = 6.dp.toPx()
    if (horizontal) {
        drawRoundRect(
            brush = Brush.horizontalGradient(listOf(Color(0xFF1B2229), Color(0xFFF4C33A), Color(0xFF1B2229))),
            topLeft = Offset(center.x - length / 2f, center.y - width / 2f),
            size = Size(length, width),
            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
        )
    } else {
        drawRoundRect(
            brush = Brush.verticalGradient(listOf(Color(0xFF1B2229), Color(0xFFF4C33A), Color(0xFF1B2229))),
            topLeft = Offset(center.x - width / 2f, center.y - length / 2f),
            size = Size(width, length),
            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
        )
    }
}
