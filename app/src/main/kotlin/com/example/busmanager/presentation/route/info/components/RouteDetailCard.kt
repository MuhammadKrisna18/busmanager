package com.example.busmanager.presentation.route.info.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.RoutePathType
import com.example.busmanager.presentation.route.info.RouteDetailItem
import com.example.busmanager.ui.theme.AppDimens
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
import com.example.busmanager.ui.theme.WarningAmberLight

@Composable
fun RouteDetailCard(
    item: RouteDetailItem,
    onTogglePricings: () -> Unit,
    onAssignBusClick: (BusRoute) -> Unit,
    onAssignDriverClick: (BusRoute, Bus?) -> Unit,
    onDeleteRouteClick: (BusRoute) -> Unit
) {
    val info = item.operationalInfo
    val route = info.route
    val isOperating = info.activeTripsCount > 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDimens.radiusMedium),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = AppDimens.cardElevation),
        border = BorderStroke(1.dp, CardBorder)
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

                    OutlinedButton(
                        onClick = { onDeleteRouteClick(route) },
                        shape = RoundedCornerShape(AppDimens.radiusPill),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                    ) {
                        Text(
                            text = "🗑️ Tutup",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFDC2626)
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
fun InfoSpecItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 9.sp, color = TextMuted)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextDark)
    }
}
