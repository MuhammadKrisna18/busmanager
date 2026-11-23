package com.example.busmanager.data

import com.example.busmanager.data.local.dao.CompanyDao
import com.example.busmanager.data.local.entity.CompanyEntity
import com.example.busmanager.data.repository.CompanyRepositoryImpl
import com.example.busmanager.domain.company.model.Money
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CompanyRepositoryTest {

    private lateinit var fakeDao: FakeCompanyDao
    private lateinit var repository: CompanyRepositoryImpl

    @Before
    fun setUp() {
        fakeDao = FakeCompanyDao()
        repository = CompanyRepositoryImpl(fakeDao)
    }

    @Test
    fun createCompanyInitializesBalanceCorrectly() = runBlocking {
        val initialMoney = Money.rupiah(1_500_000_000)
        val company = repository.createCompany("PO Sumber Kencono", initialMoney)

        assertEquals("PO Sumber Kencono", company.name)
        assertEquals(initialMoney, company.balance)

        val retrieved = repository.getCompany().first()
        assertNotNull(retrieved)
        assertEquals(initialMoney, retrieved?.balance)
    }

    @Test
    fun deductBalanceSucceedsWhenAffordable() = runBlocking {
        repository.createCompany("PO Haryanto", Money.rupiah(1_500_000_000))
        val cost = Money.rupiah(500_000_000)

        val success = repository.deductBalance(cost)

        assertTrue(success)
        val updated = repository.getCompany().first()
        assertEquals(1_000_000_000L, updated?.balance?.amount)
    }

    @Test
    fun deductBalanceFailsWhenInsufficientFunds() = runBlocking {
        repository.createCompany("PO Shantika", Money.rupiah(500_000_000))
        val cost = Money.rupiah(800_000_000)

        val success = repository.deductBalance(cost)

        assertFalse(success)
        val updated = repository.getCompany().first()
        assertEquals(500_000_000L, updated?.balance?.amount)
    }

    @Test
    fun addBalanceIncreasesAmount() = runBlocking {
        repository.createCompany("PO Rosalia Indah", Money.rupiah(1_000_000_000))
        repository.addBalance(Money.rupiah(250_000_000))

        val updated = repository.getCompany().first()
        assertEquals(1_250_000_000L, updated?.balance?.amount)
    }

    private class FakeCompanyDao : CompanyDao {
        private val state = MutableStateFlow<CompanyEntity?>(null)

        override fun getCompanyFlow(id: String): Flow<CompanyEntity?> = state

        override suspend fun getCompany(id: String): CompanyEntity? = state.value

        override suspend fun insertCompany(company: CompanyEntity) {
            state.value = company
        }

        override suspend fun updateCompany(company: CompanyEntity) {
            state.value = company
        }

        override suspend fun updateBalance(newBalance: Long, id: String): Int {
            state.value = state.value?.copy(balance = newBalance)
            return 1
        }
    }
}
