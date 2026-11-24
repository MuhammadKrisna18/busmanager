package com.example.busmanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_routes")
data class CustomRouteEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val originTerminalId: String,
    val destinationTerminalId: String,
    val pathType: String,
    val segmentIds: String,
    val transitTerminalIds: String = "",
    val createdAtMillis: Long = System.currentTimeMillis()
)
