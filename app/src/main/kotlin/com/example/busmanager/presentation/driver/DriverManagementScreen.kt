package com.example.busmanager.presentation.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.busmanager.domain.driver.model.Driver
import com.example.busmanager.domain.fleet.model.Bus
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

@Composable
fun DriverManagementScreen(
    viewModel: DriverManagementViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = BackgroundWhite
    ) {
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
            return@Surface
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(AppDimens.paddingMedium)
        ) {
            DriverHeader(
                balanceFormatted = state.companyBalance.formatRupiah(),
                onNavigateBack = onNavigateBack
            )

            Spacer(modifier = Modifier.height(AppDimens.paddingMedium))

            DriverMetricsSummary(
                totalHired = state.hiredDrivers.size,
                totalAssigned = state.assignedDriverCount,
                totalDailyWageFormatted = state.totalDailyWage.formatRupiah()
            )

            Spacer(modifier = Modifier.height(AppDimens.paddingMedium))

            if (state.message != null) {
                MessageBanner(
                    message = state.message!!,
                    onDismiss = { viewModel.clearMessage() }
                )
                Spacer(modifier = Modifier.height(AppDimens.paddingSmall))
            }

            DriverTabSwitcher(
                selectedTab = state.selectedTab,
                hiredCount = state.hiredDrivers.size,
                candidateCount = state.candidateDrivers.size,
                onTabSelected = { viewModel.selectTab(it) }
            )

            Spacer(modifier = Modifier.height(AppDimens.paddingMedium))

            when (state.selectedTab) {
                DriverTab.HIRED -> {
                    HiredDriverList(
                        drivers = state.hiredDrivers,
                        buses = state.ownedBuses,
                        onAssignClick = { viewModel.openAssignDialog(it) },
                        onUnassignClick = { driverId ->
                            state.hiredDrivers.find { it.id == driverId }?.let {
                                viewModel.openUnassignConfirmation(it)
                            } ?: viewModel.unassignDriver(driverId)
                        },
                        onFireClick = { viewModel.openFireConfirmation(it) },
                        modifier = Modifier.weight(1f)
                    )
                }
                DriverTab.CANDIDATES -> {
                    CandidateDriverList(
                        candidates = state.candidateDrivers,
                        onHireClick = { viewModel.hireDriver(it) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (state.selectedDriverForAssignment != null) {
            AssignBusDialog(
                driver = state.selectedDriverForAssignment!!,
                buses = state.ownedBuses,
                drivers = state.hiredDrivers,
                onDismiss = { viewModel.closeAssignDialog() },
                onAssignConfirm = { busId ->
                    viewModel.assignDriverToBus(state.selectedDriverForAssignment!!.id, busId)
                }
            )
        }

        if (state.driverToUnassign != null) {
            val unassignDriver = state.driverToUnassign!!
            val assignedBus = state.ownedBuses.find { it.id == unassignDriver.assignedBusId }
            UnassignDriverDialog(
                driver = unassignDriver,
                assignedBus = assignedBus,
                onDismiss = { viewModel.closeUnassignConfirmation() },
                onConfirm = { viewModel.confirmUnassignDriver() }
            )
        }

        if (state.driverToFire != null) {
            FireDriverDialog(
                driver = state.driverToFire!!,
                onDismiss = { viewModel.closeFireConfirmation() },
                onConfirm = { viewModel.confirmFireDriver() }
            )
        }
    }
}

@Composable
private fun DriverHeader(
    balanceFormatted: String,
    onNavigateBack: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(IceBlue)
            ) {
                Text(
                    text = "←",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlueDark
                )
            }

            Spacer(modifier = Modifier.width(AppDimens.paddingSmall))

            Column {
                Text(
                    text = "Manajemen Kru",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDark
                )
                Text(
                    text = "Sopir & Penugasan Armada",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(AppDimens.radiusPill))
                .background(IceBlue)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = balanceFormatted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlueDark
            )
        }
    }
}

@Composable
private fun DriverMetricsSummary(
    totalHired: Int,
    totalAssigned: Int,
    totalDailyWageFormatted: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDimens.radiusMedium),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = AppDimens.cardElevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.paddingMedium),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MetricItem(
                label = "Total Sopir",
                value = "$totalHired Orang",
                emoji = "👨‍✈️",
                modifier = Modifier.weight(1f)
            )
            MetricItem(
                label = "Bertugas",
                value = "$totalAssigned Bus",
                emoji = "🚌",
                modifier = Modifier.weight(1f)
            )
            MetricItem(
                label = "Beban Gaji",
                value = "$totalDailyWageFormatted/hr",
                emoji = "💵",
                modifier = Modifier.weight(1.2f)
            )
        }
    }
}

