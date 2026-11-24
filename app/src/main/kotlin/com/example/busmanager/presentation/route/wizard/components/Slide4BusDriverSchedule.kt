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
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = bus.busCode,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlueDark
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "• ${bus.fullName}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextDark
                                )
                            }
                            Text(
                                text = "${bus.body.busClass.displayName} • ${bus.body.passengerCapacity} Kursi",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }

                        if (isSelected) {
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
            items(uiState.availableDrivers) { driver ->
                val assignedBusCode = uiState.driverAssignedBusCodes[driver.id]
                val isAssignedToOtherBus = assignedBusCode != null && (uiState.selectedBus == null || assignedBusCode != uiState.selectedBus.busCode)
                val isSelected = uiState.selectedDriver?.id == driver.id

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(enabled = !isAssignedToOtherBus) { onSelectDriver(driver) },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            isAssignedToOtherBus -> Color(0xFFF8FAFC)
                            isSelected -> IceBlue
                            else -> SurfaceWhite
                        }
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = SolidColor(
                            when {
                                isAssignedToOtherBus -> CardBorder
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = driver.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAssignedToOtherBus) TextMuted else TextDark
                                )
                                if (isAssignedToOtherBus) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(99.dp))
                                            .background(Color(0xFFFEE2E2))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Bertugas di $assignedBusCode",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFDC2626)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isAssignedToOtherBus) {
                                    "Bagi Hasil: 30% per Rit Dinas • Sedang Bertugas"
                                } else {
                                    "Bagi Hasil: 30% per Rit Dinas • Status: Siap Dinas"
                                },
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }

                        if (isSelected) {
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
                text = "3. Atur Pola & Jam Keberangkatan",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tentukan pola dinas bolak-balik atau satu arah serta jam berangkat armada",
                fontSize = 12.sp,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Pola Perjalanan",
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
                    selected = uiState.isRoundTrip,
                    onClick = { onSetIsRoundTrip(true) },
                    label = { Text("🔄 Pulang-Pergi (A-B & B-A)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IceBlue,
                        selectedLabelColor = PrimaryBlueDark
                    )
                )
                FilterChip(
                    selected = !uiState.isRoundTrip,
                    onClick = { onSetIsRoundTrip(false) },
                    label = { Text("➡️ Satu Arah") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IceBlue,
                        selectedLabelColor = PrimaryBlueDark
                    )
                )
            }

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
                    label = { Text("$originCity ➔ $destCity") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IceBlue,
                        selectedLabelColor = PrimaryBlueDark
                    )
                )
                FilterChip(
                    selected = uiState.direction == RouteDirection.DESTINATION_TO_ORIGIN,
                    onClick = { onSetDirection(RouteDirection.DESTINATION_TO_ORIGIN) },
                    label = { Text("$destCity ➔ $originCity") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IceBlue,
                        selectedLabelColor = PrimaryBlueDark
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(CardBorder))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Jadwal Berangkat Pergi ($outboundFromCity ➔ $outboundToCity)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = String.format("%02d:%02d WIB", uiState.departureHour, uiState.departureMinute),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlueDark
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                val newH = (uiState.departureHour - 1).let { if (it < 0) 23 else it }
                                onSetDepartureTime(newH, uiState.departureMinute)
                            }
                        ) {
                            Text("-1 Jam")
                        }
                        OutlinedButton(
                            onClick = {
                                val newH = (uiState.departureHour + 1) % 24
                                onSetDepartureTime(newH, uiState.departureMinute)
                            }
                        ) {
                            Text("+1 Jam")
                        }
                        OutlinedButton(
                            onClick = {
                                val newM = (uiState.departureMinute + 15) % 60
                                onSetDepartureTime(uiState.departureHour, newM)
                            }
                        ) {
                            Text("+15 Mnt")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val quickHours = listOf(Pair(6, 0), Pair(7, 30), Pair(13, 0), Pair(19, 30))
                        quickHours.forEach { (h, m) ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(IceBlue)
                                    .clickable { onSetDepartureTime(h, m) }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = String.format("%02d:%02d", h, m),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlue
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = IceBlue),
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(PrimaryBlue.copy(alpha = 0.3f)))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "⏱️", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Estimasi Perjalanan & Waktu Tiba",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlueDark
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Durasi Tempuh ($distanceText)", fontSize = 12.sp, color = TextDark)
                        Text(text = durationText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = PrimaryBlueDark)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Estimasi Tiba di $outboundToCity", fontSize = 12.sp, color = TextDark)
                        Text(
                            text = String.format("%02d:%02d WIB", uiState.arrivalHour, uiState.arrivalMinute),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlueDark
                        )
                    }
                    if (uiState.isRoundTrip) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Jeda Istirahat Minimal Kru", fontSize = 12.sp, color = TextDark)
                            Text(text = "1 Jam (Wajib)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Keberangkatan Pulang Tercepat", fontSize = 12.sp, color = TextDark)
                            Text(
                                text = String.format("%02d:%02d WIB", uiState.minReturnHour, uiState.minReturnMinute),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen
                            )
                        }
                    }
                }
            }

            if (uiState.isRoundTrip) {
                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = SolidColor(if (uiState.hasLayoverConflict) WarningAmber else CardBorder)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Jadwal Berangkat Pulang ($returnFromCity ➔ $returnToCity)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = String.format("%02d:%02d WIB", uiState.returnHour, uiState.returnMinute),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.hasLayoverConflict) WarningAmber else PrimaryBlueDark
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val newH = (uiState.returnHour - 1).let { if (it < 0) 23 else it }
                                    onSetReturnTime(newH, uiState.returnMinute)
                                }
                            ) {
                                Text("-1 Jam")
                            }
                            OutlinedButton(
                                onClick = {
                                    val newH = (uiState.returnHour + 1) % 24
                                    onSetReturnTime(newH, uiState.returnMinute)
                                }
                            ) {
                                Text("+1 Jam")
                            }
                            OutlinedButton(
                                onClick = {
                                    val newM = (uiState.returnMinute + 15) % 60
                                    onSetReturnTime(uiState.returnHour, newM)
                                }
                            ) {
                                Text("+15 Mnt")
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(IceBlue)
                                .clickable { onResetReturnTime() }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = String.format("Set ke Rekomendasi Tercepat (%02d:%02d WIB)", uiState.minReturnHour, uiState.minReturnMinute),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        if (uiState.hasLayoverConflict) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = WarningAmberLight)
                ) {
                    Text(
                        text = "⚠️ Jeda istirahat bus & kru kurang dari 1 jam! Bus baru tiba pukul ${String.format("%02d:%02d", uiState.arrivalHour, uiState.arrivalMinute)} WIB, sehingga paling cepat dapat berangkat kembali pukul ${String.format("%02d:%02d", uiState.minReturnHour, uiState.minReturnMinute)} WIB.",
                        modifier = Modifier.padding(14.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB45309)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        if (uiState.hasOutboundConflict) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = WarningAmberLight)
                ) {
                    Text(
                        text = "⚠️ Jadwal pergi bentrok! Sudah ada bus yang berangkat pada rute dan jam yang sama. Silakan geser jam keberangkatan.",
                        modifier = Modifier.padding(14.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB45309)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        if (uiState.hasReturnConflict) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = WarningAmberLight)
                ) {
                    Text(
                        text = "⚠️ Jadwal pulang bentrok! Sudah ada bus yang berangkat pada rute pulang pada jam tersebut. Silakan sesuaikan jam pulang.",
                        modifier = Modifier.padding(14.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB45309)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
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
