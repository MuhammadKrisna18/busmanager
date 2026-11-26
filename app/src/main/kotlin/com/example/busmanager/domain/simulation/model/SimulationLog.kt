package com.example.busmanager.domain.simulation.model

enum class SimulationLogCategory(val displayName: String, val icon: String) {
    DISPATCH("Pemberangkatan", "🚌"),
    ARRIVAL("Tiba di Tujuan", "🏁"),
    TRANSIT("Ampiran Singgah", "🚏"),
    FINANCIAL("Keuangan & Tiket", "💰"),
    MILESTONE("Pencapaian", "⭐")
}

data class SimulationLog(
    val id: String,
    val timestampMillis: Long = System.currentTimeMillis(),
    val dateFormatted: String,
    val timeFormatted: String,
    val category: SimulationLogCategory,
    val title: String,
    val message: String,
    val busId: String? = null,
    val busCode: String? = null,
    val routeId: String? = null,
    val routeName: String? = null
)
