package com.example.busmanager.presentation.showroom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmanager.domain.fleet.model.BusClass
import com.example.busmanager.presentation.common.FeedbackToastData
import com.example.busmanager.presentation.common.GameFeedbackToast
import com.example.busmanager.presentation.showroom.components.BusClassSelectionCard
import com.example.busmanager.presentation.showroom.components.ChassisBrandFilterRow
import com.example.busmanager.presentation.showroom.components.ChassisSelectionCard
import com.example.busmanager.presentation.showroom.components.CompanyBalanceBanner
import com.example.busmanager.presentation.showroom.components.ConfigurationSummaryHeader
import com.example.busmanager.presentation.showroom.components.InstantBusCard
import com.example.busmanager.presentation.showroom.components.KaroseriBodySelectionCard
import com.example.busmanager.presentation.showroom.components.KaroseriBrandFilterRow
import com.example.busmanager.presentation.showroom.components.ReviewConfirmationContent
import com.example.busmanager.presentation.showroom.components.SelectedChassisSummaryBanner
import com.example.busmanager.presentation.showroom.components.ShowroomBottomActionBar
import com.example.busmanager.presentation.showroom.components.ShowroomModeSelector
import com.example.busmanager.presentation.showroom.components.ShowroomStepIndicator
import com.example.busmanager.presentation.showroom.components.ShowroomTopBar
import com.example.busmanager.ui.theme.AppDimens
import com.example.busmanager.ui.theme.BackgroundWhite
import com.example.busmanager.ui.theme.IceBlue
import com.example.busmanager.ui.theme.PrimaryBlue
import com.example.busmanager.ui.theme.PrimaryBlueDark
import com.example.busmanager.ui.theme.TextMuted

