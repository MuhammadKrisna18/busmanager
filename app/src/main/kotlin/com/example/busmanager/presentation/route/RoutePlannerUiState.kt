package com.example.busmanager.presentation.route

import com.example.busmanager.domain.driver.model.Driver
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.RoutePathType
import com.example.busmanager.domain.route.model.RoutePricing
import com.example.busmanager.domain.route.model.RouteSchedule
import com.example.busmanager.domain.route.model.RouteSegment
import com.example.busmanager.domain.route.model.RouteTripEstimate
import com.example.busmanager.domain.route.model.Terminal
import com.example.busmanager.domain.route.model.TransitStop

enum class RouteDetailTab(val title: String) {
    OVERVIEW("Jalur & Peta"),
    PRICING("Tarif Tiket"),
    SCHEDULES("Jadwal & Armada")
}

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
    val hiredDrivers: List<Driver> = emptyList(),
    val selectedBus: Bus? = null,
    val activeTab: RouteDetailTab = RouteDetailTab.OVERVIEW,
    val pricings: List<RoutePricing> = emptyList(),
    val schedules: List<RouteSchedule> = emptyList(),
    val editingPricing: RoutePricing? = null,
    val isAddScheduleDialogOpen: Boolean = false,
    val selectedScheduleForBusAssignment: RouteSchedule? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isLoading: Boolean = true
)
