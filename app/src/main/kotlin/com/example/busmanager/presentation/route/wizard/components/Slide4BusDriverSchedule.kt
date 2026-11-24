package com.example.busmanager.presentation.route.wizard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.IconButton
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
import com.example.busmanager.ui.theme.TextLight
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
    modifier: Modifier = Modifier
) {
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
                        brush = androidx.compose.ui.graphics.SolidColor(
                            if (isSelected) PrimaryBlue else CardBorder
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
                                    text = "• ${bus.chassis.brand.displayName} - ${bus.body.modelName}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextDark
                                )
                            }
                            Text(
                                text = "${bus.body.busClass.displayName} • ${bus.body.passengerCapacity} Kursi • Kesehatan: ${bus.condition.engineHealthPercent}%",
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
                        brush = androidx.compose.ui.graphics.SolidColor(
                            if (isSelected) PrimaryBlue else CardBorder
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
                        Column {
                            Text(
                                text = driver.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Text(
                                text = "Upah: ${driver.wagePerDay.formatRupiah()}/hari • Status: Siap Dinas",
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
                text = "3. Atur Jam Keberangkatan & Arah",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tentukan arah dinas dan jam berangkat bus",
                fontSize = 12.sp,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(10.dp))

            val originCity = uiState.selectedOrigin?.city ?: "Asal"
            val destCity = uiState.selectedDestination?.city ?: "Tujuan"

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
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Jam Keberangkatan",
                        fontSize = 12.sp,
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

            Spacer(modifier = Modifier.height(14.dp))
        }

        if (uiState.hasConflict) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = WarningAmberLight)
                ) {
                    Text(
                        text = "⚠️ Jadwal bentrok! Sudah ada bus yang berangkat pada rute dan jam yang sama. Silakan geser jam keberangkatan.",
                        modifier = Modifier.padding(14.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB45309)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (uiState.builtRoute != null && uiState.selectedBus != null && uiState.selectedDriver != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SuccessGreenLight),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SuccessGreen))
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
                        Text(
                            text = "Trayek, Matriks Tarif, Bus (${uiState.selectedBus.busCode}), dan Sopir (${uiState.selectedDriver.name}) telah siap. Klik 'Jalankan & Simpan' untuk mulai berdinas!",
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
