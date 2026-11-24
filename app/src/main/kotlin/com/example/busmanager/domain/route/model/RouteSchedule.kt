package com.example.busmanager.domain.route.model

import com.example.busmanager.domain.simulation.model.RouteDirection

data class RouteSchedule(
    val id: String,
    val routeId: String,
    val departureHour: Int,
    val departureMinute: Int,
    val direction: RouteDirection,
    val assignedBusId: String? = null,
    val isActive: Boolean = true
) {
    init {
        require(departureHour in 0..23) { "Departure hour must be between 0 and 23" }
        require(departureMinute in 0..59) { "Departure minute must be between 0 and 59" }
        require(routeId.isNotBlank()) { "Route ID cannot be blank" }
    }

    val formattedTime: String
        get() = String.format("%02d:%02d WIB", departureHour, departureMinute)

    val hasAssignedBus: Boolean get() = !assignedBusId.isNullOrBlank()

    fun hasConflictWith(other: RouteSchedule): Boolean {
        return id != other.id &&
                routeId == other.routeId &&
                direction == other.direction &&
                departureHour == other.departureHour &&
                departureMinute == other.departureMinute
    }
}
