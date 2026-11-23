package com.example.busmanager.presentation.showroom

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.IconButton
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
import com.example.busmanager.domain.fleet.model.AxleType
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.fleet.model.ChassisBrand
import com.example.busmanager.domain.fleet.model.KaroseriBrand
import com.example.busmanager.domain.fleet.model.SuspensionType
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
import com.example.busmanager.ui.theme.SurfaceWhite
import com.example.busmanager.ui.theme.TextDark
import com.example.busmanager.ui.theme.TextLight
import com.example.busmanager.ui.theme.TextMuted

@Composable
fun ShowroomScreen(
    viewModel: ShowroomViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            ShowroomTopBar(onNavigateBack = onNavigateBack)
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
                contentPadding = PaddingValues(vertical = AppDimens.paddingSmall),
                verticalArrangement = Arrangement.spacedBy(AppDimens.paddingMedium)
            ) {
                item {
                    CompanyBalanceBanner(
                        companyName = uiState.company?.name ?: "PO TransNusantara",
                        balanceFormatted = uiState.company?.balance?.formatRupiah() ?: "Rp 0"
                    )
                }

                item {
                    KaroseriFilterRow(
                        selected = uiState.selectedKaroseriFilter,
                        onSelect = { viewModel.setKaroseriFilter(it) }
                    )
                }

                item {
                    ChassisFilterRow(
                        selected = uiState.selectedChassisFilter,
                        onSelect = { viewModel.setChassisFilter(it) }
                    )
                }

                item {
                    Text(
                        text = "Unit Siap Beli (${uiState.filteredBuses.size})",
                        color = TextDark,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(uiState.filteredBuses) { bus ->
                    val canAfford = uiState.company?.balance?.canAfford(bus.totalPrice) ?: false
                    ShowroomBusCard(
                        bus = bus,
                        canAfford = canAfford,
                        onBuyClick = { viewModel.openPurchaseDialog(bus) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(AppDimens.paddingLarge))
                }
            }
        }

        if (uiState.selectedBusForPurchase != null) {
            PurchaseConfirmDialog(
                bus = uiState.selectedBusForPurchase!!,
                busCode = uiState.customBusCode,
                onBusCodeChange = { viewModel.updateBusCode(it) },
                isPurchasing = uiState.isPurchasing,
                errorMessage = uiState.errorMessage,
                onConfirm = { viewModel.confirmPurchase() },
                onDismiss = { viewModel.dismissPurchaseDialog() }
            )
        }

        if (uiState.purchaseSuccessBus != null) {
            PurchaseSuccessDialog(
                bus = uiState.purchaseSuccessBus!!,
                onDismiss = { viewModel.dismissSuccessNotice() }
            )
        }
    }
}

