package com.example.busmanager.domain.route.model

import com.example.busmanager.domain.simulation.model.RouteDirection

data class ScheduledTripLeg(
    val legIndex: Int,
    val ritNumber: Int,
    val direction: RouteDirection,
    val departureHour: Int,
    val departureMinute: Int,
    val arrivalHour: Int,
    val arrivalMinute: Int,
    val departureTerminalId: String,
    val arrivalTerminalId: String,
    val layoverMinutesAfterArrival: Int
) {
    val departureFormatted: String
        get() = String.format("%02d:%02d", departureHour, departureMinute)

    val arrivalFormatted: String
        get() = String.format("%02d:%02d", arrivalHour, arrivalMinute)

    val departureTotalMinutes: Int
        get() = departureHour * 60 + departureMinute

    val arrivalTotalMinutes: Int
        get() = arrivalHour * 60 + arrivalMinute
}
