package com.example.busmanager.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.busmanager.domain.quest.repository.QuestRepository

class QuestRepositoryImpl(
    context: Context,
    prefsName: String = "starter_quest_prefs"
) : QuestRepository {

    private val prefs: SharedPreferences = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    companion object {
        private const val KEY_CLAIMED_QUESTS = "key_claimed_quests"
    }

    override fun getClaimedQuestIds(): Set<String> {
        return prefs.getStringSet(KEY_CLAIMED_QUESTS, emptySet()) ?: emptySet()
    }

    override suspend fun markQuestClaimed(questId: String) {
        val updated = getClaimedQuestIds().toMutableSet()
        updated.add(questId)
        prefs.edit().putStringSet(KEY_CLAIMED_QUESTS, updated).apply()
    }

    override suspend fun resetQuests() {
        prefs.edit().remove(KEY_CLAIMED_QUESTS).apply()
    }
}
