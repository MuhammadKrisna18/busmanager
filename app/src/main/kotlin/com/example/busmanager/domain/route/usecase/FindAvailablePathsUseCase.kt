package com.example.busmanager.domain.route.usecase

import com.example.busmanager.domain.route.model.Terminal
import com.example.busmanager.domain.route.repository.RouteRepository
import com.example.busmanager.domain.route.repository.TerminalRepository

class FindAvailablePathsUseCase(
    private val terminalRepository: TerminalRepository,
    private val routeRepository: RouteRepository
) {
    operator fun invoke(originTerminalId: String, destinationTerminalId: String): List<List<Terminal>> {
        if (originTerminalId == destinationTerminalId) return emptyList()
        val origin = terminalRepository.getTerminalById(originTerminalId) ?: return emptyList()
        val destination = terminalRepository.getTerminalById(destinationTerminalId) ?: return emptyList()

        val paths = mutableListOf<List<Terminal>>()
        val currentPath = mutableListOf(origin)
        val visited = mutableSetOf(originTerminalId)

        fun dfs(currentTerminalId: String) {
            if (currentTerminalId == destinationTerminalId) {
                paths.add(currentPath.toList())
                return
            }
            val nextIds = routeRepository.getConnectedTerminalIds(currentTerminalId)
            for (nextId in nextIds) {
                if (nextId !in visited) {
                    val nextTerminal = terminalRepository.getTerminalById(nextId) ?: continue
                    visited.add(nextId)
                    currentPath.add(nextTerminal)
                    dfs(nextId)
                    currentPath.removeAt(currentPath.lastIndex)
                    visited.remove(nextId)
                }
            }
        }

        dfs(originTerminalId)
        return paths.sortedBy { it.size }
    }
}
