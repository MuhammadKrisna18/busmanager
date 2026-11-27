package com.example.busmanager.presentation.garage.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.busmanager.domain.driver.model.Driver
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.DailySchedulePlan
import com.example.busmanager.domain.route.model.RouteCategory
import com.example.busmanager.domain.route.model.RoutePathType
import com.example.busmanager.domain.route.model.TerminalDepartureSlot
import com.example.busmanager.domain.simulation.model.RouteDirection
import com.example.busmanager.presentation.garage.QuickAssignState
import com.example.busmanager.presentation.route.wizard.SlotTimeFilter
import com.example.busmanager.presentation.route.wizard.components.DepartureSlotGrid
import com.example.busmanager.presentation.route.wizard.components.ScheduleTimelineCard
import com.example.busmanager.ui.theme.AppDimens
import com.example.busmanager.ui.theme.CardBorder
import com.example.busmanager.ui.theme.IceBlue
import com.example.busmanager.ui.theme.PrimaryBlue
import com.example.busmanager.ui.theme.PrimaryBlueDark
import com.example.busmanager.ui.theme.SurfaceWhite
import com.example.busmanager.ui.theme.TextDark
import com.example.busmanager.ui.theme.TextMuted

@Composable
fun QuickAssignDialog(
    state: QuickAssignState,
    onDismiss: () -> Unit,
    onSelectRoute: (BusRoute) -> Unit,
    onSelectDriver: (Driver) -> Unit,
    onSetDirection: (RouteDirection) -> Unit,
    onSelectSlot: (TerminalDepartureSlot) -> Unit,
    onSetTimeFilter: (SlotTimeFilter) -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.90f)
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                QuickAssignHeader(
                    busCode = state.bus.busCode,
                    busClass = state.bus.body.busClass.displayName,
                    onDismiss = onDismiss
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        QuickAssignRouteSection(
                            routes = state.availableRoutes,
                            selectedRoute = state.selectedRoute,
                            onSelectRoute = onSelectRoute
                        )
                    }

                    item {
                        QuickAssignDriverSection(
                            drivers = state.availableDrivers,
                            selectedDriver = state.selectedDriver,
                            onSelectDriver = onSelectDriver
                        )
                    }

                    item {
                        QuickAssignSlotSection(
                            route = state.selectedRoute,
                            direction = state.direction,
                            category = state.routeCategory,
                            slots = state.availableSlots,
                            selectedSlot = state.selectedSlot,
                            selectedTimeFilter = state.selectedTimeFilter,
                            onSetDirection = onSetDirection,
                            onSetTimeFilter = onSetTimeFilter,
                            onSelectSlot = onSelectSlot
                        )
                    }

                    item {
                        QuickAssignScheduleSection(
                            plan = state.dailySchedulePlan
                        )
                    }

                    item {
                        QuickAssignStatusCard(
                            conflictError = state.conflictError,
                            selectedSlot = state.selectedSlot
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                QuickAssignFooter(
                    isSubmitting = state.isSubmitting,
                    canSubmit = state.canSubmit,
                    onDismiss = onDismiss,
                    onConfirm = onConfirm
                )
            }
        }
    }
}

