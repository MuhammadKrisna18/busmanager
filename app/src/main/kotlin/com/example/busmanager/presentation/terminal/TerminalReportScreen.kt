package com.example.busmanager.presentation.terminal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.busmanager.domain.route.model.Terminal
import com.example.busmanager.domain.simulation.model.GameCalendar
import com.example.busmanager.domain.terminal.model.TerminalBusBreakdown
import com.example.busmanager.domain.terminal.model.TerminalPassengerRecord
import com.example.busmanager.ui.theme.AppDimens
import com.example.busmanager.ui.theme.BackgroundWhite
import com.example.busmanager.ui.theme.CardBorder
import com.example.busmanager.ui.theme.IceBlue
import com.example.busmanager.ui.theme.PrimaryBlue
import com.example.busmanager.ui.theme.PrimaryBlueDark
import com.example.busmanager.ui.theme.SurfaceWhite
import com.example.busmanager.ui.theme.TextDark
import com.example.busmanager.ui.theme.TextMuted

@Composable
fun TerminalReportScreen(
    viewModel: TerminalReportViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = BackgroundWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(AppDimens.paddingMedium)
        ) {
            TopHeaderBar(
                title = "Laporan Terminal",
                subtitle = "Arus Penumpang Naik & Turun",
                onNavigateBack = onNavigateBack
            )

            Spacer(modifier = Modifier.height(AppDimens.paddingMedium))

            TerminalSelectorBar(
                terminals = state.availableTerminals,
                selectedTerminal = state.selectedTerminal,
                onSelectTerminal = { viewModel.selectTerminal(it) }
            )

            Spacer(modifier = Modifier.height(AppDimens.paddingSmall))

            DateFilterBar(
                dateFormatted = state.selectedDateFormatted,
                isHistoricalView = state.isHistoricalView,
                onOpenHistory = { viewModel.openHistoryModal() },
                onResetToLive = { viewModel.resetToCurrentLiveDate() }
            )

            Spacer(modifier = Modifier.height(AppDimens.paddingMedium))

            SummaryMetricsRow(
                totalBoarded = state.totalBoarded,
                totalAlighted = state.totalAlighted
            )

            Spacer(modifier = Modifier.height(AppDimens.paddingSmall))

            Button(
                onClick = { viewModel.openBreakdownModal() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(AppDimens.radiusMedium),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Text(
                    text = "🔍 Rincian Naik/Turun per Armada (${state.busBreakdowns.size} Bus)",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(AppDimens.paddingMedium))

            Text(
                text = "Riwayat Arus Penumpang Terminal",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            Spacer(modifier = Modifier.height(AppDimens.paddingSmall))

            if (!state.hasData) {
                EmptyStateCard(terminalName = state.selectedTerminal?.name ?: "Terminal")
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(AppDimens.paddingSmall)
                ) {
                    items(state.records) { record ->
                        TerminalRecordItem(record = record)
                    }
                }
            }
        }
    }

    if (state.showBreakdownModal) {
        BusBreakdownDialog(
            terminalName = state.selectedTerminal?.name ?: "Terminal",
            dateFormatted = state.selectedDateFormatted,
            breakdowns = state.busBreakdowns,
            onDismiss = { viewModel.dismissBreakdownModal() }
        )
    }

    if (state.showHistoryModal) {
        TerminalHistoryDialog(
            availableDates = state.availableHistoryDates,
            onSelectDate = { day, month, year -> viewModel.selectHistoryDate(day, month, year) },
            onDismiss = { viewModel.dismissHistoryModal() }
        )
    }
}

