package com.vector.escape.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.vector.escape.engine.MoveValidator
import com.vector.escape.model.Arrow
import com.vector.escape.model.BoardState
import com.vector.escape.model.Direction
import kotlin.math.max
import kotlin.math.min

private enum class VehicleType {
    HATCHBACK, SEDAN, SUV, TAXI, PICKUP, BUS
}

private data class VehicleStyle(
    val type: VehicleType,
    val width: Float,
    val length: Float,
    val body: Color,
    val roof: Color
)

@Composable
fun VectorBoardComposable(
    board: BoardState,
    onArrowTapped: (Arrow) -> Unit,
    onArrowSwiped: ((Arrow, Int) -> Unit)? = null,
    activeHintArrowId: String? = null,
    impactArrowId: String? = null,
    impactProgress: Float = 0f,
    exitingArrowId: String? = null,
    exitProgress: Float = 0f,
    interactionEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val legalMoves = remember(board) {
        MoveValidator.getLegalMoves(board).map { it.id }.toSet()
    }
    val transition = rememberInfiniteTransition(label = "road_lights")
    val pulse by transition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
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
            .pointerInput(board, interactionEnabled, onArrowSwiped) {
                if (!interactionEnabled || onArrowSwiped == null) return@pointerInput
                var selected: Arrow? = null
                var totalDrag = Offset.Zero
                detectDragGestures(
                    onDragStart = { offset ->
                        val cellW = size.width / board.cols
                        val cellH = size.height / board.rows
                        val col = (offset.x / cellW).toInt().coerceIn(0, board.cols - 1)
                        val row = (offset.y / cellH).toInt().coerceIn(0, board.rows - 1)
                        selected = board.getArrowAt(row, col)
                        totalDrag = Offset.Zero
                    },
                    onDrag = { change, amount ->
                        change.consume()
                        totalDrag += amount
                    },
                    onDragCancel = {
                        selected = null
                        totalDrag = Offset.Zero
                    },
                    onDragEnd = {
                        val arrow = selected
                        val minDistance = min(size.width, size.height) * 0.04f
                        if (arrow != null && totalDrag.getDistance() >= minDistance) {
                            val drag = totalDrag / totalDrag.getDistance()
                            val forward = when (arrow.direction) {
                                Direction.UP -> Offset(0f, -1f)
                                Direction.RIGHT -> Offset(1f, 0f)
                                Direction.DOWN -> Offset(0f, 1f)
                                Direction.LEFT -> Offset(-1f, 0f)
                            }
                            val alignment = drag.x * forward.x + drag.y * forward.y
                            if (kotlin.math.abs(alignment) >= 0.707f) {
                                onArrowSwiped(arrow, if (alignment >= 0f) 1 else -1)
                            }
                        }
                        selected = null
                        totalDrag = Offset.Zero
                    }
                )
            }
    ) {
        val cellW = size.width / board.cols
        val cellH = size.height / board.rows
        val edge = 7.dp.toPx()
        val road = Size(size.width - edge * 2f, size.height - edge * 2f)

        // One continuous asphalt surface. No fake raised blocks.
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF56616B),
                    Color(0xFF343E47),
                    Color(0xFF252E36)
                ),
                start = Offset.Zero,
                end = Offset(size.width, size.height)
            ),
            topLeft = Offset(edge, edge),
            size = road,
            cornerRadius = CornerRadius(26.dp.toPx(), 26.dp.toPx())
        )

        // Asphalt depth and curb.
        drawRoundRect(
            color = Color(0xFF10171D).copy(alpha = 0.60f),
            topLeft = Offset(edge + 3.dp.toPx(), edge + 7.dp.toPx()),
            size = Size(road.width - 6.dp.toPx(), road.height - 4.dp.toPx()),
            cornerRadius = CornerRadius(25.dp.toPx(), 25.dp.toPx())
        )
        drawRoundRect(
            color = Color(0xFF6E7A84),
            topLeft = Offset(edge, edge),
            size = road,
            cornerRadius = CornerRadius(26.dp.toPx(), 26.dp.toPx()),
            style = Stroke(2.5.dp.toPx())
        )

        // Yellow/black roadside curb. This is a road edge, not a puzzle block.
        drawCurb(Offset(edge + 3.dp.toPx(), edge + 3.dp.toPx()), road)

        // Real road markings: broad lanes plus subtle parking/grid guides.
        for (r in 1 until board.rows) {
            val y = r * cellH
            drawLine(
                color = Color.White.copy(alpha = 0.10f),
                start = Offset(edge + 12.dp.toPx(), y),
                end = Offset(size.width - edge - 12.dp.toPx(), y),
                strokeWidth = 1.5.dp.toPx()
            )
            drawDashedLine(
                color = Color(0xFFE9EEF2).copy(alpha = 0.12f),
                start = Offset(edge + 18.dp.toPx(), y),
                end = Offset(size.width - edge - 18.dp.toPx(), y),
                dash = 12.dp.toPx(),
                gap = 18.dp.toPx(),
                strokeWidth = 1.dp.toPx()
            )
        }
        for (c in 1 until board.cols) {
            val x = c * cellW
            drawLine(
                color = Color.White.copy(alpha = 0.055f),
                start = Offset(x, edge + 12.dp.toPx()),
                end = Offset(x, size.height - edge - 12.dp.toPx()),
                strokeWidth = 1.2.dp.toPx()
            )
        }

        // Functional escape-lane visualization. It shows the actual route
        // the selected vehicle can use, instead of decorative obstacles.
        for (arrow in board.arrows) {
            val legal = legalMoves.contains(arrow.id)
            val isExiting = arrow.id == exitingArrowId
            val center = cellCenter(arrow, cellW, cellH)
            val laneColor = when {
                isExiting -> Color(0xFF69F7FF)
                legal -> Color(0xFF49E89A)
                else -> Color(0xFFEF5350)
            }
            val alpha = if (legal || isExiting) 0.20f else 0.065f
            drawEscapeLane(
                center = center,
                direction = arrow.direction,
                boardWidth = size.width,
                boardHeight = size.height,
                color = laneColor.copy(alpha = alpha),
                strokeWidth = if (legal || isExiting) 3.dp.toPx() else 1.dp.toPx()
            )
        }

        // Vehicles are the puzzle pieces. Their silhouettes, proportions and
        // details deliberately vary so the board reads as a traffic scene.
        for (arrow in board.arrows) {
            val r = arrow.row
            val c = arrow.col
            val x = c * cellW
            val y = r * cellH
            val legal = legalMoves.contains(arrow.id)
            val hint = arrow.id == activeHintArrowId
            val isExiting = arrow.id == exitingArrowId
            val isImpacting = arrow.id == impactArrowId && !isExiting
            val style = vehicleStyle(arrow.id)

            val baseCenter = Offset(x + cellW / 2f, y + cellH / 2f)
            val center = if (isExiting) {
                baseCenter + exitOffset(
                    arrow = arrow,
                    board = board,
                    cellW = cellW,
                    cellH = cellH,
                    progress = exitProgress
                )
            } else if (isImpacting) {
                val lunge = 0.13f * kotlin.math.sin(impactProgress * kotlin.math.PI).toFloat()
                baseCenter + Offset(arrow.direction.dc * cellW * lunge, arrow.direction.dr * cellH * lunge)
            } else {
                baseCenter
            }

            if (legal && !isExiting) {
                drawCircle(
                    color = Color(0xFF50F5B4).copy(alpha = 0.11f * pulse),
                    radius = min(cellW, cellH) * 0.44f,
                    center = center
                )
            }
            if (hint && !isExiting) {
                drawRoundRect(
                    color = Color(0xFF42F5B3).copy(alpha = 0.14f),
                    topLeft = Offset(x + 4.dp.toPx(), y + 4.dp.toPx()),
                    size = Size(cellW - 8.dp.toPx(), cellH - 8.dp.toPx()),
                    cornerRadius = CornerRadius(18.dp.toPx(), 18.dp.toPx())
                )
            }

            if (isExiting) {
                drawExitTrail(
                    center = center,
                    direction = arrow.direction,
                    progress = exitProgress,
                    cellW = cellW,
                    cellH = cellH
                )
            }

            rotate(degrees = arrow.direction.angleDeg, pivot = center) {
                drawRealVehicle(
                    center = center,
                    cellW = cellW,
                    cellH = cellH,
                    style = style,
                    pulse = if (legal || hint) pulse else 1f,
                    exitProgress = if (isExiting) exitProgress else 0f,
                    impactProgress = if (isImpacting) impactProgress else 0f
                )
            }

            val border = when {
                hint -> Color(0xFF42F5B3)
                legal -> Color(0xFF23DDF7).copy(alpha = 0.82f)
                else -> Color.White.copy(alpha = 0.07f)
            }
            drawRoundRect(
                color = border,
                topLeft = Offset(x + 3.dp.toPx(), y + 3.dp.toPx()),
                size = Size(cellW - 6.dp.toPx(), cellH - 6.dp.toPx()),
                cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                style = Stroke(if (hint) 2.5.dp.toPx() else 1.dp.toPx())
            )
        }

        // Directional exits are part of the gameplay presentation.
        drawExitGate(Offset(size.width / 2f, edge + 1.dp.toPx()), true)
        drawExitGate(Offset(size.width / 2f, size.height - edge - 1.dp.toPx()), true)
        drawExitGate(Offset(edge + 1.dp.toPx(), size.height / 2f), false)
        drawExitGate(Offset(size.width - edge - 1.dp.toPx(), size.height / 2f), false)

        // Small asphalt highlights keep the road from looking like a flat grid.
        for (i in 0 until 16) {
            val px = 22.dp.toPx() + ((i * 73) % max(1, size.width.toInt() - 44.dp.toPx().toInt()))
            val py = 18.dp.toPx() + ((i * 113) % max(1, size.height.toInt() - 36.dp.toPx().toInt()))
            drawCircle(
                color = Color.White.copy(alpha = 0.018f),
                radius = 1.2.dp.toPx(),
                center = Offset(px, py)
            )
        }
    }
}

