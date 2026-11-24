package com.example.busmanager.presentation.simulation

import com.example.busmanager.domain.simulation.model.GameCalendar

data class SimulationMapUiState(
    val currentTime: GameCalendar = GameCalendar.initial(),
    val isPlaying: Boolean = false,
    val companyName: String = "PO TransNusantara",
    val balanceFormatted: String = "Rp 0",
    val ownedBusCount: Int = 0,
    val activeBusCount: Int = 0,
    val isLoading: Boolean = false
) {
    val formattedDateTime: String
        get() = currentTime.formatDateTime()

    val formattedDate: String
        get() = "${currentTime.dayOfWeekName()}, ${currentTime.formatDate()}"

    val formattedTime: String
        get() = currentTime.formatTime()

    val hasActiveFleet: Boolean
        get() = activeBusCount > 0
}