@Composable
fun ShowroomScreen(
    viewModel: ShowroomViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToGarage: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            ShowroomTopBar(onNavigateBack = {
                viewModel.dismissSuccessNotice()
                onNavigateBack()
            })
        },
        bottomBar = {
            if (!uiState.isLoading && uiState.mode == ShowroomMode.CUSTOM_ASSEMBLY) {
                ShowroomBottomActionBar(
                    currentStep = uiState.currentStep,
                    canProceed = when (uiState.currentStep) {
                        ShowroomStep.CHASSIS -> uiState.selectedChassis != null
                        ShowroomStep.BODY -> uiState.selectedBody != null
                        ShowroomStep.CLASS -> true
                        ShowroomStep.REVIEW -> uiState.canAfford && !uiState.isPurchasing
                    },
                    canAfford = uiState.canAfford,
                    isPurchasing = uiState.isPurchasing,
                    batchQuantity = uiState.batchQuantity,
                    totalInvestment = uiState.totalInvestment,
                    onPrevious = { viewModel.previousStep() },
                    onNext = { viewModel.nextStep() },
                    onConfirmPurchase = { viewModel.confirmPurchase() }
                )
            }
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
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = AppDimens.paddingMedium),
                contentPadding = PaddingValues(top = AppDimens.paddingSmall, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(AppDimens.paddingSmall)
            ) {
                item {
                    CompanyBalanceBanner(company = uiState.company)
                }

                item {
                    ShowroomModeSelector(
                        currentMode = uiState.mode,
                        onSelectMode = { viewModel.setMode(it) }
                    )
                }

                if (uiState.errorMessage != null) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(AppDimens.radiusSmall))
                                .background(Color(0xFFFEE2E2))
                                .padding(AppDimens.paddingSmall)
                        ) {
                            Text(
                                text = "⚠️ ${uiState.errorMessage}",
                                color = Color(0xFFDC2626),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (uiState.mode == ShowroomMode.INSTANT_PURCHASE) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(AppDimens.radiusMedium),
                            colors = CardDefaults.cardColors(containerColor = IceBlue)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(AppDimens.paddingMedium)
                            ) {
                                Text(
                                    text = "⚡ Katalog Bus Siap Pakai Pabrikan",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlueDark
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Pilih armada siap jalan dengan konfigurasi sasis, bodi, dan kapasitas kursi asli pabrikan. Bisa dibeli borongan sesuai kebutuhan ekspansi.",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }

                    items(uiState.readyStockBuses, key = { it.id }) { stockBus ->
                        val qty = uiState.getInstantQuantity(stockBus.id)
                        val totalCost = uiState.getInstantTotalCost(stockBus)
                        val canAffordThis = uiState.canAffordInstant(stockBus)

                        InstantBusCard(
                            bus = stockBus,
                            quantity = qty,
                            totalCost = totalCost,
                            canAfford = canAffordThis,
                            isPurchasing = uiState.isPurchasing,
                            onQuantityChange = { viewModel.setInstantQuantity(stockBus.id, it) },
                            onBuy = { viewModel.buyInstantBus(stockBus) }
                        )
                    }
                } else {
                    item {
                        ShowroomStepIndicator(
                            currentStep = uiState.currentStep,
                            selectedChassis = uiState.selectedChassis,
                            selectedBody = uiState.selectedBody,
                            onStepClick = { viewModel.goToStep(it) }
                        )
                    }

                    when (uiState.currentStep) {
                        ShowroomStep.CHASSIS -> {
                            item {
                                ChassisBrandFilterRow(
                                    selectedBrand = uiState.selectedChassisFilter,
                                    onSelectBrand = { viewModel.setChassisBrandFilter(it) }
                                )
                            }

                            items(uiState.filteredChassis, key = { it.id }) { chassis ->
                                ChassisSelectionCard(
                                    chassis = chassis,
                                    isSelected = uiState.selectedChassis?.id == chassis.id,
                                    onSelect = { viewModel.selectChassis(chassis) }
                                )
                            }
                        }

                        ShowroomStep.BODY -> {
                            item {
                                SelectedChassisSummaryBanner(chassis = uiState.selectedChassis)
                            }

                            item {
                                KaroseriBrandFilterRow(
                                    selectedBrand = uiState.selectedKaroseriFilter,
                                    onSelectBrand = { viewModel.setKaroseriBrandFilter(it) }
                                )
                            }

                            if (uiState.compatibleBodies.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Tidak ada bodi yang kompatibel dengan filter terpilih.",
                                            color = TextMuted,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            } else {
                                items(uiState.compatibleBodies, key = { it.id }) { body ->
                                    KaroseriBodySelectionCard(
                                        body = body,
                                        isSelected = uiState.selectedBody?.id == body.id,
                                        onSelect = { viewModel.selectBody(body) }
                                    )
                                }
                            }
                        }

                        ShowroomStep.CLASS -> {
                            item {
                                ConfigurationSummaryHeader(
                                    chassis = uiState.selectedChassis,
                                    body = uiState.selectedBody
                                )
                            }

                            items(BusClass.entries, key = { it.name }) { busClass ->
                                BusClassSelectionCard(
                                    busClass = busClass,
                                    body = uiState.selectedBody,
                                    chassis = uiState.selectedChassis,
                                    isSelected = uiState.selectedBusClass == busClass,
                                    onSelect = { viewModel.selectBusClass(busClass) }
                                )
                            }
                        }

                        ShowroomStep.REVIEW -> {
                            item {
                                ReviewConfirmationContent(
                                    chassis = uiState.selectedChassis,
                                    body = uiState.tailoredBody,
                                    customBusCode = uiState.customBusCode,
                                    batchQuantity = uiState.batchQuantity,
                                    unitInvestment = uiState.unitInvestment,
                                    totalInvestment = uiState.totalInvestment,
                                    company = uiState.company,
                                    canAfford = uiState.canAfford,
                                    onUpdateBusCode = { viewModel.updateBusCode(it) },
                                    onUpdateBatchQuantity = { viewModel.setBatchQuantity(it) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    val successBus = uiState.purchaseSuccessBus
    val successBuses = uiState.purchaseSuccessBuses
    val toastData = remember(successBus, successBuses) {
        if (successBuses.isNotEmpty()) {
            val first = successBuses.first()
            val last = successBuses.last()
            FeedbackToastData(
                iconEmoji = "🚌",
                title = "Berhasil Membeli ${successBuses.size} Unit Armada!",
                subtitle = "${first.busCode} s/d ${last.busCode} • ${first.fullName}",
                durationMillis = 1000L
            )
        } else if (successBus != null) {
            FeedbackToastData(
                iconEmoji = "🚌",
                title = "Armada Berhasil Dibeli!",
                subtitle = "${successBus.busCode} • ${successBus.fullName}",
                durationMillis = 1000L
            )
        } else null
    }

    GameFeedbackToast(
        data = toastData,
        onDismiss = { viewModel.dismissSuccessNotice() }
    )
}
