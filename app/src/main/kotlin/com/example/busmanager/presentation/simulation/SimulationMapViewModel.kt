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
    private val terminalPassengerRepository: TerminalPassengerRepository? = null
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
                _uiState.update { current ->
                    val updatedSelectedTrip = current.selectedTrip?.let { sel -> trips.find { it.id == sel.id } }
                    val updatedTrackedId = if (trips.any { it.id == current.trackedTripId }) current.trackedTripId else null
                    current.copy(
                        activeTrips = trips,
                        activeBusCount = trips.size,
                        selectedTrip = updatedSelectedTrip,
                        trackedTripId = updatedTrackedId
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
        if (time.day != lastObservedDay) {
            dispatchedSchedulesInCurrentDay.clear()
            lastObservedDay = time.day
        }

        val schedules = _uiState.value.schedules
        val buses = _uiState.value.ownedBuses
        val activeTrips = _uiState.value.activeTrips
        val routes = _uiState.value.routes

        for (schedule in schedules) {
            val busId = schedule.assignedBusId ?: continue
            val scheduleKey = "${schedule.id}-${time.day}"
            if (dispatchedSchedulesInCurrentDay.contains(scheduleKey)) continue

            val bus = buses.find { it.id == busId } ?: continue
            val isAlreadyOnTrip = activeTrips.any { it.busId == bus.id }
            if (isAlreadyOnTrip) continue

            val status = validateBusOperationUseCase?.invoke(bus)
            if (status != null && !status.canOperate) continue

            val scheduleMinutes = schedule.departureHour * 60 + schedule.departureMinute
            val currentMinutes = time.hour * 60 + time.minute
            val isTimeMatching = currentMinutes >= scheduleMinutes && (currentMinutes - scheduleMinutes) <= 30

            if (isTimeMatching) {
                val route = routes.find { it.id == schedule.routeId } ?: continue
                dispatchedSchedulesInCurrentDay.add(scheduleKey)
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
        val trip = dispatchBusUseCase(
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
        executeDispatchAndRevenue(
            bus = bus,
            route = route,
            direction = RouteDirection.ORIGIN_TO_DESTINATION,
            currentTime = _uiState.value.currentTime
        )
        _uiState.update { it.copy(activeBottomSheet = SimulationBottomSheetType.NONE) }
    }
}
