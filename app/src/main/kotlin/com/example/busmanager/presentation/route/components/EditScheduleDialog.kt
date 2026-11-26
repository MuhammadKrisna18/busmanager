package com.example.busmanager.presentation.route.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.busmanager.ui.theme.AppDimens
import com.example.busmanager.ui.theme.IceBlue
import com.example.busmanager.ui.theme.PrimaryBlue
import com.example.busmanager.ui.theme.PrimaryBlueDark
import com.example.busmanager.ui.theme.SurfaceWhite
import com.example.busmanager.ui.theme.TextDark
import com.example.busmanager.ui.theme.TextMuted

@Composable
fun EditScheduleDialog(
    busCode: String,
    routeName: String,
    initialHeadwayMinutes: Int,
    initialTransitDwellMinutes: Int,
    onDismiss: () -> Unit,
    onSave: (headwayMinutes: Int, transitDwellMinutes: Int) -> Unit
) {
    var headway by remember { mutableIntStateOf(initialHeadwayMinutes.coerceAtLeast(30)) }
    var dwell by remember { mutableIntStateOf(initialTransitDwellMinutes) }

    val headwayOptions = listOf(30, 40, 50, 60, 90, 120)
    val dwellOptions = listOf(0, 5, 10, 15)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(AppDimens.radiusMedium),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppDimens.paddingMedium)
            ) {
                Text(
                    text = "Ubah Jadwal Operasional",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$busCode • $routeName",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryBlueDark
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Jeda Keberangkatan (Headway):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(IceBlue)
                                .clickable { headway = (headway - 10).coerceAtLeast(30) }
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("-10", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryBlueDark)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$headway Menit",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlueDark
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(IceBlue)
                                .clickable { headway = (headway + 10).coerceAtMost(240) }
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("+10", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryBlueDark)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Waktu jeda armada di terminal sebelum berangkat kembali (PP) (Min. 30 mnt)",
                    fontSize = 10.sp,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    headwayOptions.forEach { minutes ->
                        val isSelected = headway == minutes
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) PrimaryBlue else IceBlue)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) PrimaryBlueDark else Color(0xFFBFDBFE),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { headway = minutes }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$minutes mnt",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else PrimaryBlueDark
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Waktu Pemberhentian di Terminal Ampiran:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Waktu naik & turun penumpang saat singgah di terminal transit",
                    fontSize = 10.sp,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    dwellOptions.forEach { minutes ->
                        val isSelected = dwell == minutes
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) PrimaryBlue else IceBlue)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) PrimaryBlueDark else Color(0xFFBFDBFE),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { dwell = minutes }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (minutes == 0) "Langsung" else "$minutes mnt",
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else PrimaryBlueDark
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(AppDimens.radiusMedium),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Batal", color = TextDark, fontSize = 12.sp)
                    }
                    Button(
                        onClick = { onSave(headway, dwell) },
                        shape = RoundedCornerShape(AppDimens.radiusMedium),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Simpan", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
