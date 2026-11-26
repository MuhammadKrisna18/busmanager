package com.example.busmanager.domain.usecase

import com.example.busmanager.domain.company.model.Company
import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.company.repository.CompanyRepository
import com.example.busmanager.domain.driver.model.Driver
import com.example.busmanager.domain.driver.repository.DriverRepository
import com.example.busmanager.domain.finance.model.FinancialTransaction
import com.example.busmanager.domain.finance.repository.FinancialTransactionRepository
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
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.MapCoordinate
import com.example.busmanager.domain.route.model.RoutePathType
import com.example.busmanager.domain.route.model.RoutePricing
import com.example.busmanager.domain.route.model.RouteSchedule
import com.example.busmanager.domain.route.model.RouteSegment
import com.example.busmanager.domain.route.model.Terminal
import com.example.busmanager.domain.route.model.TerminalType
import com.example.busmanager.domain.route.repository.RoutePricingRepository
import com.example.busmanager.domain.route.repository.RouteRepository
import com.example.busmanager.domain.route.repository.RouteScheduleRepository
import com.example.busmanager.domain.simulation.model.GameCalendar
import com.example.busmanager.domain.simulation.model.RouteDirection
import com.example.busmanager.domain.simulation.model.SavedSimulationState
import com.example.busmanager.domain.simulation.model.SimulationSpeed
import com.example.busmanager.domain.simulation.repository.SimulationStateRepository
import com.example.busmanager.domain.simulation.repository.SimulationTimeRepository
import com.example.busmanager.domain.simulation.usecase.ProcessOfflineProgressionUseCase
import com.example.busmanager.domain.terminal.model.TerminalPassengerRecord
import com.example.busmanager.domain.terminal.repository.TerminalPassengerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ProcessOfflineProgressionUseCaseTest {

    private lateinit var fakeStateRepo: FakeSimulationStateRepository
    private lateinit var fakeTimeRepo: FakeSimulationTimeRepository
    private lateinit var fakeCompanyRepo: FakeCompanyRepository
    private lateinit var fakeFleetRepo: FakeFleetRepository
    private lateinit var fakeDriverRepo: FakeDriverRepository
    private lateinit var fakeRouteRepo: FakeRouteRepository
    private lateinit var fakeScheduleRepo: FakeRouteScheduleRepository
    private lateinit var fakePricingRepo: FakeRoutePricingRepository
    private lateinit var fakeTxRepo: FakeFinancialTransactionRepository
    private lateinit var fakePassengerRepo: FakeTerminalPassengerRepository
    private lateinit var useCase: ProcessOfflineProgressionUseCase

    private val terminalOrigin = Terminal(
        id = "t-sby",
        name = "Purabaya",
        city = "Surabaya",
        type = TerminalType.TIPE_A,
        coordinate = MapCoordinate(0.5f, 0.5f, -7.25, 112.75),
        retributionFee = Money.rupiah(5_000L)
    )

    private val terminalDest = Terminal(
        id = "t-mlg",
        name = "Arjosari",
        city = "Malang",
        type = TerminalType.TIPE_A,
        coordinate = MapCoordinate(0.6f, 0.6f, -7.98, 112.63),
        retributionFee = Money.rupiah(5_000L)
    )

    private val testRoute = BusRoute(
        id = "route-1",
        name = "Surabaya - Malang",
        originTerminal = terminalOrigin,
        destinationTerminal = terminalDest,
        segments = emptyList(),
        pathType = RoutePathType.TOLL
    )

    @Before
    fun setUp() {
        fakeStateRepo = FakeSimulationStateRepository()
        fakeTimeRepo = FakeSimulationTimeRepository()
        fakeCompanyRepo = FakeCompanyRepository()
        fakeFleetRepo = FakeFleetRepository()
        fakeDriverRepo = FakeDriverRepository()
        fakeRouteRepo = FakeRouteRepository(listOf(testRoute))
        fakeScheduleRepo = FakeRouteScheduleRepository()
        fakePricingRepo = FakeRoutePricingRepository()
        fakeTxRepo = FakeFinancialTransactionRepository()
        fakePassengerRepo = FakeTerminalPassengerRepository()

        useCase = ProcessOfflineProgressionUseCase(
            simulationStateRepository = fakeStateRepo,
            simulationTimeRepository = fakeTimeRepo,
            companyRepository = fakeCompanyRepo,
            fleetRepository = fakeFleetRepo,
            driverRepository = fakeDriverRepo,
            routeRepository = fakeRouteRepo,
            routeScheduleRepository = fakeScheduleRepo,
            routePricingRepository = fakePricingRepo,
            financialTransactionRepository = fakeTxRepo,
            terminalPassengerRepository = fakePassengerRepo
        )
    }

    @Test
    fun invoke_returnsNullWhenSimulationWasPaused() = runBlocking {
        fakeStateRepo.state = SavedSimulationState(
            lastWallClockMillis = System.currentTimeMillis() - 600_000L,
            isPlaying = false,
            speed = SimulationSpeed.NORMAL,
            gameCalendar = GameCalendar.initial()
        )

        val result = useCase()
        assertNull(result)
    }

    @Test
    fun invoke_returnsNullWhenTimeElapsedTooBrief() = runBlocking {
        fakeStateRepo.state = SavedSimulationState(
            lastWallClockMillis = System.currentTimeMillis() - 500L,
            isPlaying = true,
            speed = SimulationSpeed.NORMAL,
            gameCalendar = GameCalendar.initial()
        )

        val result = useCase()
        assertNull(result)
    }

    @Test
    fun invoke_processesTripsEarningsAndUpdatesStateWhenPlaying() = runBlocking {
        val pastCalendar = GameCalendar(2026, 11, 24, 8, 0)
        fakeStateRepo.state = SavedSimulationState(
            lastWallClockMillis = System.currentTimeMillis() - 360_000L,
            isPlaying = true,
            speed = SimulationSpeed.NORMAL,
            gameCalendar = pastCalendar
        )

        val chassis = Chassis(
            id = "c1",
            modelName = "Chassis 1",
            brand = ChassisBrand.HINO,
            engineLocation = EngineLocation.REAR,
            suspension = SuspensionType.LEAF_SPRING,
            axleType = AxleType.SINGLE_4X2,
            transmission = TransmissionType.MANUAL,
            horsepower = 260,
            torqueNm = 750,
            price = Money.rupiah(1_000_000_000L)
        )
        val body = KaroseriBody(
            id = "b1",
            modelName = "Jetbus 3+",
            brand = KaroseriBrand.ADIPUTRO,
            bodyType = BusBodyType.SHD,
            busClass = BusClass.EKSEKUTIF,
            passengerCapacity = 32,
            luggageCapacityKg = 1500,
            comfortRating = 8,
            price = Money.rupiah(500_000_000L)
        )
        val bus = Bus(
            id = "bus-1",
            busCode = "MK01",
            chassis = chassis,
            body = body,
            routeId = "route-1",
            driverId = "drv-1"
        )
        fakeFleetRepo.buses = listOf(bus)

        val driver = Driver(
            id = "drv-1",
            name = "Mas Joko",
            wagePerDay = Money.rupiah(200_000L),
            assignedBusId = "bus-1",
            isHired = true
        )
        fakeDriverRepo.drivers = listOf(driver)

        fakePricingRepo.pricings = listOf(
            RoutePricing(
                id = "p1",
                routeId = "route-1",
                originTerminalId = "t-sby",
                destinationTerminalId = "t-mlg",
                originCity = "Surabaya",
                destinationCity = "Malang",
                price = Money.rupiah(100_000L)
            )
        )

        val initialBalance = fakeCompanyRepo.company?.balance ?: Money.ZERO

        val result = useCase()
        assertNotNull(result)
        assertTrue(result!!.totalTripsCompleted > 0)
        assertTrue(result.netEarnings.amount > 0L)
        assertEquals(1, result.activeBusesCount)

        val finalBalance = fakeCompanyRepo.company?.balance ?: Money.ZERO
        assertEquals(initialBalance + result.netEarnings, finalBalance)

        assertTrue(fakeTxRepo.transactions.isNotEmpty())
        assertTrue(fakePassengerRepo.records.isNotEmpty())
        assertTrue(fakeTimeRepo.isPlaying.value)
    }

    private class FakeSimulationStateRepository : SimulationStateRepository {
        var state: SavedSimulationState? = null
        override fun saveState(state: SavedSimulationState) {
            this.state = state
        }
        override fun loadState(): SavedSimulationState? = state
        override fun clearState() {
            state = null
        }
    }

    private class FakeSimulationTimeRepository : SimulationTimeRepository {
        private val _time = MutableStateFlow(GameCalendar.initial())
        override val currentTime: StateFlow<GameCalendar> = _time

        private val _playing = MutableStateFlow(false)
        override val isPlaying: StateFlow<Boolean> = _playing

        private val _speed = MutableStateFlow(SimulationSpeed.NORMAL)
        override val currentSpeed: StateFlow<SimulationSpeed> = _speed

        override fun play() {
            _playing.value = true
        }

        override fun pause() {
            _playing.value = false
        }

        override fun togglePlay() {
            _playing.value = !_playing.value
        }

        override fun setSpeed(speed: SimulationSpeed) {
            _speed.value = speed
        }

        override fun advanceMinutes(minutes: Int) {
            _time.value = _time.value.advanceMinutes(minutes)
        }

        override fun setTime(calendar: GameCalendar) {
            _time.value = calendar
        }
    }

    private class FakeCompanyRepository : CompanyRepository {
        var company: Company? = Company("c1", "PO TransNusantara", Money.rupiah(1_000_000_000L), 100)
        override fun getCompany(): Flow<Company?> = flowOf(company)
        override suspend fun createCompany(name: String, initialBalance: Money): Company = company!!
        override suspend fun updateBalance(newBalance: Money) {
            company = company?.copy(balance = newBalance)
        }
        override suspend fun addBalance(amount: Money) {
            val current = company?.balance ?: Money.ZERO
            company = company?.copy(balance = current + amount)
        }
        override suspend fun deductBalance(amount: Money): Boolean = true
        override suspend fun resetCompany(name: String, initialBalance: Money) {}
    }

    private class FakeFleetRepository : FleetRepository {
        var buses = listOf<Bus>()
        override fun getOwnedBuses(): Flow<List<Bus>> = flowOf(buses)
        override suspend fun getBusById(busId: String): Bus? = buses.find { it.id == busId }
        override suspend fun addBus(bus: Bus) {}
        override suspend fun updateBus(bus: Bus) {
            buses = buses.map { if (it.id == bus.id) bus else it }
        }
        override suspend fun removeBus(id: String) {}
        override suspend fun removeAllBuses() {}
    }

    private class FakeDriverRepository : DriverRepository {
        var drivers = listOf<Driver>()
        override fun getAllDrivers(): Flow<List<Driver>> = flowOf(drivers)
        override fun getHiredDrivers(): Flow<List<Driver>> = flowOf(drivers.filter { it.isHired })
        override suspend fun getDriverById(id: String): Driver? = drivers.find { it.id == id }
        override suspend fun hireDriver(driverId: String): Boolean = true
        override suspend fun assignDriverToBus(driverId: String, busId: String): Boolean = true
        override suspend fun unassignDriver(driverId: String) {}
        override suspend fun fireDriver(driverId: String) {}
        override suspend fun resetAllDrivers() {}
        override suspend fun seedCandidateDriversIfEmpty() {}
        override suspend fun setPendingRelease(driverId: String, isPending: Boolean) {}
}

    private class FakeRouteRepository(val routesList: List<BusRoute>) : RouteRepository {
        override fun getAllSegments(): List<RouteSegment> = emptyList()
        override fun getSegmentById(id: String): RouteSegment? = null
        override fun getSegmentsBetween(fromTerminalId: String, toTerminalId: String): List<RouteSegment> = emptyList()
        override fun getAllPredefinedRoutes(): List<BusRoute> = routesList
        override fun getAllRoutes(): List<BusRoute> = routesList
        override fun getRouteById(id: String): BusRoute? = routesList.find { it.id == id }
        override suspend fun saveRoute(route: BusRoute) {}
        override fun findRoutes(originTerminalId: String, destinationTerminalId: String, preferredPathType: RoutePathType?): List<BusRoute> = routesList
        override fun getConnectedTerminalIds(terminalId: String): List<String> = emptyList()
        override suspend fun deleteRoute(routeId: String) {}
        override suspend fun deleteAllCustomRoutes() {}
    }

    private class FakeRouteScheduleRepository : RouteScheduleRepository {
        override fun getSchedulesForRoute(routeId: String): Flow<List<RouteSchedule>> = flowOf(emptyList())
        override fun getAllSchedules(): Flow<List<RouteSchedule>> = flowOf(emptyList())
        override suspend fun getSchedulesListForRoute(routeId: String): List<RouteSchedule> = emptyList()
        override suspend fun getScheduleById(id: String): RouteSchedule? = null
        override suspend fun addSchedule(schedule: RouteSchedule) = Result.success(Unit)
        override suspend fun updateSchedule(schedule: RouteSchedule) = Result.success(Unit)
        override suspend fun assignBusToSchedule(scheduleId: String, busId: String): Boolean = true
        override suspend fun unassignBusFromSchedule(scheduleId: String) {}
        override suspend fun deleteSchedule(scheduleId: String) {}
        override suspend fun deleteAllSchedules() {}
        override suspend fun checkConflict(routeId: String, direction: RouteDirection, hour: Int, minute: Int, excludeScheduleId: String?): Boolean = false
        override suspend fun getSchedulesForBus(busId: String): List<com.example.busmanager.domain.route.model.RouteSchedule> = emptyList()
    override fun getSchedulesForBusFlow(busId: String): kotlinx.coroutines.flow.Flow<List<com.example.busmanager.domain.route.model.RouteSchedule>> = kotlinx.coroutines.flow.emptyFlow()
    override suspend fun deleteSchedulesForBus(busId: String) {}
    override suspend fun deleteSchedulesForRoute(routeId: String) {}
}

    private class FakeRoutePricingRepository : RoutePricingRepository {
        var pricings = listOf<RoutePricing>()
        override fun getPricingsForRoute(routeId: String): Flow<List<RoutePricing>> = flowOf(pricings.filter { it.routeId == routeId })
        override suspend fun getPricingsListForRoute(routeId: String): List<RoutePricing> = pricings.filter { it.routeId == routeId }
        override fun getAllPricingsFlow(): Flow<List<RoutePricing>> = flowOf(pricings)
        override suspend fun getAllPricings(): List<RoutePricing> = pricings
        override suspend fun savePricing(pricing: RoutePricing) {}
        override suspend fun savePricings(pricings: List<RoutePricing>) {}
        override suspend fun getPricingBetween(routeId: String, originTerminalId: String, destinationTerminalId: String): RoutePricing? = null
        override suspend fun generateAndSaveDefaultPricings(route: BusRoute): List<RoutePricing> = emptyList()
        override suspend fun deleteAllPricings() {}
}

    private class FakeFinancialTransactionRepository : FinancialTransactionRepository {
        val transactions = mutableListOf<FinancialTransaction>()
        override fun getAllTransactionsFlow(): Flow<List<FinancialTransaction>> = flowOf(transactions)
        override suspend fun getTransactions(): List<FinancialTransaction> = transactions.toList()
        override suspend fun recordTransaction(transaction: FinancialTransaction) {
            transactions.add(transaction)
        }
        override suspend fun clearTransactions() {
            transactions.clear()
        }
    }

    private class FakeTerminalPassengerRepository : TerminalPassengerRepository {
        val records = mutableListOf<TerminalPassengerRecord>()
        override fun getAllRecordsFlow(): Flow<List<TerminalPassengerRecord>> = flowOf(records)
        override fun getRecordsByTerminalAndDateFlow(terminalId: String, day: Int, month: Int, year: Int): Flow<List<TerminalPassengerRecord>> = flowOf(records)
        override fun getRecordsByTerminalFlow(terminalId: String): Flow<List<TerminalPassengerRecord>> = flowOf(records)
        override suspend fun recordPassengerExchange(record: TerminalPassengerRecord) {
            records.add(record)
        }
        override suspend fun getAvailableDates(): List<Triple<Int, Int, Int>> = emptyList()
        override suspend fun clearAllRecords() {
            records.clear()
        }
    }
}
