package com.example.busmanager.domain.route.usecase

import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.RoutePathType
import com.example.busmanager.domain.route.model.RouteSegment
import com.example.busmanager.domain.route.model.Terminal
import com.example.busmanager.domain.route.repository.RouteRepository

class BuildCustomRouteUseCase(
    private val routeRepository: RouteRepository
) {
    operator fun invoke(
        terminals: List<Terminal>,
        preferredSegmentPathTypes: Map<Pair<String, String>, RoutePathType> = emptyMap(),
        activeTransitTerminalIds: Set<String> = emptySet(),
        customName: String? = null
    ): Result<BusRoute> {
        if (terminals.size < 2) {
            return Result.failure(IllegalArgumentException("Rute minimal harus memiliki 2 titik terminal (asal dan tujuan)"))
        }

        val segments = mutableListOf<RouteSegment>()
        for (i in 0 until terminals.size - 1) {
            val from = terminals[i]
            val to = terminals[i + 1]
            val availableSegments = routeRepository.getSegmentsBetween(from.id, to.id)
            if (availableSegments.isEmpty()) {
                return Result.failure(IllegalArgumentException("Tidak ada jalur jalan penghubung antara ${from.name} dan ${to.name}"))
            }
            val preferredType = preferredSegmentPathTypes[Pair(from.id, to.id)]
            val chosenSegment = if (preferredType != null) {
                availableSegments.find { it.pathType == preferredType } ?: availableSegments.first()
            } else {
                availableSegments.find { it.pathType == RoutePathType.TOLL } ?: availableSegments.first()
            }
            segments.add(chosenSegment)
        }

        val originTerminal = terminals.first()
        val destinationTerminal = terminals.last()
        val intermediateTerminals = terminals.subList(1, terminals.size - 1)
        val transitTerminals = intermediateTerminals.filter {
            activeTransitTerminalIds.isEmpty() || it.id in activeTransitTerminalIds
        }

        val routePathType = if (segments.any { it.pathType == RoutePathType.TOLL }) {
            RoutePathType.TOLL
        } else {
            RoutePathType.NON_TOLL
        }

        val generatedName = if (!customName.isNullOrBlank()) {
            customName
        } else {
            val viaLabel = if (transitTerminals.isNotEmpty()) {
                " (Via ${transitTerminals.joinToString(", ") { it.city }})"
            } else {
                ""
            }
            "${originTerminal.city} - ${destinationTerminal.city}$viaLabel"
        }

        val routeId = "route_custom_${originTerminal.id}_${destinationTerminal.id}_${System.currentTimeMillis()}"

        val customRoute = BusRoute(
            id = routeId,
            name = generatedName,
            originTerminal = originTerminal,
            destinationTerminal = destinationTerminal,
            pathType = routePathType,
            segments = segments,
            transitTerminals = transitTerminals
        )

        return Result.success(customRoute)
    }
}
