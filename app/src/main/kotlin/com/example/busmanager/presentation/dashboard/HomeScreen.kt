package com.example.busmanager.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.window.Dialog
import com.example.busmanager.domain.route.model.RouteOperationalInfo
import com.example.busmanager.domain.route.model.RoutePathType
import com.example.busmanager.ui.theme.SuccessGreenLight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmanager.BuildConfig
import com.example.busmanager.presentation.update.UpdateDialog
import com.example.busmanager.ui.theme.AccentBlue
import com.example.busmanager.ui.theme.AppDimens
import com.example.busmanager.ui.theme.BackgroundWhite
import com.example.busmanager.ui.theme.CardBorder
import com.example.busmanager.ui.theme.DisabledBackground
import com.example.busmanager.ui.theme.DisabledGray
import com.example.busmanager.ui.theme.IceBlue
import com.example.busmanager.ui.theme.PrimaryBlue
import com.example.busmanager.ui.theme.PrimaryBlueDark
import com.example.busmanager.ui.theme.SkyBlue
import com.example.busmanager.ui.theme.SuccessGreen
import com.example.busmanager.ui.theme.SurfaceWhite
import com.example.busmanager.ui.theme.TextDark
import com.example.busmanager.ui.theme.TextLight
import com.example.busmanager.ui.theme.TextMuted

@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModel,
    onNavigateToSimulationMap: () -> Unit = {},
    onNavigateToShowroom: () -> Unit = {},
    onNavigateToGarage: () -> Unit = {},
    onNavigateToRoutes: () -> Unit = {},
    onNavigateToDrivers: () -> Unit = {},
    onNavigateToFinance: () -> Unit = {},
    onNavigateToTerminalReport: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.checkForUpdates(BuildConfig.VERSION_NAME)
    }

    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = {
                Text(
                    text = "Konfirmasi Reset Akun / Game",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextDark
                )
            },
            text = {
                Text(
                    text = "Apakah Anda yakin ingin mereset seluruh data permainan?\n\nSeluruh armada bus, rute trayek, sopir, dan catatan keuangan akan dihapus secara permanen, serta saldo kas akan dikembalikan ke modal awal Rp 50.000.000.000.",
                    fontSize = 13.sp,
                    color = TextMuted
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResetConfirmDialog = false
                        viewModel.resetGame()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Reset Sekarang", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    if (state.updateInfo != null) {
        UpdateDialog(
            updateInfo = state.updateInfo!!,
            downloadProgress = state.downloadProgress,
            onUpdateClick = { viewModel.startUpdateDownload(context, state.updateInfo!!) },
            onDismiss = { viewModel.dismissUpdateDialog() }
        )
    }

    if (state.showRouteInfoModal) {
        RouteInfoDialog(
            routeOperationalList = state.routeOperationalList,
            onDismiss = { viewModel.dismissRouteInfoModal() }
        )
    }

    if (state.isLoading) {
        Box(
            modifier = modifier.fillMaxSize().background(BackgroundWhite),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = PrimaryBlue)
        }
        return
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = BackgroundWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppDimens.paddingMedium)
        ) {
            CompanyHeader(
                companyName = state.company?.name ?: "PO TransNusantara",
                reputation = state.company?.reputation ?: 100,
                balanceFormatted = state.company?.balance?.formatRupiah() ?: "Rp 1.500.000.000",
                gameDay = state.gameDay,
                gameHour = state.gameHour,
                onResetClick = { showResetConfirmDialog = true }
            )

            Spacer(modifier = Modifier.height(AppDimens.paddingMedium))

            PlaySimulationCard(
                isPlaying = state.isPlaying,
                ownedBusCount = state.ownedBuses.size,
                onOpenSimulation = onNavigateToSimulationMap
            )

            Spacer(modifier = Modifier.height(AppDimens.paddingMedium))

            Text(
                text = "Menu Manajemen",
                color = TextDark,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = AppDimens.paddingSmall)
            )

            MenuGrid(
                ownedBusCount = state.ownedBuses.size,
                hiredDriverCount = state.hiredDriverCount,
                activeRouteCount = state.routeOperationalList.count { it.isActive },
                onNavigateToShowroom = onNavigateToShowroom,
                onNavigateToGarage = onNavigateToGarage,
                onNavigateToRoutes = onNavigateToRoutes,
                onNavigateToDrivers = onNavigateToDrivers,
                onNavigateToFinance = onNavigateToFinance,
                onNavigateToTerminalReport = onNavigateToTerminalReport,
                onOpenRouteInfo = { viewModel.openRouteInfoModal() },
                modifier = Modifier.weight(1f)
            )

            LiveInfoBanner()
        }
    }
}

