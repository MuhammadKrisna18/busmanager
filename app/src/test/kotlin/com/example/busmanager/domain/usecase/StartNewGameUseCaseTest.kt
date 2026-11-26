package com.example.busmanager.domain.usecase

import com.example.busmanager.domain.company.model.Company
import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.company.repository.CompanyRepository
import com.example.busmanager.domain.company.usecase.StartNewGameUseCase
import com.example.busmanager.domain.driver.model.Driver
import com.example.busmanager.domain.driver.repository.DriverRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class StartNewGameUseCaseTest {

    private lateinit var fakeCompanyRepo: FakeCompanyRepository
    private lateinit var startNewGameUseCase: StartNewGameUseCase

    @Before
    fun setUp() {
        fakeCompanyRepo = FakeCompanyRepository()
        startNewGameUseCase = StartNewGameUseCase(fakeCompanyRepo)
    }

    @Test
    fun startNewGameInitializesDefaultStartingBalance() = runBlocking {
        val company = startNewGameUseCase("PO Harapan Jaya")

        assertEquals("PO Harapan Jaya", company.name)
        assertEquals(Money.rupiah(50_000_000_000L), company.balance)
    }

    @Test
    fun startNewGameDoesNotOverwriteExistingCompany() = runBlocking {
        startNewGameUseCase("PO Pertama")
        val existing = startNewGameUseCase("PO Kedua")

        assertEquals("PO Pertama", existing.name)
    }

    @Test
    fun startNewGameSeedsCandidateDriversWhenDriverRepoProvided() = runBlocking {
        var seeded = false
        val fakeDriverRepo = object : DriverRepository {
            override fun getAllDrivers(): Flow<List<Driver>> = kotlinx.coroutines.flow.emptyFlow()
            override fun getHiredDrivers(): Flow<List<Driver>> = kotlinx.coroutines.flow.emptyFlow()
            override suspend fun getDriverById(id: String): Driver? = null
            override suspend fun hireDriver(driverId: String): Boolean = true
            override suspend fun assignDriverToBus(driverId: String, busId: String): Boolean = true
            override suspend fun unassignDriver(driverId: String) {}
            override suspend fun fireDriver(driverId: String) {}
            override suspend fun seedCandidateDriversIfEmpty() {
                seeded = true
            }
            override suspend fun resetAllDrivers() {}
            override suspend fun setPendingRelease(driverId: String, isPending: Boolean) {}
}
        val useCase = StartNewGameUseCase(fakeCompanyRepo, fakeDriverRepo)
        useCase("PO Nusantara")
        assertTrue(seeded)
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
        override suspend fun resetCompany(name: String, initialBalance: com.example.busmanager.domain.company.model.Money) {}
}
}
