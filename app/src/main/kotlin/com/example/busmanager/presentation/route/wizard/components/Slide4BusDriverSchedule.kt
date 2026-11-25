package com.example.busmanager.presentation.route.wizard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmanager.domain.driver.model.Driver
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.simulation.model.RouteDirection
import com.example.busmanager.presentation.route.wizard.RouteWizardUiState
import com.example.busmanager.ui.theme.CardBorder
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

@Composable
fun Slide4BusDriverSchedule(
    uiState: RouteWizardUiState,
    onSelectBus: (Bus) -> Unit,
    onSelectDriver: (Driver) -> Unit,
    onSetDepartureTime: (Int, Int) -> Unit,
    onSetDirection: (RouteDirection) -> Unit,
    onSetIsRoundTrip: (Boolean) -> Unit,
    onSetReturnTime: (Int, Int) -> Unit,
    onResetReturnTime: () -> Unit,
    onSetHeadwayMinutes: (Int) -> Unit = {},
    onSetTransitDwellMinutes: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val originCity = uiState.selectedOrigin?.city ?: "Asal"
    val destCity = uiState.selectedDestination?.city ?: "Tujuan"
    val outboundFromCity = if (uiState.direction == RouteDirection.ORIGIN_TO_DESTINATION) originCity else destCity
    val outboundToCity = if (uiState.direction == RouteDirection.ORIGIN_TO_DESTINATION) destCity else originCity
    val returnFromCity = outboundToCity
    val returnToCity = outboundFromCity

    val durationHours = uiState.estimatedDurationMinutes / 60
    val durationMins = uiState.estimatedDurationMinutes % 60
    val durationText = if (durationHours > 0) "$durationHours Jam $durationMins Menit" else "$durationMins Menit"
    val distanceText = "${uiState.builtRoute?.totalDistanceKm ?: 0} km"

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        item {
            Text(
                text = "1. Pilih Armada Bus",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Pilih bus di garasi yang akan ditugaskan melayani trayek ini",
                fontSize = 12.sp,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        if (uiState.availableBuses.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = WarningAmberLight)
                ) {
                    Text(
                        text = "Tidak ada bus yang siap beroperasi di garasi. Anda tetap dapat menyimpan trayek ini sebagai trayek Standby melalui tombol \"Simpan Standby\" di bawah, lalu menugaskan bus nanti melalui Garasi.",
                        modifier = Modifier.padding(14.dp),
                        fontSize = 12.sp,
                        color = Color(0xFFB45309)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        } else {
            items(uiState.availableBuses) { bus ->
                val isSelected = uiState.selectedBus?.id == bus.id
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onSelectBus(bus) },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) IceBlue else SurfaceWhite
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = SolidColor(if (isSelected) PrimaryBlue else CardBorder)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(IceBlue)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = bus.busCode,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlueDark
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = bus.fullName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextDark
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${bus.body.busClass.displayName} • ${bus.body.passengerCapacity} Kursi",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                            if (bus.currentTerminalId != null) {
                                val termName = uiState.allTerminals.find { it.id == bus.currentTerminalId }?.name ?: bus.currentTerminalId
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "📍 Posisi: Terminal $termName",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PrimaryBlueDark
                                )
                            }
                        }

                        if (isSelected) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "✓",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        item {
            Text(
                text = "2. Pilih Pengemudi / Sopir",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Pilih sopir berlisensi yang bertugas mengemudikan armada",
                fontSize = 12.sp,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        if (uiState.availableDrivers.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = WarningAmberLight)
                ) {
                    Text(
                        text = "Tidak ada pengemudi yang siap dinas. Anda dapat merekrut sopir di menu Manajemen Kru, atau menyimpan trayek ini sebagai Standby terlebih dahulu.",
                        modifier = Modifier.padding(14.dp),
                        fontSize = 12.sp,
                        color = Color(0xFFB45309)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        } else {
            items(uiState.availableDrivers) { driver ->
                val isSelected = uiState.selectedDriver?.id == driver.id

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onSelectDriver(driver) },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) IceBlue else SurfaceWhite
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = SolidColor(if (isSelected) PrimaryBlue else CardBorder)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = driver.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Bagi Hasil: 30% per Rit Dinas • Status: Siap Dinas",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }

                        if (isSelected) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "✓",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        item {
            Text(
                text = "3. Atur Pola & Jadwal Operasional (05.00 - 23.00 WIB)",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Armada beroperasi pada rentang pukul 05.00 - 23.00 WIB dengan jeda istirahat antar-rit (min. 30 menit). Keberangkatan malam yang tiba di atas pukul 23.00 istirahat di terminal hingga 05.00 pagi.",
                fontSize = 12.sp,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Arah Berangkat Perdana",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.direction == RouteDirection.ORIGIN_TO_DESTINATION,
                    onClick = { onSetDirection(RouteDirection.ORIGIN_TO_DESTINATION) },
                    label = { Text("Menuju $destCity dari $originCity") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IceBlue,
                        selectedLabelColor = PrimaryBlueDark
                    )
                )
                FilterChip(
                    selected = uiState.direction == RouteDirection.DESTINATION_TO_ORIGIN,
                    onClick = { onSetDirection(RouteDirection.DESTINATION_TO_ORIGIN) },
                    label = { Text("Menuju $originCity dari $destCity") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IceBlue,
                        selectedLabelColor = PrimaryBlueDark
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Jam Keberangkatan Perdana (05.00 - 23.00 WIB)",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Pilih jam armada mulai berdinas pada rute ini",
                fontSize = 11.sp,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(IceBlue)
                            .clickable {
                                val nextH = (uiState.departureHour - 1).coerceAtLeast(5)
                                onSetDepartureTime(nextH, uiState.departureMinute)
                            }
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("-1 Jam", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryBlueDark)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = String.format("%02d:%02d WIB", uiState.departureHour, uiState.departureMinute),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlueDark
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(IceBlue)
                            .clickable {
                                val nextH = (uiState.departureHour + 1).coerceAtMost(23)
                                val nextM = if (nextH == 23) 0 else uiState.departureMinute
                                onSetDepartureTime(nextH, nextM)
                            }
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("+1 Jam", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryBlueDark)
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(5, 6, 7, 8, 12, 15, 18).forEach { hour ->
                    FilterChip(
                        selected = uiState.departureHour == hour && uiState.departureMinute == 0,
                        onClick = { onSetDepartureTime(hour, 0) },
                        label = { Text(String.format("%02d:00", hour)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IceBlue,
                            selectedLabelColor = PrimaryBlueDark
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Jeda Keberangkatan (Headway)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(IceBlue)
                            .clickable { onSetHeadwayMinutes((uiState.headwayMinutes - 10).coerceAtLeast(30)) }
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("-10", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryBlueDark)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${uiState.headwayMinutes} Menit",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlueDark
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(IceBlue)
                            .clickable { onSetHeadwayMinutes((uiState.headwayMinutes + 10).coerceAtMost(240)) }
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("+10", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryBlueDark)
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Bus beristirahat selama waktu ini sebelum otomatis berangkat kembali (PP) (Min. 30 mnt)",
                fontSize = 11.sp,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(30, 40, 50, 60, 90, 120).forEach { mins ->
                    val label = when (mins) {
                        60 -> "1 Jam"
                        120 -> "2 Jam"
                        else -> "$mins Mnt"
                    }
                    FilterChip(
                        selected = uiState.headwayMinutes == mins,
                        onClick = { onSetHeadwayMinutes(mins) },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IceBlue,
                            selectedLabelColor = PrimaryBlueDark
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Waktu Pemberhentian di Terminal Ampiran / Transit",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Waktu berhenti bus untuk naik-turun penumpang di tiap terminal transit (Maksimal 15 Menit)",
                fontSize = 11.sp,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(0, 5, 10, 15).forEach { mins ->
                    val label = if (mins == 0) "0 Mnt (Kilat)" else "$mins Mnt"
                    FilterChip(
                        selected = uiState.transitDwellMinutes == mins,
                        onClick = { onSetTransitDwellMinutes(mins) },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IceBlue,
                            selectedLabelColor = PrimaryBlueDark
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = IceBlue),
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(PrimaryBlue.copy(alpha = 0.3f)))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
                ) {
                    Text(
                        text = "📋 Ringkasan Operasional (05.00 - 23.00 WIB)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlueDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Pola: Bolak-balik (PP) pukul 05.00 - 23.00 WIB",
                        fontSize = 12.sp,
                        color = TextDark
                    )
                    Text(
                        text = "• Jeda Balik: ${uiState.headwayMinutes} Menit istirahat di terminal tujuan (Min. 30 mnt)",
                        fontSize = 12.sp,
                        color = TextDark
                    )
                    Text(
                        text = "• Singgah Transit: ${uiState.transitDwellMinutes} Menit di tiap terminal ampiran",
                        fontSize = 12.sp,
                        color = TextDark
                    )
                    Text(
                        text = "• Keberangkatan Perdana: ${String.format("%02d:%02d WIB", uiState.departureHour, uiState.departureMinute)} menuju $outboundToCity dari $outboundFromCity",
                        fontSize = 12.sp,
                        color = TextDark
                    )
                    Text(
                        text = "• Jam Malam: Di atas pukul 23.00 armada menginap di terminal tujuan dan siap kembali pukul 05.00",
                        fontSize = 12.sp,
                        color = TextDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        if (uiState.builtRoute != null && uiState.selectedBus != null && uiState.selectedDriver != null && !uiState.hasConflict) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SuccessGreenLight),
                    border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(SuccessGreen))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "✨", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Semua Syarat Terpenuhi!",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        val tripTypeText = if (uiState.isRoundTrip) "Pulang-Pergi (A-B & B-A)" else "Satu Arah"
                        Text(
                            text = "Trayek $tripTypeText, Matriks Tarif, Bus (${uiState.selectedBus.busCode}), dan Sopir (${uiState.selectedDriver.name}) telah siap. Klik 'Jalankan & Simpan' untuk mulai berdinas!",
                            fontSize = 12.sp,
                            color = TextDark
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
