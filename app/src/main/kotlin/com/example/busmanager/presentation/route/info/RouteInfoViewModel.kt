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
import com.example.busmanager.domain.route.usecase.DeleteRouteResult
import com.example.busmanager.domain.route.usecase.DeleteRouteUseCase
import com.example.busmanager.domain.route.usecase.GetRouteOperationalStatusUseCase
import com.example.busmanager.domain.simulation.model.RouteDirection
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
    private val routeScheduleRepository: RouteScheduleRepository? = null,
    private val deleteRouteUseCase: DeleteRouteUseCase? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(RouteInfoUiState())
    val uiState: StateFlow<RouteInfoUiState> = _uiState.asStateFlow()

    private val expandedRouteIds = MutableStateFlow<Set<String>>(emptySet())

    init {
        observeRouteInfo()
    }

    private fun observeRouteInfo() {
        val busesFlow = fleetRepository?.getOwnedBuses() ?: flowOf(emptyList())

        viewModelScope.launch {
            combine(
                getRouteOperationalStatusUseCase(),
                routePricingRepository.getAllPricingsFlow(),
                expandedRouteIds,
                busesFlow
            ) { operationalList, allPricings, expandedIds, ownedBuses ->
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

                _uiState.update { state ->
                    state.copy(
                        allRoutes = items,
                        filteredRoutes = filterItems(items, currentFilter),
                        ownedBuses = ownedBuses,
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

    fun openEditScheduleDialog(route: BusRoute) {
        viewModelScope.launch {
            val schedules = routeScheduleRepository?.getSchedulesListForRoute(route.id) ?: emptyList()
            _uiState.update {
                it.copy(
                    selectedRouteForScheduleEdit = route,
                    activeSchedulesForEditing = schedules
                )
            }
        }
    }

    fun closeEditScheduleDialog() {
        _uiState.update {
            it.copy(
                selectedRouteForScheduleEdit = null,
                activeSchedulesForEditing = emptyList()
            )
        }
    }

    fun updateRouteSchedule(schedule: RouteSchedule) {
        viewModelScope.launch {
            val repo = routeScheduleRepository ?: return@launch
            val result = repo.updateSchedule(schedule)
            result.fold(
                onSuccess = {
                    val updatedList = repo.getSchedulesListForRoute(schedule.routeId)
                    _uiState.update {
                        it.copy(
                            activeSchedulesForEditing = updatedList,
                            message = "Jadwal ${schedule.formattedTime} berhasil diperbarui"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(errorMessage = error.message ?: "Gagal memperbarui jadwal")
                    }
                }
            )
        }
    }

    fun addRouteSchedule(
        routeId: String,
        hour: Int,
        minute: Int,
        direction: RouteDirection,
        headwayMinutes: Int,
        assignedBusId: String?
    ) {
        viewModelScope.launch {
            val repo = routeScheduleRepository ?: return@launch
            val scheduleId = "sched-$routeId-${direction.name.lowercase()}-${System.currentTimeMillis()}"
            val newSchedule = RouteSchedule(
                id = scheduleId,
                routeId = routeId,
                departureHour = hour,
                departureMinute = minute,
                direction = direction,
                assignedBusId = assignedBusId,
                headwayMinutes = headwayMinutes,
                transitDwellMinutes = 10
            )
            val result = repo.addSchedule(newSchedule)
            result.fold(
                onSuccess = {
                    val updatedList = repo.getSchedulesListForRoute(routeId)
                    _uiState.update {
                        it.copy(
                            activeSchedulesForEditing = updatedList,
                            message = "Jadwal ${newSchedule.formattedTime} berhasil ditambahkan"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(errorMessage = error.message ?: "Gagal menambah jadwal")
                    }
                }
            )
        }
    }

    fun deleteRouteSchedule(schedule: RouteSchedule) {
        viewModelScope.launch {
            val repo = routeScheduleRepository ?: return@launch
            repo.deleteSchedule(schedule.id)
            val updatedList = repo.getSchedulesListForRoute(schedule.routeId)
            _uiState.update {
                it.copy(
                    activeSchedulesForEditing = updatedList,
                    message = "Jadwal ${schedule.formattedTime} berhasil dihapus"
                )
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun requestDeleteRoute(route: BusRoute) {
        _uiState.update { it.copy(routeToDelete = route) }
    }

    fun dismissDeleteRouteDialog() {
        _uiState.update { it.copy(routeToDelete = null) }
    }

    fun confirmDeleteRoute(routeId: String) {
        viewModelScope.launch {
            val useCase = deleteRouteUseCase ?: return@launch
            when (val result = useCase(routeId)) {
                is DeleteRouteResult.Success -> {
                    _uiState.update {
                        it.copy(
                            routeToDelete = null,
                            message = "Trayek ${result.routeName} berhasil ditutup"
                        )
                    }
                }
                is DeleteRouteResult.RouteHasActiveTrips -> {
                    _uiState.update {
                        it.copy(
                            routeToDelete = null,
                            errorMessage = "Tidak dapat menutup trayek: ${result.activeCount} armada sedang berjalan di rute ini"
                        )
                    }
                }
                is DeleteRouteResult.RouteNotFound -> {
                    _uiState.update {
                        it.copy(
                            routeToDelete = null,
                            errorMessage = "Trayek tidak ditemukan"
                        )
                    }
                }
            }
        }
    }

    private fun filterItems(items: List<RouteDetailItem>, filter: RouteInfoFilter): List<RouteDetailItem> {
        return when (filter) {
            RouteInfoFilter.ALL -> items
            RouteInfoFilter.OPERATING -> items.filter { it.operationalInfo.activeTripsCount > 0 }
            RouteInfoFilter.STANDBY -> items.filter { it.operationalInfo.activeTripsCount == 0 }
        }
    }
}
