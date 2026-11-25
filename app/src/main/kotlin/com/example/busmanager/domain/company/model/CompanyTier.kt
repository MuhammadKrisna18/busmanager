package com.example.busmanager.domain.company.model

enum class CompanyTier(
    val level: Int,
    val title: String,
    val minBuses: Int,
    val minRoutes: Int,
    val minBoardedPassengers: Int
) {
    PERINTIS(level = 1, title = "PO Perintis", minBuses = 0, minRoutes = 0, minBoardedPassengers = 0),
    REGIONAL(level = 2, title = "Operator Regional", minBuses = 2, minRoutes = 1, minBoardedPassengers = 50),
    BERKEMBANG(level = 3, title = "PO Kelas Utama", minBuses = 5, minRoutes = 2, minBoardedPassengers = 200),
    EKSEKUTIF(level = 4, title = "Operator Elit Jatim", minBuses = 8, minRoutes = 3, minBoardedPassengers = 500),
    PENGUASA(level = 5, title = "Raja Jalanan Nusantara", minBuses = 12, minRoutes = 5, minBoardedPassengers = 1000);

    companion object {
        fun calculateTier(busCount: Int, routeCount: Int, boardedCount: Int): CompanyTier {
            val tiersDescending = entries.sortedByDescending { it.level }
            return tiersDescending.firstOrNull {
                busCount >= it.minBuses && routeCount >= it.minRoutes && boardedCount >= it.minBoardedPassengers
            } ?: PERINTIS
        }
    }
}
