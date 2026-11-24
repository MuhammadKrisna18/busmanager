package com.example.busmanager.presentation.simulation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.company.repository.CompanyRepository
import com.example.busmanager.domain.company.usecase.GetCompanyUseCase
import com.example.busmanager.domain.driver.repository.DriverRepository
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.fleet.usecase.BusOperationStatus
import com.example.busmanager.domain.fleet.usecase.GetOwnedFleetUseCase
import com.example.busmanager.domain.fleet.usecase.ValidateBusOperationUseCase
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.Terminal
import com.example.busmanager.domain.route.repository.RoutePricingRepository
import com.example.busmanager.domain.route.repository.RouteRepository
import com.example.busmanager.domain.route.repository.RouteScheduleRepository
import com.example.busmanager.domain.route.usecase.GetAvailableTerminalsUseCase
import com.example.busmanager.domain.simulation.model.ActiveBusTrip
import com.example.busmanager.domain.simulation.model.GameCalendar
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
import kotlin.math.abs

class SimulationMapViewModel(
    private val getSimulationTimeUseCase: GetSimulationTimeUseCase,
    private val toggleSimulationPlayUseCase: ToggleSimulationPlayUseCase,
    private val getCompanyUseCase: GetCompanyUseCase,
    private val getOwnedFleetUseCase: GetOwnedFleetUseCase,
    private val getAvailableTerminalsUseCase: GetAvailableTerminalsUseCase,
    private val routeRepository: RouteRepository,
    private val getActiveTripsUseCase: GetActiveTripsUseCase,
    private val dispatchBusUseCase: DispatchBusUseCase,
    private val validateBusOperationUseCase: ValidateBusOperationUseCase? = null,
    private val routeScheduleRepository: RouteScheduleRepository? = null,
    private val routePricingRepository: RoutePricingRepository? = null,
    private val companyRepository: CompanyRepository? = null,
    private val driverRepository: DriverRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(SimulationMapUiState())
    val uiState: StateFlow<SimulationMapUiState> = _uiState.asStateFlow()

    private val dispatchedSchedulesInCurrentHour = mutableSetOf<String>()
    private var lastObservedHour = -1

    init {
        observeSimulationEngine()
        observeCompanyAndFleet()
        observeActiveTrips()
        observeDriversAndSchedules()
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

                if (playing) {
                    handleScheduledDepartures(time)
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
                updateFleetOperationStatuses(fleet)
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

    private fun observeDriversAndSchedules() {
        viewModelScope.launch {
            driverRepository?.getAllDrivers()?.collect { driverList ->
                _uiState.update { it.copy(drivers = driverList) }
                updateFleetOperationStatuses(_uiState.value.ownedBuses)
            }
        }

        viewModelScope.launch {
            routeScheduleRepository?.getAllSchedules()?.collect { scheduleList ->
                _uiState.update { it.copy(schedules = scheduleList) }
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

    private suspend fun updateFleetOperationStatuses(buses: List<Bus>) {
        if (validateBusOperationUseCase == null) return
        val statusMap = mutableMapOf<String, BusOperationStatus>()
        buses.forEach { bus ->
            statusMap[bus.id] = validateBusOperationUseCase.invoke(bus)
        }
        _uiState.update { it.copy(busOperationStatuses = statusMap) }
    }

    private suspend fun handleScheduledDepartures(time: GameCalendar) {
        if (time.hour != lastObservedHour) {
            dispatchedSchedulesInCurrentHour.clear()
            lastObservedHour = time.hour
        }

        val schedules = _uiState.value.schedules
        val buses = _uiState.value.ownedBuses
        val activeTrips = _uiState.value.activeTrips
        val routes = _uiState.value.routes

        for (schedule in schedules) {
            val busId = schedule.assignedBusId ?: continue
            val scheduleKey = "${schedule.id}-${time.day}-${time.hour}"
            if (dispatchedSchedulesInCurrentHour.contains(scheduleKey)) continue

            val bus = buses.find { it.id == busId } ?: continue
            val isAlreadyOnTrip = activeTrips.any { it.busId == bus.id }
            if (isAlreadyOnTrip) continue

            val status = validateBusOperationUseCase?.invoke(bus)
            if (status != null && !status.canOperate) continue

            val isTimeMatching = schedule.departureHour == time.hour && abs(schedule.departureMinute - time.minute) <= 15
            if (isTimeMatching) {
                val route = routes.find { it.id == schedule.routeId } ?: continue
                dispatchedSchedulesInCurrentHour.add(scheduleKey)
                executeDispatchAndRevenue(bus, route, schedule.direction, time)
            }
        }
    }

    private fun executeDispatchAndRevenue(
        bus: Bus,
        route: BusRoute,
        direction: RouteDirection,
        currentTime: GameCalendar
    ) {
        dispatchBusUseCase(
            bus = bus,
            route = route,
            direction = direction,
            currentTime = currentTime
        )

        viewModelScope.launch {
            val pricings = routePricingRepository?.getPricingsListForRoute(route.id) ?: emptyList()
            val avgTicketPrice = if (pricings.isNotEmpty()) {
                pricings.map { it.price.amount }.average().toLong().coerceAtLeast(50_000L)
            } else {
                75_000L
            }
            val passengers = (bus.body.passengerCapacity * 0.85).toInt().coerceAtLeast(1)
            val ticketEarnings = Money.rupiah(avgTicketPrice * passengers)
            companyRepository?.addBalance(ticketEarnings)

            val fuelCost = (route.totalDistanceKm * 0.35 * 6800.0).toLong()
            val toll = route.totalTollFee.amount
            val retribution = 10_000L
            val operationalCost = Money.rupiah(fuelCost + toll + retribution)
            companyRepository?.deductBalance(operationalCost)
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
        executeDispatchAndRevenue(
            bus = bus,
            route = route,
            direction = RouteDirection.ORIGIN_TO_DESTINATION,
            currentTime = _uiState.value.currentTime
        )
        _uiState.update { it.copy(activeBottomSheet = SimulationBottomSheetType.NONE) }
    }
}
