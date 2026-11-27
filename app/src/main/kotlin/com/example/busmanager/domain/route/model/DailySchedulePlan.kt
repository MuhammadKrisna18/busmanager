package com.example.busmanager.domain.route.model

data class DailySchedulePlan(
    val routeCategory: RouteCategory,
    val targetRitCount: Int,
    val legs: List<ScheduledTripLeg>
) {
    val outboundLeg1: ScheduledTripLeg? get() = legs.getOrNull(0)
    val returnLeg1: ScheduledTripLeg? get() = legs.getOrNull(1)
    val outboundLeg2: ScheduledTripLeg? get() = legs.getOrNull(2)
    val returnLeg2: ScheduledTripLeg? get() = legs.getOrNull(3)
    val totalLegsCount: Int get() = legs.size
}
