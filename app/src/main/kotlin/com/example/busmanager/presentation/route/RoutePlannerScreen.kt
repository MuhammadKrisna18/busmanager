package com.example.busmanager.presentation.route

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.driver.model.Driver
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.RoutePathType
import com.example.busmanager.domain.route.model.RoutePricing
import com.example.busmanager.domain.route.model.RouteSchedule
import com.example.busmanager.domain.route.model.RouteTripEstimate
import com.example.busmanager.domain.route.model.Terminal
import com.example.busmanager.domain.simulation.model.RouteDirection
import com.example.busmanager.presentation.route.components.EastJavaMapCanvas
import com.example.busmanager.presentation.route.components.RouteMapLegend
import com.example.busmanager.ui.theme.AccentBlue
import com.example.busmanager.ui.theme.AppDimens
import com.example.busmanager.ui.theme.BackgroundWhite
import com.example.busmanager.ui.theme.CardBorder
import com.example.busmanager.ui.theme.DisabledBackground
import com.example.busmanager.ui.theme.DisabledGray
import com.example.busmanager.ui.theme.IceBlue
import com.example.busmanager.ui.theme.PrimaryBlue
import com.example.busmanager.ui.theme.PrimaryBlueDark
import com.example.busmanager.ui.theme.SuccessGreen
import com.example.busmanager.ui.theme.SuccessGreenLight
import com.example.busmanager.ui.theme.SurfaceWhite
import com.example.busmanager.ui.theme.TextDark
import com.example.busmanager.ui.theme.TextLight
import com.example.busmanager.ui.theme.TextMuted
import com.example.busmanager.ui.theme.WarningAmber
import com.example.busmanager.ui.theme.WarningAmberLight

