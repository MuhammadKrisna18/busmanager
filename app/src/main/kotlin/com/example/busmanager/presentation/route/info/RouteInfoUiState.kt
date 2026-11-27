package com.example.busmanager.presentation.route.info

import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.RouteOperationalInfo
import com.example.busmanager.domain.route.model.RoutePricing
import com.example.busmanager.domain.route.model.RouteSchedule

enum class RouteInfoFilter(val displayName: String) {
    ALL("Semua"),
    OPERATING("Berdinas"),
    STANDBY("Standby")
}

data class RouteDetailItem(
    val operationalInfo: RouteOperationalInfo,
    val pricings: List<RoutePricing> = emptyList(),
    val isPricingsExpanded: Boolean = false
)

data class RouteInfoUiState(
    val allRoutes: List<RouteDetailItem> = emptyList(),
    val filteredRoutes: List<RouteDetailItem> = emptyList(),
    val ownedBuses: List<Bus> = emptyList(),
    val selectedFilter: RouteInfoFilter = RouteInfoFilter.ALL,
    val selectedRouteForScheduleEdit: BusRoute? = null,
    val activeSchedulesForEditing: List<RouteSchedule> = emptyList(),
    val totalRouteCount: Int = 0,
    val operatingRouteCount: Int = 0,
    val standbyRouteCount: Int = 0,
    val isLoading: Boolean = true,
    val message: String? = null,
    val errorMessage: String? = null,
    val routeToDelete: BusRoute? = null
)
