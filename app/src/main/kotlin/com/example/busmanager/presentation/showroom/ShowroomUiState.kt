package com.example.busmanager.presentation.showroom

import com.example.busmanager.domain.company.model.Company
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.fleet.model.ChassisBrand
import com.example.busmanager.domain.fleet.model.KaroseriBrand

data class ShowroomUiState(
    val company: Company? = null,
    val readyStockBuses: List<Bus> = emptyList(),
    val filteredBuses: List<Bus> = emptyList(),
    val selectedKaroseriFilter: KaroseriBrand? = null,
    val selectedChassisFilter: ChassisBrand? = null,
    val selectedBusForPurchase: Bus? = null,
    val customBusCode: String = "",
    val isPurchasing: Boolean = false,
    val purchaseSuccessBus: Bus? = null,
    val errorMessage: String? = null,
    val isLoading: Boolean = true
)
