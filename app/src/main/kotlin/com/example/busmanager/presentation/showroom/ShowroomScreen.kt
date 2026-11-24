package com.example.busmanager.presentation.showroom

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import com.example.busmanager.domain.company.model.Company
import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.fleet.model.BusClass
import com.example.busmanager.domain.fleet.model.Chassis
import com.example.busmanager.domain.fleet.model.ChassisBrand
import com.example.busmanager.domain.fleet.model.KaroseriBody
import com.example.busmanager.domain.fleet.model.KaroseriBrand
import com.example.busmanager.ui.theme.AccentBlue
import com.example.busmanager.ui.theme.AppDimens
import com.example.busmanager.ui.theme.BackgroundWhite
import com.example.busmanager.ui.theme.CardBorder
import com.example.busmanager.ui.theme.DisabledBackground
import com.example.busmanager.ui.theme.DisabledGray
import com.example.busmanager.ui.theme.IceBlue
import com.example.busmanager.ui.theme.PrimaryBlue
import com.example.busmanager.ui.theme.PrimaryBlueDark
import com.example.busmanager.ui.theme.SuccessGreen
import com.example.busmanager.ui.theme.SuccessGreenLight
import com.example.busmanager.ui.theme.SurfaceWhite
import com.example.busmanager.ui.theme.TextDark
import com.example.busmanager.ui.theme.TextMuted
import com.example.busmanager.ui.theme.WarningAmberLight

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
            ShowroomTopBar(onNavigateBack = onNavigateBack)
        },
        bottomBar = {
            if (!uiState.isLoading) {
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
                    ShowroomStepIndicator(
                        currentStep = uiState.currentStep,
                        selectedChassis = uiState.selectedChassis,
                        selectedBody = uiState.selectedBody,
                        onStepClick = { viewModel.goToStep(it) }
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
                                totalInvestment = uiState.totalInvestment,
                                company = uiState.company,
                                canAfford = uiState.canAfford,
                                onUpdateBusCode = { viewModel.updateBusCode(it) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (uiState.purchaseSuccessBus != null) {
        PurchaseSuccessModal(
            bus = uiState.purchaseSuccessBus!!,
            onDismiss = { viewModel.dismissSuccessNotice() },
            onNavigateToGarage = {
                viewModel.dismissSuccessNotice()
                onNavigateToGarage()
            }
        )
    }
}

@Composable
private fun ShowroomTopBar(onNavigateBack: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = SurfaceWhite,
        shadowElevation = AppDimens.cardElevation
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppDimens.paddingMedium, vertical = 12.dp),
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
                    text = "Perakitan & Pembelian Armada",
                    color = TextDark,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Showroom & Karoseri PO TransNusantara",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun CompanyBalanceBanner(company: Company?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDimens.radiusMedium),
        colors = CardDefaults.cardColors(containerColor = PrimaryBlueDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppDimens.paddingMedium, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = company?.name ?: "PO TransNusantara",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp
                )
                Text(
                    text = "Saldo Kas Tersedia",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = company?.balance?.formatRupiah() ?: "Rp 0",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun ShowroomStepIndicator(
    currentStep: ShowroomStep,
    selectedChassis: Chassis?,
    selectedBody: KaroseriBody?,
    onStepClick: (ShowroomStep) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppDimens.radiusSmall))
            .background(IceBlue)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ShowroomStep.entries.forEach { step ->
            val isCurrent = step == currentStep
            val isCompleted = when (step) {
                ShowroomStep.CHASSIS -> selectedChassis != null
                ShowroomStep.BODY -> selectedBody != null
                ShowroomStep.CLASS -> selectedChassis != null && selectedBody != null
                ShowroomStep.REVIEW -> false
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isCurrent) PrimaryBlue else Color.Transparent)
                    .clickable { onStepClick(step) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isCompleted && !isCurrent) "${step.stepNumber}. ✓" else step.title,
                    color = if (isCurrent) Color.White else PrimaryBlueDark,
                    fontSize = 11.sp,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ChassisBrandFilterRow(
    selectedBrand: ChassisBrand?,
    onSelectBrand: (ChassisBrand?) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        item {
            FilterChip(
                selected = selectedBrand == null,
                onClick = { onSelectBrand(null) },
                label = { Text("Semua Sasis", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryBlue,
                    selectedLabelColor = Color.White
                )
            )
        }
        items(ChassisBrand.entries) { brand ->
            FilterChip(
                selected = selectedBrand == brand,
                onClick = { onSelectBrand(if (selectedBrand == brand) null else brand) },
                label = { Text(brand.displayName, fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryBlue,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
private fun ChassisSelectionCard(
    chassis: Chassis,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(AppDimens.radiusMedium),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) AccentBlue else SurfaceWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = AppDimens.cardElevation),
        border = androidx.compose.foundation.BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) PrimaryBlue else CardBorder
        )
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(AppDimens.radiusSmall))
                            .background(PrimaryBlueDark)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = chassis.brand.displayName,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = chassis.modelName,
                        color = TextDark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = chassis.price.formatRupiah(),
                    color = PrimaryBlue,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFF8FAFC))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                SpecBadge(label = "Gandar", value = chassis.axleType.displayName)
                SpecBadge(label = "Posisi Mesin", value = chassis.engineLocation.displayName)
            }
        }
    }
}

