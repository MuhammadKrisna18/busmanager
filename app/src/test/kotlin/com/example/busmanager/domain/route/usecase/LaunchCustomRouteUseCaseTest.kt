package com.example.busmanager.domain.route.usecase

import com.example.busmanager.data.local.datasource.ChassisDataSource
import com.example.busmanager.data.local.datasource.KaroseriBodyDataSource
import com.example.busmanager.data.local.datasource.RouteDataSource
import com.example.busmanager.data.local.datasource.TerminalDataSource
import com.example.busmanager.data.repository.RouteCatalogRepositoryImpl
import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.driver.model.Driver
import com.example.busmanager.domain.driver.repository.DriverRepository
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.fleet.repository.FleetRepository
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.RoutePathType
import com.example.busmanager.domain.route.model.RoutePricing
import com.example.busmanager.domain.route.model.RouteSchedule
import com.example.busmanager.domain.route.repository.RoutePricingRepository
import com.example.busmanager.domain.route.repository.RouteRepository
import com.example.busmanager.domain.route.repository.RouteScheduleRepository
import com.example.busmanager.domain.simulation.model.RouteDirection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class LaunchCustomRouteUseCaseTest {

    private lateinit var fakeRouteRepo: FakeRouteRepository
    private lateinit var fakePricingRepo: FakeRoutePricingRepository
    private lateinit var fakeDriverRepo: FakeDriverRepository
    private lateinit var fakeFleetRepo: FakeFleetRepository
    private lateinit var fakeScheduleRepo: FakeRouteScheduleRepository
    private lateinit var useCase: LaunchCustomRouteUseCase

    private lateinit var testRoute: BusRoute
    private lateinit var testBus: Bus
    private lateinit var testDriver: Driver

    @Before
    fun setUp() {
        fakeRouteRepo = FakeRouteRepository()
        fakePricingRepo = FakeRoutePricingRepository()
        fakeDriverRepo = FakeDriverRepository()
        fakeFleetRepo = FakeFleetRepository()
        fakeScheduleRepo = FakeRouteScheduleRepository()

        useCase = LaunchCustomRouteUseCase(
            routeRepository = fakeRouteRepo,
            routePricingRepository = fakePricingRepo,
            driverRepository = fakeDriverRepo,
            fleetRepository = fakeFleetRepo,
            routeScheduleRepository = fakeScheduleRepo
        )

        val terminalDataSource = TerminalDataSource()
        val routeDataSource = RouteDataSource(terminalDataSource)
        val routeCatalog = RouteCatalogRepositoryImpl(routeDataSource)
        testRoute = routeCatalog.getAllPredefinedRoutes().first()

        val chassis = ChassisDataSource().getChassisList().first()
        val body = KaroseriBodyDataSource().getKaroseriBodyList().first()
        testBus = Bus("bus_01", "TN-01", chassis, body)
        testDriver = Driver("drv_01", "Bambang", Money.rupiah(200_000L), isHired = true)

        fakeFleetRepo.addBusDirect(testBus)
        fakeDriverRepo.addDriverDirect(testDriver)
    }

    @Test
    fun launchRoute_oneWay_persistsAllDataAndSchedulesCorrectly() = runTest {
        val pricings = listOf(
            RoutePricing("p1", testRoute.id, testRoute.originTerminal.id, testRoute.destinationTerminal.id, "Surabaya", "Malang", Money.rupiah(45000))
        )

        val params = LaunchCustomRouteParams(
            route = testRoute,
            pricings = pricings,
            bus = testBus,
            driver = testDriver,
            departureHour = 8,
            departureMinute = 30,
            direction = RouteDirection.ORIGIN_TO_DESTINATION,
            headwayMinutes = 60,
            transitDwellMinutes = 10,
            isRoundTrip = false
        )

        useCase(params)

        assertNotNull(fakeRouteRepo.savedRoute)
        assertEquals(testRoute.id, fakeRouteRepo.savedRoute?.id)
        assertEquals(1, fakePricingRepo.savedPricings.size)

        assertEquals("bus_01", fakeDriverRepo.assignedDriverBusMap["drv_01"])
        val updatedBus = fakeFleetRepo.getBusById("bus_01")
        assertNotNull(updatedBus)
        assertEquals(testRoute.id, updatedBus?.routeId)
        assertEquals(testDriver.id, updatedBus?.driverId)

        assertEquals(1, fakeScheduleRepo.addedSchedules.size)
        val outbound = fakeScheduleRepo.addedSchedules.first()
        assertEquals(testRoute.id, outbound.routeId)
        assertEquals(8, outbound.departureHour)
        assertEquals(30, outbound.departureMinute)
        assertEquals(testBus.id, outbound.assignedBusId)
    }

    @Test
    fun launchRoute_roundTrip_addsBothOutboundAndReturnSchedules() = runTest {
        val pricings = listOf(
            RoutePricing("p1", testRoute.id, testRoute.originTerminal.id, testRoute.destinationTerminal.id, "Surabaya", "Malang", Money.rupiah(45000))
        )

        val params = LaunchCustomRouteParams(
            route = testRoute,
            pricings = pricings,
            bus = testBus,
            driver = testDriver,
            departureHour = 7,
            departureMinute = 0,
            direction = RouteDirection.ORIGIN_TO_DESTINATION,
            headwayMinutes = 60,
            transitDwellMinutes = 10,
            isRoundTrip = true,
            returnHour = 14,
            returnMinute = 30,
            returnDirection = RouteDirection.DESTINATION_TO_ORIGIN
        )

        useCase(params)

        assertEquals(2, fakeScheduleRepo.addedSchedules.size)
        val outbound = fakeScheduleRepo.addedSchedules[0]
        val inbound = fakeScheduleRepo.addedSchedules[1]

        assertEquals(7, outbound.departureHour)
        assertEquals(0, outbound.departureMinute)
        assertEquals(RouteDirection.ORIGIN_TO_DESTINATION, outbound.direction)

        assertEquals(14, inbound.departureHour)
        assertEquals(30, inbound.departureMinute)
        assertEquals(RouteDirection.DESTINATION_TO_ORIGIN, inbound.direction)
    }

    private class FakeRouteRepository : RouteRepository {
        var savedRoute: BusRoute? = null
        override fun getAllSegments() = emptyList<com.example.busmanager.domain.route.model.RouteSegment>()
        override fun getSegmentById(id: String) = null
        override fun getSegmentsBetween(fromTerminalId: String, toTerminalId: String) = emptyList<com.example.busmanager.domain.route.model.RouteSegment>()
        override fun getAllPredefinedRoutes() = emptyList<BusRoute>()
        override fun getAllRoutes() = if (savedRoute != null) listOf(savedRoute!!) else emptyList()
        override fun getRouteById(id: String) = if (savedRoute?.id == id) savedRoute else null
        override suspend fun saveRoute(route: BusRoute) { savedRoute = route }
        override fun findRoutes(originTerminalId: String, destinationTerminalId: String, preferredPathType: RoutePathType?) = emptyList<BusRoute>()
        override fun getConnectedTerminalIds(terminalId: String) = emptyList<String>()
        override suspend fun deleteRoute(routeId: String) { savedRoute = null }
        override suspend fun deleteAllCustomRoutes() { savedRoute = null }
    }

    private class FakeRoutePricingRepository : RoutePricingRepository {
        val savedPricings = mutableListOf<RoutePricing>()
        override fun getPricingsForRoute(routeId: String) = flowOf(savedPricings)
        override suspend fun getPricingsListForRoute(routeId: String) = savedPricings
        override fun getAllPricingsFlow() = flowOf(savedPricings)
        override suspend fun getAllPricings() = savedPricings
        override suspend fun savePricing(pricing: RoutePricing) { savedPricings.add(pricing) }
        override suspend fun savePricings(pricings: List<RoutePricing>) { savedPricings.addAll(pricings) }
        override suspend fun getPricingBetween(routeId: String, originTerminalId: String, destinationTerminalId: String) = savedPricings.firstOrNull()
        override suspend fun generateAndSaveDefaultPricings(route: BusRoute) = emptyList<RoutePricing>()
        override suspend fun deleteAllPricings() { savedPricings.clear() }
    }

    private class FakeDriverRepository : DriverRepository {
        val drivers = mutableListOf<Driver>()
        val assignedDriverBusMap = mutableMapOf<String, String>()

        fun addDriverDirect(driver: Driver) { drivers.add(driver) }
        override fun getAllDrivers() = flowOf(drivers)
        override fun getHiredDrivers() = flowOf(drivers.filter { it.isHired })
        override suspend fun getDriverById(id: String) = drivers.find { it.id == id }
        override suspend fun hireDriver(driverId: String) = true
        override suspend fun assignDriverToBus(driverId: String, busId: String): Boolean {
            assignedDriverBusMap[driverId] = busId
            return true
        }
        override suspend fun unassignDriver(driverId: String) { assignedDriverBusMap.remove(driverId) }
        override suspend fun setPendingRelease(driverId: String, isPending: Boolean) {}
        override suspend fun fireDriver(driverId: String) { drivers.removeAll { it.id == driverId } }
        override suspend fun seedCandidateDriversIfEmpty() {}
        override suspend fun resetAllDrivers() { drivers.clear() }
    }

    private class FakeFleetRepository : FleetRepository {
        val buses = mutableListOf<Bus>()
        fun addBusDirect(bus: Bus) { buses.add(bus) }
        override fun getOwnedBuses() = flowOf(buses)
        override suspend fun getBusById(id: String) = buses.find { it.id == id }
        override suspend fun addBus(bus: Bus) { buses.add(bus) }
        override suspend fun updateBus(bus: Bus) {
            val idx = buses.indexOfFirst { it.id == bus.id }
            if (idx >= 0) buses[idx] = bus else buses.add(bus)
        }
        override suspend fun removeBus(busId: String) { buses.removeAll { it.id == busId } }
        override suspend fun removeAllBuses() { buses.clear() }
    }

    private class FakeRouteScheduleRepository : RouteScheduleRepository {
        val addedSchedules = mutableListOf<RouteSchedule>()
        override fun getSchedulesForRoute(routeId: String) = flowOf(addedSchedules)
        override fun getAllSchedules() = flowOf(addedSchedules)
        override suspend fun getSchedulesListForRoute(routeId: String) = addedSchedules
        override suspend fun getScheduleById(id: String) = addedSchedules.find { it.id == id }
        override suspend fun getSchedulesForBus(busId: String) = addedSchedules.filter { it.assignedBusId == busId }
        override fun getSchedulesForBusFlow(busId: String) = flowOf(addedSchedules.filter { it.assignedBusId == busId })
        override suspend fun addSchedule(schedule: RouteSchedule): Result<Unit> {
            addedSchedules.add(schedule)
            return Result.success(Unit)
        }
        override suspend fun updateSchedule(schedule: RouteSchedule) = Result.success(Unit)
        override suspend fun assignBusToSchedule(scheduleId: String, busId: String) = true
        override suspend fun unassignBusFromSchedule(scheduleId: String) {}
        override suspend fun deleteSchedule(scheduleId: String) {}
        override suspend fun deleteSchedulesForBus(busId: String) {}
        override suspend fun deleteSchedulesForRoute(routeId: String) {}
        override suspend fun deleteAllSchedules() { addedSchedules.clear() }
        override suspend fun checkConflict(routeId: String, direction: RouteDirection, hour: Int, minute: Int, excludeScheduleId: String?) = false
    }
}
