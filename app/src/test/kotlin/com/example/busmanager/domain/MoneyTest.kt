package com.example.busmanager.domain

import com.example.busmanager.domain.company.model.Money
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MoneyTest {

    @Test
    fun plusAddsAmountsCorrectly() {
        val first = Money.rupiah(500_000_000)
        val second = Money.rupiah(250_000_000)
        val result = first + second
        assertEquals(750_000_000L, result.amount)
    }

    @Test
    fun minusDeductsAmountWhenAffordable() {
        val balance = Money.rupiah(1_500_000_000)
        val price = Money.rupiah(700_000_000)
        val remaining = balance - price
        assertEquals(800_000_000L, remaining.amount)
    }

    @Test(expected = IllegalArgumentException::class)
    fun minusThrowsExceptionWhenInsufficientFunds() {
        val balance = Money.rupiah(500_000_000)
        val price = Money.rupiah(700_000_000)
        balance - price
    }

    @Test
    fun canAffordReturnsExpectedBoolean() {
        val balance = Money.rupiah(1_000_000_000)
        assertTrue(balance.canAfford(Money.rupiah(1_000_000_000)))
        assertTrue(balance.canAfford(Money.rupiah(800_000_000)))
        assertFalse(balance.canAfford(Money.rupiah(1_200_000_000)))
    }

    @Test
    fun formatCompactReturnsCorrectDisplayUnit() {
        assertEquals("1.5 M", Money.rupiah(1_500_000_000).formatCompact())
        assertEquals("750.0 Jt", Money.rupiah(750_000_000).formatCompact())
        assertEquals("500.0 Rb", Money.rupiah(500_000).formatCompact())
    }
}
