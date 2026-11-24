package com.example.busmanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "financial_transactions")
data class FinancialTransactionEntity(
    @PrimaryKey
    val id: String,
    val dateText: String,
    val timeText: String,
    val day: Int,
    val month: Int,
    val year: Int,
    val type: String,
    val category: String,
    val amount: Long,
    val busId: String? = null,
    val busCode: String? = null,
    val routeId: String? = null,
    val routeName: String? = null,
    val description: String,
    val createdAtMillis: Long
)
