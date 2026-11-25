package com.example.busmanager.presentation.route.wizard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmanager.domain.route.model.Terminal
import com.example.busmanager.presentation.route.components.EastJavaMapCanvas
import com.example.busmanager.presentation.route.wizard.RouteWizardUiState
import com.example.busmanager.ui.theme.AppDimens
import com.example.busmanager.ui.theme.CardBorder
import com.example.busmanager.ui.theme.IceBlue
import com.example.busmanager.ui.theme.PrimaryBlue
import com.example.busmanager.ui.theme.PrimaryBlueDark
import com.example.busmanager.ui.theme.SuccessGreen
import com.example.busmanager.ui.theme.SurfaceWhite
import com.example.busmanager.ui.theme.TextDark
import com.example.busmanager.ui.theme.TextMuted
import com.example.busmanager.ui.theme.WarningAmber

@Composable
fun Slide1OriginDestination(
    uiState: RouteWizardUiState,
    onSelectOrigin: (Terminal) -> Unit,
    onSelectDestination: (Terminal) -> Unit,
    onTerminalMapClick: (Terminal) -> Unit = {},
    onUndoClick: () -> Unit = {},
    onResetClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val nodes = uiState.selectedPathNodes
    val head = nodes.lastOrNull()
    val route = uiState.builtRoute

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        item {
            Text(
                text = "Peta Interaktif Perancang Rute",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Sentuh terminal awal, lalu sambungkan terminal demi terminal secara berurutan",
                fontSize = 12.sp,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp)
                ) {
                    EastJavaMapCanvas(
                        terminals = uiState.allTerminals,
                        allSegments = uiState.allSegments,
                        selectedRoute = uiState.builtRoute,
                        selectedOrigin = uiState.selectedOrigin,
                        selectedDestination = uiState.selectedDestination,
                        transitTerminals = uiState.builtRoute?.transitTerminals ?: emptyList(),
                        candidateTerminalIds = uiState.candidateNeighborTerminalIds,
                        onTerminalClick = { terminal ->
                            onTerminalMapClick(terminal)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(360.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )

                    val guideStatusText = when {
                        nodes.isEmpty() -> "👉 Sentuh terminal awal di peta"
                        nodes.size == 1 -> "✨ Sentuh terminal tetangga yang menyala emas"
                        else -> "🗺️ ${nodes.first().city} ➔ ${nodes.last().city} (${route?.totalDistanceKm ?: 0} km)"
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp)
                            .shadow(4.dp, RoundedCornerShape(AppDimens.radiusPill))
                            .clip(RoundedCornerShape(AppDimens.radiusPill))
                            .background(Color(0xFF0F172A).copy(alpha = 0.90f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = guideStatusText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (nodes.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .shadow(4.dp, RoundedCornerShape(8.dp))
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceWhite)
                                    .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                                    .clickable { onUndoClick() }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "↩️", fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Undo",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextDark
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .shadow(4.dp, RoundedCornerShape(8.dp))
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFEE2E2))
                                    .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(8.dp))
                                    .clickable { onResetClick() }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "🔄", fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Reset",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFDC2626)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        if (nodes.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Rangkaian Jalur (${nodes.size} Terminal)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            if (head != null) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(AppDimens.radiusPill))
                                        .background(IceBlue)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Ujung: ${head.city}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryBlueDark
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            nodes.forEachIndexed { index, terminal ->
                                val isOrigin = index == 0
                                val isDest = index == nodes.size - 1 && nodes.size > 1

                                val badgeColor = when {
                                    isOrigin -> SuccessGreen
                                    isDest -> PrimaryBlue
                                    else -> WarningAmber
                                }

                                val roleLabel = when {
                                    isOrigin -> "Asal"
                                    isDest -> "Tujuan"
                                    else -> "Transit ${index}"
                                }

                                Card(
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(badgeColor.copy(alpha = 0.5f)))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(badgeColor)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(
                                                text = terminal.city,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextDark
                                            )
                                            Text(
                                                text = roleLabel,
                                                fontSize = 9.sp,
                                                color = TextMuted
                                            )
                                        }
                                    }
                                }

                                if (index < nodes.size - 1) {
                                    Text(
                                        text = "➔",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryBlue
                                    )
                                }
                            }
                        }

                        if (route != null && nodes.size >= 2) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(IceBlue)
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "Total Jarak", fontSize = 10.sp, color = TextMuted)
                                    Text(
                                        text = "${route.totalDistanceKm} km",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryBlueDark
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "Estimasi Waktu", fontSize = 10.sp, color = TextMuted)
                                    Text(
                                        text = "${uiState.estimatedDurationMinutes / 60}j ${uiState.estimatedDurationMinutes % 60}m",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryBlueDark
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "Singgah", fontSize = 10.sp, color = TextMuted)
                                    Text(
                                        text = "${route.transitTerminals.size} Kota",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryBlueDark
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Cara Merancang Rute",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "1. Sentuh titik kota keberangkatan awal bus pada peta (contoh: Surabaya).",
                            fontSize = 12.sp,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "2. Terminal kota tetangga yang terhubung oleh jalan akan menyala terang (✨).",
                            fontSize = 12.sp,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "3. Sentuh terminal tetangga demi terminal secara berurutan hingga tiba di tujuan.",
                            fontSize = 12.sp,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "4. Jalur akan langsung tergambar di peta dan siap diluncurkan.",
                            fontSize = 12.sp,
                            color = TextDark
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }
        }
    }
}