@Composable
private fun SelectedChassisSummaryBanner(chassis: Chassis?) {
    if (chassis == null) return
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDimens.radiusSmall),
        colors = CardDefaults.cardColors(containerColor = IceBlue)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Sasis Dipilih:", fontSize = 10.sp, color = TextMuted)
                Text(
                    text = "${chassis.brand.displayName} ${chassis.modelName}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlueDark
                )
            }
            Text(
                text = chassis.price.formatRupiah(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue
            )
        }
    }
}

@Composable
private fun KaroseriBrandFilterRow(
    selectedBrand: KaroseriBrand?,
    onSelectBrand: (KaroseriBrand?) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        item {
            FilterChip(
                selected = selectedBrand == null,
                onClick = { onSelectBrand(null) },
                label = { Text("Semua Karoseri", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryBlue,
                    selectedLabelColor = Color.White
                )
            )
        }
        items(KaroseriBrand.entries) { brand ->
            FilterChip(
                selected = selectedBrand == brand,
                onClick = { onSelectBrand(if (selectedBrand == brand) null else brand) },
                label = { Text(brand.displayName, fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryBlue,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
private fun KaroseriBodySelectionCard(
    body: KaroseriBody,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(AppDimens.radiusMedium),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) AccentBlue else SurfaceWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = AppDimens.cardElevation),
        border = androidx.compose.foundation.BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) PrimaryBlue else CardBorder
        )
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(AppDimens.radiusSmall))
                            .background(Color(0xFF0369A1))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = body.brand.displayName,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = body.modelName,
                        color = TextDark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = body.price.formatRupiah(),
                    color = PrimaryBlue,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFF8FAFC))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                SpecBadge(label = "Kenyamanan", value = "${body.comfortRating}/100")
                SpecBadge(label = "Kompatibilitas", value = if (body.requiresTronton) "Wajib 6x2 (Tronton)" else "4x2 / 6x2")
            }
        }
    }
}

@Composable
private fun ConfigurationSummaryHeader(
    chassis: Chassis?,
    body: KaroseriBody?
) {
    if (chassis == null || body == null) return
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDimens.radiusSmall),
        colors = CardDefaults.cardColors(containerColor = IceBlue)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = "${chassis.brand.displayName} ${chassis.modelName} + ${body.brand.displayName} ${body.modelName}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlueDark
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Pilih kelas interior layanan penumpang untuk menentukan konfigurasi kursi & tarif tiket.",
                fontSize = 11.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun BusClassSelectionCard(
    busClass: BusClass,
    body: KaroseriBody?,
    chassis: Chassis? = null,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val isTronton = chassis?.axleType == com.example.busmanager.domain.fleet.model.AxleType.TRONTON_6X2 || body?.requiresTronton == true
    val isDoubleDecker = body?.bodyType == com.example.busmanager.domain.fleet.model.BusBodyType.SDD

    val capacity = when (busClass) {
        BusClass.EKONOMI -> if (isDoubleDecker) 60 else if (isTronton) 54 else 48
        BusClass.BISNIS -> if (isDoubleDecker) 50 else if (isTronton) 44 else 38
        BusClass.VIP -> if (isDoubleDecker) 42 else if (isTronton) 38 else 34
        BusClass.EKSEKUTIF -> if (isDoubleDecker) 36 else if (isTronton) 32 else 28
        BusClass.SUPER_EKSEKUTIF -> if (isDoubleDecker) 28 else if (isTronton) 26 else 22
        BusClass.SUITES_CLASS -> if (isDoubleDecker) 24 else 21
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(AppDimens.radiusMedium),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) AccentBlue else SurfaceWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = AppDimens.cardElevation),
        border = androidx.compose.foundation.BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) PrimaryBlue else CardBorder
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.paddingMedium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = busClass.displayName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(AppDimens.radiusPill))
                            .background(SuccessGreenLight)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${busClass.ticketMultiplier}x Tarif Tiket",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Kapasitas: $capacity Kursi Penumpang",
                    fontSize = 12.sp,
                    color = PrimaryBlueDark,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Bebas Biaya",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SuccessGreen
                )
                Text(
                    text = "Tarif Manual",
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
private fun ReviewConfirmationContent(
    chassis: Chassis?,
    body: KaroseriBody?,
    customBusCode: String,
    totalInvestment: Money,
    company: Company?,
    canAfford: Boolean,
    onUpdateBusCode: (String) -> Unit
) {
    if (chassis == null || body == null) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDimens.radiusMedium),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = AppDimens.cardElevation),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.paddingMedium)
        ) {
            Text(
                text = "🚌 Cetak Biru Perakitan Armada Baru",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(10.dp))

            ReviewItemRow(
                label = "Sasis Pilihan",
                detail = "${chassis.brand.displayName} ${chassis.modelName}",
                price = chassis.price.formatRupiah()
            )
            Spacer(modifier = Modifier.height(6.dp))

            ReviewItemRow(
                label = "Bodi Karoseri",
                detail = "${body.brand.displayName} ${body.modelName}",
                price = body.price.formatRupiah()
            )
            Spacer(modifier = Modifier.height(6.dp))

            ReviewItemRow(
                label = "Kelas & Kursi",
                detail = "${body.busClass.displayName} (${body.passengerCapacity} Kursi)",
                price = "${body.busClass.ticketMultiplier}x Tarif"
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(IceBlue)
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Investasi Pembelian:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlueDark
                    )
                    Text(
                        text = totalInvestment.formatRupiah(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Nomor Plat / Kode Panggilan Armada:",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(4.dp))

            OutlinedTextField(
                value = customBusCode,
                onValueChange = onUpdateBusCode,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Contoh: TN-01, TN-02") }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Saldo Kas Tersedia:", fontSize = 11.sp, color = TextMuted)
                Text(
                    text = company?.balance?.formatRupiah() ?: "Rp 0",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Sisa Saldo Setelah Beli:", fontSize = 11.sp, color = TextMuted)
                val remaining = (company?.balance?.amount ?: 0L) - totalInvestment.amount
                Text(
                    text = if (remaining >= 0L) Money.rupiah(remaining).formatRupiah() else "-${Money.rupiah(-remaining).formatRupiah()}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (canAfford) SuccessGreen else Color(0xFFDC2626)
                )
            }
        }
    }
}

