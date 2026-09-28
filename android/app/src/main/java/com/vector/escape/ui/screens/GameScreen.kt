package com.vector.escape.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vector.escape.audio.SoundManager
import com.vector.escape.engine.MoveValidator
import com.vector.escape.engine.PuzzleSolver
import com.vector.escape.haptics.HapticManager
import com.vector.escape.model.*
import com.vector.escape.storage.GamePreferences
import com.vector.escape.ui.components.VectorBoardComposable
import com.vector.escape.ui.theme.*

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
    var isCleared by remember(levelDef) { mutableStateOf(false) }
    var isFailed by remember(levelDef) { mutableStateOf(false) }

    fun handleArrowTap(arrow: Arrow) {
        if (isCleared || isFailed) return

        if (MoveValidator.isMoveLegal(board, arrow)) {
            // Legal move
            SoundManager.playLaunch(flow)
            HapticManager.tap(context)
            activeHintId = null

            val nextUndo = undoStack + UndoStep(board, flow, arrow)
            val nextBoard = board.removeArrow(arrow.id)
            val nextMoves = moves + 1
            val nextFlow = flow + 1

            board = nextBoard
            moves = nextMoves
            flow = nextFlow
            undoStack = nextUndo

            if (nextBoard.isCleared) {
                isCleared = true
                SoundManager.playWin()
                HapticManager.success(context)
                val stars = GameState.calculateStars(mistakes)
                prefs.saveLevelStars(levelDef.levelNumber, stars)
            }
        } else {
            // Blocked tap
            SoundManager.playBlocked()
            HapticManager.blocked(context)
            activeHintId = null
            mistakes += 1
            flow = 1
            hearts -= 1

            if (hearts <= 0) {
                isFailed = true
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VectorBg)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top HUD
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = VectorTextPrimary)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "SECTOR \${levelDef.levelNumber}",
                        fontWeight = FontWeight.Bold,
                        color = VectorTextPrimary
                    )
                    Text(
                        text = "\${levelDef.difficulty}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = VectorCyan
                    )
                }

                // Hearts
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

            // Stats row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(VectorSurface, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "REMAINING: \${board.remainingCount}", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = VectorCyan)
                Text(text = "MOVES: \$moves", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = VectorTextSecondary)
                Text(text = "FLOW x\$flow", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = VectorViolet)
            }

            // Gameboard
            VectorBoardComposable(
                board = board,
                onArrowTapped = { handleArrowTap(it) },
                activeHintArrowId = activeHintId
            )

            // Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        if (undoStack.isNotEmpty()) {
                            val last = undoStack.last()
                            board = last.board
                            flow = last.flow
                            moves = (moves - 1).coerceAtLeast(0)
                            undoStack = undoStack.dropLast(1)
                            SoundManager.playTap()
                        }
                    },
                    enabled = undoStack.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("UNDO")
                }

                OutlinedButton(
                    onClick = {
                        val legal = MoveValidator.getLegalMoves(board)
                        if (legal.isNotEmpty()) {
                            activeHintId = legal.first().id
                            SoundManager.playTap()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("HINT")
                }

                OutlinedButton(
                    onClick = {
                        board = BoardState(levelDef.rows, levelDef.cols, levelDef.arrows)
                        hearts = 3
                        moves = 0
                        mistakes = 0
                        flow = 1
                        undoStack = emptyList()
                        isCleared = false
                        isFailed = false
                        SoundManager.playTap()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("RESET")
                }
            }
        }

        // Completion Dialog
        if (isCleared) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("LEVEL CLEARED") },
                text = { Text("Sector \${levelDef.levelNumber} cleared in \$moves moves.") },
                confirmButton = {
                    Button(onClick = onNextLevel) {
                        Text("NEXT SECTOR")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onBack) {
                        Text("MENU")
                    }
                }
            )
        }

        // Failure Dialog
        if (isFailed) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("LEVEL FAILED") },
                text = { Text("All hearts depleted in sector \${levelDef.levelNumber}.") },
                confirmButton = {
                    Button(onClick = {
                        board = BoardState(levelDef.rows, levelDef.cols, levelDef.arrows)
                        hearts = 3
                        moves = 0
                        mistakes = 0
                        flow = 1
                        undoStack = emptyList()
                        isFailed = false
                    }) {
                        Text("TRY AGAIN")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onBack) {
                        Text("LEVEL SELECT")
                    }
                }
            )
        }
    }
}
