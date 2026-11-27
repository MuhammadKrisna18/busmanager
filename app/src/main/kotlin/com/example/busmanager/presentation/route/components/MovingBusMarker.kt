package com.example.busmanager.presentation.route.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmanager.domain.simulation.model.ActiveBusTrip
import com.example.busmanager.ui.theme.AppDimens

@Composable
fun MovingBusMarker(
    trip: ActiveBusTrip,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isTracking: Boolean = false,
    zoomScale: Float = 1.0f
) {
    val busRadius = 14.dp
    val statusColor = when {
        trip.isDwellingAtTransit -> Color(0xFFF59E0B)
        trip.progressFraction <= 0.01f || trip.progressFraction >= 0.99f -> Color(0xFF6366F1)
        else -> Color(0xFF22C55E)
    }

    val labelScale = (1f / zoomScale).coerceIn(0.40f, 1.0f)

    Box(
        modifier = modifier.wrapContentSize(Alignment.Center, unbounded = true),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = -busRadius - 2.dp)
                .wrapContentSize(Alignment.BottomCenter, unbounded = true)
                .graphicsLayer {
                    scaleX = labelScale
                    scaleY = labelScale
                }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isTracking) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .shadow(4.dp, RoundedCornerShape(AppDimens.radiusSmall))
                                .clip(RoundedCornerShape(AppDimens.radiusSmall))
                                .background(Color.White)
                                .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(AppDimens.radiusSmall))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${trip.remainingMinutes} mnt",
                                        color = Color(0xFF1D4ED8),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "ke ${trip.currentDestinationName.removePrefix("Terminal ")}",
                                        color = Color(0xFF1E293B),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "${trip.passengerCount} Penumpang",
                                    color = Color(0xFF64748B),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(width = 4.dp, height = 2.dp)
                                .background(Color(0xFFBFDBFE))
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                }

                if (trip.isDwellingAtTransit) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(AppDimens.radiusPill))
                            .background(Color(0xFFD97706))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "Singgah: ${(trip.dwellingTerminalName ?: "Transit").removePrefix("Terminal ")}",
                            color = Color.White,
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(1.dp))
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(AppDimens.radiusPill))
                        .background(if (isTracking) Color(0xEE1E40AF) else Color(0xCC0F172A))
                        .border(0.5.dp, Color(0x44FFFFFF), RoundedCornerShape(AppDimens.radiusPill))
                        .padding(horizontal = if (isTracking) 6.dp else 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = if (isTracking) "[Lacak] ${trip.busCode}" else trip.busCode,
                        color = Color.White,
                        fontSize = if (isTracking) 8.5.sp else 7.5.sp,
                        fontWeight = FontWeight.Bold
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
                headingAngle = trip.headingAngle,
                statusColor = statusColor,
                isTracking = isTracking
            )
        }
    }
}
