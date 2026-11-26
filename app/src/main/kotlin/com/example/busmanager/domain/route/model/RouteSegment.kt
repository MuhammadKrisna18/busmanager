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
    val waypoints: List<MapCoordinate> = emptyList()
) {
    init {
        require(distanceKm > 0.0) { "Distance must be greater than zero" }
        require(baseDurationMinutes > 0) { "Base duration must be greater than zero" }
    }
}
