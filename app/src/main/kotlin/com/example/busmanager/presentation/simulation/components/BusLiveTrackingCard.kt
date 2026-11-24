package com.example.busmanager.presentation.simulation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.busmanager.domain.simulation.model.ActiveBusTrip
import com.example.busmanager.ui.theme.AppDimens
import com.example.busmanager.ui.theme.CardBorder
import com.example.busmanager.ui.theme.IceBlue
import com.example.busmanager.ui.theme.PrimaryBlue
import com.example.busmanager.ui.theme.PrimaryBlueDark
import com.example.busmanager.ui.theme.SuccessGreen
import com.example.busmanager.ui.theme.SurfaceWhite
import com.example.busmanager.ui.theme.TextDark
import com.example.busmanager.ui.theme.TextMuted

@Composable
fun BusLiveTrackingCard(
    trip: ActiveBusTrip,
    onClose: () -> Unit,
    onCancelNextSchedule: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDimens.radiusMedium),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = AppDimens.cardElevation),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.paddingMedium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(AppDimens.radiusSmall))
                            .background(Color(0xFFDC2626))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "🎯 ${trip.busCode}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Spacer(modifier = Modifier.size(8.dp))

                    Column {
                        Text(
                            text = "Menuju ${trip.currentDestinationName} dari ${trip.currentOriginName}",
                            color = TextDark,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Kamera Terkunci • Melacak Armada",
                            color = Color(0xFFDC2626),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE2E8F0))
                        .clickable(onClick = onClose),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✕",
                        color = TextDark,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${trip.route.name} • Menuju ${trip.currentDestinationName} dari ${trip.currentOriginName}",
                color = TextMuted,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Progres: ${(trip.progressFraction * 100).toInt()}%",
                    color = PrimaryBlue,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Sisa: ${trip.remainingMinutes} Menit",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = { trip.progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = PrimaryBlue,
                trackColor = IceBlue
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "👥 ${trip.passengerCount} Penumpang",
                    color = TextDark,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "💰 Tiket: ${trip.ticketEarnings.formatRupiah()}",
                    color = SuccessGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (trip.cancelNextSchedule) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFEF3C7))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "🛑 Jadwal berikutnya dibatalkan. Bus akan menunggu jadwal keberangkatan di ${trip.currentDestinationName}.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFB45309)
                    )
                }
            } else {
                OutlinedButton(
                    onClick = onCancelNextSchedule,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(AppDimens.radiusSmall),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFDC2626)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDC2626)),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Text(
                        text = "🛑 Batalkan Jadwal Berikutnya (Menunggu Jadwal di ${trip.currentDestinationName})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
