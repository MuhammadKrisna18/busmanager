package com.example.busmanager.presentation.garage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import com.example.busmanager.domain.company.model.Money
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
                                onAssignRouteClick = onNavigateToRoutes,
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
    onAssignRouteClick: () -> Unit,
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
                    OutlinedButton(
                        onClick = onAssignRouteClick,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "+ Trayek Baru",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryBlue
                        )
                    }
                }
                val sellPrice = Money.rupiah((bus.totalPrice.amount * 0.70).toLong().coerceAtLeast(10_000_000L))
                OutlinedButton(
                    onClick = onSellClick,
                    shape = RoundedCornerShape(12.dp),
                    modifier = if (item.routeName == null) Modifier.weight(1f) else Modifier.fillMaxWidth(),
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
