package com.example.busmanager.presentation.dashboard

import com.example.busmanager.domain.company.model.Company
import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.company.repository.CompanyRepository
import com.example.busmanager.domain.company.usecase.GetCompanyUseCase
import com.example.busmanager.domain.company.usecase.ResetGameUseCase
import com.example.busmanager.domain.company.usecase.StartNewGameUseCase
import com.example.busmanager.domain.driver.model.Driver
import com.example.busmanager.domain.driver.repository.DriverRepository
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.fleet.repository.FleetRepository
import com.example.busmanager.domain.fleet.usecase.GetOwnedFleetUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeScreenViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeCompanyRepo: FakeCompanyRepository
    private lateinit var fakeFleetRepo: FakeFleetRepository
    private lateinit var viewModel: HomeScreenViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeCompanyRepo = FakeCompanyRepository()
        fakeFleetRepo = FakeFleetRepository()

        val getCompanyUseCase = GetCompanyUseCase(fakeCompanyRepo)
        val getOwnedFleetUseCase = GetOwnedFleetUseCase(fakeFleetRepo)
        val startNewGameUseCase = StartNewGameUseCase(fakeCompanyRepo)
        val resetGameUseCase = ResetGameUseCase(
            companyRepository = fakeCompanyRepo,
            fleetRepository = fakeFleetRepo,
            driverRepository = FakeDriverRepository(),
            routeScheduleRepository = FakeRouteScheduleRepository(),
            routePricingRepository = FakeRoutePricingRepository()
        )

        viewModel = HomeScreenViewModel(
            getCompanyUseCase = getCompanyUseCase,
            getOwnedFleetUseCase = getOwnedFleetUseCase,
            startNewGameUseCase = startNewGameUseCase,
            resetGameUseCase = resetGameUseCase
        )
    }

    @After
    fun tearDown() {
        testDispatcher.scheduler.advanceUntilIdle()
        Dispatchers.resetMain()
    }

    @Test
    fun initialStateLoadsCompanyData() = runTest(testDispatcher) {
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertFalse(state.isLoading)
        assertEquals("PO TransNusantara", state.company?.name)
        assertEquals(Money.rupiah(50_000_000_000L), state.company?.balance)
    }

    @Test
    fun resetGame_resetsCompanyBalanceAndState() = runTest(testDispatcher) {
        testDispatcher.scheduler.advanceUntilIdle()

        fakeCompanyRepo.updateBalance(Money.rupiah(10_000L))
        assertEquals(Money.rupiah(10_000L), fakeCompanyRepo.getBalance())

        viewModel.resetGame()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(StartNewGameUseCase.DEFAULT_STARTING_BALANCE, fakeCompanyRepo.getBalance())
    }

    @Test
    fun togglePlaySimulationChangesState() = runTest(testDispatcher) {
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isPlaying)
        viewModel.togglePlaySimulation()
        assertTrue(viewModel.uiState.value.isPlaying)
        viewModel.togglePlaySimulation()
        assertFalse(viewModel.uiState.value.isPlaying)
    }

    @Test
    fun routeInfoModal_opensAndDismissesCorrectly() = runTest(testDispatcher) {
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showRouteInfoModal)

        viewModel.openRouteInfoModal()
        assertTrue(viewModel.uiState.value.showRouteInfoModal)

        viewModel.dismissRouteInfoModal()
        assertFalse(viewModel.uiState.value.showRouteInfoModal)
    }

    @Test
    fun offlineProgressionDialog_dismissesCorrectly() = runTest(testDispatcher) {
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(viewModel.uiState.value.offlineProgressionResult)

        viewModel.dismissOfflineProgressionDialog()
        assertNull(viewModel.uiState.value.offlineProgressionResult)
    }

    private class FakeCompanyRepository : CompanyRepository {
        private val state = MutableStateFlow<Company?>(null)

        override fun getCompany(): Flow<Company?> = state

        override suspend fun createCompany(name: String, initialBalance: Money): Company {
            val created = Company(id = "comp-1", name = name, balance = initialBalance)
            state.value = created
            return created
        }

        override suspend fun updateBalance(newBalance: Money) {
            state.value = state.value?.copy(balance = newBalance)
        }

        override suspend fun deductBalance(amount: Money): Boolean {
            val current = state.value ?: return false
            if (!current.balance.canAfford(amount)) return false
            state.value = current.copy(balance = current.balance - amount)
            return true
        }

        override suspend fun addBalance(amount: Money) {
            val current = state.value ?: return
            state.value = current.copy(balance = current.balance + amount)
        }

        fun getBalance(): Money = state.value?.balance ?: Money.ZERO

        override suspend fun resetCompany(name: String, initialBalance: Money) {
            state.value = Company("comp-1", name, initialBalance, 100)
        }
    }

    private class FakeFleetRepository : FleetRepository {
        private val state = MutableStateFlow<List<Bus>>(emptyList())

        override fun getOwnedBuses(): Flow<List<Bus>> = state

        override suspend fun getBusById(id: String): Bus? = null

        override suspend fun addBus(bus: Bus) {
            state.value = state.value + bus
        }

        override suspend fun updateBus(bus: Bus) {}

        override suspend fun removeBus(busId: String) {}

        override suspend fun removeAllBuses() {
            state.value = emptyList()
        }
    }

    private class FakeDriverRepository : DriverRepository {
        override fun getAllDrivers(): Flow<List<Driver>> = MutableStateFlow(emptyList())
        override fun getHiredDrivers(): Flow<List<Driver>> = MutableStateFlow(emptyList())
        override suspend fun getDriverById(id: String): Driver? = null
        override suspend fun hireDriver(driverId: String): Boolean = true
        override suspend fun assignDriverToBus(driverId: String, busId: String): Boolean = true
        override suspend fun unassignDriver(driverId: String) {}
        override suspend fun fireDriver(driverId: String) {}
        override suspend fun seedCandidateDriversIfEmpty() {}
        override suspend fun resetAllDrivers() {}
        override suspend fun setPendingRelease(driverId: String, isPending: Boolean) {}
}

    private class FakeRouteScheduleRepository : com.example.busmanager.domain.route.repository.RouteScheduleRepository {
        override fun getSchedulesForRoute(routeId: String) = MutableStateFlow(emptyList<com.example.busmanager.domain.route.model.RouteSchedule>())
        override fun getAllSchedules() = MutableStateFlow(emptyList<com.example.busmanager.domain.route.model.RouteSchedule>())
        override suspend fun getSchedulesListForRoute(routeId: String) = emptyList<com.example.busmanager.domain.route.model.RouteSchedule>()
        override suspend fun getScheduleById(id: String) = null
        override suspend fun addSchedule(schedule: com.example.busmanager.domain.route.model.RouteSchedule) = Result.success(Unit)
        override suspend fun updateSchedule(schedule: com.example.busmanager.domain.route.model.RouteSchedule) = Result.success(Unit)
        override suspend fun assignBusToSchedule(scheduleId: String, busId: String) = true
        override suspend fun unassignBusFromSchedule(scheduleId: String) {}
        override suspend fun deleteSchedule(scheduleId: String) {}
        override suspend fun deleteAllSchedules() {}
        override suspend fun checkConflict(routeId: String, direction: com.example.busmanager.domain.simulation.model.RouteDirection, hour: Int, minute: Int, excludeScheduleId: String?) = false
        override suspend fun getSchedulesForBus(busId: String): List<com.example.busmanager.domain.route.model.RouteSchedule> = emptyList()
    override fun getSchedulesForBusFlow(busId: String): kotlinx.coroutines.flow.Flow<List<com.example.busmanager.domain.route.model.RouteSchedule>> = kotlinx.coroutines.flow.emptyFlow()
    override suspend fun deleteSchedulesForBus(busId: String) {}
    override suspend fun deleteSchedulesForRoute(routeId: String) {}
}

    private class FakeRoutePricingRepository : com.example.busmanager.domain.route.repository.RoutePricingRepository {
        override fun getPricingsForRoute(routeId: String) = MutableStateFlow(emptyList<com.example.busmanager.domain.route.model.RoutePricing>())
        override suspend fun getPricingsListForRoute(routeId: String) = emptyList<com.example.busmanager.domain.route.model.RoutePricing>()
        override fun getAllPricingsFlow() = MutableStateFlow(emptyList<com.example.busmanager.domain.route.model.RoutePricing>())
        override suspend fun getAllPricings() = emptyList<com.example.busmanager.domain.route.model.RoutePricing>()
        override suspend fun savePricing(pricing: com.example.busmanager.domain.route.model.RoutePricing) {}
        override suspend fun savePricings(pricings: List<com.example.busmanager.domain.route.model.RoutePricing>) {}
        override suspend fun getPricingBetween(routeId: String, originTerminalId: String, destinationTerminalId: String) = null
        override suspend fun generateAndSaveDefaultPricings(route: com.example.busmanager.domain.route.model.BusRoute) = emptyList<com.example.busmanager.domain.route.model.RoutePricing>()
        override suspend fun deleteAllPricings() {}
    }
}
