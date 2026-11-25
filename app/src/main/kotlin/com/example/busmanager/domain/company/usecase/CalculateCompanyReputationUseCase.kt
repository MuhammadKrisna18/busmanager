package com.example.busmanager.domain.company.usecase

import com.example.busmanager.domain.company.model.CompanyReputation
import kotlin.math.roundToInt

class CalculateCompanyReputationUseCase {
    operator fun invoke(totalBoardedPassengers: Int, totalLostPassengers: Int): CompanyReputation {
        val total = totalBoardedPassengers + totalLostPassengers
        if (total <= 0) {
            return CompanyReputation(
                rating = 5.0f,
                statusTitle = "Sangat Dipercaya Penumpang",
                totalBoardedPassengers = 0,
                totalLostPassengers = 0
            )
        }

        val lossRatio = totalLostPassengers.toFloat() / total.toFloat()
        val calculated = (5.0f - (lossRatio * 4.0f)).coerceIn(1.0f, 5.0f)
        val roundedRating = (calculated * 10f).roundToInt() / 10f

        val status = when {
            roundedRating >= 4.7f -> "Sangat Dipercaya Penumpang"
            roundedRating >= 4.2f -> "Pelayanan Sangat Baik"
            roundedRating >= 3.5f -> "Pelayanan Cukup Baik"
            roundedRating >= 2.5f -> "Sering Mengalami Keterlambatan"
            else -> "Reputasi Buruk / Armada Minim"
        }

        return CompanyReputation(
            rating = roundedRating,
            statusTitle = status,
            totalBoardedPassengers = totalBoardedPassengers,
            totalLostPassengers = totalLostPassengers
        )
    }
}
