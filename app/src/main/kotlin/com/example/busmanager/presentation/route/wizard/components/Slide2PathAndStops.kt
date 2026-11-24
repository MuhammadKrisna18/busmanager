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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmanager.domain.route.model.RoutePathType
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
import com.example.busmanager.ui.theme.WarningAmber

@Composable
fun Slide2PathAndStops(
    uiState: RouteWizardUiState,
    onSelectPath: (Int) -> Unit,
    onToggleTransitStop: (String) -> Unit,
    onSetSegmentPathType: (String, String, RoutePathType) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentPath = uiState.availablePaths.getOrNull(uiState.selectedPathIndex) ?: emptyList()
    val route = uiState.builtRoute

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        if (uiState.availablePaths.size > 1) {
            item {
                Text(
                    text = "Pilih Koridor Jalur",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.availablePaths.forEachIndexed { index, path ->
                        val isSelected = index == uiState.selectedPathIndex
                        val viaCity = if (path.size > 2) path[1].city else "Langsung"

                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectPath(index) },
                            label = {
                                Text(
                                    text = "Jalur ${index + 1} (Via $viaCity)",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = IceBlue,
                                selectedLabelColor = PrimaryBlueDark
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) PrimaryBlue else CardBorder
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }
        }

        if (route != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = route.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val hours = route.totalEstimatedDurationMinutes / 60
                        val mins = route.totalEstimatedDurationMinutes % 60
                        val durationStr = if (hours > 0) "${hours}j ${mins}m" else "${mins} mnt"

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Jarak: ${route.totalDistanceKm.toInt()} km",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                            Text(
                                text = "Waktu: $durationStr",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                            Text(
                                text = "Tol: ${route.totalTollFee.formatRupiah()}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (route.totalTollFee.amount > 0) WarningAmber else SuccessGreen
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        item {
            Text(
                text = "Rangkaian Titik & Halte Naik-Turun",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tentukan titik singgah penumpang dan jenis jalan per segmen",
                fontSize = 12.sp,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        items(currentPath.size) { index ->
            val terminal = currentPath[index]
            val isOrigin = index == 0
            val isDestination = index == currentPath.size - 1
            val isIntermediate = !isOrigin && !isDestination
            val isTransitActive = terminal.id in uiState.activeTransitIds

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
                                text = if (isTransitActive) "Aktif" else "Lewat",
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
                val currentSegmentType = uiState.segmentPathTypes[Pair(terminal.id, nextTerminal.id)] ?: RoutePathType.TOLL

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
