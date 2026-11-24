package com.example.busmanager.data

import com.example.busmanager.data.local.dao.DriverDao
import com.example.busmanager.data.local.dao.OwnedBusDao
import com.example.busmanager.data.local.entity.DriverEntity
import com.example.busmanager.data.local.entity.OwnedBusEntity
import com.example.busmanager.data.repository.DriverRepositoryImpl
import com.example.busmanager.domain.driver.usecase.AssignDriverToBusUseCase
import com.example.busmanager.domain.driver.usecase.GetAvailableDriversUseCase
import com.example.busmanager.domain.driver.usecase.GetHiredDriversUseCase
import com.example.busmanager.domain.driver.usecase.HireDriverUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DriverRepositoryTest {

    private lateinit var fakeDriverDao: FakeDriverDao
    private lateinit var fakeBusDao: FakeOwnedBusDao
    private lateinit var driverRepository: DriverRepositoryImpl
    private lateinit var getAvailableDriversUseCase: GetAvailableDriversUseCase
    private lateinit var getHiredDriversUseCase: GetHiredDriversUseCase
    private lateinit var hireDriverUseCase: HireDriverUseCase
    private lateinit var assignDriverToBusUseCase: AssignDriverToBusUseCase

    @Before
    fun setUp() {
        fakeDriverDao = FakeDriverDao()
        fakeBusDao = FakeOwnedBusDao()
        driverRepository = DriverRepositoryImpl(fakeDriverDao, fakeBusDao)
        getAvailableDriversUseCase = GetAvailableDriversUseCase(driverRepository)
        getHiredDriversUseCase = GetHiredDriversUseCase(driverRepository)
        hireDriverUseCase = HireDriverUseCase(driverRepository)
        assignDriverToBusUseCase = AssignDriverToBusUseCase(driverRepository)
    }

    @Test
    fun seedCandidatesInsertsDefaultDriversWhenEmpty() = runBlocking {
        driverRepository.seedCandidateDriversIfEmpty()

        val allDrivers = getAvailableDriversUseCase().first()
        assertEquals(6, allDrivers.size)
        assertTrue(allDrivers.none { it.isHired })
    }

    @Test
    fun hireDriverUpdatesHiredStatus() = runBlocking {
        driverRepository.seedCandidateDriversIfEmpty()

        val success = hireDriverUseCase("driver-1")
        assertTrue(success)

        val hiredDrivers = getHiredDriversUseCase().first()
        assertEquals(1, hiredDrivers.size)
        assertEquals("driver-1", hiredDrivers.first().id)
        assertTrue(hiredDrivers.first().isHired)
    }

    @Test
    fun assignDriverToBusUpdatesBothDriverAndBus() = runBlocking {
        fakeDriverDao.insertDrivers(
            listOf(
                DriverEntity("driver-1", "Pak Bambang", 180_000L, null, true)
            )
        )
        fakeBusDao.insertBus(
            OwnedBusEntity(
                id = "bus-1",
                busCode = "TN-01",
                chassisId = "chassis-1",
                bodyId = "body-1",
                purchasedAtTimestamp = 1000L
            )
        )

        val success = assignDriverToBusUseCase("driver-1", "bus-1")
        assertTrue(success)

        val updatedDriver = driverRepository.getDriverById("driver-1")
        assertNotNull(updatedDriver)
        assertEquals("bus-1", updatedDriver?.assignedBusId)

        val updatedBus = fakeBusDao.getBusById("bus-1")
        assertNotNull(updatedBus)
        assertEquals("driver-1", updatedBus?.driverId)
    }

    @Test
    fun assignDriverReplacesPreviousDriverOnSameBus() = runBlocking {
        fakeDriverDao.insertDrivers(
            listOf(
                DriverEntity("driver-1", "Pak Bambang", 180_000L, "bus-1", true),
                DriverEntity("driver-2", "Pak Joko", 200_000L, null, true)
            )
        )
        fakeBusDao.insertBus(
            OwnedBusEntity(
                id = "bus-1",
                busCode = "TN-01",
                chassisId = "chassis-1",
                bodyId = "body-1",
                purchasedAtTimestamp = 1000L,
                driverId = "driver-1"
            )
        )

        val success = assignDriverToBusUseCase("driver-2", "bus-1")
        assertTrue(success)

        val oldDriver = driverRepository.getDriverById("driver-1")
        assertNull(oldDriver?.assignedBusId)

        val newDriver = driverRepository.getDriverById("driver-2")
        assertEquals("bus-1", newDriver?.assignedBusId)

        val updatedBus = fakeBusDao.getBusById("bus-1")
        assertEquals("driver-2", updatedBus?.driverId)
    }

    @Test
    fun unassignDriverClearsBusAndDriverLinks() = runBlocking {
        fakeDriverDao.insertDrivers(
            listOf(
                DriverEntity("driver-1", "Pak Bambang", 180_000L, "bus-1", true)
            )
        )
        fakeBusDao.insertBus(
            OwnedBusEntity(
                id = "bus-1",
                busCode = "TN-01",
                chassisId = "chassis-1",
                bodyId = "body-1",
                purchasedAtTimestamp = 1000L,
                driverId = "driver-1"
            )
        )

        driverRepository.unassignDriver("driver-1")

        val driver = driverRepository.getDriverById("driver-1")
        assertNull(driver?.assignedBusId)

        val bus = fakeBusDao.getBusById("bus-1")
        assertNull(bus?.driverId)
    }

    @Test
    fun fireDriverUnassignsAndSetsHiredToFalse() = runBlocking {
        fakeDriverDao.insertDrivers(
            listOf(
                DriverEntity("driver-1", "Pak Bambang", 180_000L, "bus-1", true)
            )
        )
        fakeBusDao.insertBus(
            OwnedBusEntity(
                id = "bus-1",
                busCode = "TN-01",
                chassisId = "chassis-1",
                bodyId = "body-1",
                purchasedAtTimestamp = 1000L,
                driverId = "driver-1"
            )
        )

        driverRepository.fireDriver("driver-1")

        val driver = driverRepository.getDriverById("driver-1")
        assertNotNull(driver)
        assertFalse(driver!!.isHired)
        assertNull(driver.assignedBusId)

        val bus = fakeBusDao.getBusById("bus-1")
        assertNull(bus?.driverId)
    }

    private class FakeDriverDao : DriverDao {
        private val driverMap = mutableMapOf<String, DriverEntity>()
        private val driversFlow = MutableStateFlow<List<DriverEntity>>(emptyList())

        override fun getAllDriversFlow(): Flow<List<DriverEntity>> = driversFlow

        override fun getHiredDriversFlow(): Flow<List<DriverEntity>> {
            val hiredFlow = MutableStateFlow<List<DriverEntity>>(emptyList())
            hiredFlow.value = driverMap.values.filter { it.isHired }
            return hiredFlow
        }

        override suspend fun getDriverById(id: String): DriverEntity? = driverMap[id]

        override suspend fun getDriverByAssignedBus(busId: String): DriverEntity? {
            return driverMap.values.find { it.assignedBusId == busId }
        }

        override suspend fun insertDriver(driver: DriverEntity) {
            driverMap[driver.id] = driver
            driversFlow.value = driverMap.values.toList()
        }

        override suspend fun insertDrivers(drivers: List<DriverEntity>) {
            drivers.forEach { driverMap[it.id] = it }
            driversFlow.value = driverMap.values.toList()
        }

        override suspend fun updateDriver(driver: DriverEntity): Int {
            driverMap[driver.id] = driver
            driversFlow.value = driverMap.values.toList()
            return 1
        }

        override suspend fun deleteDriverById(id: String): Int {
            val removed = driverMap.remove(id) != null
            driversFlow.value = driverMap.values.toList()
            return if (removed) 1 else 0
        }

        override suspend fun resetAllDrivers(): Int {
            val count = driverMap.size
            driverMap.values.forEach { driver ->
                driverMap[driver.id] = driver.copy(isHired = false, assignedBusId = null)
            }
            driversFlow.value = driverMap.values.toList()
            return count
        }
    }

    private class FakeOwnedBusDao : OwnedBusDao {
        private val busMap = mutableMapOf<String, OwnedBusEntity>()
        private val busFlow = MutableStateFlow<List<OwnedBusEntity>>(emptyList())

        override fun getAllBusesFlow(): Flow<List<OwnedBusEntity>> = busFlow

        override suspend fun getBusById(busId: String): OwnedBusEntity? = busMap[busId]

        override suspend fun insertBus(bus: OwnedBusEntity) {
            busMap[bus.id] = bus
            busFlow.value = busMap.values.toList()
        }

        override suspend fun updateBus(bus: OwnedBusEntity) {
            busMap[bus.id] = bus
            busFlow.value = busMap.values.toList()
        }

        override suspend fun deleteBusById(busId: String): Int {
            val removed = busMap.remove(busId) != null
            busFlow.value = busMap.values.toList()
            return if (removed) 1 else 0
        }

        override suspend fun deleteAllBuses(): Int {
            val count = busMap.size
            busMap.clear()
            busFlow.value = emptyList()
            return count
        }
    }
}
