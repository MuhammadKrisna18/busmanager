package com.example.busmanager.presentation.simulation

import com.example.busmanager.data.local.datasource.RouteDataSource
import com.example.busmanager.data.local.datasource.TerminalDataSource
import com.example.busmanager.data.repository.RouteCatalogRepositoryImpl
import com.example.busmanager.data.repository.SimulationTimeRepositoryImpl
import com.example.busmanager.data.repository.TerminalCatalogRepositoryImpl
import com.example.busmanager.domain.company.model.Company
import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.company.repository.CompanyRepository
import com.example.busmanager.domain.company.usecase.GetCompanyUseCase
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
import com.example.busmanager.domain.fleet.usecase.GetOwnedFleetUseCase
import com.example.busmanager.domain.route.usecase.GetAvailableTerminalsUseCase
import com.example.busmanager.domain.simulation.model.GameCalendar
import com.example.busmanager.domain.simulation.usecase.GetSimulationTimeUseCase
import com.example.busmanager.domain.simulation.usecase.ToggleSimulationPlayUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
class SimulationMapViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var simulationTimeRepo: SimulationTimeRepositoryImpl
    private lateinit var fakeCompanyRepo: FakeCompanyRepository
    private lateinit var fakeFleetRepo: FakeFleetRepository
    private lateinit var viewModel: SimulationMapViewModel

    private val terminalDataSource = TerminalDataSource()
    private val routeDataSource = RouteDataSource(terminalDataSource)
    private val terminalRepo = TerminalCatalogRepositoryImpl(terminalDataSource)
    private val routeRepo = RouteCatalogRepositoryImpl(routeDataSource)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        simulationTimeRepo = SimulationTimeRepositoryImpl(
            initialTime = GameCalendar(2026, 11, 24, 8, 0)
        )
        fakeCompanyRepo = FakeCompanyRepository()
        fakeFleetRepo = FakeFleetRepository()

        val getTimeUseCase = GetSimulationTimeUseCase(simulationTimeRepo)
        val togglePlayUseCase = ToggleSimulationPlayUseCase(simulationTimeRepo)
        val getCompanyUseCase = GetCompanyUseCase(fakeCompanyRepo)
        val getOwnedFleetUseCase = GetOwnedFleetUseCase(fakeFleetRepo)
        val getTerminalsUseCase = GetAvailableTerminalsUseCase(terminalRepo)

        viewModel = SimulationMapViewModel(
            getTimeUseCase,
            togglePlayUseCase,
            getCompanyUseCase,
            getOwnedFleetUseCase,
            getTerminalsUseCase,
            routeRepo
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_loadsDefaultCalendarAndPausedState() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2026, state.currentTime.year)
        assertEquals(11, state.currentTime.month)
        assertEquals(24, state.currentTime.day)
        assertEquals(8, state.currentTime.hour)
        assertEquals(0, state.currentTime.minute)
        assertFalse(state.isPlaying)
        assertEquals("Selasa, 24 November 2026 • 08:00 WIB", state.formattedDateTime)
        assertEquals("Selasa, 24 November 2026", state.formattedDate)
        assertEquals("08:00 WIB", state.formattedTime)
        assertFalse(state.hasActiveFleet)
        assertEquals(SimulationBottomSheetType.NONE, state.activeBottomSheet)
    }

    @Test
    fun mapData_loadsTerminalsSegmentsAndRoutes() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.terminals.isNotEmpty())
        assertTrue(state.allSegments.isNotEmpty())
        assertTrue(state.routes.isNotEmpty())
    }

    @Test
    fun onTerminalClick_selectsAndDeselectsTerminal() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val purabaya = viewModel.uiState.value.terminals.first { it.id == "term_purabaya" }

        viewModel.onTerminalClick(purabaya)
        assertEquals("term_purabaya", viewModel.uiState.value.selectedTerminal?.id)
        assertNotNull(viewModel.uiState.value.selectedRoute)

        viewModel.onTerminalClick(purabaya)
        assertNull(viewModel.uiState.value.selectedTerminal)
        assertNull(viewModel.uiState.value.selectedRoute)
    }

    @Test
    fun clearSelectedTerminal_resetsSelection() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val purabaya = viewModel.uiState.value.terminals.first { it.id == "term_purabaya" }
        viewModel.onTerminalClick(purabaya)
        assertNotNull(viewModel.uiState.value.selectedTerminal)

        viewModel.clearSelectedTerminal()
        assertNull(viewModel.uiState.value.selectedTerminal)
        assertNull(viewModel.uiState.value.selectedRoute)
    }

    @Test
    fun bottomSheets_openAndDismissCorrectly() = runTest {
        assertEquals(SimulationBottomSheetType.NONE, viewModel.uiState.value.activeBottomSheet)

        viewModel.openBusesBottomSheet()
        assertEquals(SimulationBottomSheetType.BUSES, viewModel.uiState.value.activeBottomSheet)

        viewModel.openRoutesBottomSheet()
        assertEquals(SimulationBottomSheetType.ROUTES, viewModel.uiState.value.activeBottomSheet)

        viewModel.dismissBottomSheet()
        assertEquals(SimulationBottomSheetType.NONE, viewModel.uiState.value.activeBottomSheet)
    }

    @Test
    fun selectRoute_setsRouteAndOriginAndClosesSheet() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val route = viewModel.uiState.value.routes.first()
        viewModel.openRoutesBottomSheet()
        assertEquals(SimulationBottomSheetType.ROUTES, viewModel.uiState.value.activeBottomSheet)

        viewModel.selectRoute(route)

        assertEquals(route.id, viewModel.uiState.value.selectedRoute?.id)
        assertEquals(route.originTerminal.id, viewModel.uiState.value.selectedTerminal?.id)
        assertEquals(SimulationBottomSheetType.NONE, viewModel.uiState.value.activeBottomSheet)
    }

    @Test
    fun togglePlay_togglesSimulationPlayState() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isPlaying)

        viewModel.togglePlay()
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isPlaying)

        viewModel.togglePlay()
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isPlaying)
    }

    @Test
    fun simulationTimeUpdate_updatesUiStateAndFormatting() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        simulationTimeRepo.advanceMinutes(75)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(9, state.currentTime.hour)
        assertEquals(15, state.currentTime.minute)
        assertEquals("09:15 WIB", state.formattedTime)
    }

    @Test
    fun companyAndFleetUpdate_updatesUiState() = runTest {
        fakeCompanyRepo.emitCompany(
            Company(
                id = "comp-1",
                name = "PO Sumber Kencono",
                balance = Money.rupiah(500_000_000)
            )
        )
        fakeFleetRepo.emitBuses(listOf(createDummyBus("TN-01")))

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("PO Sumber Kencono", state.companyName)
        assertEquals(Money.rupiah(500_000_000).formatRupiah(), state.balanceFormatted)
        assertEquals(1, state.ownedBusCount)
    }

    private fun createDummyBus(busCode: String): Bus {
        val dummyChassis = Chassis(
            id = "chassis-hino-rk8",
            modelName = "RK8 R260",
            brand = ChassisBrand.HINO,
            engineLocation = EngineLocation.REAR,
            suspension = SuspensionType.LEAF_SPRING,
            axleType = AxleType.SINGLE_4X2,
            transmission = TransmissionType.MANUAL,
            horsepower = 260,
            torqueNm = 750,
            fuelCapacityLiters = 270,
            fuelConsumptionKmPerLiter = 3.2f,
            price = Money.rupiah(750_000_000)
        )
        val dummyBody = KaroseriBody(
            id = "body-jb5-shd",
            modelName = "Jetbus 5 SHD",
            brand = KaroseriBrand.ADIPUTRO,
            bodyType = BusBodyType.SHD,
            busClass = BusClass.EKSEKUTIF,
            passengerCapacity = 32,
            luggageCapacityKg = 2500,
            comfortRating = 88,
            price = Money.rupiah(650_000_000)
        )
        return Bus(
            id = "bus-1",
            busCode = busCode,
            chassis = dummyChassis,
            body = dummyBody
        )
    }

    private class FakeCompanyRepository : CompanyRepository {
        private val state = MutableStateFlow<Company?>(null)

        fun emitCompany(company: Company) {
            state.value = company
        }

        override fun getCompany(): Flow<Company?> = state

        override suspend fun createCompany(name: String, initialBalance: Money): Company {
            val created = Company(id = "comp-1", name = name, balance = initialBalance)
            state.value = created
            return created
        }

        override suspend fun updateBalance(newBalance: Money) {
            state.value = state.value?.copy(balance = newBalance)
        }

        override suspend fun deductBalance(amount: Money): Boolean = true

        override suspend fun addBalance(amount: Money) {}
    }

    private class FakeFleetRepository : FleetRepository {
        private val state = MutableStateFlow<List<Bus>>(emptyList())

        fun emitBuses(buses: List<Bus>) {
            state.value = buses
        }

        override fun getOwnedBuses(): Flow<List<Bus>> = state

        override suspend fun getBusById(id: String): Bus? = null

        override suspend fun addBus(bus: Bus) {
            state.value = state.value + bus
        }

        override suspend fun updateBus(bus: Bus) {}

        override suspend fun removeBus(busId: String) {}
    }
}
