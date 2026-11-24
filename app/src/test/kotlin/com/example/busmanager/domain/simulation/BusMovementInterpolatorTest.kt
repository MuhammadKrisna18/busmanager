package com.example.busmanager.domain.simulation

import com.example.busmanager.domain.route.model.MapCoordinate
import com.example.busmanager.domain.simulation.model.BusMovementInterpolator
import com.example.busmanager.domain.simulation.model.RouteDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BusMovementInterpolatorTest {

    @Test
    fun emptyWaypoints_returnsDefaultCoordinate() {
        val coord = BusMovementInterpolator.interpolatePosition(emptyList(), 0.5f)
        assertEquals(0.5f, coord.x, 0.001f)
        assertEquals(0.5f, coord.y, 0.001f)
    }

    @Test
    fun singleWaypoint_returnsThatWaypoint() {
        val wp = MapCoordinate(0.2f, 0.4f, -7.0, 112.0)
        val coord = BusMovementInterpolator.interpolatePosition(listOf(wp), 0.5f)
        assertEquals(wp, coord)
    }

    @Test
    fun zeroProgress_returnsFirstWaypoint() {
        val wp1 = MapCoordinate(0.1f, 0.1f, -7.1, 112.1)
        val wp2 = MapCoordinate(0.9f, 0.9f, -7.9, 112.9)
        val coord = BusMovementInterpolator.interpolatePosition(listOf(wp1, wp2), 0.0f)
        assertEquals(wp1.x, coord.x, 0.001f)
        assertEquals(wp1.y, coord.y, 0.001f)
    }

    @Test
    fun fullProgress_returnsLastWaypoint() {
        val wp1 = MapCoordinate(0.1f, 0.1f, -7.1, 112.1)
        val wp2 = MapCoordinate(0.9f, 0.9f, -7.9, 112.9)
        val coord = BusMovementInterpolator.interpolatePosition(listOf(wp1, wp2), 1.0f)
        assertEquals(wp2.x, coord.x, 0.001f)
        assertEquals(wp2.y, coord.y, 0.001f)
    }

    @Test
    fun halfProgress_interpolatesMidpoint() {
        val wp1 = MapCoordinate(0.0f, 0.0f, -7.0, 112.0)
        val wp2 = MapCoordinate(1.0f, 1.0f, -8.0, 113.0)
        val coord = BusMovementInterpolator.interpolatePosition(listOf(wp1, wp2), 0.5f)
        assertEquals(0.5f, coord.x, 0.001f)
        assertEquals(0.5f, coord.y, 0.001f)
    }

    @Test
    fun reverseDirection_startsAtLastAndEndsAtFirst() {
        val wp1 = MapCoordinate(0.1f, 0.1f, -7.1, 112.1)
        val wp2 = MapCoordinate(0.9f, 0.9f, -7.9, 112.9)
        val start = BusMovementInterpolator.interpolatePosition(
            listOf(wp1, wp2),
            0.0f,
            RouteDirection.DESTINATION_TO_ORIGIN
        )
        val end = BusMovementInterpolator.interpolatePosition(
            listOf(wp1, wp2),
            1.0f,
            RouteDirection.DESTINATION_TO_ORIGIN
        )

        assertEquals(wp2.x, start.x, 0.001f)
        assertEquals(wp1.x, end.x, 0.001f)
    }

    @Test
    fun multiSegments_progressTravelsCorrectly() {
        val wp1 = MapCoordinate(0.0f, 0.0f, -7.0, 112.0)
        val wp2 = MapCoordinate(0.5f, 0.0f, -7.0, 112.5)
        val wp3 = MapCoordinate(0.5f, 0.5f, -7.5, 112.5)

        val coord = BusMovementInterpolator.interpolatePosition(listOf(wp1, wp2, wp3), 0.5f)
        assertEquals(0.5f, coord.x, 0.001f)
        assertEquals(0.0f, coord.y, 0.001f)

        val coord3Quarter = BusMovementInterpolator.interpolatePosition(listOf(wp1, wp2, wp3), 0.75f)
        assertEquals(0.5f, coord3Quarter.x, 0.001f)
        assertEquals(0.25f, coord3Quarter.y, 0.001f)
    }

    @Test
    fun calculateHeadingAngle_movingEast_returns90Degrees() {
        val wp1 = MapCoordinate(0.0f, 0.5f, -7.0, 112.0)
        val wp2 = MapCoordinate(1.0f, 0.5f, -7.0, 113.0)
        val angle = BusMovementInterpolator.calculateHeadingAngle(listOf(wp1, wp2), 0.3f)
        assertEquals(90.0f, angle, 0.5f)
    }

    @Test
    fun calculateHeadingAngle_movingSouth_returns180Degrees() {
        val wp1 = MapCoordinate(0.5f, 0.0f, -7.0, 112.0)
        val wp2 = MapCoordinate(0.5f, 1.0f, -8.0, 112.0)
        val angle = BusMovementInterpolator.calculateHeadingAngle(listOf(wp1, wp2), 0.5f)
        assertEquals(180.0f, angle, 0.5f)
    }

    @Test
    fun calculateHeadingAngle_reverseDirection_invertsAngle() {
        val wp1 = MapCoordinate(0.0f, 0.5f, -7.0, 112.0)
        val wp2 = MapCoordinate(1.0f, 0.5f, -7.0, 113.0)
        val angle = BusMovementInterpolator.calculateHeadingAngle(
            listOf(wp1, wp2),
            0.3f,
            RouteDirection.DESTINATION_TO_ORIGIN
        )
        assertEquals(270.0f, angle, 0.5f)
    }

    @Test
    fun calculateHeadingAngle_insufficientWaypoints_returnsZero() {
        val wp1 = MapCoordinate(0.5f, 0.5f, -7.0, 112.0)
        val angleEmpty = BusMovementInterpolator.calculateHeadingAngle(emptyList(), 0.5f)
        val angleSingle = BusMovementInterpolator.calculateHeadingAngle(listOf(wp1), 0.5f)
        assertEquals(0.0f, angleEmpty, 0.001f)
        assertEquals(0.0f, angleSingle, 0.001f)
    }
}
