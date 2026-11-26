package com.example.busmanager.domain.quest.usecase

import com.example.busmanager.domain.company.repository.CompanyRepository
import com.example.busmanager.domain.finance.model.FinancialTransaction
import com.example.busmanager.domain.finance.model.TransactionCategory
import com.example.busmanager.domain.finance.model.TransactionType
import com.example.busmanager.domain.finance.repository.FinancialTransactionRepository
import com.example.busmanager.domain.quest.model.QuestId
import com.example.busmanager.domain.quest.repository.QuestRepository

class ClaimQuestRewardUseCase(
    private val questRepository: QuestRepository,
    private val companyRepository: CompanyRepository,
    private val financialTransactionRepository: FinancialTransactionRepository? = null
) {
    suspend operator fun invoke(questId: QuestId): Result<Unit> {
        val claimed = questRepository.getClaimedQuestIds()
        if (claimed.contains(questId.name)) {
            return Result.failure(IllegalStateException("Hadiah misi ini sudah pernah diklaim"))
        }

        companyRepository.addBalance(questId.rewardMoney)

        financialTransactionRepository?.recordTransaction(
            FinancialTransaction(
                id = "tx-quest-${questId.name}-${System.currentTimeMillis()}",
                dateText = "Hari 1",
                timeText = "08:00",
                day = 1,
                month = 1,
                year = 2026,
                type = TransactionType.INCOME,
                category = TransactionCategory.OTHER,
                amount = questId.rewardMoney,
                description = "Klaim Misi: ${questId.title}"
            )
        )

        questRepository.markQuestClaimed(questId.name)
        return Result.success(Unit)
    }
}
