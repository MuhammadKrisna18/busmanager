package com.example.busmanager.domain.route

import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.MapCoordinate
import com.example.busmanager.domain.route.model.RoadCharacteristic
import com.example.busmanager.domain.route.model.RoutePathType
import com.example.busmanager.domain.route.model.RouteSegment
import com.example.busmanager.domain.route.model.Terminal
import com.example.busmanager.domain.route.model.TerminalType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteModelTest {

    private val purabayaTerminal = Terminal(
        id = "term_purabaya",
        name = "Terminal Purabaya",
        city = "Surabaya",
        type = TerminalType.TIPE_A,
        coordinate = MapCoordinate(0.25f, 0.20f, -7.35, 112.72),
        retributionFee = Money.rupiah(5000),
        passengerDemandMultiplier = 1.5f
    )

    private val bayuanggaTerminal = Terminal(
        id = "term_bayuangga",
        name = "Terminal Bayuangga",
        city = "Probolinggo",
        type = TerminalType.TIPE_A,
        coordinate = MapCoordinate(0.48f, 0.42f, -7.76, 113.19),
        retributionFee = Money.rupiah(3000),
        passengerDemandMultiplier = 1.1f
    )

    private val sritanjungTerminal = Terminal(
        id = "term_sritanjung",
        name = "Terminal Sritanjung",
        city = "Banyuwangi",
        type = TerminalType.TIPE_A,
        coordinate = MapCoordinate(0.92f, 0.65f, -8.13, 114.39),
        retributionFee = Money.rupiah(4000),
        passengerDemandMultiplier = 1.2f
    )

    @Test
    fun validMapCoordinate_shouldHoldCorrectNormalizedValues() {
        val coord = MapCoordinate(0.5f, 0.5f, -7.0, 112.0)
        assertEquals(0.5f, coord.x, 0.001f)
        assertEquals(0.5f, coord.y, 0.001f)
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidMapCoordinateX_shouldThrowException() {
        MapCoordinate(1.2f, 0.5f, -7.0, 112.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidMapCoordinateY_shouldThrowException() {
        MapCoordinate(0.5f, -0.1f, -7.0, 112.0)
    }

    @Test
    fun routeSegment_shouldCalculateMetricsAccurately() {
        val segment = RouteSegment(
            id = "seg_sby_probo_toll",
            fromTerminalId = purabayaTerminal.id,
            toTerminalId = bayuanggaTerminal.id,
            pathType = RoutePathType.TOLL,
            characteristic = RoadCharacteristic.FLAT,
            distanceKm = 100.0,
            baseDurationMinutes = 75,
            tollFee = Money.rupiah(65000),
            waypoints = listOf(
                MapCoordinate(0.25f, 0.20f, -7.35, 112.72),
                MapCoordinate(0.48f, 0.42f, -7.76, 113.19)
            )
        )

        assertEquals("seg_sby_probo_toll", segment.id)
        assertEquals(100.0, segment.distanceKm, 0.001)
        assertEquals(75, segment.baseDurationMinutes)
        assertEquals(Money.rupiah(65000), segment.tollFee)
        assertEquals(2, segment.waypoints.size)
        assertTrue(segment.wearTearRate < 1.0f)
    }

    @Test
    fun busRoute_shouldAggregateTotalsAndProvideTransitStops() {
        val segment1 = RouteSegment(
            id = "seg_sby_probo",
            fromTerminalId = purabayaTerminal.id,
            toTerminalId = bayuanggaTerminal.id,
            pathType = RoutePathType.TOLL,
            characteristic = RoadCharacteristic.FLAT,
            distanceKm = 100.0,
            baseDurationMinutes = 75,
            tollFee = Money.rupiah(65000),
            waypoints = listOf(MapCoordinate(0.25f, 0.20f, -7.35, 112.72))
        )
        val segment2 = RouteSegment(
            id = "seg_probo_bwi",
            fromTerminalId = bayuanggaTerminal.id,
            toTerminalId = sritanjungTerminal.id,
            pathType = RoutePathType.NON_TOLL,
            characteristic = RoadCharacteristic.COASTAL,
            distanceKm = 190.0,
            baseDurationMinutes = 240,
            tollFee = Money.ZERO,
            waypoints = listOf(MapCoordinate(0.92f, 0.65f, -8.13, 114.39))
        )

        val route = BusRoute(
            id = "route_sby_bwi_pantura",
            name = "Surabaya - Banyuwangi via Pantura",
            originTerminal = purabayaTerminal,
            destinationTerminal = sritanjungTerminal,
            pathType = RoutePathType.NON_TOLL,
            segments = listOf(segment1, segment2),
            transitTerminals = listOf(bayuanggaTerminal)
        )

        assertEquals(290.0, route.totalDistanceKm, 0.001)
        assertEquals(Money.rupiah(65000), route.totalTollFee)
        assertEquals(315, route.totalEstimatedDurationMinutes)
        assertEquals(2, route.allWaypoints.size)

        assertTrue(route.containsTerminal("term_purabaya"))
        assertTrue(route.containsTerminal("term_bayuangga"))
        assertTrue(route.containsTerminal("term_sritanjung"))
        assertFalse(route.containsTerminal("term_arjosari"))

        val stops = route.toTransitStops()
        assertEquals(3, stops.size)
        assertTrue(stops[0].isOrigin)
        assertFalse(stops[0].isTransit)
        assertEquals(purabayaTerminal, stops[0].terminal)

        assertFalse(stops[1].isOrigin)
        assertTrue(stops[1].isTransit)
        assertFalse(stops[1].isDestination)
        assertEquals(bayuanggaTerminal, stops[1].terminal)

        assertTrue(stops[2].isDestination)
        assertFalse(stops[2].isTransit)
        assertEquals(sritanjungTerminal, stops[2].terminal)
    }
}
