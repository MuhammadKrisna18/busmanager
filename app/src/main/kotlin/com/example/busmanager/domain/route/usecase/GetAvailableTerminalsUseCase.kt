package com.example.busmanager.domain.route.usecase

import com.example.busmanager.domain.route.model.Terminal
import com.example.busmanager.domain.route.repository.TerminalRepository

class GetAvailableTerminalsUseCase(
    private val terminalRepository: TerminalRepository
) {
    operator fun invoke(): List<Terminal> {
        return terminalRepository.getAllTerminals()
    }
}