@Composable
private fun MetricItem(
    label: String,
    value: String,
    emoji: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = emoji, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryBlueDark
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextMuted
        )
    }
}

@Composable
private fun DriverTabSwitcher(
    selectedTab: DriverTab,
    hiredCount: Int,
    candidateCount: Int,
    onTabSelected: (DriverTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppDimens.radiusPill))
            .background(IceBlue)
            .padding(4.dp)
    ) {
        TabButton(
            title = "Sopir Aktif ($hiredCount)",
            isSelected = selectedTab == DriverTab.HIRED,
            onClick = { onTabSelected(DriverTab.HIRED) },
            modifier = Modifier.weight(1f)
        )
        TabButton(
            title = "Bursa Pelamar ($candidateCount)",
            isSelected = selectedTab == DriverTab.CANDIDATES,
            onClick = { onTabSelected(DriverTab.CANDIDATES) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TabButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(AppDimens.radiusPill))
            .background(if (isSelected) PrimaryBlue else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (isSelected) Color.White else PrimaryBlueDark,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun HiredDriverList(
    drivers: List<Driver>,
    buses: List<Bus>,
    onAssignClick: (Driver) -> Unit,
    onUnassignClick: (String) -> Unit,
    onFireClick: (Driver) -> Unit,
    modifier: Modifier = Modifier
) {
    if (drivers.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(AppDimens.paddingLarge)
            ) {
                Text(text = "📭", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(AppDimens.paddingSmall))
                Text(
                    text = "Belum Ada Sopir yang Direkrut",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    text = "Buka tab 'Bursa Pelamar' untuk merekrut sopir ke armada Anda.",
                    fontSize = 12.sp,
                    color = TextMuted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppDimens.paddingSmall)
    ) {
        items(drivers, key = { it.id }) { driver ->
            val assignedBus = buses.find { it.id == driver.assignedBusId }
            HiredDriverCard(
                driver = driver,
                assignedBus = assignedBus,
                onAssignClick = { onAssignClick(driver) },
                onUnassignClick = { onUnassignClick(driver.id) },
                onFireClick = { onFireClick(driver) }
            )
        }
    }
}

@Composable
private fun HiredDriverCard(
    driver: Driver,
    assignedBus: Bus?,
    onAssignClick: () -> Unit,
    onUnassignClick: () -> Unit,
    onFireClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDimens.radiusMedium),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = AppDimens.cardElevation)
    ) {
        Column(
            modifier = Modifier.padding(AppDimens.paddingMedium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(IceBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "👨‍✈️", fontSize = 22.sp)
                    }

                    Spacer(modifier = Modifier.width(AppDimens.paddingSmall))

                    Column {
                        Text(
                            text = driver.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            text = "Gaji: ${driver.wagePerDay.formatRupiah()} / hari",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }

                if (assignedBus != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(AppDimens.radiusPill))
                            .background(SuccessGreenLight)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Bus ${assignedBus.busCode}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(AppDimens.radiusPill))
                            .background(WarningAmberLight)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Siaga",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = WarningAmber
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppDimens.paddingSmall)
            ) {
                Button(
                    onClick = onAssignClick,
                    shape = RoundedCornerShape(AppDimens.radiusPill),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (assignedBus != null) "Ganti Bus" else "Tugaskan Bus",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (driver.isPendingRelease) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(AppDimens.radiusPill))
                            .background(Color(0xFFFEF3C7))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "⏳ Menunggu Tiba di Terminal",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD97706)
                        )
                    }
                }

                if (assignedBus != null) {
                    OutlinedButton(
                        onClick = onUnassignClick,
                        enabled = !driver.isPendingRelease,
                        shape = RoundedCornerShape(AppDimens.radiusPill),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (driver.isPendingRelease) "Menunggu Tiba" else "Lepas",
                            fontSize = 12.sp,
                            color = if (driver.isPendingRelease) TextMuted else Color(0xFFD97706)
                        )
                    }
                }

                OutlinedButton(
                    onClick = onFireClick,
                    shape = RoundedCornerShape(AppDimens.radiusPill),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "PHK",
                        fontSize = 12.sp,
                        color = Color(0xFFDC2626)
                    )
                }
            }
        }
    }
}

