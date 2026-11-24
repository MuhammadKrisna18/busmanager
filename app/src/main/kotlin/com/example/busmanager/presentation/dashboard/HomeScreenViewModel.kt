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
    private val resetGameUseCase: ResetGameUseCase? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeScreenUiState())
    val uiState: StateFlow<HomeScreenUiState> = _uiState.asStateFlow()

    init {
        initializeData()
    }

    private fun initializeData() {
        viewModelScope.launch {
            startNewGameUseCase()
            val driversFlow = getHiredDriversUseCase?.invoke() ?: flowOf(emptyList())
            combine(
                getCompanyUseCase(),
                getOwnedFleetUseCase(),
                driversFlow
            ) { company, buses, drivers ->
                _uiState.update { current ->
                    current.copy(
                        company = company,
                        ownedBuses = buses,
                        hiredDriverCount = drivers.size,
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
}

