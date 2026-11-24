package com.example.busmanager.domain.route.model

import com.example.busmanager.domain.simulation.model.RouteDirection

data class RouteSchedule(
    val id: String,
    val routeId: String,
    val departureHour: Int = 8,
    val departureMinute: Int = 0,
    val direction: RouteDirection = RouteDirection.ORIGIN_TO_DESTINATION,
    val assignedBusId: String? = null,
    val isActive: Boolean = true,
    val headwayMinutes: Int = 60,
    val transitDwellMinutes: Int = 10,
    val nextDepartureEpochMinutes: Long = 0L
) {
    init {
        require(departureHour in 0..23) { "Departure hour must be between 0 and 23" }
        require(departureMinute in 0..59) { "Departure minute must be between 0 and 59" }
        require(routeId.isNotBlank()) { "Route ID cannot be blank" }
        require(headwayMinutes >= 0) { "Headway minutes must be positive or zero" }
        require(transitDwellMinutes in 0..15) { "Transit dwell minutes must be between 0 and 15" }
    }

    val formattedTime: String
        get() = String.format("%02d:%02d WIB", departureHour, departureMinute)

    val hasAssignedBus: Boolean get() = !assignedBusId.isNullOrBlank()

    fun formatScheduleInfo(originCity: String, destinationCity: String): String {
        val targetDest = if (direction == RouteDirection.ORIGIN_TO_DESTINATION) destinationCity else originCity
        val targetOrigin = if (direction == RouteDirection.ORIGIN_TO_DESTINATION) originCity else destinationCity
        return "Jam $formattedTime menuju $targetDest dari $targetOrigin"
    }

    fun formatScheduleInfo(route: BusRoute): String {
        return formatScheduleInfo(route.originTerminal.city, route.destinationTerminal.city)
    }

    fun hasConflictWith(other: RouteSchedule): Boolean {
        return id != other.id &&
                routeId == other.routeId &&
                direction == other.direction &&
                departureHour == other.departureHour &&
                departureMinute == other.departureMinute
    }
}
