package com.example.busmanager.domain.route.model

import com.example.busmanager.domain.company.model.Money

data class RouteSegment(
    val id: String,
    val fromTerminalId: String,
    val toTerminalId: String,
    val pathType: RoutePathType,
    val characteristic: RoadCharacteristic,
    val distanceKm: Double,
    val baseDurationMinutes: Int,
    val tollFee: Money = Money.ZERO,
    val waypoints: List<MapCoordinate> = emptyList(),
    val wearTearRate: Float = characteristic.baseWearRate * if (pathType == RoutePathType.TOLL) 0.85f else 1.15f,
    val fuelMultiplier: Float = characteristic.fuelMultiplier * if (pathType == RoutePathType.TOLL) 0.90f else 1.20f
) {
    init {
        require(distanceKm > 0.0) { "Distance must be greater than zero" }
        require(baseDurationMinutes > 0) { "Base duration must be greater than zero" }
    }
}
