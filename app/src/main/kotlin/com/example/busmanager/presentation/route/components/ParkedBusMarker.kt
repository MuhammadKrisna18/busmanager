package com.example.busmanager.presentation.route.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.Terminal
import com.example.busmanager.ui.theme.AppDimens

@Composable
fun ParkedBusMarker(
    bus: Bus,
    terminal: Terminal,
    route: BusRoute?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    zoomScale: Float = 1.0f
) {
    val busRadius = 11.dp
    val labelScale = (1f / zoomScale).coerceIn(0.40f, 1.0f)

    Box(
        modifier = modifier.wrapContentSize(Alignment.Center, unbounded = true),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = -busRadius - 1.dp)
                .wrapContentSize(Alignment.BottomCenter, unbounded = true)
                .graphicsLayer {
                    scaleX = labelScale
                    scaleY = labelScale
                }
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(AppDimens.radiusPill))
                    .background(Color(0xBB334155))
                    .border(0.5.dp, Color(0x33FFFFFF), RoundedCornerShape(AppDimens.radiusPill))
                    .padding(horizontal = 3.5.dp, vertical = 0.5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = bus.busCode,
                        color = Color.White,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            IsometricBus25D(
                headingAngle = 0f,
                statusColor = Color(0xFF6366F1),
                isTracking = false
            )
        }
    }
}
