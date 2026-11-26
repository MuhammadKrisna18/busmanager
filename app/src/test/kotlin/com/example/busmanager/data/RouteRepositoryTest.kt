package com.example.busmanager.data

import com.example.busmanager.data.repository.RouteCatalogRepositoryImpl
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.RoutePathType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RouteRepositoryTest {

    private lateinit var repository: RouteCatalogRepositoryImpl
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        repository = RouteCatalogRepositoryImpl(ioDispatcher = testDispatcher)
    }

    @Test
    fun getAllSegments_shouldReturnAllSegmentsWithWaypoints() {
        val segments = repository.getAllSegments()
        assertTrue(segments.size >= 20)
        assertTrue(segments.all { it.waypoints.isNotEmpty() })
    }

    @Test
    fun getSegmentsBetween_shouldReturnTollAndNonTollOptions() {
        val segments = repository.getSegmentsBetween("term_purabaya", "term_bayuangga")
        assertEquals(2, segments.size)

        val hasToll = segments.any { it.pathType == RoutePathType.TOLL }
        val hasNonToll = segments.any { it.pathType == RoutePathType.NON_TOLL }
        assertTrue(hasToll)
        assertTrue(hasNonToll)
    }

    @Test
    fun getSegmentById_shouldReturnMatchingSegment() {
        val segment = repository.getSegmentById("seg_sby_mlg_toll")
        assertNotNull(segment)
        assertEquals(RoutePathType.TOLL, segment?.pathType)
        assertEquals("term_purabaya", segment?.fromTerminalId)
        assertEquals("term_arjosari", segment?.toTerminalId)
    }

    @Test
    fun getSegmentById_invalidId_shouldReturnNull() {
        val segment = repository.getSegmentById("seg_invalid")
        assertNull(segment)
    }

    @Test
    fun findRoutes_surabayaToBanyuwangi_shouldReturnMultipleCorridors() {
        val routes = repository.findRoutes("term_purabaya", "term_sritanjung")
        assertTrue(routes.size >= 4)

        val panturaRoutes = routes.filter { it.transitTerminals.any { t -> t.id == "term_situbondo" } }
        val selatanRoutes = routes.filter { it.transitTerminals.any { t -> t.id == "term_tawangalun" } }

        assertTrue(panturaRoutes.isNotEmpty())
        assertTrue(selatanRoutes.isNotEmpty())
    }

    @Test
    fun findRoutes_withPreferredPathType_shouldFilterCorrectly() {
        val tollRoutes = repository.findRoutes(
            originTerminalId = "term_purabaya",
            destinationTerminalId = "term_arjosari",
            preferredPathType = RoutePathType.TOLL
        )
        assertEquals(1, tollRoutes.size)
        assertEquals(RoutePathType.TOLL, tollRoutes.first().pathType)
    }

    @Test
    fun getConnectedTerminalIds_fromPurabaya_shouldReturnConnectedTerminals() {
        val connectedIds = repository.getConnectedTerminalIds("term_purabaya")
        assertTrue(connectedIds.contains("term_arjosari"))
        assertTrue(connectedIds.contains("term_bayuangga"))
        assertTrue(connectedIds.contains("term_kertajaya"))
        assertEquals(3, connectedIds.size)
    }

    @Test
    fun southernCorridor_connectsKediriTulungagungBlitarMalang() {
        val kediriToTulungagung = repository.getSegmentsBetween("term_tamanan", "term_gayatri")
        assertTrue(kediriToTulungagung.isNotEmpty())

        val tulungagungToBlitar = repository.getSegmentsBetween("term_gayatri", "term_patria")
        assertTrue(tulungagungToBlitar.isNotEmpty())

        val blitarToMalang = repository.getSegmentsBetween("term_patria", "term_arjosari")
        assertTrue(blitarToMalang.isNotEmpty())
    }

    @Test
    fun saveRoute_and_getAllRoutes_shouldIncludeCustomRoute() = runTest(testDispatcher) {
        val baseRoute = repository.getAllPredefinedRoutes().first()
        val customRoute = BusRoute(
            id = "custom_test_route",
            name = "Rute Khusus Test",
            originTerminal = baseRoute.originTerminal,
            destinationTerminal = baseRoute.destinationTerminal,
            pathType = baseRoute.pathType,
            segments = baseRoute.segments,
            transitTerminals = baseRoute.transitTerminals
        )

        val beforeCount = repository.getAllRoutes().size
        repository.saveRoute(customRoute)
        advanceUntilIdle()
        val afterCount = repository.getAllRoutes().size

        assertEquals(beforeCount + 1, afterCount)
        val fetched = repository.getRouteById("custom_test_route")
        assertNotNull(fetched)
        assertEquals("Rute Khusus Test", fetched?.name)
    }

    @Test
    fun customRouteDao_persistsAndRestoresOnNewRepositoryInstance() = runTest(testDispatcher) {
        val fakeDao = FakeCustomRouteDao()
        val terminalRepo = com.example.busmanager.data.repository.TerminalCatalogRepositoryImpl()
        val repo1 = RouteCatalogRepositoryImpl(
            customRouteDao = fakeDao,
            terminalRepository = terminalRepo,
            ioDispatcher = testDispatcher
        )

        val baseRoute = repo1.getAllPredefinedRoutes().first()
        val customRoute = BusRoute(
            id = "custom_persisted_route",
            name = "Rute Surabaya - Malang Khusus",
            originTerminal = baseRoute.originTerminal,
            destinationTerminal = baseRoute.destinationTerminal,
            pathType = baseRoute.pathType,
            segments = baseRoute.segments,
            transitTerminals = baseRoute.transitTerminals
        )

        repo1.saveRoute(customRoute)
        advanceUntilIdle()
        assertEquals(1, fakeDao.list.size)
        assertEquals("custom_persisted_route", fakeDao.list.first().id)

        val repo2 = RouteCatalogRepositoryImpl(
            customRouteDao = fakeDao,
            terminalRepository = terminalRepo,
            ioDispatcher = testDispatcher
        )
        advanceUntilIdle()
        val restored = repo2.getRouteById("custom_persisted_route")
        assertNotNull(restored)
        assertEquals("Rute Surabaya - Malang Khusus", restored?.name)

        repo2.deleteRoute("custom_persisted_route")
        advanceUntilIdle()
        assertEquals(0, fakeDao.list.size)
        assertNull(repo2.getRouteById("custom_persisted_route"))
    }

    @Test
    fun deleteAllCustomRoutes_clearsCustomRoutesFromDao() = runTest(testDispatcher) {
        val fakeDao = FakeCustomRouteDao()
        val terminalRepo = com.example.busmanager.data.repository.TerminalCatalogRepositoryImpl()
        val repo = RouteCatalogRepositoryImpl(
            customRouteDao = fakeDao,
            terminalRepository = terminalRepo,
            ioDispatcher = testDispatcher
        )

        val baseRoute = repo.getAllPredefinedRoutes().first()
        val customRoute = BusRoute(
            id = "route_to_clear",
            name = "Rute Hapus",
            originTerminal = baseRoute.originTerminal,
            destinationTerminal = baseRoute.destinationTerminal,
            pathType = baseRoute.pathType,
            segments = baseRoute.segments,
            transitTerminals = baseRoute.transitTerminals
        )

        repo.saveRoute(customRoute)
        advanceUntilIdle()
        assertEquals(1, fakeDao.list.size)

        repo.deleteAllCustomRoutes()
        advanceUntilIdle()
        assertEquals(0, fakeDao.list.size)
        assertNull(repo.getRouteById("route_to_clear"))
    }

    private class FakeCustomRouteDao : com.example.busmanager.data.local.dao.CustomRouteDao {
        val list = mutableListOf<com.example.busmanager.data.local.entity.CustomRouteEntity>()
        private val flow = kotlinx.coroutines.flow.MutableStateFlow<List<com.example.busmanager.data.local.entity.CustomRouteEntity>>(emptyList())

        init {
            updateFlow()
        }

        private fun updateFlow() {
            flow.value = list.toList()
        }

        override fun getAllCustomRoutesFlow(): kotlinx.coroutines.flow.Flow<List<com.example.busmanager.data.local.entity.CustomRouteEntity>> = flow

        override suspend fun getAllCustomRoutes(): List<com.example.busmanager.data.local.entity.CustomRouteEntity> = list.toList()

        override suspend fun getCustomRouteById(id: String): com.example.busmanager.data.local.entity.CustomRouteEntity? =
            list.find { it.id == id }

        override suspend fun insertCustomRoute(route: com.example.busmanager.data.local.entity.CustomRouteEntity) {
            list.removeAll { it.id == route.id }
            list.add(route)
            updateFlow()
        }

        override suspend fun deleteCustomRouteById(id: String): Int {
            val removed = list.removeAll { it.id == id }
            updateFlow()
            return if (removed) 1 else 0
        }

        override suspend fun deleteAllCustomRoutes(): Int {
            val count = list.size
            list.clear()
            updateFlow()
            return count
        }
    }
}
