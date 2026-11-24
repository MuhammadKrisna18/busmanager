package com.example.busmanager.domain.fleet.usecase

import com.example.busmanager.domain.driver.repository.DriverRepository
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.route.repository.RoutePricingRepository

data class BusOperationStatus(
    val hasRoute: Boolean,
    val hasDriver: Boolean,
    val hasPricings: Boolean
) {
    val canOperate: Boolean get() = hasRoute && hasDriver && hasPricings
}

class ValidateBusOperationUseCase(
    private val driverRepository: DriverRepository,
    private val routePricingRepository: RoutePricingRepository
) {
    suspend operator fun invoke(bus: Bus): BusOperationStatus {
        val hasRoute = !bus.routeId.isNullOrBlank()
        val hasDriver = if (!bus.driverId.isNullOrBlank()) {
            val driver = driverRepository.getDriverById(bus.driverId)
            driver != null && driver.isHired && driver.assignedBusId == bus.id
        } else {
            false
        }
        val hasPricings = if (bus.routeId != null) {
            val pricings = routePricingRepository.getPricingsListForRoute(bus.routeId)
            pricings.isNotEmpty()
        } else {
            false
        }

        return BusOperationStatus(
            hasRoute = hasRoute,
            hasDriver = hasDriver,
            hasPricings = hasPricings
        )
    }
}