@Composable
private fun CompanyHeader(
    companyName: String,
    reputation: Int,
    balanceFormatted: String,
    gameDay: Int,
    gameHour: Int,
    onResetClick: () -> Unit = {}
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
                Column {
                    Text(
                        text = companyName,
                        color = TextDark,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Hari ke-$gameDay • ${String.format("%02d:00 WIB", gameHour)}",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(AppDimens.radiusPill))
                            .background(AccentBlue)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "⭐ $reputation Reputasi",
                            color = PrimaryBlueDark,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFEE2E2))
                            .clickable(onClick = onResetClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🔄",
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppDimens.paddingMedium))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AppDimens.radiusSmall))
                    .background(IceBlue)
                    .padding(horizontal = AppDimens.paddingMedium, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Saldo Kas Perusahaan",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = balanceFormatted,
                        color = PrimaryBlueDark,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SuccessGreen.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "💰",
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaySimulationCard(
    isPlaying: Boolean,
    ownedBusCount: Int,
    onOpenSimulation: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenSimulation),
        shape = RoundedCornerShape(AppDimens.radiusMedium),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = AppDimens.cardElevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.paddingMedium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Status Operasi PO",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (isPlaying) "Simulasi Berjalan" else "Simulasi Dijeda",
                    color = if (isPlaying) SuccessGreen else TextDark,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$ownedBusCount unit bus terdaftar di armada",
                    color = TextLight,
                    fontSize = 12.sp
                )
            }

            Button(
                onClick = onOpenSimulation,
                shape = RoundedCornerShape(AppDimens.radiusPill),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPlaying) Color(0xFFDC2626) else PrimaryBlue
                ),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "▶️ PLAY",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun MenuGrid(
    ownedBusCount: Int,
    hiredDriverCount: Int,
    activeRouteCount: Int,
    onNavigateToShowroom: () -> Unit,
    onNavigateToGarage: () -> Unit,
    onNavigateToRoutes: () -> Unit,
    onNavigateToDrivers: () -> Unit,
    onNavigateToFinance: () -> Unit,
    onNavigateToTerminalReport: () -> Unit,
    onOpenRouteInfo: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(AppDimens.paddingMedium),
        verticalArrangement = Arrangement.spacedBy(AppDimens.paddingMedium),
        modifier = modifier
    ) {
        item {
            MenuCard(
                title = "Showroom Bus",
                subtitle = "Beli Bus Jadi",
                iconEmoji = "🏪",
                badgeText = "Ready Stock",
                isActive = true,
                onClick = onNavigateToShowroom
            )
        }

        item {
            MenuCard(
                title = "Garasi Armada",
                subtitle = "Periksa & Servis",
                iconEmoji = "🚌",
                badgeText = "$ownedBusCount Unit",
                isActive = true,
                onClick = onNavigateToGarage
            )
        }

        item {
            MenuCard(
                title = "Rute & Trayek",
                subtitle = "Jalur Antar Kota",
                iconEmoji = "🗺️",
                badgeText = "Peta Jatim",
                isActive = true,
                onClick = onNavigateToRoutes
            )
        }

        item {
            MenuCard(
                title = "Kru & Supir",
                subtitle = "Rekrutmen & Gaji",
                iconEmoji = "👨‍✈️",
                badgeText = if (hiredDriverCount > 0) "$hiredDriverCount Sopir" else "Rekrut",
                isActive = true,
                onClick = onNavigateToDrivers
            )
        }

        item {
            MenuCard(
                title = "Keuangan",
                subtitle = "Buku Kas & Laba Rugi",
                iconEmoji = "📊",
                badgeText = "Buku Kas",
                isActive = true,
                onClick = onNavigateToFinance
            )
        }

        item {
            MenuCard(
                title = "Laporan Terminal",
                subtitle = "Arus Penumpang Naik/Turun",
                iconEmoji = "🏢",
                badgeText = "Harian",
                isActive = true,
                onClick = onNavigateToTerminalReport
            )
        }

        item {
            MenuCard(
                title = "Informasi Trayek",
                subtitle = "Status Trayek Aktif & Terbuka",
                iconEmoji = "📋",
                badgeText = if (activeRouteCount > 0) "$activeRouteCount Aktif" else "Info Rute",
                isActive = true,
                onClick = onOpenRouteInfo
            )
        }
    }
}

