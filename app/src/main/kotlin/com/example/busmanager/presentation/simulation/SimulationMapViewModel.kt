package com.example.busmanager.presentation.simulation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.busmanager.domain.company.repository.CompanyRepository
import com.example.busmanager.domain.company.usecase.GetCompanyUseCase
import com.example.busmanager.domain.driver.repository.DriverRepository
import com.example.busmanager.domain.finance.usecase.RecordFinancialTransactionUseCase
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.fleet.repository.FleetRepository
import com.example.busmanager.domain.fleet.usecase.BusOperationStatus
import com.example.busmanager.domain.fleet.usecase.GetOwnedFleetUseCase
import com.example.busmanager.domain.fleet.usecase.ValidateBusOperationUseCase
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.RouteSchedule
import com.example.busmanager.domain.route.model.Terminal
import com.example.busmanager.domain.route.repository.RoutePricingRepository
import com.example.busmanager.domain.route.repository.RouteRepository
import com.example.busmanager.domain.route.repository.RouteScheduleRepository
import com.example.busmanager.domain.route.usecase.GetAvailableTerminalsUseCase
import com.example.busmanager.domain.simulation.model.ActiveBusTrip
import com.example.busmanager.domain.simulation.model.GameCalendar
import com.example.busmanager.domain.simulation.model.RouteDirection
import com.example.busmanager.domain.simulation.model.SimulationSpeed
import com.example.busmanager.domain.simulation.usecase.DispatchBusUseCase
import com.example.busmanager.domain.simulation.usecase.GetActiveTripsUseCase
import com.example.busmanager.domain.simulation.usecase.GetSimulationTimeUseCase
import com.example.busmanager.domain.simulation.usecase.SetSimulationSpeedUseCase
import com.example.busmanager.domain.simulation.usecase.ToggleSimulationPlayUseCase
import com.example.busmanager.domain.terminal.repository.TerminalPassengerRepository
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
    private val dispatchBusUseCase: DispatchBusUseCase,
    private val validateBusOperationUseCase: ValidateBusOperationUseCase? = null,
    private val routeScheduleRepository: RouteScheduleRepository? = null,
    private val routePricingRepository: RoutePricingRepository? = null,
    private val companyRepository: CompanyRepository? = null,
    private val driverRepository: DriverRepository? = null,
    private val setSimulationSpeedUseCase: SetSimulationSpeedUseCase? = null,
    private val recordFinancialTransactionUseCase: RecordFinancialTransactionUseCase? = null,
    private val terminalPassengerRepository: TerminalPassengerRepository? = null,
    private val fleetRepository: FleetRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(SimulationMapUiState())
    val uiState: StateFlow<SimulationMapUiState> = _uiState.asStateFlow()

    private val tripLifecycleCoordinator = SimulationTripLifecycleCoordinator(
        fleetRepository = fleetRepository,
        routeScheduleRepository = routeScheduleRepository,
        driverRepository = driverRepository,
        terminalPassengerRepository = terminalPassengerRepository,
        dispatchBusUseCase = dispatchBusUseCase
    )

    private val dispatchCoordinator = SimulationDispatchCoordinator(
        dispatchBusUseCase = dispatchBusUseCase,
        routePricingRepository = routePricingRepository,
        fleetRepository = fleetRepository,
        recordFinancialTransactionUseCase = recordFinancialTransactionUseCase,
        companyRepository = companyRepository,
        driverRepository = driverRepository,
        terminalPassengerRepository = terminalPassengerRepository,
        validateBusOperationUseCase = validateBusOperationUseCase
    )

    init {
        observeSimulationEngine()
        observeCompanyAndFleet()
        observeActiveTrips()
        observeDriversAndSchedules()
        observePricings()
        observePassengerQueues()
        loadMapData()
    }

    private fun observePassengerQueues() {
        viewModelScope.launch {
            dispatchBusUseCase.passengerQueueManager.queues.collect { queueMap ->
                _uiState.update { it.copy(terminalQueues = queueMap) }
            }
        }
    }

    private fun observeSimulationEngine() {
        viewModelScope.launch {
            val speedFlow = setSimulationSpeedUseCase?.getSpeed() ?: MutableStateFlow(SimulationSpeed.NORMAL)
            combine(
                getSimulationTimeUseCase(),
                toggleSimulationPlayUseCase.isPlaying(),
                speedFlow
            ) { time, playing, speed ->
                _uiState.update { current ->
                    current.copy(
                        currentTime = time,
                        isPlaying = playing,
                        currentSpeed = speed
                    )
                }

                if (playing) {
                    dispatchCoordinator.handleScheduledDepartures(
                        time = time,
                        schedules = _uiState.value.schedules,
                        buses = _uiState.value.ownedBuses,
                        activeTrips = _uiState.value.activeTrips,
                        routes = _uiState.value.routes,
                        onDispatch = { bus, route, direction, currentTime, schedule ->
                            executeDispatchAndRevenue(bus, route, direction, currentTime, schedule)
                        }
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
                updateFleetOperationStatuses(fleet)
            }.collect {}
        }
    }

    private fun observeActiveTrips() {
        viewModelScope.launch {
            getActiveTripsUseCase().collect { trips ->
                val completedTrips = trips.filter { it.isCompleted }
                val ongoingTrips = trips.filterNot { it.isCompleted }

                if (completedTrips.isNotEmpty()) {
                    handleCompletedTrips(completedTrips)
                }

                handleTransitPassengerExchanges(trips)

                _uiState.update { current ->
                    val updatedSelectedTrip = current.selectedTrip?.let { sel -> ongoingTrips.find { it.id == sel.id } }
                    val updatedTrackedId = if (ongoingTrips.any { it.id == current.trackedTripId }) current.trackedTripId else null
                    current.copy(
                        activeTrips = ongoingTrips,
                        activeBusCount = ongoingTrips.size,
                        selectedTrip = updatedSelectedTrip,
                        trackedTripId = updatedTrackedId
                    )
                }
            }
        }
    }

    private fun handleCompletedTrips(completedTrips: List<ActiveBusTrip>) {
        viewModelScope.launch {
            tripLifecycleCoordinator.handleCompletedTrips(
                completedTrips = completedTrips,
                currentTime = _uiState.value.currentTime,
                getBus = { busId ->
                    _uiState.value.ownedBuses.find { it.id == busId } ?: fleetRepository?.getBusById(busId)
                },
                getSchedules = { busId, routeId ->
                    (routeScheduleRepository?.getSchedulesForBus(busId)
                        ?.filter { it.routeId == routeId }
                        ?: emptyList()).ifEmpty {
                        _uiState.value.schedules.filter { it.assignedBusId == busId && it.routeId == routeId }
                    }
                },
                onBusUpdated = { updatedBus ->
                    _uiState.update { current ->
                        current.copy(
                            ownedBuses = current.ownedBuses.map { if (it.id == updatedBus.id) updatedBus else it }
                        )
                    }
                },
                onScheduleUpdated = { updatedSchedule ->
                    _uiState.update { current ->
                        current.copy(
                            schedules = current.schedules.map { if (it.id == updatedSchedule.id) updatedSchedule else it }
                        )
                    }
                }
            )
        }
    }

    private fun handleTransitPassengerExchanges(trips: List<ActiveBusTrip>) {
        if (terminalPassengerRepository == null) return
        viewModelScope.launch {
            tripLifecycleCoordinator.handleTransitPassengerExchanges(
                trips = trips,
                currentTime = _uiState.value.currentTime,
                currentBuses = _uiState.value.ownedBuses
            )
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

    private fun observePricings() {
        viewModelScope.launch {
            routePricingRepository?.getAllPricingsFlow()?.collect { allPricings ->
                val grouped = allPricings.groupBy { it.routeId }
                _uiState.update { it.copy(routePricings = grouped) }
            }
        }
    }

    private fun loadMapData() {
        viewModelScope.launch {
            val terminals = getAvailableTerminalsUseCase()
            val allSegments = routeRepository.getAllSegments()
            val routes = routeRepository.getAllRoutes()

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

    private fun executeDispatchAndRevenue(
        bus: Bus,
        route: BusRoute,
        direction: RouteDirection,
        currentTime: GameCalendar,
        explicitSchedule: RouteSchedule? = null
    ) {
        val depTerminal = if (direction == RouteDirection.ORIGIN_TO_DESTINATION) {
            route.originTerminal
        } else {
            route.destinationTerminal
        }

        val optimisticBus = bus.copy(isOperating = true, currentTerminalId = depTerminal.id)
        val matchedSchedule = explicitSchedule ?: _uiState.value.schedules.find {
            it.assignedBusId == bus.id && it.routeId == route.id && it.direction == direction
        } ?: _uiState.value.schedules.find {
            it.assignedBusId == bus.id && it.routeId == route.id
        }
        val updatedSchedule = matchedSchedule?.let { sched ->
            val nextDayEpoch = (currentTime.day + 1L) * 1440L + sched.departureHour * 60L + sched.departureMinute
            sched.copy(nextDepartureEpochMinutes = nextDayEpoch)
        }

        _uiState.update { current ->
            current.copy(
                ownedBuses = current.ownedBuses.map { if (it.id == bus.id) optimisticBus else it },
                schedules = if (updatedSchedule != null) {
                    current.schedules.map { if (it.id == updatedSchedule.id) updatedSchedule else it }
                } else {
                    current.schedules
                }
            )
        }

        viewModelScope.launch {
            if (updatedSchedule != null) {
                routeScheduleRepository?.updateSchedule(updatedSchedule)
            }
            val updatedBus = dispatchCoordinator.executeDispatchAndRevenue(
                bus = optimisticBus,
                route = route,
                direction = direction,
                currentTime = currentTime,
                schedules = _uiState.value.schedules,
                drivers = _uiState.value.drivers
            )
            _uiState.update { current ->
                current.copy(
                    ownedBuses = current.ownedBuses.map { if (it.id == bus.id) updatedBus else it }
                )
            }
        }
    }

    fun togglePlay() {
        toggleSimulationPlayUseCase()
    }

    fun setSpeed(speed: SimulationSpeed) {
        setSimulationSpeedUseCase?.invoke(speed)
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
        val routes = routeRepository.getAllRoutes()
        _uiState.update { it.copy(routes = routes, activeBottomSheet = SimulationBottomSheetType.BUSES) }
    }

    fun openRoutesBottomSheet() {
        val routes = routeRepository.getAllRoutes()
        _uiState.update { it.copy(routes = routes, activeBottomSheet = SimulationBottomSheetType.ROUTES) }
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
                trackedTripId = trip.id,
                selectedRoute = trip.route,
                selectedTerminal = null
            )
        }
    }

    fun trackBus(trip: ActiveBusTrip) {
        _uiState.update { current ->
            current.copy(
                selectedTrip = trip,
                trackedTripId = trip.id,
                selectedRoute = trip.route,
                selectedTerminal = null,
                activeBottomSheet = SimulationBottomSheetType.NONE
            )
        }
    }

    fun clearSelectedTrip() {
        _uiState.update { it.copy(selectedTrip = null, trackedTripId = null) }
    }

    fun dispatchBus(bus: Bus, route: BusRoute) {
        val direction = if (bus.currentTerminalId == route.destinationTerminal.id) {
            RouteDirection.DESTINATION_TO_ORIGIN
        } else {
            RouteDirection.ORIGIN_TO_DESTINATION
        }
        val activeTrips = _uiState.value.activeTrips
        val sameRouteTrips = activeTrips.filter {
            it.route.id == route.id && it.direction == direction && !it.isCompleted
        }
        if (sameRouteTrips.any { it.elapsedMinutes < 60 }) {
            return
        }
        executeDispatchAndRevenue(
            bus = bus,
            route = route,
            direction = direction,
            currentTime = _uiState.value.currentTime
        )
        _uiState.update { it.copy(activeBottomSheet = SimulationBottomSheetType.NONE) }
    }

    fun cancelNextSchedule(tripId: String) {
        dispatchBusUseCase.markCancelNextSchedule(tripId, true)
    }

    fun openEditSchedule(bus: Bus, route: BusRoute, headway: Int, dwell: Int) {
        _uiState.update {
            it.copy(
                editingScheduleTarget = SimulationScheduleEditTarget(
                    bus = bus,
                    route = route,
                    currentHeadwayMinutes = headway,
                    currentTransitDwellMinutes = dwell
                )
            )
        }
    }

    fun dismissEditSchedule() {
        _uiState.update { it.copy(editingScheduleTarget = null) }
    }

    fun saveSchedule(headwayMinutes: Int, transitDwellMinutes: Int) {
        val target = _uiState.value.editingScheduleTarget ?: return
        viewModelScope.launch {
            val busId = target.bus.id
            val schedules = routeScheduleRepository?.getSchedulesForBus(busId) ?: emptyList()
            if (schedules.isNotEmpty()) {
                schedules.forEach { sched ->
                    routeScheduleRepository?.updateSchedule(
                        sched.copy(
                            headwayMinutes = headwayMinutes,
                            transitDwellMinutes = transitDwellMinutes
                        )
                    )
                }
            } else {
                val routeSchedules = routeScheduleRepository?.getSchedulesListForRoute(target.route.id) ?: emptyList()
                if (routeSchedules.isNotEmpty()) {
                    routeSchedules.forEach { sched ->
                        routeScheduleRepository?.updateSchedule(
                            sched.copy(
                                headwayMinutes = headwayMinutes,
                                transitDwellMinutes = transitDwellMinutes
                            )
                        )
                    }
                }
            }
            _uiState.update { current ->
                current.copy(
                    editingScheduleTarget = null,
                    schedules = current.schedules.map { sched ->
                        if (sched.assignedBusId == busId || (sched.routeId == target.route.id && sched.assignedBusId == null)) {
                            sched.copy(
                                headwayMinutes = headwayMinutes,
                                transitDwellMinutes = transitDwellMinutes
                            )
                        } else sched
                    }
                )
            }
        }
    }
}
