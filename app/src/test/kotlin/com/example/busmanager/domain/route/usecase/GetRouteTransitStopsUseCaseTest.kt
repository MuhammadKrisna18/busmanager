package com.example.busmanager.domain.route.usecase

import com.example.busmanager.data.repository.RouteCatalogRepositoryImpl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetRouteTransitStopsUseCaseTest {

    private lateinit var useCase: GetRouteTransitStopsUseCase

    @Before
    fun setUp() {
        val repository = RouteCatalogRepositoryImpl()
        useCase = GetRouteTransitStopsUseCase(repository)
    }

    @Test
    fun invoke_invalidRouteId_shouldReturnEmpty() {
        val stops = useCase("invalid_route_id")
        assertTrue(stops.isEmpty())
    }

    @Test
    fun invoke_validPanturaRoute_shouldReturnStopsInCorrectOrder() {
        val stops = useCase("route_sby_bwi_pantura_toll")
        assertEquals(4, stops.size)

        assertEquals("term_purabaya", stops[0].terminal.id)
        assertTrue(stops[0].isOrigin)
        assertFalse(stops[0].isTransit)
        assertFalse(stops[0].isDestination)

        assertEquals("term_bayuangga", stops[1].terminal.id)
        assertFalse(stops[1].isOrigin)
        assertTrue(stops[1].isTransit)
        assertFalse(stops[1].isDestination)

        assertEquals("term_situbondo", stops[2].terminal.id)
        assertFalse(stops[2].isOrigin)
        assertTrue(stops[2].isTransit)
        assertFalse(stops[2].isDestination)

        assertEquals("term_sritanjung", stops[3].terminal.id)
        assertFalse(stops[3].isOrigin)
        assertFalse(stops[3].isTransit)
        assertTrue(stops[3].isDestination)
    }
}
