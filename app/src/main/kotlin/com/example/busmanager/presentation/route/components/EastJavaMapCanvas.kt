package com.example.busmanager.presentation.route.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.RouteSegment
import com.example.busmanager.domain.route.model.Terminal
import com.example.busmanager.domain.simulation.model.ActiveBusTrip
import com.example.busmanager.presentation.simulation.ParkedBusInfo
import com.example.busmanager.ui.theme.AppDimens
import com.example.busmanager.ui.theme.CardBorder
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun EastJavaMapCanvas(
    terminals: List<Terminal>,
    allSegments: List<RouteSegment>,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(320.dp),
    openedRoutes: List<BusRoute> = emptyList(),
    selectedRoute: BusRoute? = null,
    selectedOrigin: Terminal? = null,
    selectedDestination: Terminal? = null,
    transitTerminals: List<Terminal> = emptyList(),
    candidateTerminalIds: Set<String> = emptySet(),
    segmentToggles: List<MapSegmentToggle> = emptyList(),
    terminalToggles: List<MapTerminalToggle> = emptyList(),
    activeTrips: List<ActiveBusTrip> = emptyList(),
    parkedBuses: List<ParkedBusInfo> = emptyList(),
    trackedTripId: String? = null,
    enableZoomAndPan: Boolean = true,
    availablePaths: List<List<Terminal>> = emptyList(),
    selectedPathIndex: Int = 0,
    onSelectPath: (Int) -> Unit = {},
    onTerminalClick: (Terminal) -> Unit = {},
    onTripClick: (ActiveBusTrip) -> Unit = {},
    onParkedBusClick: (Bus) -> Unit = {},
    onSegmentToggleClick: (fromId: String, toId: String) -> Unit = { _, _ -> },
    onTerminalToggleClick: (terminalId: String) -> Unit = {}
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
                    candidateTerminalIds.contains(terminal.id) -> TerminalNodeRole.NEXT_CANDIDATE
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
                        onClick = {
                            if (availablePaths.size > 1) {
                                val targetIdx = availablePaths.indexOfFirst { path -> path.any { it.id == terminal.id } }
                                if (targetIdx >= 0 && targetIdx != selectedPathIndex) {
                                    onSelectPath(targetIdx)
                                }
                            }
                            onTerminalClick(terminal)
                        },
                        zoomScale = currentZoom
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
                        onClick = { onTripClick(trip) },
                        zoomScale = currentZoom
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
                            onClick = { onParkedBusClick(parked.bus) },
                            zoomScale = currentZoom
                        )
                    }
                }
            }

            MapSegmentTogglesOverlay(
                segmentToggles = segmentToggles,
                canvasWidth = canvasWidth,
                canvasHeight = canvasHeight,
                onSegmentToggleClick = onSegmentToggleClick
            )

            MapTerminalTogglesOverlay(
                terminalToggles = terminalToggles,
                canvasWidth = canvasWidth,
                canvasHeight = canvasHeight,
                onTerminalToggleClick = onTerminalToggleClick
            )
        }

        MapCorridorSelector(
            availablePaths = availablePaths,
            selectedPathIndex = selectedPathIndex,
            onSelectPath = onSelectPath
        )

        MapControlsOverlay(
            trackedTrip = trackedTrip,
            isTrackingLocked = isTrackingLocked,
            currentZoom = currentZoom,
            enableZoomAndPan = enableZoomAndPan,
            onZoomIn = { zoomIn() },
            onZoomOut = { zoomOut() },
            onTrackOrCenter = { trackOrCenter() },
            onResetMap = { resetMap() }
        )
    }
}
