package com.example.busmanager.presentation.driver

import com.example.busmanager.data.repository.ChassisCatalogRepositoryImpl
import com.example.busmanager.data.repository.KaroseriCatalogRepositoryImpl
import com.example.busmanager.domain.company.model.Company
import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.company.repository.CompanyRepository
import com.example.busmanager.domain.company.usecase.GetCompanyUseCase
import com.example.busmanager.domain.driver.model.Driver
import com.example.busmanager.domain.driver.repository.DriverRepository
import com.example.busmanager.domain.driver.usecase.AssignDriverToBusUseCase
import com.example.busmanager.domain.driver.usecase.GetAvailableDriversUseCase
import com.example.busmanager.domain.driver.usecase.GetHiredDriversUseCase
import com.example.busmanager.domain.driver.usecase.HireDriverUseCase
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DriverManagementViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeDriverRepo: FakeDriverRepository
    private lateinit var fakeFleetRepo: FakeFleetRepository
    private lateinit var fakeCompanyRepo: FakeCompanyRepository
    private lateinit var viewModel: DriverManagementViewModel

    private val chassisRepo = ChassisCatalogRepositoryImpl()
    private val karoseriRepo = KaroseriCatalogRepositoryImpl()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeDriverRepo = FakeDriverRepository()
        fakeFleetRepo = FakeFleetRepository()
        fakeCompanyRepo = FakeCompanyRepository()

        val getAvailableDriversUseCase = GetAvailableDriversUseCase(fakeDriverRepo)
        val getHiredDriversUseCase = GetHiredDriversUseCase(fakeDriverRepo)
        val hireDriverUseCase = HireDriverUseCase(fakeDriverRepo)
        val assignDriverToBusUseCase = AssignDriverToBusUseCase(fakeDriverRepo)
        val getOwnedFleetUseCase = GetOwnedFleetUseCase(fakeFleetRepo)
        val getCompanyUseCase = GetCompanyUseCase(fakeCompanyRepo)

        viewModel = DriverManagementViewModel(
            getAvailableDriversUseCase = getAvailableDriversUseCase,
            getHiredDriversUseCase = getHiredDriversUseCase,
            hireDriverUseCase = hireDriverUseCase,
            assignDriverToBusUseCase = assignDriverToBusUseCase,
            getOwnedFleetUseCase = getOwnedFleetUseCase,
            getCompanyUseCase = getCompanyUseCase,
            driverRepository = fakeDriverRepo
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialStateLoadsHiredDriversAndCandidates() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.hiredDrivers.size)
        assertEquals(2, state.candidateDrivers.size)
        assertEquals("driver-1", state.hiredDrivers.first().id)
        assertEquals("Pak Bambang", state.hiredDrivers.first().name)
    }

    @Test
    fun selectTabSwitchesSelectedTab() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(DriverTab.HIRED, viewModel.uiState.value.selectedTab)
        viewModel.selectTab(DriverTab.CANDIDATES)
        assertEquals(DriverTab.CANDIDATES, viewModel.uiState.value.selectedTab)
    }

    @Test
    fun hireDriverUpdatesStatusAndShowsMessage() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.hireDriver("driver-2")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.hiredDrivers.size)
        assertEquals(1, state.candidateDrivers.size)
        assertEquals("Sopir berhasil direkrut ke armada!", state.message)
    }

    @Test
    fun openAndCloseAssignDialogUpdatesState() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val driver = viewModel.uiState.value.hiredDrivers.first()
        viewModel.openAssignDialog(driver)
        assertEquals(driver, viewModel.uiState.value.selectedDriverForAssignment)

        viewModel.closeAssignDialog()
        assertNull(viewModel.uiState.value.selectedDriverForAssignment)
    }

    @Test
    fun assignDriverToBusUpdatesStateAndDismissesDialog() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.openAssignDialog(viewModel.uiState.value.hiredDrivers.first())
        viewModel.assignDriverToBus("driver-1", "bus-1")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.selectedDriverForAssignment)
        assertEquals("Sopir berhasil ditugaskan ke armada!", state.message)

        val driver = state.hiredDrivers.find { it.id == "driver-1" }
        assertEquals("bus-1", driver?.assignedBusId)
    }

    @Test
    fun unassignDriverClearsBusAssignment() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.assignDriverToBus("driver-1", "bus-1")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.unassignDriver("driver-1")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        val driver = state.hiredDrivers.find { it.id == "driver-1" }
        assertNull(driver?.assignedBusId)
        assertEquals("Tugas sopir berhasil dilepas", state.message)
    }

    @Test
    fun unassignDriverWhenBusOperatingMarksPendingRelease() = runTest {
        val bus = Bus(
            id = "bus-op-1",
            busCode = "TN-99",
            chassis = chassisRepo.getAllChassis().first().first(),
            body = karoseriRepo.getAllBodies().first().first(),
            isOperating = true,
            driverId = "driver-1"
        )
        fakeFleetRepo.addBus(bus)
        viewModel.assignDriverToBus("driver-1", "bus-op-1")
        testDispatcher.scheduler.advanceUntilIdle()

        val driver = viewModel.uiState.value.hiredDrivers.first { it.id == "driver-1" }
        viewModel.openUnassignConfirmation(driver)
        assertEquals(driver, viewModel.uiState.value.driverToUnassign)

        viewModel.confirmUnassignDriver()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.driverToUnassign)
        val updatedDriver = state.hiredDrivers.first { it.id == "driver-1" }
        assertTrue(updatedDriver.isPendingRelease)
        assertEquals("bus-op-1", updatedDriver.assignedBusId)
        assertTrue(state.message!!.contains("sedang dinas"))
    }

    @Test
    fun openAndConfirmFireDriverRemovesFromHiredList() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val driver = viewModel.uiState.value.hiredDrivers.first()
        viewModel.openFireConfirmation(driver)
        assertEquals(driver, viewModel.uiState.value.driverToFire)

        viewModel.confirmFireDriver()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.driverToFire)
        assertEquals(0, state.hiredDrivers.size)
        assertEquals(3, state.candidateDrivers.size)
        assertTrue(state.message!!.contains("telah diberhentikan (PHK)"))
    }

    @Test
    fun metricsComputationMatchesState() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.hiredDrivers.size)
        assertEquals(0, state.assignedDriverCount)
        assertEquals(1, state.unassignedDriverCount)
        assertEquals(Money.rupiah(180_000L), state.totalDailyWage)

        viewModel.assignDriverToBus("driver-1", "bus-1")
        testDispatcher.scheduler.advanceUntilIdle()

        val updated = viewModel.uiState.value
        assertEquals(1, updated.assignedDriverCount)
        assertEquals(0, updated.unassignedDriverCount)
    }

    private class FakeDriverRepository : DriverRepository {
        private val drivers = mutableMapOf(
            "driver-1" to Driver("driver-1", "Pak Bambang", Money.rupiah(180_000L), null, true),
            "driver-2" to Driver("driver-2", "Pak Joko", Money.rupiah(200_000L), null, false),
            "driver-3" to Driver("driver-3", "Mas Agus", Money.rupiah(150_000L), null, false)
        )
        private val driversFlow = MutableStateFlow<List<Driver>>(drivers.values.toList())
        private val hiredFlow = MutableStateFlow<List<Driver>>(drivers.values.filter { it.isHired })

        private fun refresh() {
            driversFlow.value = drivers.values.toList()
            hiredFlow.value = drivers.values.filter { it.isHired }
        }

        override fun getAllDrivers(): Flow<List<Driver>> = driversFlow

        override fun getHiredDrivers(): Flow<List<Driver>> = hiredFlow

        override suspend fun getDriverById(id: String): Driver? = drivers[id]

        override suspend fun hireDriver(driverId: String): Boolean {
            val d = drivers[driverId] ?: return false
            drivers[driverId] = d.copy(isHired = true)
            refresh()
            return true
        }

        override suspend fun assignDriverToBus(driverId: String, busId: String): Boolean {
            val d = drivers[driverId] ?: return false
            drivers.forEach { (id, driver) ->
                if (driver.assignedBusId == busId) {
                    drivers[id] = driver.copy(assignedBusId = null)
                }
            }
            drivers[driverId] = d.copy(assignedBusId = busId, isHired = true)
            refresh()
            return true
        }

        override suspend fun unassignDriver(driverId: String) {
            val d = drivers[driverId] ?: return
            drivers[driverId] = d.copy(assignedBusId = null, isPendingRelease = false)
            refresh()
        }

        override suspend fun setPendingRelease(driverId: String, isPending: Boolean) {
            val d = drivers[driverId] ?: return
            drivers[driverId] = d.copy(isPendingRelease = isPending)
            refresh()
        }

        override suspend fun fireDriver(driverId: String) {
            val d = drivers[driverId] ?: return
            drivers[driverId] = d.copy(isHired = false, assignedBusId = null)
            refresh()
        }

        override suspend fun seedCandidateDriversIfEmpty() {}
    }

    private class FakeFleetRepository : FleetRepository {
        private val buses = MutableStateFlow<List<Bus>>(emptyList())
        override fun getOwnedBuses(): Flow<List<Bus>> = buses
        override suspend fun getBusById(id: String): Bus? = buses.value.find { it.id == id }
        override suspend fun addBus(bus: Bus) { buses.value = buses.value + bus }
        override suspend fun updateBus(bus: Bus) {
            buses.value = buses.value.map { if (it.id == bus.id) bus else it }
        }
        override suspend fun removeBus(busId: String) {
            buses.value = buses.value.filter { it.id != busId }
        }
    }

    private class FakeCompanyRepository : CompanyRepository {
        private val company = MutableStateFlow<Company?>(
            Company(id = "comp-1", name = "PO TransNusantara", balance = Money.rupiah(1_500_000_000L))
        )
        override fun getCompany(): Flow<Company?> = company
        override suspend fun createCompany(name: String, initialBalance: Money): Company = company.value!!
        override suspend fun updateBalance(newBalance: Money) { company.value = company.value?.copy(balance = newBalance) }
        override suspend fun deductBalance(amount: Money): Boolean = true
        override suspend fun addBalance(amount: Money) {}
    }
}
