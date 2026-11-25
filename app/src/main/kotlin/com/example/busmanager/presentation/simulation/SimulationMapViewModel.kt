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
    private val recordedTransitExchangeKeys = mutableSetOf<String>()
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
            for (trip in completedTrips) {
                val destTerminal = if (trip.direction == RouteDirection.ORIGIN_TO_DESTINATION) {
                    trip.route.destinationTerminal
                } else {
                    trip.route.originTerminal
                }
                val returnDir = if (trip.direction == RouteDirection.ORIGIN_TO_DESTINATION) {
                    RouteDirection.DESTINATION_TO_ORIGIN
                } else {
                    RouteDirection.ORIGIN_TO_DESTINATION
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
                if (bus != null) {
                    if (trip.cancelNextSchedule) {
                        val updatedBus = bus.copy(
                            isOperating = false,
                            routeId = null,
                            currentTerminalId = destTerminal.id
                        )
                        _uiState.update { current ->
                            current.copy(
                                ownedBuses = current.ownedBuses.map { if (it.id == bus.id) updatedBus else it }
                            )
                        }
                        fleetRepository?.updateBus(updatedBus)
                        routeScheduleRepository?.getSchedulesListForRoute(trip.route.id)
                            ?.filter { it.assignedBusId == bus.id }
                            ?.forEach { routeScheduleRepository.deleteSchedule(it.id) }
                    } else {
                        val updatedBus = bus.copy(
                            isOperating = false,
                            currentTerminalId = destTerminal.id
                        )
                        _uiState.update { current ->
                            current.copy(
                                ownedBuses = current.ownedBuses.map { if (it.id == bus.id) updatedBus else it }
                            )
                        }
                        fleetRepository?.updateBus(updatedBus)

                        val busSchedules = (routeScheduleRepository?.getSchedulesForBus(bus.id)
                            ?.filter { it.routeId == trip.route.id }
                            ?: emptyList()).ifEmpty {
                            _uiState.value.schedules.filter { it.assignedBusId == bus.id && it.routeId == trip.route.id }
                        }

                        val currentTotalMinutes = currentTime.day * 1440L + currentTime.hour * 60L + currentTime.minute
                        val effectiveHeadway = trip.headwayMinutes.coerceAtLeast(30)
                        val effectiveDwell = trip.transitDwellMinutes.coerceIn(0, 15)
                        val rawNextDeparture = currentTotalMinutes + effectiveHeadway
                        var resolvedMinutes = calculateNextOperatingDeparture(rawNextDeparture)
                        var resolvedHour = (((resolvedMinutes) % 1440L) / 60L).toInt()
                        var resolvedMin = ((resolvedMinutes) % 60L).toInt()

                        if (busSchedules.isNotEmpty()) {
                            val returnSchedule = busSchedules.find { it.direction == returnDir }
                            val targetScheduleId = returnSchedule?.id ?: busSchedules.first().id
                            if (routeScheduleRepository != null) {
                                while (routeScheduleRepository.checkConflict(trip.route.id, returnDir, resolvedHour, resolvedMin, targetScheduleId)) {
                                    resolvedMinutes += 10
                                    resolvedMinutes = calculateNextOperatingDeparture(resolvedMinutes)
                                    resolvedHour = (((resolvedMinutes) % 1440L) / 60L).toInt()
                                    resolvedMin = ((resolvedMinutes) % 60L).toInt()
                                }
                            }

                            if (returnSchedule != null) {
                                val updatedReturnSchedule = returnSchedule.copy(
                                    departureHour = resolvedHour,
                                    departureMinute = resolvedMin,
                                    direction = returnDir,
                                    headwayMinutes = effectiveHeadway,
                                    transitDwellMinutes = effectiveDwell,
                                    nextDepartureEpochMinutes = resolvedMinutes
                                )
                                routeScheduleRepository?.updateSchedule(updatedReturnSchedule)

                                val otherSchedules = busSchedules.filter { it.id != returnSchedule.id }
                                otherSchedules.forEach { other ->
                                    val nextOutboundRaw = resolvedMinutes + trip.estimatedArrivalMinutes + effectiveHeadway
                                    val nextOutboundResolved = calculateNextOperatingDeparture(nextOutboundRaw)
                                    val updatedOther = other.copy(
                                        departureHour = (((nextOutboundResolved) % 1440L) / 60L).toInt(),
                                        departureMinute = ((nextOutboundResolved) % 60L).toInt(),
                                        nextDepartureEpochMinutes = nextOutboundResolved
                                    )
                                    routeScheduleRepository?.updateSchedule(updatedOther)
                                }

                                _uiState.update { current ->
                                    current.copy(
                                        schedules = current.schedules.map { s ->
                                            if (s.id == returnSchedule.id) updatedReturnSchedule else s
                                        }
                                    )
                                }
                            } else {
                                val activeSchedule = busSchedules.first()
                                val updatedSchedule = activeSchedule.copy(
                                    departureHour = resolvedHour,
                                    departureMinute = resolvedMin,
                                    direction = returnDir,
                                    headwayMinutes = effectiveHeadway,
                                    transitDwellMinutes = effectiveDwell,
                                    nextDepartureEpochMinutes = resolvedMinutes
                                )
                                routeScheduleRepository?.updateSchedule(updatedSchedule)

                                _uiState.update { current ->
                                    current.copy(
                                        schedules = current.schedules.map { s ->
                                            if (s.id == activeSchedule.id) updatedSchedule else s
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                dispatchBusUseCase.completeTrip(trip.id)
                bus?.driverId?.let { driverId ->
                    val repo = driverRepository ?: return@let
                    val driver = repo.getDriverById(driverId)
                    if (driver?.isPendingRelease == true) {
                        repo.unassignDriver(driverId)
                    }
                }
            }
        }
    }

    private fun handleTransitPassengerExchanges(trips: List<ActiveBusTrip>) {
        if (terminalPassengerRepository == null) return
        viewModelScope.launch {
            val currentTime = _uiState.value.currentTime
            val dateText = "${currentTime.dayOfWeekName()}, ${currentTime.formatDate()}"
            val timeText = currentTime.formatTime()

            for (trip in trips) {
                if (trip.isDwellingAtTransit) {
                    val assignedBus = _uiState.value.ownedBuses.find { it.id == trip.busId }
                    assignedBus?.driverId?.let { driverId ->
                        val repo = driverRepository ?: return@let
                        val driver = repo.getDriverById(driverId)
                        if (driver?.isPendingRelease == true) {
                            repo.unassignDriver(driverId)
                        }
                    }
                }
                for (exchange in trip.transitExchanges) {
                    val exchangeKey = "${trip.id}-${exchange.terminalId}-${exchange.stopIndex}"
                    if (!recordedTransitExchangeKeys.contains(exchangeKey)) {
                        recordedTransitExchangeKeys.add(exchangeKey)
                        terminalPassengerRepository.recordPassengerExchange(
                            TerminalPassengerRecord(
                                id = "rec-term-trans-${trip.busId}-${exchange.terminalId}-${System.nanoTime()}",
                                terminalId = exchange.terminalId,
                                terminalName = exchange.terminalName,
                                busId = trip.busId,
                                busCode = trip.busCode,
                                routeId = trip.route.id,
                                routeName = trip.route.name,
                                day = currentTime.day,
                                month = currentTime.month,
                                year = currentTime.year,
                                dateFormatted = dateText,
                                timeFormatted = timeText,
                                boardedCount = exchange.boardedCount,
                                alightedCount = exchange.alightedCount
                            )
                        )
                    }
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

    private fun calculateNextOperatingDeparture(baseEpochMinutes: Long): Long {
        val minuteOfDay = (baseEpochMinutes % 1440L).toInt()
        return when {
            minuteOfDay < 4 * 60 -> {
                val dayEpoch = (baseEpochMinutes / 1440L) * 1440L
                dayEpoch + 4 * 60L
            }
            minuteOfDay > 23 * 60 + 59 -> {
                val nextDayEpoch = (baseEpochMinutes / 1440L + 1L) * 1440L
                nextDayEpoch + 4 * 60L
            }
            else -> baseEpochMinutes
        }
    }

    private suspend fun handleScheduledDepartures(time: GameCalendar) {
        val isOperatingHours = time.hour in 4..23
        if (!isOperatingHours) return

        val schedules = _uiState.value.schedules
        val buses = _uiState.value.ownedBuses
        val activeTrips = _uiState.value.activeTrips
        val routes = _uiState.value.routes
        val currentTotalMinutes = time.day * 1440L + time.hour * 60L + time.minute
        val currentDayMinutes = time.hour * 60 + time.minute
        val dispatchedBusIds = mutableSetOf<String>()

        for (schedule in schedules) {
            val busId = schedule.assignedBusId ?: continue
            if (dispatchedBusIds.contains(busId)) continue

            val bus = buses.find { it.id == busId } ?: continue
            if (bus.isOperating) continue

            val isAlreadyOnTrip = activeTrips.any { it.busId == bus.id }
            if (isAlreadyOnTrip) continue

            val status = validateBusOperationUseCase?.invoke(bus)
            if (status != null && !status.canOperate) continue

            val route = routes.find { it.id == schedule.routeId } ?: continue

            val expectedDepartureTerminal = if (schedule.direction == RouteDirection.ORIGIN_TO_DESTINATION) {
                route.originTerminal
            } else {
                route.destinationTerminal
            }

            if (bus.currentTerminalId != null && bus.currentTerminalId != expectedDepartureTerminal.id) {
                continue
            }

            val shouldDepart = if (schedule.nextDepartureEpochMinutes > 0L) {
                currentTotalMinutes >= schedule.nextDepartureEpochMinutes
            } else {
                val schedDayMinutes = schedule.departureHour * 60 + schedule.departureMinute
                currentDayMinutes >= schedDayMinutes
            }

            if (shouldDepart) {
                dispatchedBusIds.add(bus.id)
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
        val schedule = _uiState.value.schedules.find {
            it.assignedBusId == bus.id && it.routeId == route.id && it.direction == direction
        } ?: _uiState.value.schedules.find {
            it.assignedBusId == bus.id && it.routeId == route.id
        }
        val headway = schedule?.headwayMinutes ?: 60
        val dwell = schedule?.transitDwellMinutes ?: 10

        val depTerminal = if (direction == RouteDirection.ORIGIN_TO_DESTINATION) {
            route.originTerminal
        } else {
            route.destinationTerminal
        }

        val updatedBus = bus.copy(isOperating = true, currentTerminalId = depTerminal.id)
        _uiState.update { current ->
            current.copy(
                ownedBuses = current.ownedBuses.map { if (it.id == bus.id) updatedBus else it }
            )
        }

        viewModelScope.launch {
            val pricings = routePricingRepository?.getPricingsListForRoute(route.id) ?: emptyList()
            val avgTicketPrice = if (pricings.isNotEmpty()) {
                val avg = pricings.map { it.price.amount }.average().toLong()
                Money.rupiah(avg.coerceAtLeast(30_000L))
            } else {
                Money.rupiah((650L * route.totalDistanceKm).toLong())
            }

            val trip = dispatchBusUseCase(
                bus = bus,
                route = route,
                direction = direction,
                currentTime = currentTime,
                headwayMinutes = headway,
                transitDwellMinutes = dwell,
                ticketPricePerPassenger = avgTicketPrice
            )

            fleetRepository?.updateBus(updatedBus)
            val passengers = trip.passengerCount
            val ticketEarnings = trip.ticketEarnings

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
