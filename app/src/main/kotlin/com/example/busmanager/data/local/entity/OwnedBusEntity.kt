package com.example.busmanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "owned_buses")
data class OwnedBusEntity(
    @PrimaryKey
    val id: String,
    val busCode: String,
    val chassisId: String,
    val bodyId: String,
    val busClass: String? = null,
    val passengerCapacity: Int? = null,
    val currentTerminalId: String? = null,
    val engineHealthPercent: Int = 100,
    val bodyHealthPercent: Int = 100,
    val odometerKm: Long = 0L,
    val isOperating: Boolean = false,
    val routeId: String? = null,
    val driverId: String? = null,
    val purchasedAtTimestamp: Long = System.currentTimeMillis()
)
