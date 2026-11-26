package com.example.busmanager.presentation.garage.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.presentation.garage.GarageBusItem
import com.example.busmanager.ui.theme.PrimaryBlue
import com.example.busmanager.ui.theme.TextDark
import com.example.busmanager.ui.theme.TextMuted

@Composable
fun OperatingWarningDialog(
    bus: Bus,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Armada Sedang Dinas",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextDark
            )
        },
        text = {
            Text(
                text = "Armada ${bus.busCode} (${bus.fullName}) saat ini sedang berdinas melayani rute perjalanan di jalan raya.\n\nHarap batalkan dinas terlebih dahulu atau tunggu hingga bus tiba di terminal tujuan sebelum menjual armada.",
                fontSize = 13.sp,
                color = TextMuted
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Mengerti", fontWeight = FontWeight.Bold, color = PrimaryBlue)
            }
        }
    )
}

@Composable
fun SellBusDialog(
    item: GarageBusItem,
    onDismiss: () -> Unit,
    onConfirmSell: (String) -> Unit
) {
    val bus = item.bus
    val sellPrice = bus.resaleValue
    val hasAssignment = bus.hasRoute || bus.hasDriver

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (hasAssignment) "Konfirmasi Pelepasan & Penjualan" else "Konfirmasi Jual Armada",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextDark
            )
        },
        text = {
            val detailMessage = if (hasAssignment) {
                "Armada ${bus.busCode} (${bus.fullName}) saat ini memiliki penugasan${if (item.routeName != null) " trayek ${item.routeName}" else ""}${if (item.driverName != null) " bersama sopir ${item.driverName}" else ""}.\n\nMenjual armada ini akan otomatis melepas sopir kembali ke garasi dan membatalkan jadwal rute tersebut.\n\nApakah Anda yakin ingin menjual bus ini seharga ${sellPrice.formatRupiah()}?"
            } else {
                "Apakah Anda yakin ingin menjual armada ${bus.busCode} (${bus.fullName}) seharga ${sellPrice.formatRupiah()}?"
            }
            Text(
                text = detailMessage,
                fontSize = 13.sp,
                color = TextMuted
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirmSell(bus.id) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
            ) {
                Text("Jual Armada", color = Color.White, fontWeight = FontWeight.Bold)
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
fun GarageUserMessageDialog(
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Informasi",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextDark
            )
        },
        text = {
            Text(
                text = message,
                fontSize = 13.sp,
                color = TextDark
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK", fontWeight = FontWeight.Bold, color = PrimaryBlue)
            }
        }
    )
}