private fun cellCenter(arrow: Arrow, cellW: Float, cellH: Float): Offset =
    Offset((arrow.col + 0.5f) * cellW, (arrow.row + 0.5f) * cellH)

private fun exitOffset(
    arrow: Arrow,
    board: BoardState,
    cellW: Float,
    cellH: Float,
    progress: Float
): Offset {
    val extra = 1.25f
    return when (arrow.direction) {
        Direction.LEFT -> Offset(-(arrow.col + extra) * cellW * progress, 0f)
        Direction.RIGHT -> Offset((board.cols - arrow.col - 1 + extra) * cellW * progress, 0f)
        Direction.UP -> Offset(0f, -(arrow.row + extra) * cellH * progress)
        Direction.DOWN -> Offset(0f, (board.rows - arrow.row - 1 + extra) * cellH * progress)
    }
}

private fun vehicleStyle(id: String): VehicleStyle {
    val h = id.hashCode().ushr(1)
    return when (h % 6) {
        0 -> VehicleStyle(VehicleType.BUS, 0.78f, 0.96f, Color(0xFFFFD200), Color(0xFF17303A))
        1 -> VehicleStyle(VehicleType.SEDAN, 0.58f, 0.78f, Color(0xFF00A3FF), Color(0xFF101820))
        2 -> VehicleStyle(VehicleType.SUV, 0.68f, 0.84f, Color(0xFF2DFE54), Color(0xFF101820))
        3 -> VehicleStyle(VehicleType.TAXI, 0.58f, 0.77f, Color(0xFFFF7A00), Color(0xFF111820))
        4 -> VehicleStyle(VehicleType.HATCHBACK, 0.53f, 0.68f, Color(0xFFFF2222), Color(0xFF101820))
        else -> VehicleStyle(VehicleType.PICKUP, 0.64f, 0.86f, Color(0xFFFF1988), Color(0xFF101820))
    }
}

