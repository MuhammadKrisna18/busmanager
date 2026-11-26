package com.example.busmanager.presentation.route.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import com.example.busmanager.domain.route.model.Terminal
import com.example.busmanager.ui.theme.AppDimens
import com.example.busmanager.ui.theme.PrimaryBlue

@Composable
fun BoxScope.MapCorridorSelector(
    availablePaths: List<List<Terminal>>,
    selectedPathIndex: Int,
    onSelectPath: (Int) -> Unit
) {
    if (availablePaths.size <= 1) return

    val corridorScrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .align(Alignment.TopStart)
            .padding(start = AppDimens.paddingSmall, top = AppDimens.paddingSmall, end = 56.dp)
            .horizontalScroll(corridorScrollState)
            .background(Color(0xEE0F172A), RoundedCornerShape(10.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        availablePaths.forEachIndexed { index, path ->
            val isSelected = index == selectedPathIndex
            val viaCity = if (path.size > 2) {
                path.subList(1, path.size - 1).joinToString(", ") { it.city }
            } else {
                "Langsung"
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) PrimaryBlue else Color(0x33FFFFFF))
                    .clickable { onSelectPath(index) }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = if (isSelected) "✓ Koridor ${index + 1}: $viaCity" else "Koridor ${index + 1}: $viaCity",
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = Color.White,
                    maxLines = 1
                )
            }
        }
    }
}
