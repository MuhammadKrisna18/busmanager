package com.example.busmanager.domain.driver.model

import com.example.busmanager.domain.company.model.Money

data class Driver(
    val id: String,
    val name: String,
    val wagePerDay: Money,
    val assignedBusId: String? = null,
    val isHired: Boolean = false,
    val isPendingRelease: Boolean = false
) {
    init {
        require(name.isNotBlank()) { "Driver name cannot be blank" }
    }

    val isAssigned: Boolean get() = !assignedBusId.isNullOrBlank()

    fun calculateTripCommission(grossEarnings: Money): Money {
        return calculateTripCommission(grossEarnings.amount)
    }

    fun calculateTripCommission(grossEarningsAmount: Long): Money {
        val commission = (grossEarningsAmount * COMMISSION_RATE).toLong().coerceAtLeast(0L)
        return Money.rupiah(commission)
    }

    companion object {
        const val COMMISSION_RATE: Double = 0.30

        fun calculateTripCommission(grossEarnings: Money): Money {
            return calculateTripCommission(grossEarnings.amount)
        }

        fun calculateTripCommission(grossEarningsAmount: Long): Money {
            val commission = (grossEarningsAmount * COMMISSION_RATE).toLong().coerceAtLeast(0L)
            return Money.rupiah(commission)
        }
    }
}
