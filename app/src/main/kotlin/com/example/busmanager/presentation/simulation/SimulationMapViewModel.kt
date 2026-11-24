package com.example.busmanager.presentation.simulation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.busmanager.domain.company.usecase.GetCompanyUseCase
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.fleet.usecase.GetOwnedFleetUseCase
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.Terminal
import com.example.busmanager.domain.route.repository.RouteRepository
import com.example.busmanager.domain.route.usecase.GetAvailableTerminalsUseCase
import com.example.busmanager.domain.simulation.model.ActiveBusTrip
import com.example.busmanager.domain.simulation.model.RouteDirection
import com.example.busmanager.domain.simulation.usecase.DispatchBusUseCase
import com.example.busmanager.domain.simulation.usecase.GetActiveTripsUseCase
import com.example.busmanager.domain.simulation.usecase.GetSimulationTimeUseCase
import com.example.busmanager.domain.simulation.usecase.ToggleSimulationPlayUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SimulationMapViewModel(
    private val getSimulationTimeUseCase: GetSimulationTimeUseCase,
    private val toggleSimulationPlayUseCase: ToggleSimulationPlayUseCase,
    private val getCompanyUseCase: GetCompanyUseCase,
    private val getOwnedFleetUseCase: GetOwnedFleetUseCase,
    private val getAvailableTerminalsUseCase: GetAvailableTerminalsUseCase,
    private val routeRepository: RouteRepository,
    private val getActiveTripsUseCase: GetActiveTripsUseCase,
    private val dispatchBusUseCase: DispatchBusUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SimulationMapUiState())
    val uiState: StateFlow<SimulationMapUiState> = _uiState.asStateFlow()

    init {
        observeSimulationEngine()
        observeCompanyAndFleet()
        observeActiveTrips()
        loadMapData()
    }

    private fun observeSimulationEngine() {
        viewModelScope.launch {
            combine(
                getSimulationTimeUseCase(),
                toggleSimulationPlayUseCase.isPlaying()
            ) { time, playing ->
                _uiState.update { current ->
                    current.copy(
                        currentTime = time,
                        isPlaying = playing
                    )
                }
            }.collect {}
        }
    }

    private fun observeCompanyAndFleet() {
        viewModelScope.launch {
            combine(
                getCompanyUseCase(),
                getOwnedFleetUseCase()
            ) { company, fleet ->
                _uiState.update { current ->
                    current.copy(
                        companyName = company?.name ?: "PO TransNusantara",
                        balanceFormatted = company?.balance?.formatRupiah() ?: "Rp 0",
                        ownedBusCount = fleet.size,
                        ownedBuses = fleet
                    )
                }
            }.collect {}
        }
    }

    private fun observeActiveTrips() {
        viewModelScope.launch {
            getActiveTripsUseCase().collect { trips ->
                _uiState.update { current ->
                    current.copy(
                        activeTrips = trips,
                        activeBusCount = trips.size,
                        selectedTrip = current.selectedTrip?.let { sel -> trips.find { it.id == sel.id } }
                    )
                }
            }
        }
    }

    private fun loadMapData() {
        viewModelScope.launch {
            val terminals = getAvailableTerminalsUseCase()
            val allSegments = routeRepository.getAllSegments()
            val routes = routeRepository.getAllPredefinedRoutes()

            _uiState.update { current ->
                current.copy(
                    terminals = terminals,
                    allSegments = allSegments,
                    routes = routes
                )
            }
        }
    }

    fun togglePlay() {
        toggleSimulationPlayUseCase()
    }

    fun onTerminalClick(terminal: Terminal) {
        _uiState.update { current ->
            if (current.selectedTerminal?.id == terminal.id) {
                current.copy(selectedTerminal = null, selectedRoute = null)
            } else {
                val matchingRoute = current.routes.firstOrNull { it.containsTerminal(terminal.id) }
                current.copy(
                    selectedTerminal = terminal,
                    selectedRoute = matchingRoute,
                    selectedTrip = null
                )
            }
        }
    }

    fun clearSelectedTerminal() {
        _uiState.update { it.copy(selectedTerminal = null, selectedRoute = null) }
    }

    fun openBusesBottomSheet() {
        _uiState.update { it.copy(activeBottomSheet = SimulationBottomSheetType.BUSES) }
    }

    fun openRoutesBottomSheet() {
        _uiState.update { it.copy(activeBottomSheet = SimulationBottomSheetType.ROUTES) }
    }

    fun dismissBottomSheet() {
        _uiState.update { it.copy(activeBottomSheet = SimulationBottomSheetType.NONE) }
    }

    fun selectRoute(route: BusRoute) {
        _uiState.update { current ->
            current.copy(
                selectedRoute = route,
                selectedTerminal = route.originTerminal,
                selectedTrip = null,
                activeBottomSheet = SimulationBottomSheetType.NONE
            )
        }
    }

    fun onTripClick(trip: ActiveBusTrip) {
        _uiState.update { current ->
            current.copy(
                selectedTrip = trip,
                selectedRoute = trip.route,
                selectedTerminal = null
            )
        }
    }

    fun clearSelectedTrip() {
        _uiState.update { it.copy(selectedTrip = null) }
    }

    fun dispatchBus(bus: Bus, route: BusRoute) {
        dispatchBusUseCase(
            bus = bus,
            route = route,
            direction = RouteDirection.ORIGIN_TO_DESTINATION,
            currentTime = _uiState.value.currentTime
        )
        _uiState.update { it.copy(activeBottomSheet = SimulationBottomSheetType.NONE) }
    }
}
