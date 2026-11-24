package com.example.busmanager.domain.usecase

import com.example.busmanager.data.repository.ChassisCatalogRepositoryImpl
import com.example.busmanager.data.repository.KaroseriCatalogRepositoryImpl
import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.driver.model.Driver
import com.example.busmanager.domain.driver.repository.DriverRepository
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.fleet.usecase.ValidateBusOperationUseCase
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.RoutePricing
import com.example.busmanager.domain.route.repository.RoutePricingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ValidateBusOperationUseCaseTest {

    private lateinit var fakeDriverRepository: FakeDriverRepository
    private lateinit var fakePricingRepository: FakeRoutePricingRepository
    private lateinit var validateBusOperationUseCase: ValidateBusOperationUseCase

    private val chassisRepo = ChassisCatalogRepositoryImpl()
    private val karoseriRepo = KaroseriCatalogRepositoryImpl()

    @Before
    fun setUp() {
        fakeDriverRepository = FakeDriverRepository()
        fakePricingRepository = FakeRoutePricingRepository()
        validateBusOperationUseCase = ValidateBusOperationUseCase(fakeDriverRepository, fakePricingRepository)
    }

    @Test
    fun busWithoutRouteDriverAndPricingsCannotOperate() = runBlocking {
        val chassis = chassisRepo.getAllChassis().first().first()
        val body = karoseriRepo.getAllBodies().first().first()

        val bus = Bus(
            id = "bus-1",
            busCode = "TN-01",
            chassis = chassis,
            body = body,
            routeId = null,
            driverId = null
        )

        val status = validateBusOperationUseCase(bus)
        assertFalse(status.hasRoute)
        assertFalse(status.hasDriver)
        assertFalse(status.hasPricings)
        assertFalse(status.canOperate)
    }

    @Test
    fun busWithRouteAndDriverButNoPricingsCannotOperate() = runBlocking {
        val chassis = chassisRepo.getAllChassis().first().first()
        val body = karoseriRepo.getAllBodies().first().first()

        fakeDriverRepository.saveDriver(
            Driver(
                id = "driver-1",
                name = "Pak Bambang",
                wagePerDay = Money.rupiah(180_000L),
                assignedBusId = "bus-1",
                isHired = true
            )
        )

        val bus = Bus(
            id = "bus-1",
            busCode = "TN-01",
            chassis = chassis,
            body = body,
            routeId = "route-sby-jbr",
            driverId = "driver-1"
        )

        val status = validateBusOperationUseCase(bus)
        assertTrue(status.hasRoute)
        assertTrue(status.hasDriver)
        assertFalse(status.hasPricings)
        assertFalse(status.canOperate)
    }

    @Test
    fun busWithRouteDriverAndPricingsCanOperate() = runBlocking {
        val chassis = chassisRepo.getAllChassis().first().first()
        val body = karoseriRepo.getAllBodies().first().first()

        fakeDriverRepository.saveDriver(
            Driver(
                id = "driver-1",
                name = "Pak Bambang",
                wagePerDay = Money.rupiah(180_000L),
                assignedBusId = "bus-1",
                isHired = true
            )
        )
        fakePricingRepository.savePricing(
            RoutePricing(
                id = "price-1",
                routeId = "route-sby-jbr",
                originTerminalId = "term-sby",
                destinationTerminalId = "term-jbr",
                originCity = "Surabaya",
                destinationCity = "Jember",
                price = Money.rupiah(100_000L)
            )
        )

        val bus = Bus(
            id = "bus-1",
            busCode = "TN-01",
            chassis = chassis,
            body = body,
            routeId = "route-sby-jbr",
            driverId = "driver-1"
        )

        val status = validateBusOperationUseCase(bus)
        assertTrue(status.hasRoute)
        assertTrue(status.hasDriver)
        assertTrue(status.hasPricings)
        assertTrue(status.canOperate)
    }

    private class FakeDriverRepository : DriverRepository {
        private val drivers = mutableMapOf<String, Driver>()

        fun saveDriver(driver: Driver) {
            drivers[driver.id] = driver
        }

        override fun getAllDrivers(): Flow<List<Driver>> = emptyFlow()
        override fun getHiredDrivers(): Flow<List<Driver>> = emptyFlow()
        override suspend fun getDriverById(id: String): Driver? = drivers[id]
        override suspend fun hireDriver(driverId: String): Boolean = true
        override suspend fun assignDriverToBus(driverId: String, busId: String): Boolean = true
        override suspend fun unassignDriver(driverId: String) {}
        override suspend fun fireDriver(driverId: String) {}
        override suspend fun seedCandidateDriversIfEmpty() {}
    }

    private class FakeRoutePricingRepository : RoutePricingRepository {
        private val pricings = mutableListOf<RoutePricing>()

        override fun getPricingsForRoute(routeId: String): Flow<List<RoutePricing>> = emptyFlow()

        override suspend fun getPricingsListForRoute(routeId: String): List<RoutePricing> {
            return pricings.filter { it.routeId == routeId }
        }

        override fun getAllPricingsFlow(): Flow<List<RoutePricing>> = emptyFlow()

        override suspend fun getAllPricings(): List<RoutePricing> = pricings.toList()

        override suspend fun savePricing(pricing: RoutePricing) {
            pricings.add(pricing)
        }

        override suspend fun savePricings(pricings: List<RoutePricing>) {
            this.pricings.addAll(pricings)
        }

        override suspend fun getPricingBetween(
            routeId: String,
            originTerminalId: String,
            destinationTerminalId: String
        ): RoutePricing? {
            return pricings.find {
                it.routeId == routeId &&
                        it.originTerminalId == originTerminalId &&
                        it.destinationTerminalId == destinationTerminalId
            }
        }

        override suspend fun generateAndSaveDefaultPricings(route: BusRoute): List<RoutePricing> = emptyList()
    }
}
