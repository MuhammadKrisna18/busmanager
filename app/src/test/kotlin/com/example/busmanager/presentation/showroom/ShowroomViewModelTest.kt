package com.example.busmanager.presentation.showroom

import com.example.busmanager.data.local.datasource.ChassisDataSource
import com.example.busmanager.data.local.datasource.KaroseriBodyDataSource
import com.example.busmanager.data.repository.ChassisCatalogRepositoryImpl
import com.example.busmanager.data.repository.KaroseriCatalogRepositoryImpl
import com.example.busmanager.domain.company.model.Company
import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.company.repository.CompanyRepository
import com.example.busmanager.domain.company.usecase.GetCompanyUseCase
import com.example.busmanager.domain.finance.model.FinancialTransaction
import com.example.busmanager.domain.finance.model.TransactionCategory
import com.example.busmanager.domain.finance.model.TransactionType
import com.example.busmanager.domain.finance.repository.FinancialTransactionRepository
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.fleet.model.BusClass
import com.example.busmanager.domain.fleet.model.ChassisBrand
import com.example.busmanager.domain.fleet.model.KaroseriBrand
import com.example.busmanager.domain.fleet.repository.FleetRepository
import com.example.busmanager.domain.fleet.usecase.BuyBusUseCase
import com.example.busmanager.domain.fleet.usecase.GetOwnedFleetUseCase
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
class ShowroomViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val companyFlow = MutableStateFlow<Company?>(
        Company(
            id = "comp-1",
            name = "PO TransNusantara",
            balance = Money.rupiah(2_500_000_000)
        )
    )

    private val mockCompanyRepository = object : CompanyRepository {
        override fun getCompany(): Flow<Company?> = companyFlow
        override suspend fun createCompany(name: String, initialBalance: Money): Company = companyFlow.value!!
        override suspend fun deductBalance(amount: Money): Boolean {
            val current = companyFlow.value ?: return false
            if (!current.balance.canAfford(amount)) return false
            companyFlow.value = current.copy(balance = current.balance - amount)
            return true
        }
        override suspend fun addBalance(amount: Money) {
            val current = companyFlow.value ?: return
            companyFlow.value = current.copy(balance = current.balance + amount)
        }
        override suspend fun updateBalance(newBalance: Money) {
            val current = companyFlow.value ?: return
            companyFlow.value = current.copy(balance = newBalance)
        }
    }

    private val ownedBusesFlow = MutableStateFlow<List<Bus>>(emptyList())
    private val mockFleetRepository = object : FleetRepository {
        override fun getOwnedBuses(): Flow<List<Bus>> = ownedBusesFlow
        override suspend fun getBusById(id: String): Bus? = null
        override suspend fun addBus(bus: Bus) {
            ownedBusesFlow.value = ownedBusesFlow.value + bus
        }
        override suspend fun updateBus(bus: Bus) {}
        override suspend fun removeBus(busId: String) {}
    }

    private val recordedTransactions = mutableListOf<FinancialTransaction>()
    private val fakeFinancialTransactionRepository = object : FinancialTransactionRepository {
        override fun getAllTransactionsFlow(): Flow<List<FinancialTransaction>> = MutableStateFlow(recordedTransactions)
        override suspend fun getTransactions(): List<FinancialTransaction> = recordedTransactions.toList()
        override suspend fun recordTransaction(transaction: FinancialTransaction) {
            recordedTransactions.add(transaction)
        }
        override suspend fun clearTransactions() {
            recordedTransactions.clear()
        }
    }

    private val chassisRepo = ChassisCatalogRepositoryImpl(ChassisDataSource())
    private val karoseriRepo = KaroseriCatalogRepositoryImpl(KaroseriBodyDataSource())

    private val getCompanyUseCase = GetCompanyUseCase(mockCompanyRepository)
    private val buyBusUseCase = BuyBusUseCase(mockCompanyRepository, mockFleetRepository)
    private val getOwnedFleetUseCase = GetOwnedFleetUseCase(mockFleetRepository)

    private lateinit var viewModel: ShowroomViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        companyFlow.value = Company(
            id = "comp-1",
            name = "PO TransNusantara",
            balance = Money.rupiah(2_500_000_000)
        )
        ownedBusesFlow.value = emptyList()
        recordedTransactions.clear()

        viewModel = ShowroomViewModel(
            getCompanyUseCase = getCompanyUseCase,
            buyBusUseCase = buyBusUseCase,
            getOwnedFleetUseCase = getOwnedFleetUseCase,
            chassisCatalogRepository = chassisRepo,
            karoseriCatalogRepository = karoseriRepo,
            financialTransactionRepository = fakeFinancialTransactionRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_shouldLoadChassisAndBodiesAndCompany() = runTest {
        advanceUntilIdle()
        val state = viewModel.uiState.value

        assertNotNull(state.company)
        assertEquals(Money.rupiah(2_500_000_000), state.company?.balance)
        assertTrue(state.allChassis.isNotEmpty())
        assertTrue(state.allBodies.isNotEmpty())
        assertEquals(ShowroomStep.CHASSIS, state.currentStep)
        assertEquals("TN-01", state.customBusCode)
    }

    @Test
    fun selectChassis_andFilterChassis_updatesState() = runTest {
        advanceUntilIdle()
        viewModel.setChassisBrandFilter(ChassisBrand.HINO)
        val filtered = viewModel.uiState.value.filteredChassis
        assertTrue(filtered.all { it.brand == ChassisBrand.HINO })

        val hinoRk8 = filtered.first { it.modelName.contains("RK8") }
        viewModel.selectChassis(hinoRk8)

        val state = viewModel.uiState.value
        assertEquals(hinoRk8.id, state.selectedChassis?.id)
        assertTrue(state.compatibleBodies.isNotEmpty())
    }

    @Test
    fun selectBody_andFilterKaroseri_updatesCompatibleBodies() = runTest {
        advanceUntilIdle()
        val hinoRk8 = viewModel.uiState.value.allChassis.first { it.modelName.contains("RK8") }
        viewModel.selectChassis(hinoRk8)

        viewModel.setKaroseriBrandFilter(KaroseriBrand.ADIPUTRO)
        val compatibleAdiputro = viewModel.uiState.value.compatibleBodies
        assertTrue(compatibleAdiputro.all { it.brand == KaroseriBrand.ADIPUTRO })

        val jb3 = compatibleAdiputro.first()
        viewModel.selectBody(jb3)

        assertEquals(jb3.id, viewModel.uiState.value.selectedBody?.id)
    }

    @Test
    fun selectBusClass_tailorsCapacityAndPrice() = runTest {
        advanceUntilIdle()
        val chassis = viewModel.uiState.value.allChassis.first()
        val body = viewModel.uiState.value.allBodies.first()
        viewModel.selectChassis(chassis)
        viewModel.selectBody(body)

        viewModel.selectBusClass(BusClass.SUITES_CLASS)
        val stateSuites = viewModel.uiState.value
        assertEquals(BusClass.SUITES_CLASS, stateSuites.selectedBusClass)
        assertTrue(stateSuites.calculatedCapacity < 35)
        assertEquals(Money.rupiah(140_000_000L), stateSuites.classPriceAddition)

        viewModel.selectBusClass(BusClass.EKONOMI)
        val stateEkonomi = viewModel.uiState.value
        assertEquals(BusClass.EKONOMI, stateEkonomi.selectedBusClass)
        assertTrue(stateEkonomi.calculatedCapacity >= 50)
        assertEquals(Money.rupiah(0L), stateEkonomi.classPriceAddition)
    }

    @Test
    fun stepNavigation_onlyAdvancesWhenPrerequisitesMet() = runTest {
        advanceUntilIdle()
        assertEquals(ShowroomStep.CHASSIS, viewModel.uiState.value.currentStep)

        viewModel.nextStep()
        assertEquals(ShowroomStep.CHASSIS, viewModel.uiState.value.currentStep)

        val chassis = viewModel.uiState.value.allChassis.first()
        viewModel.selectChassis(chassis)
        viewModel.nextStep()
        assertEquals(ShowroomStep.BODY, viewModel.uiState.value.currentStep)

        viewModel.nextStep()
        assertEquals(ShowroomStep.BODY, viewModel.uiState.value.currentStep)

        val body = viewModel.uiState.value.compatibleBodies.first()
        viewModel.selectBody(body)
        viewModel.nextStep()
        assertEquals(ShowroomStep.CLASS, viewModel.uiState.value.currentStep)

        viewModel.nextStep()
        assertEquals(ShowroomStep.REVIEW, viewModel.uiState.value.currentStep)

        viewModel.previousStep()
        assertEquals(ShowroomStep.CLASS, viewModel.uiState.value.currentStep)
    }

    @Test
    fun confirmPurchase_sufficientFunds_buysBusAndRecordsTransaction() = runTest {
        advanceUntilIdle()
        val chassis = viewModel.uiState.value.allChassis.first()
        viewModel.selectChassis(chassis)
        val body = viewModel.uiState.value.compatibleBodies.first()
        viewModel.selectBody(body)
        viewModel.selectBusClass(BusClass.EKSEKUTIF)
        viewModel.goToStep(ShowroomStep.REVIEW)
        viewModel.updateBusCode("TN-99")

        val investment = viewModel.uiState.value.totalInvestment
        assertTrue(viewModel.uiState.value.canAfford)

        viewModel.confirmPurchase()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.purchaseSuccessBus)
        assertEquals("TN-99", state.purchaseSuccessBus?.busCode)
        assertEquals(1, ownedBusesFlow.value.size)
        assertEquals("TN-99", ownedBusesFlow.value.first().busCode)

        val expectedBalance = 2_500_000_000L - investment.amount
        assertEquals(expectedBalance, state.company?.balance?.amount)

        val tx = recordedTransactions.find { it.category == TransactionCategory.BUS_PURCHASE }
        assertNotNull(tx)
        assertEquals(TransactionType.EXPENSE, tx?.type)
        assertEquals("TN-99", tx?.busCode)
        assertEquals(investment, tx?.amount)
    }

    @Test
    fun confirmPurchase_insufficientFunds_showsErrorMessage() = runTest {
        companyFlow.value = Company(
            id = "comp-1",
            name = "PO TransNusantara",
            balance = Money.rupiah(100_000_000L)
        )
        advanceUntilIdle()

        val chassis = viewModel.uiState.value.allChassis.first()
        viewModel.selectChassis(chassis)
        val body = viewModel.uiState.value.compatibleBodies.first()
        viewModel.selectBody(body)
        viewModel.goToStep(ShowroomStep.REVIEW)

        assertFalse(viewModel.uiState.value.canAfford)

        viewModel.confirmPurchase()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.errorMessage)
        assertNull(state.purchaseSuccessBus)
        assertEquals(0, ownedBusesFlow.value.size)
    }
}
