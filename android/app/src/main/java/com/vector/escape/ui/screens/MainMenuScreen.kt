package com.vector.escape.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VectorBg)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Logo
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 32.dp)
            ) {
                Surface(
                    color = VectorSurface,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = null,
                            tint = VectorCyan,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "VECTOR ESCAPE",
                    fontSize = 24.sp,
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

            // Central Play Action
            Button(
                onClick = { onStartCampaign(prefs.currentCampaignLevel) },
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VectorCyan),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (prefs.currentCampaignLevel == 1) "START CAMPAIGN" else "RESUME SECTOR \${prefs.currentCampaignLevel}",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Menu Options
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onOpenLevelSelect,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("CAMPAIGN MATRIX (50 SECTORS)", color = VectorTextPrimary)
                }
                OutlinedButton(
                    onClick = onOpenDaily,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("DAILY VECTOR (OFFLINE)", color = VectorTextPrimary)
                }
                OutlinedButton(
                    onClick = onOpenPractice,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("PRACTICE LAB (SOLVABLE SYNTH)", color = VectorTextPrimary)
                }
            }

            // Footer Settings
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(onClick = onOpenSettings) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = VectorTextSecondary
                    )
                }
            }
        }
    }
}
