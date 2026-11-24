package com.example.busmanager.domain.simulation.model

import com.example.busmanager.domain.company.model.Money

data class OfflineBusSummary(
    val busCode: String,
    val routeName: String,
    val tripsCompleted: Int,
    val passengersCarried: Int,
    val grossRevenue: Money,
    val driverWage: Money,
    val netEarnings: Money,
    val currentTerminalName: String
)

data class OfflineProgressionResult(
    val offlineDurationMinutes: Int,
    val offlineGameHours: Int,
    val totalTripsCompleted: Int,
    val totalPassengersCarried: Int,
    val totalRevenue: Money,
    val totalDriverWages: Money,
    val netEarnings: Money,
    val activeBusesCount: Int,
    val busSummaries: List<OfflineBusSummary> = emptyList()
)
