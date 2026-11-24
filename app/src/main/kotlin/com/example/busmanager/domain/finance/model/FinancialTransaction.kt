package com.example.busmanager.domain.finance.model

import com.example.busmanager.domain.company.model.Money

data class FinancialTransaction(
    val id: String,
    val dateText: String,
    val timeText: String,
    val day: Int,
    val month: Int,
    val year: Int,
    val type: TransactionType,
    val category: TransactionCategory,
    val amount: Money,
    val busId: String? = null,
    val busCode: String? = null,
    val routeId: String? = null,
    val routeName: String? = null,
    val description: String,
    val createdAtMillis: Long = System.currentTimeMillis()
)