@Composable
fun RoutePlannerScreen(
    viewModel: RoutePlannerViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            RouteTopBar(onNavigateBack = onNavigateBack)
        },
        containerColor = BackgroundWhite,
        modifier = modifier
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .navigationBarsPadding()
                    .padding(horizontal = AppDimens.paddingMedium),
                contentPadding = PaddingValues(vertical = AppDimens.paddingSmall),
                verticalArrangement = Arrangement.spacedBy(AppDimens.paddingMedium)
            ) {
                item {
                    TerminalSelectorCard(
                        terminals = uiState.terminals,
                        selectedOrigin = uiState.selectedOrigin,
                        selectedDestination = uiState.selectedDestination,
                        onOriginSelect = { viewModel.selectOrigin(it) },
                        onDestinationSelect = { viewModel.selectDestination(it) },
                        onSwapClick = { viewModel.swapOriginDestination() }
                    )
                }

                item {
                    PathTypeFilterRow(
                        selectedPathType = uiState.preferredPathType,
                        onPathTypeSelected = { viewModel.setPreferredPathType(it) }
                    )
                }

                item {
                    Text(
                        text = "Pilihan Koridor Jalur (${uiState.availableRoutes.size})",
                        color = TextDark,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (uiState.availableRoutes.isEmpty()) {
                    item {
                        EmptyRouteNotice()
                    }
                } else {
                    items(uiState.availableRoutes, key = { it.id }) { route ->
                        RouteOptionCard(
                            route = route,
                            isSelected = uiState.selectedRoute?.id == route.id,
                            onSelect = { viewModel.selectRoute(route) }
                        )
                    }
                }

                if (uiState.selectedRoute != null) {
                    item {
                        if (uiState.successMessage != null) {
                            StatusBanner(
                                message = uiState.successMessage!!,
                                isSuccess = true,
                                onDismiss = { viewModel.clearMessages() }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        if (uiState.errorMessage != null && !uiState.isAddScheduleDialogOpen) {
                            StatusBanner(
                                message = uiState.errorMessage!!,
                                isSuccess = false,
                                onDismiss = { viewModel.clearMessages() }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        RouteDetailTabRow(
                            selectedTab = uiState.activeTab,
                            onTabSelected = { viewModel.selectTab(it) }
                        )
                    }

                    when (uiState.activeTab) {
                        RouteDetailTab.OVERVIEW -> {
                            item {
                                EastJavaMapCanvas(
                                    terminals = uiState.terminals,
                                    allSegments = uiState.allSegments,
                                    selectedRoute = uiState.selectedRoute,
                                    selectedOrigin = uiState.selectedOrigin,
                                    selectedDestination = uiState.selectedDestination,
                                    transitTerminals = uiState.selectedRoute?.transitTerminals ?: emptyList(),
                                    onTerminalClick = { terminal ->
                                        viewModel.onTerminalMapClick(terminal)
                                    }
                                )
                            }
                            item {
                                RouteMapLegend()
                            }
                            if (uiState.tripEstimate != null) {
                                item {
                                    TripEstimateCard(estimate = uiState.tripEstimate!!)
                                }
                            }
                        }
                        RouteDetailTab.PRICING -> {
                            item {
                                PricingSectionCard(
                                    pricings = uiState.pricings,
                                    onEditPricing = { viewModel.openEditPricing(it) }
                                )
                            }
                        }
                        RouteDetailTab.SCHEDULES -> {
                            item {
                                SchedulesSectionCard(
                                    route = uiState.selectedRoute!!,
                                    schedules = uiState.schedules,
                                    buses = uiState.availableBuses,
                                    drivers = uiState.hiredDrivers,
                                    onAddScheduleClick = { viewModel.openAddScheduleDialog() },
                                    onAssignBusClick = { viewModel.openAssignBusDialog(it) },
                                    onDeleteScheduleClick = { viewModel.deleteSchedule(it) }
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(AppDimens.paddingLarge))
                }
            }
        }

        if (uiState.editingPricing != null) {
            EditPricingDialog(
                pricing = uiState.editingPricing!!,
                onDismiss = { viewModel.closeEditPricing() },
                onSave = { newAmount ->
                    viewModel.savePricing(uiState.editingPricing!!.id, newAmount)
                }
            )
        }

        if (uiState.isAddScheduleDialogOpen && uiState.selectedRoute != null) {
            AddScheduleDialog(
                route = uiState.selectedRoute!!,
                errorMessage = uiState.errorMessage,
                onDismiss = { viewModel.closeAddScheduleDialog() },
                onAddConfirm = { hour, minute, direction ->
                    viewModel.addSchedule(hour, minute, direction)
                }
            )
        }

        if (uiState.selectedScheduleForBusAssignment != null) {
            AssignScheduleBusDialog(
                schedule = uiState.selectedScheduleForBusAssignment!!,
                route = uiState.selectedRoute,
                buses = uiState.availableBuses,
                drivers = uiState.hiredDrivers,
                onDismiss = { viewModel.closeAssignBusDialog() },
                onAssignConfirm = { busId ->
                    viewModel.assignBusToSchedule(uiState.selectedScheduleForBusAssignment!!.id, busId)
                }
            )
        }
    }
}

@Composable
private fun RouteTopBar(onNavigateBack: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = SurfaceWhite,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppDimens.paddingSmall, vertical = AppDimens.paddingSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Text(text = "←", fontSize = 24.sp, color = PrimaryBlue, fontWeight = FontWeight.Bold)
            }

            Column(modifier = Modifier.padding(start = 4.dp)) {
                Text(
                    text = "Buka & Atur Trayek",
                    color = TextDark,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Penetapan Rute, Tarif Antar Titik & Jadwal Armada",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun RouteDetailTabRow(
    selectedTab: RouteDetailTab,
    onTabSelected: (RouteDetailTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppDimens.radiusPill))
            .background(IceBlue)
            .padding(4.dp)
    ) {
        RouteDetailTab.entries.forEach { tab ->
            val isSelected = selectedTab == tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(AppDimens.radiusPill))
                    .background(if (isSelected) PrimaryBlue else Color.Transparent)
                    .clickable { onTabSelected(tab) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tab.title,
                    color = if (isSelected) Color.White else PrimaryBlueDark,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun PricingSectionCard(
    pricings: List<RoutePricing>,
    onEditPricing: (RoutePricing) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDimens.radiusMedium),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(AppDimens.paddingMedium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Tarif Tiket Antar Titik Naik/Turun",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Text(
                        text = "Dasar Rp 50.000 / lompatan kota (Dapat disesuaikan)",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (pricings.isEmpty()) {
                Text(
                    text = "Sedang menghitung tarif tiket rute...",
                    fontSize = 13.sp,
                    color = TextMuted
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    pricings.forEach { pricing ->
                        PricingItemRow(
                            pricing = pricing,
                            onEditClick = { onEditPricing(pricing) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PricingItemRow(
    pricing: RoutePricing,
    onEditClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppDimens.radiusSmall))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, CardBorder, RoundedCornerShape(AppDimens.radiusSmall))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${pricing.originCity} ➔ ${pricing.destinationCity}",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = TextDark
            )
            Text(
                text = "Tarif: ${pricing.price.formatRupiah()}",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = PrimaryBlue
            )
        }

        OutlinedButton(
            onClick = onEditClick,
            shape = RoundedCornerShape(AppDimens.radiusPill),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(text = "Ubah", fontSize = 11.sp, color = PrimaryBlue)
        }
    }
}

@Composable
private fun SchedulesSectionCard(
    route: BusRoute,
    schedules: List<RouteSchedule>,
    buses: List<Bus>,
    drivers: List<Driver>,
    onAddScheduleClick: () -> Unit,
    onAssignBusClick: (RouteSchedule) -> Unit,
    onDeleteScheduleClick: (String) -> Unit
) {
    val forwardSchedules = schedules.filter { it.direction == RouteDirection.ORIGIN_TO_DESTINATION }
    val backwardSchedules = schedules.filter { it.direction == RouteDirection.DESTINATION_TO_ORIGIN }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDimens.radiusMedium),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(AppDimens.paddingMedium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Jadwal & Penugasan Armada",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Text(
                        text = "Atur jadwal keberangkatan A->B dan B->A",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                Button(
                    onClick = onAddScheduleClick,
                    shape = RoundedCornerShape(AppDimens.radiusPill),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(text = "+ Jadwal", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Jadwal Keberangkatan menuju ${route.destinationTerminal.city} dari ${route.originTerminal.city}",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = PrimaryBlueDark
            )
            Spacer(modifier = Modifier.height(6.dp))

            if (forwardSchedules.isEmpty()) {
                EmptyScheduleNotice(text = "Belum ada jadwal keberangkatan dari ${route.originTerminal.city}.")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    forwardSchedules.forEach { schedule ->
                        ScheduleItemCard(
                            schedule = schedule,
                            route = route,
                            buses = buses,
                            drivers = drivers,
                            onAssignBusClick = { onAssignBusClick(schedule) },
                            onDeleteClick = { onDeleteScheduleClick(schedule.id) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Jadwal Keberangkatan menuju ${route.originTerminal.city} dari ${route.destinationTerminal.city}",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = PrimaryBlueDark
            )
            Spacer(modifier = Modifier.height(6.dp))

            if (backwardSchedules.isEmpty()) {
                EmptyScheduleNotice(text = "Belum ada jadwal keberangkatan dari ${route.destinationTerminal.city}.")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    backwardSchedules.forEach { schedule ->
                        ScheduleItemCard(
                            schedule = schedule,
                            route = route,
                            buses = buses,
                            drivers = drivers,
                            onAssignBusClick = { onAssignBusClick(schedule) },
                            onDeleteClick = { onDeleteScheduleClick(schedule.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleItemCard(
    schedule: RouteSchedule,
    route: BusRoute,
    buses: List<Bus>,
    drivers: List<Driver>,
    onAssignBusClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val assignedBus = buses.find { it.id == schedule.assignedBusId }
    val assignedDriver = if (assignedBus?.driverId != null) {
        drivers.find { it.id == assignedBus.driverId }
    } else null

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppDimens.radiusSmall))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, CardBorder, RoundedCornerShape(AppDimens.radiusSmall))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = schedule.formatScheduleInfo(route),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(2.dp))

            if (assignedBus != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Bus ${assignedBus.busCode}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = SuccessGreen
                    )
                    if (assignedDriver != null) {
                        Text(
                            text = " • Sopir: ${assignedDriver.name}",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    } else {
                        Text(
                            text = " • Belum ada sopir",
                            fontSize = 11.sp,
                            color = Color(0xFFDC2626)
                        )
                    }
                }
            } else {
                Text(
                    text = "Belum ada armada bertugas",
                    fontSize = 11.sp,
                    color = WarningAmber,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Button(
                onClick = onAssignBusClick,
                shape = RoundedCornerShape(AppDimens.radiusPill),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (assignedBus != null) PrimaryBlueDark else PrimaryBlue
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (assignedBus != null) "Ganti Bus" else "Tugaskan Bus",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(onClick = onDeleteClick, modifier = Modifier.size(32.dp)) {
                Text(text = "🗑️", fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun EmptyScheduleNotice(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppDimens.radiusSmall))
            .background(Color(0xFFF1F5F9))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, fontSize = 12.sp, color = TextMuted)
    }
}

@Composable
private fun EditPricingDialog(
    pricing: RoutePricing,
    onDismiss: () -> Unit,
    onSave: (Long) -> Unit
) {
    var priceText by remember { mutableStateOf(pricing.price.amount.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Ubah Tarif Tiket",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column {
                Text(
                    text = "${pricing.originCity} ➔ ${pricing.destinationCity}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = PrimaryBlueDark
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() }) {
                            priceText = input
                        }
                    },
                    label = { Text("Harga Tiket (Rupiah)") },
                    prefix = { Text("Rp ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = priceText.toLongOrNull() ?: pricing.price.amount
                    onSave(amount)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text(text = "Simpan Tarif")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Batal")
            }
        }
    )
}

@Composable
private fun AddScheduleDialog(
    route: BusRoute,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onAddConfirm: (hour: Int, minute: Int, direction: RouteDirection) -> Unit
) {
    var selectedDirection by remember { mutableStateOf(RouteDirection.ORIGIN_TO_DESTINATION) }
    var selectedHour by remember { mutableIntStateOf(7) }
    var selectedMinute by remember { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Tambah Jadwal Keberangkatan",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column {
                Text(
                    text = "Pilih Arah Keberangkatan:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedDirection == RouteDirection.ORIGIN_TO_DESTINATION,
                        onClick = { selectedDirection = RouteDirection.ORIGIN_TO_DESTINATION }
                    )
                    Text(
                        text = "Menuju ${route.destinationTerminal.city} dari ${route.originTerminal.city}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedDirection == RouteDirection.DESTINATION_TO_ORIGIN,
                        onClick = { selectedDirection = RouteDirection.DESTINATION_TO_ORIGIN }
                    )
                    Text(
                        text = "Menuju ${route.originTerminal.city} dari ${route.destinationTerminal.city}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Jam Keberangkatan (WIB):",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Jam", fontSize = 11.sp, color = TextMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        HourDropdown(
                            selectedHour = selectedHour,
                            onHourSelected = { selectedHour = it }
                        )
                    }

                    Text(text = ":", fontWeight = FontWeight.Bold, fontSize = 20.sp)

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Menit", fontSize = 11.sp, color = TextMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        MinuteDropdown(
                            selectedMinute = selectedMinute,
                            onMinuteSelected = { selectedMinute = it }
                        )
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorMessage,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFDC2626)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onAddConfirm(selectedHour, selectedMinute, selectedDirection) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text(text = "Tambah")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Batal")
            }
        }
    )
}

@Composable
private fun HourDropdown(selectedHour: Int, onHourSelected: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(AppDimens.radiusSmall)
        ) {
            Text(text = String.format("%02d", selectedHour), fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            (4..22).forEach { hour ->
                DropdownMenuItem(
                    text = { Text(text = String.format("%02d", hour)) },
                    onClick = {
                        onHourSelected(hour)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun MinuteDropdown(selectedMinute: Int, onMinuteSelected: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(AppDimens.radiusSmall)
        ) {
            Text(text = String.format("%02d", selectedMinute), fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            listOf(0, 15, 30, 45).forEach { minute ->
                DropdownMenuItem(
                    text = { Text(text = String.format("%02d", minute)) },
                    onClick = {
                        onMinuteSelected(minute)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun AssignScheduleBusDialog(
    schedule: RouteSchedule,
    route: BusRoute?,
    buses: List<Bus>,
    drivers: List<Driver>,
    onDismiss: () -> Unit,
    onAssignConfirm: (busId: String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (route != null) "Tugaskan Armada\n(${schedule.formatScheduleInfo(route)})" else "Tugaskan Armada (${schedule.formattedTime})",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Pilih bus untuk jam keberangkatan ini:",
                    fontSize = 13.sp,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (buses.isEmpty()) {
                    Text(
                        text = "Perusahaan belum memiliki armada bus.",
                        fontSize = 13.sp,
                        color = Color(0xFFDC2626)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.height(240.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(buses, key = { it.id }) { bus ->
                            val driver = drivers.find { it.id == bus.driverId }
                            val isSelected = schedule.assignedBusId == bus.id
                            BusScheduleCard(
                                bus = bus,
                                driver = driver,
                                isSelected = isSelected,
                                onSelect = { onAssignConfirm(bus.id) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Tutup")
            }
        }
    )
}

@Composable
private fun BusScheduleCard(
    bus: Bus,
    driver: Driver?,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(AppDimens.radiusSmall),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) IceBlue else SurfaceWhite
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) PrimaryBlue else CardBorder
        )
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bus ${bus.busCode}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = PrimaryBlueDark
                )
                if (isSelected) {
                    Text(
                        text = "✓ Bertugas",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                }
            }

            Text(
                text = bus.fullName,
                fontSize = 12.sp,
                color = TextDark
            )

            if (driver != null) {
                Text(
                    text = "👨‍✈️ Sopir: ${driver.name}",
                    fontSize = 11.sp,
                    color = SuccessGreen,
                    fontWeight = FontWeight.Medium
                )
            } else {
                Text(
                    text = "⚠️ Belum ada sopir",
                    fontSize = 11.sp,
                    color = WarningAmber
                )
            }
        }
    }
}

@Composable
private fun StatusBanner(
    message: String,
    isSuccess: Boolean,
    onDismiss: () -> Unit
) {
    val bgColor = if (isSuccess) SuccessGreenLight else Color(0xFFFEE2E2)
    val textColor = if (isSuccess) SuccessGreen else Color(0xFFDC2626)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onDismiss),
        shape = RoundedCornerShape(AppDimens.radiusSmall),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = textColor,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "✕",
                fontSize = 12.sp,
                color = textColor
            )
        }
    }
}

@Composable
private fun TerminalSelectorCard(
    terminals: List<Terminal>,
    selectedOrigin: Terminal?,
    selectedDestination: Terminal?,
    onOriginSelect: (Terminal) -> Unit,
    onDestinationSelect: (Terminal) -> Unit,
    onSwapClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDimens.radiusMedium),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(AppDimens.paddingMedium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Terminal Keberangkatan (Asal)",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    TerminalDropdownSelector(
                        terminals = terminals,
                        selected = selectedOrigin,
                        dotColor = SuccessGreen,
                        onSelect = onOriginSelect
                    )
                }

                IconButton(
                    onClick = onSwapClick,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(AccentBlue)
                ) {
                    Text(text = "⇄", fontSize = 18.sp, color = PrimaryBlueDark, fontWeight = FontWeight.Bold)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Terminal Tujuan (Akhir)",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    TerminalDropdownSelector(
                        terminals = terminals,
                        selected = selectedDestination,
                        dotColor = PrimaryBlue,
                        onSelect = onDestinationSelect
                    )
                }
            }
        }
    }
}

@Composable
private fun TerminalDropdownSelector(
    terminals: List<Terminal>,
    selected: Terminal?,
    dotColor: Color,
    onSelect: (Terminal) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(AppDimens.radiusSmall))
                .background(Color(0xFFF1F5F9))
                .border(1.dp, CardBorder, RoundedCornerShape(AppDimens.radiusSmall))
                .clickable { expanded = true }
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = selected?.city ?: "Pilih...",
                    color = TextDark,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
            Text(text = "▾", color = TextMuted, fontSize = 12.sp)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            terminals.forEach { terminal ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(
                                text = terminal.city,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = terminal.name,
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    },
                    onClick = {
                        onSelect(terminal)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun PathTypeFilterRow(
    selectedPathType: RoutePathType?,
    onPathTypeSelected: (RoutePathType?) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppDimens.paddingSmall)
    ) {
        FilterChip(
            selected = selectedPathType == null,
            onClick = { onPathTypeSelected(null) },
            label = { Text("Semua Jalur", fontSize = 12.sp) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = PrimaryBlue,
                selectedLabelColor = Color.White
            )
        )

        FilterChip(
            selected = selectedPathType == RoutePathType.TOLL,
            onClick = { onPathTypeSelected(RoutePathType.TOLL) },
            label = { Text("Tol Trans-Jawa", fontSize = 12.sp) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = PrimaryBlue,
                selectedLabelColor = Color.White
            )
        )

        FilterChip(
            selected = selectedPathType == RoutePathType.NON_TOLL,
            onClick = { onPathTypeSelected(RoutePathType.NON_TOLL) },
            label = { Text("Arteri / Non-Tol", fontSize = 12.sp) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = PrimaryBlue,
                selectedLabelColor = Color.White
            )
        )
    }
}

@Composable
private fun RouteOptionCard(
    route: BusRoute,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val borderColor = if (isSelected) PrimaryBlue else CardBorder
    val borderWidth = if (isSelected) 2.dp else 1.dp
    val containerColor = if (isSelected) IceBlue else SurfaceWhite

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(AppDimens.radiusMedium),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = androidx.compose.foundation.BorderStroke(borderWidth, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(AppDimens.paddingMedium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = route.name,
                    color = TextDark,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(AppDimens.radiusPill))
                        .background(if (route.pathType == RoutePathType.TOLL) AccentBlue else Color(0xFFF1F5F9))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = route.pathType.displayName,
                        color = if (route.pathType == RoutePathType.TOLL) PrimaryBlueDark else TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MetricSnippet(label = "Jarak", value = "${route.totalDistanceKm.toInt()} km")
                MetricSnippet(
                    label = "Waktu",
                    value = "${route.totalEstimatedDurationMinutes / 60}j ${route.totalEstimatedDurationMinutes % 60}m"
                )
                MetricSnippet(
                    label = "Biaya Tol",
                    value = if (route.totalTollFee.amount > 0) route.totalTollFee.formatRupiah() else "Gratis"
                )
            }

            if (route.transitTerminals.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFFEF3C7).copy(alpha = 0.5f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🚌 Titik Naik/Turun:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = route.transitTerminals.joinToString(" • ") { "${it.name} (${it.city})" },
                        fontSize = 11.sp,
                        color = Color(0xFF78350F)
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricSnippet(label: String, value: String) {
    Column {
        Text(text = label, color = TextLight, fontSize = 10.sp)
        Text(text = value, color = TextDark, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun TripEstimateCard(estimate: RouteTripEstimate) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDimens.radiusMedium),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(AppDimens.paddingMedium)) {
            Text(
                text = "Estimasi Operasional Perjalanan",
                color = TextDark,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Konsumsi Solar (${String.format("%.1f", estimate.estimatedFuelLiters)} L)", color = TextMuted, fontSize = 12.sp)
                Text(text = estimate.estimatedFuelCost.formatRupiah(), color = TextDark, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Tarif e-Toll", color = TextMuted, fontSize = 12.sp)
                Text(text = estimate.tollFee.formatRupiah(), color = TextDark, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Retribusi Terminal & Peron", color = TextMuted, fontSize = 12.sp)
                Text(text = estimate.terminalRetributionFee.formatRupiah(), color = TextDark, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(CardBorder))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Total Biaya Perjalanan", color = TextDark, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(text = estimate.totalOperationalCost.formatRupiah(), color = PrimaryBlue, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun EmptyRouteNotice() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDimens.radiusMedium),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.paddingLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "🗺️", fontSize = 32.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tidak Ada Rute Langsung",
                color = TextDark,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Pilih kombinasi terminal asal dan tujuan lain di peta Jawa Timur di atas.",
                color = TextMuted,
                fontSize = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
