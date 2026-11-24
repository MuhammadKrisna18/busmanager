package com.example.busmanager.domain.route.usecase

import com.example.busmanager.domain.route.model.RouteSchedule
import com.example.busmanager.domain.route.repository.RouteScheduleRepository
import kotlinx.coroutines.flow.Flow

class GetRouteSchedulesUseCase(
    private val scheduleRepository: RouteScheduleRepository
) {
    operator fun invoke(routeId: String): Flow<List<RouteSchedule>> {
        return scheduleRepository.getSchedulesForRoute(routeId)
    }
}
