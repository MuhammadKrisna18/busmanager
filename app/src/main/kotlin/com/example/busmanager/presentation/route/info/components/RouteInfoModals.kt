package com.example.busmanager.presentation.route.info.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.busmanager.domain.route.model.RouteSchedule
import com.example.busmanager.domain.simulation.model.RouteDirection
import com.example.busmanager.ui.theme.AppDimens
import com.example.busmanager.ui.theme.IceBlue
import com.example.busmanager.ui.theme.PrimaryBlue
import com.example.busmanager.ui.theme.PrimaryBlueDark
import com.example.busmanager.ui.theme.SurfaceWhite
import com.example.busmanager.ui.theme.TextDark
import com.example.busmanager.ui.theme.TextMuted
import com.example.busmanager.ui.theme.WarningAmber

@Composable
fun AssignBusModal(
    route: BusRoute,
    availableBuses: List<Bus>,
    onDismiss: () -> Unit,
    onBusSelected: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Tugaskan Armada Bus",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Trayek: ${route.originTerminal.city} - ${route.destinationTerminal.city}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryBlueDark
                )
                if (availableBuses.isEmpty()) {
                    Text(
                        text = "Tidak ada armada bus yang menganggur di garasi. Silakan beli armada baru di Showroom atau lepas penugasan bus dari trayek lain.",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(availableBuses) { bus ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onBusSelected(bus.id) },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = IceBlue)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${bus.busCode} • ${bus.fullName}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextDark
                                        )
                                        Text(
                                            text = "Kelas ${bus.body.busClass.displayName} • ${bus.body.passengerCapacity} Kursi",
                                            fontSize = 10.sp,
                                            color = TextMuted
                                        )
                                    }
                                    Button(
                                        onClick = { onBusSelected(bus.id) },
                                        shape = RoundedCornerShape(AppDimens.radiusPill),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                                    ) {
                                        Text("Pilih", fontSize = 11.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}

@Composable
fun AssignDriverModal(
    route: BusRoute,
    targetBus: Bus?,
    availableDrivers: List<Driver>,
    onDismiss: () -> Unit,
    onDriverSelected: (String, String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Tugaskan Pengemudi (Sopir)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (targetBus != null) {
                        "Target Armada: ${targetBus.busCode} (${targetBus.fullName})"
                    } else {
                        "Trayek: ${route.originTerminal.city} - ${route.destinationTerminal.city}"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryBlueDark
                )
                if (targetBus == null) {
                    Text(
                        text = "Belum ada armada bus yang ditugaskan ke trayek ini. Silakan input bus terlebih dahulu sebelum menugaskan driver.",
                        fontSize = 12.sp,
                        color = WarningAmber
                    )
                } else if (availableDrivers.isEmpty()) {
                    Text(
                        text = "Tidak ada pengemudi yang menganggur. Silakan rekrut pengemudi baru di menu Manajemen Kru.",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(availableDrivers) { driver ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onDriverSelected(driver.id, targetBus.id) },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = IceBlue)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = driver.name,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextDark
                                        )
                                        Text(
                                            text = "Gaji: ${driver.wagePerDay.formatRupiah()} / hari",
                                            fontSize = 10.sp,
                                            color = TextMuted
                                        )
                                    }
                                    Button(
                                        onClick = { onDriverSelected(driver.id, targetBus.id) },
                                        shape = RoundedCornerShape(AppDimens.radiusPill),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                                    ) {
                                        Text("Pilih", fontSize = 11.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}

@Composable
fun EditScheduleModal(
    route: BusRoute,
    schedules: List<RouteSchedule>,
    ownedBuses: List<Bus>,
    onDismiss: () -> Unit,
    onAddSchedule: (hour: Int, minute: Int, direction: RouteDirection, headwayMinutes: Int, assignedBusId: String?) -> Unit,
    onDeleteSchedule: (RouteSchedule) -> Unit
) {
    var selectedHour by remember { mutableIntStateOf(8) }
    var selectedMinute by remember { mutableIntStateOf(0) }
    var selectedDirection by remember { mutableStateOf(RouteDirection.ORIGIN_TO_DESTINATION) }
    var selectedHeadway by remember { mutableIntStateOf(60) }
    var selectedBusId by remember { mutableStateOf<String?>(null) }
    var isAddingNew by remember { mutableStateOf(false) }

    val headwayOptions = listOf(30, 45, 60, 90, 120)
    val quickHours = listOf(6, 8, 10, 13, 15, 18, 20)
    val quickMinutes = listOf(0, 15, 30, 45)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Pengaturan Jadwal Keberangkatan",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    text = "${route.originTerminal.city} - ${route.destinationTerminal.city} (${route.name})",
                    fontSize = 12.sp,
                    color = PrimaryBlueDark
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Jadwal Aktif (${schedules.size}):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    OutlinedButton(
                        onClick = { isAddingNew = !isAddingNew },
                        shape = RoundedCornerShape(AppDimens.radiusPill),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isAddingNew) "Tutup Tambah" else "+ Tambah Jadwal",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                    }
                }

                if (schedules.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(IceBlue)
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Belum ada jadwal keberangkatan untuk rute ini.",
                            fontSize = 11.sp,
                            color = PrimaryBlueDark
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = if (isAddingNew) 140.dp else 240.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(schedules) { sched ->
                            val assignedBus = ownedBuses.find { it.id == sched.assignedBusId }
                            val dirText = if (sched.direction == RouteDirection.ORIGIN_TO_DESTINATION) {
                                "${route.originTerminal.city} ke ${route.destinationTerminal.city}"
                            } else {
                                "${route.destinationTerminal.city} ke ${route.originTerminal.city}"
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = IceBlue)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = sched.formattedTime,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryBlueDark
                                        )
                                        Text(
                                            text = "Arah: $dirText",
                                            fontSize = 10.sp,
                                            color = TextDark
                                        )
                                        Text(
                                            text = "Bus: ${assignedBus?.let { "${it.busCode} • ${it.fullName}" } ?: "Belum ditentukan"} • Jeda: ${sched.headwayMinutes} mnt",
                                            fontSize = 10.sp,
                                            color = TextMuted
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = { onDeleteSchedule(sched) },
                                        shape = RoundedCornerShape(AppDimens.radiusPill),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5))
                                    ) {
                                        Text(
                                            text = "Hapus",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFDC2626)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (isAddingNew) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF8FAFC))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Tambah Jadwal Keberangkatan",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )

                        Text(
                            text = "Jam Keberangkatan: ${String.format("%02d:%02d WIB", selectedHour, selectedMinute)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryBlueDark
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            quickHours.forEach { h ->
                                val isSelected = selectedHour == h
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) PrimaryBlue else IceBlue)
                                        .clickable { selectedHour = h }
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = String.format("%02d", h),
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else PrimaryBlueDark
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            quickMinutes.forEach { m ->
                                val isSelected = selectedMinute == m
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) PrimaryBlue else IceBlue)
                                        .clickable { selectedMinute = m }
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = ":${String.format("%02d", m)}",
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else PrimaryBlueDark
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Arah Perjalanan:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDark
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val isOriginToDest = selectedDirection == RouteDirection.ORIGIN_TO_DESTINATION
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isOriginToDest) PrimaryBlue else IceBlue)
                                    .clickable { selectedDirection = RouteDirection.ORIGIN_TO_DESTINATION }
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${route.originTerminal.city} ke ${route.destinationTerminal.city}",
                                    fontSize = 10.sp,
                                    fontWeight = if (isOriginToDest) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isOriginToDest) Color.White else PrimaryBlueDark
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (!isOriginToDest) PrimaryBlue else IceBlue)
                                    .clickable { selectedDirection = RouteDirection.DESTINATION_TO_ORIGIN }
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${route.destinationTerminal.city} ke ${route.originTerminal.city}",
                                    fontSize = 10.sp,
                                    fontWeight = if (!isOriginToDest) FontWeight.Bold else FontWeight.Normal,
                                    color = if (!isOriginToDest) Color.White else PrimaryBlueDark
                                )
                            }
                        }

                        Text(
                            text = "Jeda (Headway):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDark
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            headwayOptions.forEach { hw ->
                                val isSelected = selectedHeadway == hw
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) PrimaryBlue else IceBlue)
                                        .clickable { selectedHeadway = hw }
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$hw mnt",
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else PrimaryBlueDark
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                onAddSchedule(selectedHour, selectedMinute, selectedDirection, selectedHeadway, selectedBusId)
                                isAddingNew = false
                            },
                            shape = RoundedCornerShape(AppDimens.radiusPill),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            Text(
                                text = "Simpan Jadwal Baru",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}

@Composable
fun DeleteRouteConfirmationDialog(
    route: BusRoute,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Konfirmasi Tutup Trayek",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextDark
            )
        },
        text = {
            Text(
                text = "Apakah Anda yakin ingin menutup dan menghapus trayek \"${route.name}\"?\n\nSeluruh jadwal operasional dan penugasan armada pada trayek ini akan dibatalkan.",
                fontSize = 13.sp,
                color = TextMuted
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
            ) {
                Text("Tutup Trayek", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun RouteInfoErrorDialog(
    errorMessage: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Peringatan",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFFDC2626)
            )
        },
        text = {
            Text(
                text = errorMessage,
                fontSize = 13.sp,
                color = TextDark
            )
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Mengerti")
            }
        }
    )
}
