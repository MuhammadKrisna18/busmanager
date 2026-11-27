package com.example.busmanager.presentation.route.wizard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import com.example.busmanager.domain.route.model.TerminalDepartureSlot
import com.example.busmanager.ui.theme.CardBorder
import com.example.busmanager.ui.theme.PrimaryBlue
import com.example.busmanager.ui.theme.SurfaceWhite
import com.example.busmanager.ui.theme.TextDark

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DepartureSlotGrid(
    slots: List<TerminalDepartureSlot>,
    selectedSlot: TerminalDepartureSlot?,
    onSelectSlot: (TerminalDepartureSlot) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        slots.forEach { slot ->
            val isSelected = selectedSlot?.slotId == slot.slotId
            val bg = when {
                isSelected -> PrimaryBlue
                !slot.isAvailable -> Color(0xFFFEE2E2)
                else -> SurfaceWhite
            }
            val textCol = when {
                isSelected -> Color.White
                !slot.isAvailable -> Color(0xFF991B1B)
                else -> TextDark
            }
            val borderCol = when {
                isSelected -> PrimaryBlue
                !slot.isAvailable -> Color(0xFFFCA5A5)
                else -> CardBorder
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(bg)
                    .border(1.dp, borderCol, RoundedCornerShape(8.dp))
                    .clickable { onSelectSlot(slot) }
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (isSelected) {
                        Text(
                            text = "✓",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Text(
                        text = slot.timeFormatted,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = textCol
                    )
                    if (!slot.isAvailable) {
                        val occupiedLabel = slot.occupiedDestinationCity?.let { "Arah $it" } ?: "Terisi"
                        Text(
                            text = occupiedLabel,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFDC2626)
                        )
                    }
                }
            }
        }
    }
}
