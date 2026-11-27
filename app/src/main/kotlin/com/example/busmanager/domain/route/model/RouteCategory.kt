package com.example.busmanager.domain.route.model

enum class RouteCategory {
    AKDP,
    AKAP;

    companion object {
        private const val AKAP_MIN_DURATION_MINUTES = 270
        private const val AKAP_MIN_DISTANCE_KM = 200.0

        fun determineCategory(estimatedDurationMinutes: Int, distanceKm: Double): RouteCategory {
            return if (estimatedDurationMinutes >= AKAP_MIN_DURATION_MINUTES || distanceKm >= AKAP_MIN_DISTANCE_KM) {
                AKAP
            } else {
                AKDP
            }
        }

        fun fromRoute(route: BusRoute): RouteCategory =
            determineCategory(route.totalEstimatedDurationMinutes, route.totalDistanceKm)
    }
}
