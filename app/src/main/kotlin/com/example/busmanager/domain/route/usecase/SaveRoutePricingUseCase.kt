package com.example.busmanager.domain.route.usecase

import com.example.busmanager.domain.route.model.RoutePricing
import com.example.busmanager.domain.route.repository.RoutePricingRepository

class SaveRoutePricingUseCase(
    private val pricingRepository: RoutePricingRepository
) {
    suspend operator fun invoke(pricing: RoutePricing) {
        pricingRepository.savePricing(pricing)
    }

    suspend operator fun invoke(pricings: List<RoutePricing>) {
        pricingRepository.savePricings(pricings)
    }
}
