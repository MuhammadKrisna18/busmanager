package com.example.busmanager.data

import com.example.busmanager.data.local.dao.OwnedBusDao
import com.example.busmanager.data.local.dao.RouteScheduleDao
import com.example.busmanager.data.local.entity.OwnedBusEntity
import com.example.busmanager.data.local.entity.RouteScheduleEntity
import com.example.busmanager.data.repository.RouteScheduleRepositoryImpl
import com.example.busmanager.domain.route.model.RouteSchedule
import com.example.busmanager.domain.route.usecase.AddRouteScheduleUseCase
import com.example.busmanager.domain.route.usecase.AssignBusToScheduleUseCase
import com.example.busmanager.domain.route.usecase.DeleteRouteScheduleUseCase
import com.example.busmanager.domain.route.usecase.GetRouteSchedulesUseCase
import com.example.busmanager.domain.simulation.model.RouteDirection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RouteScheduleRepositoryTest {

    private lateinit var fakeScheduleDao: FakeRouteScheduleDao
    private lateinit var fakeBusDao: FakeOwnedBusDao
    private lateinit var scheduleRepository: RouteScheduleRepositoryImpl
    private lateinit var getRouteSchedulesUseCase: GetRouteSchedulesUseCase
    private lateinit var addRouteScheduleUseCase: AddRouteScheduleUseCase
    private lateinit var assignBusToScheduleUseCase: AssignBusToScheduleUseCase
    private lateinit var deleteRouteScheduleUseCase: DeleteRouteScheduleUseCase

    @Before
    fun setUp() {
        fakeScheduleDao = FakeRouteScheduleDao()
        fakeBusDao = FakeOwnedBusDao()
        scheduleRepository = RouteScheduleRepositoryImpl(fakeScheduleDao, fakeBusDao)
        getRouteSchedulesUseCase = GetRouteSchedulesUseCase(scheduleRepository)
        addRouteScheduleUseCase = AddRouteScheduleUseCase(scheduleRepository)
        assignBusToScheduleUseCase = AssignBusToScheduleUseCase(scheduleRepository)
        deleteRouteScheduleUseCase = DeleteRouteScheduleUseCase(scheduleRepository)
    }

    @Test
    fun addScheduleSucceedsWhenNoConflict() = runBlocking {
        val schedule = RouteSchedule(
            id = "sched-1",
            routeId = "route-sby-jbr",
            departureHour = 7,
            departureMinute = 0,
            direction = RouteDirection.ORIGIN_TO_DESTINATION
        )

        val result = addRouteScheduleUseCase(schedule)
        assertTrue(result.isSuccess)

        val schedules = getRouteSchedulesUseCase("route-sby-jbr").first()
        assertEquals(1, schedules.size)
        assertEquals("07:00 WIB", schedules.first().formattedTime)
    }

    @Test
    fun addScheduleFailsWhenScheduleWithSameSlotExists() = runBlocking {
        val schedule1 = RouteSchedule(
            id = "sched-1",
            routeId = "route-sby-jbr",
            departureHour = 7,
            departureMinute = 0,
            direction = RouteDirection.ORIGIN_TO_DESTINATION
        )
        val schedule2 = RouteSchedule(
            id = "sched-2",
            routeId = "route-sby-jbr",
            departureHour = 7,
            departureMinute = 0,
            direction = RouteDirection.ORIGIN_TO_DESTINATION
        )

        val result1 = addRouteScheduleUseCase(schedule1)
        assertTrue(result1.isSuccess)

        val result2 = addRouteScheduleUseCase(schedule2)
        assertTrue(result2.isFailure)
        assertEquals(1, getRouteSchedulesUseCase("route-sby-jbr").first().size)
    }

    @Test
    fun addScheduleAllowsSameHourForDifferentDirections() = runBlocking {
        val forward = RouteSchedule(
            id = "sched-fwd",
            routeId = "route-sby-jbr",
            departureHour = 7,
            departureMinute = 0,
            direction = RouteDirection.ORIGIN_TO_DESTINATION
        )
        val backward = RouteSchedule(
            id = "sched-bwd",
            routeId = "route-sby-jbr",
            departureHour = 7,
            departureMinute = 0,
            direction = RouteDirection.DESTINATION_TO_ORIGIN
        )

        val result1 = addRouteScheduleUseCase(forward)
        val result2 = addRouteScheduleUseCase(backward)

        assertTrue(result1.isSuccess)
        assertTrue(result2.isSuccess)
        assertEquals(2, getRouteSchedulesUseCase("route-sby-jbr").first().size)
    }

    @Test
    fun assignBusToScheduleUpdatesBothScheduleAndBusRoute() = runBlocking {
        val schedule = RouteSchedule(
            id = "sched-1",
            routeId = "route-sby-jbr",
            departureHour = 8,
            departureMinute = 30,
            direction = RouteDirection.ORIGIN_TO_DESTINATION
        )
        addRouteScheduleUseCase(schedule)

        fakeBusDao.insertBus(
            OwnedBusEntity(
                id = "bus-1",
                busCode = "TN-01",
                chassisId = "chassis-1",
                bodyId = "body-1",
                purchasedAtTimestamp = 1000L
            )
        )

        val success = assignBusToScheduleUseCase("sched-1", "bus-1")
        assertTrue(success)

        val updatedSchedule = scheduleRepository.getScheduleById("sched-1")
        assertNotNull(updatedSchedule)
        assertEquals("bus-1", updatedSchedule?.assignedBusId)

        val updatedBus = fakeBusDao.getBusById("bus-1")
        assertNotNull(updatedBus)
        assertEquals("route-sby-jbr", updatedBus?.routeId)
    }

    @Test
    fun assignBusToSchedule_supportsRoundTripOutboundAndReturnWithoutUnassigning() = runBlocking {
        fakeBusDao.insertBus(
            OwnedBusEntity(
                id = "bus-1",
                busCode = "MK-01",
                chassisId = "chassis-1",
                bodyId = "body-1",
                purchasedAtTimestamp = 1000L
            )
        )

        val outbound = RouteSchedule(
            id = "sched-outbound",
            routeId = "route-sby-mlg",
            departureHour = 8,
            departureMinute = 0,
            direction = RouteDirection.ORIGIN_TO_DESTINATION
        )
        val returnTrip = RouteSchedule(
            id = "sched-return",
            routeId = "route-sby-mlg",
            departureHour = 15,
            departureMinute = 0,
            direction = RouteDirection.DESTINATION_TO_ORIGIN
        )

        scheduleRepository.addSchedule(outbound)
        scheduleRepository.addSchedule(returnTrip)

        val assignOutboundSuccess = scheduleRepository.assignBusToSchedule("sched-outbound", "bus-1")
        assertTrue(assignOutboundSuccess)

        val assignReturnSuccess = scheduleRepository.assignBusToSchedule("sched-return", "bus-1")
        assertTrue(assignReturnSuccess)

        val busSchedules = scheduleRepository.getSchedulesForBus("bus-1")
        assertEquals(2, busSchedules.size)

        val assignedOutbound = scheduleRepository.getScheduleById("sched-outbound")
        val assignedReturn = scheduleRepository.getScheduleById("sched-return")
        assertEquals("bus-1", assignedOutbound?.assignedBusId)
        assertEquals("bus-1", assignedReturn?.assignedBusId)

        val differentRouteSchedule = RouteSchedule(
            id = "sched-other",
            routeId = "route-sby-jbr",
            departureHour = 10,
            departureMinute = 0,
            direction = RouteDirection.ORIGIN_TO_DESTINATION
        )
        scheduleRepository.addSchedule(differentRouteSchedule)
        scheduleRepository.assignBusToSchedule("sched-other", "bus-1")

        val reassignedBusSchedules = scheduleRepository.getSchedulesForBus("bus-1")
        assertEquals(1, reassignedBusSchedules.size)
        assertEquals("sched-other", reassignedBusSchedules.first().id)
        assertNull(scheduleRepository.getScheduleById("sched-outbound")?.assignedBusId)
        assertNull(scheduleRepository.getScheduleById("sched-return")?.assignedBusId)
    }

    @Test
    fun deleteScheduleRemovesScheduleSuccessfully() = runBlocking {
        val schedule = RouteSchedule(
            id = "sched-del",
            routeId = "route-sby-jbr",
            departureHour = 9,
            departureMinute = 0,
            direction = RouteDirection.ORIGIN_TO_DESTINATION
        )
        addRouteScheduleUseCase(schedule)
        assertEquals(1, getRouteSchedulesUseCase("route-sby-jbr").first().size)

        deleteRouteScheduleUseCase("sched-del")
        assertEquals(0, getRouteSchedulesUseCase("route-sby-jbr").first().size)
        assertNull(scheduleRepository.getScheduleById("sched-del"))
    }

    private class FakeRouteScheduleDao : RouteScheduleDao {
        private val scheduleMap = mutableMapOf<String, RouteScheduleEntity>()
        private val scheduleFlow = MutableStateFlow<List<RouteScheduleEntity>>(emptyList())

        override fun getAllSchedulesFlow(): Flow<List<RouteScheduleEntity>> = scheduleFlow

        override fun getSchedulesByRouteFlow(routeId: String): Flow<List<RouteScheduleEntity>> {
            val flow = MutableStateFlow<List<RouteScheduleEntity>>(emptyList())
            flow.value = scheduleMap.values.filter { it.routeId == routeId }
            return flow
        }

        override suspend fun getSchedulesByRoute(routeId: String): List<RouteScheduleEntity> {
            return scheduleMap.values.filter { it.routeId == routeId }
        }

        override suspend fun getScheduleById(id: String): RouteScheduleEntity? = scheduleMap[id]

        override suspend fun getScheduleByBusId(busId: String): RouteScheduleEntity? {
            return scheduleMap.values.find { it.assignedBusId == busId }
        }

        override suspend fun getSchedulesByBusId(busId: String): List<RouteScheduleEntity> {
            return scheduleMap.values.filter { it.assignedBusId == busId }
        }

        override fun getSchedulesByBusIdFlow(busId: String): Flow<List<RouteScheduleEntity>> {
            val flow = MutableStateFlow<List<RouteScheduleEntity>>(emptyList())
            flow.value = scheduleMap.values.filter { it.assignedBusId == busId }
            return flow
        }

        override suspend fun findConflictingSchedule(
            routeId: String,
            direction: String,
            hour: Int,
            minute: Int
        ): RouteScheduleEntity? {
            return scheduleMap.values.find {
                it.routeId == routeId &&
                        it.direction == direction &&
                        it.departureHour == hour &&
                        it.departureMinute == minute
            }
        }

        override suspend fun insertSchedule(schedule: RouteScheduleEntity) {
            scheduleMap[schedule.id] = schedule
            scheduleFlow.value = scheduleMap.values.toList()
        }

        override suspend fun updateSchedule(schedule: RouteScheduleEntity): Int {
            scheduleMap[schedule.id] = schedule
            scheduleFlow.value = scheduleMap.values.toList()
            return 1
        }

        override suspend fun deleteScheduleById(id: String): Int {
            val removed = scheduleMap.remove(id) != null
            scheduleFlow.value = scheduleMap.values.toList()
            return if (removed) 1 else 0
        }

        override suspend fun deleteSchedulesByRoute(routeId: String): Int {
            val keysToRemove = scheduleMap.filter { it.value.routeId == routeId }.keys
            keysToRemove.forEach { scheduleMap.remove(it) }
            scheduleFlow.value = scheduleMap.values.toList()
            return keysToRemove.size
        }

        override suspend fun deleteSchedulesByBusId(busId: String): Int {
            val keysToRemove = scheduleMap.filter { it.value.assignedBusId == busId }.keys
            keysToRemove.forEach { scheduleMap.remove(it) }
            scheduleFlow.value = scheduleMap.values.toList()
            return keysToRemove.size
        }

        override suspend fun unassignSchedulesByBusId(busId: String): Int {
            var updated = 0
            for ((key, schedule) in scheduleMap.entries) {
                if (schedule.assignedBusId == busId) {
                    scheduleMap[key] = schedule.copy(assignedBusId = null)
                    updated++
                }
            }
            scheduleFlow.value = scheduleMap.values.toList()
            return updated
        }

        override suspend fun deleteAllSchedules(): Int {
            val count = scheduleMap.size
            scheduleMap.clear()
            scheduleFlow.value = emptyList()
            return count
        }
    }

    private class FakeOwnedBusDao : OwnedBusDao {
        private val busMap = mutableMapOf<String, OwnedBusEntity>()
        private val busFlow = MutableStateFlow<List<OwnedBusEntity>>(emptyList())

        override fun getAllBusesFlow(): Flow<List<OwnedBusEntity>> = busFlow

        override suspend fun getBusById(busId: String): OwnedBusEntity? = busMap[busId]

        override suspend fun insertBus(bus: OwnedBusEntity) {
            busMap[bus.id] = bus
            busFlow.value = busMap.values.toList()
        }

        override suspend fun updateBus(bus: OwnedBusEntity) {
            busMap[bus.id] = bus
            busFlow.value = busMap.values.toList()
        }

        override suspend fun deleteBusById(busId: String): Int {
            val removed = busMap.remove(busId) != null
            busFlow.value = busMap.values.toList()
            return if (removed) 1 else 0
        }

        override suspend fun deleteAllBuses(): Int {
            val count = busMap.size
            busMap.clear()
            busFlow.value = emptyList()
            return count
        }
    }
}
