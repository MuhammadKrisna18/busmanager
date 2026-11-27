package com.example.busmanager.presentation.route.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.busmanager.domain.route.model.RoutePathType
import com.example.busmanager.domain.route.model.RouteSegment

fun DrawScope.drawJavaIslandLandmass() {
    val path = Path().apply {
        moveTo(size.width * 0.020f, size.height * 0.310f)
        lineTo(size.width * 0.066f, size.height * 0.220f)
        lineTo(size.width * 0.102f, size.height * 0.040f)
        lineTo(size.width * 0.120f, size.height * 0.070f)
        lineTo(size.width * 0.131f, size.height * 0.055f)
        lineTo(size.width * 0.189f, size.height * 0.097f)
        lineTo(size.width * 0.207f, size.height * 0.048f)
        lineTo(size.width * 0.240f, size.height * 0.058f)
        lineTo(size.width * 0.288f, size.height * 0.129f)
        lineTo(size.width * 0.342f, size.height * 0.161f)
        lineTo(size.width * 0.364f, size.height * 0.290f)
        lineTo(size.width * 0.421f, size.height * 0.332f)
        lineTo(size.width * 0.477f, size.height * 0.345f)
        lineTo(size.width * 0.553f, size.height * 0.371f)
        lineTo(size.width * 0.592f, size.height * 0.200f)
        lineTo(size.width * 0.647f, size.height * 0.290f)
        lineTo(size.width * 0.720f, size.height * 0.348f)
        lineTo(size.width * 0.770f, size.height * 0.339f)
        lineTo(size.width * 0.791f, size.height * 0.445f)
        lineTo(size.width * 0.810f, size.height * 0.510f)
        lineTo(size.width * 0.839f, size.height * 0.619f)
        lineTo(size.width * 0.916f, size.height * 0.606f)
        lineTo(size.width * 0.964f, size.height * 0.655f)
        lineTo(size.width * 0.959f, size.height * 0.755f)
        lineTo(size.width * 0.978f, size.height * 0.942f)
        lineTo(size.width * 0.947f, size.height * 0.910f)
        lineTo(size.width * 0.865f, size.height * 0.829f)
        lineTo(size.width * 0.829f, size.height * 0.800f)
        lineTo(size.width * 0.781f, size.height * 0.842f)
        lineTo(size.width * 0.729f, size.height * 0.813f)
        lineTo(size.width * 0.694f, size.height * 0.794f)
        lineTo(size.width * 0.686f, size.height * 0.800f)
        lineTo(size.width * 0.620f, size.height * 0.787f)
        lineTo(size.width * 0.566f, size.height * 0.752f)
        lineTo(size.width * 0.543f, size.height * 0.716f)
        lineTo(size.width * 0.517f, size.height * 0.684f)
        lineTo(size.width * 0.450f, size.height * 0.635f)
        lineTo(size.width * 0.410f, size.height * 0.626f)
        lineTo(size.width * 0.373f, size.height * 0.616f)
        lineTo(size.width * 0.274f, size.height * 0.600f)
        lineTo(size.width * 0.143f, size.height * 0.506f)
        lineTo(size.width * 0.155f, size.height * 0.381f)
        lineTo(size.width * 0.103f, size.height * 0.332f)
        close()
    }

    drawPath(
        path = path,
        color = Color(0xFFF2FBF6)
    )

    drawPath(
        path = path,
        color = Color(0xFFD1E7DD),
        style = Stroke(width = 1.8f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}

fun DrawScope.drawMaduraIsland() {
    val path = Path().apply {
        moveTo(size.width * 0.788f, size.height * 0.439f)
        lineTo(size.width * 0.801f, size.height * 0.355f)
        lineTo(size.width * 0.842f, size.height * 0.348f)
        lineTo(size.width * 0.896f, size.height * 0.345f)
        lineTo(size.width * 0.929f, size.height * 0.381f)
        lineTo(size.width * 0.910f, size.height * 0.403f)
        lineTo(size.width * 0.842f, size.height * 0.452f)
        close()
    }

    drawPath(
        path = path,
        color = Color(0xFFEAF5EE)
    )

    drawPath(
        path = path,
        color = Color(0xFFC7E2D5),
        style = Stroke(width = 1.4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}

fun DrawScope.drawBackgroundSegment(
    segment: RouteSegment,
    canvasWidth: Float,
    canvasHeight: Float
) {
    if (segment.waypoints.size < 2) return

    val path = Path().apply {
        val first = segment.waypoints.first()
        moveTo(first.x * canvasWidth, first.y * canvasHeight)
        for (i in 1 until segment.waypoints.size) {
            val wp = segment.waypoints[i]
            lineTo(wp.x * canvasWidth, wp.y * canvasHeight)
        }
    }

    if (segment.pathType == RoutePathType.TOLL) {
        drawPath(
            path = path,
            color = Color(0x33F59E0B),
            style = Stroke(
                width = 5.5f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
        drawPath(
            path = path,
            color = Color(0xFFF59E0B),
            style = Stroke(
                width = 2.8f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    } else {
        drawPath(
            path = path,
            color = Color(0x220284C7),
            style = Stroke(
                width = 4.5f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
        drawPath(
            path = path,
            color = Color(0xFF0284C7),
            style = Stroke(
                width = 2.0f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
            )
        )
    }
}

fun DrawScope.drawOpenedRouteSegment(
    segment: RouteSegment,
    routeColor: Color,
    canvasWidth: Float,
    canvasHeight: Float
) {
    if (segment.waypoints.size < 2) return

    val path = Path().apply {
        val first = segment.waypoints.first()
        moveTo(first.x * canvasWidth, first.y * canvasHeight)
        for (i in 1 until segment.waypoints.size) {
            val wp = segment.waypoints[i]
            lineTo(wp.x * canvasWidth, wp.y * canvasHeight)
        }
    }

    if (segment.pathType == RoutePathType.TOLL) {
        drawPath(
            path = path,
            color = Color(0x55F59E0B),
            style = Stroke(
                width = 12f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
        drawPath(
            path = path,
            color = Color(0xFFD97706),
            style = Stroke(
                width = 6.5f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
        drawPath(
            path = path,
            color = Color(0xFFFEF08A),
            style = Stroke(
                width = 2.5f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    } else {
        drawPath(
            path = path,
            color = Color(0x440284C7),
            style = Stroke(
                width = 11f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
        drawPath(
            path = path,
            color = Color(0xFF0284C7),
            style = Stroke(
                width = 5.5f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
        drawPath(
            path = path,
            color = Color(0xFF67E8F9),
            style = Stroke(
                width = 2.0f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}

fun DrawScope.drawGoogleMapsTrackedSegment(
    segment: RouteSegment,
    canvasWidth: Float,
    canvasHeight: Float
) {
    if (segment.waypoints.size < 2) return

    val path = Path().apply {
        val first = segment.waypoints.first()
        moveTo(first.x * canvasWidth, first.y * canvasHeight)
        for (i in 1 until segment.waypoints.size) {
            val wp = segment.waypoints[i]
            lineTo(wp.x * canvasWidth, wp.y * canvasHeight)
        }
    }

    if (segment.pathType == RoutePathType.TOLL) {
        drawPath(
            path = path,
            color = Color(0x66F59E0B),
            style = Stroke(
                width = 16f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
        drawPath(
            path = path,
            color = Color(0xFFB45309),
            style = Stroke(
                width = 8.5f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
        drawPath(
            path = path,
            color = Color(0xFFFDE047),
            style = Stroke(
                width = 3.5f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    } else {
        drawPath(
            path = path,
            color = Color(0x552563EB),
            style = Stroke(
                width = 16f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
        drawPath(
            path = path,
            color = Color(0xFF1D4ED8),
            style = Stroke(
                width = 8.5f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
        drawPath(
            path = path,
            color = Color(0xFF93C5FD),
            style = Stroke(
                width = 3.5f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}
