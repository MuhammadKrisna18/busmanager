package com.example.busmanager.domain.route.model

import com.example.busmanager.domain.simulation.model.RouteDirection

object DepartureSlotCalculator {

    const val SLOT_INTERVAL_MINUTES = 15
    const val SLOTS_PER_HOUR = 4
    const val TOTAL_SLOTS_PER_DAY = 96
    const val MINUTES_PER_DAY = 1440

    const val AKDP_DESTINATION_LAYOVER_MINUTES = 30
    const val AKDP_ORIGIN_INTER_PP_LAYOVER_MINUTES = 45
    const val AKAP_DESTINATION_LAYOVER_MINUTES = 300

    fun generate24HourSlots(
        terminalId: String,
        targetDestinationCity: String,
        targetCorridor: RoutePathType? = null,
        existingOccupiedSlots: List<OccupiedSlotInfo> = emptyList()
    ): List<TerminalDepartureSlot> {
        val slots = mutableListOf<TerminalDepartureSlot>()
        for (hour in 0..23) {
            for (step in 0 until SLOTS_PER_HOUR) {
                val minute = step * SLOT_INTERVAL_MINUTES
                val slotId = String.format("%s_%02d%02d", terminalId, hour, minute)
                val matchingConflict = existingOccupiedSlots.find {
                    it.terminalId == terminalId &&
                            it.hour == hour &&
                            it.minute == minute &&
                            it.destinationCity.equals(targetDestinationCity, ignoreCase = true) &&
                            (targetCorridor == null || it.corridor == null || it.corridor == targetCorridor)
                }

                if (matchingConflict != null) {
                    slots.add(
                        TerminalDepartureSlot(
                            slotId = slotId,
                            terminalId = terminalId,
                            hour = hour,
                            minute = minute,
                            isAvailable = false,
                            occupiedByRouteId = matchingConflict.routeId,
                            occupiedDestinationCity = matchingConflict.destinationCity,
                            occupiedBusCode = matchingConflict.busCode
                        )
                    )
                } else {
                    slots.add(
                        TerminalDepartureSlot(
                            slotId = slotId,
                            terminalId = terminalId,
                            hour = hour,
                            minute = minute,
                            isAvailable = true
                        )
                    )
                }
            }
        }
        return slots
    }

    fun findNextAvailableSlot(
        slots: List<TerminalDepartureSlot>,
        fromHour: Int,
        fromMinute: Int
    ): TerminalDepartureSlot? {
        if (slots.isEmpty()) return null
        val targetMinutes = fromHour * 60 + fromMinute
        val afterOrEqual = slots.filter { it.isAvailable && it.totalMinutesOfDay >= targetMinutes }
        if (afterOrEqual.isNotEmpty()) {
            return afterOrEqual.minByOrNull { it.totalMinutesOfDay }
        }
        return slots.filter { it.isAvailable }.minByOrNull { it.totalMinutesOfDay }
    }

    fun alignToNext15MinuteSlot(totalMinutes: Int): Pair<Int, Int> {
        val remainder = totalMinutes % SLOT_INTERVAL_MINUTES
        val alignedTotal = if (remainder == 0) totalMinutes else totalMinutes + (SLOT_INTERVAL_MINUTES - remainder)
        val normalizedDayMinutes = ((alignedTotal % MINUTES_PER_DAY) + MINUTES_PER_DAY) % MINUTES_PER_DAY
        val hour = normalizedDayMinutes / 60
        val minute = normalizedDayMinutes % 60
        return Pair(hour, minute)
    }

