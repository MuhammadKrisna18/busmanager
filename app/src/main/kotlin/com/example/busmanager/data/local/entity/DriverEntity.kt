package com.example.busmanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drivers")
data class DriverEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val wagePerDayAmount: Long,
    val assignedBusId: String? = null,
    val isHired: Boolean = false
)
