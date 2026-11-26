package com.example.busmanager.domain.simulation.model

enum class SimulationLogCategory(val displayName: String, val icon: String) {
    DISPATCH("Pemberangkatan", "BRG"),
    ARRIVAL("Tiba di Tujuan", "TIBA"),
    TRANSIT("Ampiran Singgah", "SING"),
    FINANCIAL("Keuangan & Tiket", "KAS"),
    MILESTONE("Pencapaian", "ACH")
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