    fun calculateDailyPlan(
        routeCategory: RouteCategory,
        originTerminalId: String,
        destTerminalId: String,
        estimatedDurationMinutes: Int,
        departureHour: Int,
        departureMinute: Int,
        targetRitCount: Int = 2,
        initialDirection: RouteDirection = RouteDirection.ORIGIN_TO_DESTINATION
    ): DailySchedulePlan {
        val safeDuration = estimatedDurationMinutes.coerceAtLeast(15)
        val effectiveRitCount = if (routeCategory == RouteCategory.AKAP) 1 else targetRitCount.coerceIn(1, 2)
        val destLayover = if (routeCategory == RouteCategory.AKAP) {
            AKAP_DESTINATION_LAYOVER_MINUTES
        } else {
            AKDP_DESTINATION_LAYOVER_MINUTES
        }

        val legs = mutableListOf<ScheduledTripLeg>()

        val leg1DeptTotal = departureHour * 60 + departureMinute
        val leg1ArrvTotal = leg1DeptTotal + safeDuration
        val (leg1ArrvH, leg1ArrvM) = alignMinutesToClock(leg1ArrvTotal)

        val leg1OriginTerm = if (initialDirection == RouteDirection.ORIGIN_TO_DESTINATION) originTerminalId else destTerminalId
        val leg1DestTerm = if (initialDirection == RouteDirection.ORIGIN_TO_DESTINATION) destTerminalId else originTerminalId
        val leg1Dir = initialDirection
        val leg2Dir = if (initialDirection == RouteDirection.ORIGIN_TO_DESTINATION) RouteDirection.DESTINATION_TO_ORIGIN else RouteDirection.ORIGIN_TO_DESTINATION

        legs.add(
            ScheduledTripLeg(
                legIndex = 0,
                ritNumber = 1,
                direction = leg1Dir,
                departureHour = departureHour,
                departureMinute = departureMinute,
                arrivalHour = leg1ArrvH,
                arrivalMinute = leg1ArrvM,
                departureTerminalId = leg1OriginTerm,
                arrivalTerminalId = leg1DestTerm,
                layoverMinutesAfterArrival = destLayover
            )
        )

        val leg2DeptTarget = leg1ArrvTotal + destLayover
        val (leg2DeptH, leg2DeptM) = alignToNext15MinuteSlot(leg2DeptTarget)
        val leg2ActualDeptTotal = leg2DeptH * 60 + leg2DeptM
        val leg2ArrvTotal = (if (leg2ActualDeptTotal < leg1ArrvTotal) leg2ActualDeptTotal + MINUTES_PER_DAY else leg2ActualDeptTotal) + safeDuration
        val (leg2ArrvH, leg2ArrvM) = alignMinutesToClock(leg2ArrvTotal)

        val originLayover = if (effectiveRitCount == 2) AKDP_ORIGIN_INTER_PP_LAYOVER_MINUTES else 0

        legs.add(
            ScheduledTripLeg(
                legIndex = 1,
                ritNumber = 1,
                direction = leg2Dir,
                departureHour = leg2DeptH,
                departureMinute = leg2DeptM,
                arrivalHour = leg2ArrvH,
                arrivalMinute = leg2ArrvM,
                departureTerminalId = leg1DestTerm,
                arrivalTerminalId = leg1OriginTerm,
                layoverMinutesAfterArrival = originLayover
            )
        )

        if (effectiveRitCount == 2) {
            val leg3DeptTarget = leg2ArrvTotal + originLayover
            val (leg3DeptH, leg3DeptM) = alignToNext15MinuteSlot(leg3DeptTarget)
            val leg3ActualDeptTotal = leg3DeptH * 60 + leg3DeptM
            val leg3ArrvTotal = (if (leg3ActualDeptTotal < leg2ArrvTotal) leg3ActualDeptTotal + MINUTES_PER_DAY else leg3ActualDeptTotal) + safeDuration
            val (leg3ArrvH, leg3ArrvM) = alignMinutesToClock(leg3ArrvTotal)

            legs.add(
                ScheduledTripLeg(
                    legIndex = 2,
                    ritNumber = 2,
                    direction = leg1Dir,
                    departureHour = leg3DeptH,
                    departureMinute = leg3DeptM,
                    arrivalHour = leg3ArrvH,
                    arrivalMinute = leg3ArrvM,
                    departureTerminalId = leg1OriginTerm,
                    arrivalTerminalId = leg1DestTerm,
                    layoverMinutesAfterArrival = destLayover
                )
            )

            val leg4DeptTarget = leg3ArrvTotal + destLayover
            val (leg4DeptH, leg4DeptM) = alignToNext15MinuteSlot(leg4DeptTarget)
            val leg4ActualDeptTotal = leg4DeptH * 60 + leg4DeptM
            val leg4ArrvTotal = (if (leg4ActualDeptTotal < leg3ArrvTotal) leg4ActualDeptTotal + MINUTES_PER_DAY else leg4ActualDeptTotal) + safeDuration
            val (leg4ArrvH, leg4ArrvM) = alignMinutesToClock(leg4ArrvTotal)

            legs.add(
                ScheduledTripLeg(
                    legIndex = 3,
                    ritNumber = 2,
                    direction = leg2Dir,
                    departureHour = leg4DeptH,
                    departureMinute = leg4DeptM,
                    arrivalHour = leg4ArrvH,
                    arrivalMinute = leg4ArrvM,
                    departureTerminalId = leg1DestTerm,
                    arrivalTerminalId = leg1OriginTerm,
                    layoverMinutesAfterArrival = 0
                )
            )
        }

        return DailySchedulePlan(
            routeCategory = routeCategory,
            targetRitCount = effectiveRitCount,
            legs = legs
        )
    }

    private fun alignMinutesToClock(totalMinutes: Int): Pair<Int, Int> {
        val normalized = ((totalMinutes % MINUTES_PER_DAY) + MINUTES_PER_DAY) % MINUTES_PER_DAY
        return Pair(normalized / 60, normalized % 60)
    }
}
