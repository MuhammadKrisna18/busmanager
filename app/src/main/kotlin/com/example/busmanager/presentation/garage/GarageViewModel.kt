package com.example.busmanager.presentation.garage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.busmanager.domain.company.repository.CompanyRepository
import com.example.busmanager.domain.driver.repository.DriverRepository
import com.example.busmanager.domain.fleet.repository.FleetRepository
import com.example.busmanager.domain.route.repository.RoutePricingRepository
import com.example.busmanager.domain.route.repository.RouteRepository
import com.example.busmanager.domain.simulation.model.ActiveBusTrip
import com.example.busmanager.domain.simulation.usecase.GetActiveTripsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GarageViewModel(
    private val fleetRepository: FleetRepository,
    private val driverRepository: DriverRepository,
    private val routeRepository: RouteRepository,
    private val routePricingRepository: RoutePricingRepository,
    private val companyRepository: CompanyRepository,
    private val getActiveTripsUseCase: GetActiveTripsUseCase? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(GarageUiState(isLoading = true))
    val uiState: StateFlow<GarageUiState> = _uiState.asStateFlow()

    init {
        observeGarageData()
    }

    private fun observeGarageData() {
        viewModelScope.launch {
            val activeTripsFlow: Flow<List<ActiveBusTrip>> = getActiveTripsUseCase?.invoke() ?: MutableStateFlow(emptyList())
            combine(
                fleetRepository.getOwnedBuses(),
                driverRepository.getAllDrivers(),
                companyRepository.getCompany(),
                activeTripsFlow
            ) { buses, drivers, company, activeTrips ->
                val busItems = buses.map { bus ->
                    val activeTrip = activeTrips.find { it.busId == bus.id }
                    val isOperating = activeTrip != null || bus.isOperating
                    val driver = bus.driverId?.let { dId -> drivers.find { it.id == dId } }
                    val route = activeTrip?.route ?: bus.routeId?.let { rId -> routeRepository.getRouteById(rId) }
                    val pricings = (activeTrip?.route?.id ?: bus.routeId)?.let { rId ->
                        routePricingRepository.getPricingsListForRoute(rId)
                    } ?: emptyList()

                    GarageBusItem(
                        bus = bus,
                        driverName = driver?.name,
                        routeName = route?.name,
                        hasPricings = pricings.isNotEmpty(),
                        isOperating = isOperating,
                        activeTripDirection = activeTrip?.let { "${it.currentOriginName} ➔ ${it.currentDestinationName}" },
                        activeTripProgressPercent = activeTrip?.let { (it.progressFraction * 100).toInt() }
                    )
                }

                val operatingCount = busItems.count { it.isOperating }
                val standbyCount = busItems.count { !it.isOperating }
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
            GarageFilter.OPERATING -> buses.filter { it.isOperating }
            GarageFilter.STANDBY -> buses.filter { !it.isOperating }
        }
    }
}
