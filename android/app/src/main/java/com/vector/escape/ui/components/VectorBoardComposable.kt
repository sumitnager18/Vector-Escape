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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.vector.escape.engine.MoveValidator
import com.vector.escape.model.Arrow
import com.vector.escape.model.BoardState
import kotlin.math.PI

@Composable
fun VectorBoardComposable(
    board: BoardState,
    onArrowTapped: (Arrow) -> Unit,
    activeHintArrowId: String? = null,
    modifier: Modifier = Modifier
) {
    val legalMoves = remember(board) {
        MoveValidator.getLegalMoves(board).map { it.id }.toSet()
    }

    val infinite = rememberInfiniteTransition(label = "vector_board")
    val pulse by infinite.animateFloat(
        initialValue = 0.78f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "legal_pulse"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(12.dp)
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
                        board.getArrowAt(row, col)?.let(onArrowTapped)
                    }
                }
        ) {
            val cellW = size.width / board.cols
            val cellH = size.height / board.rows
            val gap = 5.dp.toPx()

            // Deep dimensional board surface.
            drawRoundRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF162945), Color(0xFF07101D), Color(0xFF03060B)),
                    center = center,
                    radius = size.minDimension * 0.78f
                ),
                topLeft = Offset.Zero,
                size = size,
                cornerRadius = CornerRadius(28.dp.toPx(), 28.dp.toPx())
            )

            for (r in 0 until board.rows) {
                for (c in 0 until board.cols) {
                    val x = c * cellW + gap
                    val y = r * cellH + gap
                    val w = cellW - gap * 2
                    val h = cellH - gap * 2

                    val arrow = board.getArrowAt(r, c)
                    val isLegal = arrow != null && legalMoves.contains(arrow.id)
                    val isHint = arrow != null && arrow.id == activeHintArrowId

                    // Physical tile shadow/depth.
                    drawRoundRect(
                        color = Color(0xAA000000),
                        topLeft = Offset(x, y + 4.dp.toPx()),
                        size = Size(w, h),
                        cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx())
                    )

                    val tileBrush = when {
                        isHint -> Brush.linearGradient(
                            listOf(Color(0xFF194E46), Color(0xFF0B2524), Color(0xFF071218)),
                            start = Offset(x, y),
                            end = Offset(x + w, y + h)
                        )
                        arrow != null -> Brush.linearGradient(
                            listOf(Color(0xFF203654), Color(0xFF111F35), Color(0xFF080F1C)),
                            start = Offset(x, y),
                            end = Offset(x + w, y + h)
                        )
                        else -> Brush.linearGradient(
                            listOf(Color(0xFF101D30), Color(0xFF08111F)),
                            start = Offset(x, y),
                            end = Offset(x + w, y + h)
                        )
                    }

                    drawRoundRect(
                        brush = tileBrush,
                        topLeft = Offset(x, y),
                        size = Size(w, h),
                        cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx())
                    )

                    // Top bevel / reflective edge.
                    drawRoundRect(
                        color = Color.White.copy(alpha = if (arrow != null) 0.075f else 0.035f),
                        topLeft = Offset(x + 1.dp.toPx(), y + 1.dp.toPx()),
                        size = Size(w - 2.dp.toPx(), h * 0.48f),
                        cornerRadius = CornerRadius(13.dp.toPx(), 13.dp.toPx()),
                        style = Stroke(width = 1.dp.toPx())
                    )

                    // Road/lane center markings give the board a miniature-world feel.
                    drawLine(
                        color = Color(0xFF8BA4C8).copy(alpha = if (arrow != null) 0.035f else 0.07f),
                        start = Offset(x + w * 0.12f, y + h * 0.5f),
                        end = Offset(x + w * 0.88f, y + h * 0.5f),
                        strokeWidth = 1.dp.toPx()
                    )

                    val borderColor = when {
                        isHint -> Color(0xFF10B981).copy(alpha = 0.9f)
                        isLegal -> Color(0xFF00F0FF).copy(alpha = 0.65f)
                        arrow != null -> Color(0xFF4FD7FF).copy(alpha = 0.20f)
                        else -> Color(0xFF334155).copy(alpha = 0.30f)
                    }

                    drawRoundRect(
                        color = borderColor,
                        topLeft = Offset(x, y),
                        size = Size(w, h),
                        cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx()),
                        style = Stroke(
                            width = if (isHint || isLegal) 1.7.dp.toPx() else 1.dp.toPx()
                        )
                    )

                    if (arrow != null) {
                        val cx = x + w / 2f
                        val cy = y + h / 2f
                        val center = Offset(cx, cy)
                        val angle = arrow.direction.angleDeg.toFloat()

                        val arrowColor = when {
                            isHint -> Color(0xFF10B981)
                            isLegal -> Color(0xFF00F0FF)
                            else -> Color(0xFF70A7E8)
                        }

                        // Legal vectors receive a soft energy halo.
                        if (isLegal) {
                            drawCircle(
                                color = arrowColor.copy(alpha = 0.09f * pulse),
                                radius = minOf(w, h) * 0.40f * pulse,
                                center = center
                            )
                        }

                        rotate(degrees = angle, pivot = center) {
                            // Ground shadow.
                            drawRoundRect(
                                color = Color.Black.copy(alpha = 0.50f),
                                topLeft = Offset(cx - w * 0.22f, cy - h * 0.20f + 4.dp.toPx()),
                                size = Size(w * 0.44f, h * 0.52f),
                                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                            )

                            // Raised vector body.
                            drawRoundRect(
                                brush = Brush.linearGradient(
                                    listOf(
                                        arrowColor.copy(alpha = 0.98f),
                                        arrowColor.copy(alpha = 0.70f),
                                        arrowColor.copy(alpha = 0.30f)
                                    ),
                                    start = Offset(cx, cy - h * 0.40f),
                                    end = Offset(cx, cy + h * 0.40f)
                                ),
                                topLeft = Offset(cx - w * 0.21f, cy - h * 0.28f),
                                size = Size(w * 0.42f, h * 0.54f),
                                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                            )

                            // Arrowhead.
                            val head = Path().apply {
                                moveTo(cx, cy - h * 0.43f)
                                lineTo(cx - w * 0.29f, cy - h * 0.08f)
                                lineTo(cx - w * 0.11f, cy - h * 0.08f)
                                lineTo(cx - w * 0.11f, cy + h * 0.05f)
                                lineTo(cx + w * 0.11f, cy + h * 0.05f)
                                lineTo(cx + w * 0.11f, cy - h * 0.08f)
                                lineTo(cx + w * 0.29f, cy - h * 0.08f)
                                close()
                            }
                            drawPath(
                                path = head,
                                brush = Brush.linearGradient(
                                    listOf(Color.White.copy(alpha = 0.88f), arrowColor, arrowColor.copy(alpha = 0.55f)),
                                    start = Offset(cx, cy - h * 0.43f),
                                    end = Offset(cx, cy)
                                )
                            )

                            // Specular highlight.
                            drawLine(
                                color = Color.White.copy(alpha = 0.48f),
                                start = Offset(cx - w * 0.09f, cy - h * 0.21f),
                                end = Offset(cx - w * 0.09f, cy + h * 0.02f),
                                strokeWidth = 1.2.dp.toPx()
                            )

                            // Energy core.
                            drawCircle(
                                color = Color(0xFF03101B),
                                radius = 4.dp.toPx(),
                                center = Offset(cx, cy + h * 0.18f)
                            )
                            drawCircle(
                                color = Color.White.copy(alpha = 0.82f),
                                radius = 2.dp.toPx(),
                                center = Offset(cx, cy + h * 0.16f)
                            )
                        }

                        if (isHint) {
                            drawRoundRect(
                                color = Color(0xFF10B981).copy(alpha = 0.65f),
                                topLeft = Offset(x - 2.dp.toPx(), y - 2.dp.toPx()),
                                size = Size(w + 4.dp.toPx(), h + 4.dp.toPx()),
                                cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                                style = Stroke(width = 2.dp.toPx())
                            )
                        }
                    } else {
                        drawCircle(
                            color = Color(0xFF7890A8).copy(alpha = 0.18f),
                            radius = 1.8.dp.toPx(),
                            center = Offset(x + w / 2f, y + h / 2f)
                        )
                    }
                }
            }

            // Subtle glass highlight across the board.
            drawRoundRect(
                color = Color.White.copy(alpha = 0.025f),
                topLeft = Offset(1.dp.toPx(), 1.dp.toPx()),
                size = Size(size.width - 2.dp.toPx(), size.height * 0.28f),
                cornerRadius = CornerRadius(27.dp.toPx(), 27.dp.toPx())
            )
        }
    }
}
