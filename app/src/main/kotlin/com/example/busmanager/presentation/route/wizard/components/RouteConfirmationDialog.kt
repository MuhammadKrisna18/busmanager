package com.example.busmanager.presentation.route.wizard.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.busmanager.presentation.route.wizard.RouteWizardUiState
import com.example.busmanager.ui.theme.AppDimens
import com.example.busmanager.ui.theme.IceBlue
import com.example.busmanager.ui.theme.PrimaryBlue
import com.example.busmanager.ui.theme.PrimaryBlueDark
import com.example.busmanager.ui.theme.SuccessGreen
import com.example.busmanager.ui.theme.TextDark
import com.example.busmanager.ui.theme.TextMuted

@Composable
fun RouteConfirmationDialog(
    uiState: RouteWizardUiState,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val route = uiState.builtRoute
    val bus = uiState.selectedBus
    val driver = uiState.selectedDriver
    val originCity = uiState.selectedOrigin?.city ?: "Asal"
    val destCity = uiState.selectedDestination?.city ?: "Tujuan"
    val durationHours = uiState.estimatedDurationMinutes / 60
    val durationMins = uiState.estimatedDurationMinutes % 60
    val durationText = if (durationHours > 0) "$durationHours Jam $durationMins Menit" else "$durationMins Menit"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(AppDimens.radiusPill))
                        .background(IceBlue)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "TRAYEK",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlueDark
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Konfirmasi Buka Trayek",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
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
                Text(
                    text = "Periksa ringkasan rute sebelum meresmikan trayek armada:",
                    fontSize = 12.sp,
                    color = TextMuted
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = IceBlue)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "$originCity - $destCity",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlueDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Jalur: ${route?.name ?: ""}",
                            fontSize = 12.sp,
                            color = TextDark
                        )
                        Text(
                            text = "Jarak: ${route?.totalDistanceKm?.toInt() ?: 0} km • Durasi: $durationText",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                        if (route != null && route.transitTerminals.isNotEmpty()) {
                            Text(
                                text = "Mampir di: ${route.transitTerminals.joinToString(", ") { it.city }}",
                                fontSize = 11.sp,
                                color = PrimaryBlueDark,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else {
                            Text(
                                text = "Pola: Jalur Langsung (Tanpa Transit)",
                                fontSize = 11.sp,
                                color = SuccessGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Armada & Kru:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            text = "• Bus: ${bus?.busCode ?: "-"} (${bus?.fullName ?: "-"})",
                            fontSize = 12.sp,
                            color = TextDark
                        )
                        Text(
                            text = "• Pengemudi: ${driver?.name ?: "-"}",
                            fontSize = 12.sp,
                            color = TextDark
                        )
                        Text(
                            text = "• Jam Keberangkatan: ${String.format("%02d:%02d WIB", uiState.departureHour, uiState.departureMinute)} (${uiState.departureTimeDescription})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryBlueDark
                        )
                        Text(
                            text = "• Jeda Istirahat (PP): ${uiState.headwayMinutes} Menit (Min. 30 mnt)",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "Konfirmasi & Luncurkan", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "Kembali")
            }
        }
    )
}
