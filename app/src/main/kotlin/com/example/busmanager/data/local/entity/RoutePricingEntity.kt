package com.example.busmanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "route_pricings")
data class RoutePricingEntity(
    @PrimaryKey
    val id: String,
    val routeId: String,
    val originTerminalId: String,
    val destinationTerminalId: String,
    val originCity: String,
    val destinationCity: String,
    val priceAmount: Long
)
