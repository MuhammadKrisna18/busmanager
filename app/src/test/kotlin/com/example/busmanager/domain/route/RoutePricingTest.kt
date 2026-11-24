package com.example.busmanager.domain.route

import com.example.busmanager.data.mapper.toDomain
import com.example.busmanager.data.mapper.toEntity
import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.route.model.RoutePricing
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RoutePricingTest {

    @Test
    fun pricingCreation_validParameters_succeeds() {
        val pricing = RoutePricing(
            id = "price-1",
            routeId = "route-jbr-sby",
            originTerminalId = "term_tawang_alun",
            destinationTerminalId = "term_purabaya",
            originCity = "Jember",
            destinationCity = "Surabaya",
            price = Money.rupiah(100_000)
        )

        assertEquals("price-1", pricing.id)
        assertEquals("Jember", pricing.originCity)
        assertEquals("Surabaya", pricing.destinationCity)
        assertEquals(Money.rupiah(100_000), pricing.price)
        assertTrue(pricing.isConfigured)
    }

    @Test
    fun pricing_zeroPrice_isConfiguredReturnsFalse() {
        val pricing = RoutePricing(
            id = "price-2",
            routeId = "route-jbr-pro",
            originTerminalId = "term_tawang_alun",
            destinationTerminalId = "term_bayuangga",
            originCity = "Jember",
            destinationCity = "Probolinggo",
            price = Money.ZERO
        )

        assertFalse(pricing.isConfigured)
    }

    @Test(expected = IllegalArgumentException::class)
    fun pricing_sameOriginAndDestination_throwsException() {
        RoutePricing(
            id = "price-3",
            routeId = "route-invalid",
            originTerminalId = "term_purabaya",
            destinationTerminalId = "term_purabaya",
            originCity = "Surabaya",
            destinationCity = "Surabaya",
            price = Money.rupiah(50_000)
        )
    }

    @Test
    fun mapper_domainToEntityAndBack_preservesData() {
        val original = RoutePricing(
            id = "price-1",
            routeId = "route-jbr-sby",
            originTerminalId = "term_tawang_alun",
            destinationTerminalId = "term_purabaya",
            originCity = "Jember",
            destinationCity = "Surabaya",
            price = Money.rupiah(100_000)
        )

        val entity = original.toEntity()
        val mapped = entity.toDomain()

        assertEquals(original, mapped)
    }
}
