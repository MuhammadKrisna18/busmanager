package com.example.busmanager.domain.simulation.model

import com.example.busmanager.domain.route.model.BusRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.roundToInt
import kotlin.random.Random

class PassengerQueueManager(
    private val random: Random = Random.Default
) {
    private val _queues = MutableStateFlow<Map<String, TerminalPassengerQueue>>(emptyMap())
    val queues: StateFlow<Map<String, TerminalPassengerQueue>> = _queues.asStateFlow()

    fun queueKey(terminalId: String, routeId: String): String = "${terminalId}_${routeId}"

    fun getQueue(terminalId: String, routeId: String): TerminalPassengerQueue {
        val key = queueKey(terminalId, routeId)
        return _queues.value[key] ?: initQueue(terminalId, routeId)
    }

    fun setInitialQueue(terminalId: String, routeId: String, count: Int) {
        val key = queueKey(terminalId, routeId)
        val queue = TerminalPassengerQueue(
            terminalId = terminalId,
            routeId = routeId,
            waitingPassengers = count.coerceAtLeast(0),
            minutesSinceLastService = 0,
            totalBoardedPassengers = 0,
            totalLostPassengers = 0
        )
        _queues.update { it + (key to queue) }
    }

    private fun initQueue(terminalId: String, routeId: String): TerminalPassengerQueue {
        val key = queueKey(terminalId, routeId)
        val initialCount = 35
        val newQueue = TerminalPassengerQueue(
            terminalId = terminalId,
            routeId = routeId,
            waitingPassengers = initialCount,
            minutesSinceLastService = 0,
            totalBoardedPassengers = 0,
            totalLostPassengers = 0
        )
        _queues.update { it + (key to newQueue) }
        return newQueue
    }

    fun advanceTime(minutes: Int, currentTime: GameCalendar, activeRoutes: List<BusRoute>) {
        if (minutes <= 0) return

        val arrivalMultiplier = random.nextDouble(0.2, 2.2).toFloat()
        val baseArrivalsPerHour = 36f

        _queues.update { currentMap ->
            val updatedMap = currentMap.toMutableMap()

            activeRoutes.forEach { route ->
                val terminalsOnRoute = listOf(route.originTerminal) + route.transitTerminals + listOf(route.destinationTerminal)

                terminalsOnRoute.forEach { terminal ->
                    val key = queueKey(terminal.id, route.id)
                    val existing = updatedMap[key] ?: TerminalPassengerQueue(
                        terminalId = terminal.id,
                        routeId = route.id,
                        waitingPassengers = 30
                    )

                    val arrivalRatePerMin = (baseArrivalsPerHour * arrivalMultiplier * terminal.passengerDemandMultiplier) / 60f
                    val newArrivals = (arrivalRatePerMin * minutes).roundToInt()

                    val newElapsedSinceService = existing.minutesSinceLastService + minutes
                    var lost = 0
                    if (newElapsedSinceService > 90) {
                        val intervals = (minutes / 30).coerceAtLeast(1)
                        lost = (existing.waitingPassengers * 0.05f * intervals).toInt().coerceAtLeast(1)
                    }

                    val updatedWaiting = (existing.waitingPassengers + newArrivals - lost).coerceIn(0, 85)
                    val updatedTotalLost = existing.totalLostPassengers + lost

                    updatedMap[key] = existing.copy(
                        waitingPassengers = updatedWaiting,
                        minutesSinceLastService = newElapsedSinceService,
                        totalLostPassengers = updatedTotalLost
                    )
                }
            }
            updatedMap
        }
    }

    fun boardPassengers(terminalId: String, routeId: String, availableSeats: Int): Int {
        if (availableSeats <= 0) return 0
        val key = queueKey(terminalId, routeId)
        val currentQueue = getQueue(terminalId, routeId)

        val boarded = minOf(availableSeats, currentQueue.waitingPassengers).coerceAtLeast(0)
        val remainingWaiting = (currentQueue.waitingPassengers - boarded).coerceAtLeast(0)

        _queues.update { map ->
            map + (key to currentQueue.copy(
                waitingPassengers = remainingWaiting,
                minutesSinceLastService = 0,
                totalBoardedPassengers = currentQueue.totalBoardedPassengers + boarded
            ))
        }

        return boarded
    }

    fun alightPassengers(currentPassengers: Int, isTerminalStop: Boolean): Int {
        if (currentPassengers <= 0) return 0
        if (isTerminalStop) return currentPassengers
        val rate = random.nextDouble(0.20, 0.40).toFloat()
        return (currentPassengers * rate).toInt().coerceIn(1, currentPassengers)
    }

    fun reset() {
        _queues.value = emptyMap()
    }
}
