package com.example.busmanager.domain.company.model

import java.util.Locale

data class CompanyReputation(
    val rating: Float = 5.0f,
    val statusTitle: String = "Sangat Dipercaya Penumpang",
    val totalBoardedPassengers: Int = 0,
    val totalLostPassengers: Int = 0
) {
    val formattedStars: String
        get() = String.format(Locale.US, "%.1f", rating)
}
