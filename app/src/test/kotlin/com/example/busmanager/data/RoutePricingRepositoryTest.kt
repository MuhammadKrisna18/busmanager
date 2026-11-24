package com.example.busmanager.data

import com.example.busmanager.data.local.dao.RoutePricingDao
import com.example.busmanager.data.local.entity.RoutePricingEntity
import com.example.busmanager.data.repository.RoutePricingRepositoryImpl
import com.example.busmanager.data.repository.TerminalCatalogRepositoryImpl
import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.RoutePathType
import com.example.busmanager.domain.route.model.RoutePricing
import com.example.busmanager.domain.route.usecase.GetRoutePricingsUseCase
import com.example.busmanager.domain.route.usecase.InitializeDefaultRoutePricingsUseCase
import com.example.busmanager.domain.route.usecase.SaveRoutePricingUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RoutePricingRepositoryTest {

    private lateinit var fakePricingDao: FakeRoutePricingDao
    private lateinit var pricingRepository: RoutePricingRepositoryImpl
    private lateinit var getRoutePricingsUseCase: GetRoutePricingsUseCase
    private lateinit var saveRoutePricingUseCase: SaveRoutePricingUseCase
    private lateinit var initializeDefaultRoutePricingsUseCase: InitializeDefaultRoutePricingsUseCase

    private val terminalRepo = TerminalCatalogRepositoryImpl()
    private lateinit var sampleRoute: BusRoute

    @Before
    fun setUp() {
        val sby = terminalRepo.getTerminalById("term_purabaya")!!
        val prob = terminalRepo.getTerminalById("term_bayuangga")!!
        val jbr = terminalRepo.getTerminalById("term_tawangalun")!!

        sampleRoute = BusRoute(
            id = "route-sby-jbr",
            name = "Surabaya - Jember via Probolinggo",
            originTerminal = sby,
            destinationTerminal = jbr,
            pathType = RoutePathType.NON_TOLL,
            segments = emptyList(),
            transitTerminals = listOf(prob)
        )

        fakePricingDao = FakeRoutePricingDao()
        pricingRepository = RoutePricingRepositoryImpl(fakePricingDao)
        getRoutePricingsUseCase = GetRoutePricingsUseCase(pricingRepository)
        saveRoutePricingUseCase = SaveRoutePricingUseCase(pricingRepository)
        initializeDefaultRoutePricingsUseCase = InitializeDefaultRoutePricingsUseCase(pricingRepository)
    }

    @Test
    fun generateDefaultPricingsCalculatesFiftyThousandPerHop() = runBlocking {
        val pricings = initializeDefaultRoutePricingsUseCase(sampleRoute)

        assertEquals(6, pricings.size)

        val sbyToProb = pricings.find { it.originTerminalId == "term_purabaya" && it.destinationTerminalId == "term_bayuangga" }
        assertNotNull(sbyToProb)
        assertEquals(50_000L, sbyToProb?.price?.amount)

        val probToJbr = pricings.find { it.originTerminalId == "term_bayuangga" && it.destinationTerminalId == "term_tawangalun" }
        assertNotNull(probToJbr)
        assertEquals(50_000L, probToJbr?.price?.amount)

        val sbyToJbr = pricings.find { it.originTerminalId == "term_purabaya" && it.destinationTerminalId == "term_tawangalun" }
        assertNotNull(sbyToJbr)
        assertEquals(100_000L, sbyToJbr?.price?.amount)

        val jbrToSby = pricings.find { it.originTerminalId == "term_tawangalun" && it.destinationTerminalId == "term_purabaya" }
        assertNotNull(jbrToSby)
        assertEquals(100_000L, jbrToSby?.price?.amount)
    }

    @Test
    fun initializeDefaultPricingsDoesNotOverwriteExisting() = runBlocking {
        val initialPricing = RoutePricing(
            id = "price-custom",
            routeId = "route-sby-jbr",
            originTerminalId = "term_purabaya",
            destinationTerminalId = "term_tawangalun",
            originCity = "Surabaya",
            destinationCity = "Jember",
            price = Money.rupiah(120_000L)
        )
        saveRoutePricingUseCase(initialPricing)

        val result = initializeDefaultRoutePricingsUseCase(sampleRoute)
        assertEquals(1, result.size)
        assertEquals(120_000L, result.first().price.amount)
    }

    @Test
    fun saveRoutePricingUpdatesCustomPrice() = runBlocking {
        initializeDefaultRoutePricingsUseCase(sampleRoute)

        val customPricing = RoutePricing(
            id = "price-route-sby-jbr-term_purabaya-term_tawangalun",
            routeId = "route-sby-jbr",
            originTerminalId = "term_purabaya",
            destinationTerminalId = "term_tawangalun",
            originCity = "Surabaya",
            destinationCity = "Jember",
            price = Money.rupiah(110_000L)
        )
        saveRoutePricingUseCase(customPricing)

        val updated = pricingRepository.getPricingBetween("route-sby-jbr", "term_purabaya", "term_tawangalun")
        assertNotNull(updated)
        assertEquals(110_000L, updated?.price?.amount)
    }

    @Test
    fun getPricingsForRouteReturnsFlowOfPricings() = runBlocking {
        initializeDefaultRoutePricingsUseCase(sampleRoute)

        val pricings = getRoutePricingsUseCase("route-sby-jbr").first()
        assertTrue(pricings.isNotEmpty())
        assertEquals(6, pricings.size)
    }

    private class FakeRoutePricingDao : RoutePricingDao {
        private val pricingMap = mutableMapOf<String, RoutePricingEntity>()
        private val pricingFlow = MutableStateFlow<List<RoutePricingEntity>>(emptyList())

        override fun getPricingsByRouteFlow(routeId: String): Flow<List<RoutePricingEntity>> {
            val flow = MutableStateFlow<List<RoutePricingEntity>>(emptyList())
            flow.value = pricingMap.values.filter { it.routeId == routeId }
            return flow
        }

        override suspend fun getPricingsByRoute(routeId: String): List<RoutePricingEntity> {
            return pricingMap.values.filter { it.routeId == routeId }
        }

        override suspend fun getPricing(
            routeId: String,
            originId: String,
            destinationId: String
        ): RoutePricingEntity? {
            return pricingMap.values.find {
                it.routeId == routeId &&
                        it.originTerminalId == originId &&
                        it.destinationTerminalId == destinationId
            }
        }

        override suspend fun insertPricing(pricing: RoutePricingEntity) {
            pricingMap[pricing.id] = pricing
            pricingFlow.value = pricingMap.values.toList()
        }

        override suspend fun insertPricings(pricings: List<RoutePricingEntity>) {
            pricings.forEach { pricingMap[it.id] = it }
            pricingFlow.value = pricingMap.values.toList()
        }

        override suspend fun deletePricingsByRoute(routeId: String): Int {
            val count = pricingMap.values.count { it.routeId == routeId }
            val keysToRemove = pricingMap.filter { it.value.routeId == routeId }.keys
            keysToRemove.forEach { pricingMap.remove(it) }
            pricingFlow.value = pricingMap.values.toList()
            return count
        }
    }
}
