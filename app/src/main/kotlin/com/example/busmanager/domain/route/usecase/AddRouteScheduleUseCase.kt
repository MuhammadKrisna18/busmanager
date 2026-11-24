package com.example.busmanager.domain.route.usecase

import com.example.busmanager.domain.route.model.RouteSchedule
import com.example.busmanager.domain.route.repository.RouteScheduleRepository

class AddRouteScheduleUseCase(
    private val scheduleRepository: RouteScheduleRepository
) {
    suspend operator fun invoke(schedule: RouteSchedule): Result<Unit> {
        return scheduleRepository.addSchedule(schedule)
    }
}
