package com.example.busmanager.presentation.driver

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.busmanager.presentation.common.GameFeedbackToast
import com.example.busmanager.presentation.driver.components.AssignBusDialog
import com.example.busmanager.presentation.driver.components.CandidateDriverList
import com.example.busmanager.presentation.driver.components.DriverHeader
import com.example.busmanager.presentation.driver.components.DriverMetricsSummary
import com.example.busmanager.presentation.driver.components.DriverTabSwitcher
import com.example.busmanager.presentation.driver.components.FireDriverDialog
import com.example.busmanager.presentation.driver.components.HiredDriverList
import com.example.busmanager.presentation.driver.components.MessageBanner
import com.example.busmanager.presentation.driver.components.UnassignDriverDialog
import com.example.busmanager.ui.theme.AppDimens
import com.example.busmanager.ui.theme.BackgroundWhite
import com.example.busmanager.ui.theme.PrimaryBlue

@Composable
fun DriverManagementScreen(
    viewModel: DriverManagementViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = BackgroundWhite
    ) {
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
            return@Surface
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(AppDimens.paddingMedium)
        ) {
            DriverHeader(
                balanceFormatted = state.companyBalance.formatRupiah(),
                onNavigateBack = onNavigateBack
            )

            Spacer(modifier = Modifier.height(AppDimens.paddingMedium))

            DriverMetricsSummary(
                totalHired = state.hiredDrivers.size,
                totalAssigned = state.assignedDriverCount,
                totalDailyWageFormatted = state.totalDailyWage.formatRupiah()
            )

            Spacer(modifier = Modifier.height(AppDimens.paddingMedium))

            state.message?.let { msg ->
                MessageBanner(
                    message = msg,
                    onDismiss = { viewModel.clearMessage() }
                )
                Spacer(modifier = Modifier.height(AppDimens.paddingSmall))
            }

            DriverTabSwitcher(
                selectedTab = state.selectedTab,
                hiredCount = state.hiredDrivers.size,
                candidateCount = state.candidateDrivers.size,
                onTabSelected = { viewModel.selectTab(it) }
            )

            Spacer(modifier = Modifier.height(AppDimens.paddingMedium))

            when (state.selectedTab) {
                DriverTab.HIRED -> {
                    HiredDriverList(
                        drivers = state.hiredDrivers,
                        buses = state.ownedBuses,
                        onAssignClick = { viewModel.openAssignDialog(it) },
                        onUnassignClick = { driverId ->
                            state.hiredDrivers.find { it.id == driverId }?.let {
                                viewModel.openUnassignConfirmation(it)
                            } ?: viewModel.unassignDriver(driverId)
                        },
                        onFireClick = { viewModel.openFireConfirmation(it) },
                        modifier = Modifier.weight(1f)
                    )
                }
                DriverTab.CANDIDATES -> {
                    CandidateDriverList(
                        candidates = state.candidateDrivers,
                        onHireClick = { viewModel.hireDriver(it) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        state.selectedDriverForAssignment?.let { driver ->
            AssignBusDialog(
                driver = driver,
                buses = state.ownedBuses,
                drivers = state.hiredDrivers,
                onDismiss = { viewModel.closeAssignDialog() },
                onAssignConfirm = { busId ->
                    viewModel.assignDriverToBus(driver.id, busId)
                }
            )
        }

        state.driverToUnassign?.let { unassignDriver ->
            val assignedBus = state.ownedBuses.find { it.id == unassignDriver.assignedBusId }
            UnassignDriverDialog(
                driver = unassignDriver,
                assignedBus = assignedBus,
                onDismiss = { viewModel.closeUnassignConfirmation() },
                onConfirm = { viewModel.confirmUnassignDriver() }
            )
        }

        state.driverToFire?.let { fireDriver ->
            FireDriverDialog(
                driver = fireDriver,
                onDismiss = { viewModel.closeFireConfirmation() },
                onConfirm = { viewModel.confirmFireDriver() }
            )
        }

        GameFeedbackToast(
            data = state.feedbackToast,
            onDismiss = { viewModel.dismissFeedbackToast() }
        )
    }
}
