package com.example.busmanager.presentation.dashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.busmanager.core.update.AppUpdateManager
import com.example.busmanager.core.update.UpdateInfo
import com.example.busmanager.domain.company.usecase.GetCompanyUseCase
import com.example.busmanager.domain.company.usecase.ResetGameUseCase
import com.example.busmanager.domain.company.usecase.StartNewGameUseCase
import com.example.busmanager.domain.driver.usecase.GetHiredDriversUseCase
import com.example.busmanager.domain.fleet.usecase.GetOwnedFleetUseCase
import com.example.busmanager.domain.fleet.usecase.ReconcileBusOperatingStateUseCase
import com.example.busmanager.domain.route.usecase.GetRouteOperationalStatusUseCase
import com.example.busmanager.domain.simulation.usecase.GetSimulationTimeUseCase
import com.example.busmanager.domain.simulation.usecase.ProcessOfflineProgressionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeScreenViewModel(
    private val getCompanyUseCase: GetCompanyUseCase,
    private val getOwnedFleetUseCase: GetOwnedFleetUseCase,
    private val startNewGameUseCase: StartNewGameUseCase,
    private val getHiredDriversUseCase: GetHiredDriversUseCase? = null,
    private val appUpdateManager: AppUpdateManager = AppUpdateManager(),
    private val resetGameUseCase: ResetGameUseCase? = null,
    private val getRouteOperationalStatusUseCase: GetRouteOperationalStatusUseCase? = null,
    private val processOfflineProgressionUseCase: ProcessOfflineProgressionUseCase? = null,
    private val reconcileBusOperatingStateUseCase: ReconcileBusOperatingStateUseCase? = null,
    private val getSimulationTimeUseCase: GetSimulationTimeUseCase? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeScreenUiState())
    val uiState: StateFlow<HomeScreenUiState> = _uiState.asStateFlow()

    init {
        initializeData()
    }

    private fun initializeData() {
        if (getSimulationTimeUseCase != null) {
            viewModelScope.launch {
                getSimulationTimeUseCase.invoke().collect { cal ->
                    _uiState.update { it.copy(gameCalendar = cal) }
                }
            }
        }
        viewModelScope.launch {
            startNewGameUseCase()
            val offlineResult = processOfflineProgressionUseCase?.invoke()
            if (offlineResult != null && offlineResult.netEarnings.amount > 0) {
                _uiState.update { it.copy(offlineProgressionResult = offlineResult) }
            }
            reconcileBusOperatingStateUseCase?.invoke()
            val driversFlow = getHiredDriversUseCase?.invoke() ?: flowOf(emptyList())
            val operationalFlow = getRouteOperationalStatusUseCase?.invoke() ?: flowOf(emptyList())
            combine(
                getCompanyUseCase(),
                getOwnedFleetUseCase(),
                driversFlow,
                operationalFlow
            ) { company, buses, drivers, operationalRoutes ->
                _uiState.update { current ->
                    current.copy(
                        company = company,
                        ownedBuses = buses,
                        hiredDriverCount = drivers.size,
                        routeOperationalList = operationalRoutes,
                        isLoading = false
                    )
                }
            }.collect {}
        }
    }

    fun checkForUpdates(currentVersion: String) {
        viewModelScope.launch {
            val update = appUpdateManager.checkForUpdate(currentVersion)
            if (update != null) {
                _uiState.update { it.copy(updateInfo = update) }
            }
        }
    }

    fun startUpdateDownload(context: Context, updateInfo: UpdateInfo) {
        viewModelScope.launch {
            _uiState.update { it.copy(downloadProgress = 0.01f) }
            val apkFile = appUpdateManager.downloadApk(updateInfo.downloadUrl, context) { progress ->
                _uiState.update { it.copy(downloadProgress = progress) }
            }
            if (apkFile != null && apkFile.exists()) {
                _uiState.update { it.copy(downloadProgress = null, updateInfo = null) }
                appUpdateManager.installApk(apkFile, context)
            } else {
                _uiState.update { it.copy(downloadProgress = null) }
            }
        }
    }

    fun dismissUpdateDialog() {
        _uiState.update { it.copy(updateInfo = null, downloadProgress = null) }
    }

    fun togglePlaySimulation() {
        _uiState.update { it.copy(isPlaying = !it.isPlaying) }
    }

    fun resetGame() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            resetGameUseCase?.invoke()
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun openRouteInfoModal() {
        _uiState.update { it.copy(showRouteInfoModal = true) }
    }

    fun dismissRouteInfoModal() {
        _uiState.update { it.copy(showRouteInfoModal = false) }
    }

    fun dismissOfflineProgressionDialog() {
        _uiState.update { it.copy(offlineProgressionResult = null) }
    }
}

