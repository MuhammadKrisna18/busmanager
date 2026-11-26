package com.example.busmanager.domain.quest

import com.example.busmanager.domain.company.model.Company
import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.company.repository.CompanyRepository
import com.example.busmanager.domain.finance.model.FinancialTransaction
import com.example.busmanager.domain.finance.repository.FinancialTransactionRepository
import com.example.busmanager.domain.quest.model.QuestId
import com.example.busmanager.domain.quest.repository.QuestRepository
import com.example.busmanager.domain.quest.usecase.ClaimQuestRewardUseCase
import com.example.busmanager.domain.quest.usecase.GetStarterQuestsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class StarterQuestUseCaseTest {

    private lateinit var fakeQuestRepo: FakeQuestRepository
    private lateinit var fakeCompanyRepo: FakeCompanyRepository
    private lateinit var fakeFinanceRepo: FakeFinancialTransactionRepository
    private lateinit var getQuestsUseCase: GetStarterQuestsUseCase
    private lateinit var claimQuestUseCase: ClaimQuestRewardUseCase

    @Before
    fun setUp() {
        fakeQuestRepo = FakeQuestRepository()
        fakeCompanyRepo = FakeCompanyRepository(Money.rupiah(500_000_000L))
        fakeFinanceRepo = FakeFinancialTransactionRepository()
        getQuestsUseCase = GetStarterQuestsUseCase(fakeQuestRepo)
        claimQuestUseCase = ClaimQuestRewardUseCase(fakeQuestRepo, fakeCompanyRepo, fakeFinanceRepo)
    }

    @Test
    fun getStarterQuests_returnsAllFiveQuestsWithInitialZeroProgress() {
        val quests = getQuestsUseCase(
            busCount = 0,
            driverCount = 0,
            routeCount = 0,
            activeRouteCount = 0,
            totalBoardedPassengers = 0
        )

        assertEquals(5, quests.size)
        quests.forEach { quest ->
            assertEquals(0, quest.currentProgress)
            assertFalse(quest.isCompleted)
            assertFalse(quest.isClaimed)
            assertEquals(0f, quest.progressFraction, 0.001f)
        }
    }

    @Test
    fun getStarterQuests_marksCompletedWhenTargetThresholdMet() {
        val quests = getQuestsUseCase(
            busCount = 2,
            driverCount = 1,
            routeCount = 1,
            activeRouteCount = 1,
            totalBoardedPassengers = 55
        )

        val buyBus = quests.first { it.id == QuestId.BUY_FIRST_BUS }
        assertTrue(buyBus.isCompleted)
        assertEquals(1, buyBus.currentProgress)

        val hireDriver = quests.first { it.id == QuestId.HIRE_FIRST_DRIVER }
        assertTrue(hireDriver.isCompleted)
        assertEquals(1, hireDriver.currentProgress)

        val passengers = quests.first { it.id == QuestId.BOARD_PASSENGERS }
        assertTrue(passengers.isCompleted)
        assertEquals(50, passengers.currentProgress)
        assertEquals(1.0f, passengers.progressFraction, 0.001f)
    }

    @Test
    fun getStarterQuests_identifiesClaimedQuests() = runTest {
        fakeQuestRepo.markQuestClaimed(QuestId.BUY_FIRST_BUS.name)

        val quests = getQuestsUseCase(
            busCount = 1,
            driverCount = 0,
            routeCount = 0,
            activeRouteCount = 0,
            totalBoardedPassengers = 0
        )

        val buyBus = quests.first { it.id == QuestId.BUY_FIRST_BUS }
        assertTrue(buyBus.isCompleted)
        assertTrue(buyBus.isClaimed)

        val hireDriver = quests.first { it.id == QuestId.HIRE_FIRST_DRIVER }
        assertFalse(hireDriver.isClaimed)
    }

    @Test
    fun claimQuestReward_successfullyAddsBalanceAndRecordsTransaction() = runTest {
        val initialBalance = fakeCompanyRepo.currentBalance
        val quest = QuestId.BUY_FIRST_BUS

        val result = claimQuestUseCase(quest)

        assertTrue(result.isSuccess)
        assertEquals(initialBalance + quest.rewardMoney, fakeCompanyRepo.currentBalance)
        assertTrue(fakeQuestRepo.getClaimedQuestIds().contains(quest.name))
        assertEquals(1, fakeFinanceRepo.recordedTransactions.size)
        assertEquals(quest.rewardMoney, fakeFinanceRepo.recordedTransactions.first().amount)
    }

    @Test
    fun claimQuestReward_failsWhenAlreadyClaimed() = runTest {
        val quest = QuestId.BUY_FIRST_BUS
        claimQuestUseCase(quest)

        val secondResult = claimQuestUseCase(quest)

        assertTrue(secondResult.isFailure)
        assertEquals("Hadiah misi ini sudah pernah diklaim", secondResult.exceptionOrNull()?.message)
    }

    private class FakeQuestRepository : QuestRepository {
        private val claimed = mutableSetOf<String>()

        override fun getClaimedQuestIds(): Set<String> = claimed.toSet()

        override suspend fun markQuestClaimed(questId: String) {
            claimed.add(questId)
        }

        override suspend fun resetQuests() {
            claimed.clear()
        }
    }

    private class FakeCompanyRepository(initialBalance: Money) : CompanyRepository {
        var currentBalance: Money = initialBalance

        override fun getCompany(): Flow<Company?> = flowOf(
            Company(id = "comp-1", name = "PO Test", balance = currentBalance)
        )

        override suspend fun createCompany(name: String, initialBalance: Money): Company {
            currentBalance = initialBalance
            return Company(id = "comp-1", name = name, balance = currentBalance)
        }

        override suspend fun updateBalance(newBalance: Money) {
            currentBalance = newBalance
        }

        override suspend fun deductBalance(amount: Money): Boolean {
            if (!currentBalance.canAfford(amount)) return false
            currentBalance -= amount
            return true
        }

        override suspend fun addBalance(amount: Money) {
            currentBalance += amount
        }

        override suspend fun resetCompany(name: String, initialBalance: Money) {
            currentBalance = initialBalance
        }
    }

    private class FakeFinancialTransactionRepository : FinancialTransactionRepository {
        val recordedTransactions = mutableListOf<FinancialTransaction>()

        override fun getAllTransactionsFlow(): Flow<List<FinancialTransaction>> = flowOf(recordedTransactions)

        override suspend fun getTransactions(): List<FinancialTransaction> = recordedTransactions.toList()

        override suspend fun recordTransaction(transaction: FinancialTransaction) {
            recordedTransactions.add(transaction)
        }

        override suspend fun clearTransactions() {
            recordedTransactions.clear()
        }
    }
}
