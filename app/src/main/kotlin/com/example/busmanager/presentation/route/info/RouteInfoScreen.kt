package com.example.busmanager.presentation.route.info

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmanager.domain.driver.model.Driver
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.RoutePathType
import com.example.busmanager.ui.theme.AccentBlue
import com.example.busmanager.ui.theme.AppDimens
import com.example.busmanager.ui.theme.BackgroundWhite
import com.example.busmanager.ui.theme.CardBorder
import com.example.busmanager.ui.theme.IceBlue
import com.example.busmanager.ui.theme.PrimaryBlue
import com.example.busmanager.ui.theme.PrimaryBlueDark
import com.example.busmanager.ui.theme.SkyBlue
import com.example.busmanager.ui.theme.SuccessGreen
import com.example.busmanager.ui.theme.SuccessGreenLight
import com.example.busmanager.ui.theme.SurfaceWhite
import com.example.busmanager.ui.theme.TextDark
import com.example.busmanager.ui.theme.TextMuted
import com.example.busmanager.ui.theme.WarningAmber
import com.example.busmanager.ui.theme.WarningAmberLight

@Composable
fun RouteInfoScreen(
    viewModel: RouteInfoViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToNewRoute: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            RouteInfoTopBar(
                onNavigateBack = onNavigateBack,
                onNavigateToNewRoute = onNavigateToNewRoute
            )
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
        } else if (uiState.allRoutes.isEmpty()) {
            EmptyRouteInfoView(
                onNavigateToNewRoute = onNavigateToNewRoute,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = AppDimens.paddingMedium),
                contentPadding = PaddingValues(top = AppDimens.paddingSmall, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(AppDimens.paddingSmall)
            ) {
                if (uiState.message != null) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.clearMessage() },
                            shape = RoundedCornerShape(AppDimens.radiusSmall),
                            colors = CardDefaults.cardColors(containerColor = SuccessGreenLight)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = uiState.message!!,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = SuccessGreen
                                )
                                Text(text = "✕", fontSize = 12.sp, color = SuccessGreen)
                            }
                        }
                    }
                }

                item {
                    RouteFilterRow(
                        selectedFilter = uiState.selectedFilter,
                        totalCount = uiState.totalRouteCount,
                        operatingCount = uiState.operatingRouteCount,
                        standbyCount = uiState.standbyRouteCount,
                        onSelectFilter = { viewModel.setFilter(it) }
                    )
                }

                if (uiState.filteredRoutes.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            shape = RoundedCornerShape(AppDimens.radiusMedium),
                            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "🗺️", fontSize = 36.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tidak Ada Trayek Sesuai Filter",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                            }
                        }
                    }
                } else {
                    items(uiState.filteredRoutes, key = { it.operationalInfo.route.id }) { item ->
                        RouteDetailCard(
                            item = item,
                            onTogglePricings = { viewModel.togglePricingsExpanded(item.operationalInfo.route.id) },
                            onAssignBusClick = { viewModel.openAssignBusDialog(it) },
                            onAssignDriverClick = { route, bus -> viewModel.openAssignDriverDialog(route, bus) }
                        )
                    }
                }
            }

            if (uiState.selectedRouteForBusAssign != null) {
                AssignBusModal(
                    route = uiState.selectedRouteForBusAssign!!,
                    availableBuses = uiState.availableBuses,
                    onDismiss = { viewModel.closeAssignBusDialog() },
                    onBusSelected = { busId ->
                        viewModel.assignBusToRoute(busId, uiState.selectedRouteForBusAssign!!.id)
                    }
                )
            }

            if (uiState.selectedRouteForDriverAssign != null) {
                AssignDriverModal(
                    route = uiState.selectedRouteForDriverAssign!!,
                    targetBus = uiState.targetBusForDriverAssign,
                    availableDrivers = uiState.availableDrivers,
                    onDismiss = { viewModel.closeAssignDriverDialog() },
                    onDriverSelected = { driverId, busId ->
                        viewModel.assignDriverToBus(driverId, busId)
                    }
                )
            }
        }
    }
}

