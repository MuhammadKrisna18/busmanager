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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.checkForUpdates(BuildConfig.VERSION_NAME)
    }

    if (state.updateInfo != null) {
        UpdateDialog(
            updateInfo = state.updateInfo!!,
            downloadProgress = state.downloadProgress,
            onUpdateClick = { viewModel.startUpdateDownload(context, state.updateInfo!!) },
            onDismiss = { viewModel.dismissUpdateDialog() }
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
                gameHour = state.gameHour
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
                onNavigateToShowroom = onNavigateToShowroom,
                onNavigateToGarage = onNavigateToGarage,
                onNavigateToRoutes = onNavigateToRoutes,
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
    gameHour: Int
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
    onNavigateToShowroom: () -> Unit,
    onNavigateToGarage: () -> Unit,
    onNavigateToRoutes: () -> Unit,
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
                badgeText = "Segera Hadir",
                isActive = false
            )
        }

        item {
            MenuCard(
                title = "Keuangan",
                subtitle = "Laba Rugi & Kredit",
                iconEmoji = "📊",
                badgeText = "Segera Hadir",
                isActive = false
            )
        }

        item {
            MenuCard(
                title = "Pengaturan",
                subtitle = "Game & Suara",
                iconEmoji = "⚙️",
                badgeText = "v1.0",
                isActive = true
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
