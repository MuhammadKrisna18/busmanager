package com.example.busmanager.domain.route.usecase

import com.example.busmanager.domain.route.repository.RouteScheduleRepository

class DeleteRouteScheduleUseCase(
    private val scheduleRepository: RouteScheduleRepository
) {
    suspend operator fun invoke(scheduleId: String) {
        scheduleRepository.deleteSchedule(scheduleId)
    }
}
