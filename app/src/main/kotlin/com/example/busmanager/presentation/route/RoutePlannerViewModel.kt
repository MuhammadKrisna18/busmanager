package com.example.busmanager.presentation.route

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.fleet.usecase.GetOwnedFleetUseCase
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.RoutePathType
import com.example.busmanager.domain.route.model.Terminal
import com.example.busmanager.domain.route.repository.RouteRepository
import com.example.busmanager.domain.route.usecase.CalculateRouteCostUseCase
import com.example.busmanager.domain.route.usecase.GetAvailableRoutesUseCase
import com.example.busmanager.domain.route.usecase.GetAvailableTerminalsUseCase
import com.example.busmanager.domain.route.usecase.GetRouteTransitStopsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RoutePlannerViewModel(
    private val getAvailableTerminalsUseCase: GetAvailableTerminalsUseCase,
    private val getAvailableRoutesUseCase: GetAvailableRoutesUseCase,
    private val getRouteTransitStopsUseCase: GetRouteTransitStopsUseCase,
    private val calculateRouteCostUseCase: CalculateRouteCostUseCase,
    private val getOwnedFleetUseCase: GetOwnedFleetUseCase,
    private val routeRepository: RouteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoutePlannerUiState())
    val uiState: StateFlow<RoutePlannerUiState> = _uiState.asStateFlow()

    init {
        initializeData()
    }

    private fun initializeData() {
        viewModelScope.launch {
            val terminals = getAvailableTerminalsUseCase()
            val allSegments = routeRepository.getAllSegments()

            val defaultOrigin = terminals.find { it.id == "term_purabaya" } ?: terminals.firstOrNull()
            val defaultDestination = terminals.find { it.id == "term_sritanjung" } ?: terminals.lastOrNull()

            _uiState.update { current ->
                current.copy(
                    terminals = terminals,
                    allSegments = allSegments,
                    selectedOrigin = defaultOrigin,
                    selectedDestination = defaultDestination,
                    isLoading = false
                )
            }

            loadRoutes()

            getOwnedFleetUseCase().collect { buses ->
                _uiState.update { current ->
                    current.copy(
                        availableBuses = buses,
                        selectedBus = current.selectedBus ?: buses.firstOrNull()
                    )
                }
                recalculateEstimate()
            }
        }
    }

    fun selectOrigin(terminal: Terminal) {
        if (_uiState.value.selectedDestination?.id == terminal.id) {
            val previousOrigin = _uiState.value.selectedOrigin
            _uiState.update { it.copy(selectedOrigin = terminal, selectedDestination = previousOrigin) }
        } else {
            _uiState.update { it.copy(selectedOrigin = terminal) }
        }
        loadRoutes()
    }

    fun selectDestination(terminal: Terminal) {
        if (_uiState.value.selectedOrigin?.id == terminal.id) {
            val previousDestination = _uiState.value.selectedDestination
            _uiState.update { it.copy(selectedDestination = terminal, selectedOrigin = previousDestination) }
        } else {
            _uiState.update { it.copy(selectedDestination = terminal) }
        }
        loadRoutes()
    }

    fun swapOriginDestination() {
        val currentOrigin = _uiState.value.selectedOrigin
        val currentDestination = _uiState.value.selectedDestination
        if (currentOrigin != null && currentDestination != null) {
            _uiState.update {
                it.copy(
                    selectedOrigin = currentDestination,
                    selectedDestination = currentOrigin
                )
            }
            loadRoutes()
        }
    }

    fun setPreferredPathType(pathType: RoutePathType?) {
        _uiState.update { it.copy(preferredPathType = pathType) }
        loadRoutes()
    }

    fun selectRoute(route: BusRoute) {
        val transitStops = getRouteTransitStopsUseCase(route)
        _uiState.update {
            it.copy(
                selectedRoute = route,
                transitStops = transitStops
            )
        }
        recalculateEstimate()
    }

    fun selectBus(bus: Bus) {
        _uiState.update { it.copy(selectedBus = bus) }
        recalculateEstimate()
    }

    fun onTerminalMapClick(terminal: Terminal) {
        val currentOrigin = _uiState.value.selectedOrigin
        val currentDestination = _uiState.value.selectedDestination

        when {
            currentOrigin == null -> selectOrigin(terminal)
            terminal.id == currentOrigin.id -> return
            currentDestination == null || terminal.id == currentDestination.id -> selectDestination(terminal)
            else -> selectDestination(terminal)
        }
    }

    private fun loadRoutes() {
        val originId = _uiState.value.selectedOrigin?.id ?: return
        val destId = _uiState.value.selectedDestination?.id ?: return
        val preferred = _uiState.value.preferredPathType

        val routes = getAvailableRoutesUseCase(
            originTerminalId = originId,
            destinationTerminalId = destId,
            preferredPathType = preferred
        )

        val selected = routes.firstOrNull()
        val transitStops = selected?.let { getRouteTransitStopsUseCase(it) } ?: emptyList()

        _uiState.update {
            it.copy(
                availableRoutes = routes,
                selectedRoute = selected,
                transitStops = transitStops
            )
        }

        recalculateEstimate()
    }

    private fun recalculateEstimate() {
        val route = _uiState.value.selectedRoute ?: return
        val bus = _uiState.value.selectedBus
        if (bus != null) {
            val estimate = calculateRouteCostUseCase(route, bus)
            _uiState.update { it.copy(tripEstimate = estimate) }
        }
    }
}
