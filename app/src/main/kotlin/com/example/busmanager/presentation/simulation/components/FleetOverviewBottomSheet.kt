package com.example.busmanager.presentation.simulation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmanager.domain.company.model.Money
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
import com.example.busmanager.ui.theme.IceBlue
import com.example.busmanager.ui.theme.PrimaryBlue
import com.example.busmanager.ui.theme.PrimaryBlueDark
import com.example.busmanager.ui.theme.SuccessGreen
import com.example.busmanager.ui.theme.SuccessGreenLight
import com.example.busmanager.ui.theme.SurfaceWhite
import com.example.busmanager.ui.theme.TextDark
import com.example.busmanager.ui.theme.TextMuted
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
    onDispatchBus: ((Bus, BusRoute) -> Unit)? = null,
    onTrackBus: (ActiveBusTrip) -> Unit,
    onCancelNextSchedule: (ActiveBusTrip) -> Unit = {},
    onDismiss: () -> Unit,
    onNavigateToShowroom: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableIntStateOf(if (activeTrips.isNotEmpty()) 0 else 1) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BackgroundWhite,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(AppDimens.paddingMedium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🚌 Status Armada & Pelacakan",
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

            Spacer(modifier = Modifier.height(AppDimens.paddingSmall))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AppDimens.radiusSmall))
                    .background(IceBlue)
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (selectedTab == 0) PrimaryBlue else Color.Transparent)
                        .clickable { selectedTab = 0 }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sedang Jalan (${activeTrips.size})",
                        color = if (selectedTab == 0) Color.White else PrimaryBlueDark,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (selectedTab == 1) PrimaryBlue else Color.Transparent)
                        .clickable { selectedTab = 1 }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Semua Armada (${buses.size})",
                        color = if (selectedTab == 1) Color.White else PrimaryBlueDark,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppDimens.paddingMedium))

            if (selectedTab == 0) {
                if (activeTrips.isEmpty()) {
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
                                text = "Tidak Ada Bus Sedang Jalan",
                                color = TextDark,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Armada yang siap akan berangkat otomatis saat jam jadwal keberangkatan tiba.",
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
                        items(activeTrips, key = { it.id }) { trip ->
                            val bus = buses.find { it.id == trip.busId }
                            ActiveBusDetailCard(
                                trip = trip,
                                bus = bus,
                                onTrack = {
                                    onTrackBus(trip)
                                    onDismiss()
                                },
                                onCancelNextSchedule = { onCancelNextSchedule(trip) }
                            )
                        }
                    }
                }
            } else {
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
                                onDispatch = { route -> onDispatchBus?.invoke(bus, route) },
                                onTrack = {
                                    if (activeTrip != null) {
                                        onTrackBus(activeTrip)
                                        onDismiss()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveBusDetailCard(
    trip: ActiveBusTrip,
    bus: Bus?,
    onTrack: () -> Unit,
    onCancelNextSchedule: () -> Unit = {}
) {
    val capacity = bus?.body?.passengerCapacity ?: 30
    val remainingSeats = (capacity - trip.passengerCount).coerceAtLeast(0)
    val pricePerPassenger = if (trip.passengerCount > 0) trip.ticketEarnings.amount / trip.passengerCount else 0L
    val priceFormatted = if (pricePerPassenger > 0L) Money.rupiah(pricePerPassenger).formatRupiah() else trip.ticketEarnings.formatRupiah()

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
                .navigationBarsPadding()
                .padding(AppDimens.paddingMedium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(AppDimens.radiusSmall))
                            .background(PrimaryBlueDark)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = trip.busCode,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Menuju ${trip.currentDestinationName} dari ${trip.currentOriginName}",
                        color = TextDark,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(AppDimens.radiusPill))
                        .background(SuccessGreenLight)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "● ${(trip.progressFraction * 100).toInt()}%",
                        color = SuccessGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = trip.route.name,
                color = TextMuted,
                fontSize = 12.sp
            )

            if (bus != null) {
                Text(
                    text = "${bus.fullName} • ${bus.body.busClass.displayName}",
                    color = PrimaryBlueDark,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF8FAFC))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricChip(label = "Total Kursi", value = "$capacity Kursi")
                MetricChip(label = "Sisa Kursi", value = "$remainingSeats Kursi")
                MetricChip(label = "Tarif Tiket", value = priceFormatted)
                MetricChip(label = "Sisa Waktu", value = "${trip.remainingMinutes} mnt")
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { trip.progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = PrimaryBlue,
                trackColor = IceBlue
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onTrack,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(AppDimens.radiusMedium),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                Text(
                    text = "🎯 Track Bus (Kunci Kamera Lapangan)",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (trip.cancelNextSchedule) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFEF3C7))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "🛑 Jadwal berikutnya dibatalkan. Bus akan menunggu jadwal keberangkatan di ${trip.currentDestinationName}.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFB45309)
                    )
                }
            } else {
                OutlinedButton(
                    onClick = onCancelNextSchedule,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(AppDimens.radiusMedium),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDC2626)),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Text(
                        text = "🛑 Batalkan Jadwal Berikutnya",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = TextMuted
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
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
    onDispatch: ((BusRoute) -> Unit)? = null,
    onTrack: () -> Unit
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
                .navigationBarsPadding()
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
                            canOperate -> "Menunggu Jadwal Keberangkatan"
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
                text = bus.fullName,
                color = TextDark,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${bus.body.busClass.displayName} • ${bus.body.passengerCapacity} Kursi",
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
                    detail = assignedRoute?.name ?: "Kosong"
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
                    Button(
                        onClick = onTrack,
                        shape = RoundedCornerShape(AppDimens.radiusPill),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "🎯 Track Bus",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (canOperate) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(AppDimens.radiusSmall))
                            .background(IceBlue)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(text = "🕒", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Otomatis jalan sesuai jam keberangkatan",
                            color = PrimaryBlueDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
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
private fun RowScope.ConditionCheckChip(
    label: String,
    isSatisfied: Boolean,
    detail: String
) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
            color = if (isSatisfied) TextMuted else Color(0xFFDC2626),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
