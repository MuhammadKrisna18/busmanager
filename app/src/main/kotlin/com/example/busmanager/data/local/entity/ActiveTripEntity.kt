package com.example.busmanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "active_trips")
data class ActiveTripEntity(
    @PrimaryKey
    val id: String,
    val busId: String,
    val busCode: String,
    val routeId: String,
    val direction: String,
    val departureYear: Int,
    val departureMonth: Int,
    val departureDay: Int,
    val departureHour: Int,
    val departureMinute: Int,
    val estimatedArrivalMinutes: Int,
    val elapsedMinutes: Int,
    val progressFraction: Float,
    val coordX: Float,
    val coordY: Float,
    val coordLat: Double,
    val coordLon: Double,
    val passengerCount: Int,
    val ticketEarningsAmount: Long,
    val isCompleted: Boolean,
    val maxCapacity: Int,
    val visitedTransitIndicesCsv: String,
    val totalBoardedPassengers: Int,
    val totalAlightedPassengers: Int,
    val cancelNextSchedule: Boolean,
    val isDwellingAtTransit: Boolean,
    val dwellingTerminalName: String?,
    val headwayMinutes: Int,
    val transitDwellMinutes: Int,
    val ticketPricePerPassengerAmount: Long?
)
