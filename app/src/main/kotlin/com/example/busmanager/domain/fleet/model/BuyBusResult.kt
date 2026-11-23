package com.example.busmanager.domain.fleet.model

import com.example.busmanager.domain.company.model.Money

sealed interface BuyBusResult {
    data class Success(val bus: Bus) : BuyBusResult
    data class InsufficientFunds(val required: Money, val available: Money) : BuyBusResult
    data object CompanyNotFound : BuyBusResult
}
