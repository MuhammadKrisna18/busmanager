package com.example.busmanager.presentation.route.components

import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.MapCoordinate
import com.example.busmanager.domain.route.model.RoutePathType
import com.example.busmanager.domain.route.model.RouteSegment
import com.example.busmanager.domain.route.model.Terminal

data class MapVisualState(
    val terminals: List<Terminal> = emptyList(),
    val allSegments: List<RouteSegment> = emptyList(),
    val selectedRoute: BusRoute? = null,
    val selectedOrigin: Terminal? = null,
    val selectedDestination: Terminal? = null,
    val transitTerminals: List<Terminal> = emptyList()
)

data class MapSegmentToggle(
    val fromTerminalId: String,
    val toTerminalId: String,
    val midCoordinate: MapCoordinate,
    val currentPathType: RoutePathType
)

data class MapTerminalToggle(
    val terminalId: String,
    val coordinate: MapCoordinate,
    val isTransitActive: Boolean
)
