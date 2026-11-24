package com.example.busmanager.presentation.route.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
    modifier: Modifier = Modifier
) {
    val busRadius = 11.dp
    val busDiameter = busRadius * 2

    Box(
        modifier = modifier.wrapContentSize(Alignment.Center, unbounded = true),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = -busRadius - 2.dp)
                .wrapContentSize(Alignment.BottomCenter, unbounded = true)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(AppDimens.radiusPill))
                    .background(Color(0xFF475569))
                    .padding(horizontal = 5.dp, vertical = 1.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🅿️ ${bus.busCode}",
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(busDiameter)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .shadow(3.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color(0xFF64748B))
                    .border(1.5.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🚌",
                    fontSize = 10.sp
                )
            }
        }
    }
}
