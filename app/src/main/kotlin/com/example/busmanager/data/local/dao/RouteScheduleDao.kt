package com.example.busmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.busmanager.data.local.entity.RouteScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RouteScheduleDao {

    @Query("SELECT * FROM route_schedules WHERE routeId = :routeId ORDER BY departureHour ASC, departureMinute ASC")
    fun getSchedulesByRouteFlow(routeId: String): Flow<List<RouteScheduleEntity>>

    @Query("SELECT * FROM route_schedules WHERE routeId = :routeId ORDER BY departureHour ASC, departureMinute ASC")
    suspend fun getSchedulesByRoute(routeId: String): List<RouteScheduleEntity>

    @Query("SELECT * FROM route_schedules WHERE id = :id LIMIT 1")
    suspend fun getScheduleById(id: String): RouteScheduleEntity?

    @Query("SELECT * FROM route_schedules WHERE routeId = :routeId AND direction = :direction AND departureHour = :hour AND departureMinute = :minute LIMIT 1")
    suspend fun findConflictingSchedule(routeId: String, direction: String, hour: Int, minute: Int): RouteScheduleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: RouteScheduleEntity)

    @Update
    suspend fun updateSchedule(schedule: RouteScheduleEntity): Int

    @Query("DELETE FROM route_schedules WHERE id = :id")
    suspend fun deleteScheduleById(id: String): Int
}
