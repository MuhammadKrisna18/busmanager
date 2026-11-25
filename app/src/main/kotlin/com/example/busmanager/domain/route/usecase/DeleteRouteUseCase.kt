package com.example.busmanager.domain.route.usecase

import com.example.busmanager.domain.fleet.repository.FleetRepository
import com.example.busmanager.domain.route.repository.RouteRepository
import com.example.busmanager.domain.simulation.repository.SimulationTripRepository
import kotlinx.coroutines.flow.first

sealed class DeleteRouteResult {
    data class Success(val routeName: String) : DeleteRouteResult()
    data class RouteHasActiveTrips(val activeCount: Int) : DeleteRouteResult()
    object RouteNotFound : DeleteRouteResult()
}

class DeleteRouteUseCase(
    private val routeRepository: RouteRepository,
    private val fleetRepository: FleetRepository,
    private val simulationTripRepository: SimulationTripRepository? = null
) {
    suspend operator fun invoke(routeId: String): DeleteRouteResult {
        val route = routeRepository.getRouteById(routeId) ?: return DeleteRouteResult.RouteNotFound
        val activeCount = simulationTripRepository?.activeTrips?.value?.count { it.route.id == routeId } ?: 0
        if (activeCount > 0) {
            return DeleteRouteResult.RouteHasActiveTrips(activeCount)
        }

        val buses = fleetRepository.getOwnedBuses().first()
        buses.filter { it.routeId == routeId }.forEach { bus ->
            fleetRepository.updateBus(bus.copy(routeId = null, isOperating = false))
        }

        routeRepository.deleteRoute(routeId)
        return DeleteRouteResult.Success(route.name)
    }
}
