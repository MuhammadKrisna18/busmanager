package com.example.busmanager.domain.usecase

import com.example.busmanager.domain.company.model.Company
import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.company.repository.CompanyRepository
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
import com.example.busmanager.domain.route.model.Terminal
import com.example.busmanager.domain.route.model.TerminalType
import com.example.busmanager.domain.simulation.model.ActiveBusTrip
import com.example.busmanager.domain.simulation.model.GameCalendar
import com.example.busmanager.domain.simulation.model.PassengerQueueManager
import com.example.busmanager.domain.simulation.model.RouteDirection
import com.example.busmanager.domain.simulation.repository.SimulationTripRepository
import com.example.busmanager.domain.simulation.usecase.ProcessCompletedTripsUseCase
import com.example.busmanager.domain.terminal.model.TerminalPassengerRecord
import com.example.busmanager.domain.terminal.repository.TerminalPassengerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ProcessCompletedTripsUseCaseTest {

    private lateinit var fakeCompanyRepo: FakeCompanyRepository
    private lateinit var fakeFleetRepo: FakeFleetRepository
    private lateinit var fakeTripRepo: FakeSimulationTripRepository
    private lateinit var fakeTxRepo: FakeFinancialTransactionRepository
    private lateinit var fakePassengerRepo: FakeTerminalPassengerRepository
    private lateinit var useCase: ProcessCompletedTripsUseCase

    private val originTerminal = Terminal(
        id = "t-sby",
        name = "Purabaya",
        city = "Surabaya",
        type = TerminalType.TIPE_A,
        coordinate = MapCoordinate(0.5f, 0.5f, -7.25, 112.75),
        retributionFee = Money.rupiah(5_000L)
    )

    private val destTerminal = Terminal(
        id = "t-mlg",
        name = "Arjosari",
        city = "Malang",
        type = TerminalType.TIPE_A,
        coordinate = MapCoordinate(0.6f, 0.6f, -7.98, 112.63),
        retributionFee = Money.rupiah(5_000L)
    )

    private val testRoute = BusRoute(
        id = "r-1",
        name = "Surabaya - Malang",
        originTerminal = originTerminal,
        destinationTerminal = destTerminal,
        segments = emptyList(),
        pathType = RoutePathType.TOLL
    )

    private val testBus = Bus(
        id = "bus-1",
        busCode = "TN-01",
        chassis = Chassis(
            id = "c-1",
            brand = ChassisBrand.HINO,
            modelName = "Hino RK8",
            engineLocation = EngineLocation.REAR,
            suspension = SuspensionType.LEAF_SPRING,
            axleType = AxleType.SINGLE_4X2,
            transmission = TransmissionType.MANUAL,
            horsepower = 260,
            torqueNm = 750,
            price = Money.rupiah(1_000_000_000L)
        ),
        body = KaroseriBody(
            id = "k-1",
            brand = KaroseriBrand.ADIPUTRO,
            modelName = "Jetbus 3+ SHD",
            bodyType = BusBodyType.SHD,
            busClass = BusClass.EKSEKUTIF,
            passengerCapacity = 40,
            luggageCapacityKg = 1500,
            comfortRating = 8,
            price = Money.rupiah(500_000_000L)
        ),
        currentTerminalId = originTerminal.id,
        isOperating = true,
        routeId = testRoute.id
    )

    @Before
    fun setUp() {
        fakeCompanyRepo = FakeCompanyRepository()
        fakeFleetRepo = FakeFleetRepository(testBus)
        fakeTripRepo = FakeSimulationTripRepository()
        fakeTxRepo = FakeFinancialTransactionRepository()
        fakePassengerRepo = FakeTerminalPassengerRepository()

        useCase = ProcessCompletedTripsUseCase(
            fleetRepository = fakeFleetRepo,
            simulationTripRepository = fakeTripRepo,
            companyRepository = fakeCompanyRepo,
            financialTransactionRepository = fakeTxRepo,
            terminalPassengerRepository = fakePassengerRepo,
            routeScheduleRepository = null,
            driverRepository = null
        )
    }

    @Test
    fun processCompletedTripsUpdatesBusAndCreditsFinances() = runBlocking {
        val completedTrip = ActiveBusTrip(
            id = "trip-1",
            busId = testBus.id,
            busCode = testBus.busCode,
            route = testRoute,
            direction = RouteDirection.ORIGIN_TO_DESTINATION,
            departureTime = GameCalendar.initial(),
            estimatedArrivalMinutes = 90,
            elapsedMinutes = 90,
            progressFraction = 1.0f,
            currentCoordinate = destTerminal.coordinate,
            passengerCount = 30,
            ticketEarnings = Money.rupiah(3_000_000L),
            isCompleted = true,
            maxCapacity = 40,
            visitedTransitStopIndices = emptySet(),
            totalBoardedPassengers = 30,
            totalAlightedPassengers = 30
        )
        fakeTripRepo.setTrips(listOf(completedTrip))

        val calendar = GameCalendar(2026, 11, 24, 10, 0)
        useCase(listOf(completedTrip), calendar)

        val updatedBus = fakeFleetRepo.getBusById(testBus.id)
        assertFalse(updatedBus!!.isOperating)
        assertEquals(destTerminal.id, updatedBus.currentTerminalId)

        assertEquals(2, fakeTxRepo.transactions.size)
        val incomeTx = fakeTxRepo.transactions[0]
        val wageTx = fakeTxRepo.transactions[1]
        assertEquals(Money.rupiah(3_000_000L), incomeTx.amount)
        assertEquals(Money.rupiah(900_000L), wageTx.amount)

        assertEquals(Money.rupiah(2_100_000L), fakeCompanyRepo.currentBalance)
        assertEquals(1, fakePassengerRepo.records.size)
        assertEquals(destTerminal.id, fakePassengerRepo.records[0].terminalId)
        assertEquals(30, fakePassengerRepo.records[0].alightedCount)

        assertTrue(fakeTripRepo.activeTrips.value.isEmpty())
    }

    private class FakeCompanyRepository : CompanyRepository {
        var currentBalance = Money.rupiah(0L)

        override fun getCompany(): Flow<Company?> = flowOf(Company("c-1", "Test", currentBalance))
        override suspend fun createCompany(name: String, initialBalance: Money): Company {
            currentBalance = initialBalance
            return Company("c-1", name, currentBalance)
        }
        override suspend fun updateBalance(newBalance: Money) { currentBalance = newBalance }
        override suspend fun deductBalance(amount: Money): Boolean {
            currentBalance -= amount
            return true
        }
        override suspend fun addBalance(amount: Money) { currentBalance += amount }
        override suspend fun resetCompany(name: String, initialBalance: com.example.busmanager.domain.company.model.Money) {}
}

    private class FakeFleetRepository(initialBus: Bus) : FleetRepository {
        private val buses = mutableMapOf(initialBus.id to initialBus)

        override fun getOwnedBuses(): Flow<List<Bus>> = flowOf(buses.values.toList())
        override suspend fun getBusById(id: String): Bus? = buses[id]
        override suspend fun updateBus(bus: Bus) { buses[bus.id] = bus }
        override suspend fun addBus(bus: Bus) { buses[bus.id] = bus }
        override suspend fun removeBus(busId: String) { buses.remove(busId) }
        override suspend fun removeAllBuses() { buses.clear() }
    }

    private class FakeFinancialTransactionRepository : FinancialTransactionRepository {
        val transactions = mutableListOf<FinancialTransaction>()

        override fun getAllTransactionsFlow(): Flow<List<FinancialTransaction>> = flowOf(transactions)
        override suspend fun getTransactions(): List<FinancialTransaction> = transactions.toList()
        override suspend fun recordTransaction(transaction: FinancialTransaction) {
            transactions.add(transaction)
        }
        override suspend fun clearTransactions() { transactions.clear() }
    }

    private class FakeTerminalPassengerRepository : TerminalPassengerRepository {
        val records = mutableListOf<TerminalPassengerRecord>()

        override fun getAllRecordsFlow(): Flow<List<TerminalPassengerRecord>> = flowOf(records)
        override fun getRecordsByTerminalAndDateFlow(terminalId: String, day: Int, month: Int, year: Int): Flow<List<TerminalPassengerRecord>> = flowOf(records)
        override fun getRecordsByTerminalFlow(terminalId: String): Flow<List<TerminalPassengerRecord>> = flowOf(records)
        override suspend fun recordPassengerExchange(record: TerminalPassengerRecord) { records.add(record) }
        override suspend fun getAvailableDates(): List<Triple<Int, Int, Int>> = emptyList()
        override suspend fun clearAllRecords() { records.clear() }
    }

    private class FakeSimulationTripRepository : SimulationTripRepository {
        private val _trips = MutableStateFlow<List<ActiveBusTrip>>(emptyList())
        override val activeTrips: StateFlow<List<ActiveBusTrip>> = _trips.asStateFlow()
        override val passengerQueueManager: PassengerQueueManager = PassengerQueueManager()

        fun setTrips(list: List<ActiveBusTrip>) { _trips.value = list }

        override fun dispatchBus(bus: Bus, route: BusRoute, direction: RouteDirection, currentTime: GameCalendar): ActiveBusTrip {
            error("Not implemented")
        }
        override fun dispatchBus(bus: Bus, route: BusRoute, direction: RouteDirection, currentTime: GameCalendar, headwayMinutes: Int, transitDwellMinutes: Int, ticketPricePerPassenger: Money?): ActiveBusTrip {
            error("Not implemented")
        }
        override fun advanceTripsTime(advanceMinutes: Int): List<ActiveBusTrip> = _trips.value
        override fun advanceTripsTime(advanceMinutes: Int, currentTime: GameCalendar?): List<ActiveBusTrip> = _trips.value
        override fun cancelTrip(tripId: String) { _trips.value = _trips.value.filterNot { it.id == tripId } }
        override fun clearTrips() { _trips.value = emptyList() }
        override fun markCancelNextSchedule(tripId: String, cancel: Boolean) {}
        override fun completeTrip(tripId: String) { _trips.value = _trips.value.filterNot { it.id == tripId } }
    }
}
