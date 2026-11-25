package com.example.busmanager.presentation.driver

import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.driver.model.Driver
import com.example.busmanager.domain.fleet.model.Bus

data class DriverManagementUiState(
    val hiredDrivers: List<Driver> = emptyList(),
    val candidateDrivers: List<Driver> = emptyList(),
    val ownedBuses: List<Bus> = emptyList(),
    val companyBalance: Money = Money.ZERO,
    val selectedTab: DriverTab = DriverTab.HIRED,
    val selectedDriverForAssignment: Driver? = null,
    val driverToFire: Driver? = null,
    val driverToUnassign: Driver? = null,
    val isLoading: Boolean = true,
    val message: String? = null
) {
    val totalDailyWage: Money
        get() = hiredDrivers.fold(Money.ZERO) { acc, driver -> acc + driver.wagePerDay }

    val assignedDriverCount: Int
        get() = hiredDrivers.count { it.isAssigned }

    val unassignedDriverCount: Int
        get() = hiredDrivers.count { !it.isAssigned }
}