@Composable
private fun QuickAssignHeader(
    busCode: String,
    busClass: String,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "⚡ Penugasan Cepat ke Trayek",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Text(
                text = "Armada $busCode • $busClass",
                fontSize = 12.sp,
                color = PrimaryBlueDark,
                fontWeight = FontWeight.SemiBold
            )
        }

        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(IceBlue)
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✕",
                fontSize = 12.sp,
                color = PrimaryBlueDark,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun QuickAssignRouteSection(
    routes: List<BusRoute>,
    selectedRoute: BusRoute?,
    onSelectRoute: (BusRoute) -> Unit
) {
    Column {
        Text(
            text = "1. Pilih Trayek Operasional",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
        Spacer(modifier = Modifier.height(6.dp))
        if (routes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFEF2F2))
                    .padding(12.dp)
            ) {
                Text(
                    text = "⚠️ Belum ada trayek aktif. Silakan buka trayek terlebih dahulu di menu Buka Trayek.",
                    fontSize = 12.sp,
                    color = Color(0xFFDC2626),
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                routes.forEach { route ->
                    val isSelected = selectedRoute?.id == route.id
                    val corridorName = if (route.pathType == RoutePathType.TOLL) "Jalan Tol" else "Arteri Non-Tol"
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectRoute(route) },
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) IceBlue else Color(0xFFF8FAFC)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            if (isSelected) 2.dp else 1.dp,
                            if (isSelected) PrimaryBlue else CardBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = route.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                                Text(
                                    text = "${route.originTerminal.city} ➔ ${route.destinationTerminal.city}",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (route.pathType == RoutePathType.TOLL) PrimaryBlue else Color(0xFF64748B))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = corridorName,
                                    fontSize = 10.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickAssignDriverSection(
    drivers: List<Driver>,
    selectedDriver: Driver?,
    onSelectDriver: (Driver) -> Unit
) {
    Column {
        Text(
            text = "2. Pilih Sopir Siap Dinas",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
        Spacer(modifier = Modifier.height(6.dp))
        if (drivers.isEmpty()) {
            Text(
                text = "⚠️ Tidak ada sopir siap dinas. Silakan rekrut sopir di menu Sopir.",
                fontSize = 12.sp,
                color = Color(0xFFB45309),
                fontWeight = FontWeight.SemiBold
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                drivers.forEach { driver ->
                    val isSelected = selectedDriver?.id == driver.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectDriver(driver) },
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) IceBlue else Color(0xFFF8FAFC)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            if (isSelected) 2.dp else 1.dp,
                            if (isSelected) PrimaryBlue else CardBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "👨‍✈️", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = driver.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                            }
                            Text(
                                text = "${driver.wagePerDay.formatRupiah()} / hari",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryBlueDark
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickAssignSlotSection(
    route: BusRoute?,
    direction: RouteDirection,
    category: RouteCategory,
    slots: List<TerminalDepartureSlot>,
    selectedSlot: TerminalDepartureSlot?,
    selectedTimeFilter: SlotTimeFilter,
    onSetDirection: (RouteDirection) -> Unit,
    onSetTimeFilter: (SlotTimeFilter) -> Unit,
    onSelectSlot: (TerminalDepartureSlot) -> Unit
) {
    val originCity = route?.originTerminal?.city ?: "Asal"
    val destCity = route?.destinationTerminal?.city ?: "Tujuan"
    val isOutbound = direction == RouteDirection.ORIGIN_TO_DESTINATION
    val isAkap = category == RouteCategory.AKAP
    val currentOriginCity = if (isOutbound) originCity else destCity
    val currentDestCity = if (isOutbound) destCity else originCity

    val filteredSlots = slots.filter { slot ->
        when (selectedTimeFilter) {
            SlotTimeFilter.ALL -> true
            else -> slot.hour in selectedTimeFilter.startHour..selectedTimeFilter.endHour
        }
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "3. Arah & Slot Keberangkatan",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(AppDimens.radiusPill))
                    .background(if (isAkap) Color(0xFFFEF3C7) else IceBlue)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = if (isAkap) "Mode AKAP" else "Mode AKDP",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isAkap) Color(0xFFB45309) else PrimaryBlueDark
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = isOutbound,
                onClick = { onSetDirection(RouteDirection.ORIGIN_TO_DESTINATION) },
                label = { Text("$originCity ➔ $destCity", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = IceBlue,
                    selectedLabelColor = PrimaryBlueDark
                ),
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = !isOutbound,
                onClick = { onSetDirection(RouteDirection.DESTINATION_TO_ORIGIN) },
                label = { Text("$destCity ➔ $originCity", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = IceBlue,
                    selectedLabelColor = PrimaryBlueDark
                ),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isAkap) Color(0xFFFFFBEB) else Color(0xFFF0F9FF)
            ),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(
                    if (isAkap) Color(0xFFFDE68A) else Color(0xFFBAE6FD)
                )
            )
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = if (isAkap) {
                        "Terminal $currentOriginCity ➔ $currentDestCity. Ketentuan AKAP: Istirahat wajib 5 jam di kota tujuan (1 PP per hari)."
                    } else {
                        "Terminal $currentOriginCity ➔ $currentDestCity. Ketentuan AKDP: Istirahat 30 mnt di tujuan dan jeda 1 jam antar-PP di pangkalan asal."
                    },
                    fontSize = 10.sp,
                    color = if (isAkap) Color(0xFF92400E) else Color(0xFF0369A1),
                    lineHeight = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SlotTimeFilter.entries.forEach { filter ->
                FilterChip(
                    selected = selectedTimeFilter == filter,
                    onClick = { onSetTimeFilter(filter) },
                    label = { Text(filter.label, fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryBlue,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        DepartureSlotGrid(
            slots = filteredSlots,
            selectedSlot = selectedSlot,
            onSelectSlot = onSelectSlot
        )
    }
}

@Composable
private fun QuickAssignScheduleSection(
    plan: DailySchedulePlan?
) {
    Column {
        Text(
            text = "4. Jadwal Operasional & Istirahat",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
        Spacer(modifier = Modifier.height(6.dp))

        plan?.let {
            ScheduleTimelineCard(plan = it)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun QuickAssignStatusCard(
    conflictError: String?,
    selectedSlot: TerminalDepartureSlot?
) {
    val isError = conflictError != null
    val isMissingSlot = selectedSlot == null

    val bg = when {
        isError -> Color(0xFFFEE2E2)
        isMissingSlot -> Color(0xFFFEF3C7)
        else -> Color(0xFFDCFCE7)
    }
    val borderCol = when {
        isError -> Color(0xFFDC2626)
        isMissingSlot -> Color(0xFFD97706)
        else -> Color(0xFF16A34A)
    }
    val textCol = when {
        isError -> Color(0xFF991B1B)
        isMissingSlot -> Color(0xFF92400E)
        else -> Color(0xFF166534)
    }
    val icon = when {
        isError -> "⚠️"
        isMissingSlot -> "ℹ️"
        else -> "✅"
    }
    val message = when {
        conflictError != null -> conflictError
        selectedSlot == null -> "Pilih slot keberangkatan terminal yang tersedia."
        else -> "Slot ${selectedSlot.formattedTime} WIB valid dan tersedia! Tidak ada bentrok armada pada rute dan jam ini."
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderCol)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = message,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = textCol
            )
        }
    }
}

@Composable
private fun QuickAssignFooter(
    isSubmitting: Boolean,
    canSubmit: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = onDismiss,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text("Batal", color = TextMuted, fontSize = 12.sp)
        }

        Button(
            onClick = onConfirm,
            enabled = canSubmit,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.weight(1.4f),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            Text(
                text = if (isSubmitting) "Menugaskan..." else "Tugaskan Sekarang",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color.White
            )
        }
    }
}
