package com.example.busmanager.domain.route

import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.DailySchedulePlan
import com.example.busmanager.domain.route.model.DepartureSlotCalculator
import com.example.busmanager.domain.route.model.OccupiedSlotInfo
import com.example.busmanager.domain.route.model.RouteCategory
import com.example.busmanager.domain.route.model.RoutePathType
import com.example.busmanager.domain.simulation.model.RouteDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DepartureSlotCalculatorTest {

    @Test
    fun generate24HourSlots_producesExactly96SlotsPerTerminal() {
        val slots = DepartureSlotCalculator.generate24HourSlots(
            terminalId = "term_purabaya",
            targetDestinationCity = "Malang"
        )

        assertEquals(96, slots.size)
        assertEquals("00:00", slots.first().timeFormatted)
        assertEquals("23:45", slots.last().timeFormatted)
        assertTrue(slots.all { it.isAvailable })
    }

    @Test
    fun generate24HourSlots_marksSlotUnavailableWhenSameDestinationCityConflicts() {
        val occupied = listOf(
            OccupiedSlotInfo(
                terminalId = "term_purabaya",
                hour = 7,
                minute = 0,
                destinationCity = "Malang",
                routeId = "route_sby_mlg_1",
                busCode = "TN-01"
            )
        )

        val slots = DepartureSlotCalculator.generate24HourSlots(
            terminalId = "term_purabaya",
            targetDestinationCity = "Malang",
            existingOccupiedSlots = occupied
        )

        val slot0700 = slots.find { it.hour == 7 && it.minute == 0 }
        assertNotNull(slot0700)
        assertFalse(slot0700!!.isAvailable)
        assertEquals("route_sby_mlg_1", slot0700.occupiedByRouteId)
        assertEquals("TN-01", slot0700.occupiedBusCode)

        val slot0715 = slots.find { it.hour == 7 && it.minute == 15 }
        assertNotNull(slot0715)
        assertTrue(slot0715!!.isAvailable)
    }

    @Test
    fun generate24HourSlots_keepsSlotAvailableWhenOccupiedByDifferentDestinationCity() {
        val occupied = listOf(
            OccupiedSlotInfo(
                terminalId = "term_purabaya",
                hour = 7,
                minute = 0,
                destinationCity = "Jember",
                routeId = "route_sby_jbr_1",
                busCode = "TN-05"
            )
        )

        val slots = DepartureSlotCalculator.generate24HourSlots(
            terminalId = "term_purabaya",
            targetDestinationCity = "Malang",
            existingOccupiedSlots = occupied
        )

        val slot0700 = slots.find { it.hour == 7 && it.minute == 0 }
        assertNotNull(slot0700)
        assertTrue(slot0700!!.isAvailable)
    }

    @Test
    fun findNextAvailableSlot_returnsImmediateSlotIfFree() {
        val slots = DepartureSlotCalculator.generate24HourSlots(
            terminalId = "term_purabaya",
            targetDestinationCity = "Malang"
        )

        val result = DepartureSlotCalculator.findNextAvailableSlot(slots, fromHour = 8, fromMinute = 30)
        assertNotNull(result)
        assertEquals(8, result!!.hour)
        assertEquals(30, result.minute)
    }

    @Test
    fun findNextAvailableSlot_returnsNextAvailableSlotIfCurrentIsOccupied() {
        val occupied = listOf(
            OccupiedSlotInfo(
                terminalId = "term_purabaya",
                hour = 8,
                minute = 30,
                destinationCity = "Malang",
                routeId = "route_1"
            )
        )
        val slots = DepartureSlotCalculator.generate24HourSlots(
            terminalId = "term_purabaya",
            targetDestinationCity = "Malang",
            existingOccupiedSlots = occupied
        )

        val result = DepartureSlotCalculator.findNextAvailableSlot(slots, fromHour = 8, fromMinute = 30)
        assertNotNull(result)
        assertEquals(8, result!!.hour)
        assertEquals(45, result.minute)
    }

    @Test
    fun findNextAvailableSlot_wrapsAroundToEarliestAvailableWhenPastLastSlot() {
        val occupied = listOf(
            OccupiedSlotInfo(
                terminalId = "term_purabaya",
                hour = 23,
                minute = 45,
                destinationCity = "Malang",
                routeId = "route_late"
            )
        )
        val slots = DepartureSlotCalculator.generate24HourSlots(
            terminalId = "term_purabaya",
            targetDestinationCity = "Malang",
            existingOccupiedSlots = occupied
        )

        val result = DepartureSlotCalculator.findNextAvailableSlot(slots, fromHour = 23, fromMinute = 45)
        assertNotNull(result)
        assertEquals(0, result!!.hour)
        assertEquals(0, result.minute)
    }

    @Test
    fun alignToNext15MinuteSlot_preservesExact15MinuteBoundaries() {
        val (h1, m1) = DepartureSlotCalculator.alignToNext15MinuteSlot(7 * 60 + 0)
        assertEquals(7, h1)
        assertEquals(0, m1)

        val (h2, m2) = DepartureSlotCalculator.alignToNext15MinuteSlot(7 * 60 + 15)
        assertEquals(7, h2)
        assertEquals(15, m2)
    }

    @Test
    fun alignToNext15MinuteSlot_advancesToNextMultipleWhenOffset() {
        val (h1, m1) = DepartureSlotCalculator.alignToNext15MinuteSlot(7 * 60 + 1)
        assertEquals(7, h1)
        assertEquals(15, m1)

        val (h2, m2) = DepartureSlotCalculator.alignToNext15MinuteSlot(7 * 60 + 16)
        assertEquals(7, h2)
        assertEquals(30, m2)

        val (h3, m3) = DepartureSlotCalculator.alignToNext15MinuteSlot(23 * 60 + 50)
        assertEquals(0, h3)
        assertEquals(0, m3)
    }

    @Test
    fun calculateDailyPlan_akdp2RoundTrips_producesExpectedLegTimesWith30MinAnd1HourLayovers() {
        val plan: DailySchedulePlan = DepartureSlotCalculator.calculateDailyPlan(
            routeCategory = RouteCategory.AKDP,
            originTerminalId = "term_purabaya",
            destTerminalId = "term_arjosari",
            estimatedDurationMinutes = 90,
            departureHour = 7,
            departureMinute = 0,
            targetRitCount = 2
        )

        assertEquals(RouteCategory.AKDP, plan.routeCategory)
        assertEquals(2, plan.targetRitCount)
        assertEquals(4, plan.totalLegsCount)

        val leg1 = plan.outboundLeg1!!
        assertEquals(1, leg1.ritNumber)
        assertEquals(RouteDirection.ORIGIN_TO_DESTINATION, leg1.direction)
        assertEquals(7, leg1.departureHour)
        assertEquals(0, leg1.departureMinute)
        assertEquals(8, leg1.arrivalHour)
        assertEquals(30, leg1.arrivalMinute)
        assertEquals(30, leg1.layoverMinutesAfterArrival)

        val leg2 = plan.returnLeg1!!
        assertEquals(1, leg2.ritNumber)
        assertEquals(RouteDirection.DESTINATION_TO_ORIGIN, leg2.direction)
        assertEquals(9, leg2.departureHour)
        assertEquals(0, leg2.departureMinute)
        assertEquals(10, leg2.arrivalHour)
        assertEquals(30, leg2.arrivalMinute)
        assertEquals(45, leg2.layoverMinutesAfterArrival)

        val leg3 = plan.outboundLeg2!!
        assertEquals(2, leg3.ritNumber)
        assertEquals(RouteDirection.ORIGIN_TO_DESTINATION, leg3.direction)
        assertEquals(11, leg3.departureHour)
        assertEquals(15, leg3.departureMinute)
        assertEquals(12, leg3.arrivalHour)
        assertEquals(45, leg3.arrivalMinute)
        assertEquals(30, leg3.layoverMinutesAfterArrival)

        val leg4 = plan.returnLeg2!!
        assertEquals(2, leg4.ritNumber)
        assertEquals(RouteDirection.DESTINATION_TO_ORIGIN, leg4.direction)
        assertEquals(13, leg4.departureHour)
        assertEquals(15, leg4.departureMinute)
        assertEquals(14, leg4.arrivalHour)
        assertEquals(45, leg4.arrivalMinute)
        assertEquals(0, leg4.layoverMinutesAfterArrival)
    }

    @Test
    fun calculateDailyPlan_longestRoute270Min_completesWithinDayWithUnder5HoursRemaining() {
        val plan = DepartureSlotCalculator.calculateDailyPlan(
            routeCategory = RouteCategory.AKDP,
            originTerminalId = "term_purabaya",
            destTerminalId = "term_sritanjung",
            estimatedDurationMinutes = BusRoute.compressRouteDuration(360),
            departureHour = 4,
            departureMinute = 0,
            targetRitCount = 2
        )

        assertEquals(4, plan.totalLegsCount)

        val leg1 = plan.legs[0]
        assertEquals(4, leg1.departureHour)
        assertEquals(0, leg1.departureMinute)
        assertEquals(8, leg1.arrivalHour)
        assertEquals(30, leg1.arrivalMinute)
        assertEquals(30, leg1.layoverMinutesAfterArrival)

        val leg2 = plan.legs[1]
        assertEquals(9, leg2.departureHour)
        assertEquals(0, leg2.departureMinute)
        assertEquals(13, leg2.arrivalHour)
        assertEquals(30, leg2.arrivalMinute)
        assertEquals(45, leg2.layoverMinutesAfterArrival)

        val leg3 = plan.legs[2]
        assertEquals(14, leg3.departureHour)
        assertEquals(15, leg3.departureMinute)
        assertEquals(18, leg3.arrivalHour)
        assertEquals(45, leg3.arrivalMinute)
        assertEquals(30, leg3.layoverMinutesAfterArrival)

        val leg4 = plan.legs[3]
        assertEquals(19, leg4.departureHour)
        assertEquals(15, leg4.departureMinute)
        assertEquals(23, leg4.arrivalHour)
        assertEquals(45, leg4.arrivalMinute)
        assertEquals(0, leg4.layoverMinutesAfterArrival)

        val lastArrivalMinutes = 23 * 60 + 45
        val firstDepartureMinutes = 4 * 60 + 0
        val remainingNightMinutes = (24 * 60 - lastArrivalMinutes) + firstDepartureMinutes
        assertEquals(255, remainingNightMinutes)
        assertTrue(remainingNightMinutes < 300)
    }

    @Test
    fun calculateDailyPlan_akdp1RoundTrip_produces2Legs() {
        val plan = DepartureSlotCalculator.calculateDailyPlan(
            routeCategory = RouteCategory.AKDP,
            originTerminalId = "term_purabaya",
            destTerminalId = "term_arjosari",
            estimatedDurationMinutes = 90,
            departureHour = 7,
            departureMinute = 0,
            targetRitCount = 1
        )

        assertEquals(1, plan.targetRitCount)
        assertEquals(2, plan.totalLegsCount)
        assertEquals(0, plan.returnLeg1!!.layoverMinutesAfterArrival)
    }

    @Test
    fun calculateDailyPlan_akap_forces1RoundTripAndApplies5HourLayover() {
        val plan = DepartureSlotCalculator.calculateDailyPlan(
            routeCategory = RouteCategory.AKAP,
            originTerminalId = "term_purabaya",
            destTerminalId = "term_pulogebang",
            estimatedDurationMinutes = 600,
            departureHour = 18,
            departureMinute = 0,
            targetRitCount = 2
        )

        assertEquals(RouteCategory.AKAP, plan.routeCategory)
        assertEquals(1, plan.targetRitCount)
        assertEquals(2, plan.totalLegsCount)

        val leg1 = plan.outboundLeg1!!
        assertEquals(18, leg1.departureHour)
        assertEquals(0, leg1.departureMinute)
        assertEquals(4, leg1.arrivalHour)
        assertEquals(0, leg1.arrivalMinute)
        assertEquals(300, leg1.layoverMinutesAfterArrival)

        val leg2 = plan.returnLeg1!!
        assertEquals(9, leg2.departureHour)
        assertEquals(0, leg2.departureMinute)
    }

    @Test
    fun calculateDailyPlan_reverseDirection_setsDestToOriginForOutboundLegs() {
        val plan = DepartureSlotCalculator.calculateDailyPlan(
            routeCategory = RouteCategory.AKDP,
            originTerminalId = "term_purabaya",
            destTerminalId = "term_arjosari",
            estimatedDurationMinutes = 90,
            departureHour = 7,
            departureMinute = 0,
            targetRitCount = 2,
            initialDirection = RouteDirection.DESTINATION_TO_ORIGIN
        )

        assertEquals(4, plan.totalLegsCount)
        val leg1 = plan.legs[0]
        assertEquals(RouteDirection.DESTINATION_TO_ORIGIN, leg1.direction)
        assertEquals("term_arjosari", leg1.departureTerminalId)
        assertEquals("term_purabaya", leg1.arrivalTerminalId)

        val leg2 = plan.legs[1]
        assertEquals(RouteDirection.ORIGIN_TO_DESTINATION, leg2.direction)
        assertEquals("term_purabaya", leg2.departureTerminalId)
        assertEquals("term_arjosari", leg2.arrivalTerminalId)

        val leg3 = plan.legs[2]
        assertEquals(RouteDirection.DESTINATION_TO_ORIGIN, leg3.direction)
        assertEquals("term_arjosari", leg3.departureTerminalId)
        assertEquals("term_purabaya", leg3.arrivalTerminalId)

        val leg4 = plan.legs[3]
        assertEquals(RouteDirection.ORIGIN_TO_DESTINATION, leg4.direction)
        assertEquals("term_purabaya", leg4.departureTerminalId)
        assertEquals("term_arjosari", leg4.arrivalTerminalId)
    }

    @Test
    fun determineCategory_ensuresCurrentRoutesAreAkdp() {
        assertEquals(RouteCategory.AKDP, RouteCategory.determineCategory(90, 85.0))
        assertEquals(RouteCategory.AKDP, RouteCategory.determineCategory(150, 180.0))
        assertEquals(RouteCategory.AKDP, RouteCategory.determineCategory(300, 220.0))
        assertEquals(RouteCategory.AKDP, RouteCategory.determineCategory(200, 350.0))
    }

    @Test
    fun generate24HourSlots_allowsSameDepartureTimeWhenCorridorsAreDifferent() {
        val occupied = listOf(
            OccupiedSlotInfo(
                terminalId = "term_bwi",
                hour = 7,
                minute = 0,
                destinationCity = "Surabaya",
                routeId = "route_bwi_sby_situbondo",
                busCode = "TN-01",
                corridor = RoutePathType.NON_TOLL
            )
        )

        val slots = DepartureSlotCalculator.generate24HourSlots(
            terminalId = "term_bwi",
            targetDestinationCity = "Surabaya",
            existingOccupiedSlots = occupied,
            targetCorridor = RoutePathType.TOLL
        )

        val slot0700 = slots.find { it.hour == 7 && it.minute == 0 }
        assertNotNull(slot0700)
        assertTrue(slot0700!!.isAvailable)
    }

    @Test
    fun generate24HourSlots_blocksSameDepartureTimeWhenCorridorsAreIdentical() {
        val occupied = listOf(
            OccupiedSlotInfo(
                terminalId = "term_bwi",
                hour = 7,
                minute = 0,
                destinationCity = "Surabaya",
                routeId = "route_bwi_sby_situbondo",
                busCode = "TN-01",
                corridor = RoutePathType.NON_TOLL
            )
        )

        val slots = DepartureSlotCalculator.generate24HourSlots(
            terminalId = "term_bwi",
            targetDestinationCity = "Surabaya",
            existingOccupiedSlots = occupied,
            targetCorridor = RoutePathType.NON_TOLL
        )

        val slot0700 = slots.find { it.hour == 7 && it.minute == 0 }
        assertNotNull(slot0700)
        assertFalse(slot0700!!.isAvailable)
        assertEquals("route_bwi_sby_situbondo", slot0700.occupiedByRouteId)
    }
}
