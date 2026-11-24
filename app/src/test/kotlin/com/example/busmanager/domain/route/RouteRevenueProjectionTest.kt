package com.example.busmanager.domain.route

import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.route.model.RouteRevenueProjection
import org.junit.Assert.assertEquals
import org.junit.Test

class RouteRevenueProjectionTest {

    @Test
    fun oneWayRevenueAndWagesAreCalculatedCorrectlyWithoutToll() {
        val projection = RouteRevenueProjection(
            seatCapacity = 30,
            ticketPrice = Money.rupiah(100_000),
            tollFee = Money.ZERO,
            driverWageRate = 0.30f
        )

        assertEquals(Money.rupiah(3_000_000), projection.grossRevenueOneWay)
        assertEquals(Money.rupiah(900_000), projection.driverWageOneWay)
        assertEquals(Money.rupiah(2_100_000), projection.netProfitOneWay)
    }

    @Test
    fun oneWayRevenueAndWagesDeductTollFee() {
        val projection = RouteRevenueProjection(
            seatCapacity = 30,
            ticketPrice = Money.rupiah(100_000),
            tollFee = Money.rupiah(150_000),
            driverWageRate = 0.30f
        )

        assertEquals(Money.rupiah(3_000_000), projection.grossRevenueOneWay)
        assertEquals(Money.rupiah(900_000), projection.driverWageOneWay)
        assertEquals(Money.rupiah(1_950_000), projection.netProfitOneWay)
    }

    @Test
    fun roundTripDoublesGrossWagesTollAndNetProfit() {
        val projection = RouteRevenueProjection(
            seatCapacity = 30,
            ticketPrice = Money.rupiah(100_000),
            tollFee = Money.rupiah(150_000),
            driverWageRate = 0.30f
        )

        assertEquals(Money.rupiah(6_000_000), projection.grossRevenueRoundTrip)
        assertEquals(Money.rupiah(1_800_000), projection.driverWageRoundTrip)
        assertEquals(Money.rupiah(300_000), projection.tollFeeRoundTrip)
        assertEquals(Money.rupiah(3_900_000), projection.netProfitRoundTrip)
    }
}
