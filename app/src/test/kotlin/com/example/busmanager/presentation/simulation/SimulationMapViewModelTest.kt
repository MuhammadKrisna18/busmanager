package com.example.busmanager.presentation.simulation

import com.example.busmanager.data.local.datasource.RouteDataSource
import com.example.busmanager.data.local.datasource.TerminalDataSource
import com.example.busmanager.data.repository.RouteCatalogRepositoryImpl
import com.example.busmanager.data.repository.SimulationTimeRepositoryImpl
import com.example.busmanager.data.repository.SimulationTripRepositoryImpl
import com.example.busmanager.data.repository.TerminalCatalogRepositoryImpl
import com.example.busmanager.domain.company.model.Company
import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.company.repository.CompanyRepository
import com.example.busmanager.domain.company.usecase.GetCompanyUseCase
import com.example.busmanager.domain.driver.model.Driver
import com.example.busmanager.domain.driver.repository.DriverRepository
import com.example.busmanager.domain.finance.model.FinancialTransaction
import com.example.busmanager.domain.finance.model.TransactionCategory
import com.example.busmanager.domain.finance.model.TransactionType
import com.example.busmanager.domain.finance.repository.FinancialTransactionRepository
import com.example.busmanager.domain.finance.usecase.RecordFinancialTransactionUseCase
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
import com.example.busmanager.domain.fleet.usecase.ValidateBusOperationUseCase
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.RoutePricing
import com.example.busmanager.domain.route.model.RouteSchedule
import com.example.busmanager.domain.route.repository.RoutePricingRepository
import com.example.busmanager.domain.route.repository.RouteScheduleRepository
import com.example.busmanager.domain.route.usecase.GetAvailableTerminalsUseCase
import com.example.busmanager.domain.simulation.model.ActiveBusTrip
import com.example.busmanager.domain.simulation.model.GameCalendar
import com.example.busmanager.domain.simulation.model.RouteDirection
import com.example.busmanager.domain.simulation.model.SimulationSpeed
import com.example.busmanager.domain.simulation.usecase.DispatchBusUseCase
import com.example.busmanager.domain.simulation.usecase.GetActiveTripsUseCase
import com.example.busmanager.domain.simulation.usecase.GetSimulationTimeUseCase
import com.example.busmanager.domain.simulation.usecase.SetSimulationSpeedUseCase
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
    private lateinit var simulationTripRepo: SimulationTripRepositoryImpl
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
        simulationTripRepo = SimulationTripRepositoryImpl()
        simulationTimeRepo = SimulationTimeRepositoryImpl(
            initialTime = GameCalendar(2026, 11, 24, 8, 0),
            simulationTripRepository = simulationTripRepo
        )
        fakeCompanyRepo = FakeCompanyRepository()
        fakeFleetRepo = FakeFleetRepository()

        val getTimeUseCase = GetSimulationTimeUseCase(simulationTimeRepo)
        val togglePlayUseCase = ToggleSimulationPlayUseCase(simulationTimeRepo)
        val getCompanyUseCase = GetCompanyUseCase(fakeCompanyRepo)
        val getOwnedFleetUseCase = GetOwnedFleetUseCase(fakeFleetRepo)
        val getTerminalsUseCase = GetAvailableTerminalsUseCase(terminalRepo)
        val getActiveTripsUseCase = GetActiveTripsUseCase(simulationTripRepo)
        val dispatchBusUseCase = DispatchBusUseCase(simulationTripRepo)

        viewModel = SimulationMapViewModel(
            getTimeUseCase,
            togglePlayUseCase,
            getCompanyUseCase,
            getOwnedFleetUseCase,
            getTerminalsUseCase,
            routeRepo,
            getActiveTripsUseCase,
            dispatchBusUseCase
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
        assertEquals(SimulationSpeed.NORMAL, state.currentSpeed)
        assertEquals("Selasa, 24 November 2026 • 08:00 WIB", state.formattedDateTime)
        assertEquals("Selasa, 24 November 2026", state.formattedDate)
        assertEquals("08:00 WIB", state.formattedTime)
        assertFalse(state.hasActiveFleet)
        assertEquals(SimulationBottomSheetType.NONE, state.activeBottomSheet)
        assertTrue(state.activeTrips.isEmpty())
        assertNull(state.selectedTrip)
    }

    @Test
    fun setSpeed_updatesUiStateSpeed() = runTest {
        val setSpeedUseCase = SetSimulationSpeedUseCase(simulationTimeRepo)
        val customViewModel = SimulationMapViewModel(
            getSimulationTimeUseCase = GetSimulationTimeUseCase(simulationTimeRepo),
            toggleSimulationPlayUseCase = ToggleSimulationPlayUseCase(simulationTimeRepo),
            getCompanyUseCase = GetCompanyUseCase(fakeCompanyRepo),
            getOwnedFleetUseCase = GetOwnedFleetUseCase(fakeFleetRepo),
            getAvailableTerminalsUseCase = GetAvailableTerminalsUseCase(terminalRepo),
            routeRepository = routeRepo,
            getActiveTripsUseCase = GetActiveTripsUseCase(simulationTripRepo),
            dispatchBusUseCase = DispatchBusUseCase(simulationTripRepo),
            setSimulationSpeedUseCase = setSpeedUseCase
        )
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(SimulationSpeed.NORMAL, customViewModel.uiState.value.currentSpeed)

        customViewModel.setSpeed(SimulationSpeed.FAST)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(SimulationSpeed.FAST, customViewModel.uiState.value.currentSpeed)

        customViewModel.setSpeed(SimulationSpeed.VERY_FAST)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(SimulationSpeed.VERY_FAST, customViewModel.uiState.value.currentSpeed)
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
    fun dispatchBus_createsActiveTripAndUpdatesState() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val bus = createDummyBus("TN-01")
        val route = viewModel.uiState.value.routes.first()

        viewModel.dispatchBus(bus, route)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.activeTrips.size)
        assertEquals(1, state.activeBusCount)
        assertTrue(state.hasActiveFleet)
        assertEquals(bus.busCode, state.activeTrips.first().busCode)
    }

    @Test
    fun onTripClickAndClear_handlesTripSelection() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val bus = createDummyBus("TN-01")
        val route = viewModel.uiState.value.routes.first()
        viewModel.dispatchBus(bus, route)
        testDispatcher.scheduler.advanceUntilIdle()

        val trip = viewModel.uiState.value.activeTrips.first()
        viewModel.onTripClick(trip)

        assertEquals(trip.id, viewModel.uiState.value.selectedTrip?.id)
        assertEquals(route.id, viewModel.uiState.value.selectedRoute?.id)

        viewModel.clearSelectedTrip()
        assertNull(viewModel.uiState.value.selectedTrip)
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

    @Test
    fun scheduledDepartureDispatchesBusWhenThreeConditionsMet() = runTest {
        val fakeDriverRepo = FakeDriverRepository()
        val fakePricingRepo = FakeRoutePricingRepository()
        val fakeScheduleRepo = FakeRouteScheduleRepository()
        val validateUseCase = ValidateBusOperationUseCase(fakeDriverRepo, fakePricingRepo)

        val route = routeRepo.getAllPredefinedRoutes().first()

        val bus = createDummyBus("TN-01").copy(
            id = "bus-1",
            routeId = route.id,
            driverId = "driver-1"
        )
        fakeFleetRepo.emitBuses(listOf(bus))

        fakeDriverRepo.saveDriver(
            Driver(
                id = "driver-1",
                name = "Pak Bambang",
                wagePerDay = Money.rupiah(180_000L),
                assignedBusId = "bus-1",
                isHired = true
            )
        )

        fakePricingRepo.savePricing(
            RoutePricing(
                id = "price-1",
                routeId = route.id,
                originTerminalId = route.originTerminal.id,
                destinationTerminalId = route.destinationTerminal.id,
                originCity = route.originTerminal.city,
                destinationCity = route.destinationTerminal.city,
                price = Money.rupiah(100_000L)
            )
        )

        val schedule = RouteSchedule(
            id = "sched-1",
            routeId = route.id,
            departureHour = 8,
            departureMinute = 0,
            direction = RouteDirection.ORIGIN_TO_DESTINATION,
            assignedBusId = "bus-1"
        )
        fakeScheduleRepo.addSchedule(schedule)

        val customViewModel = SimulationMapViewModel(
            getSimulationTimeUseCase = GetSimulationTimeUseCase(simulationTimeRepo),
            toggleSimulationPlayUseCase = ToggleSimulationPlayUseCase(simulationTimeRepo),
            getCompanyUseCase = GetCompanyUseCase(fakeCompanyRepo),
            getOwnedFleetUseCase = GetOwnedFleetUseCase(fakeFleetRepo),
            getAvailableTerminalsUseCase = GetAvailableTerminalsUseCase(terminalRepo),
            routeRepository = routeRepo,
            getActiveTripsUseCase = GetActiveTripsUseCase(simulationTripRepo),
            dispatchBusUseCase = DispatchBusUseCase(simulationTripRepo),
            validateBusOperationUseCase = validateUseCase,
            routeScheduleRepository = fakeScheduleRepo,
            routePricingRepository = fakePricingRepo,
            companyRepository = fakeCompanyRepo,
            driverRepository = fakeDriverRepo
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val status = customViewModel.uiState.value.busOperationStatuses["bus-1"]
        assertNotNull(status)
        assertTrue(status!!.canOperate)

        customViewModel.togglePlay()
        testDispatcher.scheduler.advanceUntilIdle()

        val activeTrips = customViewModel.uiState.value.activeTrips
        assertEquals(1, activeTrips.size)
        assertEquals("bus-1", activeTrips.first().busId)
    }

    @Test
    fun scheduledDepartureIgnoredWhenMissingDriver() = runTest {
        val fakeDriverRepo = FakeDriverRepository()
        val fakePricingRepo = FakeRoutePricingRepository()
        val fakeScheduleRepo = FakeRouteScheduleRepository()
        val validateUseCase = ValidateBusOperationUseCase(fakeDriverRepo, fakePricingRepo)

        val route = routeRepo.getAllPredefinedRoutes().first()

        val bus = createDummyBus("TN-02").copy(
            id = "bus-2",
            routeId = route.id,
            driverId = null
        )
        fakeFleetRepo.emitBuses(listOf(bus))

        fakePricingRepo.savePricing(
            RoutePricing(
                id = "price-2",
                routeId = route.id,
                originTerminalId = route.originTerminal.id,
                destinationTerminalId = route.destinationTerminal.id,
                originCity = route.originTerminal.city,
                destinationCity = route.destinationTerminal.city,
                price = Money.rupiah(100_000L)
            )
        )

        val schedule = RouteSchedule(
            id = "sched-2",
            routeId = route.id,
            departureHour = 8,
            departureMinute = 0,
            direction = RouteDirection.ORIGIN_TO_DESTINATION,
            assignedBusId = "bus-2"
        )
        fakeScheduleRepo.addSchedule(schedule)

        val customViewModel = SimulationMapViewModel(
            getSimulationTimeUseCase = GetSimulationTimeUseCase(simulationTimeRepo),
            toggleSimulationPlayUseCase = ToggleSimulationPlayUseCase(simulationTimeRepo),
            getCompanyUseCase = GetCompanyUseCase(fakeCompanyRepo),
            getOwnedFleetUseCase = GetOwnedFleetUseCase(fakeFleetRepo),
            getAvailableTerminalsUseCase = GetAvailableTerminalsUseCase(terminalRepo),
            routeRepository = routeRepo,
            getActiveTripsUseCase = GetActiveTripsUseCase(simulationTripRepo),
            dispatchBusUseCase = DispatchBusUseCase(simulationTripRepo),
            validateBusOperationUseCase = validateUseCase,
            routeScheduleRepository = fakeScheduleRepo,
            routePricingRepository = fakePricingRepo,
            companyRepository = fakeCompanyRepo,
            driverRepository = fakeDriverRepo
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val status = customViewModel.uiState.value.busOperationStatuses["bus-2"]
        assertNotNull(status)
        assertFalse(status!!.canOperate)
        assertFalse(status.hasDriver)

        customViewModel.togglePlay()
        testDispatcher.scheduler.advanceUntilIdle()

        val activeTrips = customViewModel.uiState.value.activeTrips
        assertEquals(0, activeTrips.size)
    }

    @Test
    fun scheduledDeparture_recordsTicketRevenueTransaction() = runTest {
        val fakeDriverRepo = FakeDriverRepository()
        val fakePricingRepo = FakeRoutePricingRepository()
        val fakeScheduleRepo = FakeRouteScheduleRepository()
        val fakeTxRepo = FakeFinancialTransactionRepository()
        val recordTxUseCase = RecordFinancialTransactionUseCase(fakeTxRepo, fakeCompanyRepo)
        val validateUseCase = ValidateBusOperationUseCase(fakeDriverRepo, fakePricingRepo)

        val route = routeRepo.getAllPredefinedRoutes().first()

        val driver = Driver(
            id = "driver-1",
            name = "Bambang",
            wagePerDay = Money.rupiah(250_000L),
            isHired = true,
            assignedBusId = "bus-1"
        )
        fakeDriverRepo.saveDriver(driver)

        val bus = createDummyBus("TN-01").copy(
            id = "bus-1",
            routeId = route.id,
            driverId = "driver-1"
        )
        fakeFleetRepo.emitBuses(listOf(bus))

        fakePricingRepo.savePricing(
            RoutePricing(
                id = "price-tx",
                routeId = route.id,
                originTerminalId = route.originTerminal.id,
                destinationTerminalId = route.destinationTerminal.id,
                originCity = route.originTerminal.city,
                destinationCity = route.destinationTerminal.city,
                price = Money.rupiah(100_000L)
            )
        )

        val schedule = RouteSchedule(
            id = "sched-tx",
            routeId = route.id,
            departureHour = 8,
            departureMinute = 0,
            direction = RouteDirection.ORIGIN_TO_DESTINATION,
            assignedBusId = "bus-1"
        )
        fakeScheduleRepo.addSchedule(schedule)

        val customViewModel = SimulationMapViewModel(
            getSimulationTimeUseCase = GetSimulationTimeUseCase(simulationTimeRepo),
            toggleSimulationPlayUseCase = ToggleSimulationPlayUseCase(simulationTimeRepo),
            getCompanyUseCase = GetCompanyUseCase(fakeCompanyRepo),
            getOwnedFleetUseCase = GetOwnedFleetUseCase(fakeFleetRepo),
            getAvailableTerminalsUseCase = GetAvailableTerminalsUseCase(terminalRepo),
            routeRepository = routeRepo,
            getActiveTripsUseCase = GetActiveTripsUseCase(simulationTripRepo),
            dispatchBusUseCase = DispatchBusUseCase(simulationTripRepo),
            validateBusOperationUseCase = validateUseCase,
            routeScheduleRepository = fakeScheduleRepo,
            routePricingRepository = fakePricingRepo,
            companyRepository = fakeCompanyRepo,
            driverRepository = fakeDriverRepo,
            recordFinancialTransactionUseCase = recordTxUseCase
        )

        testDispatcher.scheduler.advanceUntilIdle()
        customViewModel.togglePlay()
        testDispatcher.scheduler.advanceUntilIdle()

        val txs = fakeTxRepo.getTransactions()
        val ticketTx = txs.find { it.category == TransactionCategory.TICKET_SALE }
        assertNotNull(ticketTx)
        assertEquals(TransactionType.INCOME, ticketTx?.type)
        assertEquals("bus-1", ticketTx?.busId)

        val wageTx = txs.find { it.category == TransactionCategory.DRIVER_WAGE }
        assertNotNull(wageTx)
        assertEquals(TransactionType.EXPENSE, wageTx?.type)
        assertEquals("bus-1", wageTx?.busId)
        assertEquals((ticketTx!!.amount.amount * 0.30).toLong(), wageTx?.amount?.amount)
    }

    @Test
    fun scheduledTrip_recordsThirtyPercentDriverWageAndDayRolloverDoesNotDeductFlatWage() = runTest {
        val fakeDriverRepo = FakeDriverRepository()
        val fakePricingRepo = FakeRoutePricingRepository()
        val fakeScheduleRepo = FakeRouteScheduleRepository()
        val fakeTxRepo = FakeFinancialTransactionRepository()
        val recordTxUseCase = RecordFinancialTransactionUseCase(fakeTxRepo, fakeCompanyRepo)

        val driver = Driver(
            id = "driver-wage-1",
            name = "Supriadi",
            wagePerDay = Money.rupiah(200_000L),
            isHired = true,
            assignedBusId = "bus-1"
        )
        fakeDriverRepo.saveDriver(driver)

        val bus = createDummyBus("TN-01").copy(id = "bus-1", driverId = "driver-wage-1")
        fakeFleetRepo.emitBuses(listOf(bus))

        val customViewModel = SimulationMapViewModel(
            getSimulationTimeUseCase = GetSimulationTimeUseCase(simulationTimeRepo),
            toggleSimulationPlayUseCase = ToggleSimulationPlayUseCase(simulationTimeRepo),
            getCompanyUseCase = GetCompanyUseCase(fakeCompanyRepo),
            getOwnedFleetUseCase = GetOwnedFleetUseCase(fakeFleetRepo),
            getAvailableTerminalsUseCase = GetAvailableTerminalsUseCase(terminalRepo),
            routeRepository = routeRepo,
            getActiveTripsUseCase = GetActiveTripsUseCase(simulationTripRepo),
            dispatchBusUseCase = DispatchBusUseCase(simulationTripRepo),
            companyRepository = fakeCompanyRepo,
            driverRepository = fakeDriverRepo,
            recordFinancialTransactionUseCase = recordTxUseCase
        )

        testDispatcher.scheduler.advanceUntilIdle()
        customViewModel.togglePlay()
        testDispatcher.scheduler.advanceUntilIdle()

        simulationTimeRepo.setTime(GameCalendar(2026, 11, 25, 1, 0))
        testDispatcher.scheduler.advanceUntilIdle()

        val idleTxs = fakeTxRepo.getTransactions()
        assertTrue(idleTxs.isEmpty())

        val route = routeRepo.getAllPredefinedRoutes().first()
        customViewModel.dispatchBus(bus, route)
        testDispatcher.scheduler.advanceUntilIdle()

        val activeTxs = fakeTxRepo.getTransactions()
        val ticketTx = activeTxs.find { it.category == TransactionCategory.TICKET_SALE }
        val wageTx = activeTxs.find { it.category == TransactionCategory.DRIVER_WAGE }

        assertNotNull(ticketTx)
        assertNotNull(wageTx)
        assertEquals(TransactionType.EXPENSE, wageTx?.type)
        assertEquals((ticketTx!!.amount.amount * 0.30).toLong(), wageTx?.amount?.amount)
        assertEquals("bus-1", wageTx?.busId)
    }

    @Test
    fun trackBus_setsSelectedTripAndTrackedTripId() = runTest(testDispatcher) {
        val sby = terminalRepo.getTerminalById("term_purabaya")!!
        val mlg = terminalRepo.getTerminalById("term_arjosari")!!
        val route = routeRepo.getAllPredefinedRoutes().first()
        val trip = ActiveBusTrip(
            id = "trip-test-1",
            busId = "bus-1",
            busCode = "TN-01",
            route = route,
            departureTime = GameCalendar.initial(),
            estimatedArrivalMinutes = 120,
            currentCoordinate = sby.coordinate,
            passengerCount = 30,
            ticketEarnings = Money.rupiah(1_500_000L)
        )

        viewModel.trackBus(trip)

        val state = viewModel.uiState.value
        assertEquals("trip-test-1", state.selectedTrip?.id)
        assertEquals("trip-test-1", state.trackedTripId)
        assertEquals(route.id, state.selectedRoute?.id)
        assertEquals(SimulationBottomSheetType.NONE, state.activeBottomSheet)
    }

    @Test
    fun clearSelectedTrip_clearsTrackedTripId() = runTest(testDispatcher) {
        val sby = terminalRepo.getTerminalById("term_purabaya")!!
        val route = routeRepo.getAllPredefinedRoutes().first()
        val trip = ActiveBusTrip(
            id = "trip-test-2",
            busId = "bus-2",
            busCode = "TN-02",
            route = route,
            departureTime = GameCalendar.initial(),
            estimatedArrivalMinutes = 120,
            currentCoordinate = sby.coordinate,
            passengerCount = 25,
            ticketEarnings = Money.rupiah(1_250_000L)
        )

        viewModel.trackBus(trip)
        assertEquals("trip-test-2", viewModel.uiState.value.trackedTripId)

        viewModel.clearSelectedTrip()
        assertNull(viewModel.uiState.value.selectedTrip)
        assertNull(viewModel.uiState.value.trackedTripId)
    }

    @Test
    fun openedRoutes_computedFromSchedulesBusesAndTrips() = runTest(testDispatcher) {
        val route1 = routeRepo.getAllPredefinedRoutes()[0]
        val route2 = routeRepo.getAllPredefinedRoutes()[1]
        val sby = terminalRepo.getTerminalById("term_purabaya")!!

        val trip = ActiveBusTrip(
            id = "trip-route-1",
            busId = "bus-1",
            busCode = "TN-01",
            route = route1,
            departureTime = GameCalendar.initial(),
            estimatedArrivalMinutes = 120,
            currentCoordinate = sby.coordinate,
            passengerCount = 30,
            ticketEarnings = Money.rupiah(1_500_000L)
        )

        simulationTripRepo.dispatchBus(
            bus = createDummyBus("TN-01").copy(id = "bus-1", routeId = route2.id),
            route = route1,
            direction = RouteDirection.ORIGIN_TO_DESTINATION,
            currentTime = GameCalendar.initial()
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.openedRouteIds.contains(route1.id))
        assertTrue(state.openedRoutes.any { it.id == route1.id })
    }

    @Test
    fun observePricings_populatesRoutePricingsMap() = runTest(testDispatcher) {
        val pricingRepo = FakeRoutePricingRepository()
        val pricing = RoutePricing(
            id = "pr-1",
            routeId = "route-1",
            originTerminalId = "term_purabaya",
            destinationTerminalId = "term_arjosari",
            originCity = "Surabaya",
            destinationCity = "Malang",
            price = Money.rupiah(75_000L)
        )
        pricingRepo.savePricing(pricing)

        val vmWithPricing = SimulationMapViewModel(
            getSimulationTimeUseCase = GetSimulationTimeUseCase(simulationTimeRepo),
            toggleSimulationPlayUseCase = ToggleSimulationPlayUseCase(simulationTimeRepo),
            getCompanyUseCase = GetCompanyUseCase(fakeCompanyRepo),
            getOwnedFleetUseCase = GetOwnedFleetUseCase(fakeFleetRepo),
            getAvailableTerminalsUseCase = GetAvailableTerminalsUseCase(terminalRepo),
            routeRepository = routeRepo,
            getActiveTripsUseCase = GetActiveTripsUseCase(simulationTripRepo),
            dispatchBusUseCase = DispatchBusUseCase(simulationTripRepo),
            routePricingRepository = pricingRepo
        )

        testDispatcher.scheduler.advanceUntilIdle()
        val state = vmWithPricing.uiState.value
        assertTrue(state.routePricings.containsKey("route-1"))
        assertEquals(1, state.routePricings["route-1"]?.size)
        assertEquals(Money.rupiah(75_000L), state.routePricings["route-1"]?.first()?.price)
    }

    private class FakeFinancialTransactionRepository : FinancialTransactionRepository {
        private val list = mutableListOf<FinancialTransaction>()
        private val flow = MutableStateFlow<List<FinancialTransaction>>(emptyList())

        override fun getAllTransactionsFlow(): Flow<List<FinancialTransaction>> = flow
        override suspend fun getTransactions(): List<FinancialTransaction> = list.toList()
        override suspend fun recordTransaction(transaction: FinancialTransaction) {
            list.add(transaction)
            flow.value = list.toList()
        }
        override suspend fun clearTransactions() {
            list.clear()
            flow.value = emptyList()
        }
    }

    private class FakeDriverRepository : DriverRepository {
        private val drivers = mutableMapOf<String, Driver>()
        private val flow = MutableStateFlow<List<Driver>>(emptyList())

        fun saveDriver(driver: Driver) {
            drivers[driver.id] = driver
            flow.value = drivers.values.toList()
        }

        override fun getAllDrivers(): Flow<List<Driver>> = flow
        override fun getHiredDrivers(): Flow<List<Driver>> = flow
        override suspend fun getDriverById(id: String): Driver? = drivers[id]
        override suspend fun hireDriver(driverId: String): Boolean = true
        override suspend fun assignDriverToBus(driverId: String, busId: String): Boolean = true
        override suspend fun unassignDriver(driverId: String) {}
        override suspend fun fireDriver(driverId: String) {}
        override suspend fun seedCandidateDriversIfEmpty() {}
    }

    private class FakeRoutePricingRepository : RoutePricingRepository {
        private val pricings = mutableListOf<RoutePricing>()
        private val flow = MutableStateFlow<List<RoutePricing>>(emptyList())

        override fun getPricingsForRoute(routeId: String): Flow<List<RoutePricing>> = flow
        override suspend fun getPricingsListForRoute(routeId: String): List<RoutePricing> = pricings
        override fun getAllPricingsFlow(): Flow<List<RoutePricing>> = flow
        override suspend fun getAllPricings(): List<RoutePricing> = pricings.toList()
        override suspend fun savePricing(pricing: RoutePricing) {
            pricings.add(pricing)
            flow.value = pricings.toList()
        }
        override suspend fun savePricings(pricings: List<RoutePricing>) {
            this.pricings.addAll(pricings)
            flow.value = this.pricings.toList()
        }
        override suspend fun getPricingBetween(
            routeId: String,
            originTerminalId: String,
            destinationTerminalId: String
        ): RoutePricing? = pricings.find { it.routeId == routeId && it.originTerminalId == originTerminalId && it.destinationTerminalId == destinationTerminalId }
        override suspend fun generateAndSaveDefaultPricings(route: BusRoute): List<RoutePricing> = emptyList()
    }

    private class FakeRouteScheduleRepository : RouteScheduleRepository {
        private val schedules = mutableListOf<RouteSchedule>()
        private val flow = MutableStateFlow<List<RouteSchedule>>(emptyList())

        override fun getSchedulesForRoute(routeId: String): Flow<List<RouteSchedule>> = flow
        override fun getAllSchedules(): Flow<List<RouteSchedule>> = flow
        override suspend fun getSchedulesListForRoute(routeId: String): List<RouteSchedule> = schedules
        override suspend fun getScheduleById(id: String): RouteSchedule? = schedules.find { it.id == id }
        override suspend fun addSchedule(schedule: RouteSchedule): Result<Unit> {
            schedules.add(schedule)
            flow.value = schedules.toList()
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
