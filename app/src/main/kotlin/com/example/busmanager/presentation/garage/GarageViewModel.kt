package com.example.busmanager.presentation.garage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.busmanager.domain.company.repository.CompanyRepository
import com.example.busmanager.domain.driver.repository.DriverRepository
import com.example.busmanager.domain.fleet.repository.FleetRepository
import com.example.busmanager.domain.fleet.usecase.ReconcileBusOperatingStateUseCase
import com.example.busmanager.domain.fleet.usecase.SellBusResult
import com.example.busmanager.domain.fleet.usecase.SellBusUseCase
import com.example.busmanager.domain.route.repository.RoutePricingRepository
import com.example.busmanager.domain.route.repository.RouteRepository
import com.example.busmanager.domain.route.repository.RouteScheduleRepository
import com.example.busmanager.domain.simulation.model.ActiveBusTrip
import com.example.busmanager.domain.simulation.usecase.GetActiveTripsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.busmanager.domain.driver.model.Driver
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.usecase.AssignBusToActiveRouteUseCase
import com.example.busmanager.domain.route.usecase.GetRouteOperationalStatusUseCase
import com.example.busmanager.domain.route.usecase.ScheduleConflictResult
import com.example.busmanager.domain.route.usecase.ValidateScheduleConflictUseCase
import com.example.busmanager.domain.simulation.model.RouteDirection
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GarageViewModel(
    private val fleetRepository: FleetRepository,
    private val driverRepository: DriverRepository,
    private val routeRepository: RouteRepository,
    private val routePricingRepository: RoutePricingRepository,
    private val companyRepository: CompanyRepository,
    private val getActiveTripsUseCase: GetActiveTripsUseCase? = null,
    private val sellBusUseCase: SellBusUseCase? = null,
    private val reconcileBusOperatingStateUseCase: ReconcileBusOperatingStateUseCase? = null,
    private val routeScheduleRepository: RouteScheduleRepository? = null,
    private val assignBusToActiveRouteUseCase: AssignBusToActiveRouteUseCase? = null,
    private val validateScheduleConflictUseCase: ValidateScheduleConflictUseCase? = null,
    private val getRouteOperationalStatusUseCase: GetRouteOperationalStatusUseCase? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(GarageUiState(isLoading = true))
    val uiState: StateFlow<GarageUiState> = _uiState.asStateFlow()

    init {
        observeGarageData()
    }

    private fun observeGarageData() {
        viewModelScope.launch {
            reconcileBusOperatingStateUseCase?.invoke()
            val activeTripsFlow: Flow<List<ActiveBusTrip>> = getActiveTripsUseCase?.invoke() ?: MutableStateFlow(emptyList())
            val schedulesFlow = routeScheduleRepository?.getAllSchedules() ?: MutableStateFlow(emptyList())
            combine(
                fleetRepository.getOwnedBuses(),
                driverRepository.getAllDrivers(),
                companyRepository.getCompany(),
                activeTripsFlow,
                schedulesFlow
            ) { buses, drivers, company, activeTrips, schedules ->
                val busItems = buses.map { bus ->
                    val activeTrip = activeTrips.find { it.busId == bus.id }
                    val isOperating = activeTrip != null || bus.isOperating
                    val driver = bus.driverId?.let { dId -> drivers.find { it.id == dId } }
                    val route = activeTrip?.route ?: bus.routeId?.let { rId -> routeRepository.getRouteById(rId) }
                    val pricings = (activeTrip?.route?.id ?: bus.routeId)?.let { rId ->
                        routePricingRepository.getPricingsListForRoute(rId)
                    } ?: emptyList()
                    val schedule = schedules.find { it.assignedBusId == bus.id }

                    GarageBusItem(
                        bus = bus,
                        driverName = driver?.name,
                        routeName = route?.name,
                        hasPricings = pricings.isNotEmpty(),
                        isOperating = isOperating,
                        activeTripDirection = activeTrip?.let { "Menuju ${it.currentDestinationName} dari ${it.currentOriginName}" },
                        activeTripProgressPercent = activeTrip?.let { (it.progressFraction * 100).toInt() },
                        headwayMinutes = schedule?.headwayMinutes ?: 60,
                        transitDwellMinutes = schedule?.transitDwellMinutes ?: 10
                    )
                }

                val operatingCount = busItems.count { it.isOnDuty }
                val standbyCount = busItems.count { !it.isOnDuty }
                val currentFilter = _uiState.value.selectedFilter

                _uiState.update { state ->
                    state.copy(
                        allBuses = busItems,
                        filteredBuses = filterBuses(busItems, currentFilter),
                        selectedFilter = currentFilter,
                        totalBusCount = busItems.size,
                        operatingBusCount = operatingCount,
                        standbyBusCount = standbyCount,
                        companyName = company?.name ?: "PO TransNusantara",
                        isLoading = false
                    )
                }
            }.collect {}
        }
    }

    fun setFilter(filter: GarageFilter) {
        _uiState.update { state ->
            state.copy(
                selectedFilter = filter,
                filteredBuses = filterBuses(state.allBuses, filter)
            )
        }
    }

    private fun filterBuses(buses: List<GarageBusItem>, filter: GarageFilter): List<GarageBusItem> {
        return when (filter) {
            GarageFilter.ALL -> buses
            GarageFilter.OPERATING -> buses.filter { it.isOnDuty }
            GarageFilter.STANDBY -> buses.filter { !it.isOnDuty }
        }
    }

    fun sellBus(busId: String, onResult: (SellBusResult) -> Unit = {}) {
        viewModelScope.launch {
            val result = sellBusUseCase?.invoke(busId) ?: SellBusResult.BusNotFound
            onResult(result)
        }
    }

    fun openEditSchedule(item: GarageBusItem) {
        val routeName = item.routeName ?: "Trayek Operasional"
        _uiState.update {
            it.copy(
                editingScheduleTarget = GarageScheduleTarget(
                    bus = item.bus,
                    routeName = routeName,
                    currentHeadwayMinutes = item.headwayMinutes,
                    currentTransitDwellMinutes = item.transitDwellMinutes
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
            } else if (target.bus.routeId != null) {
                val routeSchedules = routeScheduleRepository?.getSchedulesListForRoute(target.bus.routeId) ?: emptyList()
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
            _uiState.update { it.copy(editingScheduleTarget = null) }
        }
    }

    fun openQuickAssignDialog(item: GarageBusItem) {
        viewModelScope.launch {
            val allDrivers = driverRepository.getHiredDrivers().firstOrNull() ?: driverRepository.getAllDrivers().first()
            val allBuses = fleetRepository.getOwnedBuses().first()
            val assignedDriverIds = allBuses.mapNotNull { it.driverId }.toSet()
            val availableDrivers = allDrivers.filter { it.isHired && !assignedDriverIds.contains(it.id) }
            val selectedDriver = availableDrivers.firstOrNull()

            val operationalRoutes = getRouteOperationalStatusUseCase?.invoke()?.firstOrNull() ?: emptyList()
            val activeRoutes = operationalRoutes.filter { it.isActive }.map { it.route }
            val availableRoutes = if (activeRoutes.isNotEmpty()) activeRoutes else routeRepository.getAllRoutes()
            val selectedRoute = availableRoutes.firstOrNull()

            val initialAssignState = QuickAssignState(
                bus = item.bus,
                availableRoutes = availableRoutes,
                selectedRoute = selectedRoute,
                availableDrivers = availableDrivers,
                selectedDriver = selectedDriver,
                departureHour = 7,
                departureMinute = 0,
                direction = RouteDirection.ORIGIN_TO_DESTINATION,
                headwayMinutes = item.headwayMinutes,
                transitDwellMinutes = item.transitDwellMinutes
            )
            _uiState.update { it.copy(quickAssignState = initialAssignState) }
            checkQuickAssignConflictInternal(initialAssignState)
        }
    }

    fun selectQuickAssignRoute(route: BusRoute) {
        val current = _uiState.value.quickAssignState ?: return
        val updated = current.copy(selectedRoute = route)
        _uiState.update { it.copy(quickAssignState = updated) }
        checkQuickAssignConflictInternal(updated)
    }

    fun selectQuickAssignDriver(driver: Driver) {
        _uiState.update { state ->
            state.copy(quickAssignState = state.quickAssignState?.copy(selectedDriver = driver))
        }
    }

    fun setQuickAssignDepartureTime(hour: Int, minute: Int) {
        val current = _uiState.value.quickAssignState ?: return
        val updated = current.copy(
            departureHour = hour.coerceIn(0, 23),
            departureMinute = minute.coerceIn(0, 59)
        )
        _uiState.update { it.copy(quickAssignState = updated) }
        checkQuickAssignConflictInternal(updated)
    }

    fun setQuickAssignDirection(direction: RouteDirection) {
        val current = _uiState.value.quickAssignState ?: return
        val updated = current.copy(direction = direction)
        _uiState.update { it.copy(quickAssignState = updated) }
        checkQuickAssignConflictInternal(updated)
    }

    fun setQuickAssignHeadway(minutes: Int) {
        _uiState.update { state ->
            state.copy(quickAssignState = state.quickAssignState?.copy(headwayMinutes = minutes.coerceIn(30, 240)))
        }
    }

    fun setQuickAssignTransitDwell(minutes: Int) {
        _uiState.update { state ->
            state.copy(quickAssignState = state.quickAssignState?.copy(transitDwellMinutes = minutes.coerceIn(0, 15)))
        }
    }

    private fun checkQuickAssignConflictInternal(target: QuickAssignState) {
        val route = target.selectedRoute ?: return
        val validator = validateScheduleConflictUseCase ?: return
        viewModelScope.launch {
            val result = validator(
                route = route,
                direction = target.direction,
                departureHour = target.departureHour,
                departureMinute = target.departureMinute
            )
            val conflictMsg = when (result) {
                is ScheduleConflictResult.Conflict -> result.errorMessage
                is ScheduleConflictResult.Valid -> null
            }
            _uiState.update { state ->
                val currentAssign = state.quickAssignState
                if (currentAssign != null && currentAssign.bus.id == target.bus.id) {
                    state.copy(quickAssignState = currentAssign.copy(conflictError = conflictMsg))
                } else {
                    state
                }
            }
        }
    }

    fun confirmQuickAssign() {
        val target = _uiState.value.quickAssignState ?: return
        val route = target.selectedRoute ?: return
        val driver = target.selectedDriver ?: return
        val useCase = assignBusToActiveRouteUseCase ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(quickAssignState = it.quickAssignState?.copy(isSubmitting = true)) }
            val result = useCase(
                busId = target.bus.id,
                routeId = route.id,
                driverId = driver.id,
                departureHour = target.departureHour,
                departureMinute = target.departureMinute,
                direction = target.direction,
                headwayMinutes = target.headwayMinutes,
                transitDwellMinutes = target.transitDwellMinutes
            )

            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        quickAssignState = null,
                        userMessage = "Armada ${target.bus.busCode} berhasil ditugaskan ke ${route.name}!"
                    )
                }
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Gagal menugaskan armada ke trayek"
                _uiState.update {
                    it.copy(quickAssignState = it.quickAssignState?.copy(conflictError = errorMsg, isSubmitting = false))
                }
            }
        }
    }

    fun dismissQuickAssignDialog() {
        _uiState.update { it.copy(quickAssignState = null) }
    }

    fun dismissUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }
}
