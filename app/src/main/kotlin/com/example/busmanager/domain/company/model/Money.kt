package com.example.busmanager.domain.company.model

import java.text.NumberFormat
import java.util.Locale

data class Money(val amount: Long) : Comparable<Money> {
    init {
        require(amount >= 0) { "Money amount cannot be negative" }
    }

    operator fun plus(other: Money): Money = Money(amount + other.amount)

    operator fun minus(other: Money): Money {
        require(canAfford(other)) { "Insufficient funds: current $amount, required ${other.amount}" }
        return Money(amount - other.amount)
    }

    fun canAfford(cost: Money): Boolean = amount >= cost.amount

    override fun compareTo(other: Money): Int = amount.compareTo(other.amount)

    fun formatRupiah(): String {
        val localeID = Locale.forLanguageTag("id-ID")
        val formatter = NumberFormat.getCurrencyInstance(localeID)
        return formatter.format(amount).replace(",00", "")
    }

    fun formatCompact(): String {
        return when {
            amount >= 1_000_000_000 -> String.format(Locale.US, "%.1f M", amount / 1_000_000_000.0)
            amount >= 1_000_000 -> String.format(Locale.US, "%.1f Jt", amount / 1_000_000.0)
            amount >= 1_000 -> String.format(Locale.US, "%.1f Rb", amount / 1_000.0)
            else -> amount.toString()
        }
    }

    companion object {
        val ZERO = Money(0)
        fun rupiah(amount: Long): Money = Money(amount)
    }
}
