package com.example.busmanager.presentation.route.wizard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmanager.domain.route.model.RouteCategory
import com.example.busmanager.ui.theme.AppDimens
import com.example.busmanager.ui.theme.IceBlue
import com.example.busmanager.ui.theme.PrimaryBlueDark
import com.example.busmanager.ui.theme.TextDark
import com.example.busmanager.ui.theme.TextMuted

@Composable
fun ModeBadgeBanner(
    routeCategory: RouteCategory,
    originCity: String,
    destCity: String,
    modifier: Modifier = Modifier
) {
    val isAkap = routeCategory == RouteCategory.AKAP

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Slot Keberangkatan Terminal",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(AppDimens.radiusPill))
                    .background(if (isAkap) Color(0xFFFEF3C7) else IceBlue)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isAkap) "Mode AKAP" else "Mode AKDP",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isAkap) Color(0xFFB45309) else PrimaryBlueDark
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Pilih slot resmi keberangkatan bus dari Terminal $originCity menuju $destCity. Terminal beroperasi 24 jam dengan interval slot 15 menit.",
            fontSize = 12.sp,
            color = TextMuted,
            lineHeight = 16.sp
        )

        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isAkap) Color(0xFFFFFBEB) else Color(0xFFF0F9FF)
            ),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(
                    if (isAkap) Color(0xFFFDE68A) else Color(0xFFBAE6FD)
                )
            )
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = if (isAkap) {
                        "Ketentuan AKAP: Jarak jauh antar-provinsi. Wajib istirahat 5 jam di kota tujuan sebelum jadwal kepulangan (maksimal 1 PP per hari)."
                    } else {
                        "Ketentuan AKDP: Antar-kota dalam provinsi. Waktu istirahat 30 menit di kota tujuan dan jeda 1 jam di pangkalan asal antar-PP."
                    },
                    fontSize = 11.sp,
                    color = if (isAkap) Color(0xFF92400E) else Color(0xFF0369A1),
                    lineHeight = 15.sp
                )
            }
        }
    }
}
