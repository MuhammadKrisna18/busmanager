package com.example.busmanager.domain.simulation.model

data class TerminalPassengerQueue(
    val terminalId: String,
    val routeId: String,
    val waitingPassengers: Int = 0,
    val minutesSinceLastService: Int = 0,
    val totalBoardedPassengers: Int = 0,
    val totalLostPassengers: Int = 0
)
