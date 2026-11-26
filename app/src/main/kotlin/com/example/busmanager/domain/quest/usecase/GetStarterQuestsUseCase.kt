package com.example.busmanager.domain.quest.usecase

import com.example.busmanager.domain.quest.model.QuestId
import com.example.busmanager.domain.quest.model.StarterQuestItem
import com.example.busmanager.domain.quest.repository.QuestRepository

class GetStarterQuestsUseCase(
    private val questRepository: QuestRepository
) {
    operator fun invoke(
        busCount: Int,
        driverCount: Int,
        routeCount: Int,
        activeRouteCount: Int,
        totalBoardedPassengers: Int
    ): List<StarterQuestItem> {
        val claimedIds = questRepository.getClaimedQuestIds()

        return QuestId.entries.map { quest ->
            val progress = when (quest) {
                QuestId.BUY_FIRST_BUS -> busCount.coerceAtLeast(0)
                QuestId.HIRE_FIRST_DRIVER -> driverCount.coerceAtLeast(0)
                QuestId.CREATE_FIRST_ROUTE -> routeCount.coerceAtLeast(0)
                QuestId.OPERATE_FIRST_ROUTE -> activeRouteCount.coerceAtLeast(0)
                QuestId.BOARD_PASSENGERS -> totalBoardedPassengers.coerceAtLeast(0)
            }
            val isCompleted = progress >= quest.targetCount
            val isClaimed = claimedIds.contains(quest.name)

            StarterQuestItem(
                id = quest,
                currentProgress = progress.coerceAtMost(quest.targetCount),
                targetCount = quest.targetCount,
                isCompleted = isCompleted,
                isClaimed = isClaimed,
                rewardMoney = quest.rewardMoney
            )
        }
    }
}
