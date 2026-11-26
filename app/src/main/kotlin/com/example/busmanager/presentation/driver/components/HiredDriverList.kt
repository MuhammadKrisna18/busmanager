package com.example.busmanager.presentation.driver.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmanager.domain.driver.model.Driver
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.ui.theme.AppDimens
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
fun HiredDriverList(
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
                Text(
                    text = "Belum Ada Sopir yang Direkrut",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(AppDimens.paddingSmall))
                Text(
                    text = "Buka tab Bursa Pelamar untuk merekrut sopir ke armada Anda.",
                    fontSize = 12.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    val hasAvailableBus = buses.any { bus ->
        drivers.none { it.assignedBusId == bus.id }
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
                hasAvailableBus = hasAvailableBus,
                onAssignClick = { onAssignClick(driver) },
                onUnassignClick = { onUnassignClick(driver.id) },
                onFireClick = { onFireClick(driver) }
            )
        }
    }
}

@Composable
fun HiredDriverCard(
    driver: Driver,
    assignedBus: Bus?,
    hasAvailableBus: Boolean,
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
                        Text(
                            text = driver.name.take(1).uppercase(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlueDark
                        )
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
                horizontalArrangement = Arrangement.spacedBy(AppDimens.paddingSmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (assignedBus == null) {
                    Button(
                        onClick = onAssignClick,
                        enabled = hasAvailableBus,
                        shape = RoundedCornerShape(AppDimens.radiusPill),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBlue,
                            disabledContainerColor = Color(0xFFE2E8F0),
                            disabledContentColor = TextMuted
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (hasAvailableBus) "Tugaskan Bus" else "Bus Tidak Tersedia",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (driver.isPendingRelease) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(AppDimens.radiusPill))
                            .background(Color(0xFFFEF3C7))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Menunggu Tiba di Terminal",
                            fontSize = 11.sp,
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
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (driver.isPendingRelease) "Menunggu Tiba" else "Lepas Tugas",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (driver.isPendingRelease) TextMuted else Color(0xFFD97706)
                        )
                    }
                }

                OutlinedButton(
                    onClick = onFireClick,
                    shape = RoundedCornerShape(AppDimens.radiusPill),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "PHK",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFDC2626)
                    )
                }
            }

            if (assignedBus == null && !hasAvailableBus) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Semua bus sudah ditugaskan ke sopir lain atau belum ada bus di garasi.",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }
    }
}
