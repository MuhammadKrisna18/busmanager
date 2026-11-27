package com.example.busmanager.domain.route.model

enum class RouteCategory {
    AKDP,
    AKAP;

    companion object {
        fun determineCategory(estimatedDurationMinutes: Int = 0, distanceKm: Double = 0.0): RouteCategory {
            return AKDP
        }

        fun fromRoute(route: BusRoute): RouteCategory = route.category
    }
}
