package com.example.busmanager.domain.usecase

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
import com.example.busmanager.domain.fleet.repository.FleetRepository
import com.example.busmanager.domain.fleet.usecase.ReconcileBusOperatingStateUseCase
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.MapCoordinate
import com.example.busmanager.domain.route.model.RoadCharacteristic
import com.example.busmanager.domain.route.model.RoutePathType
import com.example.busmanager.domain.route.model.RouteSegment
import com.example.busmanager.domain.route.model.Terminal
import com.example.busmanager.domain.route.model.TerminalType
import com.example.busmanager.domain.route.repository.RouteRepository
import com.example.busmanager.domain.simulation.model.ActiveBusTrip
import com.example.busmanager.domain.simulation.model.GameCalendar
import com.example.busmanager.domain.simulation.model.RouteDirection
import com.example.busmanager.domain.simulation.repository.SimulationTripRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ReconcileBusOperatingStateUseCaseTest {

    private lateinit var fakeFleetRepo: FakeFleetRepository
    private lateinit var fakeTripRepo: FakeSimulationTripRepository
    private lateinit var fakeRouteRepo: FakeRouteRepository
    private lateinit var reconcileUseCase: ReconcileBusOperatingStateUseCase

    private fun createDummyBus(
        id: String = "bus-1",
        busCode: String = "MK01",
        currentTerminalId: String? = null,
        routeId: String? = null,
        isOperating: Boolean = false
    ): Bus {
        val chassis = Chassis(
            id = "chassis-1",
            modelName = "Hino RK8",
            brand = ChassisBrand.HINO,
            engineLocation = EngineLocation.REAR,
            suspension = SuspensionType.LEAF_SPRING,
            axleType = AxleType.SINGLE_4X2,
            transmission = TransmissionType.MANUAL,
            horsepower = 260,
            torqueNm = 750,
            price = Money.rupiah(1_000_000_000L)
        )
        val body = KaroseriBody(
            id = "body-1",
            modelName = "Jetbus 3+ SHD",
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
            currentTerminalId = currentTerminalId,
            routeId = routeId,
            isOperating = isOperating
        )
    }

    private fun createDummyRoute(): BusRoute {
        val t1 = Terminal("term_purabaya", "Purabaya", "Surabaya", TerminalType.TIPE_A, MapCoordinate(0.5f, 0.5f, -7.35, 112.72), Money.ZERO)
        val t2 = Terminal("term_arjosari", "Arjosari", "Malang", TerminalType.TIPE_A, MapCoordinate(0.5f, 0.6f, -7.93, 112.65), Money.ZERO)
        val segment = RouteSegment(
            id = "seg-1",
            fromTerminalId = t1.id,
            toTerminalId = t2.id,
            pathType = RoutePathType.TOLL,
            characteristic = RoadCharacteristic.FLAT,
            distanceKm = 90.0,
            baseDurationMinutes = 90
        )
        return BusRoute(
            id = "route-sby-mlg",
            name = "Surabaya - Malang",
            originTerminal = t1,
            destinationTerminal = t2,
            pathType = RoutePathType.TOLL,
            segments = listOf(segment)
        )
    }

    @Before
    fun setUp() {
        fakeFleetRepo = FakeFleetRepository()
        fakeTripRepo = FakeSimulationTripRepository()
        fakeRouteRepo = FakeRouteRepository(createDummyRoute())
        reconcileUseCase = ReconcileBusOperatingStateUseCase(
            fleetRepository = fakeFleetRepo,
            simulationTripRepository = fakeTripRepo,
            routeRepository = fakeRouteRepo
        )
    }

    @Test
    fun whenBusIsOperatingInDb_butNoActiveTrip_reconcilesToFalseAndSetsTerminal() = runBlocking {
        val stuckBus = createDummyBus(
            id = "bus-stuck",
            isOperating = true,
            currentTerminalId = null,
            routeId = "route-sby-mlg"
        )
        fakeFleetRepo.addBus(stuckBus)

        val reconciled = reconcileUseCase()

        assertEquals(1, reconciled.size)
        val fixedBus = fakeFleetRepo.getBusById("bus-stuck")
        assertFalse(fixedBus!!.isOperating)
        assertEquals("term_purabaya", fixedBus.currentTerminalId)
    }

    @Test
    fun whenBusNotOperatingInDb_butActiveTripExists_reconcilesToTrue() = runBlocking {
        val bus = createDummyBus(
            id = "bus-active",
            isOperating = false,
            currentTerminalId = "term_purabaya",
            routeId = "route-sby-mlg"
        )
        fakeFleetRepo.addBus(bus)

        val route = createDummyRoute()
        val trip = ActiveBusTrip(
            id = "trip-1",
            busId = "bus-active",
            busCode = "MK01",
            route = route,
            direction = RouteDirection.ORIGIN_TO_DESTINATION,
            departureTime = GameCalendar.initial(),
            estimatedArrivalMinutes = 90,
            elapsedMinutes = 30,
            progressFraction = 0.33f,
            currentCoordinate = MapCoordinate(0.5f, 0.55f, -7.5, 112.7),
            passengerCount = 30,
            ticketEarnings = Money.rupiah(1_500_000L),
            isCompleted = false,
            maxCapacity = 32
        )
        fakeTripRepo.activeTripsFlow.value = listOf(trip)

        val reconciled = reconcileUseCase()

        assertEquals(1, reconciled.size)
        val fixedBus = fakeFleetRepo.getBusById("bus-active")
        assertTrue(fixedBus!!.isOperating)
    }

    @Test
    fun whenBusStateMatchesActiveTrips_noReconciliationNeeded() = runBlocking {
        val standbyBus = createDummyBus(
            id = "bus-standby",
            isOperating = false,
            currentTerminalId = "term_purabaya"
        )
        fakeFleetRepo.addBus(standbyBus)

        val reconciled = reconcileUseCase()

        assertTrue(reconciled.isEmpty())
        val sameBus = fakeFleetRepo.getBusById("bus-standby")
        assertFalse(sameBus!!.isOperating)
    }

    private class FakeFleetRepository : FleetRepository {
        val buses = mutableListOf<Bus>()
        private val flow = MutableStateFlow<List<Bus>>(emptyList())

        override fun getOwnedBuses(): Flow<List<Bus>> = flow

        override suspend fun getBusById(id: String): Bus? = buses.find { it.id == id }

        override suspend fun addBus(bus: Bus) {
            buses.removeAll { it.id == bus.id }
            buses.add(bus)
            flow.value = buses.toList()
        }

        override suspend fun updateBus(bus: Bus) {
            val idx = buses.indexOfFirst { it.id == bus.id }
            if (idx >= 0) {
                buses[idx] = bus
            } else {
                buses.add(bus)
            }
            flow.value = buses.toList()
        }

        override suspend fun removeBus(busId: String) {
            buses.removeAll { it.id == busId }
            flow.value = buses.toList()
        }

        override suspend fun removeAllBuses() {
            buses.clear()
            flow.value = emptyList()
        }
    }

    private class FakeSimulationTripRepository : SimulationTripRepository {
        val activeTripsFlow = MutableStateFlow<List<ActiveBusTrip>>(emptyList())
        override val activeTrips: StateFlow<List<ActiveBusTrip>> = activeTripsFlow

        override fun dispatchBus(bus: Bus, route: BusRoute, direction: RouteDirection, currentTime: GameCalendar): ActiveBusTrip {
            error("Not needed")
        }

        override fun advanceTripsTime(advanceMinutes: Int): List<ActiveBusTrip> = activeTripsFlow.value
        override fun cancelTrip(tripId: String) {}
        override fun markCancelNextSchedule(tripId: String, cancel: Boolean) {}
        override fun completeTrip(tripId: String) {}
        override fun clearTrips() {
            activeTripsFlow.value = emptyList()
        }
    }

    private class FakeRouteRepository(private val route: BusRoute) : RouteRepository {
        override fun getAllSegments() = route.segments
        override fun getSegmentById(id: String) = route.segments.find { it.id == id }
        override fun getSegmentsBetween(fromTerminalId: String, toTerminalId: String) = route.segments
        override fun getAllPredefinedRoutes() = listOf(route)
        override fun getAllRoutes() = listOf(route)
        override fun getRouteById(id: String) = if (id == route.id) route else null
        override suspend fun saveRoute(route: BusRoute) {}
        override fun findRoutes(originTerminalId: String, destinationTerminalId: String, preferredPathType: RoutePathType?) = listOf(route)
        override fun getConnectedTerminalIds(terminalId: String) = emptyList<String>()
        override suspend fun deleteRoute(routeId: String) {}
        override suspend fun deleteAllCustomRoutes() {}
    }
}
