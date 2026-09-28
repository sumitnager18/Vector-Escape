package com.vector.escape.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vector.escape.audio.SoundManager
import com.vector.escape.engine.MoveValidator
import com.vector.escape.haptics.HapticManager
import com.vector.escape.model.*
import com.vector.escape.storage.GamePreferences
import com.vector.escape.ui.components.VectorBoardComposable
import com.vector.escape.ui.theme.*

private fun exitDurationMillis(arrow: Arrow, rows: Int, cols: Int): Int {
    val cellsToEdge = when (arrow.direction) {
        Direction.LEFT -> arrow.col + 1.25f
        Direction.RIGHT -> (cols - arrow.col - 1) + 1.25f
        Direction.UP -> arrow.row + 1.25f
        Direction.DOWN -> (rows - arrow.row - 1) + 1.25f
    }
    return (360f + cellsToEdge * 105f).toInt().coerceIn(430, 1150)
}

@Composable
fun GameScreen(
    levelDef: LevelDefinition,
    prefs: GamePreferences,
    onBack: () -> Unit,
    onNextLevel: () -> Unit
) {
    val context = LocalContext.current
    var board by remember(levelDef) { mutableStateOf(BoardState(levelDef.rows, levelDef.cols, levelDef.arrows)) }
    var hearts by remember(levelDef) { mutableStateOf(3) }
    var moves by remember(levelDef) { mutableStateOf(0) }
    var mistakes by remember(levelDef) { mutableStateOf(0) }
    var flow by remember(levelDef) { mutableStateOf(1) }
    var undoStack by remember(levelDef) { mutableStateOf(listOf<UndoStep>()) }
    var activeHintId by remember(levelDef) { mutableStateOf<String?>(null) }
    var movingArrow by remember(levelDef) { mutableStateOf<Arrow?>(null) }
    var impactArrowId by remember(levelDef) { mutableStateOf<String?>(null) }
    var isCleared by remember(levelDef) { mutableStateOf(false) }
    var isFailed by remember(levelDef) { mutableStateOf(false) }
    val exitProgress = remember { Animatable(0f) }
    val impactProgress = remember { Animatable(0f) }

    fun resetBoard() {
        board = BoardState(levelDef.rows, levelDef.cols, levelDef.arrows)
        hearts = 3
        moves = 0
        mistakes = 0
        flow = 1
        undoStack = emptyList()
        activeHintId = null
        movingArrow = null
        impactArrowId = null
        isCleared = false
        isFailed = false
    }

    LaunchedEffect(movingArrow?.id) {
        val arrow = movingArrow ?: return@LaunchedEffect
        exitProgress.snapTo(0f)
        exitProgress.animateTo(
            1f,
            animationSpec = tween(
                durationMillis = exitDurationMillis(arrow, levelDef.rows, levelDef.cols),
                easing = FastOutSlowInEasing
            )
        )
        board = board.removeArrow(arrow.id)
        movingArrow = null
        exitProgress.snapTo(0f)

        if (board.isCleared) {
            isCleared = true
            SoundManager.playWin()
            HapticManager.success(context)
            prefs.saveLevelStars(levelDef.levelNumber, GameState.calculateStars(mistakes))
        }
    }

    LaunchedEffect(impactArrowId) {
        if (impactArrowId == null) return@LaunchedEffect
        impactProgress.snapTo(0f)
        impactProgress.animateTo(1f, tween(100, easing = FastOutSlowInEasing))
        impactProgress.animateTo(0f, tween(220, easing = FastOutSlowInEasing))
        impactArrowId = null
    }

    fun handleArrowAction(arrow: Arrow, directionSign: Int = 1) {
        if (isCleared || isFailed || movingArrow != null || impactArrowId != null) return

        if (directionSign > 0 && MoveValidator.isMoveLegal(board, arrow)) {
            SoundManager.playLaunch(flow)
            HapticManager.tap(context)
            activeHintId = null
            undoStack = undoStack + UndoStep(board, flow, arrow)
            moves += 1
            flow += 1
            movingArrow = arrow
        } else {
            SoundManager.playBlocked()
            HapticManager.blocked(context)
            activeHintId = null
            mistakes += 1
            flow = 1
            hearts -= 1
            impactArrowId = arrow.id
            if (hearts <= 0) isFailed = true
        }
    }

    fun handleArrowTap(arrow: Arrow) = handleArrowAction(arrow, 1)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VectorBg)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, enabled = movingArrow == null) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = VectorTextPrimary)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "SECTOR ${levelDef.levelNumber}",
                    fontWeight = FontWeight.Bold,
                    color = VectorTextPrimary,
                    fontSize = 22.sp
                )
                Text(
                    text = levelDef.title.uppercase(),
                    fontSize = 9.sp,
                    color = VectorCyan,
                    maxLines = 1
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (i in 0 until 3) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = if (i < hearts) VectorRed else Color(0x33EF4444),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(VectorSurface, RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("LEFT ${board.remainingCount}", fontSize = 12.sp, color = VectorCyan)
            Text("MOVES $moves", fontSize = 12.sp, color = VectorTextSecondary)
            Text("FLOW x$flow", fontSize = 12.sp, color = VectorViolet)
        }

        Spacer(modifier = Modifier.height(10.dp))

        VectorBoardComposable(
            board = board,
            onArrowTapped = ::handleArrowTap,
            onArrowSwiped = { arrow, sign -> handleArrowAction(arrow, sign) },
            activeHintArrowId = activeHintId,
            impactArrowId = impactArrowId,
            impactProgress = impactProgress.value,
            exitingArrowId = movingArrow?.id,
            exitProgress = exitProgress.value,
            interactionEnabled = movingArrow == null && impactArrowId == null,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = {
                    if (undoStack.isNotEmpty() && movingArrow == null) {
                        val last = undoStack.last()
                        board = last.board
                        flow = last.flow
                        moves = (moves - 1).coerceAtLeast(0)
                        undoStack = undoStack.dropLast(1)
                        activeHintId = null
                        SoundManager.playTap()
                    }
                },
                enabled = undoStack.isNotEmpty() && movingArrow == null,
                modifier = Modifier.weight(1f).height(48.dp)
            ) { Text("UNDO") }

            OutlinedButton(
                onClick = {
                    val legal = MoveValidator.getLegalMoves(board)
                    if (legal.isNotEmpty() && movingArrow == null) {
                        activeHintId = legal.first().id
                        SoundManager.playTap()
                    }
                },
                enabled = board.remainingCount > 0 && movingArrow == null,
                modifier = Modifier.weight(1f).height(48.dp)
            ) { Text("HINT") }

            OutlinedButton(
                onClick = { if (movingArrow == null) { resetBoard(); SoundManager.playTap() } },
                enabled = movingArrow == null,
                modifier = Modifier.weight(1f).height(48.dp)
            ) { Text("RESET") }
        }

        if (levelDef.tutorialTip != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = levelDef.tutorialTip,
                color = VectorTextSecondary,
                fontSize = 11.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
    }

    if (isCleared) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("SECTOR ${levelDef.levelNumber} CLEARED") },
            text = { Text("Cleared in $moves moves with $mistakes mistakes.") },
            confirmButton = {
                Button(onClick = onNextLevel) {
                    Text(if (levelDef.levelNumber < 50) "NEXT SECTOR" else "CAMPAIGN COMPLETE")
                }
            },
            dismissButton = { TextButton(onClick = onBack) { Text("MENU") } }
        )
    }

    if (isFailed) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("SECTOR ${levelDef.levelNumber} FAILED") },
            text = { Text("All three hearts were used. Try the route again.") },
            confirmButton = { Button(onClick = ::resetBoard) { Text("TRY AGAIN") } },
            dismissButton = { TextButton(onClick = onBack) { Text("LEVEL SELECT") } }
        )
    }
}
