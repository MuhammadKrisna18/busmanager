package com.example.busmanager.domain.route.model

data class TerminalDepartureSlot(
    val slotId: String,
    val terminalId: String,
    val hour: Int,
    val minute: Int,
    val isAvailable: Boolean = true,
    val occupiedByRouteId: String? = null,
    val occupiedDestinationCity: String? = null,
    val occupiedBusCode: String? = null
) {
    init {
        require(hour in 0..23) { "Hour must be between 0 and 23" }
        require(minute in 0..59) { "Minute must be between 0 and 59" }
        require(minute % 15 == 0) { "Minute must be a multiple of 15" }
    }

    val timeFormatted: String
        get() = String.format("%02d:%02d", hour, minute)

    val formattedTime: String
        get() = timeFormatted

    val formattedClockPeriod: String
        get() = when (hour) {
            in 4..10 -> String.format("%02d.%02d Pagi", hour, minute)
            in 11..14 -> String.format("%02d.%02d Siang", hour, minute)
            in 15..18 -> String.format("%02d.%02d Sore", hour, minute)
            else -> String.format("%02d.%02d Malam", hour, minute)
        }

    val totalMinutesOfDay: Int
        get() = hour * 60 + minute
}
