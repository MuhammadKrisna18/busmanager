package com.example.busmanager.domain.usecase

import com.example.busmanager.domain.company.model.Company
import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.company.repository.CompanyRepository
import com.example.busmanager.domain.company.usecase.ResetGameUseCase
import com.example.busmanager.domain.company.usecase.StartNewGameUseCase
import com.example.busmanager.domain.driver.model.Driver
import com.example.busmanager.domain.driver.repository.DriverRepository
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.fleet.repository.FleetRepository
import com.example.busmanager.domain.route.repository.RoutePricingRepository
import com.example.busmanager.domain.route.repository.RouteScheduleRepository
import com.example.busmanager.domain.simulation.model.GameCalendar
import com.example.busmanager.domain.simulation.model.SimulationSpeed
import com.example.busmanager.domain.simulation.repository.SimulationTimeRepository
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

class ResetGameUseCaseTest {

    private lateinit var fakeCompanyRepo: FakeCompanyRepository
    private lateinit var fakeFleetRepo: FakeFleetRepository
    private lateinit var fakeDriverRepo: FakeDriverRepository
    private lateinit var fakeScheduleRepo: FakeRouteScheduleRepository
    private lateinit var fakePricingRepo: FakeRoutePricingRepository
    private lateinit var fakeTimeRepo: FakeSimulationTimeRepository
    private lateinit var fakeRouteRepo: FakeRouteRepository
    private lateinit var resetGameUseCase: ResetGameUseCase

    @Before
    fun setUp() {
        fakeCompanyRepo = FakeCompanyRepository()
        fakeFleetRepo = FakeFleetRepository()
        fakeDriverRepo = FakeDriverRepository()
        fakeScheduleRepo = FakeRouteScheduleRepository()
        fakePricingRepo = FakeRoutePricingRepository()
        fakeTimeRepo = FakeSimulationTimeRepository()
        fakeRouteRepo = FakeRouteRepository()

        resetGameUseCase = ResetGameUseCase(
            companyRepository = fakeCompanyRepo,
            fleetRepository = fakeFleetRepo,
            driverRepository = fakeDriverRepo,
            routeScheduleRepository = fakeScheduleRepo,
            routePricingRepository = fakePricingRepo,
            simulationTimeRepository = fakeTimeRepo,
            routeRepository = fakeRouteRepo
        )
    }

    @Test
    fun resetGame_clearsAllDataAndRestoresDefaults() = runBlocking {
        resetGameUseCase()

        assertTrue(fakeFleetRepo.wasRemoveAllCalled)
        assertTrue(fakeDriverRepo.wasResetAllCalled)
        assertTrue(fakeDriverRepo.wasSeedCalled)
        assertTrue(fakeScheduleRepo.wasDeleteAllCalled)
        assertTrue(fakePricingRepo.wasDeleteAllCalled)
        assertTrue(fakeRouteRepo.wasDeleteAllCalled)
        assertFalse(fakeTimeRepo.isPlaying.value)
        assertEquals(StartNewGameUseCase.DEFAULT_STARTING_BALANCE, fakeCompanyRepo.company?.balance)
        assertEquals(StartNewGameUseCase.DEFAULT_COMPANY_NAME, fakeCompanyRepo.company?.name)
    }

    private class FakeCompanyRepository : CompanyRepository {
        var company: Company? = null

        override fun getCompany(): Flow<Company?> = MutableStateFlow(company)
        override suspend fun createCompany(name: String, initialBalance: Money): Company {
            val c = Company("comp-1", name, initialBalance, 100)
            company = c
            return c
        }
        override suspend fun updateBalance(newBalance: Money) {}
        override suspend fun deductBalance(amount: Money): Boolean = true
        override suspend fun addBalance(amount: Money) {}
        override suspend fun resetCompany(name: String, initialBalance: Money) {
            company = Company("comp-1", name, initialBalance, 100)
        }
    }

    private class FakeFleetRepository : FleetRepository {
        var wasRemoveAllCalled = false

        override fun getOwnedBuses(): Flow<List<Bus>> = MutableStateFlow(emptyList())
        override suspend fun getBusById(id: String): Bus? = null
        override suspend fun addBus(bus: Bus) {}
        override suspend fun updateBus(bus: Bus) {}
        override suspend fun removeBus(busId: String) {}
        override suspend fun removeAllBuses() {
            wasRemoveAllCalled = true
        }
    }

    private class FakeDriverRepository : DriverRepository {
        var wasResetAllCalled = false
        var wasSeedCalled = false

        override fun getAllDrivers(): Flow<List<Driver>> = MutableStateFlow(emptyList())
        override fun getHiredDrivers(): Flow<List<Driver>> = MutableStateFlow(emptyList())
        override suspend fun getDriverById(id: String): Driver? = null
        override suspend fun hireDriver(driverId: String): Boolean = true
        override suspend fun assignDriverToBus(driverId: String, busId: String): Boolean = true
        override suspend fun unassignDriver(driverId: String) {}
        override suspend fun fireDriver(driverId: String) {}
        override suspend fun seedCandidateDriversIfEmpty() {
            wasSeedCalled = true
        }
        override suspend fun resetAllDrivers() {
            wasResetAllCalled = true
        }
        override suspend fun setPendingRelease(driverId: String, isPending: Boolean) {}
}

