package com.example.busmanager.data.mapper

import com.example.busmanager.data.local.entity.SimulationLogEntity
import com.example.busmanager.domain.simulation.model.SimulationLog
import com.example.busmanager.domain.simulation.model.SimulationLogCategory

fun SimulationLog.toEntity(): SimulationLogEntity {
    return SimulationLogEntity(
        id = id,
        timestampMillis = timestampMillis,
        dateFormatted = dateFormatted,
        timeFormatted = timeFormatted,
        category = category.name,
        title = title,
        message = message,
        busId = busId,
        busCode = busCode,
        routeId = routeId,
        routeName = routeName
    )
}

fun SimulationLogEntity.toDomain(): SimulationLog {
    val logCategory = try {
        SimulationLogCategory.valueOf(category)
    } catch (_: Exception) {
        SimulationLogCategory.DISPATCH
    }
    return SimulationLog(
        id = id,
        timestampMillis = timestampMillis,
        dateFormatted = dateFormatted,
        timeFormatted = timeFormatted,
        category = logCategory,
        title = title,
        message = message,
        busId = busId,
        busCode = busCode,
        routeId = routeId,
        routeName = routeName
    )
}
