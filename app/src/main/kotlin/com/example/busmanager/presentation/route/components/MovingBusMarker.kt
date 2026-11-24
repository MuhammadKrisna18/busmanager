package com.example.busmanager.presentation.route.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import com.example.busmanager.domain.simulation.model.ActiveBusTrip
import com.example.busmanager.ui.theme.AppDimens
import com.example.busmanager.ui.theme.PrimaryBlue
import com.example.busmanager.ui.theme.PrimaryBlueDark

@Composable
fun MovingBusMarker(
    trip: ActiveBusTrip,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isTracking: Boolean = false
) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isTracking) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .shadow(6.dp, RoundedCornerShape(AppDimens.radiusMedium))
                        .clip(RoundedCornerShape(AppDimens.radiusMedium))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(AppDimens.radiusMedium))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "⏱️ ${trip.remainingMinutes} mnt",
                                color = Color(0xFF1D4ED8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "➔ ${trip.currentDestinationName}",
                                color = Color(0xFF1E293B),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "👥 ${trip.passengerCount} pnp • ${trip.ticketEarnings.formatCompact()}",
                            color = Color(0xFF64748B),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(width = 6.dp, height = 3.dp)
                        .background(Color(0xFFBFDBFE))
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(AppDimens.radiusPill))
                .background(if (isTracking) Color(0xFF1E40AF) else PrimaryBlueDark)
                .padding(horizontal = if (isTracking) 8.dp else 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = if (isTracking) "🎯 ${trip.busCode}" else trip.busCode,
                color = Color.White,
                fontSize = if (isTracking) 10.sp else 9.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Box(
            modifier = Modifier.size(if (isTracking) 38.dp else 24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isTracking) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Color(0x332563EB))
                )
            }
            Box(
                modifier = Modifier
                    .size(if (isTracking) 30.dp else 24.dp)
                    .shadow(if (isTracking) 8.dp else 4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(if (isTracking) Color(0xFF2563EB) else PrimaryBlue)
                    .border(if (isTracking) 2.5.dp else 2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🚌",
                    fontSize = if (isTracking) 15.sp else 12.sp
                )
            }
        }
    }
}
