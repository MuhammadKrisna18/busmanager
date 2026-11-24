package com.example.busmanager.presentation.dashboard

import com.example.busmanager.core.update.UpdateInfo
import com.example.busmanager.domain.company.model.Company
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.route.model.RouteOperationalInfo
import com.example.busmanager.domain.simulation.model.OfflineProgressionResult

data class HomeScreenUiState(
    val company: Company? = null,
    val ownedBuses: List<Bus> = emptyList(),
    val hiredDriverCount: Int = 0,
    val isPlaying: Boolean = false,
    val gameDay: Int = 1,
    val gameHour: Int = 8,
    val isLoading: Boolean = true,
    val updateInfo: UpdateInfo? = null,
    val downloadProgress: Float? = null,
    val routeOperationalList: List<RouteOperationalInfo> = emptyList(),
    val showRouteInfoModal: Boolean = false,
    val offlineProgressionResult: OfflineProgressionResult? = null
)
