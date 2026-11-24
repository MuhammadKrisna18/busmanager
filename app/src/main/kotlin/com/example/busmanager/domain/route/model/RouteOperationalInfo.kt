package com.example.busmanager.domain.route.model

import com.example.busmanager.domain.driver.model.Driver
import com.example.busmanager.domain.fleet.model.Bus

data class RouteOperationalInfo(
    val route: BusRoute,
    val isActive: Boolean,
    val assignedBuses: List<Bus> = emptyList(),
    val assignedDrivers: List<Driver> = emptyList(),
    val departureSchedules: List<String> = emptyList(),
    val activeTripsCount: Int = 0
)
