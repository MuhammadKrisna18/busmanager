package com.example.busmanager.domain.route.usecase

import com.example.busmanager.data.local.datasource.RouteDataSource
import com.example.busmanager.data.local.datasource.TerminalDataSource
import com.example.busmanager.data.repository.RouteCatalogRepositoryImpl
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.RouteSchedule
import com.example.busmanager.domain.route.repository.RouteScheduleRepository
import com.example.busmanager.domain.simulation.model.RouteDirection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ValidateScheduleConflictUseCaseTest {

    private lateinit var terminalDataSource: TerminalDataSource
    private lateinit var routeDataSource: RouteDataSource
    private lateinit var routeRepository: RouteCatalogRepositoryImpl
    private lateinit var fakeScheduleRepository: FakeRouteScheduleRepository
    private lateinit var useCase: ValidateScheduleConflictUseCase

    private lateinit var routeSbyMlgToll: BusRoute
    private lateinit var routeSbyMlgNonToll: BusRoute
    private lateinit var routeSbyProboToll: BusRoute

    @Before
    fun setUp() {
        terminalDataSource = TerminalDataSource()
        routeDataSource = RouteDataSource(terminalDataSource)
        routeRepository = RouteCatalogRepositoryImpl(routeDataSource)
        fakeScheduleRepository = FakeRouteScheduleRepository()
        useCase = ValidateScheduleConflictUseCase(fakeScheduleRepository, routeRepository)

        val routes = routeRepository.getAllPredefinedRoutes()
        routeSbyMlgToll = routes.first { it.id == "route_sby_mlg_toll" }
        routeSbyMlgNonToll = routes.first { it.id == "route_sby_mlg_nontoll" }
        routeSbyProboToll = routes.first { it.id == "route_sby_probo_toll" }
    }

    @Test
    fun validate_noExistingSchedules_returnsValid() = runBlocking {
        val result = useCase(
            route = routeSbyMlgToll,
            direction = RouteDirection.ORIGIN_TO_DESTINATION,
            departureHour = 7,
            departureMinute = 0
        )
        assertTrue(result is ScheduleConflictResult.Valid)
    }

    @Test
    fun validate_differentHour_sameOriginDestCorridor_returnsValid() = runBlocking {
        fakeScheduleRepository.addSchedule(
            RouteSchedule(
                id = "sched-1",
                routeId = routeSbyMlgToll.id,
                departureHour = 7,
                departureMinute = 0,
                direction = RouteDirection.ORIGIN_TO_DESTINATION
            )
        )

        val result = useCase(
            route = routeSbyMlgToll,
            direction = RouteDirection.ORIGIN_TO_DESTINATION,
            departureHour = 8,
            departureMinute = 0
        )
        assertTrue(result is ScheduleConflictResult.Valid)
    }

    @Test
    fun validate_differentCorridor_sameOriginDestTime_returnsValid() = runBlocking {
        fakeScheduleRepository.addSchedule(
            RouteSchedule(
                id = "sched-toll",
                routeId = routeSbyMlgToll.id,
                departureHour = 7,
                departureMinute = 30,
                direction = RouteDirection.ORIGIN_TO_DESTINATION
            )
        )

        val result = useCase(
            route = routeSbyMlgNonToll,
            direction = RouteDirection.ORIGIN_TO_DESTINATION,
            departureHour = 7,
            departureMinute = 30
        )
        assertTrue(result is ScheduleConflictResult.Valid)
    }

    @Test
    fun validate_oppositeDirection_sameRouteAndTime_returnsValid() = runBlocking {
        fakeScheduleRepository.addSchedule(
            RouteSchedule(
                id = "sched-outbound",
                routeId = routeSbyMlgToll.id,
                departureHour = 9,
                departureMinute = 0,
                direction = RouteDirection.ORIGIN_TO_DESTINATION
            )
        )

        val result = useCase(
            route = routeSbyMlgToll,
            direction = RouteDirection.DESTINATION_TO_ORIGIN,
            departureHour = 9,
            departureMinute = 0
        )
        assertTrue(result is ScheduleConflictResult.Valid)
    }

    @Test
    fun validate_differentDestination_sameOriginTimeCorridor_returnsValid() = runBlocking {
        fakeScheduleRepository.addSchedule(
            RouteSchedule(
                id = "sched-malang",
                routeId = routeSbyMlgToll.id,
                departureHour = 10,
                departureMinute = 0,
                direction = RouteDirection.ORIGIN_TO_DESTINATION
            )
        )

        val result = useCase(
            route = routeSbyProboToll,
            direction = RouteDirection.ORIGIN_TO_DESTINATION,
            departureHour = 10,
            departureMinute = 0
        )
        assertTrue(result is ScheduleConflictResult.Valid)
    }

    @Test
    fun validate_exactSameOriginDestCorridorTime_returnsConflict() = runBlocking {
        fakeScheduleRepository.addSchedule(
            RouteSchedule(
                id = "sched-existing",
                routeId = routeSbyMlgToll.id,
                departureHour = 7,
                departureMinute = 0,
                direction = RouteDirection.ORIGIN_TO_DESTINATION
            )
        )

        val result = useCase(
            route = routeSbyMlgToll,
            direction = RouteDirection.ORIGIN_TO_DESTINATION,
            departureHour = 7,
            departureMinute = 0
        )
        assertTrue(result is ScheduleConflictResult.Conflict)
        val conflict = result as ScheduleConflictResult.Conflict
        assertEquals("Surabaya", conflict.originCity)
        assertEquals("Malang", conflict.destinationCity)
        assertEquals("07:00 WIB", conflict.departureTimeFormatted)
        assertTrue(conflict.errorMessage.contains("Bentrok jadwal"))
    }

    @Test
    fun validate_sameScheduleIdExcluded_returnsValid() = runBlocking {
        fakeScheduleRepository.addSchedule(
            RouteSchedule(
                id = "sched-self",
                routeId = routeSbyMlgToll.id,
                departureHour = 7,
                departureMinute = 0,
                direction = RouteDirection.ORIGIN_TO_DESTINATION
            )
        )

        val result = useCase(
            route = routeSbyMlgToll,
            direction = RouteDirection.ORIGIN_TO_DESTINATION,
            departureHour = 7,
            departureMinute = 0,
            excludeScheduleId = "sched-self"
        )
        assertTrue(result is ScheduleConflictResult.Valid)
    }

    @Test
    fun validate_under60MinutesDifference_returnsConflict() = runBlocking {
        fakeScheduleRepository.addSchedule(
            RouteSchedule(
                id = "sched-existing-7",
                routeId = routeSbyMlgToll.id,
                departureHour = 7,
                departureMinute = 0,
                direction = RouteDirection.ORIGIN_TO_DESTINATION
            )
        )

        val result = useCase(
            route = routeSbyMlgToll,
            direction = RouteDirection.ORIGIN_TO_DESTINATION,
            departureHour = 7,
            departureMinute = 30
        )
        assertTrue(result is ScheduleConflictResult.Conflict)
        val conflict = result as ScheduleConflictResult.Conflict
        assertEquals(30, conflict.timeDifferenceMinutes)
        assertTrue(conflict.errorMessage.contains("Jeda keberangkatan terlalu dekat"))
    }

    @Test
    fun validate_exactly60MinutesDifference_returnsValid() = runBlocking {
        fakeScheduleRepository.addSchedule(
            RouteSchedule(
                id = "sched-existing-7",
                routeId = routeSbyMlgToll.id,
                departureHour = 7,
                departureMinute = 0,
                direction = RouteDirection.ORIGIN_TO_DESTINATION
            )
        )

        val result = useCase(
            route = routeSbyMlgToll,
            direction = RouteDirection.ORIGIN_TO_DESTINATION,
            departureHour = 8,
            departureMinute = 0
        )
        assertTrue(result is ScheduleConflictResult.Valid)
    }

    @Test
    fun validate_multipleOf10MinutesDifferenceOver60_returnsValid() = runBlocking {
        fakeScheduleRepository.addSchedule(
            RouteSchedule(
                id = "sched-existing-7",
                routeId = routeSbyMlgToll.id,
                departureHour = 7,
                departureMinute = 0,
                direction = RouteDirection.ORIGIN_TO_DESTINATION
            )
        )

        val result = useCase(
            route = routeSbyMlgToll,
            direction = RouteDirection.ORIGIN_TO_DESTINATION,
            departureHour = 8,
            departureMinute = 10
        )
        assertTrue(result is ScheduleConflictResult.Valid)
    }

    @Test
    fun validate_midnightWrapAroundUnder60Minutes_returnsConflict() = runBlocking {
        fakeScheduleRepository.addSchedule(
            RouteSchedule(
                id = "sched-late-night",
                routeId = routeSbyMlgToll.id,
                departureHour = 23,
                departureMinute = 30,
                direction = RouteDirection.ORIGIN_TO_DESTINATION
            )
        )

        val result = useCase(
            route = routeSbyMlgToll,
            direction = RouteDirection.ORIGIN_TO_DESTINATION,
            departureHour = 0,
            departureMinute = 10
        )
        assertTrue(result is ScheduleConflictResult.Conflict)
        val conflict = result as ScheduleConflictResult.Conflict
        assertEquals(40, conflict.timeDifferenceMinutes)
    }

    @Test
    fun validate_midnightWrapAroundOver60Minutes_returnsValid() = runBlocking {
        fakeScheduleRepository.addSchedule(
            RouteSchedule(
                id = "sched-late-night",
                routeId = routeSbyMlgToll.id,
                departureHour = 23,
                departureMinute = 20,
                direction = RouteDirection.ORIGIN_TO_DESTINATION
            )
        )

        val result = useCase(
            route = routeSbyMlgToll,
            direction = RouteDirection.ORIGIN_TO_DESTINATION,
            departureHour = 0,
            departureMinute = 30
        )
        assertTrue(result is ScheduleConflictResult.Valid)
    }

    private class FakeRouteScheduleRepository : RouteScheduleRepository {
        private val list = mutableListOf<RouteSchedule>()
        private val flow = MutableStateFlow<List<RouteSchedule>>(emptyList())

        override fun getSchedulesForRoute(routeId: String): Flow<List<RouteSchedule>> = flow
        override fun getAllSchedules(): Flow<List<RouteSchedule>> = flow
        override suspend fun getSchedulesListForRoute(routeId: String): List<RouteSchedule> = list
        override suspend fun getScheduleById(id: String): RouteSchedule? = list.find { it.id == id }

        override suspend fun addSchedule(schedule: RouteSchedule): Result<Unit> {
            list.add(schedule)
            flow.value = list.toList()
            return Result.success(Unit)
        }

        override suspend fun updateSchedule(schedule: RouteSchedule): Result<Unit> {
            val idx = list.indexOfFirst { it.id == schedule.id }
            if (idx >= 0) {
                list[idx] = schedule
                flow.value = list.toList()
            }
            return Result.success(Unit)
        }

        override suspend fun assignBusToSchedule(scheduleId: String, busId: String): Boolean = true
        override suspend fun unassignBusFromSchedule(scheduleId: String) {}
        override suspend fun deleteSchedule(scheduleId: String) {
            list.removeAll { it.id == scheduleId }
            flow.value = list.toList()
        }

        override suspend fun checkConflict(
            routeId: String,
            direction: RouteDirection,
            hour: Int,
            minute: Int,
            excludeScheduleId: String?
        ): Boolean = false
        override suspend fun getSchedulesForBus(busId: String): List<com.example.busmanager.domain.route.model.RouteSchedule> = emptyList()
    override fun getSchedulesForBusFlow(busId: String): kotlinx.coroutines.flow.Flow<List<com.example.busmanager.domain.route.model.RouteSchedule>> = kotlinx.coroutines.flow.emptyFlow()
    override suspend fun deleteSchedulesForBus(busId: String) {}
    override suspend fun deleteSchedulesForRoute(routeId: String) {}
    override suspend fun deleteAllSchedules() {}
}
}
