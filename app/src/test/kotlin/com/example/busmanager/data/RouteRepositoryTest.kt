package com.example.busmanager.data

import com.example.busmanager.data.repository.RouteCatalogRepositoryImpl
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
}
