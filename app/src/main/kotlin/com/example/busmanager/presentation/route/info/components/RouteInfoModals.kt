package com.example.busmanager.presentation.route.info.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmanager.domain.driver.model.Driver
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.ui.theme.AppDimens
import com.example.busmanager.ui.theme.IceBlue
import com.example.busmanager.ui.theme.PrimaryBlue
import com.example.busmanager.ui.theme.PrimaryBlueDark
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
                    text = "Trayek: ${route.originTerminal.city} ➔ ${route.destinationTerminal.city}",
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
                        "Trayek: ${route.originTerminal.city} ➔ ${route.destinationTerminal.city}"
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
                                            text = "👨‍✈️ ${driver.name}",
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
