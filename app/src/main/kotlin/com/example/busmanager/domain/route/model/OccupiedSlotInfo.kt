package com.example.busmanager.domain.route.model

data class OccupiedSlotInfo(
    val terminalId: String,
    val hour: Int,
    val minute: Int,
    val destinationCity: String,
    val routeId: String,
    val busCode: String? = null
)
