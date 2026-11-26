package com.example.busmanager.presentation.finance

import com.example.busmanager.data.local.datasource.ChassisDataSource
import com.example.busmanager.data.local.datasource.KaroseriBodyDataSource
import com.example.busmanager.data.local.datasource.RouteDataSource
import com.example.busmanager.data.local.datasource.TerminalDataSource
import com.example.busmanager.data.repository.RouteCatalogRepositoryImpl
import com.example.busmanager.domain.company.model.Company
import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.company.repository.CompanyRepository
import com.example.busmanager.domain.finance.model.FinancialTransaction
import com.example.busmanager.domain.finance.model.TransactionCategory
import com.example.busmanager.domain.finance.model.TransactionType
import com.example.busmanager.domain.finance.repository.FinancialTransactionRepository
import com.example.busmanager.domain.finance.usecase.GetFinancialTransactionsUseCase
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.fleet.repository.FleetRepository
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FinanceViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeCompanyRepo: FakeCompanyRepository
    private lateinit var fakeTxRepo: FakeFinancialTransactionRepository
    private lateinit var fakeFleetRepo: FakeFleetRepository
    private lateinit var routeRepo: RouteCatalogRepositoryImpl
    private lateinit var viewModel: FinanceViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeCompanyRepo = FakeCompanyRepository()
        fakeTxRepo = FakeFinancialTransactionRepository()
        fakeFleetRepo = FakeFleetRepository()

        val terminalDataSource = TerminalDataSource()
        val routeDataSource = RouteDataSource(terminalDataSource)
        routeRepo = RouteCatalogRepositoryImpl(routeDataSource)

        val sampleBus = Bus(
            id = "bus-1",
            busCode = "TN-01",
            chassis = ChassisDataSource().getChassisList().first(),
            body = KaroseriBodyDataSource().getKaroseriBodyList().first()
        )
        fakeFleetRepo.setBuses(listOf(sampleBus))

        val getTxUseCase = GetFinancialTransactionsUseCase(fakeTxRepo)

        viewModel = FinanceViewModel(
            companyRepository = fakeCompanyRepo,
            getFinancialTransactionsUseCase = getTxUseCase,
            fleetRepository = fakeFleetRepo,
            routeRepository = routeRepo
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_loadsCompanyAndEmptyTransactions() = runTest {
        advanceUntilIdle()
        val state = viewModel.uiState.value

        assertEquals("PO TransNusantara", state.company?.name)
        assertEquals(Money.rupiah(500_000_000L), state.currentBalance)
        assertTrue(state.transactions.isEmpty())
        assertEquals(TransactionTypeFilter.ALL, state.selectedTypeFilter)
        assertNull(state.selectedBusId)
        assertEquals(1, state.availableBuses.size)
    }

    @Test
    fun transactionsCalculation_incomeExpenseAndNetProfit() = runTest {
        val tx1 = FinancialTransaction(
            id = "tx-1",
            dateText = "24/11/2026",
            timeText = "08:00",
            day = 24,
            month = 11,
            year = 2026,
            type = TransactionType.INCOME,
            category = TransactionCategory.TICKET_SALE,
            amount = Money.rupiah(300_000L),
            busId = "bus-1",
            busCode = "TN-01",
            description = "Tiket"
        )
        val tx2 = FinancialTransaction(
            id = "tx-2",
            dateText = "24/11/2026",
            timeText = "00:00",
            day = 24,
            month = 11,
            year = 2026,
            type = TransactionType.EXPENSE,
            category = TransactionCategory.DRIVER_WAGE,
            amount = Money.rupiah(100_000L),
            busId = "bus-1",
            busCode = "TN-01",
            description = "Gaji"
        )
        fakeTxRepo.emitTransactions(listOf(tx1, tx2))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.filteredTransactions.size)
        assertEquals(Money.rupiah(300_000L), state.totalIncome)
        assertEquals(Money.rupiah(100_000L), state.totalExpense)
        assertEquals(200_000L, state.netProfitAmount)
        assertEquals("+${Money.rupiah(200_000L).formatRupiah()}", state.netProfitFormatted)
        assertTrue(state.isProfitable)
    }

    @Test
    fun filterByType_filtersTransactionsCorrectly() = runTest {
        val tx1 = FinancialTransaction(
            id = "tx-1",
            dateText = "24/11/2026",
            timeText = "08:00",
            day = 24,
            month = 11,
            year = 2026,
            type = TransactionType.INCOME,
            category = TransactionCategory.TICKET_SALE,
            amount = Money.rupiah(300_000L),
            description = "Tiket"
        )
        val tx2 = FinancialTransaction(
            id = "tx-2",
            dateText = "24/11/2026",
            timeText = "00:00",
            day = 24,
            month = 11,
            year = 2026,
            type = TransactionType.EXPENSE,
            category = TransactionCategory.DRIVER_WAGE,
            amount = Money.rupiah(100_000L),
            description = "Gaji"
        )
        fakeTxRepo.emitTransactions(listOf(tx1, tx2))
        advanceUntilIdle()

        viewModel.setTypeFilter(TransactionTypeFilter.INCOME)
        val stateIncome = viewModel.uiState.value
        assertEquals(1, stateIncome.filteredTransactions.size)
        assertEquals("tx-1", stateIncome.filteredTransactions.first().id)

        viewModel.setTypeFilter(TransactionTypeFilter.EXPENSE)
        val stateExpense = viewModel.uiState.value
        assertEquals(1, stateExpense.filteredTransactions.size)
        assertEquals("tx-2", stateExpense.filteredTransactions.first().id)

        viewModel.setTypeFilter(TransactionTypeFilter.ALL)
        assertEquals(2, viewModel.uiState.value.filteredTransactions.size)
    }

    @Test
    fun filterByBus_filtersTransactionsCorrectly() = runTest {
        val tx1 = FinancialTransaction(
            id = "tx-1",
            dateText = "24/11/2026",
            timeText = "08:00",
            day = 24,
            month = 11,
            year = 2026,
            type = TransactionType.INCOME,
            category = TransactionCategory.TICKET_SALE,
            amount = Money.rupiah(300_000L),
            busId = "bus-1",
            routeId = "route-a",
            description = "Tiket A"
        )
        val tx2 = FinancialTransaction(
            id = "tx-2",
            dateText = "24/11/2026",
            timeText = "09:00",
            day = 24,
            month = 11,
            year = 2026,
            type = TransactionType.INCOME,
            category = TransactionCategory.TICKET_SALE,
            amount = Money.rupiah(400_000L),
            busId = "bus-2",
            routeId = "route-b",
            description = "Tiket B"
        )
        fakeTxRepo.emitTransactions(listOf(tx1, tx2))
        advanceUntilIdle()

        viewModel.setBusFilter("bus-1")
        assertEquals(1, viewModel.uiState.value.filteredTransactions.size)
        assertEquals("tx-1", viewModel.uiState.value.filteredTransactions.first().id)

        viewModel.setBusFilter("bus-2")
        assertEquals(1, viewModel.uiState.value.filteredTransactions.size)
        assertEquals("tx-2", viewModel.uiState.value.filteredTransactions.first().id)

        viewModel.resetFilters()
        assertEquals(2, viewModel.uiState.value.filteredTransactions.size)
    }

    private class FakeCompanyRepository : CompanyRepository {
        private var company: Company? = Company(
            id = "comp-1",
            name = "PO TransNusantara",
            balance = Money.rupiah(500_000_000L),
            reputation = 100
        )
        private val flow = MutableStateFlow(company)

        override fun getCompany(): Flow<Company?> = flow
        override suspend fun createCompany(name: String, initialBalance: Money): Company = company!!
        override suspend fun updateBalance(newBalance: Money) {
            company = company?.copy(balance = newBalance)
            flow.value = company
        }
        override suspend fun addBalance(amount: Money) {
            val curr = company?.balance?.amount ?: 0L
            updateBalance(Money.rupiah(curr + amount.amount))
        }
        override suspend fun deductBalance(amount: Money): Boolean {
            val curr = company?.balance?.amount ?: 0L
            updateBalance(Money.rupiah((curr - amount.amount).coerceAtLeast(0L)))
            return true
        }
        override suspend fun resetCompany(name: String, initialBalance: com.example.busmanager.domain.company.model.Money) {}
}

    private class FakeFinancialTransactionRepository : FinancialTransactionRepository {
        private val list = mutableListOf<FinancialTransaction>()
        private val flow = MutableStateFlow<List<FinancialTransaction>>(emptyList())

        fun emitTransactions(txs: List<FinancialTransaction>) {
            list.clear()
            list.addAll(txs)
            flow.value = list.toList()
        }

        override fun getAllTransactionsFlow(): Flow<List<FinancialTransaction>> = flow
        override suspend fun getTransactions(): List<FinancialTransaction> = list.toList()
        override suspend fun recordTransaction(transaction: FinancialTransaction) {
            list.add(0, transaction)
            flow.value = list.toList()
        }
        override suspend fun clearTransactions() {
            list.clear()
            flow.value = emptyList()
        }
    }

    private class FakeFleetRepository : FleetRepository {
        private val buses = mutableListOf<Bus>()
        private val flow = MutableStateFlow<List<Bus>>(emptyList())

        fun setBuses(list: List<Bus>) {
            buses.clear()
            buses.addAll(list)
            flow.value = buses.toList()
        }

        override fun getOwnedBuses(): Flow<List<Bus>> = flow
        override suspend fun getBusById(id: String): Bus? = buses.find { it.id == id }
        override suspend fun addBus(bus: Bus) {}
        override suspend fun updateBus(bus: Bus) {}
        override suspend fun removeBus(busId: String) {}
        override suspend fun removeAllBuses() {}
}
}
