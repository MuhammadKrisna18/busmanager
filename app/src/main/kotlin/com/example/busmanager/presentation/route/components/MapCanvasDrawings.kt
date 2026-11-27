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

fun DrawScope.drawEastJavaLandmass() {
    val path = Path().apply {
        moveTo(size.width * 0.02f, size.height * 0.12f)
        lineTo(size.width * 0.06f, size.height * 0.08f)
        lineTo(size.width * 0.14f, size.height * 0.09f)
        lineTo(size.width * 0.22f, size.height * 0.12f)
        lineTo(size.width * 0.34f, size.height * 0.13f)
        lineTo(size.width * 0.44f, size.height * 0.16f)
        lineTo(size.width * 0.49f, size.height * 0.24f)
        lineTo(size.width * 0.51f, size.height * 0.28f)
        lineTo(size.width * 0.53f, size.height * 0.35f)
        lineTo(size.width * 0.58f, size.height * 0.41f)
        lineTo(size.width * 0.66f, size.height * 0.46f)
        lineTo(size.width * 0.75f, size.height * 0.43f)
        lineTo(size.width * 0.85f, size.height * 0.43f)
        lineTo(size.width * 0.93f, size.height * 0.43f)
        lineTo(size.width * 0.97f, size.height * 0.50f)
        lineTo(size.width * 0.96f, size.height * 0.62f)
        lineTo(size.width * 0.96f, size.height * 0.76f)
        lineTo(size.width * 0.97f, size.height * 0.86f)
        lineTo(size.width * 0.94f, size.height * 0.90f)
        lineTo(size.width * 0.85f, size.height * 0.90f)
        lineTo(size.width * 0.74f, size.height * 0.90f)
        lineTo(size.width * 0.61f, size.height * 0.89f)
        lineTo(size.width * 0.48f, size.height * 0.88f)
        lineTo(size.width * 0.35f, size.height * 0.88f)
        lineTo(size.width * 0.24f, size.height * 0.88f)
        lineTo(size.width * 0.15f, size.height * 0.87f)
        lineTo(size.width * 0.06f, size.height * 0.86f)
        lineTo(size.width * 0.03f, size.height * 0.82f)
        lineTo(size.width * 0.02f, size.height * 0.70f)
        lineTo(size.width * 0.02f, size.height * 0.50f)
        lineTo(size.width * 0.02f, size.height * 0.35f)
        lineTo(size.width * 0.03f, size.height * 0.22f)
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
        moveTo(size.width * 0.48f, size.height * 0.19f)
        lineTo(size.width * 0.55f, size.height * 0.14f)
        lineTo(size.width * 0.68f, size.height * 0.13f)
        lineTo(size.width * 0.80f, size.height * 0.13f)
        lineTo(size.width * 0.88f, size.height * 0.15f)
        lineTo(size.width * 0.90f, size.height * 0.18f)
        lineTo(size.width * 0.84f, size.height * 0.20f)
        lineTo(size.width * 0.72f, size.height * 0.21f)
        lineTo(size.width * 0.58f, size.height * 0.21f)
        lineTo(size.width * 0.48f, size.height * 0.21f)
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
