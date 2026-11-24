package com.example.busmanager.domain.route.usecase

import com.example.busmanager.domain.route.model.RoutePricing
import com.example.busmanager.domain.route.repository.RoutePricingRepository
import kotlinx.coroutines.flow.Flow

class GetRoutePricingsUseCase(
    private val pricingRepository: RoutePricingRepository
) {
    operator fun invoke(routeId: String): Flow<List<RoutePricing>> {
        return pricingRepository.getPricingsForRoute(routeId)
    }
}
