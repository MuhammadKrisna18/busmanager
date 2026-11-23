package com.example.busmanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "owned_buses")
data class OwnedBusEntity(
    @PrimaryKey
    val id: String,
    val plateNumber: String,
    val nickname: String,
    val chassisId: String,
    val bodyId: String,
    val engineHealthPercent: Int = 100,
    val bodyHealthPercent: Int = 100,
    val odometerKm: Long = 0L,
    val isOperating: Boolean = false,
    val purchasedAtTimestamp: Long = System.currentTimeMillis()
)
