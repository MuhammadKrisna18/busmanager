package com.example.busmanager.presentation.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmanager.domain.finance.model.FinancialTransaction
import com.example.busmanager.domain.finance.model.TransactionCategory
import com.example.busmanager.domain.finance.model.TransactionType
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
import com.example.busmanager.ui.theme.TextMuted
import com.example.busmanager.ui.theme.WarningAmber
import com.example.busmanager.ui.theme.WarningAmberLight

@Composable
fun FinanceScreen(
    viewModel: FinanceViewModel,
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
                .padding(horizontal = AppDimens.paddingMedium, vertical = 8.dp)
        ) {
            FinanceTopBar(
                companyName = state.company?.name ?: "PO TransNusantara",
                balanceFormatted = state.currentBalance.formatRupiah(),
                onNavigateBack = onNavigateBack
            )

            Spacer(modifier = Modifier.height(12.dp))

            FinanceSummaryCard(
                totalIncomeFormatted = state.totalIncome.formatRupiah(),
                totalExpenseFormatted = state.totalExpense.formatRupiah(),
                netProfitFormatted = state.netProfitFormatted,
                isProfitable = state.isProfitable
            )

            Spacer(modifier = Modifier.height(12.dp))

            FinanceFilterSection(
                selectedType = state.selectedTypeFilter,
                selectedBusId = state.selectedBusId,
                buses = state.availableBuses,
                onSelectType = { viewModel.setTypeFilter(it) },
                onSelectBus = { viewModel.setBusFilter(it) },
                onResetFilters = { viewModel.resetFilters() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Riwayat Mutasi Kas (${state.filteredTransactions.size})",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            Spacer(modifier = Modifier.height(6.dp))

            if (state.filteredTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(AppDimens.radiusMedium))
                        .background(SurfaceWhite)
                        .border(1.dp, CardBorder, RoundedCornerShape(AppDimens.radiusMedium))
                        .padding(AppDimens.paddingLarge),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "📑", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Belum Ada Mutasi Transaksi",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Transaksi tiket akan tercatat saat armada jalan dan gaji sopir tercatat saat pergantian hari.",
                            fontSize = 12.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.filteredTransactions, key = { it.id }) { tx ->
                        TransactionItemCard(tx)
                    }
                }
            }
        }
    }
}

@Composable
private fun FinanceTopBar(
    companyName: String,
    balanceFormatted: String,
    onNavigateBack: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDimens.radiusMedium),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = AppDimens.cardElevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(IceBlue)
                        .clickable(onClick = onNavigateBack),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "◀",
                        color = PrimaryBlueDark,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "Buku Kas & Keuangan",
                        color = TextDark,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = companyName,
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(AppDimens.radiusPill))
                    .background(IceBlue)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = balanceFormatted,
                    color = PrimaryBlueDark,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun FinanceSummaryCard(
    totalIncomeFormatted: String,
    totalExpenseFormatted: String,
    netProfitFormatted: String,
    isProfitable: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDimens.radiusMedium),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(CardBorder))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Ringkasan Arus Kas",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryItem(
                    label = "Total Masuk",
                    value = totalIncomeFormatted,
                    color = SuccessGreen,
                    modifier = Modifier.weight(1f)
                )
                SummaryItem(
                    label = "Total Gaji",
                    value = totalExpenseFormatted,
                    color = Color(0xFFDC2626),
                    modifier = Modifier.weight(1f)
                )
                SummaryItem(
                    label = "Laba Bersih",
                    value = netProfitFormatted,
                    color = if (isProfitable) PrimaryBlue else Color(0xFFDC2626),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 11.sp, color = TextMuted)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun FinanceFilterSection(
    selectedType: TransactionTypeFilter,
    selectedBusId: String?,
    buses: List<com.example.busmanager.domain.fleet.model.Bus>,
    onSelectType: (TransactionTypeFilter) -> Unit,
    onSelectBus: (String?) -> Unit,
    onResetFilters: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDimens.radiusMedium),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(CardBorder))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter Transaksi",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                if (selectedType != TransactionTypeFilter.ALL || selectedBusId != null) {
                    Text(
                        text = "Reset Filter",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue,
                        modifier = Modifier.clickable { onResetFilters() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TransactionTypeFilter.values().forEach { filter ->
                    FilterChip(
                        selected = selectedType == filter,
                        onClick = { onSelectType(filter) },
                        label = { Text(filter.displayName, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IceBlue,
                            selectedLabelColor = PrimaryBlueDark
                        )
                    )
                }
            }

            if (buses.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedBusId == null,
                        onClick = { onSelectBus(null) },
                        label = { Text("Semua Bus", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IceBlue,
                            selectedLabelColor = PrimaryBlueDark
                        )
                    )
                    buses.forEach { bus ->
                        FilterChip(
                            selected = selectedBusId == bus.id,
                            onClick = { onSelectBus(bus.id) },
                            label = { Text(bus.busCode, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = IceBlue,
                                selectedLabelColor = PrimaryBlueDark
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionItemCard(tx: FinancialTransaction) {
    val isIncome = tx.type == TransactionType.INCOME
    val badgeBg = when (tx.category) {
        TransactionCategory.TICKET_SALE -> SuccessGreenLight
        TransactionCategory.BUS_SALE -> SuccessGreenLight
        TransactionCategory.DRIVER_WAGE -> Color(0xFFFEE2E2)
        TransactionCategory.BUS_PURCHASE -> WarningAmberLight
        TransactionCategory.OTHER -> IceBlue
    }
    val badgeColor = when (tx.category) {
        TransactionCategory.TICKET_SALE -> SuccessGreen
        TransactionCategory.BUS_SALE -> SuccessGreen
        TransactionCategory.DRIVER_WAGE -> Color(0xFFDC2626)
        TransactionCategory.BUS_PURCHASE -> WarningAmber
        TransactionCategory.OTHER -> PrimaryBlue
    }
    val amountPrefix = if (isIncome) "+ " else "- "
    val amountColor = if (isIncome) SuccessGreen else Color(0xFFDC2626)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(CardBorder))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(badgeBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = tx.category.displayName,
                            color = badgeColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "${tx.dateText} • ${tx.timeText}",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = tx.description,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark
                )

                if (tx.busCode != null || tx.routeName != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = listOfNotNull(tx.busCode, tx.routeName).joinToString(" • "),
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }

            Text(
                text = "$amountPrefix${tx.amount.formatRupiah()}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )
        }
    }
}
