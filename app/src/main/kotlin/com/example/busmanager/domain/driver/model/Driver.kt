package com.example.busmanager.domain.driver.model

import com.example.busmanager.domain.company.model.Money

data class Driver(
    val id: String,
    val name: String,
    val wagePerDay: Money,
    val assignedBusId: String? = null,
    val isHired: Boolean = false
) {
    init {
        require(name.isNotBlank()) { "Driver name cannot be blank" }
    }

    val isAssigned: Boolean get() = !assignedBusId.isNullOrBlank()
}
