package com.example.busmanager.data.mapper

import com.example.busmanager.data.local.entity.CustomRouteEntity
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.RoutePathType
import com.example.busmanager.domain.route.model.RouteSegment
import com.example.busmanager.domain.route.repository.TerminalRepository

fun CustomRouteEntity.toDomain(
    terminalRepository: TerminalRepository,
    segmentResolver: (String) -> RouteSegment?
): BusRoute? {
    val origin = terminalRepository.getTerminalById(originTerminalId) ?: return null
    val destination = terminalRepository.getTerminalById(destinationTerminalId) ?: return null
    val segmentList = segmentIds.split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .mapNotNull { segmentResolver(it) }

    if (segmentList.isEmpty()) return null

    val transitList = if (transitTerminalIds.isNotBlank()) {
        transitTerminalIds.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull { terminalRepository.getTerminalById(it) }
    } else {
        emptyList()
    }

    val type = try {
        RoutePathType.valueOf(pathType)
    } catch (_: Exception) {
        if (segmentList.any { it.pathType == RoutePathType.TOLL }) RoutePathType.TOLL else RoutePathType.NON_TOLL
    }

    return BusRoute(
        id = id,
        name = name,
        originTerminal = origin,
        destinationTerminal = destination,
        pathType = type,
        segments = segmentList,
        transitTerminals = transitList
    )
}

fun BusRoute.toCustomRouteEntity(): CustomRouteEntity {
    return CustomRouteEntity(
        id = id,
        name = name,
        originTerminalId = originTerminal.id,
        destinationTerminalId = destinationTerminal.id,
        pathType = pathType.name,
        segmentIds = segments.joinToString(",") { it.id },
        transitTerminalIds = transitTerminals.joinToString(",") { it.id }
    )
}
