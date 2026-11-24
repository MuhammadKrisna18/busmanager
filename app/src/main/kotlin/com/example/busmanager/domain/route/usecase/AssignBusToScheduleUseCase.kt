package com.example.busmanager.domain.route.usecase

import com.example.busmanager.domain.route.repository.RouteScheduleRepository

class AssignBusToScheduleUseCase(
    private val scheduleRepository: RouteScheduleRepository
) {
    suspend operator fun invoke(scheduleId: String, busId: String): Boolean {
        return scheduleRepository.assignBusToSchedule(scheduleId, busId)
    }
}