@Composable
private fun ShowroomTopBar(onNavigateBack: () -> Unit) {
    Surface(
        color = SurfaceWhite,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppDimens.paddingSmall, vertical = AppDimens.paddingSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Text(text = "←", fontSize = 24.sp, color = PrimaryBlue, fontWeight = FontWeight.Bold)
            }

            Column(modifier = Modifier.padding(start = 4.dp)) {
                Text(
                    text = "Showroom Bus Jadi",
                    color = TextDark,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Katalog Unit Siap Beroperasi",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun CompanyBalanceBanner(
    companyName: String,
    balanceFormatted: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDimens.radiusMedium),
        colors = CardDefaults.cardColors(containerColor = PrimaryBlue),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.paddingMedium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Saldo Kas PO",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = balanceFormatted,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(AppDimens.radiusPill))
                    .background(Color.White.copy(alpha = 0.2f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Dana Siap Beli",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun KaroseriFilterRow(
    selected: KaroseriBrand?,
    onSelect: (KaroseriBrand?) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(AppDimens.paddingSmall),
        modifier = Modifier.fillMaxWidth()
    ) {
        item {
            FilterChip(
                selected = selected == null,
                onClick = { onSelect(null) },
                label = { Text("Semua Karoseri", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryBlue,
                    selectedLabelColor = Color.White
                )
            )
        }

        items(KaroseriBrand.entries) { brand ->
            FilterChip(
                selected = selected == brand,
                onClick = { onSelect(brand) },
                label = { Text(brand.displayName, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryBlue,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
private fun ChassisFilterRow(
    selected: ChassisBrand?,
    onSelect: (ChassisBrand?) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(AppDimens.paddingSmall),
        modifier = Modifier.fillMaxWidth()
    ) {
        item {
            FilterChip(
                selected = selected == null,
                onClick = { onSelect(null) },
                label = { Text("Semua Sasis", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryBlue,
                    selectedLabelColor = Color.White
                )
            )
        }

        items(ChassisBrand.entries) { brand ->
            FilterChip(
                selected = selected == brand,
                onClick = { onSelect(brand) },
                label = { Text(brand.displayName, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryBlue,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
private fun ShowroomBusCard(
    bus: Bus,
    canAfford: Boolean,
    onBuyClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDimens.radiusMedium),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(AppDimens.paddingMedium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${bus.chassis.brand.displayName} ${bus.chassis.modelName}",
                        color = PrimaryBlueDark,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${bus.body.brand.displayName} ${bus.body.modelName}",
                        color = TextDark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(AppDimens.radiusPill))
                        .background(AccentBlue)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = bus.body.busClass.displayName,
                        color = PrimaryBlueDark,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FeatureBadge(text = bus.chassis.suspension.displayName)
                FeatureBadge(text = bus.chassis.axleType.displayName)
                FeatureBadge(text = bus.chassis.transmission.displayName)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SpecItem(label = "Tenaga", value = "${bus.chassis.horsepower} HP")
                SpecItem(label = "Torsi", value = "${bus.chassis.torqueNm} Nm")
                SpecItem(label = "Kapasitas", value = "${bus.body.passengerCapacity} Kursi")
                SpecItem(label = "Efisiensi", value = "${bus.chassis.fuelConsumptionKmPerLiter} km/L")
            }

            Spacer(modifier = Modifier.height(14.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(CardBorder))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Harga Total Unit", color = TextLight, fontSize = 11.sp)
                    Text(
                        text = bus.totalPrice.formatRupiah(),
                        color = PrimaryBlue,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Button(
                    onClick = onBuyClick,
                    enabled = canAfford,
                    shape = RoundedCornerShape(AppDimens.radiusMedium),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue,
                        disabledContainerColor = DisabledBackground
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (canAfford) "Beli Unit" else "Saldo Kurang",
                        color = if (canAfford) Color.White else DisabledGray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun FeatureBadge(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFFF1F5F9))
            .border(0.5.dp, CardBorder, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text = text, color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SpecItem(label: String, value: String) {
    Column {
        Text(text = label, color = TextLight, fontSize = 10.sp)
        Text(text = value, color = TextDark, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun PurchaseConfirmDialog(
    bus: Bus,
    busCode: String,
    onBusCodeChange: (String) -> Unit,
    isPurchasing: Boolean,
    errorMessage: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(AppDimens.radiusLarge),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppDimens.paddingLarge)
            ) {
                Text(
                    text = "Konfirmasi Beli Bus",
                    color = TextDark,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${bus.chassis.brand.displayName} ${bus.chassis.modelName} - ${bus.body.brand.displayName} ${bus.body.modelName}",
                    color = PrimaryBlueDark,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(AppDimens.paddingMedium))

                Text(
                    text = "Kode Bus (Nomor Armada)",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = busCode,
                    onValueChange = onBusCodeChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Contoh: TN-01") },
                    singleLine = true,
                    shape = RoundedCornerShape(AppDimens.radiusSmall)
                )

                Spacer(modifier = Modifier.height(AppDimens.paddingMedium))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Total Harga Beli", color = TextMuted, fontSize = 13.sp)
                    Text(
                        text = bus.totalPrice.formatRupiah(),
                        color = PrimaryBlue,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        color = Color(0xFFDC2626),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(AppDimens.paddingLarge))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppDimens.paddingSmall)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(AppDimens.radiusMedium),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                    ) {
                        Text(text = "Batal", color = TextMuted)
                    }

                    Button(
                        onClick = onConfirm,
                        enabled = !isPurchasing && busCode.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(AppDimens.radiusMedium),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text(
                            text = if (isPurchasing) "Memproses..." else "Beli",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PurchaseSuccessDialog(
    bus: Bus,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(AppDimens.radiusLarge),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppDimens.paddingLarge),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "🎉", fontSize = 36.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Bus Berhasil Dibeli!",
                    color = TextDark,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Kode Bus: ${bus.busCode}",
                    color = PrimaryBlue,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${bus.chassis.brand.displayName} - ${bus.body.modelName}",
                    color = TextMuted,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Unit baru telah berhasil ditambahkan dan siap digunakan di Garasi Armada.",
                    color = TextLight,
                    fontSize = 12.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(AppDimens.paddingLarge))

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
