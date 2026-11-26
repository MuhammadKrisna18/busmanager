package com.example.busmanager.presentation.route.wizard

import androidx.compose.animation.AnimatedContent
import com.example.busmanager.presentation.common.AppTransitions
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmanager.presentation.route.wizard.components.RouteConfirmationDialog
import com.example.busmanager.presentation.route.wizard.components.Slide1OriginDestination
import com.example.busmanager.presentation.route.wizard.components.Slide2PathAndStops
import com.example.busmanager.presentation.route.wizard.components.Slide3PricingMatrix
import com.example.busmanager.presentation.route.wizard.components.Slide4BusDriverSchedule
import com.example.busmanager.presentation.route.wizard.components.WizardBottomBar
import com.example.busmanager.presentation.route.wizard.components.WizardStepperHeader
import com.example.busmanager.ui.theme.BackgroundWhite
import com.example.busmanager.ui.theme.PrimaryBlue
import com.example.busmanager.ui.theme.TextDark
import com.example.busmanager.ui.theme.WarningAmber

@Composable
fun RouteWizardScreen(
    viewModel: RouteWizardViewModel,
    onNavigateBack: () -> Unit,
    onRouteLaunched: (routeId: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        val error = uiState.errorMessage
        if (error != null) {
            snackbarHostState.showSnackbar(error)
        }
    }

    val canProceed = when (uiState.currentStep) {
        RouteWizardStep.ORIGIN_DESTINATION -> uiState.canProceedFromStep1
        RouteWizardStep.PATH_AND_STOPS -> uiState.canProceedFromStep2
        RouteWizardStep.PRICING_MATRIX -> uiState.canProceedFromStep3
        RouteWizardStep.BUS_DRIVER_SCHEDULE -> uiState.canLaunchFromStep4
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundWhite,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            WizardStepperHeader(
                currentStep = uiState.currentStep,
                onStepClick = { step -> viewModel.goToStep(step) },
                onBackClick = {
                    if (uiState.currentStep == RouteWizardStep.ORIGIN_DESTINATION) {
                        onNavigateBack()
                    } else {
                        viewModel.previousStep()
                    }
                }
            )
        },
        bottomBar = {
            WizardBottomBar(
                currentStep = uiState.currentStep,
                canProceed = canProceed,
                canSaveStandby = uiState.canSaveStandby,
                isSaving = uiState.isSaving,
                onPreviousClick = { viewModel.previousStep() },
                onNextClick = { viewModel.nextStep() },
                onLaunchClick = {
                    viewModel.openConfirmationDialog()
                },
                onSaveStandbyClick = {
                    viewModel.saveRouteStandby { routeId ->
                        onRouteLaunched(routeId)
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedContent(
                targetState = uiState.currentStep,
                transitionSpec = { AppTransitions.contentTransform() },
                label = "WizardStepTransition"
            ) { step ->
                when (step) {
                    RouteWizardStep.ORIGIN_DESTINATION -> {
                        Slide1OriginDestination(
                            uiState = uiState,
                            onSelectOrigin = { viewModel.selectOrigin(it) },
                            onSelectDestination = { viewModel.selectDestination(it) },
                            onTerminalMapClick = { viewModel.onTerminalMapClick(it) },
                            onSwapClick = { viewModel.swapOriginDestination() },
                            onResetClick = { viewModel.resetOriginDestination() }
                        )
                    }
                    RouteWizardStep.PATH_AND_STOPS -> {
                        Slide2PathAndStops(
                            uiState = uiState,
                            onSelectPath = { viewModel.selectPath(it) },
                            onToggleTransitStop = { viewModel.toggleTransitStop(it) },
                            onSetIsExpress = { viewModel.setIsExpress(it) },
                            onSelectAllTransit = { viewModel.selectAllTransitStops() },
                            onClearAllTransit = { viewModel.clearAllTransitStops() }
                        )
                    }
                    RouteWizardStep.PRICING_MATRIX -> {
                        Slide3PricingMatrix(
                            uiState = uiState,
                            onStartEditPricing = { viewModel.startEditingPricing(it) },
                            onSaveEditedPrice = { pricing, newAmount ->
                                viewModel.saveEditedPrice(pricing, newAmount)
                            },
                            onDismissEditPricing = { viewModel.startEditingPricing(null) },
                            onResetPricingsToDefault = { viewModel.resetPricingsToDefault() }
                        )
                    }
                    RouteWizardStep.BUS_DRIVER_SCHEDULE -> {
                        Slide4BusDriverSchedule(
                            uiState = uiState,
                            onSelectBus = { viewModel.selectBus(it) },
                            onSelectDriver = { viewModel.selectDriver(it) },
                            onSetDepartureTime = { h, m -> viewModel.setDepartureTime(h, m) },
                            onSetDepartureTimeInput = { viewModel.setDepartureTimeInput(it) },
                            onSetDirection = { viewModel.setDirection(it) },
                            onSetIsRoundTrip = { viewModel.setIsRoundTrip(it) },
                            onSetReturnTime = { h, m -> viewModel.setReturnTime(h, m) },
                            onResetReturnTime = { viewModel.resetReturnTimeToRecommendation() },
                            onSetHeadwayMinutes = { viewModel.setHeadwayMinutes(it) },
                            onSetTransitDwellMinutes = { viewModel.setTransitDwellMinutes(it) }
                        )
                    }
                }
            }
        }
    }

    if (uiState.showConfirmationDialog) {
        RouteConfirmationDialog(
            uiState = uiState,
            onConfirm = {
                viewModel.dismissConfirmationDialog()
                viewModel.launchAndSaveRoute { routeId ->
                    onRouteLaunched(routeId)
                }
            },
            onDismiss = { viewModel.dismissConfirmationDialog() }
        )
    }

    if (uiState.launchSuccess) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissSuccessDialog() },
            title = {
                Text(
                    text = if (uiState.isStandbySaved) "Trayek Berhasil Disimpan (Standby)" else "Trayek Berhasil Diluncurkan",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            },
            text = {
                Text(
                    text = if (uiState.isStandbySaved) {
                        "Trayek ${uiState.builtRoute?.name ?: ""} berhasil disimpan ke daftar trayek dalam status Standby.\n\nAnda dapat menugaskan armada bus dan sopir kapan saja melalui menu Garasi untuk mulai mengoperasikannya."
                    } else {
                        "Armada ${uiState.selectedBus?.busCode ?: ""} kini resmi ditugaskan melayani rute ${uiState.builtRoute?.name ?: ""} dan siap berdinas mencari pemasukan.\n\nAnda dapat memantau pergerakan armada secara langsung melalui tombol Play Simulasi di Layar Utama."
                    },
                    fontSize = 13.sp,
                    color = TextDark
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.dismissSuccessDialog()
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Kembali ke Menu")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.dismissSuccessDialog()
                    }
                ) {
                    Text("Buka Trayek Lain")
                }
            }
        )
    }
}
