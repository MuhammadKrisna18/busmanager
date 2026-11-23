package com.example.busmanager.domain.route.usecase

import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.RoutePathType
import com.example.busmanager.domain.route.repository.RouteRepository

class GetAvailableRoutesUseCase(
    private val routeRepository: RouteRepository
) {
    operator fun invoke(
        originTerminalId: String,
        destinationTerminalId: String,
        preferredPathType: RoutePathType? = null
    ): List<BusRoute> {
        if (originTerminalId.isBlank() || destinationTerminalId.isBlank()) return emptyList()
        if (originTerminalId == destinationTerminalId) return emptyList()

        return routeRepository.findRoutes(
            originTerminalId = originTerminalId,
            destinationTerminalId = destinationTerminalId,
            preferredPathType = preferredPathType
        )
    }
}
