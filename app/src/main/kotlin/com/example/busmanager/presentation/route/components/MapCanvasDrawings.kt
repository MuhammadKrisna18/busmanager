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
        moveTo(size.width * 0.02f, size.height * 0.06f)
        lineTo(size.width * 0.08f, size.height * 0.07f)
        lineTo(size.width * 0.18f, size.height * 0.10f)
        lineTo(size.width * 0.36f, size.height * 0.12f)
        lineTo(size.width * 0.50f, size.height * 0.22f)
        lineTo(size.width * 0.54f, size.height * 0.40f)
        lineTo(size.width * 0.64f, size.height * 0.46f)
        lineTo(size.width * 0.78f, size.height * 0.42f)
        lineTo(size.width * 0.86f, size.height * 0.44f)
        lineTo(size.width * 0.97f, size.height * 0.58f)
        lineTo(size.width * 0.98f, size.height * 0.78f)
        lineTo(size.width * 0.96f, size.height * 0.86f)
        lineTo(size.width * 0.88f, size.height * 0.90f)
        lineTo(size.width * 0.74f, size.height * 0.90f)
        lineTo(size.width * 0.61f, size.height * 0.89f)
        lineTo(size.width * 0.48f, size.height * 0.88f)
        lineTo(size.width * 0.32f, size.height * 0.88f)
        lineTo(size.width * 0.20f, size.height * 0.87f)
        lineTo(size.width * 0.14f, size.height * 0.86f)
        lineTo(size.width * 0.04f, size.height * 0.86f)
        lineTo(size.width * 0.02f, size.height * 0.75f)
        lineTo(size.width * 0.02f, size.height * 0.50f)
        lineTo(size.width * 0.02f, size.height * 0.32f)
        lineTo(size.width * 0.02f, size.height * 0.18f)
        close()
    }

    drawPath(
        path = path,
        color = Color(0xFFF2FBF6)
    )

    drawPath(
        path = path,
        color = Color(0xFFD1E7DD),
        style = Stroke(width = 1.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}

fun DrawScope.drawMaduraIsland() {
    val path = Path().apply {
        moveTo(size.width * 0.45f, size.height * 0.08f)
        lineTo(size.width * 0.76f, size.height * 0.07f)
        lineTo(size.width * 0.78f, size.height * 0.16f)
        lineTo(size.width * 0.60f, size.height * 0.18f)
        lineTo(size.width * 0.45f, size.height * 0.16f)
        close()
    }

    drawPath(
        path = path,
        color = Color(0xFFEAF5EE)
    )

    drawPath(
        path = path,
        color = Color(0xFFC7E2D5),
        style = Stroke(width = 1.2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
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