@Composable
private fun ReviewItemRow(label: String, detail: String, price: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, fontSize = 10.sp, color = TextMuted)
            Text(text = detail, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
        }
        Text(text = price, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryBlueDark)
    }
}

@Composable
private fun SpecBadge(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 9.sp, color = TextMuted)
        Text(text = value, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextDark)
    }
}

@Composable
private fun ShowroomBottomActionBar(
    currentStep: ShowroomStep,
    canProceed: Boolean,
    canAfford: Boolean,
    isPurchasing: Boolean,
    totalInvestment: Money,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onConfirmPurchase: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = SurfaceWhite,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppDimens.paddingMedium, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(AppDimens.paddingSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentStep != ShowroomStep.CHASSIS) {
                OutlinedButton(
                    onClick = onPrevious,
                    shape = RoundedCornerShape(AppDimens.radiusMedium),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("◀ Kembali", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            if (currentStep == ShowroomStep.REVIEW) {
                Button(
                    onClick = onConfirmPurchase,
                    enabled = canProceed && canAfford && !isPurchasing,
                    shape = RoundedCornerShape(AppDimens.radiusMedium),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canAfford) SuccessGreen else DisabledGray
                    ),
                    modifier = Modifier.weight(if (currentStep == ShowroomStep.CHASSIS) 1f else 2f)
                ) {
                    Text(
                        text = if (isPurchasing) "Memproses..." else if (canAfford) "🛒 Beli Armada (${totalInvestment.formatRupiah()})" else "Saldo Tidak Cukup",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            } else {
                Button(
                    onClick = onNext,
                    enabled = canProceed,
                    shape = RoundedCornerShape(AppDimens.radiusMedium),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    modifier = Modifier.weight(if (currentStep == ShowroomStep.CHASSIS) 1f else 2f)
                ) {
                    Text("Lanjut Langkah Berikutnya ➔", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun PurchaseSuccessModal(
    bus: Bus,
    onDismiss: () -> Unit,
    onNavigateToGarage: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(AppDimens.radiusLarge),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.paddingSmall)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppDimens.paddingLarge),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(SuccessGreenLight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🚌", fontSize = 28.sp)
                }

                Spacer(modifier = Modifier.height(AppDimens.paddingMedium))

                Text(
                    text = "Armada Berhasil Dirakit!",
                    color = TextDark,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${bus.busCode} • ${bus.fullName}",
                    color = PrimaryBlueDark,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Kelas ${bus.body.busClass.displayName} (${bus.body.passengerCapacity} Kursi)",
                    color = TextMuted,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(AppDimens.paddingMedium))

                Button(
                    onClick = onNavigateToGarage,
                    shape = RoundedCornerShape(AppDimens.radiusPill),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Buka Garasi Armada", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(AppDimens.radiusPill),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Rakit Armada Baru Lagi", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
