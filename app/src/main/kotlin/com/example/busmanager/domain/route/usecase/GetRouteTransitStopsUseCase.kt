package com.example.busmanager.domain.route.usecase

import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.TransitStop
import com.example.busmanager.domain.route.repository.RouteRepository

class GetRouteTransitStopsUseCase(
    private val routeRepository: RouteRepository
) {
    operator fun invoke(routeId: String): List<TransitStop> {
        val route = routeRepository.getRouteById(routeId) ?: return emptyList()
        return invoke(route)
    }

    operator fun invoke(route: BusRoute): List<TransitStop> {
        return route.toTransitStops()
    }
}