@Composable
private fun TopHeaderBar(
    title: String,
    subtitle: String,
    onNavigateBack: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(IceBlue)
                .clickable(onClick = onNavigateBack),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "◀",
                color = PrimaryBlueDark,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(AppDimens.paddingMedium))

        Column {
            Text(
                text = title,
                color = TextDark,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = TextMuted,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun TerminalSelectorBar(
    terminals: List<Terminal>,
    selectedTerminal: Terminal?,
    onSelectTerminal: (Terminal) -> Unit
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        terminals.forEach { terminal ->
            val isSelected = terminal.id == selectedTerminal?.id
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(AppDimens.radiusPill))
                    .background(if (isSelected) PrimaryBlue else SurfaceWhite)
                    .border(
                        1.dp,
                        if (isSelected) PrimaryBlue else CardBorder,
                        RoundedCornerShape(AppDimens.radiusPill)
                    )
                    .clickable { onSelectTerminal(terminal) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = terminal.city,
                    color = if (isSelected) Color.White else TextDark,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun DateFilterBar(
    dateFormatted: String,
    isHistoricalView: Boolean,
    onOpenHistory: () -> Unit,
    onResetToLive: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDimens.radiusMedium),
        colors = CardDefaults.cardColors(
            containerColor = if (isHistoricalView) Color(0xFFFEF3C7) else SurfaceWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "📅 $dateFormatted",
                    color = TextDark,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isHistoricalView) "📜 Menampilkan Riwayat Lalu" else "🔒 Tanggal Simulasi Berjalan (Terkunci)",
                    color = if (isHistoricalView) Color(0xFFB45309) else PrimaryBlueDark,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            if (isHistoricalView) {
                Button(
                    onClick = onResetToLive,
                    shape = RoundedCornerShape(AppDimens.radiusPill),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(text = "Kembali Hari Ini", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            } else {
                OutlinedButton(
                    onClick = onOpenHistory,
                    shape = RoundedCornerShape(AppDimens.radiusPill),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(text = "📜 Riwayat Lalu", fontSize = 11.sp, color = PrimaryBlue, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun TerminalHistoryDialog(
    availableDates: List<Triple<Int, Int, Int>>,
    onSelectDate: (Int, Int, Int) -> Unit,
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
                    Text(
                        text = "📜 Riwayat Laporan",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
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

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Pilih tanggal catatan arus penumpang sebelumnya:",
                    fontSize = 12.sp,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (availableDates.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Belum ada catatan riwayat tanggal sebelumnya.",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(availableDates) { (day, month, year) ->
                            val cal = GameCalendar(year = year, month = month, day = day, hour = 0, minute = 0)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectDate(day, month, year) },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = IceBlue)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${cal.dayOfWeekName()}, ${cal.formatDate()}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryBlueDark
                                    )
                                    Text(
                                        text = "Lihat Data ➔",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = PrimaryBlue
                                    )
                                }
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
                    Text(text = "Tutup", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SummaryMetricsRow(
    totalBoarded: Int,
    totalAlighted: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppDimens.paddingSmall)
    ) {
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(AppDimens.radiusMedium),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = "🟢 Total Naik", color = Color(0xFF047857), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "$totalBoarded", color = Color(0xFF065F46), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                Text(text = "Penumpang", color = Color(0xFF047857), fontSize = 11.sp)
            }
        }

        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(AppDimens.radiusMedium),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = "🔴 Total Turun", color = Color(0xFFB91C1C), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "$totalAlighted", color = Color(0xFF991B1B), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                Text(text = "Penumpang", color = Color(0xFFB91C1C), fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun TerminalRecordItem(record: TerminalPassengerRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDimens.radiusMedium),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(AppDimens.radiusPill))
                            .background(PrimaryBlueDark)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(text = record.busCode, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = record.timeFormatted, color = TextMuted, fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = record.routeName, color = TextDark, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (record.boardedCount > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(AppDimens.radiusPill))
                            .background(Color(0xFFD1FAE5))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = "+${record.boardedCount} Naik", color = Color(0xFF065F46), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                if (record.alightedCount > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(AppDimens.radiusPill))
                            .background(Color(0xFFFEE2E2))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = "-${record.alightedCount} Turun", color = Color(0xFF991B1B), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyStateCard(terminalName: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDimens.radiusMedium),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "📭", fontSize = 32.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Belum Ada Aktivitas di $terminalName", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Text(text = "Armada yang singgah di terminal ini pada tanggal terpilih akan tercatat otomatis di sini.", fontSize = 12.sp, color = TextMuted)
        }
    }
}

@Composable
private fun BusBreakdownDialog(
    terminalName: String,
    dateFormatted: String,
    breakdowns: List<TerminalBusBreakdown>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(AppDimens.radiusLarge),
            color = SurfaceWhite
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Rincian per Armada", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Text(text = "$terminalName • $dateFormatted", fontSize = 11.sp, color = TextMuted)
                    }
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9))
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "✕", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    }
                }

                Spacer(modifier = Modifier.height(AppDimens.paddingMedium))

                if (breakdowns.isEmpty()) {
                    Text(
                        text = "Tidak ada rincian armada pada tanggal ini.",
                        fontSize = 12.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().height(260.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(breakdowns) { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(AppDimens.radiusMedium),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "🚌 ${item.busCode}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextDark)
                                        Text(text = "${item.tripCount}x Singgah", fontSize = 11.sp, color = PrimaryBlue, fontWeight = FontWeight.SemiBold)
                                    }
                                    Text(text = item.routeName, fontSize = 11.sp, color = TextMuted)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                        Text(text = "🟢 Naik: ${item.totalBoarded} pnp", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF065F46))
                                        Text(text = "🔴 Turun: ${item.totalAlighted} pnp", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF991B1B))
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(AppDimens.paddingMedium))

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
