package com.example.busmanager.presentation.route

import com.example.busmanager.data.local.datasource.ChassisDataSource
import com.example.busmanager.data.local.datasource.KaroseriBodyDataSource
import com.example.busmanager.data.local.datasource.RouteDataSource
import com.example.busmanager.data.local.datasource.TerminalDataSource
import com.example.busmanager.data.repository.RouteCatalogRepositoryImpl
import com.example.busmanager.data.repository.TerminalCatalogRepositoryImpl
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.fleet.repository.FleetRepository
import com.example.busmanager.domain.fleet.usecase.GetOwnedFleetUseCase
import com.example.busmanager.domain.route.model.RoutePathType
import com.example.busmanager.domain.route.usecase.CalculateRouteCostUseCase
import com.example.busmanager.domain.route.usecase.GetAvailableRoutesUseCase
import com.example.busmanager.domain.route.usecase.GetAvailableTerminalsUseCase
import com.example.busmanager.domain.route.usecase.GetRouteTransitStopsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RoutePlannerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val terminalDataSource = TerminalDataSource()
    private val routeDataSource = RouteDataSource(terminalDataSource)

    private val terminalRepository = TerminalCatalogRepositoryImpl(terminalDataSource)
    private val routeRepository = RouteCatalogRepositoryImpl(routeDataSource)

    private val getAvailableTerminalsUseCase = GetAvailableTerminalsUseCase(terminalRepository)
    private val getAvailableRoutesUseCase = GetAvailableRoutesUseCase(routeRepository)
    private val getRouteTransitStopsUseCase = GetRouteTransitStopsUseCase(routeRepository)
    private val calculateRouteCostUseCase = CalculateRouteCostUseCase()

    private val mockFleetRepository = object : FleetRepository {
        override fun getOwnedBuses(): Flow<List<Bus>> {
            val chassis = ChassisDataSource().getChassisList().first()
            val body = KaroseriBodyDataSource().getKaroseriBodyList().first()
            val bus = Bus(
                id = "bus-1",
                plateNumber = "L 1234 AB",
                nickname = "Semeru",
                chassis = chassis,
                body = body
            )
            return flowOf(listOf(bus))
        }

        override suspend fun getBusById(id: String): Bus? = null
        override suspend fun addBus(bus: Bus) {}
        override suspend fun updateBus(bus: Bus) {}
        override suspend fun removeBus(busId: String) {}
    }

    private val getOwnedFleetUseCase = GetOwnedFleetUseCase(mockFleetRepository)

    private lateinit var viewModel: RoutePlannerViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = RoutePlannerViewModel(
            getAvailableTerminalsUseCase = getAvailableTerminalsUseCase,
            getAvailableRoutesUseCase = getAvailableRoutesUseCase,
            getRouteTransitStopsUseCase = getRouteTransitStopsUseCase,
            calculateRouteCostUseCase = calculateRouteCostUseCase,
            getOwnedFleetUseCase = getOwnedFleetUseCase,
            routeRepository = routeRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_shouldLoadTerminalsAndSelectDefaults() = runTest {
        advanceUntilIdle()
        val state = viewModel.uiState.value

        assertEquals(6, state.terminals.size)
        assertEquals("term_purabaya", state.selectedOrigin?.id)
        assertEquals("term_sritanjung", state.selectedDestination?.id)
        assertTrue(state.availableRoutes.isNotEmpty())
        assertNotNull(state.selectedRoute)
    }

    @Test
    fun selectOriginAndDestination_shouldUpdateAvailableRoutes() = runTest {
        advanceUntilIdle()
        val malang = viewModel.uiState.value.terminals.first { it.id == "term_arjosari" }
        viewModel.selectDestination(malang)

        val state = viewModel.uiState.value
        assertEquals("term_arjosari", state.selectedDestination?.id)
        assertEquals(2, state.availableRoutes.size)
    }

    @Test
    fun swapOriginDestination_shouldInvertEndpoints() = runTest {
        advanceUntilIdle()
        val originBefore = viewModel.uiState.value.selectedOrigin
        val destBefore = viewModel.uiState.value.selectedDestination

        viewModel.swapOriginDestination()

        val state = viewModel.uiState.value
        assertEquals(destBefore?.id, state.selectedOrigin?.id)
        assertEquals(originBefore?.id, state.selectedDestination?.id)
    }

    @Test
    fun setPreferredPathType_toll_shouldFilterToTollRoutesOnly() = runTest {
        advanceUntilIdle()
        val malang = viewModel.uiState.value.terminals.first { it.id == "term_arjosari" }
        viewModel.selectDestination(malang)

        viewModel.setPreferredPathType(RoutePathType.TOLL)

        val state = viewModel.uiState.value
        assertEquals(1, state.availableRoutes.size)
        assertEquals(RoutePathType.TOLL, state.availableRoutes.first().pathType)
    }

    @Test
    fun onTerminalMapClick_shouldUpdateDestination() = runTest {
        advanceUntilIdle()
        val probolinggo = viewModel.uiState.value.terminals.first { it.id == "term_bayuangga" }

        viewModel.onTerminalMapClick(probolinggo)

        val state = viewModel.uiState.value
        assertEquals("term_bayuangga", state.selectedDestination?.id)
    }
}
