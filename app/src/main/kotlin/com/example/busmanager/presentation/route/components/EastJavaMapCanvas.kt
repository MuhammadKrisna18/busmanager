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
import androidx.compose.foundation.layout.requiredSize
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
import androidx.compose.ui.platform.LocalDensity
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
    val zoomAnim = remember { Animatable(EastJavaMapProjection.DEFAULT_ZOOM) }
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
        val density = LocalDensity.current
        val viewportWidth = constraints.maxWidth.toFloat()
        val viewportHeight = constraints.maxHeight.toFloat()
        val worldDimensions = remember(viewportWidth, viewportHeight) {
            EastJavaMapProjection.calculateWorldDimensions(viewportWidth, viewportHeight)
        }
        val worldWidth = worldDimensions.width
        val worldHeight = worldDimensions.height
        val worldWidthDp = with(density) { worldWidth.toDp() }
        val worldHeightDp = with(density) { worldHeight.toDp() }

        val trackedTrip = activeTrips.find { it.id == trackedTripId }
        val activeNavigationRoute = trackedTrip?.route ?: selectedRoute
        val activeNavigationSegmentIds = activeNavigationRoute?.segments?.map { it.id }?.toSet() ?: emptySet()

        val currentZoom = zoomAnim.value
        val currentPanX = panXAnim.value
        val currentPanY = panYAnim.value

        LaunchedEffect(trackedTrip?.currentCoordinate, isTrackingLocked, worldWidth, worldHeight) {
            if (isTrackingLocked && trackedTrip != null && worldWidth > 0f && worldHeight > 0f) {
                val targetScale = currentZoom.coerceAtLeast(1.5f)
                val maxPx = EastJavaMapProjection.calculateMaxPanX(worldWidth, viewportWidth, targetScale)
                val maxPy = EastJavaMapProjection.calculateMaxPanY(worldHeight, viewportHeight, targetScale)
                val targetPanX = EastJavaMapProjection.calculateTargetPanX(
                    trackedTrip.currentCoordinate.x,
                    worldWidth,
                    targetScale,
                    maxPx
                )
                val targetPanY = EastJavaMapProjection.calculateTargetPanY(
                    trackedTrip.currentCoordinate.y,
                    worldHeight,
                    targetScale,
                    maxPy
                )
                launch { zoomAnim.animateTo(targetScale, animationSpec = tween(500)) }
                launch { panXAnim.animateTo(targetPanX, animationSpec = tween(500)) }
                launch { panYAnim.animateTo(targetPanY, animationSpec = tween(500)) }
            }
        }

        var hasCenteredInitialView by remember { mutableStateOf(false) }
        LaunchedEffect(worldWidth, viewportWidth, worldHeight, viewportHeight) {
            if (!hasCenteredInitialView && worldWidth > 0f && viewportWidth > 0f && trackedTripId == null) {
                hasCenteredInitialView = true
                val initialScale = EastJavaMapProjection.DEFAULT_ZOOM
                val maxPx = EastJavaMapProjection.calculateMaxPanX(worldWidth, viewportWidth, initialScale)
                val maxPy = EastJavaMapProjection.calculateMaxPanY(worldHeight, viewportHeight, initialScale)
                val targetPanX = EastJavaMapProjection.calculateTargetPanX(0.76f, worldWidth, initialScale, maxPx)
                val targetPanY = EastJavaMapProjection.calculateTargetPanY(0.60f, worldHeight, initialScale, maxPy)
                panXAnim.snapTo(targetPanX)
                panYAnim.snapTo(targetPanY)
            }
        }

        fun zoomIn() {
            val newZoom = (zoomAnim.value + 0.35f).coerceAtMost(EastJavaMapProjection.MAX_ZOOM)
            val maxPx = EastJavaMapProjection.calculateMaxPanX(worldWidth, viewportWidth, newZoom)
            val maxPy = EastJavaMapProjection.calculateMaxPanY(worldHeight, viewportHeight, newZoom)
            val newPanX = panXAnim.value.coerceIn(-maxPx, maxPx)
            val newPanY = panYAnim.value.coerceIn(-maxPy, maxPy)
            coroutineScope.launch {
                launch { zoomAnim.animateTo(newZoom, animationSpec = tween(250)) }
                launch { panXAnim.animateTo(newPanX, animationSpec = tween(250)) }
                launch { panYAnim.animateTo(newPanY, animationSpec = tween(250)) }
            }
        }

        fun zoomOut() {
            val newZoom = (zoomAnim.value - 0.35f).coerceAtLeast(EastJavaMapProjection.MIN_ZOOM)
            val maxPx = EastJavaMapProjection.calculateMaxPanX(worldWidth, viewportWidth, newZoom)
            val maxPy = EastJavaMapProjection.calculateMaxPanY(worldHeight, viewportHeight, newZoom)
            val newPanX = panXAnim.value.coerceIn(-maxPx, maxPx)
            val newPanY = panYAnim.value.coerceIn(-maxPy, maxPy)
            coroutineScope.launch {
                launch { zoomAnim.animateTo(newZoom, animationSpec = tween(250)) }
                launch { panXAnim.animateTo(newPanX, animationSpec = tween(250)) }
                launch { panYAnim.animateTo(newPanY, animationSpec = tween(250)) }
            }
        }

        fun resetMap() {
            isTrackingLocked = false
            val targetScale = EastJavaMapProjection.DEFAULT_ZOOM
            val maxPx = EastJavaMapProjection.calculateMaxPanX(worldWidth, viewportWidth, targetScale)
            val maxPy = EastJavaMapProjection.calculateMaxPanY(worldHeight, viewportHeight, targetScale)
            val targetPanX = EastJavaMapProjection.calculateTargetPanX(0.76f, worldWidth, targetScale, maxPx)
            val targetPanY = EastJavaMapProjection.calculateTargetPanY(0.60f, worldHeight, targetScale, maxPy)
            coroutineScope.launch {
                launch { zoomAnim.animateTo(targetScale, animationSpec = tween(300)) }
                launch { panXAnim.animateTo(targetPanX, animationSpec = tween(300)) }
                launch { panYAnim.animateTo(targetPanY, animationSpec = tween(300)) }
            }
        }

        fun trackOrCenter() {
            val targetTrip = trackedTrip ?: activeTrips.firstOrNull()
            if (targetTrip != null) {
                if (targetTrip.id != trackedTripId) {
                    onTripClick(targetTrip)
                }
                isTrackingLocked = true
                val targetScale = zoomAnim.value.coerceAtLeast(1.5f)
                val maxPx = EastJavaMapProjection.calculateMaxPanX(worldWidth, viewportWidth, targetScale)
                val maxPy = EastJavaMapProjection.calculateMaxPanY(worldHeight, viewportHeight, targetScale)
                val tPanX = EastJavaMapProjection.calculateTargetPanX(
                    targetTrip.currentCoordinate.x,
                    worldWidth,
                    targetScale,
                    maxPx
                )
                val tPanY = EastJavaMapProjection.calculateTargetPanY(
                    targetTrip.currentCoordinate.y,
                    worldHeight,
                    targetScale,
                    maxPy
                )
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

        val viewportGestureModifier = if (enableZoomAndPan) {
            Modifier
                .fillMaxSize()
                .clipToBounds()
                .pointerInput(worldWidth, worldHeight, viewportWidth, viewportHeight) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        isTrackingLocked = false
                        val newZoom = (zoomAnim.value * zoom).coerceIn(
                            EastJavaMapProjection.MIN_ZOOM,
                            EastJavaMapProjection.MAX_ZOOM
                        )
                        val maxPanX = EastJavaMapProjection.calculateMaxPanX(worldWidth, viewportWidth, newZoom)
                        val maxPanY = EastJavaMapProjection.calculateMaxPanY(worldHeight, viewportHeight, newZoom)
                        val newPanX = (panXAnim.value + pan.x).coerceIn(-maxPanX, maxPanX)
                        val newPanY = (panYAnim.value + pan.y).coerceIn(-maxPanY, maxPanY)
                        coroutineScope.launch {
                            zoomAnim.snapTo(newZoom)
                            panXAnim.snapTo(newPanX)
                            panYAnim.snapTo(newPanY)
                        }
                    }
                }
        } else {
            Modifier
                .fillMaxSize()
                .clipToBounds()
        }

        Box(
            modifier = viewportGestureModifier,
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .requiredSize(worldWidthDp, worldHeightDp)
                    .graphicsLayer {
                        scaleX = currentZoom
                        scaleY = currentZoom
                        translationX = currentPanX
                        translationY = currentPanY
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawJavaIslandLandmass()
                    drawMaduraIsland()

                    allSegments.forEach { segment ->
                        if (!activeNavigationSegmentIds.contains(segment.id)) {
                            val openedColor = openedSegmentColorMap[segment.id]
                            if (openedColor != null) {
                                drawOpenedRouteSegment(
                                    segment = segment,
                                    routeColor = openedColor,
                                    canvasWidth = worldWidth,
                                    canvasHeight = worldHeight
                                )
                            } else {
                                drawBackgroundSegment(
                                    segment = segment,
                                    canvasWidth = worldWidth,
                                    canvasHeight = worldHeight
                                )
                            }
                        }
                    }

                    activeNavigationRoute?.segments?.forEach { segment ->
                        drawGoogleMapsTrackedSegment(
                            segment = segment,
                            canvasWidth = worldWidth,
                            canvasHeight = worldHeight
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

                    val xPx = (terminal.coordinate.x * worldWidth) - 24.dp.value
                    val yPx = (terminal.coordinate.y * worldHeight) - 28.dp.value

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

                val tripOffsets = remember(activeTrips, worldWidth, worldHeight) {
                    BusMarkerAntiOverlapCalculator.calculateOffsets(
                        items = activeTrips,
                        coordinateSelector = { it.currentCoordinate },
                        canvasWidth = worldWidth,
                        canvasHeight = worldHeight
                    )
                }

                activeTrips.forEach { trip ->
                    val isTracked = trip.id == trackedTripId
                    val offset = tripOffsets[trip] ?: DisplayMarkerOffset()
                    val xPx = (trip.currentCoordinate.x * worldWidth) + offset.dxPx
                    val yPx = (trip.currentCoordinate.y * worldHeight) + offset.dyPx

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
                    val firstParked = busesAtTerminal.first()
                    val baseXPx = firstParked.terminal.coordinate.x * worldWidth
                    val baseYPx = firstParked.terminal.coordinate.y * worldHeight

                    if (busesAtTerminal.size > 2) {
                        val offsetXPx = baseXPx + 22.dp.value
                        val posYPx = baseYPx - 10.dp.value

                        Box(
                            modifier = Modifier
                                .offset {
                                    IntOffset(offsetXPx.roundToInt(), posYPx.roundToInt())
                                }
                                .size(0.dp)
                                .wrapContentSize(Alignment.Center, unbounded = true)
                        ) {
                            ParkedBusClusterMarker(
                                terminal = firstParked.terminal,
                                count = busesAtTerminal.size,
                                onClick = { onParkedBusClick(firstParked.bus) },
                                zoomScale = currentZoom
                            )
                        }
                    } else {
                        busesAtTerminal.forEachIndexed { index, parked ->
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
                }

                MapSegmentTogglesOverlay(
                    segmentToggles = segmentToggles,
                    canvasWidth = worldWidth,
                    canvasHeight = worldHeight,
                    onSegmentToggleClick = onSegmentToggleClick
                )

                MapTerminalTogglesOverlay(
                    terminalToggles = terminalToggles,
                    canvasWidth = worldWidth,
                    canvasHeight = worldHeight,
                    onTerminalToggleClick = onTerminalToggleClick
                )
            }
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
