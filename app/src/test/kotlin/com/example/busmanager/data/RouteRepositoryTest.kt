package com.example.busmanager.data

import com.example.busmanager.data.repository.RouteCatalogRepositoryImpl
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.RoutePathType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RouteRepositoryTest {

    private lateinit var repository: RouteCatalogRepositoryImpl

    @Before
    fun setUp() {
        repository = RouteCatalogRepositoryImpl()
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
    fun getConnectedTerminalIds_fromPurabaya_shouldReturnBayuanggaAndArjosari() {
        val connectedIds = repository.getConnectedTerminalIds("term_purabaya")
        assertTrue(connectedIds.contains("term_arjosari"))
        assertTrue(connectedIds.contains("term_bayuangga"))
        assertEquals(2, connectedIds.size)
    }

    @Test
    fun saveRoute_and_getAllRoutes_shouldIncludeCustomRoute() {
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
        val afterCount = repository.getAllRoutes().size

        assertEquals(beforeCount + 1, afterCount)
        val fetched = repository.getRouteById("custom_test_route")
        assertNotNull(fetched)
        assertEquals("Rute Khusus Test", fetched?.name)
    }

    @Test
    fun customRouteDao_persistsAndRestoresOnNewRepositoryInstance() {
        val fakeDao = FakeCustomRouteDao()
        val terminalRepo = com.example.busmanager.data.repository.TerminalCatalogRepositoryImpl()
        val repo1 = RouteCatalogRepositoryImpl(
            customRouteDao = fakeDao,
            terminalRepository = terminalRepo
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
        assertEquals(1, fakeDao.list.size)
        assertEquals("custom_persisted_route", fakeDao.list.first().id)

        val repo2 = RouteCatalogRepositoryImpl(
            customRouteDao = fakeDao,
            terminalRepository = terminalRepo
        )
        val restored = repo2.getRouteById("custom_persisted_route")
        assertNotNull(restored)
        assertEquals("Rute Surabaya - Malang Khusus", restored?.name)

        repo2.deleteRoute("custom_persisted_route")
        assertEquals(0, fakeDao.list.size)
        assertNull(repo2.getRouteById("custom_persisted_route"))
    }

    @Test
    fun deleteAllCustomRoutes_clearsCustomRoutesFromDao() {
        val fakeDao = FakeCustomRouteDao()
        val terminalRepo = com.example.busmanager.data.repository.TerminalCatalogRepositoryImpl()
        val repo = RouteCatalogRepositoryImpl(
            customRouteDao = fakeDao,
            terminalRepository = terminalRepo
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
        assertEquals(1, fakeDao.list.size)

        repo.deleteAllCustomRoutes()
        assertEquals(0, fakeDao.list.size)
        assertNull(repo.getRouteById("route_to_clear"))
    }

    private class FakeCustomRouteDao : com.example.busmanager.data.local.dao.CustomRouteDao {
        val list = mutableListOf<com.example.busmanager.data.local.entity.CustomRouteEntity>()

        override fun getAllCustomRoutesFlow(): kotlinx.coroutines.flow.Flow<List<com.example.busmanager.data.local.entity.CustomRouteEntity>> =
            kotlinx.coroutines.flow.flowOf(list.toList())

        override fun getAllCustomRoutesSync(): List<com.example.busmanager.data.local.entity.CustomRouteEntity> = list.toList()

        override suspend fun getAllCustomRoutes(): List<com.example.busmanager.data.local.entity.CustomRouteEntity> = list.toList()

        override suspend fun getCustomRouteById(id: String): com.example.busmanager.data.local.entity.CustomRouteEntity? =
            list.find { it.id == id }

        override fun getCustomRouteByIdSync(id: String): com.example.busmanager.data.local.entity.CustomRouteEntity? =
            list.find { it.id == id }

        override suspend fun insertCustomRoute(route: com.example.busmanager.data.local.entity.CustomRouteEntity) {
            list.removeAll { it.id == route.id }
            list.add(route)
        }

        override fun insertCustomRouteSync(route: com.example.busmanager.data.local.entity.CustomRouteEntity) {
            list.removeAll { it.id == route.id }
            list.add(route)
        }

        override fun deleteCustomRouteById(id: String): Int {
            val removed = list.removeAll { it.id == id }
            return if (removed) 1 else 0
        }

        override fun deleteAllCustomRoutes(): Int {
            val count = list.size
            list.clear()
            return count
        }
    }
}
