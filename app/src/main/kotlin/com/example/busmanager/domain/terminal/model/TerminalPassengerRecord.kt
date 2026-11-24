package com.example.busmanager.domain.terminal.model

data class TerminalPassengerRecord(
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
    val timestamp: Long = System.currentTimeMillis()
)

data class TerminalBusBreakdown(
    val busId: String,
    val busCode: String,
    val routeName: String,
    val totalBoarded: Int,
    val totalAlighted: Int,
    val tripCount: Int
)
