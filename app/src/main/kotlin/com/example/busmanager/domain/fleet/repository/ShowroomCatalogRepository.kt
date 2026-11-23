package com.example.busmanager.domain.fleet.repository

import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.fleet.model.KaroseriBody
import kotlinx.coroutines.flow.Flow

interface ShowroomCatalogRepository {
    fun getReadyStockBuses(): Flow<List<Bus>>
    fun getCompatibleBodies(chassisId: String): Flow<List<KaroseriBody>>
    suspend fun createCustomBus(busCode: String, chassisId: String, bodyId: String): Bus?
}
