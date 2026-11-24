package com.example.busmanager.data

import com.example.busmanager.data.local.dao.OwnedBusDao
import com.example.busmanager.data.local.entity.OwnedBusEntity
import com.example.busmanager.data.repository.ChassisCatalogRepositoryImpl
import com.example.busmanager.data.repository.FleetRepositoryImpl
import com.example.busmanager.data.repository.KaroseriCatalogRepositoryImpl
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.fleet.model.BusCondition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FleetRepositoryTest {

    private lateinit var fakeBusDao: FakeOwnedBusDao
    private lateinit var chassisRepo: ChassisCatalogRepositoryImpl
    private lateinit var karoseriRepo: KaroseriCatalogRepositoryImpl
    private lateinit var fleetRepo: FleetRepositoryImpl

    @Before
    fun setUp() {
        fakeBusDao = FakeOwnedBusDao()
        chassisRepo = ChassisCatalogRepositoryImpl()
        karoseriRepo = KaroseriCatalogRepositoryImpl()
        fleetRepo = FleetRepositoryImpl(fakeBusDao, chassisRepo, karoseriRepo)
    }

    @Test
    fun addBusStoresAndRetrievesOwnedBus() = runBlocking {
        val chassis = chassisRepo.getChassisById("chassis-hino-rk8")!!
        val body = karoseriRepo.getBodyById("body-ap-jb3-shd")!!
        val bus = Bus(
            id = "bus-test-1",
            busCode = "TN-01",
            chassis = chassis,
            body = body,
            condition = BusCondition(95, 90, 5000)
        )

        fleetRepo.addBus(bus)

        val retrieved = fleetRepo.getBusById("bus-test-1")
        assertNotNull(retrieved)
        assertEquals("TN-01", retrieved?.busCode)
        assertEquals(5000L, retrieved?.condition?.odometerKm)

        val list = fleetRepo.getOwnedBuses().first()
        assertEquals(1, list.size)
    }

    @Test
    fun removeBusDeletesFromRepository() = runBlocking {
        val chassis = chassisRepo.getChassisById("chassis-mercy-1626")!!
        val body = karoseriRepo.getBodyById("body-lk-sr3-suites")!!
        val bus = Bus(
            id = "bus-test-2",
            busCode = "TN-02",
            chassis = chassis,
            body = body
        )

        fleetRepo.addBus(bus)
        assertEquals(1, fleetRepo.getOwnedBuses().first().size)

        fleetRepo.removeBus("bus-test-2")
        assertTrue(fleetRepo.getOwnedBuses().first().isEmpty())
        assertNull(fleetRepo.getBusById("bus-test-2"))
    }

    private class FakeOwnedBusDao : OwnedBusDao {
        private val busMap = mutableMapOf<String, OwnedBusEntity>()
        private val state = MutableStateFlow<List<OwnedBusEntity>>(emptyList())

        override fun getAllBusesFlow(): Flow<List<OwnedBusEntity>> = state

        override suspend fun getBusById(busId: String): OwnedBusEntity? = busMap[busId]

        override suspend fun insertBus(bus: OwnedBusEntity) {
            busMap[bus.id] = bus
            state.value = busMap.values.toList()
        }

        override suspend fun updateBus(bus: OwnedBusEntity) {
            busMap[bus.id] = bus
            state.value = busMap.values.toList()
        }

        override suspend fun deleteBusById(busId: String): Int {
            val removed = busMap.remove(busId) != null
            state.value = busMap.values.toList()
            return if (removed) 1 else 0
        }

        override suspend fun deleteAllBuses(): Int {
            val count = busMap.size
            busMap.clear()
            state.value = emptyList()
            return count
        }
    }
}
