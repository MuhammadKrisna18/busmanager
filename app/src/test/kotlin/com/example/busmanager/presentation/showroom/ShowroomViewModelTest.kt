package com.example.busmanager.presentation.showroom

import com.example.busmanager.data.local.datasource.ChassisDataSource
import com.example.busmanager.data.local.datasource.KaroseriBodyDataSource
import com.example.busmanager.data.repository.ShowroomCatalogRepositoryImpl
import com.example.busmanager.domain.company.model.Company
import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.company.repository.CompanyRepository
import com.example.busmanager.domain.company.usecase.GetCompanyUseCase
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.fleet.model.ChassisBrand
import com.example.busmanager.domain.fleet.model.KaroseriBrand
import com.example.busmanager.domain.fleet.repository.FleetRepository
import com.example.busmanager.domain.fleet.usecase.BuyBusUseCase
import com.example.busmanager.domain.fleet.usecase.GetOwnedFleetUseCase
import com.example.busmanager.domain.fleet.usecase.GetShowroomBusesUseCase
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
            balance = Money.rupiah(1_500_000_000)
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

    private val showroomRepository = ShowroomCatalogRepositoryImpl(
        chassisDataSource = ChassisDataSource(),
        bodyDataSource = KaroseriBodyDataSource()
    )

    private val getCompanyUseCase = GetCompanyUseCase(mockCompanyRepository)
    private val getShowroomBusesUseCase = GetShowroomBusesUseCase(showroomRepository)
    private val buyBusUseCase = BuyBusUseCase(mockCompanyRepository, mockFleetRepository)
    private val getOwnedFleetUseCase = GetOwnedFleetUseCase(mockFleetRepository)

    private lateinit var viewModel: ShowroomViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        companyFlow.value = Company(
            id = "comp-1",
            name = "PO TransNusantara",
            balance = Money.rupiah(1_500_000_000)
        )
        ownedBusesFlow.value = emptyList()

        viewModel = ShowroomViewModel(
            getCompanyUseCase = getCompanyUseCase,
            getShowroomBusesUseCase = getShowroomBusesUseCase,
            buyBusUseCase = buyBusUseCase,
            getOwnedFleetUseCase = getOwnedFleetUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_shouldLoadBusesAndCompanyBalance() = runTest {
        advanceUntilIdle()
        val state = viewModel.uiState.value

        assertNotNull(state.company)
        assertEquals(Money.rupiah(1_500_000_000), state.company?.balance)
        assertTrue(state.readyStockBuses.isNotEmpty())
        assertEquals(state.readyStockBuses.size, state.filteredBuses.size)
    }

    @Test
    fun setKaroseriFilter_shouldFilterCorrectly() = runTest {
        advanceUntilIdle()
        viewModel.setKaroseriFilter(KaroseriBrand.ADIPUTRO)

        val state = viewModel.uiState.value
        assertEquals(KaroseriBrand.ADIPUTRO, state.selectedKaroseriFilter)
        assertTrue(state.filteredBuses.all { it.body.brand == KaroseriBrand.ADIPUTRO })
    }

    @Test
    fun setChassisFilter_shouldFilterCorrectly() = runTest {
        advanceUntilIdle()
        viewModel.setChassisFilter(ChassisBrand.MERCEDES_BENZ)

        val state = viewModel.uiState.value
        assertEquals(ChassisBrand.MERCEDES_BENZ, state.selectedChassisFilter)
        assertTrue(state.filteredBuses.all { it.chassis.brand == ChassisBrand.MERCEDES_BENZ })
    }

    @Test
    fun openPurchaseDialog_shouldGenerateDefaultBusCode() = runTest {
        advanceUntilIdle()
        val bus = viewModel.uiState.value.readyStockBuses.first()

        viewModel.openPurchaseDialog(bus)

        val state = viewModel.uiState.value
        assertEquals(bus, state.selectedBusForPurchase)
        assertEquals("TN-01", state.customBusCode)
        assertNull(state.errorMessage)
    }

    @Test
    fun confirmPurchase_sufficientFunds_shouldDeductBalanceAndAddBus() = runTest {
        advanceUntilIdle()
        val affordableBus = viewModel.uiState.value.readyStockBuses.first {
            it.totalPrice.amount <= 1_500_000_000
        }

        viewModel.openPurchaseDialog(affordableBus)
        viewModel.updateBusCode("TN-01")
        viewModel.confirmPurchase()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.purchaseSuccessBus)
        assertEquals("TN-01", state.purchaseSuccessBus?.busCode)
        assertNull(state.selectedBusForPurchase)
        assertEquals(1, ownedBusesFlow.value.size)
        assertEquals("TN-01", ownedBusesFlow.value.first().busCode)
        assertTrue(state.company!!.balance.amount < 1_500_000_000)
    }

    @Test
    fun confirmPurchase_insufficientFunds_shouldSetErrorMessage() = runTest {
        advanceUntilIdle()
        val expensiveBus = viewModel.uiState.value.readyStockBuses.first {
            it.totalPrice.amount > 1_500_000_000
        }

        viewModel.openPurchaseDialog(expensiveBus)
        viewModel.confirmPurchase()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.errorMessage)
        assertNull(state.purchaseSuccessBus)
        assertEquals(0, ownedBusesFlow.value.size)
    }
}
