package com.example.busmanager.presentation.driver.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmanager.presentation.driver.DriverTab
import com.example.busmanager.ui.theme.AppDimens
import com.example.busmanager.ui.theme.IceBlue
import com.example.busmanager.ui.theme.PrimaryBlue
import com.example.busmanager.ui.theme.PrimaryBlueDark

@Composable
fun DriverTabSwitcher(
    selectedTab: DriverTab,
    hiredCount: Int,
    candidateCount: Int,
    onTabSelected: (DriverTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppDimens.radiusPill))
            .background(IceBlue)
            .padding(4.dp)
    ) {
        TabButton(
            title = "Sopir Aktif ($hiredCount)",
            isSelected = selectedTab == DriverTab.HIRED,
            onClick = { onTabSelected(DriverTab.HIRED) },
            modifier = Modifier.weight(1f)
        )
        TabButton(
            title = "Bursa Pelamar ($candidateCount)",
            isSelected = selectedTab == DriverTab.CANDIDATES,
            onClick = { onTabSelected(DriverTab.CANDIDATES) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun TabButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(AppDimens.radiusPill))
            .background(if (isSelected) PrimaryBlue else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (isSelected) Color.White else PrimaryBlueDark,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