@Composable
private fun CandidateDriverList(
    candidates: List<Driver>,
    onHireClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (candidates.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(AppDimens.paddingLarge)
            ) {
                Text(text = "💼", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(AppDimens.paddingSmall))
                Text(
                    text = "Tidak Ada Pelamar Baru",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    text = "Semua kandidat sopir telah direkrut ke dalam armada.",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppDimens.paddingSmall)
    ) {
        items(candidates, key = { it.id }) { candidate ->
            CandidateDriverCard(
                candidate = candidate,
                onHireClick = { onHireClick(candidate.id) }
            )
        }
    }
}

@Composable
private fun CandidateDriverCard(
    candidate: Driver,
    onHireClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDimens.radiusMedium),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = AppDimens.cardElevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.paddingMedium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(AccentBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "👨‍💼", fontSize = 22.sp)
                }

                Spacer(modifier = Modifier.width(AppDimens.paddingSmall))

                Column {
                    Text(
                        text = candidate.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Text(
                        text = "Permintaan: ${candidate.wagePerDay.formatRupiah()} / hari",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }

            Button(
                onClick = onHireClick,
                shape = RoundedCornerShape(AppDimens.radiusPill),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Rekrut",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun AssignBusDialog(
    driver: Driver,
    buses: List<Bus>,
    drivers: List<Driver>,
    onDismiss: () -> Unit,
    onAssignConfirm: (busId: String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Tugaskan ${driver.name}",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Pilih armada bus yang akan dikemudikan:",
                    fontSize = 13.sp,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (buses.isEmpty()) {
                    Text(
                        text = "Perusahaan belum memiliki bus. Beli bus terlebih dahulu di Showroom.",
                        fontSize = 13.sp,
                        color = Color(0xFFDC2626)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.height(240.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(buses, key = { it.id }) { bus ->
                            val currentDriver = drivers.find { it.assignedBusId == bus.id }
                            BusSelectionItem(
                                bus = bus,
                                currentDriver = currentDriver,
                                isAssignedToThisDriver = driver.assignedBusId == bus.id,
                                onSelect = { onAssignConfirm(bus.id) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Tutup")
            }
        }
    )
}

@Composable
private fun BusSelectionItem(
    bus: Bus,
    currentDriver: Driver?,
    isAssignedToThisDriver: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(AppDimens.radiusSmall),
        colors = CardDefaults.cardColors(
            containerColor = if (isAssignedToThisDriver) IceBlue else SurfaceWhite
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isAssignedToThisDriver) PrimaryBlue else CardBorder
        )
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Armada ${bus.busCode}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = PrimaryBlueDark
                )
                if (isAssignedToThisDriver) {
                    Text(
                        text = "✓ Ditugaskan",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                }
            }

            Text(
                text = bus.fullName,
                fontSize = 12.sp,
                color = TextDark
            )

            if (currentDriver != null && !isAssignedToThisDriver) {
                Text(
                    text = "Sopir saat ini: ${currentDriver.name} (akan digantikan)",
                    fontSize = 11.sp,
                    color = WarningAmber
                )
            }
        }
    }
}

@Composable
private fun UnassignDriverDialog(
    driver: Driver,
    assignedBus: Bus?,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val isOperating = assignedBus?.isOperating == true
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Lepas Tugas Pengemudi",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Text(
                text = if (isOperating) {
                    "Pengemudi ${driver.name} saat ini sedang bertugas di jalan raya bersama armada ${assignedBus?.busCode ?: "bus"}. Tugas pengemudi akan otomatis dilepas setelah armada tiba di terminal/titik singgah berikutnya."
                } else {
                    "Apakah Anda yakin ingin melepas tugas pengemudi ${driver.name} dari armada ${assignedBus?.busCode ?: "bus"}?"
                },
                fontSize = 14.sp,
                color = TextDark
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text(text = if (isOperating) "Mengerti & Jadwalkan Lepas" else "Ya, Lepas", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Batal")
            }
        }
    )
}

@Composable
private fun FireDriverDialog(
    driver: Driver,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Pemberhentian Sopir",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Text(
                text = "Apakah Anda yakin ingin memberhentikan (PHK) ${driver.name}? Sopir akan dilepas dari penugasan bus dan kembali menjadi kandidat pencari kerja.",
                fontSize = 14.sp,
                color = TextDark
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
            ) {
                Text(text = "Ya, PHK", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Batal")
            }
        }
    )
}

@Composable
private fun MessageBanner(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onDismiss),
        shape = RoundedCornerShape(AppDimens.radiusSmall),
        colors = CardDefaults.cardColors(containerColor = SuccessGreenLight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = SuccessGreen
            )
            Text(
                text = "✕",
                fontSize = 12.sp,
                color = SuccessGreen
            )
        }
    }
}
