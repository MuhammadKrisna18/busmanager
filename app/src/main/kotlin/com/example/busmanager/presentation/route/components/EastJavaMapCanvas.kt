package com.example.busmanager.presentation.route.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
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
import kotlinx.coroutines.launch
import java.util.Locale
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
    enableZoomAndPan: Boolean = true,
    onTerminalClick: (Terminal) -> Unit = {},
    onTripClick: (ActiveBusTrip) -> Unit = {},
    onParkedBusClick: (com.example.busmanager.domain.fleet.model.Bus) -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val zoomAnim = remember { Animatable(1.0f) }
    val panXAnim = remember { Animatable(0f) }
    val panYAnim = remember { Animatable(0f) }
    var isTrackingLocked by remember { mutableStateOf(trackedTripId != null) }

    LaunchedEffect(trackedTripId) {
        if (trackedTripId != null) {
            isTrackingLocked = true
        }
    }

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

        val currentZoom = zoomAnim.value
        val currentPanX = panXAnim.value
        val currentPanY = panYAnim.value

        LaunchedEffect(trackedTrip?.currentCoordinate, isTrackingLocked, canvasWidth, canvasHeight) {
            if (isTrackingLocked && trackedTrip != null && canvasWidth > 0f && canvasHeight > 0f) {
                val targetScale = currentZoom.coerceAtLeast(2.0f)
                val maxPx = (canvasWidth * (targetScale - 1f)) / 2f
                val maxPy = (canvasHeight * (targetScale - 1f)) / 2f
                val targetPanX = ((0.5f - trackedTrip.currentCoordinate.x) * canvasWidth * targetScale).coerceIn(-maxPx, maxPx)
                val targetPanY = ((0.5f - trackedTrip.currentCoordinate.y) * canvasHeight * targetScale).coerceIn(-maxPy, maxPy)
                launch { zoomAnim.animateTo(targetScale, animationSpec = tween(500)) }
                launch { panXAnim.animateTo(targetPanX, animationSpec = tween(500)) }
                launch { panYAnim.animateTo(targetPanY, animationSpec = tween(500)) }
            }
        }

        fun zoomIn() {
            val newZoom = (zoomAnim.value + 0.5f).coerceAtMost(4.0f)
            val maxPx = (canvasWidth * (newZoom - 1f)) / 2f
            val maxPy = (canvasHeight * (newZoom - 1f)) / 2f
            val newPanX = panXAnim.value.coerceIn(-maxPx, maxPx)
            val newPanY = panYAnim.value.coerceIn(-maxPy, maxPy)
            coroutineScope.launch {
                launch { zoomAnim.animateTo(newZoom, animationSpec = tween(250)) }
                launch { panXAnim.animateTo(newPanX, animationSpec = tween(250)) }
                launch { panYAnim.animateTo(newPanY, animationSpec = tween(250)) }
            }
        }

        fun zoomOut() {
            val newZoom = (zoomAnim.value - 0.5f).coerceAtLeast(1.0f)
            val maxPx = (canvasWidth * (newZoom - 1f)) / 2f
            val maxPy = (canvasHeight * (newZoom - 1f)) / 2f
            val newPanX = if (newZoom <= 1.0f) 0f else panXAnim.value.coerceIn(-maxPx, maxPx)
            val newPanY = if (newZoom <= 1.0f) 0f else panYAnim.value.coerceIn(-maxPy, maxPy)
            coroutineScope.launch {
                launch { zoomAnim.animateTo(newZoom, animationSpec = tween(250)) }
                launch { panXAnim.animateTo(newPanX, animationSpec = tween(250)) }
                launch { panYAnim.animateTo(newPanY, animationSpec = tween(250)) }
            }
        }

        fun resetMap() {
            isTrackingLocked = false
            coroutineScope.launch {
                launch { zoomAnim.animateTo(1.0f, animationSpec = tween(300)) }
                launch { panXAnim.animateTo(0f, animationSpec = tween(300)) }
                launch { panYAnim.animateTo(0f, animationSpec = tween(300)) }
            }
        }

        fun trackOrCenter() {
            val targetTrip = trackedTrip ?: activeTrips.firstOrNull()
            if (targetTrip != null) {
                if (targetTrip.id != trackedTripId) {
                    onTripClick(targetTrip)
                }
                isTrackingLocked = true
                val targetScale = zoomAnim.value.coerceAtLeast(2.0f)
                val maxPx = (canvasWidth * (targetScale - 1f)) / 2f
                val maxPy = (canvasHeight * (targetScale - 1f)) / 2f
                val tPanX = ((0.5f - targetTrip.currentCoordinate.x) * canvasWidth * targetScale).coerceIn(-maxPx, maxPx)
                val tPanY = ((0.5f - targetTrip.currentCoordinate.y) * canvasHeight * targetScale).coerceIn(-maxPy, maxPy)
                coroutineScope.launch {
                    launch { zoomAnim.animateTo(targetScale, animationSpec = tween(500)) }
                    launch { panXAnim.animateTo(tPanX, animationSpec = tween(500)) }
                    launch { panYAnim.animateTo(tPanY, animationSpec = tween(500)) }
                }
            } else {
                resetMap()
            }
        }

        val openedSegmentColorMap = mutableMapOf<String, Color>()
        openedRoutes.forEach { route ->
            val color = RouteColorPalette.getColorForRoute(route.id)
            route.segments.forEach { seg ->
                if (!openedSegmentColorMap.containsKey(seg.id)) {
                    openedSegmentColorMap[seg.id] = color
                }
            }
        }

        val contentModifier = if (enableZoomAndPan) {
            Modifier
                .fillMaxSize()
                .clipToBounds()
                .pointerInput(canvasWidth, canvasHeight) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        isTrackingLocked = false
                        val newZoom = (zoomAnim.value * zoom).coerceIn(1.0f, 4.0f)
                        val maxPanX = (canvasWidth * (newZoom - 1f)) / 2f
                        val maxPanY = (canvasHeight * (newZoom - 1f)) / 2f
                        val newPanX = (panXAnim.value + pan.x).coerceIn(-maxPanX, maxPanX)
                        val newPanY = (panYAnim.value + pan.y).coerceIn(-maxPanY, maxPanY)
                        coroutineScope.launch {
                            zoomAnim.snapTo(newZoom)
                            panXAnim.snapTo(if (newZoom <= 1.0f) 0f else newPanX)
                            panYAnim.snapTo(if (newZoom <= 1.0f) 0f else newPanY)
                        }
                    }
                }
                .graphicsLayer {
                    scaleX = currentZoom
                    scaleY = currentZoom
                    translationX = currentPanX
                    translationY = currentPanY
                }
        } else {
            Modifier.fillMaxSize()
        }

        Box(modifier = contentModifier) {
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

            val tripOffsets = remember(activeTrips, canvasWidth, canvasHeight) {
                BusMarkerAntiOverlapCalculator.calculateOffsets(
                    items = activeTrips,
                    coordinateSelector = { it.currentCoordinate },
                    canvasWidth = canvasWidth,
                    canvasHeight = canvasHeight
                )
            }

            activeTrips.forEach { trip ->
                val isTracked = trip.id == trackedTripId
                val offset = tripOffsets[trip] ?: DisplayMarkerOffset()
                val xPx = (trip.currentCoordinate.x * canvasWidth) + offset.dxPx
                val yPx = (trip.currentCoordinate.y * canvasHeight) + offset.dyPx

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
        }

        if (trackedTrip != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(AppDimens.paddingSmall)
                    .shadow(4.dp, RoundedCornerShape(AppDimens.radiusPill))
                    .clip(RoundedCornerShape(AppDimens.radiusPill))
                    .background(Color(0xFF1E293B))
                    .clickable { trackOrCenter() }
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
                    .align(Alignment.TopEnd)
                    .padding(AppDimens.paddingSmall),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .shadow(3.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { zoomIn() },
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
                        .clickable { zoomOut() },
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
                        .clickable { trackOrCenter() },
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
                        .clickable { resetMap() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "↺",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569)
                    )
                }
            }

            if (currentZoom > 1.05f) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(AppDimens.paddingSmall)
                        .shadow(2.dp, RoundedCornerShape(AppDimens.radiusPill))
                        .clip(RoundedCornerShape(AppDimens.radiusPill))
                        .background(Color(0xCC0F172A))
                        .clickable { resetMap() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    val zoomFormatted = String.format(Locale.US, "%.1fx", currentZoom)
                    Text(
                        text = "$zoomFormatted • ↺",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawEastJavaLandmass() {
    val path = Path().apply {
        moveTo(size.width * 0.05f, size.height * 0.22f)
        lineTo(size.width * 0.18f, size.height * 0.10f)
        lineTo(size.width * 0.36f, size.height * 0.12f)
        lineTo(size.width * 0.50f, size.height * 0.22f)
        lineTo(size.width * 0.54f, size.height * 0.40f)
        lineTo(size.width * 0.64f, size.height * 0.46f)
        lineTo(size.width * 0.78f, size.height * 0.42f)
        lineTo(size.width * 0.86f, size.height * 0.44f)
        lineTo(size.width * 0.97f, size.height * 0.60f)
        lineTo(size.width * 0.98f, size.height * 0.82f)
        lineTo(size.width * 0.90f, size.height * 0.90f)
        lineTo(size.width * 0.74f, size.height * 0.90f)
        lineTo(size.width * 0.50f, size.height * 0.88f)
        lineTo(size.width * 0.32f, size.height * 0.88f)
        lineTo(size.width * 0.20f, size.height * 0.86f)
        lineTo(size.width * 0.05f, size.height * 0.86f)
        lineTo(size.width * 0.04f, size.height * 0.62f)
        lineTo(size.width * 0.05f, size.height * 0.42f)
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
