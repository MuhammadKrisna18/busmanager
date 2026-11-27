package com.example.busmanager.presentation.route.wizard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import com.example.busmanager.domain.route.model.RouteCategory
import com.example.busmanager.domain.route.model.TerminalDepartureSlot
import com.example.busmanager.presentation.route.wizard.RouteWizardUiState
import com.example.busmanager.presentation.route.wizard.SlotTimeFilter
import com.example.busmanager.ui.theme.AppDimens
import com.example.busmanager.ui.theme.CardBorder
import com.example.busmanager.ui.theme.IceBlue
import com.example.busmanager.ui.theme.PrimaryBlue
import com.example.busmanager.ui.theme.PrimaryBlueDark
import com.example.busmanager.ui.theme.SuccessGreen
import com.example.busmanager.ui.theme.SurfaceWhite
import com.example.busmanager.ui.theme.TextDark
import com.example.busmanager.ui.theme.TextMuted

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Slide4DepartureSlots(
    uiState: RouteWizardUiState,
    onSelectSlot: (TerminalDepartureSlot) -> Unit,
    onSetTargetRitCount: (Int) -> Unit,
    onSetTimeFilter: (SlotTimeFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val originCity = uiState.selectedOrigin?.city ?: "Asal"
    val destCity = uiState.selectedDestination?.city ?: "Tujuan"
    val isAkap = uiState.routeCategory == RouteCategory.AKAP

    val filteredSlots = uiState.availableDepartureSlots.filter { slot ->
        when (uiState.selectedTimeFilter) {
            SlotTimeFilter.ALL -> true
            else -> slot.hour in uiState.selectedTimeFilter.startHour..uiState.selectedTimeFilter.endHour
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Slot Keberangkatan Terminal",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(AppDimens.radiusPill))
                        .background(if (isAkap) Color(0xFFFEF3C7) else IceBlue)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isAkap) "Mode AKAP" else "Mode AKDP",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isAkap) Color(0xFFB45309) else PrimaryBlueDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Pilih slot resmi keberangkatan bus dari Terminal $originCity menuju $destCity. Terminal aktif 24 jam dengan jeda per 15 menit.",
                fontSize = 12.sp,
                color = TextMuted,
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        if (!isAkap) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(AppDimens.radiusMedium),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Target Ritase Harian (AKDP)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = uiState.targetRitCount == 1,
                                onClick = { onSetTargetRitCount(1) },
                                label = { Text("1 PP (1x Pulang-Pergi)", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = IceBlue,
                                    selectedLabelColor = PrimaryBlueDark
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = uiState.targetRitCount == 2,
                                onClick = { onSetTargetRitCount(2) },
                                label = { Text("2 PP (Jeda 1 Jam di Asal)", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = IceBlue,
                                    selectedLabelColor = PrimaryBlueDark
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SlotTimeFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = uiState.selectedTimeFilter == filter,
                        onClick = { onSetTimeFilter(filter) },
                        label = { Text(filter.label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryBlue,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        item {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filteredSlots.forEach { slot ->
                    val isSelected = uiState.selectedDepartureSlot?.slotId == slot.slotId
                    val bg = when {
                        isSelected -> PrimaryBlue
                        !slot.isAvailable -> Color(0xFFFEE2E2)
                        else -> SurfaceWhite
                    }
                    val textCol = when {
                        isSelected -> Color.White
                        !slot.isAvailable -> Color(0xFF991B1B)
                        else -> TextDark
                    }
                    val borderCol = when {
                        isSelected -> PrimaryBlue
                        !slot.isAvailable -> Color(0xFFFCA5A5)
                        else -> CardBorder
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(bg)
                            .border(1.dp, borderCol, RoundedCornerShape(8.dp))
                            .clickable { onSelectSlot(slot) }
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (isSelected) {
                                Text(
                                    text = "✓",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = slot.timeFormatted,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = textCol
                            )
                            if (!slot.isAvailable) {
                                Text(
                                    text = "Terisi",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFDC2626)
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        uiState.dailySchedulePlan?.let { plan ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(AppDimens.radiusMedium),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFBBF7D0)))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(SuccessGreen)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Ringkasan Jadwal Harian (${plan.targetRitCount} PP)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF166534)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        plan.legs.forEach { leg ->
                            val label = "Rit ${leg.ritNumber} ${if (leg.direction == com.example.busmanager.domain.simulation.model.RouteDirection.ORIGIN_TO_DESTINATION) "Pergi" else "Pulang"}"
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextDark
                                )
                                Text(
                                    text = "${leg.departureFormatted} ➔ ${leg.arrivalFormatted} WIB",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlueDark
                                )
                            }
                        }

                        if (plan.targetRitCount == 2) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Jeda istirahat pangkalan antar-PP: 1 Jam (${plan.returnLeg1?.arrivalFormatted} - ${plan.outboundLeg2?.departureFormatted} WIB)",
                                fontSize = 10.sp,
                                color = Color(0xFF15803D),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
