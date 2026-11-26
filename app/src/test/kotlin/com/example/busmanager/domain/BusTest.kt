package com.example.busmanager.domain

import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.fleet.model.AxleType
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.fleet.model.BusBodyType
import com.example.busmanager.domain.fleet.model.BusClass
import com.example.busmanager.domain.fleet.model.Chassis
import com.example.busmanager.domain.fleet.model.ChassisBrand
import com.example.busmanager.domain.fleet.model.EngineLocation
import com.example.busmanager.domain.fleet.model.KaroseriBody
import com.example.busmanager.domain.fleet.model.KaroseriBrand
import com.example.busmanager.domain.fleet.model.SuspensionType
import com.example.busmanager.domain.fleet.model.TransmissionType
import org.junit.Assert.assertEquals
import org.junit.Test

class BusTest {

    private fun createHinoRk8(): Chassis = Chassis(
        id = "chassis-hino-rk8",
        modelName = "RK8 R260",
        brand = ChassisBrand.HINO,
        engineLocation = EngineLocation.REAR,
        suspension = SuspensionType.LEAF_SPRING,
        axleType = AxleType.SINGLE_4X2,
        transmission = TransmissionType.MANUAL,
        horsepower = 260,
        torqueNm = 750,
        price = Money.rupiah(750_000_000)
    )

    private fun createScaniaK410(): Chassis = Chassis(
        id = "chassis-scania-k410",
        modelName = "K410IB",
        brand = ChassisBrand.SCANIA,
        engineLocation = EngineLocation.REAR,
        suspension = SuspensionType.AIR_SUSPENSION,
        axleType = AxleType.TRONTON_6X2,
        transmission = TransmissionType.OPTICRUISE,
        horsepower = 410,
        torqueNm = 2000,
        price = Money.rupiah(1_850_000_000)
    )

    private fun createJetbusShd(): KaroseriBody = KaroseriBody(
        id = "body-jb5-shd",
        modelName = "Jetbus 5 SHD",
        brand = KaroseriBrand.ADIPUTRO,
        bodyType = BusBodyType.SHD,
        busClass = BusClass.EKSEKUTIF,
        passengerCapacity = 32,
        luggageCapacityKg = 2500,
        comfortRating = 88,
        price = Money.rupiah(650_000_000)
    )

    private fun createJetbusDoubleDecker(): KaroseriBody = KaroseriBody(
        id = "body-jb5-sdd",
        modelName = "Jetbus 5 SDD",
        brand = KaroseriBrand.ADIPUTRO,
        bodyType = BusBodyType.SDD,
        busClass = BusClass.SUITES_CLASS,
        passengerCapacity = 44,
        luggageCapacityKg = 3500,
        comfortRating = 95,
        price = Money.rupiah(1_100_000_000)
    )

    @Test
    fun totalPriceCombinesChassisAndBody() {
        val bus = Bus(
            id = "bus-1",
            busCode = "TN-01",
            chassis = createHinoRk8(),
            body = createJetbusShd()
        )
        val expected = Money.rupiah(1_400_000_000)
        assertEquals(expected, bus.totalPrice)
    }

    @Test(expected = IllegalArgumentException::class)
    fun doubleDeckerThrowsWhenPairedWithSingleAxle() {
        Bus(
            id = "bus-invalid",
            busCode = "TN-02",
            chassis = createHinoRk8(),
            body = createJetbusDoubleDecker()
        )
    }

    @Test
    fun doubleDeckerSucceedsWhenPairedWithTronton() {
        val bus = Bus(
            id = "bus-valid",
            busCode = "TN-03",
            chassis = createScaniaK410(),
            body = createJetbusDoubleDecker()
        )
        assertEquals(Money.rupiah(2_950_000_000), bus.totalPrice)
    }
}
