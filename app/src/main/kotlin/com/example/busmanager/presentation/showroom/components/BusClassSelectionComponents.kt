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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.example.busmanager.domain.fleet.model.AxleType
import com.example.busmanager.domain.fleet.model.BusBodyType
import com.example.busmanager.domain.fleet.model.BusClass
import com.example.busmanager.domain.fleet.model.Chassis
import com.example.busmanager.domain.fleet.model.KaroseriBody
import com.example.busmanager.ui.theme.AccentBlue
import com.example.busmanager.ui.theme.AppDimens
import com.example.busmanager.ui.theme.CardBorder
import com.example.busmanager.ui.theme.IceBlue
import com.example.busmanager.ui.theme.PrimaryBlue
import com.example.busmanager.ui.theme.PrimaryBlueDark
import com.example.busmanager.ui.theme.SuccessGreen
import com.example.busmanager.ui.theme.SuccessGreenLight
import com.example.busmanager.ui.theme.SurfaceWhite
import com.example.busmanager.ui.theme.TextDark
import com.example.busmanager.ui.theme.TextMuted

@Composable
fun ConfigurationSummaryHeader(
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
fun BusClassSelectionCard(
    busClass: BusClass,
    body: KaroseriBody?,
    chassis: Chassis? = null,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val isTronton = chassis?.axleType == AxleType.TRONTON_6X2 || body?.requiresTronton == true
    val isDoubleDecker = body?.bodyType == BusBodyType.SDD

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
