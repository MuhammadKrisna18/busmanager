package com.example.busmanager.data

import com.example.busmanager.data.repository.ChassisCatalogRepositoryImpl
import com.example.busmanager.data.repository.KaroseriCatalogRepositoryImpl
import com.example.busmanager.data.repository.ShowroomCatalogRepositoryImpl
import com.example.busmanager.domain.fleet.model.BusBodyType
import com.example.busmanager.domain.fleet.model.ChassisBrand
import com.example.busmanager.domain.fleet.model.KaroseriBrand
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogRepositoryTest {

    private val chassisRepo = ChassisCatalogRepositoryImpl()
    private val karoseriRepo = KaroseriCatalogRepositoryImpl()
    private val showroomRepo = ShowroomCatalogRepositoryImpl()

    @Test
    fun getAllChassisContainsAllFourBrands() = runBlocking {
        val allChassis = chassisRepo.getAllChassis().first()
        assertTrue(allChassis.size >= 12)

        val brands = allChassis.map { it.brand }.toSet()
        assertTrue(brands.contains(ChassisBrand.HINO))
        assertTrue(brands.contains(ChassisBrand.MERCEDES_BENZ))
        assertTrue(brands.contains(ChassisBrand.SCANIA))
        assertTrue(brands.contains(ChassisBrand.VOLVO))
    }

    @Test
    fun getAllBodiesContainsAdiputroLaksanaTentrem() = runBlocking {
        val allBodies = karoseriRepo.getAllBodies().first()
        assertTrue(allBodies.size >= 12)

        val brands = allBodies.map { it.brand }.toSet()
        assertTrue(brands.contains(KaroseriBrand.ADIPUTRO))
        assertTrue(brands.contains(KaroseriBrand.LAKSANA))
        assertTrue(brands.contains(KaroseriBrand.TENTREM))
    }

    @Test
    fun singleAxleChassisDoesNotMatchDoubleDecker() = runBlocking {
        val singleAxleId = "chassis-hino-rk8"
        val compatibleBodies = showroomRepo.getCompatibleBodies(singleAxleId).first()

        assertFalse(compatibleBodies.any { it.bodyType == BusBodyType.SDD })
    }

    @Test
    fun trontonChassisMatchesDoubleDeckerAndUhd() = runBlocking {
        val trontonId = "chassis-scania-k410"
        val compatibleBodies = showroomRepo.getCompatibleBodies(trontonId).first()

        assertTrue(compatibleBodies.any { it.bodyType == BusBodyType.SDD })
        assertTrue(compatibleBodies.any { it.bodyType == BusBodyType.UHD })
    }

    @Test
    fun readyStockShowroomProvidesValidPrebuiltBuses() = runBlocking {
        val stocks = showroomRepo.getReadyStockBuses().first()
        assertTrue(stocks.isNotEmpty())
        stocks.forEach { bus ->
            assertNotNull(bus.id)
            assertNotNull(bus.totalPrice)
            assertTrue(bus.totalPrice.amount > 0)
        }
    }

    @Test
    fun createCustomBusWithCompatiblePairSucceeds() = runBlocking {
        val customBus = showroomRepo.createCustomBus(
            busCode = "TN-99",
            chassisId = "chassis-mercy-1626",
            bodyId = "body-lk-sr3-suites"
        )
        assertNotNull(customBus)
        assertEquals("TN-99", customBus?.busCode)
    }
}
