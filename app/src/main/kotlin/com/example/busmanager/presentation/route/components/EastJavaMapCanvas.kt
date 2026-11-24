package com.example.busmanager.presentation.route.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.MapCoordinate
import com.example.busmanager.domain.route.model.RoutePathType
import com.example.busmanager.domain.route.model.RouteSegment
import com.example.busmanager.domain.route.model.Terminal
import com.example.busmanager.ui.theme.AppDimens
import com.example.busmanager.ui.theme.CardBorder
import com.example.busmanager.ui.theme.IceBlue
import com.example.busmanager.ui.theme.PrimaryBlue
import com.example.busmanager.ui.theme.SkyBlue
import kotlin.math.roundToInt

@Composable
fun EastJavaMapCanvas(
    terminals: List<Terminal>,
    allSegments: List<RouteSegment>,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(290.dp),
    selectedRoute: BusRoute? = null,
    selectedOrigin: Terminal? = null,
    selectedDestination: Terminal? = null,
    transitTerminals: List<Terminal> = emptyList(),
    onTerminalClick: (Terminal) -> Unit = {}
) {
    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(AppDimens.radiusLarge))
            .background(Color(0xFFE8F3FC))
            .border(1.dp, CardBorder, RoundedCornerShape(AppDimens.radiusLarge))
    ) {
        val canvasWidth = constraints.maxWidth.toFloat()
        val canvasHeight = constraints.maxHeight.toFloat()

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawEastJavaLandmass()
            drawMaduraIsland()

            val selectedSegmentIds = selectedRoute?.segments?.map { it.id }?.toSet() ?: emptySet()

            allSegments.forEach { segment ->
                if (!selectedSegmentIds.contains(segment.id)) {
                    drawRoadSegment(
                        segment = segment,
                        isSelected = false,
                        canvasWidth = canvasWidth,
                        canvasHeight = canvasHeight
                    )
                }
            }

            selectedRoute?.segments?.forEach { segment ->
                drawRoadSegment(
                    segment = segment,
                    isSelected = true,
                    canvasWidth = canvasWidth,
                    canvasHeight = canvasHeight
                )
            }
        }

        terminals.forEach { terminal ->
            val role = when {
                selectedOrigin?.id == terminal.id -> TerminalNodeRole.ORIGIN
                selectedDestination?.id == terminal.id -> TerminalNodeRole.DESTINATION
                transitTerminals.any { it.id == terminal.id } -> TerminalNodeRole.TRANSIT
                else -> TerminalNodeRole.DEFAULT
            }

            val xPx = (terminal.coordinate.x * canvasWidth) - 24.dp.value
            val yPx = (terminal.coordinate.y * canvasHeight) - 28.dp.value

            Box(
                modifier = Modifier.offset {
                    IntOffset(xPx.roundToInt(), yPx.roundToInt())
                }
            ) {
                TerminalMarker(
                    terminal = terminal,
                    role = role,
                    onClick = { onTerminalClick(terminal) }
                )
            }
        }
    }
}

private fun DrawScope.drawEastJavaLandmass() {
    val path = Path().apply {
        moveTo(size.width * 0.05f, size.height * 0.12f)
        lineTo(size.width * 0.18f, size.height * 0.14f)
        lineTo(size.width * 0.25f, size.height * 0.24f)
        lineTo(size.width * 0.35f, size.height * 0.33f)
        lineTo(size.width * 0.46f, size.height * 0.37f)
        lineTo(size.width * 0.65f, size.height * 0.34f)
        lineTo(size.width * 0.78f, size.height * 0.34f)
        lineTo(size.width * 0.88f, size.height * 0.36f)
        lineTo(size.width * 0.95f, size.height * 0.44f)
        lineTo(size.width * 0.96f, size.height * 0.65f)
        lineTo(size.width * 0.93f, size.height * 0.88f)
        lineTo(size.width * 0.84f, size.height * 0.82f)
        lineTo(size.width * 0.66f, size.height * 0.82f)
        lineTo(size.width * 0.48f, size.height * 0.80f)
        lineTo(size.width * 0.28f, size.height * 0.82f)
        lineTo(size.width * 0.05f, size.height * 0.78f)
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

private fun DrawScope.drawMaduraIsland() {
    val path = Path().apply {
        moveTo(size.width * 0.28f, size.height * 0.05f)
        lineTo(size.width * 0.68f, size.height * 0.04f)
        lineTo(size.width * 0.74f, size.height * 0.12f)
        lineTo(size.width * 0.52f, size.height * 0.15f)
        lineTo(size.width * 0.28f, size.height * 0.13f)
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

private fun DrawScope.drawRoadSegment(
    segment: RouteSegment,
    isSelected: Boolean,
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

    if (isSelected) {
        drawPath(
            path = path,
            color = if (segment.pathType == RoutePathType.TOLL) Color(0x660F52BA) else Color(0x6638BDF8),
            style = Stroke(
                width = 10f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        drawPath(
            path = path,
            color = if (segment.pathType == RoutePathType.TOLL) PrimaryBlue else SkyBlue,
            style = Stroke(
                width = 5.5f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    } else {
        if (segment.pathType == RoutePathType.TOLL) {
            drawPath(
                path = path,
                color = Color(0xFFB9D7F9),
                style = Stroke(
                    width = 2.8f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        } else {
            drawPath(
                path = path,
                color = Color(0xFFCBD5E1),
                style = Stroke(
                    width = 2.0f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
                )
            )
        }
    }
}
