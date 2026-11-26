package com.example.busmanager.data

import com.example.busmanager.data.local.dao.FinancialTransactionDao
import com.example.busmanager.data.local.entity.FinancialTransactionEntity
import com.example.busmanager.data.repository.FinancialTransactionRepositoryImpl
import com.example.busmanager.domain.company.model.Company
import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.company.repository.CompanyRepository
import com.example.busmanager.domain.finance.model.FinancialTransaction
import com.example.busmanager.domain.finance.model.TransactionCategory
import com.example.busmanager.domain.finance.model.TransactionType
import com.example.busmanager.domain.finance.usecase.GetFinancialTransactionsUseCase
import com.example.busmanager.domain.finance.usecase.RecordFinancialTransactionUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FinancialTransactionRepositoryTest {

    private lateinit var fakeDao: FakeFinancialTransactionDao
    private lateinit var repository: FinancialTransactionRepositoryImpl
    private lateinit var fakeCompanyRepo: FakeCompanyRepository

    @Before
    fun setUp() {
        fakeDao = FakeFinancialTransactionDao()
        repository = FinancialTransactionRepositoryImpl(fakeDao)
        fakeCompanyRepo = FakeCompanyRepository(initialBalance = Money.rupiah(1_000_000L))
    }

    @Test
    fun recordTransaction_savesToDaoAndReadsBack() = runTest {
        val tx = FinancialTransaction(
            id = "tx-1",
            dateText = "Selasa, 24/11/2026",
            timeText = "08:00 WIB",
            day = 24,
            month = 11,
            year = 2026,
            type = TransactionType.INCOME,
            category = TransactionCategory.TICKET_SALE,
            amount = Money.rupiah(250_000L),
            busId = "bus-1",
            busCode = "TN-01",
            routeId = "route-1",
            routeName = "Surabaya - Malang",
            description = "Tiket Surabaya - Malang (TN-01)"
        )

        repository.recordTransaction(tx)

        val list = repository.getTransactions()
        assertEquals(1, list.size)
        assertEquals("tx-1", list.first().id)
        assertEquals(Money.rupiah(250_000L), list.first().amount)
        assertEquals(TransactionType.INCOME, list.first().type)

        val flowResult = repository.getAllTransactionsFlow().first()
        assertEquals(1, flowResult.size)
    }

    @Test
    fun recordFinancialTransactionUseCase_incomeAddsBalance() = runTest {
        val useCase = RecordFinancialTransactionUseCase(repository, fakeCompanyRepo)

        val incomeTx = FinancialTransaction(
            id = "tx-inc",
            dateText = "Selasa, 24/11/2026",
            timeText = "09:00 WIB",
            day = 24,
            month = 11,
            year = 2026,
            type = TransactionType.INCOME,
            category = TransactionCategory.TICKET_SALE,
            amount = Money.rupiah(500_000L),
            description = "Tiket"
        )

        useCase(incomeTx)

        val company = fakeCompanyRepo.getCompany().first()
        assertEquals(Money.rupiah(1_500_000L), company?.balance)
    }

    @Test
    fun recordFinancialTransactionUseCase_expenseDeductsBalance() = runTest {
        val useCase = RecordFinancialTransactionUseCase(repository, fakeCompanyRepo)

        val expenseTx = FinancialTransaction(
            id = "tx-exp",
            dateText = "Selasa, 24/11/2026",
            timeText = "00:00 WIB",
            day = 24,
            month = 11,
            year = 2026,
            type = TransactionType.EXPENSE,
            category = TransactionCategory.DRIVER_WAGE,
            amount = Money.rupiah(200_000L),
            description = "Gaji Sopir"
        )

        useCase(expenseTx)

        val company = fakeCompanyRepo.getCompany().first()
        assertEquals(Money.rupiah(800_000L), company?.balance)
    }

    @Test
    fun clearTransactions_removesAllEntries() = runTest {
        val tx = FinancialTransaction(
            id = "tx-1",
            dateText = "Selasa, 24/11/2026",
            timeText = "08:00 WIB",
            day = 24,
            month = 11,
            year = 2026,
            type = TransactionType.INCOME,
            category = TransactionCategory.TICKET_SALE,
            amount = Money.rupiah(100_000L),
            description = "Tiket"
        )
        repository.recordTransaction(tx)
        assertEquals(1, repository.getTransactions().size)

        repository.clearTransactions()
        assertTrue(repository.getTransactions().isEmpty())
    }

    private class FakeFinancialTransactionDao : FinancialTransactionDao {
        private val list = mutableListOf<FinancialTransactionEntity>()
        private val flow = MutableStateFlow<List<FinancialTransactionEntity>>(emptyList())

        override fun getAllTransactionsFlow(): Flow<List<FinancialTransactionEntity>> = flow

        override suspend fun getAllTransactions(): List<FinancialTransactionEntity> = list.toList()

        override suspend fun insertTransaction(transaction: FinancialTransactionEntity) {
            val idx = list.indexOfFirst { it.id == transaction.id }
            if (idx >= 0) list[idx] = transaction else list.add(transaction)
            flow.value = list.toList()
        }

        override suspend fun clearAll() {
            list.clear()
            flow.value = emptyList()
        }
    }

    private class FakeCompanyRepository(initialBalance: Money = Money.rupiah(1_000_000L)) : CompanyRepository {
        private var company: Company? = Company(
            id = "comp-1",
            name = "PO TransNusantara",
            balance = initialBalance,
            reputation = 100
        )
        private val flow = MutableStateFlow(company)

        override fun getCompany(): Flow<Company?> = flow
        override suspend fun createCompany(name: String, initialBalance: Money): Company {
            val created = Company("comp-1", name, initialBalance, 100)
            company = created
            flow.value = created
            return created
        }
        override suspend fun updateBalance(newBalance: Money) {
            company = company?.copy(balance = newBalance)
            flow.value = company
        }
        override suspend fun addBalance(amount: Money) {
            val current = company?.balance?.amount ?: 0L
            updateBalance(Money.rupiah(current + amount.amount))
        }
        override suspend fun deductBalance(amount: Money): Boolean {
            val current = company?.balance?.amount ?: 0L
            updateBalance(Money.rupiah((current - amount.amount).coerceAtLeast(0L)))
            return true
        }
        override suspend fun resetCompany(name: String, initialBalance: com.example.busmanager.domain.company.model.Money) {}
}
}
