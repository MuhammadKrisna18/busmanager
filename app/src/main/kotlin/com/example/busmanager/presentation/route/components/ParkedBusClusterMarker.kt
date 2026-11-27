package com.example.busmanager.presentation.route.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmanager.domain.route.model.Terminal
import com.example.busmanager.ui.theme.AppDimens

@Composable
fun ParkedBusClusterMarker(
    terminal: Terminal,
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    zoomScale: Float = 1.0f
) {
    val labelScale = (1f / zoomScale).coerceIn(0.40f, 1.0f)

    Box(
        modifier = modifier
            .wrapContentSize(Alignment.Center, unbounded = true)
            .graphicsLayer {
                scaleX = labelScale
                scaleY = labelScale
            }
            .shadow(3.dp, RoundedCornerShape(AppDimens.radiusPill))
            .clip(RoundedCornerShape(AppDimens.radiusPill))
            .background(Color(0xF01E293B))
            .border(1.dp, Color(0xFF6366F1), RoundedCornerShape(AppDimens.radiusPill))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF6366F1)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "P",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$count Bus",
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
