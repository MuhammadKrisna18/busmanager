package com.example.busmanager.data.mapper

import com.example.busmanager.data.local.entity.RouteScheduleEntity
import com.example.busmanager.domain.route.model.RouteSchedule
import com.example.busmanager.domain.simulation.model.RouteDirection

fun RouteScheduleEntity.toDomain(): RouteSchedule = RouteSchedule(
    id = id,
    routeId = routeId,
    departureHour = departureHour,
    departureMinute = departureMinute,
    direction = RouteDirection.valueOf(direction),
    assignedBusId = assignedBusId,
    isActive = isActive,
    headwayMinutes = headwayMinutes,
    transitDwellMinutes = transitDwellMinutes,
    nextDepartureEpochMinutes = nextDepartureEpochMinutes
)

fun RouteSchedule.toEntity(): RouteScheduleEntity = RouteScheduleEntity(
    id = id,
    routeId = routeId,
    departureHour = departureHour,
    departureMinute = departureMinute,
    direction = direction.name,
    assignedBusId = assignedBusId,
    isActive = isActive,
    headwayMinutes = headwayMinutes,
    transitDwellMinutes = transitDwellMinutes,
    nextDepartureEpochMinutes = nextDepartureEpochMinutes
)
