package com.example.busmanager.presentation.route.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(AppDimens.radiusPill))
                .background(if (isTracking) Color(0xFFDC2626) else PrimaryBlueDark)
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
            modifier = Modifier
                .size(if (isTracking) 32.dp else 24.dp)
                .shadow(if (isTracking) 10.dp else 6.dp, CircleShape)
                .clip(CircleShape)
                .background(if (isTracking) Color(0xFFF59E0B) else PrimaryBlue)
                .border(if (isTracking) 3.dp else 2.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🚌",
                fontSize = if (isTracking) 16.sp else 12.sp
            )
        }
    }
}
