package com.example.busmanager.presentation.route.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import java.util.Locale

@Composable
fun BoxScope.MapControlsOverlay(
    trackedTrip: ActiveBusTrip?,
    isTrackingLocked: Boolean,
    currentZoom: Float,
    enableZoomAndPan: Boolean,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onTrackOrCenter: () -> Unit,
    onResetMap: () -> Unit
) {
    if (trackedTrip != null) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 116.dp, start = AppDimens.paddingSmall)
                .shadow(4.dp, RoundedCornerShape(AppDimens.radiusPill))
                .clip(RoundedCornerShape(AppDimens.radiusPill))
                .background(Color(0xFF1E293B))
                .clickable { onTrackOrCenter() }
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isTrackingLocked) Color(0xFF22C55E) else Color(0xFFF59E0B))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isTrackingLocked) {
                        "Melacak ${trackedTrip.busCode} • Sisa ${trackedTrip.remainingMinutes} mnt"
                    } else {
                        "Bebas • Klik untuk mengunci ${trackedTrip.busCode}"
                    },
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (enableZoomAndPan) {
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = AppDimens.paddingSmall),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .shadow(3.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable { onZoomIn() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryBlue
                )
            }

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .shadow(3.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable { onZoomOut() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "−",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryBlue
                )
            }

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .shadow(3.dp, CircleShape)
                    .clip(CircleShape)
                    .background(if (trackedTrip != null && isTrackingLocked) PrimaryBlue else Color.White)
                    .clickable { onTrackOrCenter() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🎯",
                    fontSize = 14.sp
                )
            }

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .shadow(3.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable { onResetMap() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "↺",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF475569)
                )
            }

            if (currentZoom > 1.05f) {
                Box(
                    modifier = Modifier
                        .shadow(2.dp, RoundedCornerShape(AppDimens.radiusPill))
                        .clip(RoundedCornerShape(AppDimens.radiusPill))
                        .background(Color(0xCC0F172A))
                        .clickable { onResetMap() }
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    val zoomFormatted = String.format(Locale.US, "%.1fx", currentZoom)
                    Text(
                        text = zoomFormatted,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