@Composable
private fun MenuCard(
    title: String,
    subtitle: String,
    iconEmoji: String,
    badgeText: String,
    isActive: Boolean,
    onClick: () -> Unit = {}
) {
    val containerBg = if (isActive) SurfaceWhite else DisabledBackground
    val titleColor = if (isActive) TextDark else DisabledGray
    val subtitleColor = if (isActive) TextMuted else DisabledGray

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clickable(enabled = isActive, onClick = onClick),
        shape = RoundedCornerShape(AppDimens.radiusMedium),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isActive) AppDimens.cardElevation else 0.dp),
        border = if (isActive) null else androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppDimens.paddingMedium),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(text = iconEmoji, fontSize = 28.sp)

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(AppDimens.radiusPill))
                        .background(if (isActive) AccentBlue else Color(0xFFE2E8F0))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = if (isActive) PrimaryBlueDark else DisabledGray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column {
                Text(
                    text = title,
                    color = titleColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = subtitleColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun LiveInfoBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppDimens.radiusSmall))
            .background(AccentBlue)
            .padding(horizontal = AppDimens.paddingMedium, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "📢", fontSize = 14.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Selamat datang di TransNusantara! Beli armada pertamamu di Showroom Bus.",
                color = PrimaryBlueDark,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun RouteInfoDialog(
    routeOperationalList: List<RouteOperationalInfo>,
    onDismiss: () -> Unit
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val activeRoutes = routeOperationalList.filter { it.isActive }
    val inactiveRoutes = routeOperationalList.filter { !it.isActive }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(AppDimens.radiusMedium),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
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
                    Column {
                        Text(
                            text = "📋 Informasi Trayek",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            text = "Status Trayek Aktif & Terbuka PO",
                            fontSize = 11.sp,
                            color = TextMuted
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

                Spacer(modifier = Modifier.height(12.dp))

                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = IceBlue,
                    contentColor = PrimaryBlueDark
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = {
                            Text(
                                text = "Trayek Aktif (${activeRoutes.size})",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = {
                            Text(
                                text = "Terbuka (${inactiveRoutes.size})",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                val currentList = if (selectedTabIndex == 0) activeRoutes else inactiveRoutes

                if (currentList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (selectedTabIndex == 0) {
                                "Belum ada trayek aktif.\nTrayek aktif adalah rute yang telah memiliki armada bus dan pengemudi yang melayani rute."
                            } else {
                                "Tidak ada trayek terbuka tanpa armada.\nSeluruh trayek yang dibuka telah memiliki armada dan sopir bertugas."
                            },
                            color = TextMuted,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(currentList) { item ->
                            RouteOperationalItemCard(item = item)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(AppDimens.radiusMedium),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text(text = "Tutup", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun RouteOperationalItemCard(item: RouteOperationalInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = if (item.isActive) SurfaceWhite else Color(0xFFF8FAFC)),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (item.isActive) PrimaryBlue else CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${item.route.originTerminal.city} ➔ ${item.route.destinationTerminal.city}",
                    color = TextDark,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(AppDimens.radiusPill))
                        .background(if (item.isActive) SuccessGreenLight else Color(0xFFFEF3C7))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (item.isActive) "● Aktif" else "⚠️ Belum Aktif",
                        color = if (item.isActive) SuccessGreen else Color(0xFFB45309),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = item.route.name,
                color = TextMuted,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "📏 ${item.route.totalDistanceKm.toInt()} km • ⏱️ ${item.route.totalEstimatedDurationMinutes} mnt • ${if (item.route.pathType == RoutePathType.TOLL) "Tol" else "Non-Tol"}",
                color = PrimaryBlueDark,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )

            if (item.isActive) {
                Spacer(modifier = Modifier.height(6.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(IceBlue)
                        .padding(6.dp)
                ) {
                    item.assignedBuses.forEach { bus ->
                        val driver = item.assignedDrivers.find { it.id == bus.driverId || it.assignedBusId == bus.id }
                        Text(
                            text = "🚌 ${bus.busCode} (${bus.body.modelName}) • 👨‍✈️ ${driver?.name ?: "Sopir Ditugaskan"}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlueDark
                        )
                    }
                    if (item.departureSchedules.isNotEmpty()) {
                        Text(
                            text = "⏰ Jadwal: ${item.departureSchedules.joinToString(" • ")}",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Rute sudah dibuka. Silakan tugaskan bus & sopir melalui wizard rute baru atau garasi.",
                    color = Color(0xFFB45309),
                    fontSize = 10.sp
                )
            }
        }
    }
}
