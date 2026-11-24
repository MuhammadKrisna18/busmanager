package com.example.busmanager.domain.route.usecase

import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.repository.RouteRepository

class SaveCustomRouteUseCase(
    private val routeRepository: RouteRepository
) {
    operator fun invoke(route: BusRoute) {
        routeRepository.saveRoute(route)
    }
}
