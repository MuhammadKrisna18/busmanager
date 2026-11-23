package com.example.busmanager.presentation.route

import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.RoutePathType
import com.example.busmanager.domain.route.model.RouteSegment
import com.example.busmanager.domain.route.model.RouteTripEstimate
import com.example.busmanager.domain.route.model.Terminal
import com.example.busmanager.domain.route.model.TransitStop

data class RoutePlannerUiState(
    val terminals: List<Terminal> = emptyList(),
    val allSegments: List<RouteSegment> = emptyList(),
    val selectedOrigin: Terminal? = null,
    val selectedDestination: Terminal? = null,
    val availableRoutes: List<BusRoute> = emptyList(),
    val selectedRoute: BusRoute? = null,
    val preferredPathType: RoutePathType? = null,
    val transitStops: List<TransitStop> = emptyList(),
    val tripEstimate: RouteTripEstimate? = null,
    val availableBuses: List<Bus> = emptyList(),
    val selectedBus: Bus? = null,
    val isLoading: Boolean = true
)
