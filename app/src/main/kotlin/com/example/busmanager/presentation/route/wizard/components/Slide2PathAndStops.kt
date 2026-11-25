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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import com.example.busmanager.domain.route.model.MapCoordinate
import com.example.busmanager.domain.route.model.RoutePathType
import com.example.busmanager.presentation.route.components.EastJavaMapCanvas
import com.example.busmanager.presentation.route.components.MapSegmentToggle
import com.example.busmanager.presentation.route.components.MapTerminalToggle
import com.example.busmanager.presentation.route.wizard.RouteWizardUiState
import com.example.busmanager.ui.theme.AccentBlue
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
    onSetSegmentPathType: (String, String, RoutePathType) -> Unit,
    onSetIsExpress: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentPath = uiState.selectedPathNodes.ifEmpty {
        uiState.availablePaths.getOrNull(uiState.selectedPathIndex) ?: emptyList()
    }
    val route = uiState.builtRoute

    val segmentToggles = remember(currentPath, uiState.segmentPathTypes, route) {
        if (currentPath.size < 2) emptyList() else {
            (0 until currentPath.size - 1).map { i ->
                val from = currentPath[i]
                val to = currentPath[i + 1]
                val midX = (from.coordinate.x + to.coordinate.x) / 2f
                val midY = (from.coordinate.y + to.coordinate.y) / 2f
                val midLat = (from.coordinate.latitude + to.coordinate.latitude) / 2.0
                val midLng = (from.coordinate.longitude + to.coordinate.longitude) / 2.0
                val segType = route?.segments?.getOrNull(i)?.pathType
                    ?: uiState.segmentPathTypes[Pair(from.id, to.id)]
                    ?: RoutePathType.TOLL
                MapSegmentToggle(
                    fromTerminalId = from.id,
                    toTerminalId = to.id,
                    midCoordinate = MapCoordinate(midX, midY, midLat, midLng),
                    currentPathType = segType
                )
            }
        }
    }

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
                text = "Sentuh pill Tol/Arteri dan Singgah/Lewat langsung di peta untuk mengubah konfigurasi",
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
                EastJavaMapCanvas(
                    terminals = uiState.allTerminals,
                    allSegments = uiState.allSegments,
                    selectedRoute = route,
                    selectedOrigin = route?.originTerminal ?: uiState.selectedOrigin,
                    selectedDestination = route?.destinationTerminal ?: uiState.selectedDestination,
                    transitTerminals = route?.transitTerminals ?: emptyList(),
                    segmentToggles = segmentToggles,
                    terminalToggles = terminalToggles,
                    onSegmentToggleClick = { fromId, toId ->
                        val currentType = uiState.segmentPathTypes[Pair(fromId, toId)] ?: route?.segments?.find {
                            it.fromTerminalId == fromId && it.toTerminalId == toId
                        }?.pathType ?: RoutePathType.TOLL
                        val newType = if (currentType == RoutePathType.TOLL) RoutePathType.NON_TOLL else RoutePathType.TOLL
                        onSetSegmentPathType(fromId, toId, newType)
                    },
                    onTerminalToggleClick = { termId ->
                        onToggleTransitStop(termId)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
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
                                color = PrimaryBlueDark
                            )
                            Text(
                                text = "Live Metrics",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceWhite)
                                .padding(horizontal = 8.dp, vertical = 8.dp),
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
                                Text(text = "Biaya Tol", fontSize = 10.sp, color = TextMuted)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (route.totalTollFee.amount > 0) route.totalTollFee.formatRupiah() else "Rp 0 (Arteri)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (route.totalTollFee.amount > 0) Color(0xFFD97706) else SuccessGreen
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }
        }

        item {
            Text(
                text = "Pola Perjalanan Koridor",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tentukan apakah armada berjalan langsung express atau berhenti di halte transit",
                fontSize = 11.sp,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.isExpress,
                    onClick = { onSetIsExpress(true) },
                    label = { Text("⚡ Jalur Langsung Express") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IceBlue,
                        selectedLabelColor = PrimaryBlueDark
                    )
                )
                FilterChip(
                    selected = !uiState.isExpress,
                    onClick = { onSetIsExpress(false) },
                    label = { Text("🚏 Berhenti di Halte Transit") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IceBlue,
                        selectedLabelColor = PrimaryBlueDark
                    )
                )
            }
            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Daftar Titik & Ruas Koridor",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Pengaturan detail titik singgah dan jenis jalan tiap ruas jalan",
                fontSize = 11.sp,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(10.dp))
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
                    .padding(vertical = 4.dp),
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                                    isTransitActive -> "Halte Naik & Turun Penumpang"
                                    else -> "Dilewati (Tidak Berhenti)"
                                },
                                fontSize = 11.sp,
                                color = if (isTransitActive || isOrigin || isDestination) PrimaryBlue else TextLight
                            )
                        }
                    }

                    if (isIntermediate) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isTransitActive) "Singgah" else "Lewat",
                                fontSize = 11.sp,
                                color = if (isTransitActive) SuccessGreen else TextLight,
                                fontWeight = FontWeight.Medium
                            )
                            Checkbox(
                                checked = isTransitActive,
                                onCheckedChange = { onToggleTransitStop(terminal.id) },
                                colors = CheckboxDefaults.colors(checkedColor = PrimaryBlue)
                            )
                        }
                    }
                }
            }

            if (index < currentPath.size - 1) {
                val nextTerminal = currentPath[index + 1]
                val currentSegmentType = uiState.segmentPathTypes[Pair(terminal.id, nextTerminal.id)] ?: route?.segments?.getOrNull(index)?.pathType ?: RoutePathType.TOLL

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(28.dp)
                            .background(CardBorder)
                    )

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        SegmentTypeBadge(
                            label = "Tol Cepat",
                            isSelected = currentSegmentType == RoutePathType.TOLL,
                            onClick = { onSetSegmentPathType(terminal.id, nextTerminal.id, RoutePathType.TOLL) }
                        )
                        SegmentTypeBadge(
                            label = "Arteri (Gratis)",
                            isSelected = currentSegmentType == RoutePathType.NON_TOLL,
                            onClick = { onSetSegmentPathType(terminal.id, nextTerminal.id, RoutePathType.NON_TOLL) }
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SegmentTypeBadge(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) PrimaryBlue else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else TextMuted
        )
    }
}