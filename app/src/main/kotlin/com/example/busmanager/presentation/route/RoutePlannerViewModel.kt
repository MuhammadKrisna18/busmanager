package com.example.busmanager.presentation.route

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.driver.usecase.GetHiredDriversUseCase
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.fleet.usecase.GetOwnedFleetUseCase
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.RoutePathType
import com.example.busmanager.domain.route.model.RoutePricing
import com.example.busmanager.domain.route.model.RouteSchedule
import com.example.busmanager.domain.route.model.Terminal
import com.example.busmanager.domain.route.repository.RouteRepository
import com.example.busmanager.domain.route.usecase.AddRouteScheduleUseCase
import com.example.busmanager.domain.route.usecase.AssignBusToScheduleUseCase
import com.example.busmanager.domain.route.usecase.CalculateRouteCostUseCase
import com.example.busmanager.domain.route.usecase.DeleteRouteScheduleUseCase
import com.example.busmanager.domain.route.usecase.GetAvailableRoutesUseCase
import com.example.busmanager.domain.route.usecase.GetAvailableTerminalsUseCase
import com.example.busmanager.domain.route.usecase.GetRoutePricingsUseCase
import com.example.busmanager.domain.route.usecase.GetRouteSchedulesUseCase
import com.example.busmanager.domain.route.usecase.GetRouteTransitStopsUseCase
import com.example.busmanager.domain.route.usecase.InitializeDefaultRoutePricingsUseCase
import com.example.busmanager.domain.route.usecase.SaveRoutePricingUseCase
import com.example.busmanager.domain.simulation.model.RouteDirection
import kotlinx.coroutines.Job
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
    private val routeRepository: RouteRepository,
    private val getRoutePricingsUseCase: GetRoutePricingsUseCase? = null,
    private val saveRoutePricingUseCase: SaveRoutePricingUseCase? = null,
    private val initializeDefaultRoutePricingsUseCase: InitializeDefaultRoutePricingsUseCase? = null,
    private val getRouteSchedulesUseCase: GetRouteSchedulesUseCase? = null,
    private val addRouteScheduleUseCase: AddRouteScheduleUseCase? = null,
    private val assignBusToScheduleUseCase: AssignBusToScheduleUseCase? = null,
    private val deleteRouteScheduleUseCase: DeleteRouteScheduleUseCase? = null,
    private val getHiredDriversUseCase: GetHiredDriversUseCase? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoutePlannerUiState())
    val uiState: StateFlow<RoutePlannerUiState> = _uiState.asStateFlow()

    private var pricingJob: Job? = null
    private var scheduleJob: Job? = null

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

        viewModelScope.launch {
            getHiredDriversUseCase?.invoke()?.collect { drivers ->
                _uiState.update { it.copy(hiredDrivers = drivers) }
            }
        }
    }

    fun selectTab(tab: RouteDetailTab) {
        _uiState.update { it.copy(activeTab = tab) }
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
        loadRoutePricingsAndSchedules(route)
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

    fun openEditPricing(pricing: RoutePricing) {
        _uiState.update { it.copy(editingPricing = pricing) }
    }

    fun closeEditPricing() {
        _uiState.update { it.copy(editingPricing = null) }
    }

    fun savePricing(pricingId: String, newPriceAmount: Long) {
        viewModelScope.launch {
            val pricing = _uiState.value.pricings.find { it.id == pricingId } ?: return@launch
            val updated = pricing.copy(price = Money.rupiah(newPriceAmount))
            saveRoutePricingUseCase?.invoke(updated)

            val reversePricing = _uiState.value.pricings.find {
                it.routeId == pricing.routeId &&
                    it.originTerminalId == pricing.destinationTerminalId &&
                    it.destinationTerminalId == pricing.originTerminalId
            }
            if (reversePricing != null) {
                saveRoutePricingUseCase?.invoke(reversePricing.copy(price = updated.price))
            }

            _uiState.update {
                it.copy(
                    editingPricing = null,
                    successMessage = "Tarif ${updated.originCity} ⇄ ${updated.destinationCity} berhasil diubah ke ${updated.price.formatRupiah()}"
                )
            }
        }
    }

    fun openAddScheduleDialog() {
        _uiState.update { it.copy(isAddScheduleDialogOpen = true, errorMessage = null) }
    }

    fun closeAddScheduleDialog() {
        _uiState.update { it.copy(isAddScheduleDialogOpen = false, errorMessage = null) }
    }

    fun addSchedule(departureHour: Int, departureMinute: Int, direction: RouteDirection) {
        val route = _uiState.value.selectedRoute ?: return
        viewModelScope.launch {
            val scheduleId = "sched-${route.id}-${direction.name.lowercase()}-$departureHour-$departureMinute-${System.currentTimeMillis()}"
            val schedule = RouteSchedule(
                id = scheduleId,
                routeId = route.id,
                departureHour = departureHour,
                departureMinute = departureMinute,
                direction = direction
            )
            val result = addRouteScheduleUseCase?.invoke(schedule) ?: Result.success(Unit)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isAddScheduleDialogOpen = false,
                        errorMessage = null,
                        successMessage = "Jadwal keberangkatan ${schedule.formattedTime} berhasil ditambahkan!"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        errorMessage = result.exceptionOrNull()?.message ?: "Gagal menambahkan jadwal"
                    )
                }
            }
        }
    }

    fun deleteSchedule(scheduleId: String) {
        viewModelScope.launch {
            deleteRouteScheduleUseCase?.invoke(scheduleId)
            _uiState.update { it.copy(successMessage = "Jadwal keberangkatan berhasil dihapus") }
        }
    }

    fun openAssignBusDialog(schedule: RouteSchedule) {
        _uiState.update { it.copy(selectedScheduleForBusAssignment = schedule) }
    }

    fun closeAssignBusDialog() {
        _uiState.update { it.copy(selectedScheduleForBusAssignment = null) }
    }

    fun assignBusToSchedule(scheduleId: String, busId: String) {
        viewModelScope.launch {
            val success = assignBusToScheduleUseCase?.invoke(scheduleId, busId) ?: false
            if (success) {
                _uiState.update {
                    it.copy(
                        selectedScheduleForBusAssignment = null,
                        successMessage = "Armada berhasil ditugaskan ke jadwal!"
                    )
                }
            } else {
                _uiState.update { it.copy(errorMessage = "Gagal menugaskan armada ke jadwal") }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
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

        if (selected != null) {
            loadRoutePricingsAndSchedules(selected)
        }
    }

    private fun loadRoutePricingsAndSchedules(route: BusRoute) {
        pricingJob?.cancel()
        scheduleJob?.cancel()

        viewModelScope.launch {
            initializeDefaultRoutePricingsUseCase?.invoke(route)
        }

        pricingJob = viewModelScope.launch {
            getRoutePricingsUseCase?.invoke(route.id)?.collect { pricingsList ->
                _uiState.update { it.copy(pricings = pricingsList) }
            }
        }

        scheduleJob = viewModelScope.launch {
            getRouteSchedulesUseCase?.invoke(route.id)?.collect { schedulesList ->
                _uiState.update { it.copy(schedules = schedulesList) }
            }
        }
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
