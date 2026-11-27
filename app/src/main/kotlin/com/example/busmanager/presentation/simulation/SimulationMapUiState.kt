package com.example.busmanager.presentation.simulation

import com.example.busmanager.domain.driver.model.Driver
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.fleet.usecase.BusOperationStatus
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.RoutePricing
import com.example.busmanager.domain.route.model.RouteSchedule
import com.example.busmanager.domain.route.model.RouteSegment
import com.example.busmanager.domain.route.model.Terminal
import com.example.busmanager.domain.simulation.model.ActiveBusTrip
import com.example.busmanager.domain.simulation.model.GameCalendar
import com.example.busmanager.domain.simulation.model.SimulationSpeed
import com.example.busmanager.domain.simulation.model.TerminalPassengerQueue

enum class SimulationBottomSheetType {
    NONE,
    BUSES,
    ROUTES
}

data class SimulationMapUiState(
    val currentTime: GameCalendar = GameCalendar.initial(),
    val isPlaying: Boolean = false,
    val currentSpeed: SimulationSpeed = SimulationSpeed.NORMAL,
    val companyName: String = "PO TransNusantara",
    val balanceFormatted: String = "Rp 0",
    val ownedBusCount: Int = 0,
    val activeBusCount: Int = 0,
    val ownedBuses: List<Bus> = emptyList(),
    val activeTrips: List<ActiveBusTrip> = emptyList(),
    val terminals: List<Terminal> = emptyList(),
    val allSegments: List<RouteSegment> = emptyList(),
    val routes: List<BusRoute> = emptyList(),
    val drivers: List<Driver> = emptyList(),
    val schedules: List<RouteSchedule> = emptyList(),
    val routePricings: Map<String, List<RoutePricing>> = emptyMap(),
    val busOperationStatuses: Map<String, BusOperationStatus> = emptyMap(),
    val terminalQueues: Map<String, TerminalPassengerQueue> = emptyMap(),
    val selectedTerminal: Terminal? = null,
    val selectedRoute: BusRoute? = null,
    val selectedTrip: ActiveBusTrip? = null,
    val trackedTripId: String? = null,
    val activeBottomSheet: SimulationBottomSheetType = SimulationBottomSheetType.NONE,
    val isLoading: Boolean = false,
    val editingScheduleTarget: SimulationScheduleEditTarget? = null
) {
    val formattedDateTime: String
        get() = currentTime.formatDateTime()

    val formattedDate: String
        get() = "${currentTime.dayOfWeekName()}, ${currentTime.formatDate()}"

    val formattedTime: String
        get() = currentTime.formatTime()

    val hasActiveFleet: Boolean
        get() = activeBusCount > 0

    val openedRouteIds: Set<String>
        get() = (schedules.map { it.routeId } + ownedBuses.mapNotNull { it.routeId } + activeTrips.map { it.route.id }).toSet()

    val openedRoutes: List<BusRoute>
        get() = routes.filter { openedRouteIds.contains(it.id) }

    val parkedBuses: List<ParkedBusInfo>
        get() {
            val activeBusIds = activeTrips.map { it.busId }.toSet()
            return ownedBuses
                .filter { it.routeId != null && !activeBusIds.contains(it.id) }
                .mapNotNull { bus ->
                    val route = routes.find { it.id == bus.routeId }
                    val termId = bus.currentTerminalId ?: route?.originTerminal?.id
                    val term = terminals.find { it.id == termId } ?: route?.originTerminal
                    if (term != null) {
                        ParkedBusInfo(bus = bus, terminal = term, route = route)
                    } else null
                }
        }
}

data class ParkedBusInfo(
    val bus: Bus,
    val terminal: Terminal,
    val route: BusRoute?
)

data class SimulationScheduleEditTarget(
    val bus: Bus,
    val route: BusRoute,
    val currentHeadwayMinutes: Int = 60
)
