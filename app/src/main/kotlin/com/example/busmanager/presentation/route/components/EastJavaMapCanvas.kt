package com.example.busmanager.presentation.route.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.MapCoordinate
import com.example.busmanager.domain.route.model.RoutePathType
import com.example.busmanager.domain.route.model.RouteSegment
import com.example.busmanager.domain.route.model.Terminal
import com.example.busmanager.domain.simulation.model.ActiveBusTrip
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
    openedRoutes: List<BusRoute> = emptyList(),
    selectedRoute: BusRoute? = null,
    selectedOrigin: Terminal? = null,
    selectedDestination: Terminal? = null,
    transitTerminals: List<Terminal> = emptyList(),
    activeTrips: List<ActiveBusTrip> = emptyList(),
    parkedBuses: List<com.example.busmanager.presentation.simulation.ParkedBusInfo> = emptyList(),
    trackedTripId: String? = null,
    onTerminalClick: (Terminal) -> Unit = {},
    onTripClick: (ActiveBusTrip) -> Unit = {},
    onParkedBusClick: (com.example.busmanager.domain.fleet.model.Bus) -> Unit = {}
) {
    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(AppDimens.radiusLarge))
            .background(Color(0xFFE8F3FC))
            .border(1.dp, CardBorder, RoundedCornerShape(AppDimens.radiusLarge))
    ) {
        val canvasWidth = constraints.maxWidth.toFloat()
        val canvasHeight = constraints.maxHeight.toFloat()
        val trackedTrip = activeTrips.find { it.id == trackedTripId }
        val activeNavigationRoute = trackedTrip?.route ?: selectedRoute
        val activeNavigationSegmentIds = activeNavigationRoute?.segments?.map { it.id }?.toSet() ?: emptySet()

        val openedSegmentColorMap = mutableMapOf<String, Color>()
        openedRoutes.forEach { route ->
            val color = RouteColorPalette.getColorForRoute(route.id)
            route.segments.forEach { seg ->
                if (!openedSegmentColorMap.containsKey(seg.id)) {
                    openedSegmentColorMap[seg.id] = color
                }
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawEastJavaLandmass()
            drawMaduraIsland()

            allSegments.forEach { segment ->
                if (!activeNavigationSegmentIds.contains(segment.id)) {
                    val openedColor = openedSegmentColorMap[segment.id]
                    if (openedColor != null) {
                        drawOpenedRouteSegment(
                            segment = segment,
                            routeColor = openedColor,
                            canvasWidth = canvasWidth,
                            canvasHeight = canvasHeight
                        )
                    } else {
                        drawBackgroundSegment(
                            segment = segment,
                            canvasWidth = canvasWidth,
                            canvasHeight = canvasHeight
                        )
                    }
                }
            }

            activeNavigationRoute?.segments?.forEach { segment ->
                drawGoogleMapsTrackedSegment(
                    segment = segment,
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

        activeTrips.forEach { trip ->
            val isTracked = trip.id == trackedTripId
            val xPx = trip.currentCoordinate.x * canvasWidth
            val yPx = trip.currentCoordinate.y * canvasHeight

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(xPx.roundToInt(), yPx.roundToInt())
                    }
                    .size(0.dp)
                    .wrapContentSize(Alignment.Center, unbounded = true)
            ) {
                MovingBusMarker(
                    trip = trip,
                    isTracking = isTracked,
                    onClick = { onTripClick(trip) }
                )
            }
        }

        val parkedGroupedByTerminal = parkedBuses.groupBy { it.terminal.id }
        parkedGroupedByTerminal.forEach { (_, busesAtTerminal) ->
            busesAtTerminal.forEachIndexed { index, parked ->
                val baseXPx = parked.terminal.coordinate.x * canvasWidth
                val baseYPx = parked.terminal.coordinate.y * canvasHeight
                val offsetXPx = baseXPx + 20.dp.value + (index * 22.dp.value)
                val posYPx = baseYPx - 10.dp.value

                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(offsetXPx.roundToInt(), posYPx.roundToInt())
                        }
                        .size(0.dp)
                        .wrapContentSize(Alignment.Center, unbounded = true)
                ) {
                    ParkedBusMarker(
                        bus = parked.bus,
                        terminal = parked.terminal,
                        route = parked.route,
                        onClick = { onParkedBusClick(parked.bus) }
                    )
                }
            }
        }

        if (trackedTrip != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(AppDimens.paddingSmall)
                    .shadow(4.dp, RoundedCornerShape(AppDimens.radiusPill))
                    .clip(RoundedCornerShape(AppDimens.radiusPill))
                    .background(Color(0xFF1E293B))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF22C55E))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Melacak ${trackedTrip.busCode} • Sisa ${trackedTrip.remainingMinutes} mnt",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(AppDimens.paddingSmall),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .shadow(3.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable {
                        val firstTrip = activeTrips.firstOrNull()
                        if (firstTrip != null) onTripClick(firstTrip)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🧭", fontSize = 14.sp)
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .shadow(3.dp, CircleShape)
                    .clip(CircleShape)
                    .background(if (trackedTrip != null) Color(0xFF2563EB) else Color.White)
                    .clickable {
                        val target = trackedTrip ?: activeTrips.firstOrNull()
                        if (target != null) onTripClick(target)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🎯",
                    fontSize = 14.sp
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

private fun DrawScope.drawBackgroundSegment(
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
            color = Color(0xFFD6E4F0),
            style = Stroke(
                width = 2.5f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    } else {
        drawPath(
            path = path,
            color = Color(0xFFCBD5E1),
            style = Stroke(
                width = 1.8f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
            )
        )
    }
}

private fun DrawScope.drawOpenedRouteSegment(
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

    drawPath(
        path = path,
        color = routeColor.copy(alpha = 0.28f),
        style = Stroke(
            width = 8.5f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    drawPath(
        path = path,
        color = routeColor,
        style = Stroke(
            width = 4.5f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}

private fun DrawScope.drawGoogleMapsTrackedSegment(
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

    drawPath(
        path = path,
        color = Color(0x442563EB),
        style = Stroke(
            width = 14f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    drawPath(
        path = path,
        color = Color(0xFF1D4ED8),
        style = Stroke(
            width = 7.0f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    drawPath(
        path = path,
        color = Color(0xFF93C5FD),
        style = Stroke(
            width = 2.5f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}
