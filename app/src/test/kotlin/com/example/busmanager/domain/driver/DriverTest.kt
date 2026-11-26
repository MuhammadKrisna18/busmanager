package com.example.busmanager.domain.driver

import com.example.busmanager.data.mapper.toDomain
import com.example.busmanager.data.mapper.toEntity
import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.driver.model.Driver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DriverTest {

    @Test
    fun driverCreation_validParameters_succeeds() {
        val driver = Driver(
            id = "driver-1",
            name = "Pak Bambang",
            wagePerDay = Money.rupiah(200_000),
            assignedBusId = "bus-1",
            isHired = true
        )

        assertEquals("driver-1", driver.id)
        assertEquals("Pak Bambang", driver.name)
        assertEquals(Money.rupiah(200_000), driver.wagePerDay)
        assertTrue(driver.isAssigned)
        assertTrue(driver.isHired)
    }

    @Test
    fun driver_unassigned_returnsFalseForIsAssigned() {
        val driver = Driver(
            id = "driver-2",
            name = "Pak Joko",
            wagePerDay = Money.rupiah(180_000)
        )

        assertFalse(driver.isAssigned)
        assertFalse(driver.isHired)
    }

    @Test(expected = IllegalArgumentException::class)
    fun driver_blankName_throwsException() {
        Driver(
            id = "driver-3",
            name = "  ",
            wagePerDay = Money.rupiah(150_000)
        )
    }

    @Test
    fun mapper_domainToEntityAndBack_preservesData() {
        val original = Driver(
            id = "driver-1",
            name = "Pak Bambang",
            wagePerDay = Money.rupiah(200_000),
            assignedBusId = "bus-1",
            isHired = true
        )

        val entity = original.toEntity()
        val mapped = entity.toDomain()

        assertEquals(original, mapped)
    }

    @Test
    fun calculateTripCommission_companionWithMoney_calculatesThirtyPercent() {
        val gross = Money.rupiah(1_000_000)
        val commission = Driver.calculateTripCommission(gross)
        assertEquals(Money.rupiah(300_000), commission)
    }

    @Test
    fun calculateTripCommission_companionWithLong_calculatesThirtyPercent() {
        val commission = Driver.calculateTripCommission(500_000L)
        assertEquals(Money.rupiah(150_000), commission)
    }

    @Test
    fun calculateTripCommission_instanceMethod_calculatesThirtyPercent() {
        val driver = Driver(
            id = "driver-1",
            name = "Pak Bambang",
            wagePerDay = Money.rupiah(200_000)
        )
        val gross = Money.rupiah(2_500_000)
        val commission = driver.calculateTripCommission(gross)
        assertEquals(Money.rupiah(750_000), commission)
    }

    @Test
    fun calculateTripCommission_zeroOrNegative_returnsZero() {
        assertEquals(Money.ZERO, Driver.calculateTripCommission(Money.ZERO))
        assertEquals(Money.ZERO, Driver.calculateTripCommission(0L))
        assertEquals(Money.ZERO, Driver.calculateTripCommission(-100_000L))
    }
}
