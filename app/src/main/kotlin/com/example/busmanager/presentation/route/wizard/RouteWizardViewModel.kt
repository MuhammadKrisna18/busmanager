package com.example.busmanager.presentation.route.wizard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.driver.model.Driver
import com.example.busmanager.domain.driver.repository.DriverRepository
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.fleet.repository.FleetRepository
import com.example.busmanager.domain.route.model.RoutePathType
import com.example.busmanager.domain.route.model.RoutePricing
import com.example.busmanager.domain.route.model.RouteSchedule
import com.example.busmanager.domain.route.model.Terminal
import com.example.busmanager.domain.route.repository.RoutePricingRepository
import com.example.busmanager.domain.route.repository.RouteRepository
import com.example.busmanager.domain.route.repository.RouteScheduleRepository
import com.example.busmanager.domain.route.repository.TerminalRepository
import com.example.busmanager.domain.route.usecase.BuildCustomRouteUseCase
import com.example.busmanager.domain.route.usecase.FindAvailablePathsUseCase
import com.example.busmanager.domain.route.usecase.SaveCustomRouteUseCase
import com.example.busmanager.domain.simulation.model.RouteDirection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RouteWizardViewModel(
    private val terminalRepository: TerminalRepository,
    private val routeRepository: RouteRepository,
    private val findAvailablePathsUseCase: FindAvailablePathsUseCase,
    private val buildCustomRouteUseCase: BuildCustomRouteUseCase,
    private val saveCustomRouteUseCase: SaveCustomRouteUseCase,
    private val routePricingRepository: RoutePricingRepository,
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
        _uiState.update { it.copy(allTerminals = terminals) }

        viewModelScope.launch {
            combine(
                fleetRepository.getOwnedBuses(),
                driverRepository.getHiredDrivers()
            ) { buses, drivers ->
                Pair(buses, drivers)
            }.collect { (buses, drivers) ->
                _uiState.update { state ->
                    val availableBuses = buses.filter { !it.isOperating }
                    val selectedBus = state.selectedBus?.let { sel -> availableBuses.find { it.id == sel.id } } ?: availableBuses.firstOrNull()

                    val hiredDrivers = drivers.filter { it.isHired }

                    val assignmentMap = mutableMapOf<String, String>()
                    hiredDrivers.forEach { driver ->
                        val bus = buses.find { it.driverId == driver.id || it.id == driver.assignedBusId }
                        if (bus != null) {
                            assignmentMap[driver.id] = bus.busCode
                        }
                    }

                    val selectedDriver = state.selectedDriver?.let { sel ->
                        val assignedBusCode = assignmentMap[sel.id]
                        if (assignedBusCode == null || (selectedBus != null && assignedBusCode == selectedBus.busCode)) {
                            hiredDrivers.find { it.id == sel.id }
                        } else null
                    } ?: hiredDrivers.firstOrNull { driver ->
                        val assignedBusCode = assignmentMap[driver.id]
                        assignedBusCode == null || (selectedBus != null && assignedBusCode == selectedBus.busCode)
                    }

                    state.copy(
                        availableBuses = availableBuses,
                        selectedBus = selectedBus,
                        availableDrivers = hiredDrivers,
                        selectedDriver = selectedDriver,
                        driverAssignedBusCodes = assignmentMap
                    )
                }
            }
        }
    }

    fun selectOrigin(terminal: Terminal) {
        _uiState.update {
            val updated = it.copy(selectedOrigin = terminal, errorMessage = null)
            if (updated.selectedDestination != null && updated.selectedDestination.id != terminal.id) {
                calculatePathsForTerminals(terminal, updated.selectedDestination, updated)
            } else {
                updated.copy(availablePaths = emptyList(), builtRoute = null, pricings = emptyList())
            }
        }
    }

    fun selectDestination(terminal: Terminal) {
        _uiState.update {
            val updated = it.copy(selectedDestination = terminal, errorMessage = null)
            if (updated.selectedOrigin != null && updated.selectedOrigin.id != terminal.id) {
                calculatePathsForTerminals(updated.selectedOrigin, terminal, updated)
            } else {
                updated.copy(availablePaths = emptyList(), builtRoute = null, pricings = emptyList())
            }
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
                selectedPathIndex = index,
                activeTransitIds = intermediateTerminals
            )
            rebuildRouteInternal(updated)
        }
    }

    fun toggleTransitStop(terminalId: String) {
        _uiState.update { state ->
            val updatedTransit = if (terminalId in state.activeTransitIds) {
                state.activeTransitIds - terminalId
            } else {
                state.activeTransitIds + terminalId
            }
            val updated = state.copy(activeTransitIds = updatedTransit)
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
            activeTransitTerminalIds = state.activeTransitIds
        )

        return if (buildResult.isSuccess) {
            val route = buildResult.getOrThrow()
            val generatedPricings = generateHopPricings(route)
            val intermediate = state.copy(
                builtRoute = route,
                pricings = generatedPricings,
                errorMessage = null
            )
            if (intermediate.isRoundTrip && intermediate.layoverMinutes < 60) {
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

    private fun generateHopPricings(route: com.example.busmanager.domain.route.model.BusRoute): List<RoutePricing> {
        val stops = listOf(route.originTerminal) + route.transitTerminals + listOf(route.destinationTerminal)
        val pricings = mutableListOf<RoutePricing>()

        for (i in 0 until stops.size) {
            for (j in i + 1 until stops.size) {
                val origin = stops[i]
                val dest = stops[j]
                val hops = (j - i).toLong()
                val priceAmount = (hops * 50_000L).coerceAtLeast(50_000L)

                pricings.add(
                    RoutePricing(
                        id = "price-${route.id}-${origin.id}-${dest.id}",
                        routeId = route.id,
                        originTerminalId = origin.id,
                        destinationTerminalId = dest.id,
                        originCity = origin.city,
                        destinationCity = dest.city,
                        price = Money.rupiah(priceAmount)
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
                        price = Money.rupiah(priceAmount)
                    )
                )
            }
        }
        return pricings
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

    fun selectBus(bus: Bus) {
        _uiState.update { current ->
            val assignedBusCode = current.driverAssignedBusCodes[current.selectedDriver?.id]
            val isCurrentDriverConflicted = assignedBusCode != null && assignedBusCode != bus.busCode
            val updatedDriver = if (isCurrentDriverConflicted) {
                current.availableDrivers.firstOrNull { d ->
                    val code = current.driverAssignedBusCodes[d.id]
                    code == null || code == bus.busCode
                }
            } else {
                current.selectedDriver
            }
            current.copy(selectedBus = bus, selectedDriver = updatedDriver)
        }
    }

    fun selectDriver(driver: Driver) {
        val assignedBusCode = _uiState.value.driverAssignedBusCodes[driver.id]
        val currentBus = _uiState.value.selectedBus
        val isAssignedToOtherBus = assignedBusCode != null && (currentBus == null || assignedBusCode != currentBus.busCode)
        if (isAssignedToOtherBus) return
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
        val h = hour.coerceIn(0, 23)
        val m = minute.coerceIn(0, 59)
        _uiState.update { current ->
            val updatedDeparture = current.copy(departureHour = h, departureMinute = m)
            if (updatedDeparture.isRoundTrip && updatedDeparture.layoverMinutes < 60) {
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

    fun setReturnTime(hour: Int, minute: Int) {
        _uiState.update {
            it.copy(
                returnHour = hour.coerceIn(0, 23),
                returnMinute = minute.coerceIn(0, 59)
            )
        }
        checkScheduleConflict()
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
                    _uiState.update { it.copy(currentStep = RouteWizardStep.BUS_DRIVER_SCHEDULE, errorMessage = null) }
                    checkScheduleConflict()
                } else {
                    _uiState.update { it.copy(errorMessage = "Daftar tarif tiket belum siap") }
                }
            }
            RouteWizardStep.BUS_DRIVER_SCHEDULE -> {}
        }
    }

    fun previousStep() {
        _uiState.update { state ->
            val prev = when (state.currentStep) {
                RouteWizardStep.ORIGIN_DESTINATION -> RouteWizardStep.ORIGIN_DESTINATION
                RouteWizardStep.PATH_AND_STOPS -> RouteWizardStep.ORIGIN_DESTINATION
                RouteWizardStep.PRICING_MATRIX -> RouteWizardStep.PATH_AND_STOPS
                RouteWizardStep.BUS_DRIVER_SCHEDULE -> RouteWizardStep.PRICING_MATRIX
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
            RouteWizardStep.BUS_DRIVER_SCHEDULE -> {
                if (state.canProceedFromStep1 && state.canProceedFromStep2 && state.canProceedFromStep3) {
                    _uiState.update { it.copy(currentStep = step, errorMessage = null) }
                    checkScheduleConflict()
                }
            }
        }
    }

    fun launchAndSaveRoute(onSuccess: (String) -> Unit) {
        val state = _uiState.value
        val route = state.builtRoute ?: return
        val bus = state.selectedBus ?: return
        val driver = state.selectedDriver ?: return

        if (state.hasConflict || state.isSaving) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                saveCustomRouteUseCase(route)
                routePricingRepository.savePricings(state.pricings)

                driverRepository.assignDriverToBus(driver.id, bus.id)
                fleetRepository.updateBus(
                    bus.copy(
                        routeId = route.id,
                        driverId = driver.id
                    )
                )

                val baseTime = System.currentTimeMillis()
                val outboundScheduleId = "sched-${route.id}-outbound-$baseTime"
                val outboundSchedule = RouteSchedule(
                    id = outboundScheduleId,
                    routeId = route.id,
                    departureHour = state.departureHour,
                    departureMinute = state.departureMinute,
                    direction = state.direction,
                    assignedBusId = bus.id
                )
                routeScheduleRepository.addSchedule(outboundSchedule)
                routeScheduleRepository.assignBusToSchedule(outboundScheduleId, bus.id)

                if (state.isRoundTrip) {
                    val returnScheduleId = "sched-${route.id}-return-${baseTime + 1}"
                    val returnSchedule = RouteSchedule(
                        id = returnScheduleId,
                        routeId = route.id,
                        departureHour = state.returnHour,
                        departureMinute = state.returnMinute,
                        direction = state.returnDirection,
                        assignedBusId = bus.id
                    )
                    routeScheduleRepository.addSchedule(returnSchedule)
                    routeScheduleRepository.assignBusToSchedule(returnScheduleId, bus.id)
                }

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
                launchedRouteId = null
            )
        }
    }
}
