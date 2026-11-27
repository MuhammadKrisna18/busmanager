package com.example.busmanager.presentation.route.wizard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmanager.domain.driver.model.Driver
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.presentation.route.wizard.RouteWizardUiState
import com.example.busmanager.ui.theme.AppDimens
import com.example.busmanager.ui.theme.SurfaceWhite
import com.example.busmanager.ui.theme.TextDark
import com.example.busmanager.ui.theme.TextMuted

@Composable
fun Slide5BusDriverAssignment(
    uiState: RouteWizardUiState,
    onSelectBus: (Bus?) -> Unit,
    onSelectDriver: (Driver?) -> Unit,
    modifier: Modifier = Modifier
) {
    val originCity = uiState.selectedOrigin?.city ?: "Asal"
    val destCity = uiState.selectedDestination?.city ?: "Tujuan"

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        item {
            Text(
                text = "1. Pilih Armada Bus",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tugaskan bus yang siap beroperasi untuk trayek $originCity - $destCity.",
                fontSize = 12.sp,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (uiState.availableBuses.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(AppDimens.radiusMedium),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Text(
                        text = "Belum ada armada bus yang menganggur. Kamu bisa beli bus baru di menu Armada atau simpan trayek ini sebagai standby.",
                        fontSize = 12.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    uiState.availableBuses.forEach { bus ->
                        BusAssignmentCard(
                            bus = bus,
                            isSelected = uiState.selectedBus?.id == bus.id,
                            onSelect = { onSelectBus(bus) }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        item {
            Text(
                text = "2. Pilih Pengemudi / Driver",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tugaskan pengemudi berlisensi untuk mengemudikan bus.",
                fontSize = 12.sp,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (uiState.availableDrivers.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(AppDimens.radiusMedium),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Text(
                        text = "Tidak ada pengemudi yang bebas tugas. Kamu bisa rekrut pengemudi di menu SDM.",
                        fontSize = 12.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    uiState.availableDrivers.forEach { driver ->
                        DriverAssignmentCard(
                            driver = driver,
                            isSelected = uiState.selectedDriver?.id == driver.id,
                            onSelect = { onSelectDriver(driver) }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        uiState.dailySchedulePlan?.let { plan ->
            item {
                ScheduleTimelineCard(plan = plan)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
