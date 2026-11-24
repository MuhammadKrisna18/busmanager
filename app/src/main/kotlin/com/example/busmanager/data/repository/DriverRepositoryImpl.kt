package com.example.busmanager.data.repository

import com.example.busmanager.data.local.dao.DriverDao
import com.example.busmanager.data.local.dao.OwnedBusDao
import com.example.busmanager.data.local.entity.DriverEntity
import com.example.busmanager.data.mapper.toDomain
import com.example.busmanager.data.mapper.toEntity
import com.example.busmanager.domain.driver.model.Driver
import com.example.busmanager.domain.driver.repository.DriverRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DriverRepositoryImpl(
    private val driverDao: DriverDao,
    private val busDao: OwnedBusDao
) : DriverRepository {

    override fun getAllDrivers(): Flow<List<Driver>> {
        return driverDao.getAllDriversFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getHiredDrivers(): Flow<List<Driver>> {
        return driverDao.getHiredDriversFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getDriverById(id: String): Driver? {
        return driverDao.getDriverById(id)?.toDomain()
    }

    override suspend fun hireDriver(driverId: String): Boolean {
        val driverEntity = driverDao.getDriverById(driverId) ?: return false
        driverDao.updateDriver(driverEntity.copy(isHired = true))
        return true
    }

    override suspend fun assignDriverToBus(driverId: String, busId: String): Boolean {
        val driverEntity = driverDao.getDriverById(driverId) ?: return false
        val busEntity = busDao.getBusById(busId) ?: return false

        val currentDriverOnBus = driverDao.getDriverByAssignedBus(busId)
        if (currentDriverOnBus != null && currentDriverOnBus.id != driverId) {
            driverDao.updateDriver(currentDriverOnBus.copy(assignedBusId = null))
        }

        driverDao.updateDriver(driverEntity.copy(assignedBusId = busId, isHired = true))
        busDao.updateBus(busEntity.copy(driverId = driverId))
        return true
    }

    override suspend fun unassignDriver(driverId: String) {
        val driverEntity = driverDao.getDriverById(driverId) ?: return
        val busId = driverEntity.assignedBusId

        driverDao.updateDriver(driverEntity.copy(assignedBusId = null))

        if (busId != null) {
            val busEntity = busDao.getBusById(busId)
            if (busEntity != null && busEntity.driverId == driverId) {
                busDao.updateBus(busEntity.copy(driverId = null))
            }
        }
    }

    override suspend fun fireDriver(driverId: String) {
        unassignDriver(driverId)
        val driverEntity = driverDao.getDriverById(driverId) ?: return
        driverDao.updateDriver(driverEntity.copy(isHired = false, assignedBusId = null))
    }

    override suspend fun seedCandidateDriversIfEmpty() {
        val existing = driverDao.getDriverById("driver-1")
        if (existing == null) {
            val initialCandidates = listOf(
                DriverEntity("driver-1", "Pak Bambang Purwanto", 180_000L, null, false),
                DriverEntity("driver-2", "Pak Joko Widodo", 200_000L, null, false),
                DriverEntity("driver-3", "Mas Agus Santoso", 150_000L, null, false),
                DriverEntity("driver-4", "Pak Slamet Riyadi", 220_000L, null, false),
                DriverEntity("driver-5", "Mas Budi Hartono", 160_000L, null, false),
                DriverEntity("driver-6", "Pak Sutrisno", 190_000L, null, false)
            )
            driverDao.insertDrivers(initialCandidates)
        }
    }
}
