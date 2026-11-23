package com.example.busmanager.domain.route.model

import com.example.busmanager.domain.company.model.Money

data class RouteTripEstimate(
    val routeId: String,
    val totalDistanceKm: Double,
    val estimatedDurationMinutes: Int,
    val tollFee: Money,
    val terminalRetributionFee: Money,
    val estimatedFuelLiters: Double,
    val estimatedFuelCost: Money,
    val totalOperationalCost: Money
)
