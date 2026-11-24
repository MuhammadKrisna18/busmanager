package com.example.busmanager.domain.route

import com.example.busmanager.data.mapper.toDomain
import com.example.busmanager.data.mapper.toEntity
import com.example.busmanager.domain.route.model.RouteSchedule
import com.example.busmanager.domain.simulation.model.RouteDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteScheduleTest {

    @Test
    fun scheduleCreation_validParameters_succeeds() {
        val schedule = RouteSchedule(
            id = "sched-1",
            routeId = "route-jbr-sby",
            departureHour = 7,
            departureMinute = 0,
            direction = RouteDirection.ORIGIN_TO_DESTINATION,
            assignedBusId = "bus-1",
            isActive = true
        )

        assertEquals("sched-1", schedule.id)
        assertEquals(7, schedule.departureHour)
        assertEquals(0, schedule.departureMinute)
        assertEquals("07:00 WIB", schedule.formattedTime)
        assertTrue(schedule.hasAssignedBus)
        assertTrue(schedule.isActive)
    }

    @Test
    fun hasConflictWith_detectsDuplicateSlot() {
        val s1 = RouteSchedule(
            id = "sched-1",
            routeId = "route-jbr-sby",
            departureHour = 7,
            departureMinute = 0,
            direction = RouteDirection.ORIGIN_TO_DESTINATION
        )
        val s2 = RouteSchedule(
            id = "sched-2",
            routeId = "route-jbr-sby",
            departureHour = 7,
            departureMinute = 0,
            direction = RouteDirection.ORIGIN_TO_DESTINATION
        )
        val s3 = RouteSchedule(
            id = "sched-3",
            routeId = "route-jbr-sby",
            departureHour = 9,
            departureMinute = 30,
            direction = RouteDirection.ORIGIN_TO_DESTINATION
        )
        val s4 = RouteSchedule(
            id = "sched-4",
            routeId = "route-jbr-sby",
            departureHour = 7,
            departureMinute = 0,
            direction = RouteDirection.DESTINATION_TO_ORIGIN
        )

        assertTrue(s1.hasConflictWith(s2))
        assertFalse(s1.hasConflictWith(s3))
        assertFalse(s1.hasConflictWith(s4))
        assertFalse(s1.hasConflictWith(s1))
    }

    @Test(expected = IllegalArgumentException::class)
    fun schedule_invalidHour_throwsException() {
        RouteSchedule(
            id = "sched-invalid",
            routeId = "route-1",
            departureHour = 24,
            departureMinute = 0,
            direction = RouteDirection.ORIGIN_TO_DESTINATION
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun schedule_invalidMinute_throwsException() {
        RouteSchedule(
            id = "sched-invalid",
            routeId = "route-1",
            departureHour = 7,
            departureMinute = 60,
            direction = RouteDirection.ORIGIN_TO_DESTINATION
        )
    }

    @Test
    fun mapper_domainToEntityAndBack_preservesData() {
        val original = RouteSchedule(
            id = "sched-1",
            routeId = "route-jbr-sby",
            departureHour = 14,
            departureMinute = 30,
            direction = RouteDirection.DESTINATION_TO_ORIGIN,
            assignedBusId = "bus-2",
            isActive = true
        )

        val entity = original.toEntity()
        val mapped = entity.toDomain()

        assertEquals(original, mapped)
    }
}
