package com.example.busmanager.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.busmanager.BuildConfig
import com.example.busmanager.presentation.dashboard.components.ActiveRoutesSummarySection
import com.example.busmanager.presentation.dashboard.components.CompanyHeader
import com.example.busmanager.presentation.dashboard.components.EventTickerBanner
import com.example.busmanager.presentation.dashboard.components.MenuGrid
import com.example.busmanager.presentation.dashboard.components.OfflineProgressionDialog
import com.example.busmanager.presentation.dashboard.components.PlaySimulationCard
import com.example.busmanager.presentation.dashboard.components.RouteInfoDialog
import com.example.busmanager.presentation.dashboard.components.SimulationLogCenterDialog
import com.example.busmanager.presentation.update.UpdateDialog
import com.example.busmanager.ui.theme.AppDimens
import com.example.busmanager.ui.theme.BackgroundWhite
import com.example.busmanager.ui.theme.PrimaryBlue
import com.example.busmanager.ui.theme.TextDark
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

    state.updateInfo?.let { updateInfo ->
        UpdateDialog(
            updateInfo = updateInfo,
            downloadProgress = state.downloadProgress,
            onUpdateClick = { viewModel.startUpdateDownload(context, updateInfo) },
            onDismiss = { viewModel.dismissUpdateDialog() }
        )
    }

    if (state.showRouteInfoModal) {
        RouteInfoDialog(
            routeOperationalList = state.routeOperationalList,
            onDismiss = { viewModel.dismissRouteInfoModal() }
        )
    }

    state.offlineProgressionResult?.let { progressionResult ->
        OfflineProgressionDialog(
            result = progressionResult,
            onDismiss = { viewModel.dismissOfflineProgressionDialog() }
        )
    }

    if (state.showLogCenterModal) {
        SimulationLogCenterDialog(
            logs = state.recentLogs,
            onDismiss = { viewModel.dismissLogCenterModal() }
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
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(scrollState)
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

            Spacer(modifier = Modifier.height(AppDimens.paddingSmall))

            EventTickerBanner(
                recentLogs = state.recentLogs,
                onOpenLogCenter = { viewModel.openLogCenterModal() }
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
                activeRoutes = activeRoutes,
                onOpenRouteInfo = onNavigateToRouteInfo,
                onNavigateToGarage = onNavigateToGarage
            )

            Spacer(modifier = Modifier.height(AppDimens.paddingMedium))

            Text(
                text = "Manajemen & Operasional PO",
                color = TextDark,
                fontSize = 16.sp,
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
                onOpenRouteInfo = onNavigateToRouteInfo
            )

            Spacer(modifier = Modifier.height(AppDimens.paddingLarge))
        }
    }
}
