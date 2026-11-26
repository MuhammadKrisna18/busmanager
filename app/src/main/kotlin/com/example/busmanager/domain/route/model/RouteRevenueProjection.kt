package com.example.busmanager.domain.route.model

import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.driver.model.Driver

data class RouteRevenueProjection(
    val seatCapacity: Int,
    val ticketPrice: Money,
    val tollFee: Money = Money.ZERO,
    val driverWageRate: Float = Driver.COMMISSION_RATE.toFloat()
) {
    val grossRevenueOneWay: Money
        get() = ticketPrice * seatCapacity

    val driverWageOneWay: Money
        get() = Money.rupiah((grossRevenueOneWay.amount * driverWageRate).toLong())

    val netProfitOneWay: Money
        get() = Money.rupiah((grossRevenueOneWay.amount - driverWageOneWay.amount - tollFee.amount).coerceAtLeast(0L))

    val grossRevenueRoundTrip: Money
        get() = grossRevenueOneWay * 2

    val driverWageRoundTrip: Money
        get() = driverWageOneWay * 2

    val tollFeeRoundTrip: Money
        get() = tollFee * 2

    val netProfitRoundTrip: Money
        get() = netProfitOneWay * 2
}