    private class FakeRouteScheduleRepository : RouteScheduleRepository {
        var wasDeleteAllCalled = false

        override fun getSchedulesForRoute(routeId: String) = MutableStateFlow(emptyList<com.example.busmanager.domain.route.model.RouteSchedule>())
        override fun getAllSchedules() = MutableStateFlow(emptyList<com.example.busmanager.domain.route.model.RouteSchedule>())
        override suspend fun getSchedulesListForRoute(routeId: String) = emptyList<com.example.busmanager.domain.route.model.RouteSchedule>()
        override suspend fun getScheduleById(id: String) = null
        override suspend fun addSchedule(schedule: com.example.busmanager.domain.route.model.RouteSchedule) = Result.success(Unit)
        override suspend fun updateSchedule(schedule: com.example.busmanager.domain.route.model.RouteSchedule) = Result.success(Unit)
        override suspend fun assignBusToSchedule(scheduleId: String, busId: String) = true
        override suspend fun unassignBusFromSchedule(scheduleId: String) {}
        override suspend fun deleteSchedule(scheduleId: String) {}
        override suspend fun deleteAllSchedules() {
            wasDeleteAllCalled = true
        }
        override suspend fun checkConflict(routeId: String, direction: com.example.busmanager.domain.simulation.model.RouteDirection, hour: Int, minute: Int, excludeScheduleId: String?) = false
        override suspend fun getSchedulesForBus(busId: String): List<com.example.busmanager.domain.route.model.RouteSchedule> = emptyList()
    override fun getSchedulesForBusFlow(busId: String): kotlinx.coroutines.flow.Flow<List<com.example.busmanager.domain.route.model.RouteSchedule>> = kotlinx.coroutines.flow.emptyFlow()
    override suspend fun deleteSchedulesForBus(busId: String) {}
    override suspend fun deleteSchedulesForRoute(routeId: String) {}
}

    private class FakeRoutePricingRepository : RoutePricingRepository {
        var wasDeleteAllCalled = false

        override fun getPricingsForRoute(routeId: String) = MutableStateFlow(emptyList<com.example.busmanager.domain.route.model.RoutePricing>())
        override suspend fun getPricingsListForRoute(routeId: String) = emptyList<com.example.busmanager.domain.route.model.RoutePricing>()
        override fun getAllPricingsFlow() = MutableStateFlow(emptyList<com.example.busmanager.domain.route.model.RoutePricing>())
        override suspend fun getAllPricings() = emptyList<com.example.busmanager.domain.route.model.RoutePricing>()
        override suspend fun savePricing(pricing: com.example.busmanager.domain.route.model.RoutePricing) {}
        override suspend fun savePricings(pricings: List<com.example.busmanager.domain.route.model.RoutePricing>) {}
        override suspend fun getPricingBetween(routeId: String, originTerminalId: String, destinationTerminalId: String) = null
        override suspend fun generateAndSaveDefaultPricings(route: com.example.busmanager.domain.route.model.BusRoute) = emptyList<com.example.busmanager.domain.route.model.RoutePricing>()
        override suspend fun deleteAllPricings() {
            wasDeleteAllCalled = true
        }
    }

    private class FakeSimulationTimeRepository : SimulationTimeRepository {
        override val currentTime = MutableStateFlow(GameCalendar.initial())
        override val isPlaying = MutableStateFlow(true)
        override val currentSpeed = MutableStateFlow(SimulationSpeed.NORMAL)

        override fun play() {
            isPlaying.value = true
        }
        override fun pause() {
            isPlaying.value = false
        }
        override fun togglePlay() {
            isPlaying.value = !isPlaying.value
        }
        override fun setSpeed(speed: SimulationSpeed) {}
        override fun advanceMinutes(minutes: Int) {}
        override fun setTime(calendar: GameCalendar) {
            currentTime.value = calendar
        }
    }

    private class FakeRouteRepository : com.example.busmanager.domain.route.repository.RouteRepository {
        var wasDeleteAllCalled = false

        override fun getAllSegments() = emptyList<com.example.busmanager.domain.route.model.RouteSegment>()
        override fun getSegmentById(id: String) = null
        override fun getSegmentsBetween(fromTerminalId: String, toTerminalId: String) = emptyList<com.example.busmanager.domain.route.model.RouteSegment>()
        override fun getAllPredefinedRoutes() = emptyList<com.example.busmanager.domain.route.model.BusRoute>()
        override fun getAllRoutes() = emptyList<com.example.busmanager.domain.route.model.BusRoute>()
        override fun getRouteById(id: String) = null
        override suspend fun saveRoute(route: com.example.busmanager.domain.route.model.BusRoute) {}
        override fun findRoutes(originTerminalId: String, destinationTerminalId: String, preferredPathType: com.example.busmanager.domain.route.model.RoutePathType?) = emptyList<com.example.busmanager.domain.route.model.BusRoute>()
        override fun getConnectedTerminalIds(terminalId: String) = emptyList<String>()
        override suspend fun deleteRoute(routeId: String) {}
        override suspend fun deleteAllCustomRoutes() {
            wasDeleteAllCalled = true
        }
    }
}