private fun DrawScope.drawRealVehicle(
    center: Offset,
    cellW: Float,
    cellH: Float,
    style: VehicleStyle,
    pulse: Float,
    exitProgress: Float,
    impactProgress: Float
) {
    val maxW = cellW * style.width
    val maxH = cellH * style.length
    val left = center.x - maxW / 2f
    val top = center.y - maxH / 2f
    val radius = min(maxW, maxH) * 0.13f
    val fade = 1f - exitProgress * 0.16f
    val squash = kotlin.math.sin(impactProgress * kotlin.math.PI).toFloat()
    val visualScaleX = 1f + squash * 0.10f
    val visualScaleY = 1f - squash * 0.08f

    scale(scaleX = visualScaleX, scaleY = visualScaleY, pivot = center) {
    // Soft contact shadow.
    drawRoundRect(
        color = Color.Black.copy(alpha = 0.48f * fade),
        topLeft = Offset(left + 4.dp.toPx(), top + 7.dp.toPx()),
        size = Size(maxW, maxH),
        cornerRadius = CornerRadius(radius, radius)
    )

    // Lower chassis makes the vehicle sit on the road.
    drawRoundRect(
        color = Color(0xFF11171C).copy(alpha = 0.96f),
        topLeft = Offset(left - 2.dp.toPx(), top + 5.dp.toPx()),
        size = Size(maxW + 4.dp.toPx(), maxH - 1.dp.toPx()),
        cornerRadius = CornerRadius(radius + 2.dp.toPx(), radius + 2.dp.toPx())
    )

    // Main body.
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(
                style.body.copy(alpha = fade),
                style.body.copy(alpha = 0.86f * fade),
                darken(style.body, 0.42f).copy(alpha = fade)
            ),
            start = Offset(left, top),
            end = Offset(left + maxW, top + maxH)
        ),
        topLeft = Offset(left, top),
        size = Size(maxW, maxH),
        cornerRadius = CornerRadius(radius, radius)
    )

    when (style.type) {
        VehicleType.HATCHBACK -> drawHatchback(center, maxW, maxH, style)
        VehicleType.SEDAN -> drawSedan(center, maxW, maxH, style)
        VehicleType.SUV -> drawSuv(center, maxW, maxH, style)
        VehicleType.TAXI -> drawTaxi(center, maxW, maxH, style)
        VehicleType.PICKUP -> drawPickup(center, maxW, maxH, style)
        VehicleType.BUS -> drawBus(center, maxW, maxH, style)
    }

    // Wheels are always visible, making the silhouette read as a vehicle rather
    // than a coloured block.
    val wheelR = maxW * 0.095f
    val wheelX = maxW * 0.10f
    val wheelYs = listOf(top + maxH * 0.23f, top + maxH * 0.77f)
    for (wy in wheelYs) {
        drawWheel(Offset(left + wheelX, wy), wheelR)
        drawWheel(Offset(left + maxW - wheelX, wy), wheelR)
    }

    // Headlights, tail lamps and bumpers.
    drawRoundRect(
        color = Color(0xFFFFF2BF).copy(alpha = 0.98f),
        topLeft = Offset(left + maxW * 0.17f, top + maxH * 0.045f),
        size = Size(maxW * 0.20f, maxH * 0.045f),
        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
    )
    drawRoundRect(
        color = Color(0xFFFFF2BF).copy(alpha = 0.98f),
        topLeft = Offset(left + maxW * 0.63f, top + maxH * 0.045f),
        size = Size(maxW * 0.20f, maxH * 0.045f),
        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
    )
    drawRoundRect(
        color = Color(0xFFD7223B).copy(alpha = 0.95f),
        topLeft = Offset(left + maxW * 0.18f, top + maxH * 0.91f),
        size = Size(maxW * 0.64f, maxH * 0.038f),
        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
    )
    drawRoundRect(
        color = Color.White.copy(alpha = 0.72f),
        topLeft = Offset(center.x - maxW * 0.16f, top + maxH * 0.87f),
        size = Size(maxW * 0.32f, maxH * 0.035f),
        cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
    )

    // Highlight sweep.
    drawLine(
        color = Color.White.copy(alpha = 0.18f * pulse),
        start = Offset(left + maxW * 0.12f, top + maxH * 0.12f),
        end = Offset(left + maxW * 0.78f, top + maxH * 0.12f),
        strokeWidth = 2.dp.toPx(),
        cap = StrokeCap.Round
    )
    }
}

