package com.example.busmanager.presentation.dashboard

import com.example.busmanager.core.update.UpdateInfo
import com.example.busmanager.domain.company.model.Company
import com.example.busmanager.domain.company.model.CompanyReputation
import com.example.busmanager.domain.company.model.CompanyTier
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.quest.model.StarterQuestItem
import com.example.busmanager.domain.route.model.RouteOperationalInfo
import com.example.busmanager.domain.simulation.model.GameCalendar
import com.example.busmanager.domain.simulation.model.OfflineProgressionResult
import com.example.busmanager.domain.simulation.model.SimulationLog

data class HomeScreenUiState(
    val company: Company? = null,
    val ownedBuses: List<Bus> = emptyList(),
    val hiredDriverCount: Int = 0,
    val isPlaying: Boolean = false,
    val gameCalendar: GameCalendar = GameCalendar.initial(),
    val isLoading: Boolean = true,
    val updateInfo: UpdateInfo? = null,
    val downloadProgress: Float? = null,
    val routeOperationalList: List<RouteOperationalInfo> = emptyList(),
    val showRouteInfoModal: Boolean = false,
    val offlineProgressionResult: OfflineProgressionResult? = null,
    val reputation: CompanyReputation = CompanyReputation(),
    val tier: CompanyTier = CompanyTier.PERINTIS,
    val recentLogs: List<SimulationLog> = emptyList(),
    val showLogCenterModal: Boolean = false,
    val starterQuests: List<StarterQuestItem> = emptyList(),
    val questMessage: String? = null
) {
    val formattedDateTime: String
        get() = gameCalendar.formatDateTime()
}
