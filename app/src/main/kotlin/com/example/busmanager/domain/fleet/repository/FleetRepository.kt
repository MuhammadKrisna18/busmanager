package com.example.busmanager.domain.fleet.repository

import com.example.busmanager.domain.fleet.model.Bus
import kotlinx.coroutines.flow.Flow

interface FleetRepository {
    fun getOwnedBuses(): Flow<List<Bus>>
    suspend fun getBusById(id: String): Bus?
    suspend fun addBus(bus: Bus)
    suspend fun updateBus(bus: Bus)
    suspend fun removeBus(busId: String)
}
