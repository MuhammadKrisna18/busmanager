package com.example.busmanager.presentation.simulation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmanager.ui.theme.AccentBlue
import com.example.busmanager.ui.theme.AppDimens
import com.example.busmanager.ui.theme.BackgroundWhite
import com.example.busmanager.ui.theme.CardBorder
import com.example.busmanager.ui.theme.IceBlue
import com.example.busmanager.ui.theme.PrimaryBlue
import com.example.busmanager.ui.theme.PrimaryBlueDark
import com.example.busmanager.ui.theme.SuccessGreen
import com.example.busmanager.ui.theme.SurfaceWhite
import com.example.busmanager.ui.theme.TextDark
import com.example.busmanager.ui.theme.TextMuted
import com.example.busmanager.ui.theme.WarningAmber
import com.example.busmanager.ui.theme.WarningAmberLight

@Composable
fun SimulationMapScreen(
    viewModel: SimulationMapViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToBuses: () -> Unit = {},
    onNavigateToRoutes: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = BackgroundWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppDimens.paddingMedium),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                SimulationTopBar(
                    dateText = state.formattedDate,
                    timeText = state.formattedTime,
                    isPlaying = state.isPlaying,
                    onTogglePlay = { viewModel.togglePlay() },
                    onNavigateBack = onNavigateBack
                )

                Spacer(modifier = Modifier.height(AppDimens.paddingSmall))

                SimulationStatusBanner(
                    ownedBusCount = state.ownedBusCount,
                    activeBusCount = state.activeBusCount
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = AppDimens.paddingSmall)
                    .clip(RoundedCornerShape(AppDimens.radiusMedium))
                    .background(Color(0xFFE2E8F0).copy(alpha = 0.4f))
                    .border(1.dp, CardBorder, RoundedCornerShape(AppDimens.radiusMedium)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(AppDimens.paddingLarge)
                ) {
                    Text(
                        text = "🗺️",
                        fontSize = 44.sp
                    )
                    Spacer(modifier = Modifier.height(AppDimens.paddingSmall))
                    Text(
                        text = "Peta Simulasi Jawa Timur",
                        color = TextDark,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (state.ownedBusCount == 0) {
                            "Belum ada armada atau rute aktif.\nBeli armada di Showroom dan rencanakan rute untuk memulai operasi."
                        } else {
                            "Armada siap beroperasi di koridor antarkota Jawa Timur."
                        },
                        color = TextMuted,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            SimulationBottomBar(
                ownedBusCount = state.ownedBusCount,
                onNavigateToBuses = onNavigateToBuses,
                onNavigateToRoutes = onNavigateToRoutes
            )
        }
    }
}

@Composable
private fun SimulationTopBar(
    dateText: String,
    timeText: String,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDimens.radiusMedium),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = AppDimens.cardElevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.paddingSmall),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(IceBlue)
                    .clickable(onClick = onNavigateBack),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "◀",
                    color = PrimaryBlueDark,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(AppDimens.paddingSmall))

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = dateText,
                    color = TextDark,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = timeText,
                    color = PrimaryBlue,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.width(AppDimens.paddingSmall))

            Button(
                onClick = onTogglePlay,
                shape = RoundedCornerShape(AppDimens.radiusPill),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPlaying) Color(0xFFDC2626) else PrimaryBlue
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = if (isPlaying) "⏸️ Jeda" else "▶️ PLAY",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun SimulationStatusBanner(
    ownedBusCount: Int,
    activeBusCount: Int
) {
    val isIdle = activeBusCount == 0
    val bgColor = if (isIdle) WarningAmberLight else AccentBlue
    val textColor = if (isIdle) Color(0xFFB45309) else PrimaryBlueDark
    val icon = if (isIdle) "ℹ️" else "🚌"
    val message = if (ownedBusCount == 0) {
        "Armada belum tersedia. Operasi bus belum dapat berjalan."
    } else if (activeBusCount == 0) {
        "Semua armada ($ownedBusCount unit) saat ini standby di garasi."
    } else {
        "$activeBusCount dari $ownedBusCount armada sedang bergerak melayani trayek."
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppDimens.radiusSmall))
            .background(bgColor)
            .padding(horizontal = AppDimens.paddingMedium, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = message,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SimulationBottomBar(
    ownedBusCount: Int,
    onNavigateToBuses: () -> Unit,
    onNavigateToRoutes: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppDimens.paddingSmall)
    ) {
        Button(
            onClick = onNavigateToBuses,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(AppDimens.radiusMedium),
            colors = ButtonDefaults.buttonColors(containerColor = SurfaceWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = "🚌", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Bus",
                        color = TextDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "$ownedBusCount Unit",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Button(
            onClick = onNavigateToRoutes,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(AppDimens.radiusMedium),
            colors = ButtonDefaults.buttonColors(containerColor = SurfaceWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = "🗺️", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Trayek",
                        color = TextDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Jalur & Tarif",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
