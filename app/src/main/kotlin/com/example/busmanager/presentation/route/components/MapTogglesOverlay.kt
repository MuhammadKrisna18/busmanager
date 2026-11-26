package com.example.busmanager.presentation.route.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmanager.domain.route.model.RoutePathType
import com.example.busmanager.ui.theme.AppDimens
import kotlin.math.roundToInt

@Composable
fun MapSegmentTogglesOverlay(
    segmentToggles: List<MapSegmentToggle>,
    canvasWidth: Float,
    canvasHeight: Float,
    onSegmentToggleClick: (fromId: String, toId: String) -> Unit
) {
    segmentToggles.forEach { toggle ->
        val xPx = (toggle.midCoordinate.x * canvasWidth) - 36.dp.value
        val yPx = (toggle.midCoordinate.y * canvasHeight) - 12.dp.value
        val isToll = toggle.currentPathType == RoutePathType.TOLL

        Box(
            modifier = Modifier
                .offset {
                    IntOffset(xPx.roundToInt(), yPx.roundToInt())
                }
                .shadow(4.dp, RoundedCornerShape(AppDimens.radiusPill))
                .clip(RoundedCornerShape(AppDimens.radiusPill))
                .background(if (isToll) Color(0xFFD97706) else Color(0xFF0F766E))
                .border(1.5.dp, Color.White, RoundedCornerShape(AppDimens.radiusPill))
                .clickable { onSegmentToggleClick(toggle.fromTerminalId, toggle.toTerminalId) }
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = if (isToll) "🛣️ Tol" else "🚗 Arteri",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun MapTerminalTogglesOverlay(
    terminalToggles: List<MapTerminalToggle>,
    canvasWidth: Float,
    canvasHeight: Float,
    onTerminalToggleClick: (terminalId: String) -> Unit
) {
    terminalToggles.forEach { toggle ->
        val xPx = (toggle.coordinate.x * canvasWidth) - 36.dp.value
        val yPx = (toggle.coordinate.y * canvasHeight) - 46.dp.value

        Box(
            modifier = Modifier
                .offset {
                    IntOffset(xPx.roundToInt(), yPx.roundToInt())
                }
                .shadow(4.dp, RoundedCornerShape(AppDimens.radiusPill))
                .clip(RoundedCornerShape(AppDimens.radiusPill))
                .background(if (toggle.isTransitActive) Color(0xFF2563EB) else Color(0xFF64748B))
                .border(1.5.dp, Color.White, RoundedCornerShape(AppDimens.radiusPill))
                .clickable { onTerminalToggleClick(toggle.terminalId) }
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = if (toggle.isTransitActive) "🚏 Singgah" else "⚡ Lewat",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
