package com.example.busmanager.data.mapper

import com.example.busmanager.data.local.entity.OwnedBusEntity
import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.fleet.model.AxleType
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.fleet.model.BusBodyType
import com.example.busmanager.domain.fleet.model.BusClass
import com.example.busmanager.domain.fleet.model.BusCondition
import com.example.busmanager.domain.fleet.model.Chassis
import com.example.busmanager.domain.fleet.model.ChassisBrand
import com.example.busmanager.domain.fleet.model.EngineLocation
import com.example.busmanager.domain.fleet.model.KaroseriBody
import com.example.busmanager.domain.fleet.model.KaroseriBrand
import com.example.busmanager.domain.fleet.model.SuspensionType
import com.example.busmanager.domain.fleet.model.TransmissionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BusMapperTest {

    private fun createSampleChassis(): Chassis = Chassis(
        id = "chassis-scania-k360",
        modelName = "K360IB Opticruise",
        brand = ChassisBrand.SCANIA,
        engineLocation = EngineLocation.REAR,
        suspension = SuspensionType.AIR_SUSPENSION,
        axleType = AxleType.SINGLE_4X2,
        transmission = TransmissionType.OPTICRUISE,
        horsepower = 360,
        torqueNm = 1750,
        fuelCapacityLiters = 400,
        fuelConsumptionKmPerLiter = 2.8f,
        price = Money.rupiah(1_550_000_000)
    )

    private fun createPresetBody(): KaroseriBody = KaroseriBody(
        id = "body-tt-avante-h8",
        modelName = "Tentrem Avante H8",
        brand = KaroseriBrand.TENTREM,
        bodyType = BusBodyType.SHD,
        busClass = BusClass.EKSEKUTIF,
        passengerCapacity = 32,
        luggageCapacityKg = 2600,
        comfortRating = 88,
        price = Money.rupiah(630_000_000),
        requiresTronton = false
    )

    @Test
    fun toEntity_preservesCustomBusClassAndCapacity() {
        val chassis = createSampleChassis()
        val customBody = createPresetBody().copy(
            busClass = BusClass.VIP,
            passengerCapacity = 34
        )
        val bus = Bus(
            id = "bus-1",
            busCode = "MK03",
            chassis = chassis,
            body = customBody,
            currentTerminalId = "term-malang"
        )

        val entity = bus.toEntity()

        assertEquals("bus-1", entity.id)
        assertEquals("MK03", entity.busCode)
        assertEquals("chassis-scania-k360", entity.chassisId)
        assertEquals("body-tt-avante-h8", entity.bodyId)
        assertEquals("VIP", entity.busClass)
        assertEquals(34, entity.passengerCapacity)
        assertEquals("term-malang", entity.currentTerminalId)
    }

    @Test
    fun toDomain_restoresCustomBusClassAndCapacity() {
        val chassis = createSampleChassis()
        val presetBody = createPresetBody()
        val entity = OwnedBusEntity(
            id = "bus-1",
            busCode = "MK03",
            chassisId = chassis.id,
            bodyId = presetBody.id,
            busClass = "VIP",
            passengerCapacity = 34,
            currentTerminalId = "term-malang"
        )

        val domainBus = entity.toDomain(chassis, presetBody)

        assertEquals("bus-1", domainBus.id)
        assertEquals("MK03", domainBus.busCode)
        assertEquals(BusClass.VIP, domainBus.body.busClass)
        assertEquals(34, domainBus.body.passengerCapacity)
        assertEquals("term-malang", domainBus.currentTerminalId)
    }

    @Test
    fun toDomain_fallsBackToPresetWhenEntityHasNullCustomValues() {
        val chassis = createSampleChassis()
        val presetBody = createPresetBody()
        val entity = OwnedBusEntity(
            id = "bus-legacy",
            busCode = "TN-01",
            chassisId = chassis.id,
            bodyId = presetBody.id,
            busClass = null,
            passengerCapacity = null
        )

        val domainBus = entity.toDomain(chassis, presetBody)

        assertEquals(BusClass.EKSEKUTIF, domainBus.body.busClass)
        assertEquals(32, domainBus.body.passengerCapacity)
        assertNull(domainBus.currentTerminalId)
    }
}
