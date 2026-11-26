package com.example.busmanager.domain.simulation.usecase

import com.example.busmanager.domain.simulation.model.SimulationLog
import com.example.busmanager.domain.simulation.model.SimulationLogCategory
import com.example.busmanager.domain.simulation.repository.SimulationLogRepository
import java.util.UUID

class RecordSimulationLogUseCase(
    private val repository: SimulationLogRepository
) {
    suspend operator fun invoke(log: SimulationLog) {
        repository.addLog(log)
    }

    suspend fun recordDispatch(
        busCode: String,
        routeName: String,
        destTerminalName: String,
        passengerCount: Int,
        timeText: String,
        dateText: String,
        busId: String,
        routeId: String
    ) {
        val log = SimulationLog(
            id = "log-disp-${UUID.randomUUID().toString().take(8)}",
            timestampMillis = System.currentTimeMillis(),
            dateFormatted = dateText,
            timeFormatted = timeText,
            category = SimulationLogCategory.DISPATCH,
            title = "Armada Berangkat: $busCode",
            message = "$busCode berangkat rute $routeName menuju $destTerminalName dengan $passengerCount penumpang.",
            busId = busId,
            busCode = busCode,
            routeId = routeId,
            routeName = routeName
        )
        repository.addLog(log)
    }

    suspend fun recordArrival(
        busCode: String,
        routeName: String,
        destTerminalName: String,
        passengerCount: Int,
        earningsAmount: Long,
        timeText: String,
        dateText: String,
        busId: String,
        routeId: String
    ) {
        val earningsFormatted = "Rp %,d".format(earningsAmount).replace(',', '.')
        val log = SimulationLog(
            id = "log-arr-${UUID.randomUUID().toString().take(8)}",
            timestampMillis = System.currentTimeMillis(),
            dateFormatted = dateText,
            timeFormatted = timeText,
            category = SimulationLogCategory.ARRIVAL,
            title = "Armada Tiba: $busCode",
            message = "$busCode telah tiba di $destTerminalName ($passengerCount penumpang alighted, pendapatan tiket: $earningsFormatted).",
            busId = busId,
            busCode = busCode,
            routeId = routeId,
            routeName = routeName
        )
        repository.addLog(log)
    }

    suspend fun recordTransit(
        busCode: String,
        routeName: String,
        transitTerminalName: String,
        boardedCount: Int,
        alightedCount: Int,
        timeText: String,
        dateText: String,
        busId: String,
        routeId: String
    ) {
        val log = SimulationLog(
            id = "log-trans-${UUID.randomUUID().toString().take(8)}",
            timestampMillis = System.currentTimeMillis(),
            dateFormatted = dateText,
            timeFormatted = timeText,
            category = SimulationLogCategory.TRANSIT,
            title = "Ampiran Singgah: $busCode",
            message = "$busCode singgah di $transitTerminalName (naik: $boardedCount, turun: $alightedCount pnp).",
            busId = busId,
            busCode = busCode,
            routeId = routeId,
            routeName = routeName
        )
        repository.addLog(log)
    }
}
