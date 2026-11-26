package com.example.busmanager.domain.quest.repository

interface QuestRepository {
    fun getClaimedQuestIds(): Set<String>
    suspend fun markQuestClaimed(questId: String)
    suspend fun resetQuests()
}
