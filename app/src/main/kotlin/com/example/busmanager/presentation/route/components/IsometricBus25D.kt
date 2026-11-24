package com.example.busmanager.presentation.route.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp

@Composable
fun IsometricBus25D(
    headingAngle: Float,
    statusColor: Color,
    isTracking: Boolean = false,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier.size(34.dp)
    ) {
        val cx = size.width / 2f
        val cy = size.height / 2f

        drawCircle(
            color = statusColor.copy(alpha = 0.22f),
            radius = cx - 1f,
            center = Offset(cx, cy)
        )

        drawCircle(
            color = statusColor,
            radius = cx - 2.5f,
            center = Offset(cx, cy),
            style = Stroke(width = if (isTracking) 2.6f else 2.0f)
        )

        if (isTracking) {
            drawCircle(
                color = Color(0x442563EB),
                radius = cx + 2f,
                center = Offset(cx, cy),
                style = Stroke(width = 1.8f)
            )
        }

        withTransform({
            rotate(degrees = headingAngle, pivot = Offset(cx, cy))
        }) {
            drawRoundRect(
                color = Color(0x35000000),
                topLeft = Offset(cx - 5.5f, cy - 9.5f),
                size = Size(13f, 23f),
                cornerRadius = CornerRadius(4f, 4f)
            )

            drawRoundRect(
                color = Color(0xFF0F172A),
                topLeft = Offset(cx - 5.5f, cy - 11.5f),
                size = Size(11f, 3f),
                cornerRadius = CornerRadius(1.5f, 1.5f)
            )
            drawRoundRect(
                color = Color(0xFF0F172A),
                topLeft = Offset(cx - 5.5f, cy + 9.5f),
                size = Size(11f, 3f),
                cornerRadius = CornerRadius(1.5f, 1.5f)
            )

            drawRoundRect(
                color = Color(0xFF1D4ED8),
                topLeft = Offset(cx - 5.5f, cy - 10.5f),
                size = Size(11f, 21f),
                cornerRadius = CornerRadius(3f, 3f)
            )

            drawRect(
                color = Color(0xFFE2E8F0),
                topLeft = Offset(cx - 5.5f, cy - 7f),
                size = Size(1f, 14f)
            )
            drawRect(
                color = Color(0xFFE2E8F0),
                topLeft = Offset(cx + 4.5f, cy - 7f),
                size = Size(1f, 14f)
            )

            drawRoundRect(
                color = Color(0xFFDBEAFE),
                topLeft = Offset(cx - 4.5f, cy - 8.5f),
                size = Size(9f, 17f),
                cornerRadius = CornerRadius(2.5f, 2.5f)
            )

            drawRoundRect(
                color = Color(0xFFF1F5F9),
                topLeft = Offset(cx - 2.5f, cy - 2f),
                size = Size(5f, 5.5f),
                cornerRadius = CornerRadius(1.2f, 1.2f)
            )
            drawLine(
                color = Color(0xFF94A3B8),
                start = Offset(cx - 1.8f, cy - 0.2f),
                end = Offset(cx + 1.8f, cy - 0.2f),
                strokeWidth = 0.8f
            )
            drawLine(
                color = Color(0xFF94A3B8),
                start = Offset(cx - 1.8f, cy + 1.5f),
                end = Offset(cx + 1.8f, cy + 1.5f),
                strokeWidth = 0.8f
            )

            drawRoundRect(
                color = Color(0xFF0369A1),
                topLeft = Offset(cx - 4f, cy - 9.5f),
                size = Size(8f, 3.2f),
                cornerRadius = CornerRadius(1.8f, 1.8f)
            )
            drawCircle(
                color = Color(0xCCFFFFFF),
                radius = 0.7f,
                center = Offset(cx - 2.2f, cy - 8.2f)
            )

            drawRect(
                color = Color(0xFF0F172A),
                topLeft = Offset(cx - 4.8f, cy - 5.5f),
                size = Size(0.8f, 12f)
            )
            drawRect(
                color = Color(0xFF0F172A),
                topLeft = Offset(cx + 4f, cy - 5.5f),
                size = Size(0.8f, 12f)
            )

            drawRoundRect(
                color = Color(0xFF1E293B),
                topLeft = Offset(cx - 3.8f, cy + 7.5f),
                size = Size(7.6f, 1.8f),
                cornerRadius = CornerRadius(1f, 1f)
            )

            drawCircle(
                color = Color(0xFFFEF08A),
                radius = 1.0f,
                center = Offset(cx - 3.8f, cy - 10.8f)
            )
            drawCircle(
                color = Color(0xFFFEF08A),
                radius = 1.0f,
                center = Offset(cx + 3.8f, cy - 10.8f)
            )

            drawCircle(
                color = Color(0xFFEF4444),
                radius = 0.9f,
                center = Offset(cx - 3.8f, cy + 10.8f)
            )
            drawCircle(
                color = Color(0xFFEF4444),
                radius = 0.9f,
                center = Offset(cx + 3.8f, cy + 10.8f)
            )
        }
    }
}
