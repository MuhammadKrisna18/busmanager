package com.example.busmanager.domain.route.usecase

import com.example.busmanager.data.local.datasource.ChassisDataSource
import com.example.busmanager.data.local.datasource.KaroseriBodyDataSource
import com.example.busmanager.data.local.datasource.TerminalDataSource
import com.example.busmanager.data.repository.RouteCatalogRepositoryImpl
import com.example.busmanager.data.repository.SimulationTripRepositoryImpl
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.fleet.repository.FleetRepository
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.RoutePathType
import com.example.busmanager.domain.route.model.RoutePricing
import com.example.busmanager.domain.route.model.RouteSchedule
import com.example.busmanager.domain.route.repository.RoutePricingRepository
import com.example.busmanager.domain.route.repository.RouteScheduleRepository
import com.example.busmanager.domain.simulation.model.GameCalendar
import com.example.busmanager.domain.simulation.model.RouteDirection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DeleteRouteUseCaseTest {

    private lateinit var routeRepository: RouteCatalogRepositoryImpl
    private lateinit var fleetRepository: FakeFleetRepository
    private lateinit var simulationTripRepository: SimulationTripRepositoryImpl
    private lateinit var scheduleRepository: FakeRouteScheduleRepository
    private lateinit var pricingRepository: FakeRoutePricingRepository
    private lateinit var useCase: DeleteRouteUseCase
    private lateinit var testRoute: BusRoute

    private class FakeRouteScheduleRepository : RouteScheduleRepository {
        var deletedRouteId: String? = null
        override fun getSchedulesForRoute(routeId: String): Flow<List<RouteSchedule>> = MutableStateFlow(emptyList())
        override fun getAllSchedules(): Flow<List<RouteSchedule>> = MutableStateFlow(emptyList())
        override suspend fun getSchedulesListForRoute(routeId: String): List<RouteSchedule> = emptyList()
        override suspend fun getScheduleById(id: String): RouteSchedule? = null
        override suspend fun getSchedulesForBus(busId: String): List<RouteSchedule> = emptyList()
        override fun getSchedulesForBusFlow(busId: String): Flow<List<RouteSchedule>> = MutableStateFlow(emptyList())
        override suspend fun addSchedule(schedule: RouteSchedule): Result<Unit> = Result.success(Unit)
        override suspend fun updateSchedule(schedule: RouteSchedule): Result<Unit> = Result.success(Unit)
        override suspend fun assignBusToSchedule(scheduleId: String, busId: String): Boolean = true
        override suspend fun unassignBusFromSchedule(scheduleId: String) {}
        override suspend fun deleteSchedule(scheduleId: String) {}
        override suspend fun deleteSchedulesForBus(busId: String) {}
        override suspend fun deleteSchedulesForRoute(routeId: String) {
            deletedRouteId = routeId
        }
        override suspend fun deleteAllSchedules() {}
        override suspend fun checkConflict(
            routeId: String,
            direction: RouteDirection,
            hour: Int,
            minute: Int,
            excludeScheduleId: String?
        ): Boolean = false
    }

    private class FakeRoutePricingRepository : RoutePricingRepository {
        var deletedRouteId: String? = null
        override fun getPricingsForRoute(routeId: String): Flow<List<RoutePricing>> = MutableStateFlow(emptyList())
        override suspend fun getPricingsListForRoute(routeId: String): List<RoutePricing> = emptyList()
        override fun getAllPricingsFlow(): Flow<List<RoutePricing>> = MutableStateFlow(emptyList())
        override suspend fun getAllPricings(): List<RoutePricing> = emptyList()
        override suspend fun savePricing(pricing: RoutePricing) {}
        override suspend fun savePricings(pricings: List<RoutePricing>) {}
        override suspend fun getPricingBetween(routeId: String, originTerminalId: String, destinationTerminalId: String): RoutePricing? = null
        override suspend fun generateAndSaveDefaultPricings(route: BusRoute): List<RoutePricing> = emptyList()
        override suspend fun deletePricingsForRoute(routeId: String) {
            deletedRouteId = routeId
        }
        override suspend fun deleteAllPricings() {}
    }

    private class FakeFleetRepository : FleetRepository {
        private val _buses = MutableStateFlow<List<Bus>>(emptyList())
        override fun getOwnedBuses(): Flow<List<Bus>> = _buses.asStateFlow()
        override suspend fun getBusById(id: String): Bus? = _buses.value.find { it.id == id }
        override suspend fun addBus(bus: Bus) {
            _buses.value = _buses.value + bus
        }
        override suspend fun updateBus(bus: Bus) {
            _buses.value = _buses.value.map { if (it.id == bus.id) bus else it }
        }
        override suspend fun removeBus(busId: String) {
            _buses.value = _buses.value.filterNot { it.id == busId }
        }
        override suspend fun removeAllBuses() {}
}

    @Before
    fun setUp() {
        routeRepository = RouteCatalogRepositoryImpl()
        fleetRepository = FakeFleetRepository()
        simulationTripRepository = SimulationTripRepositoryImpl()
        scheduleRepository = FakeRouteScheduleRepository()
        pricingRepository = FakeRoutePricingRepository()
        useCase = DeleteRouteUseCase(
            routeRepository,
            fleetRepository,
            simulationTripRepository,
            scheduleRepository,
            pricingRepository
        )

        val terminals = TerminalDataSource()
        val sby = terminals.getTerminalById("term_purabaya")!!
        val mlg = terminals.getTerminalById("term_arjosari")!!

        testRoute = BusRoute(
            id = "custom_deletable_route",
            name = "Surabaya - Malang Express Test",
            originTerminal = sby,
            destinationTerminal = mlg,
            pathType = RoutePathType.TOLL,
            segments = routeRepository.getSegmentsBetween(sby.id, mlg.id),
            transitTerminals = emptyList()
        )
        runBlocking {
            routeRepository.saveRoute(testRoute)
        }
    }

    @Test
    fun invoke_routeNotFound_returnsRouteNotFound() = runBlocking {
        val result = useCase("non_existent_route")
        assertEquals(DeleteRouteResult.RouteNotFound, result)
    }

    @Test
    fun invoke_routeHasActiveTrips_returnsRouteHasActiveTrips() = runBlocking {
        val chassis = ChassisDataSource().getChassisList().first()
        val body = KaroseriBodyDataSource().getKaroseriBodyList().first()
        val bus = Bus(
            id = "bus-active-1",
            busCode = "TN-01",
            chassis = chassis,
            body = body,
            routeId = testRoute.id,
            isOperating = true
        )
        fleetRepository.addBus(bus)

        simulationTripRepository.dispatchBus(
            bus = bus,
            route = testRoute,
            direction = RouteDirection.ORIGIN_TO_DESTINATION,
            currentTime = GameCalendar.initial()
        )

        val result = useCase(testRoute.id)
        assertTrue(result is DeleteRouteResult.RouteHasActiveTrips)
        assertEquals(1, (result as DeleteRouteResult.RouteHasActiveTrips).activeCount)
    }

    @Test
    fun invoke_idleRoute_unassignsBusesAndDeletesRoute() = runBlocking {
        val chassis = ChassisDataSource().getChassisList().first()
        val body = KaroseriBodyDataSource().getKaroseriBodyList().first()
        val bus1 = Bus(
            id = "bus-idle-1",
            busCode = "TN-01",
            chassis = chassis,
            body = body,
            routeId = testRoute.id,
            isOperating = false
        )
        val bus2 = Bus(
            id = "bus-idle-2",
            busCode = "TN-02",
            chassis = chassis,
            body = body,
            routeId = "other_route",
            isOperating = false
        )
        fleetRepository.addBus(bus1)
        fleetRepository.addBus(bus2)

        val result = useCase(testRoute.id)
        assertTrue(result is DeleteRouteResult.Success)
        assertEquals(testRoute.name, (result as DeleteRouteResult.Success).routeName)

        assertEquals(testRoute.id, scheduleRepository.deletedRouteId)
        assertEquals(testRoute.id, pricingRepository.deletedRouteId)

        assertNull(routeRepository.getRouteById(testRoute.id))

        val updatedBus1 = fleetRepository.getBusById("bus-idle-1")
        assertNull(updatedBus1?.routeId)

        val updatedBus2 = fleetRepository.getBusById("bus-idle-2")
        assertEquals("other_route", updatedBus2?.routeId)
    }
}
