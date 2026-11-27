package com.example.busmanager.domain.route.repository

import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.RoutePricing
import kotlinx.coroutines.flow.Flow

interface RoutePricingRepository {
    fun getPricingsForRoute(routeId: String): Flow<List<RoutePricing>>
    suspend fun getPricingsListForRoute(routeId: String): List<RoutePricing>
    fun getAllPricingsFlow(): Flow<List<RoutePricing>>
    suspend fun getAllPricings(): List<RoutePricing>
    suspend fun savePricing(pricing: RoutePricing)
    suspend fun savePricings(pricings: List<RoutePricing>)
    suspend fun getPricingBetween(routeId: String, originTerminalId: String, destinationTerminalId: String): RoutePricing?
    suspend fun generateAndSaveDefaultPricings(route: BusRoute): List<RoutePricing>
    suspend fun deletePricingsForRoute(routeId: String) {}
    suspend fun deleteAllPricings()
}
