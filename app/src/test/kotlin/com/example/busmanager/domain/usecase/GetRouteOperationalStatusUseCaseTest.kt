package com.example.busmanager.domain.usecase

import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.driver.model.Driver
import com.example.busmanager.domain.driver.repository.DriverRepository
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
import com.example.busmanager.domain.fleet.repository.FleetRepository
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.MapCoordinate
import com.example.busmanager.domain.route.model.RoutePathType
import com.example.busmanager.domain.route.model.RoutePricing
import com.example.busmanager.domain.route.model.RouteSchedule
import com.example.busmanager.domain.route.model.RouteSegment
import com.example.busmanager.domain.route.model.Terminal
import com.example.busmanager.domain.route.model.TerminalType
import com.example.busmanager.domain.route.repository.RoutePricingRepository
import com.example.busmanager.domain.route.repository.RouteRepository
import com.example.busmanager.domain.route.repository.RouteScheduleRepository
import com.example.busmanager.domain.route.usecase.GetRouteOperationalStatusUseCase
import com.example.busmanager.domain.simulation.model.ActiveBusTrip
import com.example.busmanager.domain.simulation.model.GameCalendar
import com.example.busmanager.domain.simulation.model.RouteDirection
import com.example.busmanager.domain.simulation.repository.SimulationTripRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetRouteOperationalStatusUseCaseTest {

    private lateinit var fakeRouteRepo: FakeRouteRepository
    private lateinit var fakeScheduleRepo: FakeRouteScheduleRepository
    private lateinit var fakePricingRepo: FakeRoutePricingRepository
    private lateinit var fakeFleetRepo: FakeFleetRepository
    private lateinit var fakeDriverRepo: FakeDriverRepository
    private lateinit var fakeTripRepo: FakeSimulationTripRepository
    private lateinit var useCase: GetRouteOperationalStatusUseCase

    private val terminalA = Terminal(
        id = "term-a",
        name = "Terminal A",
        city = "Surabaya",
        type = TerminalType.TIPE_A,
        coordinate = MapCoordinate(0.5f, 0.5f, -7.25, 112.75),
        retributionFee = Money.rupiah(5_000L)
    )
    private val terminalB = Terminal(
        id = "term-b",
        name = "Terminal B",
        city = "Malang",
        type = TerminalType.TIPE_A,
        coordinate = MapCoordinate(0.6f, 0.6f, -7.98, 112.63),
        retributionFee = Money.rupiah(5_000L)
    )

    private val activeRoute = BusRoute(
        id = "route-active",
        name = "Surabaya - Malang",
        originTerminal = terminalA,
        destinationTerminal = terminalB,
        segments = emptyList(),
        pathType = RoutePathType.TOLL
    )

    private val inactiveRoute = BusRoute(
        id = "route-inactive",
        name = "Surabaya - Probolinggo",
        originTerminal = terminalA,
        destinationTerminal = terminalB,
        segments = emptyList(),
        pathType = RoutePathType.NON_TOLL
    )

    private fun createBus(id: String, busCode: String, routeId: String?, driverId: String?): Bus {
        val chassis = Chassis(
            id = "c1",
            modelName = "Chassis 1",
            brand = ChassisBrand.HINO,
            engineLocation = EngineLocation.REAR,
            suspension = SuspensionType.LEAF_SPRING,
            axleType = AxleType.SINGLE_4X2,
            transmission = TransmissionType.MANUAL,
            horsepower = 260,
            torqueNm = 750,
            fuelCapacityLiters = 270,
            fuelConsumptionKmPerLiter = 3.5f,
            price = Money.rupiah(1_000_000_000L)
        )
        val body = KaroseriBody(
            id = "b1",
            modelName = "Body 1",
            brand = KaroseriBrand.ADIPUTRO,
            bodyType = BusBodyType.SHD,
            busClass = BusClass.EKSEKUTIF,
            passengerCapacity = 32,
            luggageCapacityKg = 1500,
            comfortRating = 8,
            price = Money.rupiah(500_000_000L)
        )
        return Bus(
            id = id,
            busCode = busCode,
            chassis = chassis,
            body = body,
            routeId = routeId,
            driverId = driverId
        )
    }

    @Before
    fun setUp() {
        fakeRouteRepo = FakeRouteRepository(listOf(activeRoute, inactiveRoute))
        fakeScheduleRepo = FakeRouteScheduleRepository()
        fakePricingRepo = FakeRoutePricingRepository()
        fakeFleetRepo = FakeFleetRepository()
        fakeDriverRepo = FakeDriverRepository()
        fakeTripRepo = FakeSimulationTripRepository()

        useCase = GetRouteOperationalStatusUseCase(
            routeRepository = fakeRouteRepo,
            routeScheduleRepository = fakeScheduleRepo,
            routePricingRepository = fakePricingRepo,
            fleetRepository = fakeFleetRepo,
            driverRepository = fakeDriverRepo,
            simulationTripRepository = fakeTripRepo
        )
    }

    @Test
    fun returnsOnlyOpenedRoutesCategorizedByActiveStatus() = runBlocking {
        fakePricingRepo.pricings = listOf(
            RoutePricing(
                id = "p1",
                routeId = "route-active",
                originTerminalId = "term-a",
                destinationTerminalId = "term-b",
                originCity = "Surabaya",
                destinationCity = "Malang",
                price = Money.rupiah(50_000L)
            ),
            RoutePricing(
                id = "p2",
                routeId = "route-inactive",
                originTerminalId = "term-a",
                destinationTerminalId = "term-b",
                originCity = "Surabaya",
                destinationCity = "Malang",
                price = Money.rupiah(40_000L)
            )
        )

        val driver = Driver(
            id = "drv-1",
            name = "Sopir Handal",
            wagePerDay = Money.rupiah(200_000L),
            isHired = true,
            assignedBusId = "bus-1"
        )
        fakeDriverRepo.drivers = listOf(driver)

        val bus = createBus("bus-1", "MK01", "route-active", "drv-1")
        fakeFleetRepo.buses = listOf(bus)

        val schedule = RouteSchedule(
            id = "sched-1",
            routeId = "route-active",
            departureHour = 8,
            departureMinute = 0,
            direction = RouteDirection.ORIGIN_TO_DESTINATION,
            assignedBusId = "bus-1"
        )
        fakeScheduleRepo.schedules = listOf(schedule)

        val result = useCase().first()

        assertEquals(2, result.size)

        val activeResult = result.find { it.route.id == "route-active" }
        assertTrue(activeResult != null)
        assertTrue(activeResult!!.isActive)
        assertEquals(1, activeResult.assignedBuses.size)
        assertEquals(1, activeResult.assignedDrivers.size)
        assertEquals(1, activeResult.departureSchedules.size)

        val inactiveResult = result.find { it.route.id == "route-inactive" }
        assertTrue(inactiveResult != null)
        assertFalse(inactiveResult!!.isActive)
        assertTrue(inactiveResult.assignedBuses.isEmpty())
        assertTrue(inactiveResult.assignedDrivers.isEmpty())
    }

    private class FakeRouteRepository(val routesList: List<BusRoute>) : RouteRepository {
        override fun getAllSegments(): List<RouteSegment> = emptyList()
        override fun getSegmentById(id: String): RouteSegment? = null
        override fun getSegmentsBetween(fromTerminalId: String, toTerminalId: String): List<RouteSegment> = emptyList()
        override fun getAllPredefinedRoutes(): List<BusRoute> = routesList
        override fun getAllRoutes(): List<BusRoute> = routesList
        override fun getRouteById(id: String): BusRoute? = routesList.find { it.id == id }
        override suspend fun saveRoute(route: BusRoute) {}
        override fun findRoutes(originTerminalId: String, destinationTerminalId: String, preferredPathType: RoutePathType?): List<BusRoute> = routesList
        override fun getConnectedTerminalIds(terminalId: String): List<String> = emptyList()
        override suspend fun deleteRoute(routeId: String) {}
        override suspend fun deleteAllCustomRoutes() {}
    }

    private class FakeRouteScheduleRepository : RouteScheduleRepository {
        var schedules = listOf<RouteSchedule>()
        override fun getSchedulesForRoute(routeId: String): Flow<List<RouteSchedule>> = flowOf(schedules.filter { it.routeId == routeId })
        override fun getAllSchedules(): Flow<List<RouteSchedule>> = flowOf(schedules)
        override suspend fun getSchedulesListForRoute(routeId: String): List<RouteSchedule> = schedules.filter { it.routeId == routeId }
        override suspend fun getScheduleById(id: String): RouteSchedule? = schedules.find { it.id == id }
        override suspend fun addSchedule(schedule: RouteSchedule) = Result.success(Unit)
        override suspend fun updateSchedule(schedule: RouteSchedule) = Result.success(Unit)
        override suspend fun assignBusToSchedule(scheduleId: String, busId: String) = true
        override suspend fun unassignBusFromSchedule(scheduleId: String) {}
        override suspend fun deleteSchedule(scheduleId: String) {}
        override suspend fun deleteAllSchedules() {}
        override suspend fun checkConflict(routeId: String, direction: RouteDirection, hour: Int, minute: Int, excludeScheduleId: String?) = false
    }

    private class FakeRoutePricingRepository : RoutePricingRepository {
        var pricings = listOf<RoutePricing>()
        override fun getPricingsForRoute(routeId: String): Flow<List<RoutePricing>> = flowOf(pricings.filter { it.routeId == routeId })
        override suspend fun getPricingsListForRoute(routeId: String): List<RoutePricing> = pricings.filter { it.routeId == routeId }
        override fun getAllPricingsFlow(): Flow<List<RoutePricing>> = flowOf(pricings)
        override suspend fun getAllPricings(): List<RoutePricing> = pricings
        override suspend fun savePricing(pricing: RoutePricing) {}
        override suspend fun savePricings(pricings: List<RoutePricing>) {}
        override suspend fun getPricingBetween(routeId: String, originTerminalId: String, destinationTerminalId: String): RoutePricing? = null
        override suspend fun generateAndSaveDefaultPricings(route: BusRoute): List<RoutePricing> = emptyList()
    }

    private class FakeFleetRepository : FleetRepository {
        var buses = listOf<Bus>()
        override fun getOwnedBuses(): Flow<List<Bus>> = flowOf(buses)
        override suspend fun getBusById(busId: String): Bus? = buses.find { it.id == busId }
        override suspend fun addBus(bus: Bus) {}
        override suspend fun updateBus(bus: Bus) {}
        override suspend fun removeBus(id: String) {}
        override suspend fun removeAllBuses() {}
    }

    private class FakeDriverRepository : DriverRepository {
        var drivers = listOf<Driver>()
        override fun getAllDrivers(): Flow<List<Driver>> = flowOf(drivers)
        override fun getHiredDrivers(): Flow<List<Driver>> = flowOf(drivers.filter { it.isHired })
        override suspend fun getDriverById(id: String): Driver? = drivers.find { it.id == id }
        override suspend fun hireDriver(driverId: String): Boolean = true
        override suspend fun assignDriverToBus(driverId: String, busId: String): Boolean = true
        override suspend fun unassignDriver(driverId: String) {}
        override suspend fun fireDriver(driverId: String) {}
        override suspend fun resetAllDrivers() {}
        override suspend fun seedCandidateDriversIfEmpty() {}
    }

    private class FakeSimulationTripRepository : SimulationTripRepository {
        private val _trips = MutableStateFlow<List<ActiveBusTrip>>(emptyList())
        override val activeTrips: StateFlow<List<ActiveBusTrip>> = _trips
        override fun dispatchBus(bus: Bus, route: BusRoute, direction: RouteDirection, currentTime: GameCalendar): ActiveBusTrip = error("Not implemented")
        override fun advanceTripsTime(advanceMinutes: Int): List<ActiveBusTrip> = emptyList()
        override fun cancelTrip(tripId: String) {}
        override fun markCancelNextSchedule(tripId: String, cancel: Boolean) {}
        override fun completeTrip(tripId: String) {}
        override fun clearTrips() {}
    }
}
