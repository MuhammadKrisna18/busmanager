package com.example.busmanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "route_schedules")
data class RouteScheduleEntity(
    @PrimaryKey
    val id: String,
    val routeId: String,
    val departureHour: Int,
    val departureMinute: Int,
    val direction: String,
    val assignedBusId: String? = null,
    val isActive: Boolean = true
)
