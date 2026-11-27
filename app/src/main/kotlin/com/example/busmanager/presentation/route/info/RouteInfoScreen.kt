package com.example.busmanager.presentation.route.info

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmanager.presentation.route.info.components.DeleteRouteConfirmationDialog
import com.example.busmanager.presentation.route.info.components.EditScheduleModal
import com.example.busmanager.presentation.route.info.components.EmptyRouteInfoView
import com.example.busmanager.presentation.route.info.components.RouteDetailCard
import com.example.busmanager.presentation.route.info.components.RouteFilterRow
import com.example.busmanager.presentation.route.info.components.RouteInfoErrorDialog
import com.example.busmanager.presentation.route.info.components.RouteInfoTopBar
import com.example.busmanager.ui.theme.AppDimens
import com.example.busmanager.ui.theme.BackgroundWhite
import com.example.busmanager.ui.theme.CardBorder
import com.example.busmanager.ui.theme.IceBlue
import com.example.busmanager.ui.theme.PrimaryBlue
import com.example.busmanager.ui.theme.PrimaryBlueDark
import com.example.busmanager.ui.theme.SuccessGreen
import com.example.busmanager.ui.theme.SuccessGreenLight
import com.example.busmanager.ui.theme.SurfaceWhite
import com.example.busmanager.ui.theme.TextDark

@Composable
fun RouteInfoScreen(
    viewModel: RouteInfoViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToNewRoute: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            RouteInfoTopBar(
                onNavigateBack = onNavigateBack,
                onNavigateToNewRoute = onNavigateToNewRoute
            )
        },
        containerColor = BackgroundWhite,
        modifier = modifier
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
        } else if (uiState.allRoutes.isEmpty()) {
            EmptyRouteInfoView(
                onNavigateToNewRoute = onNavigateToNewRoute,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = AppDimens.paddingMedium),
                contentPadding = PaddingValues(top = AppDimens.paddingSmall, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(AppDimens.paddingSmall)
            ) {
                uiState.message?.let { msg ->
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.clearMessage() },
                            shape = RoundedCornerShape(AppDimens.radiusSmall),
                            colors = CardDefaults.cardColors(containerColor = SuccessGreenLight)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = msg,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = SuccessGreen
                                )
                                Text(
                                    text = "Tutup",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen
                                )
                            }
                        }
                    }
                }

                item {
                    RouteFilterRow(
                        selectedFilter = uiState.selectedFilter,
                        totalCount = uiState.totalRouteCount,
                        operatingCount = uiState.operatingRouteCount,
                        standbyCount = uiState.standbyRouteCount,
                        onSelectFilter = { viewModel.setFilter(it) }
                    )
                }

                if (uiState.filteredRoutes.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            shape = RoundedCornerShape(AppDimens.radiusMedium),
                            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                            border = BorderStroke(1.dp, CardBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(AppDimens.radiusPill))
                                        .background(IceBlue)
                                        .padding(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "KOSONG",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryBlueDark
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tidak Ada Trayek Sesuai Filter",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                            }
                        }
                    }
                } else {
                    items(
                        items = uiState.filteredRoutes,
                        key = { it.operationalInfo.route.id }
                    ) { item ->
                        RouteDetailCard(
                            item = item,
                            onTogglePricings = { viewModel.togglePricingsExpanded(item.operationalInfo.route.id) },
                            onEditScheduleClick = { viewModel.openEditScheduleDialog(it) },
                            onDeleteRouteClick = { viewModel.requestDeleteRoute(it) }
                        )
                    }
                }
            }

            uiState.routeToDelete?.let { route ->
                DeleteRouteConfirmationDialog(
                    route = route,
                    onDismiss = { viewModel.dismissDeleteRouteDialog() },
                    onConfirm = { viewModel.confirmDeleteRoute(route.id) }
                )
            }

            uiState.errorMessage?.let { error ->
                RouteInfoErrorDialog(
                    errorMessage = error,
                    onDismiss = { viewModel.clearErrorMessage() }
                )
            }

            uiState.selectedRouteForScheduleEdit?.let { route ->
                EditScheduleModal(
                    route = route,
                    schedules = uiState.activeSchedulesForEditing,
                    ownedBuses = uiState.ownedBuses,
                    onDismiss = { viewModel.closeEditScheduleDialog() },
                    onAddSchedule = { hour, minute, direction, headway, busId ->
                        viewModel.addRouteSchedule(route.id, hour, minute, direction, headway, busId)
                    },
                    onDeleteSchedule = { schedule ->
                        viewModel.deleteRouteSchedule(schedule)
                    }
                )
            }
        }
    }
}
