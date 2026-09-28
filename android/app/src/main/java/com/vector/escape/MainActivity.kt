package com.vector.escape

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.vector.escape.engine.CampaignLevels
import com.vector.escape.engine.DailyVectorGenerator
import com.vector.escape.engine.PuzzleGenerator
import com.vector.escape.model.Difficulty
import com.vector.escape.model.LevelDefinition
import com.vector.escape.storage.GamePreferences
import com.vector.escape.ui.screens.GameScreen
import com.vector.escape.ui.screens.MainMenuScreen
import com.vector.escape.ui.theme.VectorBg
import com.vector.escape.ui.theme.VectorEscapeTheme

enum class Screen {
    MENU,
    GAME
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = GamePreferences(this)

        setContent {
            VectorEscapeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = VectorBg
                ) {
                    var currentScreen by remember { mutableStateOf(Screen.MENU) }
                    var activeLevel by remember { mutableStateOf(CampaignLevels.getLevel(1)) }

                    when (currentScreen) {
                        Screen.MENU -> {
                            MainMenuScreen(
                                prefs = prefs,
                                onStartCampaign = { levelNum ->
                                    activeLevel = CampaignLevels.getLevel(levelNum)
                                    currentScreen = Screen.GAME
                                },
                                onOpenLevelSelect = {
                                    activeLevel = CampaignLevels.getLevel(prefs.currentCampaignLevel)
                                    currentScreen = Screen.GAME
                                },
                                onOpenDaily = {
                                    val dateStr = DailyVectorGenerator.getTodayDateString()
                                    activeLevel = DailyVectorGenerator.generateForDate(dateStr)
                                    currentScreen = Screen.GAME
                                },
                                onOpenPractice = {
                                    val board = PuzzleGenerator.generate(6, 6, 14, System.currentTimeMillis())
                                    activeLevel = LevelDefinition(
                                        levelNumber = 0,
                                        title = "Practice Vector",
                                        rows = 6,
                                        cols = 6,
                                        difficulty = Difficulty.MEDIUM,
                                        arrows = board.arrows,
                                        parMoves = board.arrows.size
                                    )
                                    currentScreen = Screen.GAME
                                },
                                onOpenSettings = {}
                            )
                        }
                        Screen.GAME -> {
                            GameScreen(
                                levelDef = activeLevel,
                                prefs = prefs,
                                onBack = { currentScreen = Screen.MENU },
                                onNextLevel = {
                                    val nextNum = activeLevel.levelNumber + 1
                                    if (nextNum <= 50) {
                                        activeLevel = CampaignLevels.getLevel(nextNum)
                                    } else {
                                        currentScreen = Screen.MENU
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
