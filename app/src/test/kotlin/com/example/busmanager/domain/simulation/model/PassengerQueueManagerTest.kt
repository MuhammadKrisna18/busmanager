package com.example.busmanager.domain.simulation.model

import com.example.busmanager.data.local.datasource.TerminalDataSource
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.RoutePathType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PassengerQueueManagerTest {

    private lateinit var queueManager: PassengerQueueManager
    private lateinit var testRoute: BusRoute

    @Before
    fun setUp() {
        queueManager = PassengerQueueManager()
        val terminals = TerminalDataSource()
        val sby = terminals.getTerminalById("term_purabaya")!!
        val mlg = terminals.getTerminalById("term_arjosari")!!

        testRoute = BusRoute(
            id = "route_test_sby_mlg",
            name = "Surabaya - Malang",
            originTerminal = sby,
            destinationTerminal = mlg,
            pathType = RoutePathType.TOLL,
            segments = emptyList(),
            transitTerminals = emptyList()
        )
    }

    @Test
    fun boardPassengers_deductsFromQueue_andResetsHeadway() {
        val terminalId = testRoute.originTerminal.id
        val routeId = testRoute.id

        queueManager.setInitialQueue(terminalId, routeId, 40)
        val initialQueue = queueManager.getQueue(terminalId, routeId)
        assertEquals(40, initialQueue.waitingPassengers)

        val boarded = queueManager.boardPassengers(terminalId, routeId, availableSeats = 30)
        assertEquals(30, boarded)

        val updatedQueue = queueManager.getQueue(terminalId, routeId)
        assertEquals(10, updatedQueue.waitingPassengers)
        assertEquals(0, updatedQueue.minutesSinceLastService)
        assertEquals(30, updatedQueue.totalBoardedPassengers)
    }

    @Test
    fun headwayEffect_bunchingBus_getsFewPassengers() {
        val terminalId = testRoute.originTerminal.id
        val routeId = testRoute.id

        queueManager.setInitialQueue(terminalId, routeId, 40)
        val bus1Boarded = queueManager.boardPassengers(terminalId, routeId, availableSeats = 45)
        assertEquals(40, bus1Boarded)

        val queueAfterBus1 = queueManager.getQueue(terminalId, routeId)
        assertEquals(0, queueAfterBus1.waitingPassengers)

        val morningTime = GameCalendar(day = 1, month = 1, year = 2026, hour = 7, minute = 0)
        queueManager.advanceTime(minutes = 5, currentTime = morningTime, activeRoutes = listOf(testRoute))

        val bus2Boarded = queueManager.boardPassengers(terminalId, routeId, availableSeats = 45)
        assertTrue(bus2Boarded in 1..8)
        assertTrue(bus2Boarded < 15)
    }

    @Test
    fun lossRateEffect_whenHeadwayExceedsThreshold_passengersAbandonPeron() {
        val terminalId = testRoute.originTerminal.id
        val routeId = testRoute.id

        queueManager.setInitialQueue(terminalId, routeId, 50)
        val afternoonTime = GameCalendar(day = 1, month = 1, year = 2026, hour = 14, minute = 0)

        queueManager.advanceTime(minutes = 120, currentTime = afternoonTime, activeRoutes = listOf(testRoute))

        val queue = queueManager.getQueue(terminalId, routeId)
        assertTrue(queue.totalLostPassengers > 0)
        assertTrue(queue.minutesSinceLastService >= 120)
    }

    @Test
    fun peakHourMultiplier_generatesMorePassengersThanOffPeak() {
        val terminalId = testRoute.originTerminal.id
        val routeId = testRoute.id

        queueManager.setInitialQueue(terminalId, routeId, 0)
        val morningPeak = GameCalendar(day = 1, month = 1, year = 2026, hour = 7, minute = 30)
        queueManager.advanceTime(minutes = 60, currentTime = morningPeak, activeRoutes = listOf(testRoute))
        val morningPassengers = queueManager.getQueue(terminalId, routeId).waitingPassengers

        val offPeakManager = PassengerQueueManager()
        offPeakManager.setInitialQueue(terminalId, routeId, 0)
        val nightOffPeak = GameCalendar(day = 1, month = 1, year = 2026, hour = 23, minute = 30)
        offPeakManager.advanceTime(minutes = 60, currentTime = nightOffPeak, activeRoutes = listOf(testRoute))
        val nightPassengers = offPeakManager.getQueue(terminalId, routeId).waitingPassengers

        assertTrue(morningPassengers > nightPassengers)
    }

    @Test
    fun alightPassengers_fullAtDestination_partialAtTransit() {
        val fullAlight = queueManager.alightPassengers(currentPassengers = 35, isTerminalStop = true)
        assertEquals(35, fullAlight)

        val partialAlight = queueManager.alightPassengers(currentPassengers = 35, isTerminalStop = false)
        assertTrue(partialAlight in 1..34)
    }
}
