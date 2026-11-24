package com.example.busmanager.presentation.garage

import com.example.busmanager.data.local.datasource.ChassisDataSource
import com.example.busmanager.data.local.datasource.KaroseriBodyDataSource
import com.example.busmanager.data.local.datasource.RouteDataSource
import com.example.busmanager.data.local.datasource.TerminalDataSource
import com.example.busmanager.data.repository.RouteCatalogRepositoryImpl
import com.example.busmanager.domain.company.model.Company
import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.company.repository.CompanyRepository
import com.example.busmanager.domain.driver.model.Driver
import com.example.busmanager.domain.driver.repository.DriverRepository
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.fleet.repository.FleetRepository
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.RoutePricing
import com.example.busmanager.domain.route.repository.RoutePricingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
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
class GarageViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val terminalDataSource = TerminalDataSource()
    private val routeDataSource = RouteDataSource(terminalDataSource)
    private val routeRepository = RouteCatalogRepositoryImpl(routeDataSource)

    private lateinit var fakeFleetRepo: FakeFleetRepository
    private lateinit var fakeDriverRepo: FakeDriverRepository
    private lateinit var fakePricingRepo: FakeRoutePricingRepository
    private lateinit var fakeCompanyRepo: FakeCompanyRepository

    private lateinit var viewModel: GarageViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        fakeFleetRepo = FakeFleetRepository()
        fakeDriverRepo = FakeDriverRepository()
        fakePricingRepo = FakeRoutePricingRepository()
        fakeCompanyRepo = FakeCompanyRepository()

        val sampleBus1 = Bus(
            id = "bus-1",
            busCode = "TN-01",
            chassis = ChassisDataSource().getChassisList().first(),
            body = KaroseriBodyDataSource().getKaroseriBodyList().first(),
            routeId = "route_sby_mlg_toll",
            driverId = "driver-1",
            isOperating = true
        )

        val sampleBus2 = Bus(
            id = "bus-2",
            busCode = "TN-02",
            chassis = ChassisDataSource().getChassisList().first(),
            body = KaroseriBodyDataSource().getKaroseriBodyList().first(),
            routeId = null,
            driverId = null,
            isOperating = false
        )

        fakeFleetRepo.setBuses(listOf(sampleBus1, sampleBus2))

        val driver1 = Driver(
            id = "driver-1",
            name = "Suryanto",
            wagePerDay = Money.rupiah(200_000L),
            assignedBusId = "bus-1",
            isHired = true
        )
        fakeDriverRepo.setDrivers(listOf(driver1))

        viewModel = GarageViewModel(
            fleetRepository = fakeFleetRepo,
            driverRepository = fakeDriverRepo,
            routeRepository = routeRepository,
            routePricingRepository = fakePricingRepo,
            companyRepository = fakeCompanyRepo
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_loadsBusesAndCalculatesCounts() = runTest {
        advanceUntilIdle()
        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertEquals(2, state.totalBusCount)
        assertEquals(1, state.operatingBusCount)
        assertEquals(1, state.standbyBusCount)
        assertEquals(2, state.filteredBuses.size)
    }

    @Test
    fun resolvesDriverAndRouteNamesCorrectly() = runTest {
        advanceUntilIdle()
        val bus1Item = viewModel.uiState.value.allBuses.find { it.bus.id == "bus-1" }
        assertNotNull(bus1Item)
        assertEquals("Suryanto", bus1Item?.driverName)
        assertNotNull(bus1Item?.routeName)
        assertTrue(bus1Item?.isOperating == true)

        val bus2Item = viewModel.uiState.value.allBuses.find { it.bus.id == "bus-2" }
        assertNotNull(bus2Item)
        assertNull(bus2Item?.driverName)
        assertNull(bus2Item?.routeName)
        assertFalse(bus2Item?.isOperating == true)
    }

    @Test
    fun setFilter_updatesFilteredBusesList() = runTest {
        advanceUntilIdle()

        viewModel.setFilter(GarageFilter.OPERATING)
        assertEquals(1, viewModel.uiState.value.filteredBuses.size)
        assertEquals("bus-1", viewModel.uiState.value.filteredBuses.first().bus.id)

        viewModel.setFilter(GarageFilter.STANDBY)
        assertEquals(1, viewModel.uiState.value.filteredBuses.size)
        assertEquals("bus-2", viewModel.uiState.value.filteredBuses.first().bus.id)

        viewModel.setFilter(GarageFilter.ALL)
        assertEquals(2, viewModel.uiState.value.filteredBuses.size)
    }

    private class FakeFleetRepository : FleetRepository {
        private val list = mutableListOf<Bus>()
        private val flow = MutableStateFlow<List<Bus>>(emptyList())

        fun setBuses(buses: List<Bus>) {
            list.clear()
            list.addAll(buses)
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
        override suspend fun removeBus(busId: String) {}
    }

    private class FakeDriverRepository : DriverRepository {
        private val list = mutableListOf<Driver>()
        private val flow = MutableStateFlow<List<Driver>>(emptyList())

        fun setDrivers(drivers: List<Driver>) {
            list.clear()
            list.addAll(drivers)
            flow.value = list.toList()
        }

        override fun getAllDrivers(): Flow<List<Driver>> = flow
        override fun getHiredDrivers(): Flow<List<Driver>> = flow
        override suspend fun getDriverById(id: String): Driver? = list.find { it.id == id }
        override suspend fun hireDriver(driverId: String): Boolean = true
        override suspend fun assignDriverToBus(driverId: String, busId: String): Boolean = true
        override suspend fun unassignDriver(driverId: String) {}
        override suspend fun fireDriver(driverId: String) {}
        override suspend fun seedCandidateDriversIfEmpty() {}
    }

    private class FakeRoutePricingRepository : RoutePricingRepository {
        override fun getPricingsForRoute(routeId: String): Flow<List<RoutePricing>> = flowOf(emptyList())
        override suspend fun getPricingsListForRoute(routeId: String): List<RoutePricing> = emptyList()
        override fun getAllPricingsFlow(): Flow<List<RoutePricing>> = flowOf(emptyList())
        override suspend fun getAllPricings(): List<RoutePricing> = emptyList()
        override suspend fun savePricing(pricing: RoutePricing) {}
        override suspend fun savePricings(pricings: List<RoutePricing>) {}
        override suspend fun getPricingBetween(
            routeId: String,
            originTerminalId: String,
            destinationTerminalId: String
        ): RoutePricing? = null
        override suspend fun generateAndSaveDefaultPricings(route: BusRoute): List<RoutePricing> = emptyList()
    }

    private class FakeCompanyRepository : CompanyRepository {
        private val company = Company(
            id = "comp-1",
            name = "PO TransNusantara",
            balance = Money.rupiah(1_000_000_000L),
            reputation = 100
        )
        private val flow = MutableStateFlow<Company?>(company)

        override fun getCompany(): Flow<Company?> = flow
        override suspend fun createCompany(name: String, initialBalance: Money): Company = company
        override suspend fun updateBalance(newBalance: Money) {
            flow.value = flow.value?.copy(balance = newBalance)
        }
        override suspend fun addBalance(amount: Money) {
            val current = flow.value?.balance ?: Money.ZERO
            flow.value = flow.value?.copy(balance = current + amount)
        }
        override suspend fun deductBalance(amount: Money): Boolean {
            val current = flow.value?.balance ?: Money.ZERO
            if (current.amount >= amount.amount) {
                flow.value = flow.value?.copy(balance = current - amount)
                return true
            }
            return false
        }
    }
}
