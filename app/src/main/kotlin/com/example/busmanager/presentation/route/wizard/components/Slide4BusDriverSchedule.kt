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
                        text = "Tidak ada bus yang menganggur di garasi. Silakan beli bus baru di Showroom atau lepaskan dinas bus yang ada.",
                        modifier = Modifier.padding(14.dp),
                        fontSize = 12.sp,
                        color = Color(0xFFB45309)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        } else {
            val hasEligibleBus = uiState.availableBuses.any { !it.isOperating && !it.hasRoute && !it.hasDriver }
            if (!hasEligibleBus) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = WarningAmberLight)
                    ) {
                        Text(
                            text = "Seluruh armada di garasi sudah memiliki penugasan trayek/sopir. Silakan beli bus baru di Showroom atau lepaskan dinas bus yang ada.",
                            modifier = Modifier.padding(12.dp),
                            fontSize = 12.sp,
                            color = Color(0xFFB45309)
                        )
                    }
                }
            }

            items(uiState.availableBuses) { bus ->
                val isBusEligible = !bus.isOperating && !bus.hasRoute && !bus.hasDriver
                val isSelected = uiState.selectedBus?.id == bus.id
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(enabled = isBusEligible) { onSelectBus(bus) },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            !isBusEligible -> Color(0xFFF8FAFC)
                            isSelected -> IceBlue
                            else -> SurfaceWhite
                        }
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = SolidColor(
                            when {
                                !isBusEligible -> CardBorder
                                isSelected -> PrimaryBlue
                                else -> CardBorder
                            }
                        )
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isBusEligible) IceBlue else Color(0xFFF1F5F9))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = bus.busCode,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isBusEligible) PrimaryBlueDark else TextMuted
                                    )
                                }
                                if (!isBusEligible) {
                                    val reasonBadge = when {
                                        bus.isOperating -> "Sedang Berdinas"
                                        bus.hasRoute && bus.hasDriver -> "Sudah Ada Trayek & Sopir"
                                        bus.hasRoute -> "Sudah Ada Trayek"
                                        bus.hasDriver -> "Sudah Ada Sopir"
                                        else -> "Tidak Tersedia"
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(99.dp))
                                            .background(Color(0xFFFEE2E2))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = reasonBadge,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFDC2626)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = bus.fullName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isBusEligible) TextDark else TextMuted
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

                        if (isSelected && isBusEligible) {
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
                        text = "Belum ada sopir aktif. Silakan rekrut pengemudi di menu Manajemen Kru terlebih dahulu.",
                        modifier = Modifier.padding(14.dp),
                        fontSize = 12.sp,
                        color = Color(0xFFB45309)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        } else {
            val hasEligibleDriver = uiState.availableDrivers.any { uiState.driverAssignedBusCodes[it.id] == null && !it.isAssigned }
            if (!hasEligibleDriver) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = WarningAmberLight)
                    ) {
                        Text(
                            text = "Seluruh sopir sudah bertugas di armada lain. Silakan rekrut sopir baru di menu Manajemen Kru.",
                            modifier = Modifier.padding(12.dp),
                            fontSize = 12.sp,
                            color = Color(0xFFB45309)
                        )
                    }
                }
            }

            items(uiState.availableDrivers) { driver ->
                val assignedBusCode = uiState.driverAssignedBusCodes[driver.id]
                val isDriverAssigned = assignedBusCode != null || driver.isAssigned
                val isSelected = uiState.selectedDriver?.id == driver.id

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(enabled = !isDriverAssigned) { onSelectDriver(driver) },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            isDriverAssigned -> Color(0xFFF8FAFC)
                            isSelected -> IceBlue
                            else -> SurfaceWhite
                        }
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = SolidColor(
                            when {
                                isDriverAssigned -> CardBorder
                                isSelected -> PrimaryBlue
                                else -> CardBorder
                            }
                        )
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = driver.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDriverAssigned) TextMuted else TextDark
                                )
                                if (isDriverAssigned) {
                                    val badgeLabel = if (assignedBusCode != null) "Bertugas di $assignedBusCode" else "Sedang Bertugas"
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(99.dp))
                                            .background(Color(0xFFFEE2E2))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = badgeLabel,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFDC2626)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isDriverAssigned) {
                                    "Bagi Hasil: 30% per Rit Dinas • Sedang Bertugas"
                                } else {
                                    "Bagi Hasil: 30% per Rit Dinas • Status: Siap Dinas"
                                },
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }

                        if (isSelected && !isDriverAssigned) {
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
                text = "3. Atur Pola & Jadwal Operasional 24 Jam",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Armada beroperasi 24 jam non-stop dengan jeda istirahat di terminal tujuan dan waktu singgah di terminal ampiran",
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
                text = "Jeda Keberangkatan (Waktu Istirahat di Terminal Tujuan)",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Bus akan beristirahat selama waktu ini setelah tiba sebelum otomatis berangkat kembali (PP)",
                fontSize = 11.sp,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(30, 45, 60, 90, 120).forEach { mins ->
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
                        text = "📋 Ringkasan Operasional 24 Jam",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlueDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Pola: Berjalan terus menerus bolak-balik (PP) 24 jam",
                        fontSize = 12.sp,
                        color = TextDark
                    )
                    Text(
                        text = "• Jeda Balik: ${uiState.headwayMinutes} Menit istirahat di terminal tujuan",
                        fontSize = 12.sp,
                        color = TextDark
                    )
                    Text(
                        text = "• Singgah Transit: ${uiState.transitDwellMinutes} Menit di tiap terminal ampiran",
                        fontSize = 12.sp,
                        color = TextDark
                    )
                    Text(
                        text = "• Keberangkatan Perdana: Menuju $outboundToCity dari $outboundFromCity",
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
