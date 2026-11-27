package com.example.busmanager.domain.route.model

import com.example.busmanager.domain.company.model.Money

const val STANDARD_TRANSIT_DWELL_MINUTES = 10

data class BusRoute(
    val id: String,
    val name: String,
    val originTerminal: Terminal,
    val destinationTerminal: Terminal,
    val pathType: RoutePathType,
    val segments: List<RouteSegment>,
    val transitTerminals: List<Terminal> = emptyList(),
    val category: RouteCategory = RouteCategory.AKDP
) {
    companion object {
        const val STANDARD_TRANSIT_DWELL_MINUTES = 10

        fun compressRouteDuration(rawMinutes: Int): Int {
            return when {
                rawMinutes <= 45 -> 30
                rawMinutes <= 65 -> 45
                rawMinutes <= 85 -> 60
                rawMinutes <= 105 -> 75
                rawMinutes <= 125 -> 90
                rawMinutes <= 145 -> 105
                rawMinutes <= 165 -> 120
                rawMinutes <= 185 -> 135
                rawMinutes <= 205 -> 150
                rawMinutes <= 225 -> 165
                rawMinutes <= 245 -> 180
                rawMinutes <= 265 -> 195
                rawMinutes <= 285 -> 210
                rawMinutes <= 305 -> 225
                rawMinutes <= 325 -> 240
                rawMinutes <= 345 -> 255
                else -> 270
            }
        }
    }
    val totalDistanceKm: Double get() = segments.sumOf { it.distanceKm }

    val totalTollFee: Money get() = segments.fold(Money.ZERO) { acc, segment -> acc + segment.tollFee }

    val rawDurationMinutes: Int get() = segments.sumOf { it.baseDurationMinutes }

    val totalEstimatedDurationMinutes: Int get() = compressRouteDuration(rawDurationMinutes)

    val totalTransitDwellMinutes: Int get() = transitTerminals.size * STANDARD_TRANSIT_DWELL_MINUTES

    val totalDurationWithDwellMinutes: Int get() = compressRouteDuration(rawDurationMinutes + totalTransitDwellMinutes)

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
