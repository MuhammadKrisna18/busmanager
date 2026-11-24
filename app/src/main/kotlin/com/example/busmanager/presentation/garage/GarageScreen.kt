package com.example.busmanager.presentation.garage

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.driver.model.Driver
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.RoutePathType
import com.example.busmanager.domain.simulation.model.RouteDirection
import com.example.busmanager.presentation.route.components.EditScheduleDialog
import com.example.busmanager.ui.theme.BackgroundWhite
import com.example.busmanager.ui.theme.CardBorder
import com.example.busmanager.ui.theme.IceBlue
import com.example.busmanager.ui.theme.PrimaryBlue
import com.example.busmanager.ui.theme.PrimaryBlueDark
import com.example.busmanager.ui.theme.SuccessGreen
import com.example.busmanager.ui.theme.SuccessGreenLight
import com.example.busmanager.ui.theme.SurfaceWhite
import com.example.busmanager.ui.theme.TextDark
import com.example.busmanager.ui.theme.TextLight
import com.example.busmanager.ui.theme.TextMuted
import com.example.busmanager.ui.theme.WarningAmber
import com.example.busmanager.ui.theme.WarningAmberLight

@Composable
fun GarageScreen(
    viewModel: GarageViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToShowroom: () -> Unit,
    onNavigateToRoutes: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var busToSell by remember { mutableStateOf<GarageBusItem?>(null) }
    var operatingWarningBus by remember { mutableStateOf<GarageBusItem?>(null) }

    if (operatingWarningBus != null) {
        val bus = operatingWarningBus!!.bus
        AlertDialog(
            onDismissRequest = { operatingWarningBus = null },
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
                TextButton(onClick = { operatingWarningBus = null }) {
                    Text("Mengerti", fontWeight = FontWeight.Bold, color = PrimaryBlue)
                }
            }
        )
    }

    if (busToSell != null) {
        val item = busToSell!!
        val bus = item.bus
        val sellPrice = Money.rupiah((bus.totalPrice.amount * 0.70).toLong().coerceAtLeast(10_000_000L))
        val hasAssignment = bus.hasRoute || bus.hasDriver

        AlertDialog(
            onDismissRequest = { busToSell = null },
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
                    onClick = {
                        val busId = bus.id
                        busToSell = null
                        viewModel.sellBus(busId)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Jual Armada", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { busToSell = null }) {
                    Text("Batal")
                }
            }
        )
    }

    if (uiState.editingScheduleTarget != null) {
        val target = uiState.editingScheduleTarget!!
        EditScheduleDialog(
            busCode = target.bus.busCode,
            routeName = target.routeName,
            initialHeadwayMinutes = target.currentHeadwayMinutes,
            initialTransitDwellMinutes = target.currentTransitDwellMinutes,
            onDismiss = { viewModel.dismissEditSchedule() },
            onSave = { headway, dwell ->
                viewModel.saveSchedule(headway, dwell)
            }
        )
    }

    if (uiState.quickAssignState != null) {
        val assignState = uiState.quickAssignState!!
        QuickAssignDialog(
            state = assignState,
            onDismiss = { viewModel.dismissQuickAssignDialog() },
            onSelectRoute = { viewModel.selectQuickAssignRoute(it) },
            onSelectDriver = { viewModel.selectQuickAssignDriver(it) },
            onSetTime = { h, m -> viewModel.setQuickAssignDepartureTime(h, m) },
            onSetDirection = { viewModel.setQuickAssignDirection(it) },
            onSetHeadway = { viewModel.setQuickAssignHeadway(it) },
            onSetTransitDwell = { viewModel.setQuickAssignTransitDwell(it) },
            onConfirm = { viewModel.confirmQuickAssign() }
        )
    }

    if (uiState.userMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissUserMessage() },
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
                    text = uiState.userMessage!!,
                    fontSize = 13.sp,
                    color = TextDark
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissUserMessage() }) {
                    Text("OK", fontWeight = FontWeight.Bold, color = PrimaryBlue)
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundWhite,
        topBar = {
            GarageTopBar(
                companyName = uiState.companyName,
                totalBusCount = uiState.totalBusCount,
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
        } else if (uiState.allBuses.isEmpty()) {
            EmptyGarageView(
                onNavigateToShowroom = onNavigateToShowroom,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GarageFilter.entries.forEach { filter ->
                        val count = when (filter) {
                            GarageFilter.ALL -> uiState.totalBusCount
                            GarageFilter.OPERATING -> uiState.operatingBusCount
                            GarageFilter.STANDBY -> uiState.standbyBusCount
                        }
                        val isSelected = uiState.selectedFilter == filter

                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setFilter(filter) },
                            label = {
                                Text(
                                    text = "${filter.displayName} ($count)",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = IceBlue,
                                selectedLabelColor = PrimaryBlueDark
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) PrimaryBlue else CardBorder
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (uiState.filteredBuses.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = when (uiState.selectedFilter) {
                                            GarageFilter.OPERATING -> "🚌"
                                            GarageFilter.STANDBY -> "🏢"
                                            GarageFilter.ALL -> "🚍"
                                        },
                                        fontSize = 36.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = when (uiState.selectedFilter) {
                                            GarageFilter.OPERATING -> "Tidak Ada Armada Berdinas"
                                            GarageFilter.STANDBY -> "Tidak Ada Armada di Garasi"
                                            GarageFilter.ALL -> "Belum Ada Armada Bus"
                                        },
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextDark
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = when (uiState.selectedFilter) {
                                            GarageFilter.OPERATING -> "Semua armada saat ini berada di garasi dan belum memiliki penugasan dinas."
                                            GarageFilter.STANDBY -> "Semua armada saat ini telah memiliki penugasan dinas atau sedang beroperasi di jalan raya."
                                            GarageFilter.ALL -> "Silakan beli bus di Showroom untuk memulai armada PO Anda."
                                        },
                                        fontSize = 12.sp,
                                        color = TextMuted,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        items(uiState.filteredBuses) { item ->
                            GarageBusCard(
                                item = item,
                                onQuickAssignClick = { viewModel.openQuickAssignDialog(item) },
                                onAssignRouteClick = onNavigateToRoutes,
                                onEditScheduleClick = { viewModel.openEditSchedule(item) },
                                onSellClick = {
                                    if (item.isOperating) {
                                        operatingWarningBus = item
                                    } else {
                                        busToSell = item
                                    }
                                }
                            )
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun GarageTopBar(
    companyName: String,
    totalBusCount: Int,
    onNavigateBack: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = SurfaceWhite,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.size(36.dp)
            ) {
                Text(
                    text = "←",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Garasi Armada",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    text = "$companyName • $totalBusCount Unit Bus Dimiliki",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
private fun GarageBusCard(
    item: GarageBusItem,
    onQuickAssignClick: () -> Unit,
    onAssignRouteClick: () -> Unit,
    onEditScheduleClick: () -> Unit,
    onSellClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bus = item.bus

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(IceBlue)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = bus.busCode,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlueDark
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = bus.body.busClass.displayName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextMuted
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when {
                                item.isOperating -> SuccessGreenLight
                                item.canOperate -> IceBlue
                                item.bus.hasRoute -> WarningAmberLight
                                else -> Color(0xFFF1F5F9)
                            }
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = item.statusLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            item.isOperating -> SuccessGreen
                            item.canOperate -> PrimaryBlueDark
                            item.bus.hasRoute -> Color(0xFFB45309)
                            else -> TextMuted
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = bus.fullName,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Text(
                text = "${bus.body.passengerCapacity} Kursi Penumpang",
                fontSize = 12.sp,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF8FAFC))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "👨‍✈️", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Sopir Bertugas:", fontSize = 12.sp, color = TextMuted)
                    }
                    if (item.driverName != null) {
                        Text(
                            text = item.driverName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(WarningAmberLight)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "⚠️ Belum Ada Sopir",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFB45309)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🗺️", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Rute Aktif:", fontSize = 12.sp, color = TextMuted)
                    }
                    if (item.routeName != null) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = item.routeName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlueDark
                            )
                            if (item.activeTripDirection != null && item.activeTripProgressPercent != null) {
                                Text(
                                    text = "${item.activeTripDirection} • Progres ${item.activeTripProgressPercent}%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SuccessGreen
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFE2E8F0))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Belum Ada Rute",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextMuted
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
        if (item.routeName == null) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = onQuickAssignClick,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text(
                        text = "⚡ Tugaskan ke Trayek Aktif",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onAssignRouteClick,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "+ Desain Rute Baru",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryBlue
                        )
                    }
                    val sellPrice = Money.rupiah((bus.totalPrice.amount * 0.70).toLong().coerceAtLeast(10_000_000L))
                    OutlinedButton(
                        onClick = onSellClick,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDC2626))
                    ) {
                        Text(
                            text = "💰 Jual (${sellPrice.formatRupiah()})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onEditScheduleClick,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "⚙️ Ubah Jadwal",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryBlue
                    )
                }
                val sellPrice = Money.rupiah((bus.totalPrice.amount * 0.70).toLong().coerceAtLeast(10_000_000L))
                OutlinedButton(
                    onClick = onSellClick,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDC2626))
                ) {
                    Text(
                        text = "💰 Jual (${sellPrice.formatRupiah()})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
            }
        }
    }
}

@Composable
private fun EmptyGarageView(
    onNavigateToShowroom: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "🚌", fontSize = 48.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Belum Memiliki Armada Bus",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Kunjungi Showroom Bus untuk membeli sasis dan bodi bus pertamamu!",
            fontSize = 13.sp,
            color = TextMuted,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(18.dp))
        Button(
            onClick = onNavigateToShowroom,
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            Text("🏪 Kunjungi Showroom")
        }
    }
}

@Composable
private fun QuickAssignDialog(
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
                                            text = String.format(java.util.Locale.US, "%02d:%02d WIB", state.departureHour, state.departureMinute),
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
                                                text = String.format(java.util.Locale.US, "%02d:00", presetHour),
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
