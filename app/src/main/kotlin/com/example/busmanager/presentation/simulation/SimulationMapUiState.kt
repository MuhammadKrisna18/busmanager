package com.example.busmanager.presentation.simulation

import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.RouteSegment
import com.example.busmanager.domain.route.model.Terminal
import com.example.busmanager.domain.simulation.model.GameCalendar

enum class SimulationBottomSheetType {
    NONE,
    BUSES,
    ROUTES
}

data class SimulationMapUiState(
    val currentTime: GameCalendar = GameCalendar.initial(),
    val isPlaying: Boolean = false,
    val companyName: String = "PO TransNusantara",
    val balanceFormatted: String = "Rp 0",
    val ownedBusCount: Int = 0,
    val activeBusCount: Int = 0,
    val ownedBuses: List<Bus> = emptyList(),
    val terminals: List<Terminal> = emptyList(),
    val allSegments: List<RouteSegment> = emptyList(),
    val routes: List<BusRoute> = emptyList(),
    val selectedTerminal: Terminal? = null,
    val selectedRoute: BusRoute? = null,
    val activeBottomSheet: SimulationBottomSheetType = SimulationBottomSheetType.NONE,
    val isLoading: Boolean = false
) {
    val formattedDateTime: String
        get() = currentTime.formatDateTime()

    val formattedDate: String
        get() = "${currentTime.dayOfWeekName()}, ${currentTime.formatDate()}"

    val formattedTime: String
        get() = currentTime.formatTime()

    val hasActiveFleet: Boolean
        get() = activeBusCount > 0
}
