package com.example.busmanager.presentation.route.info

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.busmanager.domain.driver.repository.DriverRepository
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.fleet.repository.FleetRepository
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.RouteSchedule
import com.example.busmanager.domain.route.repository.RoutePricingRepository
import com.example.busmanager.domain.route.repository.RouteScheduleRepository
import com.example.busmanager.domain.route.usecase.GetRouteOperationalStatusUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RouteInfoViewModel(
    private val getRouteOperationalStatusUseCase: GetRouteOperationalStatusUseCase,
    private val routePricingRepository: RoutePricingRepository,
    private val fleetRepository: FleetRepository? = null,
    private val driverRepository: DriverRepository? = null,
    private val routeScheduleRepository: RouteScheduleRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(RouteInfoUiState())
    val uiState: StateFlow<RouteInfoUiState> = _uiState.asStateFlow()

    private val expandedRouteIds = MutableStateFlow<Set<String>>(emptySet())

    init {
        observeRouteInfo()
    }

    private fun observeRouteInfo() {
        val busesFlow = fleetRepository?.getOwnedBuses() ?: flowOf(emptyList())
        val driversFlow = driverRepository?.getHiredDrivers() ?: flowOf(emptyList())

        viewModelScope.launch {
            combine(
                getRouteOperationalStatusUseCase(),
                routePricingRepository.getAllPricingsFlow(),
                expandedRouteIds,
                busesFlow,
                driversFlow
            ) { operationalList, allPricings, expandedIds, ownedBuses, hiredDrivers ->
                val items = operationalList.map { opInfo ->
                    val routePricings = allPricings.filter { it.routeId == opInfo.route.id }
                    RouteDetailItem(
                        operationalInfo = opInfo,
                        pricings = routePricings,
                        isPricingsExpanded = expandedIds.contains(opInfo.route.id)
                    )
                }

                val operatingCount = items.count { it.operationalInfo.activeTripsCount > 0 }
                val standbyCount = items.size - operatingCount
                val currentFilter = _uiState.value.selectedFilter

                val availableBuses = ownedBuses.filter { it.routeId == null && !it.isOperating }
                val availableDrivers = hiredDrivers.filter { it.assignedBusId == null }

                _uiState.update { state ->
                    state.copy(
                        allRoutes = items,
                        filteredRoutes = filterItems(items, currentFilter),
                        availableBuses = availableBuses,
                        availableDrivers = availableDrivers,
                        selectedFilter = currentFilter,
                        totalRouteCount = items.size,
                        operatingRouteCount = operatingCount,
                        standbyRouteCount = standbyCount,
                        isLoading = false
                    )
                }
            }.collect {}
        }
    }

    fun setFilter(filter: RouteInfoFilter) {
        _uiState.update { state ->
            state.copy(
                selectedFilter = filter,
                filteredRoutes = filterItems(state.allRoutes, filter)
            )
        }
    }

    fun togglePricingsExpanded(routeId: String) {
        expandedRouteIds.update { current ->
            if (current.contains(routeId)) current - routeId else current + routeId
        }
    }

    fun openAssignBusDialog(route: BusRoute) {
        _uiState.update { it.copy(selectedRouteForBusAssign = route) }
    }

    fun closeAssignBusDialog() {
        _uiState.update { it.copy(selectedRouteForBusAssign = null) }
    }

    fun assignBusToRoute(busId: String, routeId: String) {
        viewModelScope.launch {
            val bus = fleetRepository?.getBusById(busId) ?: return@launch
            fleetRepository.updateBus(bus.copy(routeId = routeId))

            val schedules = routeScheduleRepository?.getSchedulesListForRoute(routeId) ?: emptyList()
            val unassignedSchedule = schedules.firstOrNull { it.assignedBusId == null }
            if (unassignedSchedule != null) {
                routeScheduleRepository?.assignBusToSchedule(unassignedSchedule.id, busId)
            }

            _uiState.update {
                it.copy(
                    selectedRouteForBusAssign = null,
                    message = "Armada ${bus.busCode} berhasil ditugaskan ke trayek"
                )
            }
        }
    }

    fun openAssignDriverDialog(route: BusRoute, bus: Bus?) {
        _uiState.update {
            it.copy(
                selectedRouteForDriverAssign = route,
                targetBusForDriverAssign = bus
            )
        }
    }

    fun closeAssignDriverDialog() {
        _uiState.update {
            it.copy(
                selectedRouteForDriverAssign = null,
                targetBusForDriverAssign = null
            )
        }
    }

    fun assignDriverToBus(driverId: String, busId: String) {
        viewModelScope.launch {
            driverRepository?.assignDriverToBus(driverId, busId)
            val driver = driverRepository?.getDriverById(driverId)
            _uiState.update {
                it.copy(
                    selectedRouteForDriverAssign = null,
                    targetBusForDriverAssign = null,
                    message = "Sopir ${driver?.name ?: ""} berhasil ditugaskan ke armada"
                )
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    private fun filterItems(items: List<RouteDetailItem>, filter: RouteInfoFilter): List<RouteDetailItem> {
        return when (filter) {
            RouteInfoFilter.ALL -> items
            RouteInfoFilter.OPERATING -> items.filter { it.operationalInfo.activeTripsCount > 0 }
            RouteInfoFilter.STANDBY -> items.filter { it.operationalInfo.activeTripsCount == 0 }
        }
    }
}
