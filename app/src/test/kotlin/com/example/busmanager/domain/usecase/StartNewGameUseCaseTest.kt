package com.example.busmanager.domain.usecase

import com.example.busmanager.domain.company.model.Company
import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.company.repository.CompanyRepository
import com.example.busmanager.domain.company.usecase.StartNewGameUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
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
        assertEquals(Money.rupiah(1_500_000_000), company.balance)
    }

    @Test
    fun startNewGameDoesNotOverwriteExistingCompany() = runBlocking {
        startNewGameUseCase("PO Pertama")
        val existing = startNewGameUseCase("PO Kedua")

        assertEquals("PO Pertama", existing.name)
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
}
