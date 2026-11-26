package com.example.busmanager.presentation.driver.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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

@Composable
fun AssignBusDialog(
    driver: Driver,
    buses: List<Bus>,
    drivers: List<Driver>,
    onDismiss: () -> Unit,
    onAssignConfirm: (busId: String) -> Unit
) {
    val availableBuses = buses.filter { bus ->
        drivers.none { it.assignedBusId == bus.id && it.id != driver.id }
    }

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

                if (availableBuses.isEmpty()) {
                    Text(
                        text = "Tidak ada armada bus yang tersedia. Semua bus sudah memiliki pengemudi atau Anda belum memiliki bus.",
                        fontSize = 13.sp,
                        color = Color(0xFFDC2626)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.height(240.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(availableBuses, key = { it.id }) { bus ->
                            BusSelectionItem(
                                bus = bus,
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
fun BusSelectionItem(
    bus: Bus,
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
        border = BorderStroke(1.dp, if (isAssignedToThisDriver) PrimaryBlue else CardBorder)
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
                        text = "Ditugaskan",
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
        }
    }
}

@Composable
fun UnassignDriverDialog(
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
                    "Pengemudi ${driver.name} saat ini sedang bertugas di jalan raya bersama armada ${assignedBus.busCode}. Tugas pengemudi akan otomatis dilepas setelah armada tiba di terminal/titik singgah berikutnya."
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
fun FireDriverDialog(
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
fun MessageBanner(
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
                text = "Tutup",
                fontSize = 12.sp,
                color = SuccessGreen
            )
        }
    }
}
