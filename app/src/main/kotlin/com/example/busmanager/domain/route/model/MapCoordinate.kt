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
}
