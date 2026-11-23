package com.example.busmanager.domain.route.model

import com.example.busmanager.domain.company.model.Money

data class Terminal(
    val id: String,
    val name: String,
    val city: String,
    val type: TerminalType,
    val coordinate: MapCoordinate,
    val retributionFee: Money,
    val passengerDemandMultiplier: Float = 1.0f
)
