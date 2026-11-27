package com.example.busmanager.presentation.route.wizard.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmanager.domain.route.model.RouteCategory
import com.example.busmanager.domain.route.model.TerminalDepartureSlot
import com.example.busmanager.presentation.route.wizard.RouteWizardUiState
import com.example.busmanager.presentation.route.wizard.SlotTimeFilter
import com.example.busmanager.ui.theme.AppDimens
import com.example.busmanager.ui.theme.IceBlue
import com.example.busmanager.ui.theme.PrimaryBlue
import com.example.busmanager.ui.theme.PrimaryBlueDark
import com.example.busmanager.ui.theme.SurfaceWhite
import com.example.busmanager.ui.theme.TextDark

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
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentPadding = PaddingValues(bottom = 48.dp)
    ) {
        item {
            ModeBadgeBanner(
                routeCategory = uiState.routeCategory,
                originCity = originCity,
                destCity = destCity
            )
            Spacer(modifier = Modifier.height(14.dp))
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
            DepartureSlotGrid(
                slots = filteredSlots,
                selectedSlot = uiState.selectedDepartureSlot,
                onSelectSlot = onSelectSlot
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        uiState.dailySchedulePlan?.let { plan ->
            item {
                ScheduleTimelineCard(plan = plan)
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
