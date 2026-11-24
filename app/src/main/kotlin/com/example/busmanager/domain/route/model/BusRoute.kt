package com.example.busmanager.domain.route.model

import com.example.busmanager.domain.company.model.Money

data class BusRoute(
    val id: String,
    val name: String,
    val originTerminal: Terminal,
    val destinationTerminal: Terminal,
    val pathType: RoutePathType,
    val segments: List<RouteSegment>,
    val transitTerminals: List<Terminal> = emptyList()
) {
    val totalDistanceKm: Double get() = segments.sumOf { it.distanceKm }

    val totalTollFee: Money get() = segments.fold(Money.ZERO) { acc, segment -> acc + segment.tollFee }

    val totalEstimatedDurationMinutes: Int get() = segments.sumOf { it.baseDurationMinutes }

    val allWaypoints: List<MapCoordinate>
        get() {
            val raw = segments.flatMap { it.waypoints }
            if (raw.isEmpty()) return emptyList()
            val result = mutableListOf(raw.first())
            for (i in 1 until raw.size) {
                val curr = raw[i]
                val prev = result.last()
                if (curr.x != prev.x || curr.y != prev.y) {
                    result.add(curr)
                }
            }
            return result
        }

    fun containsTerminal(terminalId: String): Boolean {
        return originTerminal.id == terminalId ||
                destinationTerminal.id == terminalId ||
                transitTerminals.any { it.id == terminalId }
    }

    fun toTransitStops(): List<TransitStop> {
        val stops = mutableListOf<TransitStop>()
        stops.add(TransitStop(terminal = originTerminal, stopOrder = 0, isOrigin = true))
        transitTerminals.forEachIndexed { index, terminal ->
            stops.add(TransitStop(terminal = terminal, stopOrder = index + 1))
        }
        stops.add(
            TransitStop(
                terminal = destinationTerminal,
                stopOrder = stops.size,
                isDestination = true
            )
        )
        return stops
    }
}
