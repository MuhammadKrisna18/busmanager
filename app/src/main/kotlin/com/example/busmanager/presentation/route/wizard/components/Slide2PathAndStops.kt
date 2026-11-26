package com.example.busmanager.presentation.route.wizard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmanager.presentation.route.components.EastJavaMapCanvas
import com.example.busmanager.presentation.route.components.MapTerminalToggle
import com.example.busmanager.presentation.route.wizard.RouteWizardUiState
import com.example.busmanager.ui.theme.CardBorder
import com.example.busmanager.ui.theme.IceBlue
import com.example.busmanager.ui.theme.PrimaryBlue
import com.example.busmanager.ui.theme.PrimaryBlueDark
import com.example.busmanager.ui.theme.SuccessGreen
import com.example.busmanager.ui.theme.SurfaceWhite
import com.example.busmanager.ui.theme.TextDark
import com.example.busmanager.ui.theme.TextLight
import com.example.busmanager.ui.theme.TextMuted

@Composable
fun Slide2PathAndStops(
    uiState: RouteWizardUiState,
    onSelectPath: (Int) -> Unit,
    onToggleTransitStop: (String) -> Unit,
    onSetIsExpress: (Boolean) -> Unit = {},
    onSelectAllTransit: () -> Unit = {},
    onClearAllTransit: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentPath = uiState.selectedPathNodes.ifEmpty {
        uiState.availablePaths.getOrNull(uiState.selectedPathIndex) ?: emptyList()
    }
    val route = uiState.builtRoute

    val terminalToggles = remember(currentPath, uiState.activeTransitIds, uiState.isExpress) {
        if (currentPath.size <= 2) emptyList() else {
            (1 until currentPath.size - 1).map { i ->
                val term = currentPath[i]
                MapTerminalToggle(
                    terminalId = term.id,
                    coordinate = term.coordinate,
                    isTransitActive = !uiState.isExpress && (term.id in uiState.activeTransitIds)
                )
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        item {
            Text(
                text = "Kustomisasi Jalur & Titik Singgah",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Pilih koridor jalan dan atur terminal singgah (ampiran) secara bebas",
                fontSize = 12.sp,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder))
            ) {
                EastJavaMapCanvas(
                    terminals = uiState.allTerminals,
                    allSegments = uiState.allSegments,
                    selectedRoute = route,
                    selectedOrigin = route?.originTerminal ?: uiState.selectedOrigin,
                    selectedDestination = route?.destinationTerminal ?: uiState.selectedDestination,
                    transitTerminals = route?.transitTerminals ?: emptyList(),
                    terminalToggles = terminalToggles,
                    availablePaths = uiState.availablePaths,
                    selectedPathIndex = uiState.selectedPathIndex,
                    onSelectPath = onSelectPath,
                    onTerminalToggleClick = { termId ->
                        onToggleTransitStop(termId)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .clip(RoundedCornerShape(14.dp))
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        if (route != null) {
            item {
                val hours = route.totalEstimatedDurationMinutes / 60
                val mins = route.totalEstimatedDurationMinutes % 60
                val durationStr = if (hours > 0) "${hours}j ${mins}m" else "${mins} mnt"
                val transitCount = route.transitTerminals.size

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = IceBlue),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(PrimaryBlue.copy(alpha = 0.5f)))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = route.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlueDark,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Live Metrics",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue,
                                maxLines = 1
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceWhite)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "Total Jarak", fontSize = 10.sp, color = TextMuted)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${route.totalDistanceKm.toInt()} km",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlueDark
                                )
                            }
                            Box(modifier = Modifier.width(1.dp).height(24.dp).background(CardBorder))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "Waktu Tempuh", fontSize = 10.sp, color = TextMuted)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = durationStr,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlueDark
                                )
                            }
                            Box(modifier = Modifier.width(1.dp).height(24.dp).background(CardBorder))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "Pola Singgah", fontSize = 10.sp, color = TextMuted)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (transitCount == 0) "Non-Stop" else "$transitCount Halte Singgah",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (transitCount == 0) PrimaryBlue else PrimaryBlueDark
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }
        }

        item {
            val isNonStop = uiState.isExpress || uiState.activeTransitIds.isEmpty() || (uiState.builtRoute?.transitTerminals?.isEmpty() == true)
            val totalIntermediate = if (currentPath.size > 2) currentPath.size - 2 else 0
            val activeCount = if (isNonStop) 0 else currentPath.subList(1, currentPath.size - 1).count { it.id in uiState.activeTransitIds }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Pengaturan Titik Singgah (Ampiran)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Bebas menentukan terminal singgah untuk menaik-turunkan penumpang, atau tanpa ampiran sama sekali (langsung non-stop).",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isNonStop) IceBlue else Color(0xFFF8FAFC))
                                .border(
                                    width = 1.dp,
                                    color = if (isNonStop) PrimaryBlue else CardBorder,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { onClearAllTransit() }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Tanpa Ampiran (Non-Stop)",
                                fontSize = 11.sp,
                                fontWeight = if (isNonStop) FontWeight.Bold else FontWeight.Medium,
                                color = if (isNonStop) PrimaryBlueDark else TextDark
                            )
                        }

                        if (totalIntermediate > 0) {
                            val allSelected = !isNonStop && activeCount == totalIntermediate
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (allSelected) IceBlue else Color(0xFFF8FAFC))
                                    .border(
                                        width = 1.dp,
                                        color = if (allSelected) PrimaryBlue else CardBorder,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onSelectAllTransit() }
                                    .padding(vertical = 10.dp, horizontal = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Singgahi Semua",
                                    fontSize = 11.sp,
                                    fontWeight = if (allSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (allSelected) PrimaryBlueDark else TextDark
                                )
                            }
                        }
                    }

                    if (totalIntermediate > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Status Ampiran Terpilih:",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                            Text(
                                text = if (isNonStop) "0 dari $totalIntermediate (Langsung Non-Stop)" else "$activeCount dari $totalIntermediate Terminal Aktif",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isNonStop) SuccessGreen else PrimaryBlue
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Rincian Titik Terminal",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Centang terminal untuk singgah, atau biarkan tidak tercentang untuk dilewati langsung:",
                fontSize = 11.sp,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(currentPath.size) { index ->
            val terminal = currentPath[index]
            val isOrigin = index == 0
            val isDestination = index == currentPath.size - 1
            val isIntermediate = !isOrigin && !isDestination
            val isTransitActive = !uiState.isExpress && (terminal.id in uiState.activeTransitIds)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .then(
                        if (isIntermediate) {
                            Modifier.clickable { onToggleTransitStop(terminal.id) }
                        } else {
                            Modifier
                        }
                    ),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        isOrigin || isDestination -> IceBlue
                        isTransitActive -> SurfaceWhite
                        else -> Color(0xFFF8FAFC)
                    }
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                        if (isOrigin || isDestination || isTransitActive) PrimaryBlue else CardBorder
                    )
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isOrigin -> PrimaryBlue
                                        isDestination -> SuccessGreen
                                        isTransitActive -> PrimaryBlueDark
                                        else -> CardBorder
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "${terminal.city} (${terminal.name})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Text(
                                text = when {
                                    isOrigin -> "Titik Keberangkatan Awal"
                                    isDestination -> "Titik Tujuan Akhir"
                                    isTransitActive -> "Singgah (Naik & Turun Penumpang)"
                                    else -> "Dilewati Saja (Bypass / Tanpa Singgah)"
                                },
                                fontSize = 11.sp,
                                color = if (isTransitActive || isOrigin || isDestination) PrimaryBlue else TextLight
                            )
                        }
                    }

                    if (isIntermediate) {
                        Checkbox(
                            checked = isTransitActive,
                            onCheckedChange = { onToggleTransitStop(terminal.id) },
                            colors = CheckboxDefaults.colors(checkedColor = PrimaryBlue)
                        )
                    }
                }
            }

            if (index < currentPath.size - 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 28.dp, top = 2.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(18.dp)
                            .background(PrimaryBlue.copy(alpha = 0.35f))
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
