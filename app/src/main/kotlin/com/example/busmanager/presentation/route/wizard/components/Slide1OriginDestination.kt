package com.example.busmanager.presentation.route.wizard.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.busmanager.domain.route.model.Terminal
import com.example.busmanager.presentation.route.wizard.RouteWizardUiState
import com.example.busmanager.ui.theme.CardBorder
import com.example.busmanager.ui.theme.IceBlue
import com.example.busmanager.ui.theme.PrimaryBlue
import com.example.busmanager.ui.theme.PrimaryBlueDark
import com.example.busmanager.ui.theme.SurfaceWhite
import com.example.busmanager.ui.theme.TextDark
import com.example.busmanager.ui.theme.TextLight
import com.example.busmanager.ui.theme.TextMuted

@Composable
fun Slide1OriginDestination(
    uiState: RouteWizardUiState,
    onSelectOrigin: (Terminal) -> Unit,
    onSelectDestination: (Terminal) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        item {
            Text(
                text = "1. Pilih Terminal Asal (Titik A)",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tentukan terminal keberangkatan awal bus",
                fontSize = 12.sp,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                uiState.allTerminals.forEach { terminal ->
                    val isSelected = uiState.selectedOrigin?.id == terminal.id
                    val isDest = uiState.selectedDestination?.id == terminal.id
                    val isDisabled = isDest
                    val badgeLabel = when {
                        isSelected -> "Titik Awal"
                        isDest -> "Tujuan Akhir"
                        else -> null
                    }

                    TerminalSelectCard(
                        terminal = terminal,
                        isSelected = isSelected,
                        isDisabled = isDisabled,
                        badgeLabel = badgeLabel,
                        onClick = { if (!isDisabled) onSelectOrigin(terminal) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        item {
            Text(
                text = "2. Pilih Terminal Tujuan (Titik B)",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tentukan terminal tujuan akhir perjalanan bus",
                fontSize = 12.sp,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                uiState.allTerminals.forEach { terminal ->
                    val isSelected = uiState.selectedDestination?.id == terminal.id
                    val isOrigin = uiState.selectedOrigin?.id == terminal.id

                    TerminalSelectCard(
                        terminal = terminal,
                        isSelected = isSelected,
                        isDisabled = isOrigin,
                        badgeLabel = if (isSelected) "Tujuan Akhir" else if (isOrigin) "Titik Awal" else null,
                        onClick = { if (!isOrigin) onSelectDestination(terminal) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (uiState.selectedOrigin != null && uiState.selectedDestination != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = IceBlue),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(PrimaryBlue))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Rencana Trayek",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlueDark
                            )
                            Text(
                                text = "${uiState.selectedOrigin.city} ➔ ${uiState.selectedDestination.city}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Text(
                                text = "${uiState.selectedOrigin.name} ke ${uiState.selectedDestination.name}",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PrimaryBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✓",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun TerminalSelectCard(
    terminal: Terminal,
    isSelected: Boolean,
    isDisabled: Boolean,
    badgeLabel: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(enabled = !isDisabled, onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> IceBlue
                isDisabled -> Color(0xFFF1F5F9)
                else -> SurfaceWhite
            }
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isSelected) PrimaryBlue else CardBorder
            )
        )
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
                        text = terminal.city,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDisabled) TextLight else TextDark
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${terminal.name}",
                        fontSize = 13.sp,
                        color = if (isDisabled) TextLight else TextMuted
                    )
                }
                Text(
                    text = "${terminal.type.displayName} • Retribusi: ${terminal.retributionFee.formatRupiah()}",
                    fontSize = 11.sp,
                    color = TextLight
                )
            }

            if (badgeLabel != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) PrimaryBlue else TextLight)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = badgeLabel,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
