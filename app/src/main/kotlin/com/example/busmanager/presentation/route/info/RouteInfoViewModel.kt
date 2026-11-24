package com.example.busmanager.presentation.route.info

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.busmanager.domain.route.repository.RoutePricingRepository
import com.example.busmanager.domain.route.usecase.GetRouteOperationalStatusUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RouteInfoViewModel(
    private val getRouteOperationalStatusUseCase: GetRouteOperationalStatusUseCase,
    private val routePricingRepository: RoutePricingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RouteInfoUiState())
    val uiState: StateFlow<RouteInfoUiState> = _uiState.asStateFlow()

    private val expandedRouteIds = MutableStateFlow<Set<String>>(emptySet())

    init {
        observeRouteInfo()
    }

    private fun observeRouteInfo() {
        viewModelScope.launch {
            combine(
                getRouteOperationalStatusUseCase(),
                routePricingRepository.getAllPricingsFlow(),
                expandedRouteIds
            ) { operationalList, allPricings, expandedIds ->
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

    private fun filterItems(items: List<RouteDetailItem>, filter: RouteInfoFilter): List<RouteDetailItem> {
        return when (filter) {
            RouteInfoFilter.ALL -> items
            RouteInfoFilter.OPERATING -> items.filter { it.operationalInfo.activeTripsCount > 0 }
            RouteInfoFilter.STANDBY -> items.filter { it.operationalInfo.activeTripsCount == 0 }
        }
    }
}
