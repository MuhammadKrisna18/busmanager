package com.example.busmanager.presentation.route.components

import com.example.busmanager.domain.route.model.MapCoordinate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.sqrt

class BusMarkerAntiOverlapCalculatorTest {

    private data class DummyMarker(
        val id: String,
        val coordinate: MapCoordinate
    )

    private fun createCoordinate(x: Float, y: Float): MapCoordinate {
        return MapCoordinate(x = x, y = y, latitude = 0.0, longitude = 0.0)
    }

    @Test
    fun emptyList_returnsEmptyMap() {
        val result = BusMarkerAntiOverlapCalculator.calculateOffsets(
            items = emptyList<DummyMarker>(),
            coordinateSelector = { it.coordinate },
            canvasWidth = 1000f,
            canvasHeight = 600f
        )
        assertTrue(result.isEmpty())
    }

    @Test
    fun singleBus_returnsZeroOffset() {
        val bus = DummyMarker("bus-1", createCoordinate(0.5f, 0.5f))
        val result = BusMarkerAntiOverlapCalculator.calculateOffsets(
            items = listOf(bus),
            coordinateSelector = { it.coordinate },
            canvasWidth = 1000f,
            canvasHeight = 600f
        )
        assertEquals(1, result.size)
        assertEquals(0f, result[bus]?.dxPx ?: -1f, 0.001f)
        assertEquals(0f, result[bus]?.dyPx ?: -1f, 0.001f)
    }

    @Test
    fun twoBusesDistant_returnsZeroOffsets() {
        val bus1 = DummyMarker("bus-1", createCoordinate(0.1f, 0.1f))
        val bus2 = DummyMarker("bus-2", createCoordinate(0.9f, 0.9f))
        val result = BusMarkerAntiOverlapCalculator.calculateOffsets(
            items = listOf(bus1, bus2),
            coordinateSelector = { it.coordinate },
            canvasWidth = 1000f,
            canvasHeight = 600f
        )
        assertEquals(0f, result[bus1]?.dxPx ?: -1f, 0.001f)
        assertEquals(0f, result[bus1]?.dyPx ?: -1f, 0.001f)
        assertEquals(0f, result[bus2]?.dxPx ?: -1f, 0.001f)
        assertEquals(0f, result[bus2]?.dyPx ?: -1f, 0.001f)
    }

    @Test
    fun twoBusesColliding_returnsOpposingHorizontalOffsets() {
        val bus1 = DummyMarker("bus-1", createCoordinate(0.35f, 0.65f))
        val bus2 = DummyMarker("bus-2", createCoordinate(0.35f, 0.65f))
        val result = BusMarkerAntiOverlapCalculator.calculateOffsets(
            items = listOf(bus1, bus2),
            coordinateSelector = { it.coordinate },
            canvasWidth = 1000f,
            canvasHeight = 600f,
            collisionRadiusPx = 32f,
            spreadRadiusPx = 18f
        )
        val offset1 = result[bus1]!!
        val offset2 = result[bus2]!!

        assertEquals(-18f, offset1.dxPx, 0.001f)
        assertEquals(0f, offset1.dyPx, 0.001f)
        assertEquals(18f, offset2.dxPx, 0.001f)
        assertEquals(0f, offset2.dyPx, 0.001f)

        val separation = offset2.dxPx - offset1.dxPx
        assertEquals(36f, separation, 0.001f)
    }

    @Test
    fun threeBusesColliding_returnsDistinctRadialOffsets() {
        val bus1 = DummyMarker("bus-1", createCoordinate(0.5f, 0.5f))
        val bus2 = DummyMarker("bus-2", createCoordinate(0.5f, 0.5f))
        val bus3 = DummyMarker("bus-3", createCoordinate(0.5f, 0.5f))
        val result = BusMarkerAntiOverlapCalculator.calculateOffsets(
            items = listOf(bus1, bus2, bus3),
            coordinateSelector = { it.coordinate },
            canvasWidth = 1000f,
            canvasHeight = 600f,
            spreadRadiusPx = 20f
        )

        val o1 = result[bus1]!!
        val o2 = result[bus2]!!
        val o3 = result[bus3]!!

        assertNotEquals(o1, o2)
        assertNotEquals(o2, o3)
        assertNotEquals(o1, o3)

        val dist12 = sqrt((o1.dxPx - o2.dxPx) * (o1.dxPx - o2.dxPx) + (o1.dyPx - o2.dyPx) * (o1.dyPx - o2.dyPx))
        val dist23 = sqrt((o2.dxPx - o3.dxPx) * (o2.dxPx - o3.dxPx) + (o2.dyPx - o3.dyPx) * (o2.dyPx - o3.dyPx))
        val dist31 = sqrt((o3.dxPx - o1.dxPx) * (o3.dxPx - o1.dxPx) + (o3.dyPx - o1.dyPx) * (o3.dyPx - o1.dyPx))

        assertTrue(dist12 > 15f)
        assertTrue(dist23 > 15f)
        assertTrue(dist31 > 15f)
    }

    @Test
    fun invalidCanvasDimensions_returnsZeroOffsetsSafely() {
        val bus1 = DummyMarker("bus-1", createCoordinate(0.5f, 0.5f))
        val bus2 = DummyMarker("bus-2", createCoordinate(0.5f, 0.5f))
        val result = BusMarkerAntiOverlapCalculator.calculateOffsets(
            items = listOf(bus1, bus2),
            coordinateSelector = { it.coordinate },
            canvasWidth = 0f,
            canvasHeight = 0f
        )
        assertEquals(0f, result[bus1]?.dxPx ?: -1f, 0.001f)
        assertEquals(0f, result[bus2]?.dxPx ?: -1f, 0.001f)
    }
}
