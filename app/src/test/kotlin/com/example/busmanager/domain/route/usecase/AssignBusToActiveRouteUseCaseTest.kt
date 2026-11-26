package com.example.busmanager.domain.route.usecase

import com.example.busmanager.data.local.datasource.ChassisDataSource
import com.example.busmanager.data.local.datasource.KaroseriBodyDataSource
import com.example.busmanager.data.local.datasource.RouteDataSource
import com.example.busmanager.data.local.datasource.TerminalDataSource
import com.example.busmanager.data.repository.RouteCatalogRepositoryImpl
import com.example.busmanager.domain.driver.model.Driver
import com.example.busmanager.domain.driver.repository.DriverRepository
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.fleet.repository.FleetRepository
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.RouteSchedule
import com.example.busmanager.domain.route.repository.RouteScheduleRepository
import com.example.busmanager.domain.simulation.model.RouteDirection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AssignBusToActiveRouteUseCaseTest {

    private lateinit var terminalDataSource: TerminalDataSource
    private lateinit var routeDataSource: RouteDataSource
    private lateinit var routeRepository: RouteCatalogRepositoryImpl
    private lateinit var fakeFleetRepository: FakeFleetRepository
    private lateinit var fakeDriverRepository: FakeDriverRepository
    private lateinit var fakeScheduleRepository: FakeRouteScheduleRepository
    private lateinit var validateScheduleConflictUseCase: ValidateScheduleConflictUseCase
    private lateinit var useCase: AssignBusToActiveRouteUseCase

    private lateinit var sampleBus: Bus
    private lateinit var sampleDriver: Driver
    private lateinit var sampleRoute: BusRoute

    @Before
    fun setUp() {
        terminalDataSource = TerminalDataSource()
        routeDataSource = RouteDataSource(terminalDataSource)
        routeRepository = RouteCatalogRepositoryImpl(routeDataSource)
        fakeFleetRepository = FakeFleetRepository()
        fakeDriverRepository = FakeDriverRepository()
        fakeScheduleRepository = FakeRouteScheduleRepository()
        validateScheduleConflictUseCase = ValidateScheduleConflictUseCase(fakeScheduleRepository, routeRepository)

        useCase = AssignBusToActiveRouteUseCase(
            fleetRepository = fakeFleetRepository,
            routeRepository = routeRepository,
            driverRepository = fakeDriverRepository,
            routeScheduleRepository = fakeScheduleRepository,
            validateScheduleConflictUseCase = validateScheduleConflictUseCase
        )

        sampleRoute = routeRepository.getAllPredefinedRoutes().first { it.id == "route_sby_mlg_toll" }

        val chassis = ChassisDataSource().getChassisList().first()
        val body = KaroseriBodyDataSource().getKaroseriBodyList().first()
        sampleBus = Bus(
            id = "bus-1",
            busCode = "TN-01",
            chassis = chassis,
            body = body,
            routeId = null,
            driverId = null
        )
        fakeFleetRepository.addBusSync(sampleBus)

        sampleDriver = Driver(
            id = "driver-1",
            name = "Pak Bambang",
            wagePerDay = com.example.busmanager.domain.company.model.Money.rupiah(200_000L),
            assignedBusId = null,
            isHired = true
        )
        fakeDriverRepository.addDriverSync(sampleDriver)
    }

    @Test
    fun assignBus_whenValid_updatesBusDriverAndCreatesSchedule() = runBlocking {
        val result = useCase(
            busId = sampleBus.id,
            routeId = sampleRoute.id,
            driverId = sampleDriver.id,
            departureHour = 7,
            departureMinute = 0,
            direction = RouteDirection.ORIGIN_TO_DESTINATION,
            headwayMinutes = 60,
            transitDwellMinutes = 10
        )

        assertTrue(result.isSuccess)

        val updatedBus = fakeFleetRepository.getBusById(sampleBus.id)
        assertNotNull(updatedBus)
        assertEquals(sampleRoute.id, updatedBus?.routeId)
        assertEquals(sampleDriver.id, updatedBus?.driverId)

        val updatedDriver = fakeDriverRepository.getDriverById(sampleDriver.id)
        assertNotNull(updatedDriver)
        assertEquals(sampleBus.id, updatedDriver?.assignedBusId)

        val schedules = fakeScheduleRepository.getSchedulesListForRoute(sampleRoute.id)
        assertEquals(1, schedules.size)
        assertEquals(7, schedules.first().departureHour)
        assertEquals(0, schedules.first().departureMinute)
        assertEquals(sampleBus.id, schedules.first().assignedBusId)
    }

    @Test
    fun assignBus_whenScheduleConflicts_failsWithoutModifyingBus() = runBlocking {
        fakeScheduleRepository.addSchedule(
            RouteSchedule(
                id = "existing-sched",
                routeId = sampleRoute.id,
                departureHour = 7,
                departureMinute = 0,
                direction = RouteDirection.ORIGIN_TO_DESTINATION
            )
        )

        val result = useCase(
            busId = sampleBus.id,
            routeId = sampleRoute.id,
            driverId = sampleDriver.id,
            departureHour = 7,
            departureMinute = 0,
            direction = RouteDirection.ORIGIN_TO_DESTINATION
        )

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull(exception)
        assertTrue(exception?.message?.contains("Bentrok jadwal") == true)

        val busStillIdle = fakeFleetRepository.getBusById(sampleBus.id)
        assertEquals(null, busStillIdle?.routeId)
    }

    @Test
    fun assignBus_whenDriverAssignedToOtherBus_fails() = runBlocking {
        val busyDriver = Driver(
            id = "driver-busy",
            name = "Pak Rudi",
            wagePerDay = com.example.busmanager.domain.company.model.Money.rupiah(200_000L),
            assignedBusId = "bus-other",
            isHired = true
        )
        fakeDriverRepository.addDriverSync(busyDriver)

        val result = useCase(
            busId = sampleBus.id,
            routeId = sampleRoute.id,
            driverId = busyDriver.id,
            departureHour = 8,
            departureMinute = 0,
            direction = RouteDirection.ORIGIN_TO_DESTINATION
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("sudah bertugas") == true)
    }

    private class FakeFleetRepository : FleetRepository {
        private val list = mutableListOf<Bus>()
        private val flow = MutableStateFlow<List<Bus>>(emptyList())

        fun addBusSync(bus: Bus) {
            list.add(bus)
            flow.value = list.toList()
        }

        override fun getOwnedBuses(): Flow<List<Bus>> = flow
        override suspend fun getBusById(id: String): Bus? = list.find { it.id == id }
        override suspend fun addBus(bus: Bus) {
            list.add(bus)
            flow.value = list.toList()
        }
        override suspend fun updateBus(bus: Bus) {
            val idx = list.indexOfFirst { it.id == bus.id }
            if (idx >= 0) {
                list[idx] = bus
                flow.value = list.toList()
            }
        }
        override suspend fun removeBus(busId: String) {
            list.removeAll { it.id == busId }
            flow.value = list.toList()
        }
        override suspend fun removeAllBuses() {}
}

    private class FakeDriverRepository : DriverRepository {
        private val list = mutableListOf<Driver>()
        private val flow = MutableStateFlow<List<Driver>>(emptyList())

        fun addDriverSync(driver: Driver) {
            list.add(driver)
            flow.value = list.toList()
        }

        override fun getAllDrivers(): Flow<List<Driver>> = flow
        override fun getHiredDrivers(): Flow<List<Driver>> = flow
        override suspend fun getDriverById(id: String): Driver? = list.find { it.id == id }
        override suspend fun hireDriver(driverId: String): Boolean = true
        override suspend fun fireDriver(driverId: String) {}
        override suspend fun assignDriverToBus(driverId: String, busId: String): Boolean {
            val idx = list.indexOfFirst { it.id == driverId }
            if (idx >= 0) {
                list[idx] = list[idx].copy(assignedBusId = busId)
                flow.value = list.toList()
            }
            return true
        }
        override suspend fun unassignDriver(driverId: String) {}
        override suspend fun seedCandidateDriversIfEmpty() {}
        override suspend fun setPendingRelease(driverId: String, isPending: Boolean) {}
    override suspend fun resetAllDrivers() {}
}

    private class FakeRouteScheduleRepository : RouteScheduleRepository {
        private val list = mutableListOf<RouteSchedule>()
        private val flow = MutableStateFlow<List<RouteSchedule>>(emptyList())

        override fun getSchedulesForRoute(routeId: String): Flow<List<RouteSchedule>> = flow
        override fun getAllSchedules(): Flow<List<RouteSchedule>> = flow
        override suspend fun getSchedulesListForRoute(routeId: String): List<RouteSchedule> = list
        override suspend fun getScheduleById(id: String): RouteSchedule? = list.find { it.id == id }

        override suspend fun addSchedule(schedule: RouteSchedule): Result<Unit> {
            list.add(schedule)
            flow.value = list.toList()
            return Result.success(Unit)
        }

        override suspend fun updateSchedule(schedule: RouteSchedule): Result<Unit> = Result.success(Unit)
        override suspend fun assignBusToSchedule(scheduleId: String, busId: String): Boolean = true
        override suspend fun unassignBusFromSchedule(scheduleId: String) {}
        override suspend fun deleteSchedule(scheduleId: String) {}
        override suspend fun checkConflict(
            routeId: String,
            direction: RouteDirection,
            hour: Int,
            minute: Int,
            excludeScheduleId: String?
        ): Boolean = false
        override suspend fun getSchedulesForBus(busId: String): List<com.example.busmanager.domain.route.model.RouteSchedule> = emptyList()
    override fun getSchedulesForBusFlow(busId: String): kotlinx.coroutines.flow.Flow<List<com.example.busmanager.domain.route.model.RouteSchedule>> = kotlinx.coroutines.flow.emptyFlow()
    override suspend fun deleteSchedulesForBus(busId: String) {}
    override suspend fun deleteSchedulesForRoute(routeId: String) {}
    override suspend fun deleteAllSchedules() {}
}
}
