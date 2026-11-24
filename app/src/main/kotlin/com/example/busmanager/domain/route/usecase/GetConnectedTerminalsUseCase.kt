package com.example.busmanager.domain.route.usecase

import com.example.busmanager.domain.route.model.Terminal
import com.example.busmanager.domain.route.repository.RouteRepository
import com.example.busmanager.domain.route.repository.TerminalRepository

class GetConnectedTerminalsUseCase(
    private val terminalRepository: TerminalRepository,
    private val routeRepository: RouteRepository
) {
    operator fun invoke(terminalId: String): List<Terminal> {
        val connectedIds = routeRepository.getConnectedTerminalIds(terminalId)
        return connectedIds.mapNotNull { terminalRepository.getTerminalById(it) }
    }
}
