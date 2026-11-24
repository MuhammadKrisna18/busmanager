package com.example.busmanager.presentation.route

import com.example.busmanager.data.local.datasource.ChassisDataSource
import com.example.busmanager.data.local.datasource.KaroseriBodyDataSource
import com.example.busmanager.data.local.datasource.RouteDataSource
import com.example.busmanager.data.local.datasource.TerminalDataSource
import com.example.busmanager.data.repository.RouteCatalogRepositoryImpl
import com.example.busmanager.data.repository.TerminalCatalogRepositoryImpl
import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.fleet.repository.FleetRepository
import com.example.busmanager.domain.fleet.usecase.GetOwnedFleetUseCase
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.RoutePathType
import com.example.busmanager.domain.route.model.RoutePricing
import com.example.busmanager.domain.route.model.RouteSchedule
import com.example.busmanager.domain.route.repository.RoutePricingRepository
import com.example.busmanager.domain.route.repository.RouteScheduleRepository
import com.example.busmanager.domain.route.usecase.AddRouteScheduleUseCase
import com.example.busmanager.domain.route.usecase.AssignBusToScheduleUseCase
import com.example.busmanager.domain.route.usecase.CalculateRouteCostUseCase
import com.example.busmanager.domain.route.usecase.DeleteRouteScheduleUseCase
import com.example.busmanager.domain.route.usecase.GetAvailableRoutesUseCase
import com.example.busmanager.domain.route.usecase.GetAvailableTerminalsUseCase
import com.example.busmanager.domain.route.usecase.GetRoutePricingsUseCase
import com.example.busmanager.domain.route.usecase.GetRouteSchedulesUseCase
import com.example.busmanager.domain.route.usecase.GetRouteTransitStopsUseCase
import com.example.busmanager.domain.route.usecase.InitializeDefaultRoutePricingsUseCase
import com.example.busmanager.domain.route.usecase.SaveRoutePricingUseCase
import com.example.busmanager.domain.simulation.model.RouteDirection
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
                busCode = "TN-01",
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

    private lateinit var fakePricingRepo: FakeRoutePricingRepository
    private lateinit var fakeScheduleRepo: FakeRouteScheduleRepository

    private lateinit var viewModel: RoutePlannerViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakePricingRepo = FakeRoutePricingRepository()
        fakeScheduleRepo = FakeRouteScheduleRepository()

        val getRoutePricingsUseCase = GetRoutePricingsUseCase(fakePricingRepo)
        val saveRoutePricingUseCase = SaveRoutePricingUseCase(fakePricingRepo)
        val initializeDefaultRoutePricingsUseCase = InitializeDefaultRoutePricingsUseCase(fakePricingRepo)
        val getRouteSchedulesUseCase = GetRouteSchedulesUseCase(fakeScheduleRepo)
        val addRouteScheduleUseCase = AddRouteScheduleUseCase(fakeScheduleRepo)
        val assignBusToScheduleUseCase = AssignBusToScheduleUseCase(fakeScheduleRepo)
        val deleteRouteScheduleUseCase = DeleteRouteScheduleUseCase(fakeScheduleRepo)

        viewModel = RoutePlannerViewModel(
            getAvailableTerminalsUseCase = getAvailableTerminalsUseCase,
            getAvailableRoutesUseCase = getAvailableRoutesUseCase,
            getRouteTransitStopsUseCase = getRouteTransitStopsUseCase,
            calculateRouteCostUseCase = calculateRouteCostUseCase,
            getOwnedFleetUseCase = getOwnedFleetUseCase,
            routeRepository = routeRepository,
            getRoutePricingsUseCase = getRoutePricingsUseCase,
            saveRoutePricingUseCase = saveRoutePricingUseCase,
            initializeDefaultRoutePricingsUseCase = initializeDefaultRoutePricingsUseCase,
            getRouteSchedulesUseCase = getRouteSchedulesUseCase,
            addRouteScheduleUseCase = addRouteScheduleUseCase,
            assignBusToScheduleUseCase = assignBusToScheduleUseCase,
            deleteRouteScheduleUseCase = deleteRouteScheduleUseCase
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

        assertEquals(10, state.terminals.size)
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

    @Test
    fun selectTab_shouldChangeActiveTab() = runTest {
        advanceUntilIdle()
        assertEquals(RouteDetailTab.OVERVIEW, viewModel.uiState.value.activeTab)

        viewModel.selectTab(RouteDetailTab.PRICING)
        assertEquals(RouteDetailTab.PRICING, viewModel.uiState.value.activeTab)

        viewModel.selectTab(RouteDetailTab.SCHEDULES)
        assertEquals(RouteDetailTab.SCHEDULES, viewModel.uiState.value.activeTab)
    }

    @Test
    fun openAndSavePricing_shouldUpdatePriceAndShowSuccessMessage() = runTest {
        advanceUntilIdle()
        val route = viewModel.uiState.value.selectedRoute!!
        val samplePricing = RoutePricing(
            id = "price-1",
            routeId = route.id,
            originTerminalId = "term_purabaya",
            destinationTerminalId = "term_sritanjung",
            originCity = "Surabaya",
            destinationCity = "Banyuwangi",
            price = Money.rupiah(150_000L)
        )
        fakePricingRepo.savePricing(samplePricing)
        advanceUntilIdle()

        viewModel.openEditPricing(samplePricing)
        assertEquals(samplePricing, viewModel.uiState.value.editingPricing)

        viewModel.savePricing("price-1", 175_000L)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.editingPricing)
        assertTrue(viewModel.uiState.value.successMessage!!.contains("175.000"))
    }

    @Test
    fun addSchedule_shouldSucceedWhenNoConflict() = runTest {
        advanceUntilIdle()
        viewModel.openAddScheduleDialog()
        assertTrue(viewModel.uiState.value.isAddScheduleDialogOpen)

        viewModel.addSchedule(7, 0, RouteDirection.ORIGIN_TO_DESTINATION)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isAddScheduleDialogOpen)
        assertNotNull(viewModel.uiState.value.successMessage)
        assertEquals(1, viewModel.uiState.value.schedules.size)
        assertEquals("07:00 WIB", viewModel.uiState.value.schedules.first().formattedTime)
    }

    @Test
    fun addSchedule_shouldFailAndShowErrorWhenConflictExists() = runTest {
        advanceUntilIdle()
        viewModel.addSchedule(7, 0, RouteDirection.ORIGIN_TO_DESTINATION)
        advanceUntilIdle()

        viewModel.openAddScheduleDialog()
        viewModel.addSchedule(7, 0, RouteDirection.ORIGIN_TO_DESTINATION)
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.errorMessage)
        assertEquals(1, viewModel.uiState.value.schedules.size)
    }

    @Test
    fun deleteSchedule_shouldRemoveSchedule() = runTest {
        advanceUntilIdle()
        viewModel.addSchedule(8, 30, RouteDirection.ORIGIN_TO_DESTINATION)
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.schedules.size)

        val schedId = viewModel.uiState.value.schedules.first().id
        viewModel.deleteSchedule(schedId)
        advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.schedules.size)
        assertNotNull(viewModel.uiState.value.successMessage)
    }

    @Test
    fun assignBusToSchedule_shouldUpdateScheduleAndShowSuccess() = runTest {
        advanceUntilIdle()
        viewModel.addSchedule(9, 0, RouteDirection.ORIGIN_TO_DESTINATION)
        advanceUntilIdle()

        val schedule = viewModel.uiState.value.schedules.first()
        viewModel.openAssignBusDialog(schedule)
        assertEquals(schedule, viewModel.uiState.value.selectedScheduleForBusAssignment)

        viewModel.assignBusToSchedule(schedule.id, "bus-1")
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.selectedScheduleForBusAssignment)
        assertNotNull(viewModel.uiState.value.successMessage)
        assertEquals("bus-1", viewModel.uiState.value.schedules.first().assignedBusId)
    }

    private class FakeRoutePricingRepository : RoutePricingRepository {
        private val list = mutableListOf<RoutePricing>()
        private val flow = MutableStateFlow<List<RoutePricing>>(emptyList())

        override fun getPricingsForRoute(routeId: String): Flow<List<RoutePricing>> = flow

        override suspend fun getPricingsListForRoute(routeId: String): List<RoutePricing> = list

        override fun getAllPricingsFlow(): Flow<List<RoutePricing>> = flow

        override suspend fun getAllPricings(): List<RoutePricing> = list.toList()

        override suspend fun savePricing(pricing: RoutePricing) {
            val idx = list.indexOfFirst { it.id == pricing.id }
            if (idx >= 0) {
                list[idx] = pricing
            } else {
                list.add(pricing)
            }
            flow.value = list.toList()
        }

        override suspend fun savePricings(pricings: List<RoutePricing>) {
            list.addAll(pricings)
            flow.value = list.toList()
        }

        override suspend fun getPricingBetween(
            routeId: String,
            originTerminalId: String,
            destinationTerminalId: String
        ): RoutePricing? = list.find { it.routeId == routeId && it.originTerminalId == originTerminalId && it.destinationTerminalId == destinationTerminalId }

        override suspend fun generateAndSaveDefaultPricings(route: BusRoute): List<RoutePricing> = emptyList()
    }

    private class FakeRouteScheduleRepository : RouteScheduleRepository {
        private val list = mutableListOf<RouteSchedule>()
        private val flow = MutableStateFlow<List<RouteSchedule>>(emptyList())

        override fun getSchedulesForRoute(routeId: String): Flow<List<RouteSchedule>> = flow

        override fun getAllSchedules(): Flow<List<RouteSchedule>> = flow

        override suspend fun getSchedulesListForRoute(routeId: String): List<RouteSchedule> = list

        override suspend fun getScheduleById(id: String): RouteSchedule? = list.find { it.id == id }

        override suspend fun addSchedule(schedule: RouteSchedule): Result<Unit> {
            val conflict = checkConflict(schedule.routeId, schedule.direction, schedule.departureHour, schedule.departureMinute)
            if (conflict) {
                return Result.failure(IllegalStateException("Jadwal bentrok pada ${schedule.formattedTime}"))
            }
            list.add(schedule)
            flow.value = list.toList()
            return Result.success(Unit)
        }

        override suspend fun updateSchedule(schedule: RouteSchedule): Result<Unit> {
            val idx = list.indexOfFirst { it.id == schedule.id }
            if (idx >= 0) {
                list[idx] = schedule
                flow.value = list.toList()
            }
            return Result.success(Unit)
        }

        override suspend fun assignBusToSchedule(scheduleId: String, busId: String): Boolean {
            val idx = list.indexOfFirst { it.id == scheduleId }
            if (idx >= 0) {
                list[idx] = list[idx].copy(assignedBusId = busId)
                flow.value = list.toList()
                return true
            }
            return false
        }

        override suspend fun unassignBusFromSchedule(scheduleId: String) {
            val idx = list.indexOfFirst { it.id == scheduleId }
            if (idx >= 0) {
                list[idx] = list[idx].copy(assignedBusId = null)
                flow.value = list.toList()
            }
        }

        override suspend fun deleteSchedule(scheduleId: String) {
            list.removeAll { it.id == scheduleId }
            flow.value = list.toList()
        }

        override suspend fun checkConflict(
            routeId: String,
            direction: RouteDirection,
            hour: Int,
            minute: Int,
            excludeScheduleId: String?
        ): Boolean {
            return list.any {
                it.id != excludeScheduleId &&
                        it.routeId == routeId &&
                        it.direction == direction &&
                        it.departureHour == hour &&
                        it.departureMinute == minute
            }
        }
    }
}