private fun DrawScope.drawHatchback(center: Offset, w: Float, h: Float, s: VehicleStyle) {
    drawCabin(center, w * 0.72f, h * 0.39f, h * 0.02f, s.roof)
    drawRoundRect(
        color = Color.White.copy(alpha = 0.11f),
        topLeft = Offset(center.x - w * 0.34f, center.y - h * 0.25f),
        size = Size(w * 0.68f, h * 0.06f),
        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
    )
}

private fun DrawScope.drawSedan(center: Offset, w: Float, h: Float, s: VehicleStyle) {
    drawCabin(center, w * 0.70f, h * 0.38f, h * 0.01f, s.roof)
    drawRoundRect(
        color = Color.White.copy(alpha = 0.10f),
        topLeft = Offset(center.x - w * 0.31f, center.y - h * 0.31f),
        size = Size(w * 0.62f, h * 0.10f),
        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
    )
    drawRoundRect(
        color = Color.Black.copy(alpha = 0.16f),
        topLeft = Offset(center.x - w * 0.31f, center.y + h * 0.21f),
        size = Size(w * 0.62f, h * 0.08f),
        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
    )
}

private fun DrawScope.drawSuv(center: Offset, w: Float, h: Float, s: VehicleStyle) {
    drawRoundRect(
        color = Color(0xFF0C141B).copy(alpha = 0.30f),
        topLeft = Offset(center.x - w * 0.39f, center.y - h * 0.24f),
        size = Size(w * 0.78f, h * 0.50f),
        cornerRadius = CornerRadius(w * 0.09f, w * 0.09f)
    )
    drawCabin(center, w * 0.76f, h * 0.42f, h * 0.005f, s.roof)
    drawLine(
        color = Color.White.copy(alpha = 0.16f),
        start = Offset(center.x - w * 0.30f, center.y + h * 0.27f),
        end = Offset(center.x + w * 0.30f, center.y + h * 0.27f),
        strokeWidth = 2.dp.toPx()
    )
}

