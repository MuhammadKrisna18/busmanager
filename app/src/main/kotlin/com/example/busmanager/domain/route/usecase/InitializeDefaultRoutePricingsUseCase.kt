package com.example.busmanager.domain.route.usecase

import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.RoutePricing
import com.example.busmanager.domain.route.repository.RoutePricingRepository

class InitializeDefaultRoutePricingsUseCase(
    private val pricingRepository: RoutePricingRepository
) {
    suspend operator fun invoke(route: BusRoute): List<RoutePricing> {
        val existing = pricingRepository.getPricingsListForRoute(route.id)
        if (existing.isNotEmpty()) {
            return existing
        }
        return pricingRepository.generateAndSaveDefaultPricings(route)
    }
}
