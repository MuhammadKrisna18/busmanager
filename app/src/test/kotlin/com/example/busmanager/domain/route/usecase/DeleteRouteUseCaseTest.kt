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
    private lateinit var useCase: DeleteRouteUseCase
    private lateinit var testRoute: BusRoute

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
    }

    @Before
    fun setUp() {
        routeRepository = RouteCatalogRepositoryImpl()
        fleetRepository = FakeFleetRepository()
        simulationTripRepository = SimulationTripRepositoryImpl()
        useCase = DeleteRouteUseCase(routeRepository, fleetRepository, simulationTripRepository)

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
        routeRepository.saveRoute(testRoute)
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

        assertNull(routeRepository.getRouteById(testRoute.id))

        val updatedBus1 = fleetRepository.getBusById("bus-idle-1")
        assertNull(updatedBus1?.routeId)

        val updatedBus2 = fleetRepository.getBusById("bus-idle-2")
        assertEquals("other_route", updatedBus2?.routeId)
    }
}
