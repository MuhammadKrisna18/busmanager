package com.example.busmanager.domain.simulation.model

import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.MapCoordinate

data class ActiveBusTrip(
    val id: String,
    val busId: String,
    val busCode: String,
    val route: BusRoute,
    val direction: RouteDirection = RouteDirection.ORIGIN_TO_DESTINATION,
    val departureTime: GameCalendar,
    val estimatedArrivalMinutes: Int,
    val elapsedMinutes: Int = 0,
    val progressFraction: Float = 0.0f,
    val currentCoordinate: MapCoordinate,
    val passengerCount: Int,
    val ticketEarnings: Money,
    val isCompleted: Boolean = false,
    val maxCapacity: Int = 40,
    val visitedTransitStopIndices: Set<Int> = emptySet(),
    val totalBoardedPassengers: Int = passengerCount,
    val totalAlightedPassengers: Int = 0,
    val cancelNextSchedule: Boolean = false
) {
    val remainingMinutes: Int
        get() = (estimatedArrivalMinutes - elapsedMinutes).coerceAtLeast(0)

    val currentDestinationName: String
        get() = when (direction) {
            RouteDirection.ORIGIN_TO_DESTINATION -> route.destinationTerminal.city
            RouteDirection.DESTINATION_TO_ORIGIN -> route.originTerminal.city
        }

    val currentOriginName: String
        get() = when (direction) {
            RouteDirection.ORIGIN_TO_DESTINATION -> route.originTerminal.city
            RouteDirection.DESTINATION_TO_ORIGIN -> route.destinationTerminal.city
        }
}