private fun DrawScope.drawTaxi(center: Offset, w: Float, h: Float, s: VehicleStyle) {
    drawCabin(center, w * 0.71f, h * 0.40f, h * 0.01f, s.roof)
    drawRoundRect(
        color = Color(0xFFF8F2D2),
        topLeft = Offset(center.x - w * 0.12f, center.y - h * 0.40f),
        size = Size(w * 0.24f, h * 0.09f),
        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
    )
    drawLine(
        color = Color(0xFF111820).copy(alpha = 0.70f),
        start = Offset(center.x - w * 0.31f, center.y),
        end = Offset(center.x + w * 0.31f, center.y),
        strokeWidth = max(1.dp.toPx(), h * 0.028f)
    )
}

private fun DrawScope.drawPickup(center: Offset, w: Float, h: Float, s: VehicleStyle) {
    val cabinTop = center.y - h * 0.31f
    drawCabin(
        center = Offset(center.x, center.y - h * 0.08f),
        width = w * 0.74f,
        height = h * 0.34f,
        yBias = 0f,
        roof = s.roof
    )
    drawRoundRect(
        color = Color(0xFF17212A).copy(alpha = 0.82f),
        topLeft = Offset(center.x - w * 0.35f, center.y + h * 0.18f),
        size = Size(w * 0.70f, h * 0.25f),
        cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
    )
    drawLine(
        color = Color.White.copy(alpha = 0.14f),
        start = Offset(center.x - w * 0.28f, center.y + h * 0.20f),
        end = Offset(center.x + w * 0.28f, center.y + h * 0.20f),
        strokeWidth = 2.dp.toPx()
    )
}

