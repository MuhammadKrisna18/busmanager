package com.example.busmanager.presentation.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmanager.ui.theme.AppDimens
import com.example.busmanager.ui.theme.CardBorder
import com.example.busmanager.ui.theme.DisabledBackground
import com.example.busmanager.ui.theme.DisabledGray
import com.example.busmanager.ui.theme.TextDark
import com.example.busmanager.ui.theme.TextMuted

@Composable
fun MenuGrid(
    ownedBusCount: Int,
    hiredDriverCount: Int,
    activeRouteCount: Int,
    onNavigateToShowroom: () -> Unit,
    onNavigateToGarage: () -> Unit,
    onNavigateToRoutes: () -> Unit,
    onNavigateToDrivers: () -> Unit,
    onNavigateToFinance: () -> Unit,
    onNavigateToTerminalReport: () -> Unit,
    onOpenRouteInfo: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                MenuCard(
                    title = "Garasi Armada",
                    subtitle = "Kelola & Servis Unit",
                    iconCode = "GRS",
                    badgeText = "$ownedBusCount Unit",
                    containerColor = Color(0xFFEFF6FF),
                    borderColor = Color(0xFFBFDBFE),
                    badgeBgColor = Color(0xFFDBEAFE),
                    badgeTextColor = Color(0xFF1D4ED8),
                    onClick = onNavigateToGarage
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                MenuCard(
                    title = "Showroom Bus",
                    subtitle = "Beli Bus & Sasis Baru",
                    iconCode = "BUS",
                    badgeText = "Beli Unit",
                    containerColor = Color(0xFFEEF2FF),
                    borderColor = Color(0xFFC7D2FE),
                    badgeBgColor = Color(0xFFE0E7FF),
                    badgeTextColor = Color(0xFF4338CA),
                    onClick = onNavigateToShowroom
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                MenuCard(
                    title = "Rute & Trayek",
                    subtitle = "Jalur Antar Terminal",
                    iconCode = "RTE",
                    badgeText = "Peta Jalur",
                    containerColor = Color(0xFFECFDF5),
                    borderColor = Color(0xFFA7F3D0),
                    badgeBgColor = Color(0xFFD1FAE5),
                    badgeTextColor = Color(0xFF065F46),
                    onClick = onNavigateToRoutes
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                MenuCard(
                    title = "Kru & Sopir",
                    subtitle = "Rekrutmen & Penugasan",
                    iconCode = "KRU",
                    badgeText = if (hiredDriverCount > 0) "$hiredDriverCount Sopir" else "Rekrut",
                    containerColor = Color(0xFFFFFBEB),
                    borderColor = Color(0xFFFDE68A),
                    badgeBgColor = Color(0xFFFEF3C7),
                    badgeTextColor = Color(0xFF92400E),
                    onClick = onNavigateToDrivers
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                MenuCard(
                    title = "Keuangan PO",
                    subtitle = "Buku Kas & Laba Rugi",
                    iconCode = "KAS",
                    badgeText = "Buku Kas",
                    containerColor = Color(0xFFFEFCE8),
                    borderColor = Color(0xFFFEF08A),
                    badgeBgColor = Color(0xFFFEF9C3),
                    badgeTextColor = Color(0xFF854D0E),
                    onClick = onNavigateToFinance
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                MenuCard(
                    title = "Laporan Terminal",
                    subtitle = "Arus Penumpang Wilayah",
                    iconCode = "LPR",
                    badgeText = "Data Arus",
                    containerColor = Color(0xFFF0FDFA),
                    borderColor = Color(0xFFA5F3FC),
                    badgeBgColor = Color(0xFFCFFAFE),
                    badgeTextColor = Color(0xFF0E7490),
                    onClick = onNavigateToTerminalReport
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                MenuCard(
                    title = "Informasi Trayek",
                    subtitle = "Status Operasi Trayek",
                    iconCode = "INFO",
                    badgeText = if (activeRouteCount > 0) "$activeRouteCount Aktif" else "Info Rute",
                    containerColor = Color(0xFFFAF5FF),
                    borderColor = Color(0xFFE9D5FF),
                    badgeBgColor = Color(0xFFF3E8FF),
                    badgeTextColor = Color(0xFF6B21A8),
                    onClick = onOpenRouteInfo
                )
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun MenuCard(
    title: String,
    subtitle: String,
    iconCode: String,
    badgeText: String,
    containerColor: Color,
    borderColor: Color,
    badgeBgColor: Color,
    badgeTextColor: Color,
    isActive: Boolean = true,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isActive, onClick = onClick),
        shape = RoundedCornerShape(AppDimens.radiusMedium),
        colors = CardDefaults.cardColors(containerColor = if (isActive) containerColor else DisabledBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isActive) 1.dp else 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isActive) borderColor else CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.85f))
                        .border(1.dp, borderColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = iconCode,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isActive) badgeTextColor else DisabledGray
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(AppDimens.radiusPill))
                        .background(if (isActive) badgeBgColor else Color(0xFFE2E8F0))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = if (isActive) badgeTextColor else DisabledGray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                color = if (isActive) TextDark else DisabledGray,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                color = if (isActive) TextMuted else DisabledGray,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
