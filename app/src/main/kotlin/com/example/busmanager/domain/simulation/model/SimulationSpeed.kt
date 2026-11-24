package com.example.busmanager.domain.simulation.model

enum class SimulationSpeed(val multiplier: Int, val displayName: String) {
    NORMAL(1, "1x"),
    FAST(3, "3x"),
    VERY_FAST(5, "5x")
}
