package com.example.busmanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "terminal_passenger_records")
data class TerminalPassengerRecordEntity(
    @PrimaryKey
    val id: String,
    val terminalId: String,
    val terminalName: String,
    val busId: String,
    val busCode: String,
    val routeId: String,
    val routeName: String,
    val day: Int,
    val month: Int,
    val year: Int,
    val dateFormatted: String,
    val timeFormatted: String,
    val boardedCount: Int,
    val alightedCount: Int,
    val timestamp: Long
)
