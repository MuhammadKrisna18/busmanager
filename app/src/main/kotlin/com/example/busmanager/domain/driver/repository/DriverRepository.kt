package com.example.busmanager.domain.driver.repository

import com.example.busmanager.domain.driver.model.Driver
import kotlinx.coroutines.flow.Flow

interface DriverRepository {
    fun getAllDrivers(): Flow<List<Driver>>
    fun getHiredDrivers(): Flow<List<Driver>>
    suspend fun getDriverById(id: String): Driver?
    suspend fun hireDriver(driverId: String): Boolean
    suspend fun assignDriverToBus(driverId: String, busId: String): Boolean
    suspend fun unassignDriver(driverId: String)
    suspend fun fireDriver(driverId: String)
    suspend fun seedCandidateDriversIfEmpty()
}