private fun DrawScope.drawBus(center: Offset, w: Float, h: Float, s: VehicleStyle) {
    drawRoundRect(
        color = Color(0xFF0B141B).copy(alpha = 0.22f),
        topLeft = Offset(center.x - w * 0.44f, center.y - h * 0.47f),
        size = Size(w * 0.88f, h * 0.94f),
        cornerRadius = CornerRadius(w * 0.08f, w * 0.08f)
    )
    val windowW = w * 0.17f
    for (i in 0 until 4) {
        drawRoundRect(
            color = Color(0xFFBFEAF2).copy(alpha = 0.82f),
            topLeft = Offset(
                center.x - w * 0.35f + i * w * 0.23f,
                center.y - h * 0.28f
            ),
            size = Size(windowW, h * 0.23f),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
        )
    }
    drawLine(
        color = Color(0xFF17232B).copy(alpha = 0.85f),
        start = Offset(center.x - w * 0.35f, center.y + h * 0.17f),
        end = Offset(center.x + w * 0.35f, center.y + h * 0.17f),
        strokeWidth = 2.dp.toPx()
    )
}

private fun DrawScope.drawCabin(
    center: Offset,
    width: Float,
    height: Float,
    yBias: Float,
    roof: Color
) {
    val left = center.x - width / 2f
    val top = center.y - height / 2f + yBias
    drawRoundRect(
        color = Color(0xFF0A1219).copy(alpha = 0.96f),
        topLeft = Offset(left, top),
        size = Size(width, height),
        cornerRadius = CornerRadius(min(width, height) * 0.16f, min(width, height) * 0.16f)
    )
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFD7F6FA).copy(alpha = 0.88f), Color(0xFF6D9BA6).copy(alpha = 0.80f))
        ),
        topLeft = Offset(left + width * 0.09f, top + height * 0.13f),
        size = Size(width * 0.82f, height * 0.74f),
        cornerRadius = CornerRadius(min(width, height) * 0.12f, min(width, height) * 0.12f)
    )
    drawLine(
        color = Color.White.copy(alpha = 0.35f),
        start = Offset(center.x, top + height * 0.12f),
        end = Offset(center.x, top + height * 0.86f),
        strokeWidth = 1.4.dp.toPx()
    )
    drawLine(
        color = roof.copy(alpha = 0.70f),
        start = Offset(left + width * 0.08f, top + height * 0.90f),
        end = Offset(left + width * 0.92f, top + height * 0.90f),
        strokeWidth = 1.8.dp.toPx()
    )
}

private fun DrawScope.drawWheel(center: Offset, radius: Float) {
    drawCircle(Color(0xFF07090C), radius, center)
    drawCircle(Color(0xFF59636C), radius * 0.57f, center)
    drawCircle(Color(0xFF0E141A), radius * 0.35f, center)
    drawCircle(Color(0xFFC8D0D6), radius * 0.14f, center)
}

