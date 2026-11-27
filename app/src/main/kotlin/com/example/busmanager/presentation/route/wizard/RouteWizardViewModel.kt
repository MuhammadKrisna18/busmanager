package com.example.busmanager.presentation.route.wizard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.driver.model.Driver
import com.example.busmanager.domain.driver.repository.DriverRepository
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.fleet.repository.FleetRepository
import com.example.busmanager.domain.route.model.DepartureSlotCalculator
import com.example.busmanager.domain.route.model.OccupiedSlotInfo
import com.example.busmanager.domain.route.model.RouteCategory
import com.example.busmanager.domain.route.model.RoutePathType
import com.example.busmanager.domain.route.model.RoutePricing
import com.example.busmanager.domain.route.model.SmartScheduleTimeParser
import com.example.busmanager.domain.route.model.Terminal
import com.example.busmanager.domain.route.model.TerminalDepartureSlot
import com.example.busmanager.domain.route.repository.RouteRepository
import com.example.busmanager.domain.route.repository.RouteScheduleRepository
import com.example.busmanager.domain.route.repository.TerminalRepository
import com.example.busmanager.domain.route.usecase.BuildCustomRouteUseCase
import com.example.busmanager.domain.route.usecase.FindAvailablePathsUseCase
import com.example.busmanager.domain.route.usecase.LaunchCustomRouteParams
import com.example.busmanager.domain.route.usecase.LaunchCustomRouteUseCase
import com.example.busmanager.domain.route.usecase.SaveCustomRouteUseCase
import com.example.busmanager.domain.route.usecase.SaveRoutePricingUseCase
import com.example.busmanager.domain.simulation.model.RouteDirection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RouteWizardViewModel(
    private val terminalRepository: TerminalRepository,
    private val routeRepository: RouteRepository,
    private val findAvailablePathsUseCase: FindAvailablePathsUseCase,
    private val buildCustomRouteUseCase: BuildCustomRouteUseCase,
    private val saveCustomRouteUseCase: SaveCustomRouteUseCase,
    private val saveRoutePricingUseCase: SaveRoutePricingUseCase,
    private val launchCustomRouteUseCase: LaunchCustomRouteUseCase,
    private val routeScheduleRepository: RouteScheduleRepository,
    private val driverRepository: DriverRepository,
    private val fleetRepository: FleetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RouteWizardUiState())
    val uiState: StateFlow<RouteWizardUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        val terminals = terminalRepository.getAllTerminals()
        val segments = routeRepository.getAllSegments()
        _uiState.update { it.copy(allTerminals = terminals, allSegments = segments) }

        viewModelScope.launch {
            combine(
                fleetRepository.getOwnedBuses(),
                driverRepository.getHiredDrivers()
            ) { buses, drivers ->
                Pair(buses, drivers)
            }.collect { (buses, drivers) ->
                _uiState.update { state ->
                    val eligibleBuses = buses.filter { !it.isOperating && !it.hasRoute && !it.hasDriver }
                    val selectedBus = state.selectedBus?.takeIf { sel -> eligibleBuses.any { it.id == sel.id } }
                        ?: eligibleBuses.firstOrNull()

                    val hiredDrivers = drivers.filter { it.isHired }

                    val assignmentMap = mutableMapOf<String, String>()
                    hiredDrivers.forEach { driver ->
                        val bus = buses.find { it.driverId == driver.id || it.id == driver.assignedBusId }
                        if (bus != null) {
                            assignmentMap[driver.id] = bus.busCode
                        }
                    }

                    val eligibleDrivers = hiredDrivers.filter { driver ->
                        assignmentMap[driver.id] == null && !driver.isAssigned
                    }
                    val selectedDriver = state.selectedDriver?.takeIf { sel -> eligibleDrivers.any { it.id == sel.id } }
                        ?: eligibleDrivers.firstOrNull()

                    state.copy(
                        availableBuses = eligibleBuses,
                        selectedBus = selectedBus,
                        availableDrivers = eligibleDrivers,
                        selectedDriver = selectedDriver,
                        driverAssignedBusCodes = assignmentMap
                    )
                }
            }
        }
    }

    fun selectOrigin(terminal: Terminal?) {
        _uiState.update { state ->
            val nodes = if (terminal != null) listOf(terminal) else emptyList()
            val updated = state.copy(selectedOrigin = terminal, selectedPathNodes = nodes, errorMessage = null)
            if (terminal != null && updated.selectedDestination != null && updated.selectedDestination.id != terminal.id) {
                calculatePathsForTerminals(terminal, updated.selectedDestination, updated)
            } else {
                updated.copy(availablePaths = emptyList(), builtRoute = null, pricings = emptyList())
            }
        }
    }

    fun selectDestination(terminal: Terminal?) {
        _uiState.update { state ->
            val updated = state.copy(selectedDestination = terminal, errorMessage = null)
            if (terminal != null && updated.selectedOrigin != null && updated.selectedOrigin.id != terminal.id) {
                calculatePathsForTerminals(updated.selectedOrigin, terminal, updated)
            } else {
                updated.copy(availablePaths = emptyList(), builtRoute = null, pricings = emptyList())
            }
        }
    }

    fun onTerminalMapClick(terminal: Terminal) {
        val state = _uiState.value
        val origin = state.selectedOrigin
        val destination = state.selectedDestination

        if (origin == null) {
            selectOrigin(terminal)
        } else if (destination == null) {
            if (terminal.id == origin.id) {
                selectOrigin(null)
            } else {
                selectDestination(terminal)
            }
        } else {
            if (terminal.id == destination.id) {
                selectDestination(null)
            } else if (terminal.id == origin.id) {
                selectOrigin(null)
            } else {
                selectDestination(terminal)
            }
        }
    }

    fun swapOriginDestination() {
        _uiState.update { state ->
            val origin = state.selectedOrigin
            val dest = state.selectedDestination
            if (origin != null && dest != null && origin.id != dest.id) {
                val swapped = state.copy(
                    selectedOrigin = dest,
                    selectedDestination = origin,
                    selectedPathNodes = listOf(dest),
                    errorMessage = null
                )
                calculatePathsForTerminals(dest, origin, swapped)
            } else {
                state
            }
        }
    }

    fun resetOriginDestination() {
        _uiState.update {
            it.copy(
                selectedOrigin = null,
                selectedDestination = null,
                selectedPathNodes = emptyList(),
                builtRoute = null,
                pricings = emptyList(),
                availablePaths = emptyList(),
                activeTransitIds = emptySet(),
                errorMessage = null
            )
        }
    }

    fun selectFirstTerminal(terminal: Terminal) {
        selectOrigin(terminal)
    }

    fun appendPathNode(terminal: Terminal) {
        selectDestination(terminal)
    }

    fun undoLastPathNode() {
        val state = _uiState.value
        if (state.selectedDestination != null) {
            selectDestination(null)
        } else if (state.selectedOrigin != null) {
            selectOrigin(null)
        }
    }

    fun resetPathNodes() {
        resetOriginDestination()
    }

    fun setIsExpress(isExpress: Boolean) {
        _uiState.update { state ->
            val updated = state.copy(isExpress = isExpress)
            rebuildRouteInternal(updated)
        }
    }

    private fun calculatePathsForTerminals(
        origin: Terminal,
        destination: Terminal,
        currentState: RouteWizardUiState
    ): RouteWizardUiState {
        val paths = findAvailablePathsUseCase(origin.id, destination.id)
        if (paths.isEmpty()) {
            return currentState.copy(
                availablePaths = emptyList(),
                builtRoute = null,
                pricings = emptyList(),
                errorMessage = "Tidak ditemukan jalur jalan terhubung antara ${origin.name} dan ${destination.name}"
            )
        }

        val firstPath = paths.first()
        val intermediateTerminals = if (firstPath.size > 2) {
            firstPath.subList(1, firstPath.size - 1).map { it.id }.toSet()
        } else {
            emptySet()
        }

        val intermediateState = currentState.copy(
            selectedPathNodes = firstPath,
            availablePaths = paths,
            selectedPathIndex = 0,
            activeTransitIds = intermediateTerminals,
            errorMessage = null
        )

        return rebuildRouteInternal(intermediateState)
    }

    fun selectPath(index: Int) {
        _uiState.update { state ->
            if (index !in state.availablePaths.indices) return@update state
            val path = state.availablePaths[index]
            val intermediateTerminals = if (path.size > 2) {
                path.subList(1, path.size - 1).map { it.id }.toSet()
            } else {
                emptySet()
            }
            val updated = state.copy(
                selectedPathNodes = path,
                selectedPathIndex = index,
                activeTransitIds = if (state.isExpress) emptySet() else intermediateTerminals
            )
            rebuildRouteInternal(updated)
        }
    }

    fun toggleTransitStop(terminalId: String) {
        _uiState.update { state ->
            val updatedTransit = if (terminalId in state.activeTransitIds && !state.isExpress) {
                state.activeTransitIds - terminalId
            } else {
                state.activeTransitIds + terminalId
            }
            val updated = state.copy(activeTransitIds = updatedTransit, isExpress = updatedTransit.isEmpty())
            rebuildRouteInternal(updated)
        }
    }

    fun selectAllTransitStops() {
        _uiState.update { state ->
            if (state.selectedPathIndex !in state.availablePaths.indices) return@update state
            val path = state.availablePaths[state.selectedPathIndex]
            val intermediate = if (path.size > 2) {
                path.subList(1, path.size - 1).map { it.id }.toSet()
            } else {
                emptySet()
            }
            val updated = state.copy(activeTransitIds = intermediate, isExpress = false)
            rebuildRouteInternal(updated)
        }
    }

    fun clearAllTransitStops() {
        _uiState.update { state ->
            val updated = state.copy(activeTransitIds = emptySet(), isExpress = true)
            rebuildRouteInternal(updated)
        }
    }

    fun setSegmentPathType(fromId: String, toId: String, pathType: RoutePathType) {
        _uiState.update { state ->
            val updatedMap = state.segmentPathTypes + (Pair(fromId, toId) to pathType)
            val updated = state.copy(segmentPathTypes = updatedMap)
            rebuildRouteInternal(updated)
        }
    }

    private fun rebuildRouteInternal(state: RouteWizardUiState): RouteWizardUiState {
        if (state.selectedPathIndex !in state.availablePaths.indices) {
            return state.copy(builtRoute = null, pricings = emptyList())
        }
        val currentPath = state.availablePaths[state.selectedPathIndex]
        val buildResult = buildCustomRouteUseCase(
            terminals = currentPath,
            preferredSegmentPathTypes = state.segmentPathTypes,
            activeTransitTerminalIds = state.activeTransitIds,
            isExpress = state.isExpress
        )

        return if (buildResult.isSuccess) {
            val route = buildResult.getOrThrow()
            val category = RouteCategory.fromRoute(route)
            val generatedPricings = generateHopPricings(route, state.pricings)
            val intermediate = state.copy(
                builtRoute = route,
                routeCategory = category,
                pricings = generatedPricings,
                errorMessage = null
            )
            if (intermediate.isRoundTrip && intermediate.layoverMinutes < 30) {
                intermediate.copy(
                    returnHour = intermediate.minReturnHour,
                    returnMinute = intermediate.minReturnMinute
                )
            } else {
                intermediate
            }
        } else {
            state.copy(
                builtRoute = null,
                pricings = emptyList(),
                errorMessage = buildResult.exceptionOrNull()?.message
            )
        }
    }

    private fun generateHopPricings(
        route: com.example.busmanager.domain.route.model.BusRoute,
        existingPricings: List<RoutePricing> = emptyList()
    ): List<RoutePricing> {
        val stops = listOf(route.originTerminal) + route.transitTerminals + listOf(route.destinationTerminal)
        val pricings = mutableListOf<RoutePricing>()
        val existingMap = existingPricings.associateBy { it.originTerminalId to it.destinationTerminalId }

        for (i in 0 until stops.size) {
            for (j in i + 1 until stops.size) {
                val origin = stops[i]
                val dest = stops[j]
                val hops = (j - i).toLong()
                val defaultPrice = (hops * 50_000L).coerceAtLeast(50_000L)

                val forwardPrice = existingMap[origin.id to dest.id]?.price ?: Money.rupiah(defaultPrice)
                val reversePrice = existingMap[dest.id to origin.id]?.price ?: forwardPrice

                pricings.add(
                    RoutePricing(
                        id = "price-${route.id}-${origin.id}-${dest.id}",
                        routeId = route.id,
                        originTerminalId = origin.id,
                        destinationTerminalId = dest.id,
                        originCity = origin.city,
                        destinationCity = dest.city,
                        price = forwardPrice
                    )
                )
                pricings.add(
                    RoutePricing(
                        id = "price-${route.id}-${dest.id}-${origin.id}",
                        routeId = route.id,
                        originTerminalId = dest.id,
                        destinationTerminalId = origin.id,
                        originCity = dest.city,
                        destinationCity = origin.city,
                        price = reversePrice
                    )
                )
            }
        }
        return pricings
    }

    fun resetPricingsToDefault() {
        _uiState.update { state ->
            val route = state.builtRoute ?: return@update state
            val resetPricings = generateHopPricings(route, emptyList())
            state.copy(pricings = resetPricings)
        }
    }

    fun startEditingPricing(pricing: RoutePricing?) {
        _uiState.update { it.copy(editingPricing = pricing) }
    }

    fun saveEditedPrice(pricing: RoutePricing, newAmount: Long) {
        val validAmount = newAmount.coerceAtLeast(10_000L)
        val updatedPrice = Money.rupiah(validAmount)

        _uiState.update { state ->
            val updatedList = state.pricings.map { item ->
                val isForward = (item.originTerminalId == pricing.originTerminalId && item.destinationTerminalId == pricing.destinationTerminalId) || item.id == pricing.id
                val isReverse = (item.originTerminalId == pricing.destinationTerminalId && item.destinationTerminalId == pricing.originTerminalId)
                if (isForward || isReverse) {
                    item.copy(price = updatedPrice)
                } else {
                    item
                }
            }
            state.copy(pricings = updatedList, editingPricing = null)
        }
    }

    fun selectBus(bus: Bus?) {
        if (bus == null) {
            _uiState.update { it.copy(selectedBus = null, errorMessage = null) }
            return
        }
        if (bus.isOperating || bus.hasRoute || bus.hasDriver) return
        _uiState.update { current ->
            val updated = current.copy(selectedBus = bus, errorMessage = null)
            if (current.selectedOrigin == null && bus.currentTerminalId != null) {
                val originTerm = current.allTerminals.find { it.id == bus.currentTerminalId }
                if (originTerm != null) {
                    updated.copy(selectedOrigin = originTerm)
                } else {
                    updated
                }
            } else {
                updated
            }
        }
    }

    fun selectDriver(driver: Driver?) {
        if (driver == null) {
            _uiState.update { it.copy(selectedDriver = null) }
            return
        }
        val assignedBusCode = _uiState.value.driverAssignedBusCodes[driver.id]
        if (assignedBusCode != null || driver.isAssigned) return
        _uiState.update { it.copy(selectedDriver = driver) }
    }

    fun setIsRoundTrip(enabled: Boolean) {
        _uiState.update { current ->
            if (enabled && current.layoverMinutes < 60) {
                current.copy(
                    isRoundTrip = true,
                    returnHour = current.minReturnHour,
                    returnMinute = current.minReturnMinute
                )
            } else {
                current.copy(isRoundTrip = enabled)
            }
        }
        checkScheduleConflict()
    }

    fun setDepartureTime(hour: Int, minute: Int) {
        val h = hour.coerceIn(4, 23)
        val m = minute.coerceIn(0, 59)
        val parsed = SmartScheduleTimeParser.ParsedTime(h, m)
        _uiState.update { current ->
            val updatedDeparture = current.copy(
                departureHour = h,
                departureMinute = m,
                departureTimeInputText = String.format("%02d:%02d", h, m),
                departureTimeDescription = parsed.descriptionText
            )
            if (updatedDeparture.isRoundTrip && updatedDeparture.layoverMinutes < 30) {
                updatedDeparture.copy(
                    returnHour = updatedDeparture.minReturnHour,
                    returnMinute = updatedDeparture.minReturnMinute
                )
            } else {
                updatedDeparture
            }
        }
        checkScheduleConflict()
    }

    fun setDepartureTimeInput(input: String) {
        val parsed = SmartScheduleTimeParser.parse(input)
        _uiState.update { current ->
            if (parsed != null) {
                val updatedDeparture = current.copy(
                    departureHour = parsed.hour,
                    departureMinute = parsed.minute,
                    departureTimeInputText = input,
                    departureTimeDescription = parsed.descriptionText
                )
                if (updatedDeparture.isRoundTrip && updatedDeparture.layoverMinutes < 30) {
                    updatedDeparture.copy(
                        returnHour = updatedDeparture.minReturnHour,
                        returnMinute = updatedDeparture.minReturnMinute
                    )
                } else {
                    updatedDeparture
                }
            } else {
                current.copy(departureTimeInputText = input)
            }
        }
        checkScheduleConflict()
    }

    fun setReturnTime(hour: Int, minute: Int) {
        val h = hour.coerceIn(4, 23)
        val m = minute.coerceIn(0, 59)
        _uiState.update {
            it.copy(
                returnHour = h,
                returnMinute = m
            )
        }
        checkScheduleConflict()
    }

    fun openConfirmationDialog() {
        _uiState.update { it.copy(showConfirmationDialog = true) }
    }

    fun dismissConfirmationDialog() {
        _uiState.update { it.copy(showConfirmationDialog = false) }
    }

    fun resetReturnTimeToRecommendation() {
        _uiState.update {
            it.copy(
                returnHour = it.minReturnHour,
                returnMinute = it.minReturnMinute
            )
        }
        checkScheduleConflict()
    }

    fun setDirection(direction: RouteDirection) {
        _uiState.update { it.copy(direction = direction) }
        checkScheduleConflict()
    }

    fun setHeadwayMinutes(minutes: Int) {
        _uiState.update { it.copy(headwayMinutes = minutes.coerceIn(30, 240)) }
    }

    fun setTransitDwellMinutes(minutes: Int) {
        _uiState.update { it.copy(transitDwellMinutes = minutes.coerceIn(0, 15)) }
    }

    private fun checkScheduleConflict() {
        val state = _uiState.value
        val routeId = state.builtRoute?.id ?: return
        viewModelScope.launch {
            val outboundConflict = routeScheduleRepository.checkConflict(
                routeId = routeId,
                direction = state.direction,
                hour = state.departureHour,
                minute = state.departureMinute
            )
            val returnConflict = if (state.isRoundTrip) {
                routeScheduleRepository.checkConflict(
                    routeId = routeId,
                    direction = state.returnDirection,
                    hour = state.returnHour,
                    minute = state.returnMinute
                )
            } else {
                false
            }
            _uiState.update {
                it.copy(
                    hasOutboundConflict = outboundConflict,
                    hasReturnConflict = returnConflict
                )
            }
        }
    }

    fun nextStep() {
        val state = _uiState.value
        when (state.currentStep) {
            RouteWizardStep.ORIGIN_DESTINATION -> {
                if (state.canProceedFromStep1) {
                    _uiState.update { it.copy(currentStep = RouteWizardStep.PATH_AND_STOPS, errorMessage = null) }
                } else {
                    _uiState.update { it.copy(errorMessage = "Pilih terminal asal dan tujuan yang berbeda terlebih dahulu") }
                }
            }
            RouteWizardStep.PATH_AND_STOPS -> {
                if (state.canProceedFromStep2) {
                    _uiState.update { it.copy(currentStep = RouteWizardStep.PRICING_MATRIX, errorMessage = null) }
                } else {
                    _uiState.update { it.copy(errorMessage = "Jalur rute belum terbentuk dengan valid") }
                }
            }
            RouteWizardStep.PRICING_MATRIX -> {
                if (state.canProceedFromStep3) {
                    _uiState.update { it.copy(currentStep = RouteWizardStep.DEPARTURE_SCHEDULE, errorMessage = null) }
                    refreshDepartureSlots()
                } else {
                    _uiState.update { it.copy(errorMessage = "Daftar tarif tiket belum siap") }
                }
            }
            RouteWizardStep.DEPARTURE_SCHEDULE -> {
                if (state.canProceedFromStep4) {
                    _uiState.update { it.copy(currentStep = RouteWizardStep.BUS_DRIVER_ASSIGNMENT, errorMessage = null) }
                    checkScheduleConflict()
                } else {
                    _uiState.update { it.copy(errorMessage = "Pilih slot keberangkatan terminal yang tersedia") }
                }
            }
            RouteWizardStep.BUS_DRIVER_ASSIGNMENT -> {}
        }
    }

    fun previousStep() {
        _uiState.update { state ->
            val prev = when (state.currentStep) {
                RouteWizardStep.ORIGIN_DESTINATION -> RouteWizardStep.ORIGIN_DESTINATION
                RouteWizardStep.PATH_AND_STOPS -> RouteWizardStep.ORIGIN_DESTINATION
                RouteWizardStep.PRICING_MATRIX -> RouteWizardStep.PATH_AND_STOPS
                RouteWizardStep.DEPARTURE_SCHEDULE -> RouteWizardStep.PRICING_MATRIX
                RouteWizardStep.BUS_DRIVER_ASSIGNMENT -> RouteWizardStep.DEPARTURE_SCHEDULE
            }
            state.copy(currentStep = prev, errorMessage = null)
        }
    }

    fun goToStep(step: RouteWizardStep) {
        val state = _uiState.value
        if (step.stepNumber < state.currentStep.stepNumber) {
            _uiState.update { it.copy(currentStep = step, errorMessage = null) }
            return
        }
        when (step) {
            RouteWizardStep.ORIGIN_DESTINATION -> {
                _uiState.update { it.copy(currentStep = step, errorMessage = null) }
            }
            RouteWizardStep.PATH_AND_STOPS -> {
                if (state.canProceedFromStep1) {
                    _uiState.update { it.copy(currentStep = step, errorMessage = null) }
                }
            }
            RouteWizardStep.PRICING_MATRIX -> {
                if (state.canProceedFromStep1 && state.canProceedFromStep2) {
                    _uiState.update { it.copy(currentStep = step, errorMessage = null) }
                }
            }
            RouteWizardStep.DEPARTURE_SCHEDULE -> {
                if (state.canProceedFromStep1 && state.canProceedFromStep2 && state.canProceedFromStep3) {
                    _uiState.update { it.copy(currentStep = step, errorMessage = null) }
                    refreshDepartureSlots()
                }
            }
            RouteWizardStep.BUS_DRIVER_ASSIGNMENT -> {
                if (state.canProceedFromStep1 && state.canProceedFromStep2 && state.canProceedFromStep3 && state.canProceedFromStep4) {
                    _uiState.update { it.copy(currentStep = step, errorMessage = null) }
                    checkScheduleConflict()
                }
            }
        }
    }

    fun refreshDepartureSlots() {
        val state = _uiState.value
        val route = state.builtRoute ?: return
        val origin = state.selectedOrigin ?: return
        val dest = state.selectedDestination ?: return
        val category = RouteCategory.fromRoute(route)

        viewModelScope.launch {
            val allSchedules = routeScheduleRepository.getAllSchedules().first()
            val allRoutes = routeRepository.getAllRoutes()
            val routesMap = allRoutes.associateBy { it.id }

            val occupiedSlots = allSchedules.mapNotNull { schedule ->
                val r = routesMap[schedule.routeId] ?: return@mapNotNull null
                val scheduleOriginTerminalId = if (schedule.direction == RouteDirection.ORIGIN_TO_DESTINATION) {
                    r.originTerminal.id
                } else {
                    r.destinationTerminal.id
                }
                val scheduleDestCity = if (schedule.direction == RouteDirection.ORIGIN_TO_DESTINATION) {
                    r.destinationTerminal.city
                } else {
                    r.originTerminal.city
                }

                OccupiedSlotInfo(
                    terminalId = scheduleOriginTerminalId,
                    hour = schedule.departureHour,
                    minute = schedule.departureMinute,
                    destinationCity = scheduleDestCity,
                    routeId = schedule.routeId,
                    busCode = schedule.assignedBusId
                )
            }

            val slots = DepartureSlotCalculator.generate24HourSlots(
                terminalId = origin.id,
                targetDestinationCity = dest.city,
                existingOccupiedSlots = occupiedSlots
            )

            val currentSelected = state.selectedDepartureSlot
            val stillAvailable = currentSelected != null && slots.any { it.slotId == currentSelected.slotId && it.isAvailable }

            val chosenSlot = if (stillAvailable) {
                slots.first { it.slotId == currentSelected.slotId }
            } else {
                DepartureSlotCalculator.findNextAvailableSlot(slots, state.departureHour, state.departureMinute)
                    ?: slots.firstOrNull { it.isAvailable }
            }

            val targetRit = if (category == RouteCategory.AKAP) 1 else state.targetRitCount.coerceIn(1, 2)
            val updatedPlan = if (chosenSlot != null) {
                DepartureSlotCalculator.calculateDailyPlan(
                    routeCategory = category,
                    originTerminalId = origin.id,
                    destTerminalId = dest.id,
                    estimatedDurationMinutes = state.estimatedDurationMinutes,
                    departureHour = chosenSlot.hour,
                    departureMinute = chosenSlot.minute,
                    targetRitCount = targetRit
                )
            } else null

            val returnLeg = updatedPlan?.legs?.firstOrNull { it.direction == RouteDirection.DESTINATION_TO_ORIGIN }

            _uiState.update { curr ->
                curr.copy(
                    routeCategory = category,
                    targetRitCount = targetRit,
                    availableDepartureSlots = slots,
                    selectedDepartureSlot = chosenSlot,
                    departureHour = chosenSlot?.hour ?: curr.departureHour,
                    departureMinute = chosenSlot?.minute ?: curr.departureMinute,
                    departureTimeInputText = chosenSlot?.formattedTime ?: curr.departureTimeInputText,
                    departureTimeDescription = chosenSlot?.let { "${it.formattedTime} WIB (${it.formattedClockPeriod})" } ?: curr.departureTimeDescription,
                    isRoundTrip = true,
                    returnHour = returnLeg?.departureHour ?: curr.returnHour,
                    returnMinute = returnLeg?.departureMinute ?: curr.returnMinute,
                    dailySchedulePlan = updatedPlan
                )
            }
        }
    }

    fun selectDepartureSlot(slot: TerminalDepartureSlot) {
        val state = _uiState.value
        val slots = state.availableDepartureSlots
        val effectiveSlot = if (slot.isAvailable) {
            slot
        } else {
            val (nextH, nextM) = DepartureSlotCalculator.alignToNext15MinuteSlot(slot.totalMinutesOfDay + DepartureSlotCalculator.SLOT_INTERVAL_MINUTES)
            val nextSlot = DepartureSlotCalculator.findNextAvailableSlot(slots, nextH, nextM)
                ?: DepartureSlotCalculator.findNextAvailableSlot(slots, slot.hour, slot.minute)
            if (nextSlot != null) {
                _uiState.update { it.copy(errorMessage = "Slot ${slot.formattedTime} tujuan ${slot.occupiedDestinationCity ?: "sama"} telah terisi. Dialihkan ke slot ${nextSlot.formattedTime}.") }
                nextSlot
            } else {
                _uiState.update { it.copy(errorMessage = "Slot ${slot.formattedTime} telah terisi dan tidak ada slot alternatif.") }
                return
            }
        }
        applyDepartureSlot(effectiveSlot)
    }

    private fun applyDepartureSlot(slot: TerminalDepartureSlot) {
        val state = _uiState.value
        val origin = state.selectedOrigin ?: return
        val dest = state.selectedDestination ?: return
        val category = state.routeCategory

        val targetRit = if (category == RouteCategory.AKAP) 1 else state.targetRitCount.coerceIn(1, 2)
        val plan = DepartureSlotCalculator.calculateDailyPlan(
            routeCategory = category,
            originTerminalId = origin.id,
            destTerminalId = dest.id,
            estimatedDurationMinutes = state.estimatedDurationMinutes,
            departureHour = slot.hour,
            departureMinute = slot.minute,
            targetRitCount = targetRit
        )

        val returnLeg = plan.legs.firstOrNull { it.direction == RouteDirection.DESTINATION_TO_ORIGIN }

        _uiState.update { curr ->
            curr.copy(
                selectedDepartureSlot = slot,
                departureHour = slot.hour,
                departureMinute = slot.minute,
                departureTimeInputText = slot.formattedTime,
                departureTimeDescription = "${slot.formattedTime} WIB (${slot.formattedClockPeriod})",
                isRoundTrip = true,
                returnHour = returnLeg?.departureHour ?: curr.returnHour,
                returnMinute = returnLeg?.departureMinute ?: curr.returnMinute,
                dailySchedulePlan = plan
            )
        }
    }

    fun setTargetRitCount(ritCount: Int) {
        _uiState.update { curr ->
            if (curr.routeCategory == RouteCategory.AKAP) return@update curr
            val safeCount = ritCount.coerceIn(1, 2)
            val slot = curr.selectedDepartureSlot ?: return@update curr.copy(targetRitCount = safeCount)
            val origin = curr.selectedOrigin ?: return@update curr.copy(targetRitCount = safeCount)
            val dest = curr.selectedDestination ?: return@update curr.copy(targetRitCount = safeCount)

            val updatedPlan = DepartureSlotCalculator.calculateDailyPlan(
                routeCategory = curr.routeCategory,
                originTerminalId = origin.id,
                destTerminalId = dest.id,
                estimatedDurationMinutes = curr.estimatedDurationMinutes,
                departureHour = slot.hour,
                departureMinute = slot.minute,
                targetRitCount = safeCount
            )

            curr.copy(
                targetRitCount = safeCount,
                dailySchedulePlan = updatedPlan
            )
        }
    }

    fun setSlotTimeFilter(filter: SlotTimeFilter) {
        _uiState.update { it.copy(selectedTimeFilter = filter) }
    }

    fun launchAndSaveRoute(onSuccess: (String) -> Unit) {
        val state = _uiState.value
        val route = state.builtRoute ?: return
        val bus = state.selectedBus ?: return
        val driver = state.selectedDriver ?: return

        if (!state.isSelectedBusAvailable || !state.isSelectedDriverAvailable || state.hasConflict || state.isSaving) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                launchCustomRouteUseCase(
                    LaunchCustomRouteParams(
                        route = route,
                        pricings = state.pricings,
                        bus = bus,
                        driver = driver,
                        departureHour = state.departureHour,
                        departureMinute = state.departureMinute,
                        direction = state.direction,
                        headwayMinutes = state.headwayMinutes,
                        transitDwellMinutes = state.transitDwellMinutes,
                        isRoundTrip = state.isRoundTrip,
                        returnHour = state.returnHour,
                        returnMinute = state.returnMinute,
                        returnDirection = state.returnDirection,
                        dailySchedulePlan = state.dailySchedulePlan
                    )
                )

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        launchSuccess = true,
                        launchedRouteId = route.id
                    )
                }
                onSuccess(route.id)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Gagal meluncurkan trayek: ${e.message}"
                    )
                }
            }
        }
    }

    fun saveRouteStandby(onSuccess: (String) -> Unit) {
        val state = _uiState.value
        val route = state.builtRoute ?: return
        if (state.pricings.isEmpty() || state.isSaving) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                saveCustomRouteUseCase(route)
                saveRoutePricingUseCase(state.pricings)
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        launchSuccess = true,
                        isStandbySaved = true,
                        launchedRouteId = route.id
                    )
                }
                onSuccess(route.id)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Gagal menyimpan trayek standby: ${e.message}"
                    )
                }
            }
        }
    }

    fun selectRouteCategory(category: RouteCategory) {
        _uiState.update { it.copy(routeCategory = category) }
    }

    fun dismissSuccessDialog() {
        resetWizard()
    }

    fun resetWizard() {
        _uiState.update { current ->
            RouteWizardUiState(
                currentStep = RouteWizardStep.ORIGIN_DESTINATION,
                allTerminals = current.allTerminals,
                availableBuses = current.availableBuses,
                availableDrivers = current.availableDrivers,
                selectedBus = current.availableBuses.firstOrNull(),
                selectedDriver = current.availableDrivers.firstOrNull(),
                launchSuccess = false,
                isStandbySaved = false,
                launchedRouteId = null
            )
        }
    }
}
