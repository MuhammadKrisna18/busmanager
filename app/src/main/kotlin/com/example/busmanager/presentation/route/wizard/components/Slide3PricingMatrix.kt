package com.example.busmanager.presentation.route.wizard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.route.model.RoutePricing
import com.example.busmanager.domain.route.model.RouteRevenueProjection
import com.example.busmanager.presentation.route.wizard.RouteWizardUiState
import com.example.busmanager.ui.theme.AppDimens
import com.example.busmanager.ui.theme.CardBorder
import com.example.busmanager.ui.theme.IceBlue
import com.example.busmanager.ui.theme.PrimaryBlue
import com.example.busmanager.ui.theme.PrimaryBlueDark
import com.example.busmanager.ui.theme.SuccessGreen
import com.example.busmanager.ui.theme.SuccessGreenLight
import com.example.busmanager.ui.theme.SurfaceWhite
import com.example.busmanager.ui.theme.TextDark
import com.example.busmanager.ui.theme.TextLight
import com.example.busmanager.ui.theme.TextMuted

@Composable
fun Slide3PricingMatrix(
    uiState: RouteWizardUiState,
    onStartEditPricing: (RoutePricing) -> Unit,
    onSaveEditedPrice: (RoutePricing, Long) -> Unit,
    onDismissEditPricing: () -> Unit,
    onResetPricingsToDefault: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val stops = uiState.builtRoute?.let { route ->
        listOf(route.originTerminal) + route.transitTerminals + listOf(route.destinationTerminal)
    } ?: emptyList()

    val stopIndexMap = stops.mapIndexed { index, terminal -> terminal.id to index }.toMap()

    val roundTripPricings = uiState.pricings.filter { pricing ->
        val fromIndex = stopIndexMap[pricing.originTerminalId]
        val toIndex = stopIndexMap[pricing.destinationTerminalId]
        fromIndex != null && toIndex != null && fromIndex < toIndex
    }.sortedWith(
        compareBy(
            { stopIndexMap[it.originTerminalId] ?: 0 },
            { stopIndexMap[it.destinationTerminalId] ?: 0 }
        )
    ).distinctBy {
        minOf(it.originTerminalId, it.destinationTerminalId) to maxOf(it.originTerminalId, it.destinationTerminalId)
    }

    val assumedCapacity = uiState.selectedBus?.body?.passengerCapacity
        ?: uiState.availableBuses.firstOrNull()?.body?.passengerCapacity
        ?: 30

    val mainPricing = roundTripPricings.find {
        it.originTerminalId == uiState.selectedOrigin?.id && it.destinationTerminalId == uiState.selectedDestination?.id
    } ?: roundTripPricings.firstOrNull()

    val mainTicketPrice = mainPricing?.price ?: Money.rupiah(50_000)

    val projection = RouteRevenueProjection(
        seatCapacity = assumedCapacity,
        ticketPrice = mainTicketPrice
    )

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = IceBlue),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(PrimaryBlue))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(AppDimens.radiusPill))
                                .background(PrimaryBlue)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "INFO",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Kalkulasi Otomatis Rp 50.000 / Hop",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlueDark
                            )
                            Text(
                                text = "Harga dihitung berdasarkan jarak per lompatan kota transit. Anda bebas menyesuaikan tarif tiap relasi perjalanan.",
                                fontSize = 11.sp,
                                color = TextDark
                            )
                        }
                    }
                    TextButton(onClick = onResetPricingsToDefault) {
                        Text(
                            text = "Reset",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        item {
            RevenueProjectionCard(
                projection = projection,
                originCity = uiState.selectedOrigin?.city ?: "Asal",
                destinationCity = uiState.selectedDestination?.city ?: "Tujuan"
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Text(
                text = "Daftar Tarif Tiket Antar Kota",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Penyesuaian tarif berlaku otomatis untuk perjalanan pulang-pergi (PP)",
                fontSize = 12.sp,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        items(roundTripPricings) { pricing ->
            val fromIdx = stopIndexMap[pricing.originTerminalId] ?: 0
            val toIdx = stopIndexMap[pricing.destinationTerminalId] ?: 1
            val hopCount = (toIdx - fromIdx).coerceAtLeast(1)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onStartEditPricing(pricing) },
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${pricing.originCity} - ${pricing.destinationCity}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(IceBlue)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "PP",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PrimaryBlue
                                )
                            }
                        }
                        Text(
                            text = "$hopCount Hop Relasi • Tarif Pulang-Pergi (2 Arah)",
                            fontSize = 11.sp,
                            color = TextLight
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = pricing.price.formatRupiah(),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlueDark
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        OutlinedButton(
                            onClick = { onStartEditPricing(pricing) },
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Ubah",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryBlue
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (uiState.editingPricing != null) {
        EditPricingModal(
            pricing = uiState.editingPricing,
            onDismiss = onDismissEditPricing,
            onSave = { newAmount ->
                onSaveEditedPrice(uiState.editingPricing, newAmount)
            }
        )
    }
}

@Composable
private fun RevenueProjectionCard(
    projection: RouteRevenueProjection,
    originCity: String,
    destinationCity: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Proyeksi Pendapatan & Laba",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(AppDimens.radiusPill))
                        .background(IceBlue)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${projection.seatCapacity} Kursi (Full Seat)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlueDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Simulasi pendapatan trayek $originCity - $destinationCity berdasarkan tarif utama ${projection.ticketPrice.formatRupiah()}",
                fontSize = 11.sp,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF8FAFC))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ProjectionDetailRow(
                    label = "Omzet Tiket (1x Jalan):",
                    amount = "+ ${projection.grossRevenueOneWay.formatRupiah()}",
                    color = TextDark,
                    subtext = "PP: + ${projection.grossRevenueRoundTrip.formatRupiah()}"
                )

                ProjectionDetailRow(
                    label = "Gaji / Bagi Hasil Sopir (30%):",
                    amount = "- ${projection.driverWageOneWay.formatRupiah()}",
                    color = Color(0xFFDC2626),
                    subtext = "PP: - ${projection.driverWageRoundTrip.formatRupiah()}"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SuccessGreenLight)
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Laba Bersih Perusahaan:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            text = "Pulang-Pergi (PP): + ${projection.netProfitRoundTrip.formatRupiah()}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SuccessGreen
                        )
                    }

                    Text(
                        text = "+ ${projection.netProfitOneWay.formatRupiah()}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SuccessGreen
                    )
                }
            }
        }
    }
}

@Composable
private fun ProjectionDetailRow(
    label: String,
    amount: String,
    color: Color,
    subtext: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = label, fontSize = 11.sp, color = TextMuted)
            Text(text = subtext, fontSize = 9.sp, color = TextLight)
        }
        Text(
            text = amount,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
