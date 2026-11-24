package com.example.busmanager.presentation.simulation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import com.example.busmanager.domain.route.model.RoutePricing
import com.example.busmanager.domain.route.model.RouteSchedule
import com.example.busmanager.domain.simulation.model.ActiveBusTrip
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutesOverviewBottomSheet(
    routes: List<BusRoute>,
    selectedRoute: BusRoute?,
    routePricings: Map<String, List<RoutePricing>> = emptyMap(),
    buses: List<Bus> = emptyList(),
    activeTrips: List<ActiveBusTrip> = emptyList(),
    schedules: List<RouteSchedule> = emptyList(),
    drivers: List<Driver> = emptyList(),
    onSelectRoute: (BusRoute) -> Unit,
    onDismiss: () -> Unit,
    onNavigateToRoutePlanner: () -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val activeRoutes = routes.filter { route ->
        val scheduledBusIds = schedules.filter { it.routeId == route.id }.mapNotNull { it.assignedBusId }.toSet()
        val routeBuses = buses.filter { it.routeId == route.id || scheduledBusIds.contains(it.id) }
        val hasDriver = routeBuses.any { bus -> bus.driverId != null || drivers.any { it.assignedBusId == bus.id } }
        val hasTripsOrSchedules = activeTrips.any { it.route.id == route.id } || schedules.any { it.routeId == route.id }
        routeBuses.isNotEmpty() && hasDriver && hasTripsOrSchedules
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BackgroundWhite,
        dragHandle = null
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
                Column {
                    Text(
                        text = "🗺️ Trayek Aktif",
                        color = TextDark,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${activeRoutes.size} Trayek Sedang Dilayani Armada",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(IceBlue)
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "✕", fontSize = 13.sp, color = PrimaryBlueDark, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(AppDimens.paddingMedium))

            if (activeRoutes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🚌", fontSize = 42.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Belum Ada Trayek Aktif",
                            color = TextDark,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Trayek aktif akan muncul setelah armada bus dan pengemudi ditugaskan melayani rute.",
                            color = TextMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 24.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(AppDimens.paddingSmall),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(activeRoutes, key = { it.id }) { route ->
                        val isSelected = selectedRoute?.id == route.id
                        val scheduledBusIds = schedules.filter { it.routeId == route.id }.mapNotNull { it.assignedBusId }.toSet()
                        val routeBuses = buses.filter { scheduledBusIds.contains(it.id) || it.routeId == route.id }
                        val routeSchedules = schedules.filter { it.routeId == route.id }

                        ActiveRouteItemCard(
                            route = route,
                            buses = routeBuses,
                            drivers = drivers,
                            schedules = routeSchedules,
                            activeTrips = activeTrips,
                            isSelected = isSelected,
                            onSelect = {
                                onSelectRoute(route)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveRouteItemCard(
    route: BusRoute,
    buses: List<Bus>,
    drivers: List<Driver>,
    schedules: List<RouteSchedule>,
    activeTrips: List<ActiveBusTrip>,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(AppDimens.radiusMedium),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) AccentBlue else SurfaceWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = AppDimens.cardElevation),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) PrimaryBlue else CardBorder
        )
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

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(AppDimens.radiusPill))
                        .background(if (route.pathType == RoutePathType.TOLL) PrimaryBlue else SkyBlue)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (route.pathType == RoutePathType.TOLL) "JALUR TOL" else "NON-TOL",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = route.name,
                color = TextMuted,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📏 ${route.totalDistanceKm.toInt()} km • ⏱️ ${route.totalEstimatedDurationMinutes} mnt",
                    color = PrimaryBlueDark,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )

                if (route.transitTerminals.isNotEmpty()) {
                    Text(
                        text = "Via: ${route.transitTerminals.joinToString { it.city }}",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF8FAFC))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "🚌 Armada & Pengemudi (${buses.size} Bus):",
                    color = TextDark,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                buses.forEach { bus ->
                    val isRunning = activeTrips.any { it.busCode == bus.busCode }
                    val busDriver = drivers.find { it.id == bus.driverId || it.assignedBusId == bus.id }
                    val busSchedules = schedules.filter { it.assignedBusId == bus.id }
                    val scheduleSummary = if (busSchedules.isNotEmpty()) {
                        busSchedules.joinToString(" • ") { sched ->
                            String.format("%02d:%02d WIB (%s)", sched.departureHour, sched.departureMinute, sched.direction.displayName)
                        }
                    } else {
                        "Jadwal Standby"
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(SurfaceWhite)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = bus.busCode,
                                    color = TextDark,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "• ${bus.body.modelName}",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                            Text(
                                text = "👨‍✈️ ${busDriver?.name ?: "Sopir Ditugaskan"} • $scheduleSummary",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(AppDimens.radiusPill))
                                .background(if (isRunning) SuccessGreenLight else IceBlue)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isRunning) "● Berdinas" else "Standby",
                                color = if (isRunning) SuccessGreen else PrimaryBlueDark,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
