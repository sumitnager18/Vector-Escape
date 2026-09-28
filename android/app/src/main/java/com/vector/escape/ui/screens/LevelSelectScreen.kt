package com.vector.escape.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vector.escape.storage.GamePreferences
import com.vector.escape.ui.theme.*

@Composable
fun LevelSelectScreen(
    prefs: GamePreferences,
    onSelectLevel: (Int) -> Unit,
    onBack: () -> Unit
) {
    val levels = (1..50).toList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VectorBg)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = VectorTextPrimary)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "SECTOR MATRIX",
                    color = VectorTextPrimary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Text(
                    "50 CAMPAIGN LEVELS",
                    color = VectorCyan,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )
            }
            Text(
                "${prefs.getTotalStars()} / 150",
                color = VectorAmber,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(levels) { level ->
                val unlocked = level <= prefs.unlockedLevel
                val stars = prefs.getLevelStars(level)
                val isCurrent = level == prefs.currentCampaignLevel

                Surface(
                    onClick = { if (unlocked) onSelectLevel(level) },
                    enabled = unlocked,
                    shape = RoundedCornerShape(14.dp),
                    color = if (isCurrent) Color(0xFF12344A) else if (unlocked) VectorCard else Color(0xFF080D17),
                    border = BorderStroke(
                        if (isCurrent) 2.dp else 1.dp,
                        if (isCurrent) VectorCyan else Color(0x22334155)
                    ),
                    modifier = Modifier.aspectRatio(1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (unlocked) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "$level",
                                    color = if (isCurrent) VectorCyan else VectorTextPrimary,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                                    for (s in 1..3) {
                                        Icon(
                                            Icons.Default.Star,
                                            contentDescription = null,
                                            tint = if (s <= stars) VectorAmber else Color(0x33475565),
                                            modifier = Modifier.size(11.dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            Icon(Icons.Default.Lock, contentDescription = "Locked", tint = Color(0x665F6B7A))
                        }
                    }
                }
            }
        }
    }
}