@Composable
private fun RouteInfoTopBar(
    onNavigateBack: () -> Unit,
    onNavigateToNewRoute: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = SurfaceWhite,
        shadowElevation = AppDimens.cardElevation
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppDimens.paddingMedium, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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

                Spacer(modifier = Modifier.width(AppDimens.paddingMedium))

                Column {
                    Text(
                        text = "📋 Informasi Trayek",
                        color = TextDark,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Daftar Trayek & Status Operasional PO",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Button(
                onClick = onNavigateToNewRoute,
                shape = RoundedCornerShape(AppDimens.radiusPill),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(text = "+ Buka Trayek", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun RouteFilterRow(
    selectedFilter: RouteInfoFilter,
    totalCount: Int,
    operatingCount: Int,
    standbyCount: Int,
    onSelectFilter: (RouteInfoFilter) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RouteInfoFilter.entries.forEach { filter ->
            val count = when (filter) {
                RouteInfoFilter.ALL -> totalCount
                RouteInfoFilter.OPERATING -> operatingCount
                RouteInfoFilter.STANDBY -> standbyCount
            }
            val isSelected = selectedFilter == filter

            FilterChip(
                selected = isSelected,
                onClick = { onSelectFilter(filter) },
                label = {
                    Text(
                        text = "${filter.displayName} ($count)",
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
}

@Composable
private fun RouteDetailCard(
    item: RouteDetailItem,
    onTogglePricings: () -> Unit,
    onAssignBusClick: (BusRoute) -> Unit,
    onAssignDriverClick: (BusRoute, Bus?) -> Unit
) {
    val info = item.operationalInfo
    val route = info.route
    val isOperating = info.activeTripsCount > 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDimens.radiusMedium),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = AppDimens.cardElevation),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.paddingMedium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${route.originTerminal.city} ➔ ${route.destinationTerminal.city}",
                    color = TextDark,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(AppDimens.radiusPill))
                            .background(if (route.pathType == RoutePathType.TOLL) PrimaryBlue else SkyBlue)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (route.pathType == RoutePathType.TOLL) "JALUR TOL" else "NON-TOL",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(AppDimens.radiusPill))
                            .background(if (isOperating) SuccessGreenLight else IceBlue)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (isOperating) "● ${info.activeTripsCount} Berdinas" else "Standby",
                            color = if (isOperating) SuccessGreen else PrimaryBlueDark,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = route.name,
                color = TextMuted,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            val hours = route.totalEstimatedDurationMinutes / 60
            val mins = route.totalEstimatedDurationMinutes % 60
            val durationStr = if (hours > 0) "${hours}j ${mins}m" else "${mins} mnt"

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF8FAFC))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                InfoSpecItem(label = "Jarak", value = "${route.totalDistanceKm.toInt()} km")
                InfoSpecItem(label = "Waktu", value = durationStr)
                InfoSpecItem(
                    label = "Tarif Tol",
                    value = if (route.totalTollFee.amount > 0) route.totalTollFee.formatRupiah() else "Gratis"
                )
            }

            if (route.transitTerminals.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🚏 Halte Transit: ", fontSize = 11.sp, color = TextMuted)
                    Text(
                        text = route.transitTerminals.joinToString(" ➔ ") { it.city },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryBlueDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF8FAFC))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "🚌 Armada & Pengemudi Ditugaskan (${info.assignedBuses.size} Bus):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                if (info.assignedBuses.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(WarningAmberLight)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "⚠️ Belum ada bus & pengemudi yang ditugaskan",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFB45309)
                        )
                    }
                } else {
                    info.assignedBuses.forEach { bus ->
                        val driver = info.assignedDrivers.find { it.id == bus.driverId || it.assignedBusId == bus.id }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(SurfaceWhite)
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${bus.busCode} • ${bus.fullName}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                                Text(
                                    text = "👨‍✈️ ${driver?.name ?: "Belum ada sopir"} • Kelas ${bus.body.busClass.displayName} (${bus.body.passengerCapacity} Kursi)",
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onAssignBusClick(route) },
                        shape = RoundedCornerShape(AppDimens.radiusPill),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "🚌 Input Bus",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                    }

                    val busNeedsDriver = info.assignedBuses.firstOrNull { it.driverId == null } ?: info.assignedBuses.firstOrNull()
                    OutlinedButton(
                        onClick = { onAssignDriverClick(route, busNeedsDriver) },
                        shape = RoundedCornerShape(AppDimens.radiusPill),
                        modifier = Modifier.weight(1f),
                        enabled = info.assignedBuses.isNotEmpty(),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "👨‍✈️ Input Driver",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (info.assignedBuses.isNotEmpty()) PrimaryBlue else TextMuted
                        )
                    }
                }
            }

            if (info.departureSchedules.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(IceBlue)
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "🕒 Jadwal Keberangkatan:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlueDark
                    )
                    info.departureSchedules.forEach { sched ->
                        Text(
                            text = "• $sched",
                            fontSize = 10.sp,
                            color = PrimaryBlueDark
                        )
                    }
                }
            }

            val uniquePricings = item.pricings.distinctBy {
                minOf(it.originTerminalId, it.destinationTerminalId) to maxOf(it.originTerminalId, it.destinationTerminalId)
            }
            if (uniquePricings.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onTogglePricings)
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🏷️ Daftar Tarif Tiket (${uniquePricings.size} Relasi)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                    Text(
                        text = if (item.isPricingsExpanded) "▲ Tutup" else "▼ Lihat Tarif",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                }

                if (item.isPricingsExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        uniquePricings.forEach { pricing ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${pricing.originCity} ⇄ ${pricing.destinationCity}",
                                    fontSize = 10.sp,
                                    color = TextDark
                                )
                                Text(
                                    text = pricing.price.formatRupiah(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlueDark
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
private fun AssignBusModal(
    route: BusRoute,
    availableBuses: List<Bus>,
    onDismiss: () -> Unit,
    onBusSelected: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Tugaskan Armada Bus",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Trayek: ${route.originTerminal.city} ➔ ${route.destinationTerminal.city}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryBlueDark
                )
                if (availableBuses.isEmpty()) {
                    Text(
                        text = "Tidak ada armada bus yang menganggur di garasi. Silakan beli armada baru di Showroom atau lepas penugasan bus dari trayek lain.",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(availableBuses) { bus ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onBusSelected(bus.id) },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = IceBlue)
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
                                            text = "${bus.busCode} • ${bus.fullName}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextDark
                                        )
                                        Text(
                                            text = "Kelas ${bus.body.busClass.displayName} • ${bus.body.passengerCapacity} Kursi",
                                            fontSize = 10.sp,
                                            color = TextMuted
                                        )
                                    }
                                    Button(
                                        onClick = { onBusSelected(bus.id) },
                                        shape = RoundedCornerShape(AppDimens.radiusPill),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                                    ) {
                                        Text("Pilih", fontSize = 11.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}

@Composable
private fun AssignDriverModal(
    route: BusRoute,
    targetBus: Bus?,
    availableDrivers: List<Driver>,
    onDismiss: () -> Unit,
    onDriverSelected: (String, String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Tugaskan Pengemudi (Sopir)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (targetBus != null) {
                        "Target Armada: ${targetBus.busCode} (${targetBus.fullName})"
                    } else {
                        "Trayek: ${route.originTerminal.city} ➔ ${route.destinationTerminal.city}"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryBlueDark
                )
                if (targetBus == null) {
                    Text(
                        text = "Belum ada armada bus yang ditugaskan ke trayek ini. Silakan input bus terlebih dahulu sebelum menugaskan driver.",
                        fontSize = 12.sp,
                        color = WarningAmber
                    )
                } else if (availableDrivers.isEmpty()) {
                    Text(
                        text = "Tidak ada pengemudi yang menganggur. Silakan rekrut pengemudi baru di menu Manajemen Kru.",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(availableDrivers) { driver ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onDriverSelected(driver.id, targetBus.id) },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = IceBlue)
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
                                            text = "👨‍✈️ ${driver.name}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextDark
                                        )
                                        Text(
                                            text = "Gaji: ${driver.wagePerDay.formatRupiah()} / hari",
                                            fontSize = 10.sp,
                                            color = TextMuted
                                        )
                                    }
                                    Button(
                                        onClick = { onDriverSelected(driver.id, targetBus.id) },
                                        shape = RoundedCornerShape(AppDimens.radiusPill),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                                    ) {
                                        Text("Pilih", fontSize = 11.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}

@Composable
private fun InfoSpecItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 9.sp, color = TextMuted)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextDark)
    }
}

@Composable
private fun EmptyRouteInfoView(
    onNavigateToNewRoute: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "🗺️", fontSize = 48.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Belum Ada Trayek Terbuka",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Buka trayek baru melalui Wizard Perencanaan Rute untuk menghubungkan kota-kota di Jawa Timur!",
            fontSize = 13.sp,
            color = TextMuted,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(18.dp))
        Button(
            onClick = onNavigateToNewRoute,
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            Text("➕ Buka Trayek Baru")
        }
    }
}
