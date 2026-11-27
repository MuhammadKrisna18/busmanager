package com.example.busmanager.data.repository

import com.example.busmanager.data.local.dao.RoutePricingDao
import com.example.busmanager.data.mapper.toDomain
import com.example.busmanager.data.mapper.toEntity
import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.RoutePricing
import com.example.busmanager.domain.route.repository.RoutePricingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoutePricingRepositoryImpl(
    private val pricingDao: RoutePricingDao
) : RoutePricingRepository {

    override fun getPricingsForRoute(routeId: String): Flow<List<RoutePricing>> {
        return pricingDao.getPricingsByRouteFlow(routeId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getPricingsListForRoute(routeId: String): List<RoutePricing> {
        return pricingDao.getPricingsByRoute(routeId).map { it.toDomain() }
    }

    override fun getAllPricingsFlow(): Flow<List<RoutePricing>> {
        return pricingDao.getAllPricingsFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getAllPricings(): List<RoutePricing> {
        return pricingDao.getAllPricings().map { it.toDomain() }
    }

    override suspend fun savePricing(pricing: RoutePricing) {
        pricingDao.insertPricing(pricing.toEntity())
    }

    override suspend fun savePricings(pricings: List<RoutePricing>) {
        pricingDao.insertPricings(pricings.map { it.toEntity() })
    }

    override suspend fun getPricingBetween(
        routeId: String,
        originTerminalId: String,
        destinationTerminalId: String
    ): RoutePricing? {
        return pricingDao.getPricing(routeId, originTerminalId, destinationTerminalId)?.toDomain()
    }

    override suspend fun generateAndSaveDefaultPricings(route: BusRoute): List<RoutePricing> {
        val allStops = buildList {
            add(route.originTerminal)
            addAll(route.transitTerminals)
            add(route.destinationTerminal)
        }

        val generatedPricings = mutableListOf<RoutePricing>()

        for (i in 0 until allStops.size) {
            for (j in i + 1 until allStops.size) {
                val origin = allStops[i]
                val dest = allStops[j]
                val hops = (j - i).toLong()
                val priceAmount = (hops * 50_000L).coerceAtLeast(50_000L)

                val forward = RoutePricing(
                    id = "price-${route.id}-${origin.id}-${dest.id}",
                    routeId = route.id,
                    originTerminalId = origin.id,
                    destinationTerminalId = dest.id,
                    originCity = origin.city,
                    destinationCity = dest.city,
                    price = Money.rupiah(priceAmount)
                )

                val backward = RoutePricing(
                    id = "price-${route.id}-${dest.id}-${origin.id}",
                    routeId = route.id,
                    originTerminalId = dest.id,
                    destinationTerminalId = origin.id,
                    originCity = dest.city,
                    destinationCity = origin.city,
                    price = Money.rupiah(priceAmount)
                )

                generatedPricings.add(forward)
                generatedPricings.add(backward)
            }
        }

        pricingDao.insertPricings(generatedPricings.map { it.toEntity() })
        return generatedPricings
    }

    override suspend fun deletePricingsForRoute(routeId: String) {
        pricingDao.deletePricingsByRoute(routeId)
    }

    override suspend fun deleteAllPricings() {
        pricingDao.deleteAllPricings()
    }
}
