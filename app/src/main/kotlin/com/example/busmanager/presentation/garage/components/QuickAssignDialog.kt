package com.example.busmanager.presentation.garage.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.busmanager.domain.driver.model.Driver
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.RoutePathType
import com.example.busmanager.domain.simulation.model.RouteDirection
import com.example.busmanager.presentation.garage.QuickAssignState
import com.example.busmanager.ui.theme.CardBorder
import com.example.busmanager.ui.theme.IceBlue
import com.example.busmanager.ui.theme.PrimaryBlue
import com.example.busmanager.ui.theme.PrimaryBlueDark
import com.example.busmanager.ui.theme.SurfaceWhite
import com.example.busmanager.ui.theme.TextDark
import com.example.busmanager.ui.theme.TextMuted
import java.util.Locale

@Composable
fun QuickAssignDialog(
    state: QuickAssignState,
    onDismiss: () -> Unit,
    onSelectRoute: (BusRoute) -> Unit,
    onSelectDriver: (Driver) -> Unit,
    onSetTime: (Int, Int) -> Unit,
    onSetDirection: (RouteDirection) -> Unit,
    onSetHeadway: (Int) -> Unit,
    onSetTransitDwell: (Int) -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.88f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "⚡ Penugasan Cepat ke Trayek",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            text = "Armada ${state.bus.busCode} • ${state.bus.body.busClass.displayName}",
                            fontSize = 12.sp,
                            color = PrimaryBlueDark,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(IceBlue)
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "✕", fontSize = 12.sp, color = PrimaryBlueDark, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "1. Pilih Trayek Operasional",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        if (state.availableRoutes.isEmpty()) {
                            Text(
                                text = "Belum ada trayek aktif. Buat trayek terlebih dahulu di Wizard Rute.",
                                fontSize = 12.sp,
                                color = Color(0xFFDC2626)
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                state.availableRoutes.forEach { route ->
                                    val isSelected = state.selectedRoute?.id == route.id
                                    val corridorName = if (route.pathType == RoutePathType.TOLL) "Jalan Tol" else "Arteri Non-Tol"
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onSelectRoute(route) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) IceBlue else Color(0xFFF8FAFC)
                                        ),
                                        border = androidx.compose.foundation.BorderStroke(
                                            if (isSelected) 2.dp else 1.dp,
                                            if (isSelected) PrimaryBlue else CardBorder
                                        )
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
                                                    text = route.name,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextDark
                                                )
                                                Text(
                                                    text = "${route.originTerminal.city} ➔ ${route.destinationTerminal.city}",
                                                    fontSize = 11.sp,
                                                    color = TextMuted
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (route.pathType == RoutePathType.TOLL) PrimaryBlue else Color(0xFF64748B))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = corridorName,
                                                    fontSize = 10.sp,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "2. Pilih Sopir Siap Dinas",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        if (state.availableDrivers.isEmpty()) {
                            Text(
                                text = "⚠️ Tidak ada sopir siap dinas. Silakan rekrut sopir di menu Sopir.",
                                fontSize = 12.sp,
                                color = Color(0xFFB45309),
                                fontWeight = FontWeight.SemiBold
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                state.availableDrivers.forEach { driver ->
                                    val isSelected = state.selectedDriver?.id == driver.id
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onSelectDriver(driver) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) IceBlue else Color(0xFFF8FAFC)
                                        ),
                                        border = androidx.compose.foundation.BorderStroke(
                                            if (isSelected) 2.dp else 1.dp,
                                            if (isSelected) PrimaryBlue else CardBorder
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(text = "👨‍✈️", fontSize = 16.sp)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = driver.name,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextDark
                                                )
                                            }
                                            Text(
                                                text = "${driver.wagePerDay.formatRupiah()} / hari",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = PrimaryBlueDark
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        val route = state.selectedRoute
                        val originCity = route?.originTerminal?.city ?: "Asal"
                        val destCity = route?.destinationTerminal?.city ?: "Tujuan"

                        Text(
                            text = "3. Arah & Jam Keberangkatan",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val isOutbound = state.direction == RouteDirection.ORIGIN_TO_DESTINATION
                            FilterChip(
                                selected = isOutbound,
                                onClick = { onSetDirection(RouteDirection.ORIGIN_TO_DESTINATION) },
                                label = { Text("$originCity ➔ $destCity", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = !isOutbound,
                                onClick = { onSetDirection(RouteDirection.DESTINATION_TO_ORIGIN) },
                                label = { Text("$destCity ➔ $originCity", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Jam Keberangkatan:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextDark
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = {
                                                val newHour = if (state.departureHour > 0) state.departureHour - 1 else 23
                                                onSetTime(newHour, state.departureMinute)
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Text("◀", fontSize = 12.sp, color = PrimaryBlue)
                                        }
                                        Text(
                                            text = String.format(Locale.US, "%02d:%02d WIB", state.departureHour, state.departureMinute),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryBlueDark,
                                            modifier = Modifier.padding(horizontal = 6.dp)
                                        )
                                        IconButton(
                                            onClick = {
                                                val newHour = if (state.departureHour < 23) state.departureHour + 1 else 0
                                                onSetTime(newHour, state.departureMinute)
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Text("▶", fontSize = 12.sp, color = PrimaryBlue)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf(6, 7, 8, 9, 13, 15, 18).forEach { presetHour ->
                                        val isSel = state.departureHour == presetHour && state.departureMinute == 0
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isSel) PrimaryBlue else SurfaceWhite)
                                                .border(0.5.dp, if (isSel) PrimaryBlue else CardBorder, RoundedCornerShape(6.dp))
                                                .clickable { onSetTime(presetHour, 0) }
                                                .padding(vertical = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = String.format(Locale.US, "%02d:00", presetHour),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSel) Color.White else TextDark
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "4. Jeda Operasional PP & Waktu Transit",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Jeda Keberangkatan (Headway):",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextDark
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(IceBlue)
                                                .clickable { onSetHeadway((state.headwayMinutes - 10).coerceAtLeast(30)) }
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("-10", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryBlueDark)
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "${state.headwayMinutes} Menit",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryBlueDark
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(IceBlue)
                                                .clickable { onSetHeadway((state.headwayMinutes + 10).coerceAtMost(240)) }
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("+10", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryBlueDark)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Waktu istirahat sebelum putar balik PP (Min. 30 mnt)",
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf(30, 40, 50, 60, 90, 120).forEach { mins ->
                                        val isSel = state.headwayMinutes == mins
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isSel) PrimaryBlue else SurfaceWhite)
                                                .border(0.5.dp, if (isSel) PrimaryBlue else CardBorder, RoundedCornerShape(6.dp))
                                                .clickable { onSetHeadway(mins) }
                                                .padding(vertical = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "$mins m",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSel) Color.White else TextDark
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Waktu Singgah Ampiran:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextDark
                                    )
                                    Text(
                                        text = if (state.transitDwellMinutes == 0) "Langsung" else "${state.transitDwellMinutes} Menit",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryBlueDark
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf(0, 5, 10, 15).forEach { dwell ->
                                        val isSel = state.transitDwellMinutes == dwell
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isSel) PrimaryBlue else SurfaceWhite)
                                                .border(0.5.dp, if (isSel) PrimaryBlue else CardBorder, RoundedCornerShape(6.dp))
                                                .clickable { onSetTransitDwell(dwell) }
                                                .padding(vertical = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (dwell == 0) "0 m" else "$dwell m",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSel) Color.White else TextDark
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (state.conflictError != null) Color(0xFFFEE2E2) else Color(0xFFDCFCE7)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (state.conflictError != null) Color(0xFFDC2626) else Color(0xFF16A34A)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (state.conflictError != null) "⚠️" else "✅",
                                    fontSize = 18.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = state.conflictError ?: "Jadwal valid dan tersedia! Tidak ada bentrok armada pada rute dan jam ini.",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (state.conflictError != null) Color(0xFF991B1B) else Color(0xFF166534)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Batal", color = TextMuted, fontSize = 12.sp)
                    }

                    Button(
                        onClick = onConfirm,
                        enabled = state.canSubmit,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.4f),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text(
                            text = if (state.isSubmitting) "Menugaskan..." else "Tugaskan Sekarang",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
