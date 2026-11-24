package com.example.busmanager.domain.route.model

import com.example.busmanager.domain.company.model.Money

data class RoutePricing(
    val id: String,
    val routeId: String,
    val originTerminalId: String,
    val destinationTerminalId: String,
    val originCity: String,
    val destinationCity: String,
    val price: Money
) {
    init {
        require(routeId.isNotBlank()) { "Route ID cannot be blank" }
        require(originTerminalId != destinationTerminalId) { "Origin and destination terminals must be different" }
    }

    val isConfigured: Boolean get() = price.amount > 0L
}