private fun DrawScope.drawEscapeLane(
    center: Offset,
    direction: Direction,
    boardWidth: Float,
    boardHeight: Float,
    color: Color,
    strokeWidth: Float
) {
    val end = when (direction) {
        Direction.LEFT -> Offset(6.dp.toPx(), center.y)
        Direction.RIGHT -> Offset(boardWidth - 6.dp.toPx(), center.y)
        Direction.UP -> Offset(center.x, 6.dp.toPx())
        Direction.DOWN -> Offset(center.x, boardHeight - 6.dp.toPx())
    }
    drawLine(
        color = color,
        start = center,
        end = end,
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
    drawLine(
        color = color.copy(alpha = color.alpha * 0.45f),
        start = center,
        end = end,
        strokeWidth = strokeWidth * 4.5f,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawExitTrail(
    center: Offset,
    direction: Direction,
    progress: Float,
    cellW: Float,
    cellH: Float
) {
    val len = min(cellW, cellH) * (0.8f + progress * 2.4f)
    val dx = direction.dc.toFloat()
    val dy = direction.dr.toFloat()
    drawLine(
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFFB7FBFF).copy(alpha = 0.85f), Color.Transparent)
        ),
        start = center - Offset(dx, dy) * (len * 0.15f),
        end = center - Offset(dx, dy) * len,
        strokeWidth = 10.dp.toPx(),
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color.White.copy(alpha = 0.65f),
        start = center - Offset(dx, dy) * (len * 0.10f),
        end = center - Offset(dx, dy) * len,
        strokeWidth = 2.dp.toPx(),
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawCurb(topLeft: Offset, road: Size) {
    val inset = 4.dp.toPx()
    drawRoundRect(
        color = Color(0xFFF0C02E),
        topLeft = Offset(topLeft.x + inset, topLeft.y + inset),
        size = Size(road.width - inset * 2f, road.height - inset * 2f),
        cornerRadius = CornerRadius(23.dp.toPx(), 23.dp.toPx()),
        style = Stroke(4.dp.toPx())
    )
    // Four short black segments break the yellow curb like a real roadside barrier.
    val black = Color(0xFF20262B)
    val seg = 28.dp.toPx()
    drawLine(black, Offset(18.dp.toPx(), 7.dp.toPx()), Offset(18.dp.toPx() + seg, 7.dp.toPx()), 4.dp.toPx())
    drawLine(black, Offset(18.dp.toPx(), size.height - 7.dp.toPx()), Offset(18.dp.toPx() + seg, size.height - 7.dp.toPx()), 4.dp.toPx())
    drawLine(black, Offset(7.dp.toPx(), 18.dp.toPx()), Offset(7.dp.toPx(), 18.dp.toPx() + seg), 4.dp.toPx())
    drawLine(black, Offset(size.width - 7.dp.toPx(), 18.dp.toPx()), Offset(size.width - 7.dp.toPx(), 18.dp.toPx() + seg), 4.dp.toPx())
}

private fun DrawScope.drawDashedLine(
    color: Color,
    start: Offset,
    end: Offset,
    dash: Float,
    gap: Float,
    strokeWidth: Float
) {
    val dx = end.x - start.x
    val dy = end.y - start.y
    val distance = kotlin.math.sqrt(dx * dx + dy * dy)
    if (distance <= 0f) return
    val ux = dx / distance
    val uy = dy / distance
    var travelled = 0f
    while (travelled < distance) {
        val a = Offset(start.x + ux * travelled, start.y + uy * travelled)
        val bDist = min(travelled + dash, distance)
        val b = Offset(start.x + ux * bDist, start.y + uy * bDist)
        drawLine(color, a, b, strokeWidth)
        travelled += dash + gap
    }
}


private fun DrawScope.drawExitGate(center: Offset, horizontal: Boolean) {
    val length = 46.dp.toPx()
    val thickness = 6.dp.toPx()
    if (horizontal) {
        drawRoundRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFF151C22),
                    Color(0xFFF4C33A),
                    Color(0xFF151C22)
                )
            ),
            topLeft = Offset(center.x - length / 2f, center.y - thickness / 2f),
            size = Size(length, thickness),
            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
        )
    } else {
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF151C22),
                    Color(0xFFF4C33A),
                    Color(0xFF151C22)
                )
            ),
            topLeft = Offset(center.x - thickness / 2f, center.y - length / 2f),
            size = Size(thickness, length),
            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
        )
    }
}

private fun darken(color: Color, amount: Float): Color =
    Color(
        red = color.red * (1f - amount),
        green = color.green * (1f - amount),
        blue = color.blue * (1f - amount),
        alpha = color.alpha
    )

private fun Offset.times(scale: Float): Offset = Offset(x * scale, y * scale)
