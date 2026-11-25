package com.example.busmanager.data.repository

import com.example.busmanager.data.local.dao.OwnedBusDao
import com.example.busmanager.data.local.dao.RouteScheduleDao
import com.example.busmanager.data.mapper.toDomain
import com.example.busmanager.data.mapper.toEntity
import com.example.busmanager.domain.route.model.RouteSchedule
import com.example.busmanager.domain.route.repository.RouteScheduleRepository
import com.example.busmanager.domain.simulation.model.RouteDirection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RouteScheduleRepositoryImpl(
    private val scheduleDao: RouteScheduleDao,
    private val busDao: OwnedBusDao
) : RouteScheduleRepository {

    override fun getSchedulesForRoute(routeId: String): Flow<List<RouteSchedule>> {
        return scheduleDao.getSchedulesByRouteFlow(routeId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAllSchedules(): Flow<List<RouteSchedule>> {
        return scheduleDao.getAllSchedulesFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getSchedulesListForRoute(routeId: String): List<RouteSchedule> {
        return scheduleDao.getSchedulesByRoute(routeId).map { it.toDomain() }
    }

    override suspend fun getScheduleById(id: String): RouteSchedule? {
        return scheduleDao.getScheduleById(id)?.toDomain()
    }

    override suspend fun getSchedulesForBus(busId: String): List<RouteSchedule> {
        return scheduleDao.getSchedulesByBusId(busId).map { it.toDomain() }
    }

    override fun getSchedulesForBusFlow(busId: String): Flow<List<RouteSchedule>> {
        return scheduleDao.getSchedulesByBusIdFlow(busId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addSchedule(schedule: RouteSchedule): Result<Unit> {
        val conflict = checkConflict(schedule.routeId, schedule.direction, schedule.departureHour, schedule.departureMinute)
        if (conflict) {
            return Result.failure(IllegalStateException("Jadwal pada jam ${schedule.formattedTime} sudah ada untuk trayek dan arah ini"))
        }
        scheduleDao.insertSchedule(schedule.toEntity())
        return Result.success(Unit)
    }

    override suspend fun updateSchedule(schedule: RouteSchedule): Result<Unit> {
        val conflict = checkConflict(schedule.routeId, schedule.direction, schedule.departureHour, schedule.departureMinute, schedule.id)
        if (conflict) {
            return Result.failure(IllegalStateException("Jadwal pada jam ${schedule.formattedTime} sudah ada untuk trayek dan arah ini"))
        }
        scheduleDao.updateSchedule(schedule.toEntity())
        return Result.success(Unit)
    }

    override suspend fun assignBusToSchedule(scheduleId: String, busId: String): Boolean {
        val scheduleEntity = scheduleDao.getScheduleById(scheduleId) ?: return false
        val busEntity = busDao.getBusById(busId) ?: return false

        val existingSchedulesForBus = scheduleDao.getSchedulesByBusId(busId)
        for (existing in existingSchedulesForBus) {
            val isDifferentRoute = existing.routeId != scheduleEntity.routeId
            val isSameRouteAndSameDirection = existing.routeId == scheduleEntity.routeId &&
                    existing.direction == scheduleEntity.direction &&
                    existing.id != scheduleId

            if (isDifferentRoute || isSameRouteAndSameDirection) {
                scheduleDao.updateSchedule(existing.copy(assignedBusId = null))
            }
        }

        scheduleDao.updateSchedule(scheduleEntity.copy(assignedBusId = busId))
        busDao.updateBus(busEntity.copy(routeId = scheduleEntity.routeId))
        return true
    }

    override suspend fun unassignBusFromSchedule(scheduleId: String) {
        val scheduleEntity = scheduleDao.getScheduleById(scheduleId) ?: return
        scheduleDao.updateSchedule(scheduleEntity.copy(assignedBusId = null))
    }

    override suspend fun deleteSchedule(scheduleId: String) {
        scheduleDao.deleteScheduleById(scheduleId)
    }

    override suspend fun deleteSchedulesForBus(busId: String) {
        scheduleDao.deleteSchedulesByBusId(busId)
    }

    override suspend fun deleteSchedulesForRoute(routeId: String) {
        scheduleDao.deleteSchedulesByRoute(routeId)
    }

    override suspend fun checkConflict(
        routeId: String,
        direction: RouteDirection,
        hour: Int,
        minute: Int,
        excludeScheduleId: String?
    ): Boolean {
        val schedules = scheduleDao.getSchedulesByRoute(routeId)
        val targetTotalMinutes = hour * 60 + minute
        for (sched in schedules) {
            if (sched.direction != direction.name) continue
            if (excludeScheduleId != null && sched.id == excludeScheduleId) continue
            val schedTotalMinutes = sched.departureHour * 60 + sched.departureMinute
            val diff = kotlin.math.abs(targetTotalMinutes - schedTotalMinutes)
            val circularDiff = kotlin.math.min(diff, 1440 - diff)
            if (circularDiff < 60) {
                return true
            }
        }
        return false
    }

    override suspend fun deleteAllSchedules() {
        scheduleDao.deleteAllSchedules()
    }
}
