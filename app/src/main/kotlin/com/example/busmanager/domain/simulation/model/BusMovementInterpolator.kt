package com.example.busmanager.domain.simulation.model

import com.example.busmanager.domain.route.model.MapCoordinate
import kotlin.math.hypot

object BusMovementInterpolator {

    fun interpolatePosition(
        waypoints: List<MapCoordinate>,
        fraction: Float,
        direction: RouteDirection = RouteDirection.ORIGIN_TO_DESTINATION
    ): MapCoordinate {
        if (waypoints.isEmpty()) {
            return MapCoordinate(0.5f, 0.5f, -7.5, 112.5)
        }
        if (waypoints.size == 1) {
            return waypoints.first()
        }

        val orderedPoints = if (direction == RouteDirection.DESTINATION_TO_ORIGIN) {
            waypoints.reversed()
        } else {
            waypoints
        }

        val clampedFraction = fraction.coerceIn(0.0f, 1.0f)
        if (clampedFraction <= 0.0f) return orderedPoints.first()
        if (clampedFraction >= 1.0f) return orderedPoints.last()

        val segmentLengths = mutableListOf<Float>()
        var totalLength = 0.0f

        for (i in 0 until orderedPoints.size - 1) {
            val p1 = orderedPoints[i]
            val p2 = orderedPoints[i + 1]
            val length = hypot(p2.x - p1.x, p2.y - p1.y)
            segmentLengths.add(length)
            totalLength += length
        }

        if (totalLength <= 0.0001f) {
            return orderedPoints.first()
        }

        val targetDistance = totalLength * clampedFraction
        var accumulatedDistance = 0.0f

        for (i in 0 until segmentLengths.size) {
            val segmentLength = segmentLengths[i]
            val nextAccumulated = accumulatedDistance + segmentLength

            if (targetDistance <= nextAccumulated || i == segmentLengths.size - 1) {
                val t = if (segmentLength > 0.0001f) {
                    ((targetDistance - accumulatedDistance) / segmentLength).coerceIn(0.0f, 1.0f)
                } else {
                    0.0f
                }

                val start = orderedPoints[i]
                val end = orderedPoints[i + 1]

                val interpolatedX = (start.x + t * (end.x - start.x)).coerceIn(0.0f, 1.0f)
                val interpolatedY = (start.y + t * (end.y - start.y)).coerceIn(0.0f, 1.0f)
                val interpolatedLat = start.latitude + t * (end.latitude - start.latitude)
                val interpolatedLng = start.longitude + t * (end.longitude - start.longitude)

                return MapCoordinate(
                    x = interpolatedX,
                    y = interpolatedY,
                    latitude = interpolatedLat,
                    longitude = interpolatedLng
                )
            }

            accumulatedDistance = nextAccumulated
        }

        return orderedPoints.last()
    }
}
