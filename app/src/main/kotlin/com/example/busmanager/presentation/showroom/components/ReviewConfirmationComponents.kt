package com.example.busmanager.presentation.showroom.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmanager.domain.company.model.Company
import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.fleet.model.Chassis
import com.example.busmanager.domain.fleet.model.KaroseriBody
import com.example.busmanager.ui.theme.AppDimens
import com.example.busmanager.ui.theme.CardBorder
import com.example.busmanager.ui.theme.DisabledGray
import com.example.busmanager.ui.theme.IceBlue
import com.example.busmanager.ui.theme.PrimaryBlue
import com.example.busmanager.ui.theme.PrimaryBlueDark
import com.example.busmanager.ui.theme.SuccessGreen
import com.example.busmanager.ui.theme.SurfaceWhite
import com.example.busmanager.ui.theme.TextDark
import com.example.busmanager.ui.theme.TextMuted

@Composable
fun ReviewConfirmationContent(
    chassis: Chassis?,
    body: KaroseriBody?,
    customBusCode: String,
    batchQuantity: Int,
    unitInvestment: Money,
    totalInvestment: Money,
    company: Company?,
    canAfford: Boolean,
    onUpdateBusCode: (String) -> Unit,
    onUpdateBatchQuantity: (Int) -> Unit
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF8FAFC))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Jumlah Pembelian Unit:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Text(
                        text = "Harga satuan: ${unitInvestment.formatRupiah()}",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (batchQuantity > 1) IceBlue else Color(0xFFF1F5F9))
                            .clickable(enabled = batchQuantity > 1) { onUpdateBatchQuantity(batchQuantity - 1) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "-",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (batchQuantity > 1) PrimaryBlueDark else DisabledGray
                        )
                    }

                    Text(
                        text = "$batchQuantity",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (batchQuantity < 20) IceBlue else Color(0xFFF1F5F9))
                            .clickable(enabled = batchQuantity < 20) { onUpdateBatchQuantity(batchQuantity + 1) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (batchQuantity < 20) PrimaryBlueDark else DisabledGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

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
                    Column {
                        Text(
                            text = "Total Investasi ($batchQuantity Unit):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlueDark
                        )
                    }
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
                text = if (batchQuantity > 1) "Awalan Nomor Plat / Kode Panggilan Armada:" else "Nomor Plat / Kode Panggilan Armada:",
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
                placeholder = { Text(if (batchQuantity > 1) "Contoh: TN (akan jadi TN-01, TN-02, dst)" else "Contoh: TN-01") }
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
fun ReviewItemRow(label: String, detail: String, price: String) {
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
