package com.vector.escape.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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
fun MainMenuScreen(
    prefs: GamePreferences,
    onStartCampaign: (Int) -> Unit,
    onOpenLevelSelect: () -> Unit,
    onOpenDaily: () -> Unit,
    onOpenPractice: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val current = prefs.currentCampaignLevel.coerceIn(1, 50)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VectorBg)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 8.dp, bottom = 22.dp)
        ) {
            Surface(
                color = VectorSurface,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.size(76.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = null,
                        tint = VectorCyan,
                        modifier = Modifier.size(42.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "VECTOR ESCAPE",
                fontSize = 25.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp,
                color = VectorTextPrimary
            )
            Text(
                text = "DIRECTIONAL CLEARANCE SYSTEM",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = VectorCyan,
                letterSpacing = 2.sp
            )
        }

        Button(
            onClick = { onStartCampaign(current) },
            shape = RoundedCornerShape(22.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VectorCyan),
            modifier = Modifier.fillMaxWidth().height(64.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (current == 1) "START CAMPAIGN" else "RESUME SECTOR $current",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = onOpenLevelSelect,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) { Text("CAMPAIGN MATRIX · 50 SECTORS", color = VectorTextPrimary) }

            OutlinedButton(
                onClick = onOpenDaily,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) { Text("DAILY VECTOR · OFFLINE", color = VectorTextPrimary) }

            OutlinedButton(
                onClick = onOpenPractice,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) { Text("PRACTICE LAB · SOLVABLE SYNTH", color = VectorTextPrimary) }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "CURRENT SECTOR $current / 50",
            color = VectorTextSecondary,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        IconButton(onClick = onOpenSettings) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = VectorTextSecondary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}
