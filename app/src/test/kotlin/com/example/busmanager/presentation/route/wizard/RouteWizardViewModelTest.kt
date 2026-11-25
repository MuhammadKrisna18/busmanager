package com.example.busmanager.presentation.route.wizard

import com.example.busmanager.data.local.datasource.ChassisDataSource
import com.example.busmanager.data.local.datasource.KaroseriBodyDataSource
import com.example.busmanager.data.local.datasource.RouteDataSource
import com.example.busmanager.data.local.datasource.TerminalDataSource
import com.example.busmanager.data.repository.RouteCatalogRepositoryImpl
import com.example.busmanager.data.repository.TerminalCatalogRepositoryImpl
import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.driver.model.Driver
import com.example.busmanager.domain.driver.repository.DriverRepository
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.fleet.repository.FleetRepository
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.RoutePricing
import com.example.busmanager.domain.route.model.RouteSchedule
import com.example.busmanager.domain.route.repository.RoutePricingRepository
import com.example.busmanager.domain.route.repository.RouteScheduleRepository
import com.example.busmanager.domain.route.usecase.BuildCustomRouteUseCase
import com.example.busmanager.domain.route.usecase.FindAvailablePathsUseCase
import com.example.busmanager.domain.route.usecase.SaveCustomRouteUseCase
import com.example.busmanager.domain.simulation.model.RouteDirection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
class RouteWizardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val terminalDataSource = TerminalDataSource()
    private val routeDataSource = RouteDataSource(terminalDataSource)

    private val terminalRepository = TerminalCatalogRepositoryImpl(terminalDataSource)
    private val routeRepository = RouteCatalogRepositoryImpl(routeDataSource)

    private val findAvailablePathsUseCase = FindAvailablePathsUseCase(terminalRepository, routeRepository)
    private val buildCustomRouteUseCase = BuildCustomRouteUseCase(routeRepository)
    private val saveCustomRouteUseCase = SaveCustomRouteUseCase(routeRepository)

    private lateinit var fakePricingRepo: FakeRoutePricingRepository
    private lateinit var fakeScheduleRepo: FakeRouteScheduleRepository
    private lateinit var fakeDriverRepo: FakeDriverRepository
    private lateinit var fakeFleetRepo: FakeFleetRepository

    private lateinit var viewModel: RouteWizardViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        fakePricingRepo = FakeRoutePricingRepository()
        fakeScheduleRepo = FakeRouteScheduleRepository()
        fakeDriverRepo = FakeDriverRepository()
        fakeFleetRepo = FakeFleetRepository()

        val sampleBus = Bus(
            id = "bus-test-1",
            busCode = "TN-01",
            chassis = ChassisDataSource().getChassisList().first(),
            body = KaroseriBodyDataSource().getKaroseriBodyList().first()
        )
        fakeFleetRepo.setBuses(listOf(sampleBus))

        val sampleDriver = Driver(
            id = "driver-test-1",
            name = "Budi Hartono",
            wagePerDay = Money.rupiah(200_000L),
            isHired = true
        )
        fakeDriverRepo.setDrivers(listOf(sampleDriver))

        viewModel = RouteWizardViewModel(
            terminalRepository = terminalRepository,
            routeRepository = routeRepository,
            findAvailablePathsUseCase = findAvailablePathsUseCase,
            buildCustomRouteUseCase = buildCustomRouteUseCase,
            saveCustomRouteUseCase = saveCustomRouteUseCase,
            routePricingRepository = fakePricingRepo,
            routeScheduleRepository = fakeScheduleRepo,
            driverRepository = fakeDriverRepo,
            fleetRepository = fakeFleetRepo
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_loadsTerminals_and_startsAtStep1() = runTest {
        advanceUntilIdle()
        val state = viewModel.uiState.value

        assertEquals(RouteWizardStep.ORIGIN_DESTINATION, state.currentStep)
        assertTrue(state.allTerminals.size >= 6)
        assertNotNull(state.selectedBus)
        assertNotNull(state.selectedDriver)
        assertFalse(state.canProceedFromStep1)
    }

    @Test
    fun selectOriginAndDestination_calculatesPaths_and_buildsRoute() = runTest {
        advanceUntilIdle()
        val purabaya = terminalRepository.getTerminalById("term_purabaya")!!
        val sritanjung = terminalRepository.getTerminalById("term_sritanjung")!!

        viewModel.selectOrigin(purabaya)
        viewModel.selectDestination(sritanjung)

        val state = viewModel.uiState.value
        assertEquals(purabaya, state.selectedOrigin)
        assertEquals(sritanjung, state.selectedDestination)
        assertTrue(state.availablePaths.size >= 2)
        assertNotNull(state.builtRoute)
        assertTrue(state.pricings.isNotEmpty())
        assertTrue(state.canProceedFromStep1)
        assertTrue(state.canProceedFromStep2)
    }

    @Test
    fun stepTransitions_workCorrectly() = runTest {
        advanceUntilIdle()
        val purabaya = terminalRepository.getTerminalById("term_purabaya")!!
        val arjosari = terminalRepository.getTerminalById("term_arjosari")!!

        viewModel.selectOrigin(purabaya)
        viewModel.selectDestination(arjosari)

        assertEquals(RouteWizardStep.ORIGIN_DESTINATION, viewModel.uiState.value.currentStep)

        viewModel.nextStep()
        assertEquals(RouteWizardStep.PATH_AND_STOPS, viewModel.uiState.value.currentStep)

        viewModel.nextStep()
        assertEquals(RouteWizardStep.PRICING_MATRIX, viewModel.uiState.value.currentStep)

        viewModel.nextStep()
        assertEquals(RouteWizardStep.BUS_DRIVER_SCHEDULE, viewModel.uiState.value.currentStep)

        viewModel.previousStep()
        assertEquals(RouteWizardStep.PRICING_MATRIX, viewModel.uiState.value.currentStep)

        viewModel.goToStep(RouteWizardStep.ORIGIN_DESTINATION)
        assertEquals(RouteWizardStep.ORIGIN_DESTINATION, viewModel.uiState.value.currentStep)
    }

    @Test
    fun nextStep_blockedWhenStep1Invalid() = runTest {
        advanceUntilIdle()
        viewModel.nextStep()
        assertEquals(RouteWizardStep.ORIGIN_DESTINATION, viewModel.uiState.value.currentStep)
        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun toggleTransitStop_updatesBuiltRouteTransitTerminals() = runTest {
        advanceUntilIdle()
        val purabaya = terminalRepository.getTerminalById("term_purabaya")!!
        val sritanjung = terminalRepository.getTerminalById("term_sritanjung")!!

        viewModel.selectOrigin(purabaya)
        viewModel.selectDestination(sritanjung)

        val initialTransit = viewModel.uiState.value.builtRoute?.transitTerminals?.map { it.id } ?: emptyList()
        assertTrue(initialTransit.isNotEmpty())

        val firstTransitId = initialTransit.first()
        viewModel.toggleTransitStop(firstTransitId)

        val updatedTransit = viewModel.uiState.value.builtRoute?.transitTerminals?.map { it.id } ?: emptyList()
        assertFalse(updatedTransit.contains(firstTransitId))
    }

    @Test
    fun saveEditedPrice_updatesTicketPriceForBothDirections() = runTest {
        advanceUntilIdle()
        val purabaya = terminalRepository.getTerminalById("term_purabaya")!!
        val arjosari = terminalRepository.getTerminalById("term_arjosari")!!

        viewModel.selectOrigin(purabaya)
        viewModel.selectDestination(arjosari)

        val targetPricing = viewModel.uiState.value.pricings.first()
        viewModel.saveEditedPrice(targetPricing, 75_000L)

        val updatedForward = viewModel.uiState.value.pricings.find { it.id == targetPricing.id }
        assertNotNull(updatedForward)
        assertEquals(Money.rupiah(75_000L), updatedForward?.price)

        val updatedReverse = viewModel.uiState.value.pricings.find {
            it.originTerminalId == targetPricing.destinationTerminalId && it.destinationTerminalId == targetPricing.originTerminalId
        }
        assertNotNull(updatedReverse)
        assertEquals(Money.rupiah(75_000L), updatedReverse?.price)
    }

    @Test
    fun roundTrip_calculatesArrivalTime_and_minimumReturnTimeCorrectly() = runTest {
        advanceUntilIdle()
        val purabaya = terminalRepository.getTerminalById("term_purabaya")!!
        val arjosari = terminalRepository.getTerminalById("term_arjosari")!!

        viewModel.selectOrigin(purabaya)
        viewModel.selectDestination(arjosari)
        viewModel.setDepartureTime(6, 0)

        val state = viewModel.uiState.value
        val duration = state.estimatedDurationMinutes
        assertTrue(duration > 0)

        val expectedArrivalTotal = 360 + duration
        val expectedArrivalHour = (expectedArrivalTotal / 60) % 24
        val expectedArrivalMinute = expectedArrivalTotal % 60
        val rawMinReturn = expectedArrivalTotal + 30
        val minOfDay = rawMinReturn % 1440
        val expectedMinReturnTotal = when {
            minOfDay < 4 * 60 -> (rawMinReturn / 1440) * 1440 + 4 * 60
            else -> rawMinReturn
        }
        val expectedMinReturnHour = (expectedMinReturnTotal / 60) % 24
        val expectedMinReturnMinute = expectedMinReturnTotal % 60

        assertEquals(expectedArrivalHour, state.arrivalHour)
        assertEquals(expectedArrivalMinute, state.arrivalMinute)
        assertEquals(expectedMinReturnHour, state.minReturnHour)
        assertEquals(expectedMinReturnMinute, state.minReturnMinute)
        assertFalse(state.hasLayoverConflict)
    }

    @Test
    fun roundTrip_flagsLayoverConflict_whenReturnTimeLessThanThirtyMinutesAfterArrival() = runTest {
        advanceUntilIdle()
        val purabaya = terminalRepository.getTerminalById("term_purabaya")!!
        val arjosari = terminalRepository.getTerminalById("term_arjosari")!!

        viewModel.selectOrigin(purabaya)
        viewModel.selectDestination(arjosari)
        viewModel.setDepartureTime(6, 0)

        val arrivalH = viewModel.uiState.value.arrivalHour
        val arrivalM = viewModel.uiState.value.arrivalMinute
        viewModel.setReturnTime(arrivalH, arrivalM)

        val state = viewModel.uiState.value
        assertTrue(state.hasLayoverConflict)
        assertTrue(state.hasConflict)
        assertFalse(state.canLaunchFromStep4)
    }

    @Test
    fun roundTrip_clearsLayoverConflict_whenReturnTimeAtLeastThirtyMinutesAfterArrival() = runTest {
        advanceUntilIdle()
        val purabaya = terminalRepository.getTerminalById("term_purabaya")!!
        val arjosari = terminalRepository.getTerminalById("term_arjosari")!!

        viewModel.selectOrigin(purabaya)
        viewModel.selectDestination(arjosari)
        viewModel.setDepartureTime(6, 0)

        val minH = viewModel.uiState.value.minReturnHour
        val minM = viewModel.uiState.value.minReturnMinute
        viewModel.setReturnTime(minH, minM)

        val state = viewModel.uiState.value
        assertFalse(state.hasLayoverConflict)
        assertFalse(state.hasConflict)
        assertTrue(state.canLaunchFromStep4)
    }

    @Test
    fun resetReturnTimeToRecommendation_resetsToEarliestAllowedTime() = runTest {
        advanceUntilIdle()
        val purabaya = terminalRepository.getTerminalById("term_purabaya")!!
        val arjosari = terminalRepository.getTerminalById("term_arjosari")!!

        viewModel.selectOrigin(purabaya)
        viewModel.selectDestination(arjosari)
        viewModel.setDepartureTime(6, 0)

        viewModel.setReturnTime(6, 30)
        assertTrue(viewModel.uiState.value.hasLayoverConflict)

        viewModel.resetReturnTimeToRecommendation()
        val state = viewModel.uiState.value
        assertEquals(state.minReturnHour, state.returnHour)
        assertEquals(state.minReturnMinute, state.returnMinute)
        assertFalse(state.hasLayoverConflict)
    }

    @Test
    fun oneWay_doesNotCheckLayoverOrReturnConflict() = runTest {
        advanceUntilIdle()
        val purabaya = terminalRepository.getTerminalById("term_purabaya")!!
        val arjosari = terminalRepository.getTerminalById("term_arjosari")!!

        viewModel.selectOrigin(purabaya)
        viewModel.selectDestination(arjosari)
        viewModel.setDepartureTime(6, 0)
        viewModel.setReturnTime(6, 10)

        viewModel.setIsRoundTrip(false)
        val state = viewModel.uiState.value
        assertFalse(state.isRoundTrip)
        assertFalse(state.hasConflict)
        assertTrue(state.canLaunchFromStep4)
    }

    @Test
    fun launchAndSaveRoute_withRoundTrip_createsTwoSchedulesWithAssignedBus() = runTest {
        advanceUntilIdle()
        val purabaya = terminalRepository.getTerminalById("term_purabaya")!!
        val arjosari = terminalRepository.getTerminalById("term_arjosari")!!

        viewModel.selectOrigin(purabaya)
        viewModel.selectDestination(arjosari)
        viewModel.setDepartureTime(8, 30)

        var launchedId: String? = null
        viewModel.launchAndSaveRoute { id ->
            launchedId = id
        }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.launchSuccess)
        assertNotNull(launchedId)
        val finalRouteId = requireNotNull(launchedId)
        assertEquals(state.builtRoute?.id, finalRouteId)

        val savedRoute = routeRepository.getRouteById(finalRouteId)
        assertNotNull(savedRoute)

        val savedPricings = fakePricingRepo.getPricingsListForRoute(finalRouteId)
        assertTrue(savedPricings.isNotEmpty())

        val savedSchedules = fakeScheduleRepo.getSchedulesListForRoute(finalRouteId)
        assertEquals(2, savedSchedules.size)

        val outboundSched = savedSchedules.find { it.direction == RouteDirection.ORIGIN_TO_DESTINATION }
        assertNotNull(outboundSched)
        assertEquals(8, outboundSched?.departureHour)
        assertEquals(30, outboundSched?.departureMinute)
        assertEquals("bus-test-1", outboundSched?.assignedBusId)

        val returnSched = savedSchedules.find { it.direction == RouteDirection.DESTINATION_TO_ORIGIN }
        assertNotNull(returnSched)
        assertEquals(state.returnHour, returnSched?.departureHour)
        assertEquals(state.returnMinute, returnSched?.departureMinute)
        assertEquals("bus-test-1", returnSched?.assignedBusId)

        val assignedBus = fakeFleetRepo.getBusById("bus-test-1")
        assertEquals(launchedId, assignedBus?.routeId)
        assertEquals("driver-test-1", assignedBus?.driverId)
    }

    @Test
    fun launchAndSaveRoute_withOneWay_createsSingleSchedule() = runTest {
        advanceUntilIdle()
        val purabaya = terminalRepository.getTerminalById("term_purabaya")!!
        val arjosari = terminalRepository.getTerminalById("term_arjosari")!!

        viewModel.selectOrigin(purabaya)
        viewModel.selectDestination(arjosari)
        viewModel.setIsRoundTrip(false)
        viewModel.setDepartureTime(9, 0)

        var launchedId: String? = null
        viewModel.launchAndSaveRoute { id ->
            launchedId = id
        }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.launchSuccess)
        val finalRouteId = requireNotNull(launchedId)

        val savedSchedules = fakeScheduleRepo.getSchedulesListForRoute(finalRouteId)
        assertEquals(1, savedSchedules.size)
        assertEquals(9, savedSchedules.first().departureHour)
        assertEquals(0, savedSchedules.first().departureMinute)
    }

    @Test
    fun launchAndSaveRoute_blockedWhenLayoverConflictExists() = runTest {
        advanceUntilIdle()
        val purabaya = terminalRepository.getTerminalById("term_purabaya")!!
        val arjosari = terminalRepository.getTerminalById("term_arjosari")!!

        viewModel.selectOrigin(purabaya)
        viewModel.selectDestination(arjosari)
        viewModel.setDepartureTime(6, 0)

        val arrivalH = viewModel.uiState.value.arrivalHour
        val arrivalM = viewModel.uiState.value.arrivalMinute
        viewModel.setReturnTime(arrivalH, arrivalM)

        var launchedId: String? = null
        viewModel.launchAndSaveRoute { id ->
            launchedId = id
        }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.launchSuccess)
        assertNull(launchedId)
        assertTrue(fakeScheduleRepo.getAllList().isEmpty())
    }

    @Test
    fun driverAssignment_blocksSelectionOfDriverAssignedToAnotherBus() = runTest {
        advanceUntilIdle()
        val bus1 = Bus(
            id = "bus-1",
            busCode = "MK001",
            chassis = ChassisDataSource().getChassisList().first(),
            body = KaroseriBodyDataSource().getKaroseriBodyList().first(),
            driverId = "driver-1"
        )
        val bus2 = Bus(
            id = "bus-2",
            busCode = "MK002",
            chassis = ChassisDataSource().getChassisList().first(),
            body = KaroseriBodyDataSource().getKaroseriBodyList().first()
        )
        val busyDriver = Driver(
            id = "driver-1",
            name = "Pak Slamet",
            wagePerDay = Money.rupiah(200_000L),
            assignedBusId = "bus-1",
            isHired = true
        )
        val freeDriver = Driver(
            id = "driver-2",
            name = "Pak Budi",
            wagePerDay = Money.rupiah(200_000L),
            assignedBusId = null,
            isHired = true
        )

        fakeFleetRepo.setBuses(listOf(bus1, bus2))
        fakeDriverRepo.setDrivers(listOf(busyDriver, freeDriver))
        advanceUntilIdle()

        viewModel.selectBus(bus2)
        assertEquals("MK001", viewModel.uiState.value.driverAssignedBusCodes[busyDriver.id])
        assertNull(viewModel.uiState.value.driverAssignedBusCodes[freeDriver.id])

        assertEquals(freeDriver.id, viewModel.uiState.value.selectedDriver?.id)

        viewModel.selectDriver(busyDriver)
        assertEquals(freeDriver.id, viewModel.uiState.value.selectedDriver?.id)

        viewModel.selectDriver(freeDriver)
        assertEquals(freeDriver.id, viewModel.uiState.value.selectedDriver?.id)
        assertTrue(viewModel.uiState.value.isSelectedDriverAvailable)
    }

    @Test
    fun busAssignment_blocksSelectionOfBusWithRouteOrDriver() = runTest {
        advanceUntilIdle()
        val busWithRoute = Bus(
            id = "bus-route",
            busCode = "MK01",
            chassis = ChassisDataSource().getChassisList().first(),
            body = KaroseriBodyDataSource().getKaroseriBodyList().first(),
            routeId = "route-1"
        )
        val busWithDriver = Bus(
            id = "bus-driver",
            busCode = "MK02",
            chassis = ChassisDataSource().getChassisList().first(),
            body = KaroseriBodyDataSource().getKaroseriBodyList().first(),
            driverId = "driver-1"
        )
        val busOperating = Bus(
            id = "bus-operating",
            busCode = "MK03",
            chassis = ChassisDataSource().getChassisList().first(),
            body = KaroseriBodyDataSource().getKaroseriBodyList().first(),
            isOperating = true
        )
        val freeBus = Bus(
            id = "bus-free",
            busCode = "MK04",
            chassis = ChassisDataSource().getChassisList().first(),
            body = KaroseriBodyDataSource().getKaroseriBodyList().first()
        )

        fakeFleetRepo.setBuses(listOf(busWithRoute, busWithDriver, busOperating, freeBus))
        advanceUntilIdle()

        assertEquals(freeBus.id, viewModel.uiState.value.selectedBus?.id)
        assertTrue(viewModel.uiState.value.isSelectedBusAvailable)

        viewModel.selectBus(busWithRoute)
        assertEquals(freeBus.id, viewModel.uiState.value.selectedBus?.id)

        viewModel.selectBus(busWithDriver)
        assertEquals(freeBus.id, viewModel.uiState.value.selectedBus?.id)

        viewModel.selectBus(busOperating)
        assertEquals(freeBus.id, viewModel.uiState.value.selectedBus?.id)

        viewModel.selectBus(freeBus)
        assertEquals(freeBus.id, viewModel.uiState.value.selectedBus?.id)
    }

    @Test
    fun selectOrigin_allowsAnyTerminalRegardlessOfBusLocation() = runTest {
        advanceUntilIdle()
        val purabaya = terminalRepository.getTerminalById("term_purabaya")!!
        val arjosari = terminalRepository.getTerminalById("term_arjosari")!!
        val busAtArjosari = Bus(
            id = "bus-arjosari",
            busCode = "MK-ARJ",
            chassis = ChassisDataSource().getChassisList().first(),
            body = KaroseriBodyDataSource().getKaroseriBodyList().first(),
            currentTerminalId = arjosari.id
        )

        fakeFleetRepo.setBuses(listOf(busAtArjosari))
        advanceUntilIdle()

        viewModel.selectBus(busAtArjosari)
        viewModel.selectOrigin(purabaya)
        assertEquals(purabaya.id, viewModel.uiState.value.selectedOrigin?.id)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun saveRouteStandby_savesRouteAndPricingWithoutAssigningBusOrDriver() = runTest {
        advanceUntilIdle()
        val purabaya = terminalRepository.getTerminalById("term_purabaya")!!
        val arjosari = terminalRepository.getTerminalById("term_arjosari")!!

        viewModel.selectOrigin(purabaya)
        viewModel.selectDestination(arjosari)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.canProceedFromStep1)
        assertTrue(viewModel.uiState.value.canProceedFromStep2)
        assertTrue(viewModel.uiState.value.canProceedFromStep3)
        assertTrue(viewModel.uiState.value.canSaveStandby)

        var savedRouteId: String? = null
        viewModel.saveRouteStandby { id ->
            savedRouteId = id
        }
        advanceUntilIdle()

        assertNotNull(savedRouteId)
        assertTrue(viewModel.uiState.value.launchSuccess)
        assertTrue(viewModel.uiState.value.isStandbySaved)

        val persistedRoute = routeRepository.getRouteById(savedRouteId!!)
        assertNotNull(persistedRoute)
        assertEquals("term_purabaya", persistedRoute!!.originTerminal.id)
        assertEquals("term_arjosari", persistedRoute.destinationTerminal.id)

        val persistedPricings = fakePricingRepo.getAllPricings()
        assertTrue(persistedPricings.isNotEmpty())

        val persistedSchedules = fakeScheduleRepo.getAllList()
        assertTrue(persistedSchedules.isEmpty())

        val freeBus = fakeFleetRepo.getBusById("bus-test-1")
        assertNotNull(freeBus)
        assertNull(freeBus!!.routeId)
        assertNull(freeBus.driverId)

        val freeDriver = fakeDriverRepo.getDriverById("driver-test-1")
        assertNotNull(freeDriver)
        assertNull(freeDriver!!.assignedBusId)
    }

    private class FakeRoutePricingRepository : RoutePricingRepository {
        private val list = mutableListOf<RoutePricing>()
        private val flow = MutableStateFlow<List<RoutePricing>>(emptyList())

        override fun getPricingsForRoute(routeId: String): Flow<List<RoutePricing>> = flow
        override suspend fun getPricingsListForRoute(routeId: String): List<RoutePricing> = list.filter { it.routeId == routeId }
        override fun getAllPricingsFlow(): Flow<List<RoutePricing>> = flow
        override suspend fun getAllPricings(): List<RoutePricing> = list.toList()

        override suspend fun savePricing(pricing: RoutePricing) {
            val idx = list.indexOfFirst { it.id == pricing.id }
            if (idx >= 0) list[idx] = pricing else list.add(pricing)
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

        fun getAllList(): List<RouteSchedule> = list.toList()

        override fun getSchedulesForRoute(routeId: String): Flow<List<RouteSchedule>> = flow
        override fun getAllSchedules(): Flow<List<RouteSchedule>> = flow
        override suspend fun getSchedulesListForRoute(routeId: String): List<RouteSchedule> = list.filter { it.routeId == routeId }
        override suspend fun getScheduleById(id: String): RouteSchedule? = list.find { it.id == id }

        override suspend fun addSchedule(schedule: RouteSchedule): Result<Unit> {
            list.add(schedule)
            flow.value = list.toList()
            return Result.success(Unit)
        }

        override suspend fun updateSchedule(schedule: RouteSchedule): Result<Unit> = Result.success(Unit)

        override suspend fun assignBusToSchedule(scheduleId: String, busId: String): Boolean {
            val idx = list.indexOfFirst { it.id == scheduleId }
            if (idx >= 0) {
                list[idx] = list[idx].copy(assignedBusId = busId)
                flow.value = list.toList()
                return true
            }
            return false
        }

        override suspend fun unassignBusFromSchedule(scheduleId: String) {}
        override suspend fun deleteSchedule(scheduleId: String) {}

        override suspend fun checkConflict(
            routeId: String,
            direction: RouteDirection,
            hour: Int,
            minute: Int,
            excludeScheduleId: String?
        ): Boolean = false
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
        override suspend fun assignDriverToBus(driverId: String, busId: String): Boolean {
            val idx = list.indexOfFirst { it.id == driverId }
            if (idx >= 0) {
                list[idx] = list[idx].copy(assignedBusId = busId)
                flow.value = list.toList()
                return true
            }
            return false
        }
        override suspend fun unassignDriver(driverId: String) {}
        override suspend fun fireDriver(driverId: String) {}
        override suspend fun seedCandidateDriversIfEmpty() {}
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
}
