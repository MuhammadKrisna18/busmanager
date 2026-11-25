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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import com.example.busmanager.domain.company.model.CompanyReputation
import com.example.busmanager.domain.company.model.CompanyTier
import com.example.busmanager.domain.route.model.RouteOperationalInfo
import com.example.busmanager.domain.route.model.RoutePathType
import com.example.busmanager.domain.simulation.model.OfflineProgressionResult
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
    onNavigateToRouteInfo: () -> Unit = {},
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

    if (state.offlineProgressionResult != null) {
        OfflineProgressionDialog(
            result = state.offlineProgressionResult!!,
            onDismiss = { viewModel.dismissOfflineProgressionDialog() }
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
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = AppDimens.paddingMedium, vertical = AppDimens.paddingSmall)
        ) {
            CompanyHeader(
                companyName = state.company?.name ?: "PO TransNusantara",
                dateTimeText = state.formattedDateTime,
                balanceFormatted = state.company?.balance?.formatRupiah() ?: "Rp 1.500.000.000",
                reputation = state.reputation,
                tier = state.tier,
                onResetClick = { showResetConfirmDialog = true }
            )

            Spacer(modifier = Modifier.height(AppDimens.paddingMedium))

            val activeRoutes = state.routeOperationalList.filter { it.isActive }
            val activeBusCount = activeRoutes.flatMap { it.assignedBuses }.distinctBy { it.id }.size
            val activeTripsCount = activeRoutes.sumOf { it.activeTripsCount }

            PlaySimulationCard(
                isPlaying = state.isPlaying,
                ownedBusCount = state.ownedBuses.size,
                activeBusCount = activeBusCount,
                activeRouteCount = activeRoutes.size,
                activeTripsCount = activeTripsCount,
                onOpenSimulation = onNavigateToSimulationMap
            )

            Spacer(modifier = Modifier.height(AppDimens.paddingMedium))

            ActiveRoutesSummarySection(
                activeRoutes = state.routeOperationalList.filter { it.isActive },
                onOpenRouteInfo = onNavigateToRouteInfo,
                onNavigateToGarage = onNavigateToGarage
            )

            Spacer(modifier = Modifier.height(AppDimens.paddingSmall))

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
                onOpenRouteInfo = onNavigateToRouteInfo,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CompanyHeader(
    companyName: String,
    dateTimeText: String,
    balanceFormatted: String,
    reputation: CompanyReputation,
    tier: CompanyTier,
    onResetClick: () -> Unit = {}
) {
    val initialLetters = if (companyName.isNotBlank()) companyName.take(2).uppercase() else "PO"

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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(PrimaryBlue, AccentBlue)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initialLetters,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = companyName,
                            color = TextDark,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFEEF2FF))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Lv.${tier.level} • ${tier.title}",
                                    color = Color(0xFF4F46E5),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = dateTimeText,
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEE2E2))
                        .clickable(onClick = onResetClick),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🔄",
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFFFFBEB))
                    .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Text(
                        text = "⭐ ${reputation.formattedStars}",
                        color = Color(0xFFB45309),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )

                    Text(
                        text = reputation.statusTitle,
                        color = Color(0xFF92400E),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFFEF3C7))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "👥 ${reputation.totalBoardedPassengers} pnp",
                        color = Color(0xFF78350F),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AppDimens.radiusSmall))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFECFDF5),
                                Color(0xFFF0FDF4)
                            )
                        )
                    )
                    .border(1.dp, Color(0xFFA7F3D0), RoundedCornerShape(AppDimens.radiusSmall))
                    .padding(horizontal = AppDimens.paddingMedium, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Saldo Kas PO",
                        color = Color(0xFF047857),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = balanceFormatted,
                        color = Color(0xFF065F46),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFD1FAE5)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "💰",
                        fontSize = 20.sp
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
    activeBusCount: Int,
    activeRouteCount: Int,
    activeTripsCount: Int,
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = if (isPlaying) {
                            listOf(Color(0xFFF0FDF4), Color(0xFFEFF6FF))
                        } else {
                            listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9))
                        }
                    )
                )
                .border(
                    width = 1.dp,
                    color = if (isPlaying) Color(0xFF86EFAC) else Color(0xFFE2E8F0),
                    shape = RoundedCornerShape(AppDimens.radiusMedium)
                )
                .padding(AppDimens.paddingMedium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(AppDimens.radiusPill))
                            .background(if (isPlaying) Color(0xFFDCFCE7) else Color(0xFFFEF3C7))
                            .border(
                                1.dp,
                                if (isPlaying) Color(0xFF86EFAC) else Color(0xFFFDE68A),
                                RoundedCornerShape(AppDimens.radiusPill)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isPlaying) Color(0xFF16A34A) else Color(0xFFD97706))
                            )
                            Text(
                                text = if (isPlaying) "RADAR LIVE" else "STANDBY",
                                color = if (isPlaying) Color(0xFF15803D) else Color(0xFFB45309),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Text(
                        text = "Pusat Komando Jalur",
                        color = TextDark,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(AppDimens.radiusPill))
                        .background(Color.White.copy(alpha = 0.85f))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(AppDimens.radiusPill))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "🚌 $activeBusCount/$ownedBusCount Jalan",
                        color = TextDark,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isPlaying && activeTripsCount > 0) {
                            "$activeTripsCount Armada Melaju di Rute"
                        } else if (isPlaying) {
                            "Memantau Jalur Operasi Realtime"
                        } else {
                            "Operasi Armada Sedang Dijeda"
                        },
                        color = if (isPlaying) PrimaryBlueDark else TextDark,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (activeRouteCount > 0) {
                            "$activeRouteCount trayek aktif terhubung ke jaringan terminal"
                        } else {
                            "Belum ada trayek aktif, siapkan armada di garasi"
                        },
                        color = TextMuted,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(PrimaryBlue, AccentBlue)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🗺️",
                        fontSize = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = if (isPlaying) {
                                listOf(PrimaryBlue, AccentBlue)
                            } else {
                                listOf(Color(0xFF2563EB), Color(0xFF1D4ED8))
                            }
                        )
                    )
                    .padding(vertical = 9.dp, horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (isPlaying) "Pantau Radar Operasi" else "Buka Peta Operasi & Radar",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "➔",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
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
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
    ) {
        item {
            MenuCard(
                title = "Garasi Armada",
                subtitle = "Kelola & Servis Unit",
                iconEmoji = "🚌",
                badgeText = "$ownedBusCount Unit",
                containerColor = Color(0xFFEFF6FF),
                borderColor = Color(0xFFBFDBFE),
                badgeBgColor = Color(0xFFDBEAFE),
                badgeTextColor = Color(0xFF1D4ED8),
                onClick = onNavigateToGarage
            )
        }

        item {
            MenuCard(
                title = "Showroom Bus",
                subtitle = "Beli Bus & Sasis Baru",
                iconEmoji = "🏪",
                badgeText = "Beli Unit",
                containerColor = Color(0xFFEEF2FF),
                borderColor = Color(0xFFC7D2FE),
                badgeBgColor = Color(0xFFE0E7FF),
                badgeTextColor = Color(0xFF4338CA),
                onClick = onNavigateToShowroom
            )
        }

        item {
            MenuCard(
                title = "Rute & Trayek",
                subtitle = "Jalur Antar Terminal",
                iconEmoji = "🗺️",
                badgeText = "Peta Jalur",
                containerColor = Color(0xFFECFDF5),
                borderColor = Color(0xFFA7F3D0),
                badgeBgColor = Color(0xFFD1FAE5),
                badgeTextColor = Color(0xFF065F46),
                onClick = onNavigateToRoutes
            )
        }

        item {
            MenuCard(
                title = "Kru & Sopir",
                subtitle = "Rekrutmen & Penugasan",
                iconEmoji = "👨‍✈️",
                badgeText = if (hiredDriverCount > 0) "$hiredDriverCount Sopir" else "Rekrut",
                containerColor = Color(0xFFFFFBEB),
                borderColor = Color(0xFFFDE68A),
                badgeBgColor = Color(0xFFFEF3C7),
                badgeTextColor = Color(0xFF92400E),
                onClick = onNavigateToDrivers
            )
        }

        item {
            MenuCard(
                title = "Keuangan PO",
                subtitle = "Buku Kas & Laba Rugi",
                iconEmoji = "📊",
                badgeText = "Buku Kas",
                containerColor = Color(0xFFFEFCE8),
                borderColor = Color(0xFFFEF08A),
                badgeBgColor = Color(0xFFFEF9C3),
                badgeTextColor = Color(0xFF854D0E),
                onClick = onNavigateToFinance
            )
        }

        item {
            MenuCard(
                title = "Laporan Terminal",
                subtitle = "Arus Penumpang Wilayah",
                iconEmoji = "🏢",
                badgeText = "Data Arus",
                containerColor = Color(0xFFF0FDFA),
                borderColor = Color(0xFFA5F3FC),
                badgeBgColor = Color(0xFFCFFAFE),
                badgeTextColor = Color(0xFF0E7490),
                onClick = onNavigateToTerminalReport
            )
        }

        item {
            MenuCard(
                title = "Informasi Trayek",
                subtitle = "Status Operasi Trayek",
                iconEmoji = "📋",
                badgeText = if (activeRouteCount > 0) "$activeRouteCount Aktif" else "Info Rute",
                containerColor = Color(0xFFFAF5FF),
                borderColor = Color(0xFFE9D5FF),
                badgeBgColor = Color(0xFFF3E8FF),
                badgeTextColor = Color(0xFF6B21A8),
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
    containerColor: Color,
    borderColor: Color,
    badgeBgColor: Color,
    badgeTextColor: Color,
    isActive: Boolean = true,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isActive, onClick = onClick),
        shape = RoundedCornerShape(AppDimens.radiusMedium),
        colors = CardDefaults.cardColors(containerColor = if (isActive) containerColor else DisabledBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isActive) 1.dp else 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isActive) borderColor else CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.85f))
                        .border(1.dp, borderColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = iconEmoji, fontSize = 18.sp)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(AppDimens.radiusPill))
                        .background(if (isActive) badgeBgColor else Color(0xFFE2E8F0))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = if (isActive) badgeTextColor else DisabledGray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                color = if (isActive) TextDark else DisabledGray,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                color = if (isActive) TextMuted else DisabledGray,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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

                PrimaryTabRow(
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
                        item.departureSchedules.forEach { scheduleText ->
                            Text(
                                text = "⏰ $scheduleText",
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }
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

@Composable
private fun OfflineProgressionDialog(
    result: OfflineProgressionResult,
    onDismiss: () -> Unit
) {
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
                            text = "🚌 Operasi Latar Belakang",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            text = "Armada terus berdinas saat aplikasi ditutup",
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

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(IceBlue)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "⏱️ Waktu Offline: ${result.offlineGameHours} Jam Permainan (${result.offlineDurationMinutes} mnt)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlueDark
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = SuccessGreenLight)
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
                                text = "Laba Bersih Masuk Kas:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextDark
                            )
                            Text(
                                text = "+ ${result.netEarnings.formatRupiah()}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SuccessGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Pendapatan Tiket:", fontSize = 11.sp, color = TextMuted)
                            Text(text = "+ ${result.totalRevenue.formatRupiah()}", fontSize = 11.sp, color = TextDark, fontWeight = FontWeight.SemiBold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Bagi Hasil Sopir (30%):", fontSize = 11.sp, color = TextMuted)
                            Text(text = "- ${result.totalDriverWages.formatRupiah()}", fontSize = 11.sp, color = Color(0xFFDC2626), fontWeight = FontWeight.SemiBold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Total Penumpang & Rit:", fontSize = 11.sp, color = TextMuted)
                            Text(text = "${result.totalPassengersCarried} pnp • ${result.totalTripsCompleted} rit", fontSize = 11.sp, color = TextDark, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Armada yang Berdinas (${result.busSummaries.size} Bus):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(result.busSummaries) { summary ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(6.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${summary.busCode} • ${summary.routeName}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextDark
                                    )
                                    Text(
                                        text = "${summary.tripsCompleted} rit • ${summary.passengersCarried} pnp • Posisi: ${summary.currentTerminalName}",
                                        fontSize = 10.sp,
                                        color = TextMuted
                                    )
                                }
                                Text(
                                    text = "+ ${summary.netEarnings.formatRupiah()}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen
                                )
                            }
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
                    Text(text = "Klaim & Lanjutkan", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ActiveRoutesSummarySection(
    activeRoutes: List<RouteOperationalInfo>,
    onOpenRouteInfo: () -> Unit,
    onNavigateToGarage: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDimens.radiusMedium),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = AppDimens.cardElevation),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFD1FAE5)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🛣️", fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Trayek Aktif Beroperasi",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(AppDimens.radiusPill))
                            .background(if (activeRoutes.isNotEmpty()) Color(0xFFDCFCE7) else Color(0xFFF1F5F9))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${activeRoutes.size} Trayek",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (activeRoutes.isNotEmpty()) Color(0xFF15803D) else TextMuted
                        )
                    }
                }

                Text(
                    text = "Lihat Detail ➔",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue,
                    modifier = Modifier.clickable(onClick = onOpenRouteInfo)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (activeRoutes.isEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF8FAFC))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Belum ada trayek aktif beroperasi.",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    Text(
                        text = "Tugaskan di Garasi ➔",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlueDark,
                        modifier = Modifier.clickable(onClick = onNavigateToGarage)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    activeRoutes.take(3).forEach { item ->
                        val corridor = if (item.route.pathType == com.example.busmanager.domain.route.model.RoutePathType.TOLL) "Tol" else "Non-Tol"
                        val busCodes = item.assignedBuses.joinToString { it.busCode }.ifEmpty { "Siap Berdinas" }
                        val schedText = item.departureSchedules.firstOrNull() ?: "Jadwal Reguler"

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF8FAFC))
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                .clickable(onClick = onOpenRouteInfo)
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${item.route.originTerminal.city} ➔ ${item.route.destinationTerminal.city}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextDark
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (item.route.pathType == com.example.busmanager.domain.route.model.RoutePathType.TOLL) PrimaryBlue else Color(0xFF64748B))
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Text(text = corridor, fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Armada: $busCodes • $schedText",
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(AppDimens.radiusPill))
                                    .background(Color(0xFFDCFCE7))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "● Aktif",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF15803D)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
