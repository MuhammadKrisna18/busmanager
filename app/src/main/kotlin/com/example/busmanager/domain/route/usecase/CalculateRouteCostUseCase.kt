package com.example.busmanager.domain.route.usecase

import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.RouteTripEstimate

class CalculateRouteCostUseCase(
    private val solarPricePerLiter: Money = Money.rupiah(6800)
) {
    operator fun invoke(route: BusRoute, bus: Bus): RouteTripEstimate {
        val totalDistance = route.totalDistanceKm
        val baseKmPerLiter = bus.chassis.fuelConsumptionKmPerLiter.toDouble().coerceAtLeast(1.0)

        var totalLiters = 0.0
        route.segments.forEach { segment ->
            val segmentLiters = (segment.distanceKm / baseKmPerLiter) * segment.fuelMultiplier
            totalLiters += segmentLiters
        }

        val fuelCostAmount = (totalLiters * solarPricePerLiter.amount).toLong()
        val fuelCost = Money.rupiah(fuelCostAmount)

        val totalToll = route.totalTollFee

        val totalRetributionAmount = route.toTransitStops().sumOf { it.terminal.retributionFee.amount }
        val retributionFee = Money.rupiah(totalRetributionAmount)

        val totalOperationalCost = fuelCost + totalToll + retributionFee

        return RouteTripEstimate(
            routeId = route.id,
            totalDistanceKm = totalDistance,
            estimatedDurationMinutes = route.totalEstimatedDurationMinutes,
            tollFee = totalToll,
            terminalRetributionFee = retributionFee,
            estimatedFuelLiters = totalLiters,
            estimatedFuelCost = fuelCost,
            totalOperationalCost = totalOperationalCost
        )
    }
}
