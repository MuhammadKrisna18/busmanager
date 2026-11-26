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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.busmanager.ui.theme.AppDimens
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

@Composable
fun Slide4BusDriverSchedule(
    uiState: RouteWizardUiState,
    onSelectBus: (Bus?) -> Unit,
    onSelectDriver: (Driver?) -> Unit,
    onSetDepartureTime: (Int, Int) -> Unit,
    onSetDepartureTimeInput: (String) -> Unit = {},
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

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        item {
            Text(
                text = "1. Pilih Armada Bus (Opsional)",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Pilih bus di garasi untuk melayani rute ini atau lewati untuk menyimpan rute dalam status Standby",
                fontSize = 12.sp,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(8.dp))

            val isNoBusSelected = uiState.selectedBus == null
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onSelectBus(null) },
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isNoBusSelected) IceBlue else SurfaceWhite
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = SolidColor(if (isNoBusSelected) PrimaryBlue else CardBorder)
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
                            text = "Lewati Penugasan Bus (Tugaskan Nanti)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isNoBusSelected) PrimaryBlueDark else TextDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Rute disimpan standby tanpa armada; bus dapat dimasukkan nanti di Informasi Trayek atau Garasi",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }

                    if (isNoBusSelected) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(AppDimens.radiusPill))
                                .background(PrimaryBlue)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Terpilih",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
        }

        if (uiState.availableBuses.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = WarningAmberLight)
                ) {
                    Text(
                        text = "Tidak ada bus yang siap beroperasi di garasi. Rute akan disimpan sebagai Standby melalui tombol 'Simpan Standby' di bawah.",
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
                                    text = "Posisi: Terminal $termName",
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
                                    .clip(RoundedCornerShape(AppDimens.radiusPill))
                                    .background(PrimaryBlue)
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Terpilih",
                                    color = Color.White,
                                    fontSize = 10.sp,
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
                text = "2. Pilih Pengemudi / Sopir (Opsional)",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Pilih sopir yang bertugas atau lewati untuk menugaskan nanti",
                fontSize = 12.sp,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(8.dp))

            val isNoDriverSelected = uiState.selectedDriver == null
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onSelectDriver(null) },
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isNoDriverSelected) IceBlue else SurfaceWhite
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = SolidColor(if (isNoDriverSelected) PrimaryBlue else CardBorder)
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
                            text = "Lewati Penugasan Sopir (Tugaskan Nanti)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isNoDriverSelected) PrimaryBlueDark else TextDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Sopir dapat direkrut dan ditugaskan nanti melalui menu Manajemen Kru atau Informasi Trayek",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }

                    if (isNoDriverSelected) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(AppDimens.radiusPill))
                                .background(PrimaryBlue)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Terpilih",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
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
                                    .clip(RoundedCornerShape(AppDimens.radiusPill))
                                    .background(PrimaryBlue)
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Terpilih",
                                    color = Color.White,
                                    fontSize = 10.sp,
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
                text = "3. Atur Pola & Jadwal Operasional (04.00 - 23.59 WIB)",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Pola operasional default adalah bolak-balik (PP) dari pagi hingga malam",
                fontSize = 12.sp,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Arah Keberangkatan Awal",
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
                    label = { Text("$originCity ke $destCity") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IceBlue,
                        selectedLabelColor = PrimaryBlueDark
                    ),
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = uiState.direction == RouteDirection.DESTINATION_TO_ORIGIN,
                    onClick = { onSetDirection(RouteDirection.DESTINATION_TO_ORIGIN) },
                    label = { Text("$destCity ke $originCity") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IceBlue,
                        selectedLabelColor = PrimaryBlueDark
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Jam Keberangkatan Perdana",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Ketik jam keberangkatan pertama armada (Contoh: 07:00)",
                fontSize = 11.sp,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = uiState.departureTimeInputText,
                onValueChange = onSetDepartureTimeInput,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = CardBorder,
                    focusedContainerColor = SurfaceWhite,
                    unfocusedContainerColor = SurfaceWhite
                ),
                placeholder = { Text("Contoh: 07:00") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(6.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = IceBlue)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Terdeteksi: ${uiState.departureTimeDescription}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlueDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("05:00", "06:00", "07:00", "12:00", "15:00", "19:00").forEach { quickTime ->
                    FilterChip(
                        selected = uiState.departureTimeInputText.trim() == quickTime,
                        onClick = { onSetDepartureTimeInput(quickTime) },
                        label = { Text(quickTime) },
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

            if (uiState.builtRoute?.transitTerminals.isNullOrEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = IceBlue)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Rute Langsung Non-Stop: Bus melaju langsung ke tujuan tanpa berhenti di halte perantara.",
                            fontSize = 11.sp,
                            color = PrimaryBlueDark
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            } else {
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
            }

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
                        text = "Ringkasan Operasional (04.00 - 23.59 WIB)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlueDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Pola: Bolak-balik (PP) pukul 04.00 - 23.59 WIB",
                        fontSize = 12.sp,
                        color = TextDark
                    )
                    Text(
                        text = "• Jeda Balik: ${uiState.headwayMinutes} Menit istirahat di terminal tujuan (Min. 30 mnt)",
                        fontSize = 12.sp,
                        color = TextDark
                    )
                    val transitSummary = if (uiState.builtRoute?.transitTerminals.isNullOrEmpty()) {
                        "• Pola Rute: Rute langsung non-stop tanpa transit"
                    } else {
                        "• Singgah Transit: ${uiState.transitDwellMinutes} Menit di ${uiState.builtRoute.transitTerminals.size} terminal ampiran"
                    }
                    Text(
                        text = transitSummary,
                        fontSize = 12.sp,
                        color = TextDark
                    )
                    Text(
                        text = "• Keberangkatan Perdana: ${String.format("%02d:%02d WIB", uiState.departureHour, uiState.departureMinute)} (${uiState.departureTimeDescription}) menuju $outboundToCity dari $outboundFromCity",
                        fontSize = 12.sp,
                        color = TextDark
                    )
                    Text(
                        text = "• Jam Malam: Di atas pukul 23.59 armada menginap di terminal tujuan dan siap kembali pukul 04.00",
                        fontSize = 12.sp,
                        color = TextDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        if (uiState.builtRoute != null && (uiState.selectedBus == null || uiState.selectedDriver == null)) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = IceBlue),
                    border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(PrimaryBlue.copy(alpha = 0.5f)))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Status Simpan Standby",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlueDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Trayek ini akan disimpan dalam status Standby tanpa armada langsung. Anda dapat menugaskan bus dan sopir kapan saja melalui menu Garasi atau Informasi Trayek. Klik tombol 'Simpan Standby' di bawah untuk menyimpan.",
                            fontSize = 12.sp,
                            color = TextDark
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        } else if (uiState.builtRoute != null && uiState.selectedBus != null && uiState.selectedDriver != null && !uiState.hasConflict) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SuccessGreenLight),
                    border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(SuccessGreen))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Semua Syarat Terpenuhi",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        )
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
