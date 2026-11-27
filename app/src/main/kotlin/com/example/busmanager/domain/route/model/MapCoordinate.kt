package com.example.busmanager.domain.route.model

data class MapCoordinate(
    val x: Float,
    val y: Float,
    val latitude: Double,
    val longitude: Double
) {
    init {
        require(x in 0.0f..1.0f) { "X coordinate must be normalized between 0.0 and 1.0" }
        require(y in 0.0f..1.0f) { "Y coordinate must be normalized between 0.0 and 1.0" }
    }

    companion object {
        const val MIN_LON = 105.0
        const val MAX_LON = 114.8
        const val MIN_LAT = -8.9
        const val MAX_LAT = -5.8

        fun fromGeo(latitude: Double, longitude: Double): MapCoordinate {
            val projectedX = ((longitude - MIN_LON) / (MAX_LON - MIN_LON)).toFloat().coerceIn(0.0f, 1.0f)
            val projectedY = ((MAX_LAT - latitude) / (MAX_LAT - MIN_LAT)).toFloat().coerceIn(0.0f, 1.0f)
            return MapCoordinate(
                x = projectedX,
                y = projectedY,
                latitude = latitude,
                longitude = longitude
            )
        }
    }
}
