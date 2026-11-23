package com.example.busmanager.presentation.dashboard

import com.example.busmanager.domain.company.model.Company
import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.company.repository.CompanyRepository
import com.example.busmanager.domain.company.usecase.GetCompanyUseCase
import com.example.busmanager.domain.company.usecase.StartNewGameUseCase
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.fleet.repository.FleetRepository
import com.example.busmanager.domain.fleet.usecase.GetOwnedFleetUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeScreenViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeCompanyRepo: FakeCompanyRepository
    private lateinit var fakeFleetRepo: FakeFleetRepository
    private lateinit var viewModel: HomeScreenViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeCompanyRepo = FakeCompanyRepository()
        fakeFleetRepo = FakeFleetRepository()

        val getCompanyUseCase = GetCompanyUseCase(fakeCompanyRepo)
        val getOwnedFleetUseCase = GetOwnedFleetUseCase(fakeFleetRepo)
        val startNewGameUseCase = StartNewGameUseCase(fakeCompanyRepo)

        viewModel = HomeScreenViewModel(
            getCompanyUseCase,
            getOwnedFleetUseCase,
            startNewGameUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialStateLoadsCompanyData() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertFalse(state.isLoading)
        assertEquals("PO TransNusantara", state.company?.name)
        assertEquals(Money.rupiah(1_500_000_000), state.company?.balance)
    }

    @Test
    fun togglePlaySimulationChangesState() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isPlaying)
        viewModel.togglePlaySimulation()
        assertTrue(viewModel.uiState.value.isPlaying)
        viewModel.togglePlaySimulation()
        assertFalse(viewModel.uiState.value.isPlaying)
    }

    private class FakeCompanyRepository : CompanyRepository {
        private val state = MutableStateFlow<Company?>(null)

        override fun getCompany(): Flow<Company?> = state

        override suspend fun createCompany(name: String, initialBalance: Money): Company {
            val created = Company(id = "comp-1", name = name, balance = initialBalance)
            state.value = created
            return created
        }

        override suspend fun updateBalance(newBalance: Money) {
            state.value = state.value?.copy(balance = newBalance)
        }

        override suspend fun deductBalance(amount: Money): Boolean {
            val current = state.value ?: return false
            if (!current.balance.canAfford(amount)) return false
            state.value = current.copy(balance = current.balance - amount)
            return true
        }

        override suspend fun addBalance(amount: Money) {
            val current = state.value ?: return
            state.value = current.copy(balance = current.balance + amount)
        }
    }

    private class FakeFleetRepository : FleetRepository {
        private val state = MutableStateFlow<List<Bus>>(emptyList())

        override fun getOwnedBuses(): Flow<List<Bus>> = state

        override suspend fun getBusById(id: String): Bus? = null

        override suspend fun addBus(bus: Bus) {
            state.value = state.value + bus
        }

        override suspend fun updateBus(bus: Bus) {}

        override suspend fun removeBus(busId: String) {}
    }
}
