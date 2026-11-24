package com.example.busmanager.presentation.simulation

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmanager.domain.route.model.Terminal
import com.example.busmanager.domain.simulation.model.SimulationSpeed
import com.example.busmanager.presentation.route.components.EastJavaMapCanvas
import com.example.busmanager.presentation.route.components.EditScheduleDialog
import com.example.busmanager.presentation.simulation.components.BusLiveTrackingCard
import com.example.busmanager.presentation.simulation.components.FleetOverviewBottomSheet
import com.example.busmanager.presentation.simulation.components.RoutesOverviewBottomSheet
import com.example.busmanager.ui.theme.AccentBlue
import com.example.busmanager.ui.theme.AppDimens
import com.example.busmanager.ui.theme.BackgroundWhite
import com.example.busmanager.ui.theme.CardBorder
import com.example.busmanager.ui.theme.IceBlue
import com.example.busmanager.ui.theme.PrimaryBlue
import com.example.busmanager.ui.theme.PrimaryBlueDark
import com.example.busmanager.ui.theme.SurfaceWhite
import com.example.busmanager.ui.theme.TextDark
import com.example.busmanager.ui.theme.TextMuted
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
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(AppDimens.paddingMedium),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                SimulationTopBar(
                    dateText = state.formattedDate,
                    timeText = state.formattedTime,
                    isPlaying = state.isPlaying,
                    currentSpeed = state.currentSpeed,
                    onTogglePlay = { viewModel.togglePlay() },
                    onSetSpeed = { viewModel.setSpeed(it) },
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
                    .padding(vertical = AppDimens.paddingSmall),
                contentAlignment = Alignment.Center
            ) {
                EastJavaMapCanvas(
                    terminals = state.terminals,
                    allSegments = state.allSegments,
                    modifier = Modifier.fillMaxSize(),
                    openedRoutes = state.openedRoutes,
                    selectedRoute = state.selectedRoute,
                    selectedOrigin = state.selectedTerminal,
                    transitTerminals = state.selectedRoute?.transitTerminals ?: emptyList(),
                    activeTrips = state.activeTrips,
                    parkedBuses = state.parkedBuses,
                    trackedTripId = state.trackedTripId,
                    onTerminalClick = { terminal -> viewModel.onTerminalClick(terminal) },
                    onTripClick = { trip -> viewModel.onTripClick(trip) },
                    onParkedBusClick = { viewModel.openBusesBottomSheet() }
                )
            }

            Column {
                if (state.selectedTrip != null) {
                    BusLiveTrackingCard(
                        trip = state.selectedTrip!!,
                        onClose = { viewModel.clearSelectedTrip() },
                        onCancelNextSchedule = { viewModel.cancelNextSchedule(state.selectedTrip!!.id) }
                    )
                    Spacer(modifier = Modifier.height(AppDimens.paddingSmall))
                } else if (state.selectedTerminal != null) {
                    TerminalInspectionCard(
                        terminal = state.selectedTerminal!!,
                        routeName = state.selectedRoute?.name,
                        onClose = { viewModel.clearSelectedTerminal() }
                    )
                    Spacer(modifier = Modifier.height(AppDimens.paddingSmall))
                }

                SimulationBottomBar(
                    ownedBusCount = state.ownedBusCount,
                    routeCount = state.openedRoutes.size,
                    onOpenBuses = { viewModel.openBusesBottomSheet() },
                    onOpenRoutes = { viewModel.openRoutesBottomSheet() }
                )
            }
        }
    }

    when (state.activeBottomSheet) {
        SimulationBottomSheetType.BUSES -> {
            FleetOverviewBottomSheet(
                buses = state.ownedBuses,
                activeTrips = state.activeTrips,
                availableRoutes = state.routes,
                drivers = state.drivers,
                schedules = state.schedules,
                busOperationStatuses = state.busOperationStatuses,
                onDispatchBus = { bus, route -> viewModel.dispatchBus(bus, route) },
                onTrackBus = { viewModel.trackBus(it) },
                onCancelNextSchedule = { viewModel.cancelNextSchedule(it.id) },
                onDismiss = { viewModel.dismissBottomSheet() },
                onNavigateToShowroom = onNavigateToBuses
            )
        }
        SimulationBottomSheetType.ROUTES -> {
            RoutesOverviewBottomSheet(
                routes = state.routes,
                selectedRoute = state.selectedRoute,
                routePricings = state.routePricings,
                buses = state.ownedBuses,
                activeTrips = state.activeTrips,
                schedules = state.schedules,
                drivers = state.drivers,
                onSelectRoute = { viewModel.selectRoute(it) },
                onEditSchedule = { bus, route, headway, dwell ->
                    viewModel.openEditSchedule(bus, route, headway, dwell)
                },
                onDismiss = { viewModel.dismissBottomSheet() },
                onNavigateToRoutePlanner = onNavigateToRoutes
            )
        }
        SimulationBottomSheetType.NONE -> {}
    }

    if (state.editingScheduleTarget != null) {
        val target = state.editingScheduleTarget!!
        EditScheduleDialog(
            busCode = target.bus.busCode,
            routeName = target.route.name,
            initialHeadwayMinutes = target.currentHeadwayMinutes,
            initialTransitDwellMinutes = target.currentTransitDwellMinutes,
            onDismiss = { viewModel.dismissEditSchedule() },
            onSave = { headway, dwell ->
                viewModel.saveSchedule(headway, dwell)
            }
        )
    }
}

@Composable
private fun SimulationTopBar(
    dateText: String,
    timeText: String,
    isPlaying: Boolean,
    currentSpeed: SimulationSpeed,
    onTogglePlay: () -> Unit,
    onSetSpeed: (SimulationSpeed) -> Unit,
    onNavigateBack: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDimens.radiusMedium),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = AppDimens.cardElevation)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.paddingSmall)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                        text = if (isPlaying) "⏸️ Jeda" else "▶️ Lanjut",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Kecepatan:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.width(8.dp))
                SimulationSpeed.entries.forEach { speed ->
                    val isSelected = currentSpeed == speed
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) PrimaryBlue else IceBlue)
                            .clickable { onSetSpeed(speed) }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = speed.displayName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else PrimaryBlueDark
                        )
                    }
                }
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
        "Semua armada ($ownedBusCount unit) saat ini menunggu jadwal keberangkatan di terminal / garasi."
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
private fun TerminalInspectionCard(
    terminal: Terminal,
    routeName: String?,
    onClose: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDimens.radiusMedium),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = AppDimens.cardElevation),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.paddingMedium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🚏 ${terminal.name}",
                        color = TextDark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${terminal.city} • Tipe ${terminal.type.name}",
                    color = TextMuted,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (routeName != null) {
                        "Trayek Terkait: $routeName (Status: Menunggu Jadwal Keberangkatan)"
                    } else {
                        "Status: Menunggu Jadwal Keberangkatan | Tidak ada bus beroperasi"
                    },
                    color = PrimaryBlueDark,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE2E8F0))
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✕",
                    color = TextDark,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SimulationBottomBar(
    ownedBusCount: Int,
    routeCount: Int,
    onOpenBuses: () -> Unit,
    onOpenRoutes: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppDimens.paddingSmall)
    ) {
        Button(
            onClick = onOpenBuses,
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
            onClick = onOpenRoutes,
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
                        text = "$routeCount Rute",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
