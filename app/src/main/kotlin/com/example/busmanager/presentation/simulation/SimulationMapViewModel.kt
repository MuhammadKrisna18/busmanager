package com.example.busmanager.presentation.simulation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.company.repository.CompanyRepository
import com.example.busmanager.domain.company.usecase.GetCompanyUseCase
import com.example.busmanager.domain.driver.repository.DriverRepository
import com.example.busmanager.domain.finance.model.FinancialTransaction
import com.example.busmanager.domain.finance.model.TransactionCategory
import com.example.busmanager.domain.finance.model.TransactionType
import com.example.busmanager.domain.finance.usecase.RecordFinancialTransactionUseCase
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.fleet.repository.FleetRepository
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
import com.example.busmanager.domain.simulation.model.SimulationSpeed
import com.example.busmanager.domain.simulation.usecase.DispatchBusUseCase
import com.example.busmanager.domain.simulation.usecase.GetActiveTripsUseCase
import com.example.busmanager.domain.simulation.usecase.GetSimulationTimeUseCase
import com.example.busmanager.domain.simulation.usecase.SetSimulationSpeedUseCase
import com.example.busmanager.domain.simulation.usecase.ToggleSimulationPlayUseCase
import com.example.busmanager.domain.terminal.model.TerminalPassengerRecord
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

    private val dispatchedSchedulesInCurrentDay = mutableSetOf<String>()
    private var lastObservedDay = -1

    init {
        observeSimulationEngine()
        observeCompanyAndFleet()
        observeActiveTrips()
        observeDriversAndSchedules()
        observePricings()
        loadMapData()
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
                val completedTrips = trips.filter { it.isCompleted }
                val ongoingTrips = trips.filterNot { it.isCompleted }

                if (completedTrips.isNotEmpty()) {
                    handleCompletedTrips(completedTrips)
                }

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
            for (trip in completedTrips) {
                val destTerminal = if (trip.direction == RouteDirection.ORIGIN_TO_DESTINATION) {
                    trip.route.destinationTerminal
                } else {
                    trip.route.originTerminal
                }

                val currentTime = _uiState.value.currentTime
                val dateText = "${currentTime.dayOfWeekName()}, ${currentTime.formatDate()}"
                val timeText = currentTime.formatTime()

                terminalPassengerRepository?.recordPassengerExchange(
                    TerminalPassengerRecord(
                        id = "rec-term-arr-${trip.busId}-${System.nanoTime()}",
                        terminalId = destTerminal.id,
                        terminalName = destTerminal.name,
                        busId = trip.busId,
                        busCode = trip.busCode,
                        routeId = trip.route.id,
                        routeName = trip.route.name,
                        day = currentTime.day,
                        month = currentTime.month,
                        year = currentTime.year,
                        dateFormatted = dateText,
                        timeFormatted = timeText,
                        boardedCount = 0,
                        alightedCount = trip.passengerCount
                    )
                )

                val bus = _uiState.value.ownedBuses.find { it.id == trip.busId }
                if (bus != null && fleetRepository != null) {
                    if (trip.cancelNextSchedule) {
                        routeScheduleRepository?.getSchedulesListForRoute(trip.route.id)
                            ?.filter { it.assignedBusId == bus.id }
                            ?.forEach { routeScheduleRepository.deleteSchedule(it.id) }
                        fleetRepository.updateBus(
                            bus.copy(
                                isOperating = false,
                                routeId = null,
                                currentTerminalId = destTerminal.id
                            )
                        )
                    } else {
                        fleetRepository.updateBus(
                            bus.copy(
                                isOperating = false,
                                currentTerminalId = destTerminal.id
                            )
                        )
                        val busSchedules = routeScheduleRepository?.getSchedulesForBus(bus.id)
                        val schedule = busSchedules?.firstOrNull { it.routeId == trip.route.id }
                            ?: busSchedules?.firstOrNull()
                        if (schedule != null && routeScheduleRepository != null) {
                            val currentTotalMinutes = currentTime.day * 1440L + currentTime.hour * 60L + currentTime.minute
                            val nextDepMinutes = currentTotalMinutes + schedule.headwayMinutes
                            val nextDepHour = ((currentTime.hour * 60 + currentTime.minute + schedule.headwayMinutes) / 60) % 24
                            val nextDepMin = (currentTime.minute + schedule.headwayMinutes) % 60
                            val returnDir = if (trip.direction == RouteDirection.ORIGIN_TO_DESTINATION) {
                                RouteDirection.DESTINATION_TO_ORIGIN
                            } else {
                                RouteDirection.ORIGIN_TO_DESTINATION
                            }
                            routeScheduleRepository.updateSchedule(
                                schedule.copy(
                                    departureHour = nextDepHour,
                                    departureMinute = nextDepMin,
                                    direction = returnDir,
                                    nextDepartureEpochMinutes = nextDepMinutes
                                )
                            )
                        }
                    }
                }

                dispatchBusUseCase.completeTrip(trip.id)
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

    private suspend fun handleScheduledDepartures(time: GameCalendar) {
        val schedules = _uiState.value.schedules
        val buses = _uiState.value.ownedBuses
        val activeTrips = _uiState.value.activeTrips
        val routes = _uiState.value.routes
        val currentTotalMinutes = time.day * 1440L + time.hour * 60L + time.minute

        for (schedule in schedules) {
            val busId = schedule.assignedBusId ?: continue
            val bus = buses.find { it.id == busId } ?: continue
            val isAlreadyOnTrip = activeTrips.any { it.busId == bus.id }
            if (isAlreadyOnTrip) continue

            val status = validateBusOperationUseCase?.invoke(bus)
            if (status != null && !status.canOperate) continue

            val route = routes.find { it.id == schedule.routeId } ?: continue

            val shouldDepart = if (schedule.nextDepartureEpochMinutes <= 0L) {
                true
            } else {
                currentTotalMinutes >= schedule.nextDepartureEpochMinutes
            }

            if (shouldDepart) {
                val direction = if (bus.currentTerminalId == route.destinationTerminal.id) {
                    RouteDirection.DESTINATION_TO_ORIGIN
                } else {
                    RouteDirection.ORIGIN_TO_DESTINATION
                }
                executeDispatchAndRevenue(bus, route, direction, time)
            }
        }
    }

    private fun executeDispatchAndRevenue(
        bus: Bus,
        route: BusRoute,
        direction: RouteDirection,
        currentTime: GameCalendar
    ) {
        val schedule = _uiState.value.schedules.find { it.assignedBusId == bus.id && it.routeId == route.id }
        val headway = schedule?.headwayMinutes ?: 60
        val dwell = schedule?.transitDwellMinutes ?: 10
        val trip = dispatchBusUseCase(
            bus = bus,
            route = route,
            direction = direction,
            currentTime = currentTime,
            headwayMinutes = headway,
            transitDwellMinutes = dwell
        )

        viewModelScope.launch {
            fleetRepository?.updateBus(bus.copy(isOperating = true))
            val pricings = routePricingRepository?.getPricingsListForRoute(route.id) ?: emptyList()
            val avgTicketPrice = if (pricings.isNotEmpty()) {
                pricings.map { it.price.amount }.average().toLong().coerceAtLeast(50_000L)
            } else {
                75_000L
            }
            val passengers = trip.passengerCount
            val ticketEarnings = Money.rupiah(avgTicketPrice * passengers)

            val dateText = "${currentTime.dayOfWeekName()}, ${currentTime.formatDate()}"
            val timeText = currentTime.formatTime()
            val ticketTransaction = FinancialTransaction(
                id = "tx-ticket-${bus.id}-${System.nanoTime()}",
                dateText = dateText,
                timeText = timeText,
                day = currentTime.day,
                month = currentTime.month,
                year = currentTime.year,
                type = TransactionType.INCOME,
                category = TransactionCategory.TICKET_SALE,
                amount = ticketEarnings,
                busId = bus.id,
                busCode = bus.busCode,
                routeId = route.id,
                routeName = route.name,
                description = "Tiket ${route.name} (${bus.busCode})"
            )

            if (recordFinancialTransactionUseCase != null) {
                recordFinancialTransactionUseCase.invoke(ticketTransaction)
            } else {
                companyRepository?.addBalance(ticketEarnings)
            }

            val driverWageAmount = (ticketEarnings.amount * 0.30).toLong().coerceAtLeast(0L)
            if (driverWageAmount > 0L) {
                val driverWage = Money.rupiah(driverWageAmount)
                val driver = bus.driverId?.let { driverRepository?.getDriverById(it) }
                    ?: _uiState.value.drivers.find { it.id == bus.driverId }
                val driverName = driver?.name ?: bus.busCode
                val wageTransaction = FinancialTransaction(
                    id = "tx-wage-${bus.id}-${System.nanoTime()}",
                    dateText = dateText,
                    timeText = timeText,
                    day = currentTime.day,
                    month = currentTime.month,
                    year = currentTime.year,
                    type = TransactionType.EXPENSE,
                    category = TransactionCategory.DRIVER_WAGE,
                    amount = driverWage,
                    busId = bus.id,
                    busCode = bus.busCode,
                    routeId = route.id,
                    routeName = route.name,
                    description = "Bagi hasil 30% sopir: $driverName"
                )

                if (recordFinancialTransactionUseCase != null) {
                    recordFinancialTransactionUseCase.invoke(wageTransaction)
                } else {
                    companyRepository?.deductBalance(driverWage)
                }
            }

            val originTerminal = if (direction == RouteDirection.ORIGIN_TO_DESTINATION) {
                route.originTerminal
            } else {
                route.destinationTerminal
            }
            terminalPassengerRepository?.recordPassengerExchange(
                TerminalPassengerRecord(
                    id = "rec-term-${bus.id}-${System.nanoTime()}",
                    terminalId = originTerminal.id,
                    terminalName = originTerminal.name,
                    busId = bus.id,
                    busCode = bus.busCode,
                    routeId = route.id,
                    routeName = route.name,
                    day = currentTime.day,
                    month = currentTime.month,
                    year = currentTime.year,
                    dateFormatted = dateText,
                    timeFormatted = timeText,
                    boardedCount = passengers,
                    alightedCount = 0
                )
            )
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
}
