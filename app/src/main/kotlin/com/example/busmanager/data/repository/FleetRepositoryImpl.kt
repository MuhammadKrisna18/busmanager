package com.example.busmanager.data.repository

import com.example.busmanager.data.local.dao.OwnedBusDao
import com.example.busmanager.data.mapper.toDomain
import com.example.busmanager.data.mapper.toEntity
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.fleet.repository.ChassisCatalogRepository
import com.example.busmanager.domain.fleet.repository.FleetRepository
import com.example.busmanager.domain.fleet.repository.KaroseriCatalogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class FleetRepositoryImpl(
    private val busDao: OwnedBusDao,
    private val chassisCatalog: ChassisCatalogRepository,
    private val karoseriCatalog: KaroseriCatalogRepository
) : FleetRepository {

    override fun getOwnedBuses(): Flow<List<Bus>> {
        return combine(
            busDao.getAllBusesFlow(),
            chassisCatalog.getAllChassis(),
            karoseriCatalog.getAllBodies()
        ) { entities, chassisList, bodyList ->
            entities.mapNotNull { entity ->
                val chassis = chassisList.find { it.id == entity.chassisId }
                val body = bodyList.find { it.id == entity.bodyId }
                if (chassis != null && body != null) {
                    entity.toDomain(chassis, body)
                } else {
                    null
                }
            }
        }
    }

    override suspend fun getBusById(id: String): Bus? {
        val entity = busDao.getBusById(id) ?: return null
        val chassis = chassisCatalog.getChassisById(entity.chassisId) ?: return null
        val body = karoseriCatalog.getBodyById(entity.bodyId) ?: return null
        return entity.toDomain(chassis, body)
    }

    override suspend fun addBus(bus: Bus) {
        busDao.insertBus(bus.toEntity())
    }

    override suspend fun updateBus(bus: Bus) {
        busDao.updateBus(bus.toEntity())
    }

    override suspend fun removeBus(busId: String) {
        busDao.deleteBusById(busId)
    }
}
