package com.example.busmanager.presentation.garage

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmanager.presentation.common.GameFeedbackToast
import com.example.busmanager.presentation.garage.components.EmptyGarageView
import com.example.busmanager.presentation.garage.components.GarageBusCard
import com.example.busmanager.presentation.garage.components.GarageTopBar
import com.example.busmanager.presentation.garage.components.GarageUserMessageDialog
import com.example.busmanager.presentation.garage.components.OperatingWarningDialog
import com.example.busmanager.presentation.garage.components.QuickAssignDialog
import com.example.busmanager.presentation.garage.components.SellBusDialog
import com.example.busmanager.presentation.route.components.EditScheduleDialog
import com.example.busmanager.ui.theme.BackgroundWhite
import com.example.busmanager.ui.theme.CardBorder
import com.example.busmanager.ui.theme.IceBlue
import com.example.busmanager.ui.theme.PrimaryBlue
import com.example.busmanager.ui.theme.PrimaryBlueDark
import com.example.busmanager.ui.theme.SurfaceWhite
import com.example.busmanager.ui.theme.TextDark
import com.example.busmanager.ui.theme.TextMuted

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

    operatingWarningBus?.let { item ->
        OperatingWarningDialog(
            bus = item.bus,
            onDismiss = { operatingWarningBus = null }
        )
    }

    busToSell?.let { item ->
        SellBusDialog(
            item = item,
            onDismiss = { busToSell = null },
            onConfirmSell = { busId ->
                busToSell = null
                viewModel.sellBus(busId)
            }
        )
    }

    uiState.editingScheduleTarget?.let { target ->
        EditScheduleDialog(
            busCode = target.bus.busCode,
            routeName = target.routeName,
            initialHeadwayMinutes = target.currentHeadwayMinutes,
            onDismiss = { viewModel.dismissEditSchedule() },
            onSave = { headway ->
                viewModel.saveSchedule(headway)
            }
        )
    }

    uiState.quickAssignState?.let { assignState ->
        QuickAssignDialog(
            state = assignState,
            onDismiss = { viewModel.dismissQuickAssignDialog() },
            onSelectRoute = { viewModel.selectQuickAssignRoute(it) },
            onSelectDriver = { viewModel.selectQuickAssignDriver(it) },
            onSetDirection = { viewModel.setQuickAssignDirection(it) },
            onSelectSlot = { viewModel.selectQuickAssignSlot(it) },
            onSetTimeFilter = { viewModel.setQuickAssignTimeFilter(it) },
            onConfirm = { viewModel.confirmQuickAssign() }
        )
    }

    uiState.userMessage?.let { message ->
        GarageUserMessageDialog(
            message = message,
            onDismiss = { viewModel.dismissUserMessage() }
        )
    }

    GameFeedbackToast(
        data = uiState.feedbackToast,
        onDismiss = { viewModel.dismissFeedbackToast() }
    )

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
                                border = BorderStroke(1.dp, CardBorder)
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
                        items(
                            items = uiState.filteredBuses,
                            key = { it.bus.id }
                        ) { item ->
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
