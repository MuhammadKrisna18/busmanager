package com.example.busmanager.domain.route.usecase

import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.RoutePathType
import com.example.busmanager.domain.route.repository.RouteRepository
import com.example.busmanager.domain.route.repository.RouteScheduleRepository
import com.example.busmanager.domain.simulation.model.RouteDirection
import kotlinx.coroutines.flow.first
import java.util.Locale
import kotlin.math.abs
import kotlin.math.min

sealed interface ScheduleConflictResult {
    data object Valid : ScheduleConflictResult
    data class Conflict(
        val conflictingRouteName: String,
        val originCity: String,
        val destinationCity: String,
        val corridor: RoutePathType,
        val departureTimeFormatted: String,
        val timeDifferenceMinutes: Int,
        val errorMessage: String
    ) : ScheduleConflictResult
}

class ValidateScheduleConflictUseCase(
    private val routeScheduleRepository: RouteScheduleRepository,
    private val routeRepository: RouteRepository
) {
    suspend operator fun invoke(
        route: BusRoute,
        direction: RouteDirection,
        departureHour: Int,
        departureMinute: Int,
        excludeScheduleId: String? = null,
        minHeadwayBufferMinutes: Int = 30
    ): ScheduleConflictResult {
        val targetOriginId = if (direction == RouteDirection.ORIGIN_TO_DESTINATION) {
            route.originTerminal.id
        } else {
            route.destinationTerminal.id
        }

        val targetDestId = if (direction == RouteDirection.ORIGIN_TO_DESTINATION) {
            route.destinationTerminal.id
        } else {
            route.originTerminal.id
        }

        val targetCorridor = route.pathType
        val targetTotalMinutes = departureHour * 60 + departureMinute

        val allSchedules = routeScheduleRepository.getAllSchedules().first()
        val allRoutes = routeRepository.getAllRoutes().associateBy { it.id }

        for (sched in allSchedules) {
            if (excludeScheduleId != null && sched.id == excludeScheduleId) {
                continue
            }

            val schedRoute = allRoutes[sched.routeId] ?: continue
            val schedOriginId = if (sched.direction == RouteDirection.ORIGIN_TO_DESTINATION) {
                schedRoute.originTerminal.id
            } else {
                schedRoute.destinationTerminal.id
            }

            val schedDestId = if (sched.direction == RouteDirection.ORIGIN_TO_DESTINATION) {
                schedRoute.destinationTerminal.id
            } else {
                schedRoute.originTerminal.id
            }

            val schedCorridor = schedRoute.pathType

            val isSameOrigin = schedOriginId == targetOriginId
            val isSameDest = schedDestId == targetDestId
            val isSameCorridor = schedCorridor == targetCorridor

            if (isSameOrigin && isSameDest && isSameCorridor) {
                val schedTotalMinutes = sched.departureHour * 60 + sched.departureMinute
                val directDiff = abs(targetTotalMinutes - schedTotalMinutes)
                val circularDiff = min(directDiff, 1440 - directDiff)

                if (circularDiff < minHeadwayBufferMinutes) {
                    val originCity = if (direction == RouteDirection.ORIGIN_TO_DESTINATION) {
                        route.originTerminal.city
                    } else {
                        route.destinationTerminal.city
                    }

                    val destCity = if (direction == RouteDirection.ORIGIN_TO_DESTINATION) {
                        route.destinationTerminal.city
                    } else {
                        route.originTerminal.city
                    }

                    val timeFormatted = String.format(Locale.US, "%02d:%02d WIB", sched.departureHour, sched.departureMinute)
                    val corridorName = if (targetCorridor == RoutePathType.TOLL) "Tol" else "Non-Tol"

                    val errorReason = if (circularDiff == 0) {
                        "Bentrok jadwal: Sudah ada keberangkatan dari $originCity ke $destCity ($corridorName) pada pukul $timeFormatted di trayek '${schedRoute.name}'."
                    } else {
                        "Jeda keberangkatan terlalu dekat: Sudah ada keberangkatan dari $originCity ke $destCity ($corridorName) pada pukul $timeFormatted di trayek '${schedRoute.name}' (selisih $circularDiff menit). Minimal jeda antar armada adalah $minHeadwayBufferMinutes menit."
                    }

                    return ScheduleConflictResult.Conflict(
                        conflictingRouteName = schedRoute.name,
                        originCity = originCity,
                        destinationCity = destCity,
                        corridor = targetCorridor,
                        departureTimeFormatted = timeFormatted,
                        timeDifferenceMinutes = circularDiff,
                        errorMessage = errorReason
                    )
                }
            }
        }

        return ScheduleConflictResult.Valid
    }
}
