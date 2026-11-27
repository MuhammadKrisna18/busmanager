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
import com.example.busmanager.domain.fleet.usecase.SellBusResult
import com.example.busmanager.domain.fleet.usecase.SellBusUseCase
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.RouteCategory
import com.example.busmanager.domain.route.model.RoutePricing
import com.example.busmanager.domain.route.model.RouteSchedule
import com.example.busmanager.domain.route.model.TerminalDepartureSlot
import com.example.busmanager.domain.route.repository.RoutePricingRepository
import com.example.busmanager.domain.route.repository.RouteScheduleRepository
import com.example.busmanager.domain.route.usecase.AssignBusToActiveRouteUseCase
import com.example.busmanager.domain.route.usecase.ValidateScheduleConflictUseCase
import com.example.busmanager.domain.simulation.model.ActiveBusTrip
import com.example.busmanager.domain.simulation.model.GameCalendar
import com.example.busmanager.domain.simulation.model.RouteDirection
import com.example.busmanager.domain.simulation.repository.SimulationTripRepository
import com.example.busmanager.domain.simulation.usecase.GetActiveTripsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    private lateinit var fakeScheduleRepo: FakeRouteScheduleRepository

    private lateinit var viewModel: GarageViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        fakeFleetRepo = FakeFleetRepository()
        fakeDriverRepo = FakeDriverRepository()
        fakePricingRepo = FakeRoutePricingRepository()
        fakeCompanyRepo = FakeCompanyRepository()
        fakeScheduleRepo = FakeRouteScheduleRepository()

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
        val driver2 = Driver(
            id = "driver-2",
            name = "Pak Bambang",
            wagePerDay = Money.rupiah(180_000L),
            assignedBusId = null,
            isHired = true
        )
        fakeDriverRepo.setDrivers(listOf(driver1, driver2))

        val sellBusUseCase = SellBusUseCase(
            fleetRepository = fakeFleetRepo,
            companyRepository = fakeCompanyRepo,
            driverRepository = fakeDriverRepo,
            routeScheduleRepository = fakeScheduleRepo
        )

        val validateConflictUseCase = ValidateScheduleConflictUseCase(fakeScheduleRepo, routeRepository)
        val assignBusUseCase = AssignBusToActiveRouteUseCase(
            fleetRepository = fakeFleetRepo,
            routeRepository = routeRepository,
            driverRepository = fakeDriverRepo,
            routeScheduleRepository = fakeScheduleRepo,
            validateScheduleConflictUseCase = validateConflictUseCase
        )

        viewModel = GarageViewModel(
            fleetRepository = fakeFleetRepo,
            driverRepository = fakeDriverRepo,
            routeRepository = routeRepository,
            routePricingRepository = fakePricingRepo,
            companyRepository = fakeCompanyRepo,
            sellBusUseCase = sellBusUseCase,
            routeScheduleRepository = fakeScheduleRepo,
            assignBusToActiveRouteUseCase = assignBusUseCase,
            validateScheduleConflictUseCase = validateConflictUseCase
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

    @Test
    fun activeTripsFlow_updatesOperatingStatusAndRouteDetailsDynamically() = runTest {
        val fakeTripRepo = FakeSimulationTripRepository()
        val getActiveTripsUseCase = GetActiveTripsUseCase(fakeTripRepo)

        val vm = GarageViewModel(
            fleetRepository = fakeFleetRepo,
            driverRepository = fakeDriverRepo,
            routeRepository = routeRepository,
            routePricingRepository = fakePricingRepo,
            companyRepository = fakeCompanyRepo,
            getActiveTripsUseCase = getActiveTripsUseCase
        )
        advanceUntilIdle()

        val sampleRoute = routeRepository.getRouteById("route_sby_mlg_toll")!!
        val sampleTrip = ActiveBusTrip(
            id = "trip-test-1",
            busId = "bus-2",
            busCode = "TN-02",
            route = sampleRoute,
            direction = RouteDirection.DESTINATION_TO_ORIGIN,
            departureTime = GameCalendar.initial(),
            estimatedArrivalMinutes = 60,
            elapsedMinutes = 30,
            progressFraction = 0.5f,
            currentCoordinate = sampleRoute.originTerminal.coordinate,
            passengerCount = 25,
            ticketEarnings = Money.rupiah(1_500_000L),
            isCompleted = false,
            maxCapacity = 30
        )

        fakeTripRepo.setActiveTrips(listOf(sampleTrip))
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(2, state.operatingBusCount)
        assertEquals(0, state.standbyBusCount)

        val bus2Item = state.allBuses.find { it.bus.id == "bus-2" }
        assertNotNull(bus2Item)
        assertTrue(bus2Item!!.isOperating)
        assertEquals(sampleRoute.name, bus2Item.routeName)
        assertEquals("Menuju Surabaya dari Malang", bus2Item.activeTripDirection)
        assertEquals(50, bus2Item.activeTripProgressPercent)

        fakeTripRepo.setActiveTrips(emptyList())
        advanceUntilIdle()

        val updatedState = vm.uiState.value
        assertEquals(1, updatedState.operatingBusCount)
        assertEquals(1, updatedState.standbyBusCount)
        assertFalse(updatedState.allBuses.find { it.bus.id == "bus-2" }!!.isOperating)
    }

    @Test
    fun busWaitingForDeparture_classifiedAsOperatingAndDisplaysWaitingStatus() = runTest {
        val sampleBusWaiting = Bus(
            id = "bus-waiting",
            busCode = "TN-99",
            chassis = ChassisDataSource().getChassisList().first(),
            body = KaroseriBodyDataSource().getKaroseriBodyList().first(),
            routeId = "route_sby_mlg_toll",
            driverId = "driver-99",
            isOperating = false
        )
        val driver99 = Driver(
            id = "driver-99",
            name = "Budi Santoso",
            wagePerDay = Money.rupiah(200_000L),
            assignedBusId = "bus-waiting",
            isHired = true
        )
        val defaultPricings = listOf(
            RoutePricing(
                id = "p-1",
                routeId = "route_sby_mlg_toll",
                originTerminalId = "term-a",
                destinationTerminalId = "term-b",
                originCity = "Surabaya",
                destinationCity = "Malang",
                price = Money.rupiah(50_000L)
            )
        )
        fakePricingRepo.savePricings(defaultPricings)
        fakeDriverRepo.setDrivers(listOf(driver99))
        fakeFleetRepo.setBuses(listOf(sampleBusWaiting))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.operatingBusCount)
        assertEquals(0, state.standbyBusCount)

        val waitingItem = state.allBuses.first()
        assertTrue(waitingItem.isOnDuty)
        assertTrue(waitingItem.canOperate)
        assertFalse(waitingItem.isOperating)
        assertEquals("Menunggu Jadwal Keberangkatan", waitingItem.statusLabel)

        viewModel.setFilter(GarageFilter.OPERATING)
        assertEquals(1, viewModel.uiState.value.filteredBuses.size)

        viewModel.setFilter(GarageFilter.STANDBY)
        assertEquals(0, viewModel.uiState.value.filteredBuses.size)
    }

    @Test
    fun sellBus_succeedsForStandbyBus() = runTest {
        advanceUntilIdle()

        var sellResult: SellBusResult? = null
        viewModel.sellBus("bus-2") { result ->
            sellResult = result
        }
        advanceUntilIdle()

        assertTrue(sellResult is SellBusResult.Success)
        val state = viewModel.uiState.value
        assertEquals(1, state.totalBusCount)
        assertEquals("bus-1", state.allBuses.first().bus.id)
    }

    @Test
    fun sellBus_failsForOperatingBus() = runTest {
        advanceUntilIdle()

        var sellResult: SellBusResult? = null
        viewModel.sellBus("bus-1") { result ->
            sellResult = result
        }
        advanceUntilIdle()

        assertTrue(sellResult is SellBusResult.BusOperatingError)
        assertEquals(2, viewModel.uiState.value.totalBusCount)
    }

    @Test
    fun openQuickAssignDialog_generates24HourSlotsAndPlan() = runTest {
        advanceUntilIdle()
        val standbyItem = viewModel.uiState.value.allBuses.first { it.bus.id == "bus-2" }

        viewModel.openQuickAssignDialog(standbyItem)
        advanceUntilIdle()

        val assignState = viewModel.uiState.value.quickAssignState
        assertNotNull(assignState)
        assertEquals(96, assignState?.availableSlots?.size)
        assertNotNull(assignState?.selectedSlot)
        assertNotNull(assignState?.dailySchedulePlan)
        assertEquals(RouteCategory.AKDP, assignState?.routeCategory)
        assertEquals(2, assignState?.targetRitCount)
        assertEquals(4, assignState?.dailySchedulePlan?.legs?.size)
        assertTrue(assignState?.canSubmit == true)
    }

    @Test
    fun selectQuickAssignSlot_updatesSelectedSlotAndPlan() = runTest {
        advanceUntilIdle()
        val standbyItem = viewModel.uiState.value.allBuses.first { it.bus.id == "bus-2" }
        viewModel.openQuickAssignDialog(standbyItem)
        advanceUntilIdle()

        val targetSlot = viewModel.uiState.value.quickAssignState?.availableSlots?.find { it.hour == 8 && it.minute == 15 }
        assertNotNull(targetSlot)

        viewModel.selectQuickAssignSlot(targetSlot!!)
        val state = viewModel.uiState.value.quickAssignState

        assertEquals(8, state?.selectedSlot?.hour)
        assertEquals(15, state?.selectedSlot?.minute)
        assertEquals(8, state?.dailySchedulePlan?.legs?.first()?.departureHour)
        assertEquals(15, state?.dailySchedulePlan?.legs?.first()?.departureMinute)
    }

    @Test
    fun selectQuickAssignSlot_whenOccupied_redirectsToNextAvailableSlot() = runTest {
        advanceUntilIdle()
        val standbyItem = viewModel.uiState.value.allBuses.first { it.bus.id == "bus-2" }
        viewModel.openQuickAssignDialog(standbyItem)
        advanceUntilIdle()

        val occupiedSlot = TerminalDepartureSlot(
            slotId = "term-sub_0815",
            terminalId = "term-sub",
            hour = 8,
            minute = 15,
            isAvailable = false,
            occupiedDestinationCity = "Malang"
        )

        viewModel.selectQuickAssignSlot(occupiedSlot)
        val state = viewModel.uiState.value.quickAssignState

        assertNotNull(state?.conflictError)
        assertTrue(state?.conflictError?.contains("Dialihkan ke slot") == true)
        assertTrue(state?.selectedSlot?.isAvailable == true)
    }

    @Test
    fun setQuickAssignTargetRitCount_updatesPlanToSingleOrDoubleRit() = runTest {
        advanceUntilIdle()
        val standbyItem = viewModel.uiState.value.allBuses.first { it.bus.id == "bus-2" }
        viewModel.openQuickAssignDialog(standbyItem)
        advanceUntilIdle()

        viewModel.setQuickAssignTargetRitCount(1)
        var state = viewModel.uiState.value.quickAssignState
        assertEquals(1, state?.targetRitCount)
        assertEquals(2, state?.dailySchedulePlan?.legs?.size)

        viewModel.setQuickAssignTargetRitCount(2)
        state = viewModel.uiState.value.quickAssignState
        assertEquals(2, state?.targetRitCount)
        assertEquals(4, state?.dailySchedulePlan?.legs?.size)
    }

    @Test
    fun confirmQuickAssign_assignsBusAndPersistsAllScheduleLegs() = runTest {
        advanceUntilIdle()
        val standbyItem = viewModel.uiState.value.allBuses.first { it.bus.id == "bus-2" }
        viewModel.openQuickAssignDialog(standbyItem)
        advanceUntilIdle()

        viewModel.confirmQuickAssign()
        advanceUntilIdle()

        val finalState = viewModel.uiState.value
        assertNull(finalState.quickAssignState)
        assertNotNull(finalState.feedbackToast)

        val updatedBus = fakeFleetRepo.getBusById("bus-2")
        assertNotNull(updatedBus?.routeId)
        assertEquals("driver-2", updatedBus?.driverId)

        val schedules = fakeScheduleRepo.schedulesList.filter { it.assignedBusId == "bus-2" }
        assertEquals(4, schedules.size)
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
        override suspend fun removeBus(busId: String) {
            list.removeAll { it.id == busId }
            flow.value = list.toList()
        }
        override suspend fun removeAllBuses() {
            list.clear()
            flow.value = emptyList()
        }
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
        override suspend fun setPendingRelease(driverId: String, isPending: Boolean) {}
        override suspend fun resetAllDrivers() {}
    }

    private class FakeRoutePricingRepository : RoutePricingRepository {
        val pricingsList = mutableListOf<RoutePricing>()

        override fun getPricingsForRoute(routeId: String): Flow<List<RoutePricing>> = flowOf(pricingsList.filter { it.routeId == routeId })
        override suspend fun getPricingsListForRoute(routeId: String): List<RoutePricing> = pricingsList.filter { it.routeId == routeId }
        override fun getAllPricingsFlow(): Flow<List<RoutePricing>> = flowOf(pricingsList)
        override suspend fun getAllPricings(): List<RoutePricing> = pricingsList
        override suspend fun savePricing(pricing: RoutePricing) {
            pricingsList.removeAll { it.id == pricing.id }
            pricingsList.add(pricing)
        }
        override suspend fun savePricings(pricings: List<RoutePricing>) {
            pricings.forEach { savePricing(it) }
        }
        override suspend fun getPricingBetween(
            routeId: String,
            originTerminalId: String,
            destinationTerminalId: String
        ): RoutePricing? = pricingsList.find { it.routeId == routeId && it.originTerminalId == originTerminalId && it.destinationTerminalId == destinationTerminalId }
        override suspend fun generateAndSaveDefaultPricings(route: BusRoute): List<RoutePricing> = emptyList()
        override suspend fun deleteAllPricings() {}
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
        override suspend fun resetCompany(name: String, initialBalance: Money) {}
    }

    private class FakeSimulationTripRepository : SimulationTripRepository {
        private val _trips = MutableStateFlow<List<ActiveBusTrip>>(emptyList())
        override val activeTrips: StateFlow<List<ActiveBusTrip>> = _trips

        fun setActiveTrips(trips: List<ActiveBusTrip>) {
            _trips.value = trips
        }

        override fun dispatchBus(bus: Bus, route: BusRoute, direction: RouteDirection, currentTime: GameCalendar): ActiveBusTrip {
            error("Not implemented")
        }

        override fun advanceTripsTime(advanceMinutes: Int): List<ActiveBusTrip> = emptyList()

        override fun cancelTrip(tripId: String) {}

        override fun markCancelNextSchedule(tripId: String, cancel: Boolean) {}

        override fun completeTrip(tripId: String) {}

        override fun clearTrips() {}
    }

    private class FakeRouteScheduleRepository : RouteScheduleRepository {
        val schedulesList = mutableListOf<RouteSchedule>()
        private val schedulesFlow = MutableStateFlow<List<RouteSchedule>>(emptyList())

        override fun getSchedulesForRoute(routeId: String): Flow<List<RouteSchedule>> = flowOf(schedulesList.filter { it.routeId == routeId })
        override fun getAllSchedules(): Flow<List<RouteSchedule>> = schedulesFlow
        override suspend fun getSchedulesListForRoute(routeId: String): List<RouteSchedule> = schedulesList.filter { it.routeId == routeId }
        override suspend fun getScheduleById(id: String): RouteSchedule? = schedulesList.find { it.id == id }
        override suspend fun addSchedule(schedule: RouteSchedule): Result<Unit> {
            schedulesList.add(schedule)
            schedulesFlow.value = schedulesList.toList()
            return Result.success(Unit)
        }
        override suspend fun updateSchedule(schedule: RouteSchedule): Result<Unit> {
            val idx = schedulesList.indexOfFirst { it.id == schedule.id }
            if (idx >= 0) {
                schedulesList[idx] = schedule
                schedulesFlow.value = schedulesList.toList()
            }
            return Result.success(Unit)
        }
        override suspend fun assignBusToSchedule(scheduleId: String, busId: String): Boolean {
            val idx = schedulesList.indexOfFirst { it.id == scheduleId }
            if (idx >= 0) {
                schedulesList[idx] = schedulesList[idx].copy(assignedBusId = busId)
                schedulesFlow.value = schedulesList.toList()
                return true
            }
            return false
        }
        override suspend fun unassignBusFromSchedule(scheduleId: String) {
            val idx = schedulesList.indexOfFirst { it.id == scheduleId }
            if (idx >= 0) {
                schedulesList[idx] = schedulesList[idx].copy(assignedBusId = null)
                schedulesFlow.value = schedulesList.toList()
            }
        }
        override suspend fun deleteSchedule(scheduleId: String) {
            schedulesList.removeAll { it.id == scheduleId }
            schedulesFlow.value = schedulesList.toList()
        }
        override suspend fun deleteAllSchedules() {
            schedulesList.clear()
            schedulesFlow.value = emptyList()
        }
        override suspend fun checkConflict(routeId: String, direction: RouteDirection, hour: Int, minute: Int, excludeScheduleId: String?) = false
        override suspend fun getSchedulesForBus(busId: String): List<RouteSchedule> = schedulesList.filter { it.assignedBusId == busId }
        override fun getSchedulesForBusFlow(busId: String): Flow<List<RouteSchedule>> = schedulesFlow
        override suspend fun deleteSchedulesForBus(busId: String) {
            schedulesList.removeAll { it.assignedBusId == busId }
            schedulesFlow.value = schedulesList.toList()
        }
        override suspend fun deleteSchedulesForRoute(routeId: String) {
            schedulesList.removeAll { it.routeId == routeId }
            schedulesFlow.value = schedulesList.toList()
        }
    }
}
