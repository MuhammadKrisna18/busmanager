package com.example.busmanager.presentation.simulation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.busmanager.domain.fleet.usecase.BusOperationStatus
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.RouteSchedule
import com.example.busmanager.domain.simulation.model.ActiveBusTrip
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
import com.example.busmanager.ui.theme.TextMuted
import com.example.busmanager.ui.theme.WarningAmber
import com.example.busmanager.ui.theme.WarningAmberLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FleetOverviewBottomSheet(
    buses: List<Bus>,
    activeTrips: List<ActiveBusTrip>,
    availableRoutes: List<BusRoute>,
    drivers: List<Driver> = emptyList(),
    schedules: List<RouteSchedule> = emptyList(),
    busOperationStatuses: Map<String, BusOperationStatus> = emptyMap(),
    onDispatchBus: (Bus, BusRoute) -> Unit,
    onDismiss: () -> Unit,
    onNavigateToShowroom: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                        text = "🚌 Status Armada & Syarat Operasi",
                        color = TextDark,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Total: ${buses.size} Unit • ${activeTrips.size} Sedang Berdinas",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = {
                        onDismiss()
                        onNavigateToShowroom()
                    },
                    shape = RoundedCornerShape(AppDimens.radiusPill),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "+ Showroom",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppDimens.paddingMedium))

            if (buses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🚌", fontSize = 40.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Belum Ada Armada di Garasi",
                            color = TextDark,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Beli bus pertama di Showroom untuk memulai simulasi.",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(AppDimens.paddingSmall),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(buses, key = { it.id }) { bus ->
                        val activeTrip = activeTrips.find { it.busId == bus.id }
                        val driver = drivers.find { it.id == bus.driverId }
                        val assignedRoute = availableRoutes.find { it.id == bus.routeId }
                        val status = busOperationStatuses[bus.id]

                        FleetBusItemCard(
                            bus = bus,
                            activeTrip = activeTrip,
                            driver = driver,
                            assignedRoute = assignedRoute,
                            operationStatus = status,
                            availableRoutes = availableRoutes,
                            onDispatch = { route -> onDispatchBus(bus, route) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FleetBusItemCard(
    bus: Bus,
    activeTrip: ActiveBusTrip?,
    driver: Driver?,
    assignedRoute: BusRoute?,
    operationStatus: BusOperationStatus?,
    availableRoutes: List<BusRoute>,
    onDispatch: (BusRoute) -> Unit
) {
    val isOperating = activeTrip != null
    val canOperate = operationStatus?.canOperate ?: false

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
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(AppDimens.radiusSmall))
                        .background(PrimaryBlueDark)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = bus.busCode,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(AppDimens.radiusPill))
                        .background(
                            when {
                                activeTrip != null -> SuccessGreenLight
                                canOperate -> IceBlue
                                else -> WarningAmberLight
                            }
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = when {
                            activeTrip != null -> "● Berdinas (${(activeTrip.progressFraction * 100).toInt()}%)"
                            canOperate -> "Siap Beroperasi"
                            else -> "Belum Siap Operasi"
                        },
                        color = when {
                            activeTrip != null -> SuccessGreen
                            canOperate -> PrimaryBlueDark
                            else -> Color(0xFFB45309)
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${bus.chassis.brand.displayName} ${bus.chassis.modelName} - ${bus.body.brand.displayName} ${bus.body.modelName}",
                color = TextDark,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${bus.body.busClass.displayName} • ${bus.body.passengerCapacity} Kursi • Kondisi: ${bus.condition.engineHealthPercent}%",
                color = TextMuted,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFF8FAFC))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ConditionCheckChip(
                    label = "Trayek",
                    isSatisfied = operationStatus?.hasRoute ?: !bus.routeId.isNullOrBlank(),
                    detail = assignedRoute?.name?.take(15) ?: "Kosong"
                )
                ConditionCheckChip(
                    label = "Sopir",
                    isSatisfied = operationStatus?.hasDriver ?: (driver != null),
                    detail = driver?.name?.take(12) ?: "Belum ada"
                )
                ConditionCheckChip(
                    label = "Tarif",
                    isSatisfied = operationStatus?.hasPricings ?: false,
                    detail = if (operationStatus?.hasPricings == true) "Sudah ada" else "Belum set"
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (activeTrip != null) {
                    Text(
                        text = "Rute: ${activeTrip.route.name}",
                        color = SuccessGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                } else if (canOperate) {
                    val targetRoute = assignedRoute ?: availableRoutes.firstOrNull()
                    if (targetRoute != null) {
                        Button(
                            onClick = { onDispatch(targetRoute) },
                            shape = RoundedCornerShape(AppDimens.radiusPill),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Berdinas Sekarang",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Lengkapi 3 syarat agar armada dapat berdinas",
                        color = Color(0xFFDC2626),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun ConditionCheckChip(
    label: String,
    isSatisfied: Boolean,
    detail: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (isSatisfied) "✓" else "✕",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isSatisfied) SuccessGreen else Color(0xFFDC2626)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
        }
        Text(
            text = detail,
            fontSize = 10.sp,
            color = if (isSatisfied) TextMuted else Color(0xFFDC2626)
        )
    }
}
