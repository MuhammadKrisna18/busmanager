package com.example.busmanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "simulation_logs")
data class SimulationLogEntity(
    @PrimaryKey
    val id: String,
    val timestampMillis: Long,
    val dateFormatted: String,
    val timeFormatted: String,
    val category: String,
    val title: String,
    val message: String,
    val busId: String? = null,
    val busCode: String? = null,
    val routeId: String? = null,
    val routeName: String? = null
)
