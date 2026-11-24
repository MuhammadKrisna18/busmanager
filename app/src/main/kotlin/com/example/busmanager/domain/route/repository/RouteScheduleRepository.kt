package com.example.busmanager.domain.route.repository

import com.example.busmanager.domain.route.model.RouteSchedule
import com.example.busmanager.domain.simulation.model.RouteDirection
import kotlinx.coroutines.flow.Flow

interface RouteScheduleRepository {
    fun getSchedulesForRoute(routeId: String): Flow<List<RouteSchedule>>
    fun getAllSchedules(): Flow<List<RouteSchedule>>
    suspend fun getSchedulesListForRoute(routeId: String): List<RouteSchedule>
    suspend fun getScheduleById(id: String): RouteSchedule?
    suspend fun getSchedulesForBus(busId: String): List<RouteSchedule> { return emptyList() }
    fun getSchedulesForBusFlow(busId: String): Flow<List<RouteSchedule>> { return kotlinx.coroutines.flow.emptyFlow() }
    suspend fun addSchedule(schedule: RouteSchedule): Result<Unit>
    suspend fun updateSchedule(schedule: RouteSchedule): Result<Unit>
    suspend fun assignBusToSchedule(scheduleId: String, busId: String): Boolean
    suspend fun unassignBusFromSchedule(scheduleId: String)
    suspend fun deleteSchedule(scheduleId: String)
    suspend fun deleteSchedulesForBus(busId: String) {}
    suspend fun deleteSchedulesForRoute(routeId: String) {}
    suspend fun deleteAllSchedules() {}
    suspend fun checkConflict(
        routeId: String,
        direction: RouteDirection,
        hour: Int,
        minute: Int,
        excludeScheduleId: String? = null
    ): Boolean
}
