package com.example.busmanager.presentation.garage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.busmanager.domain.company.repository.CompanyRepository
import com.example.busmanager.domain.driver.model.Driver
import com.example.busmanager.domain.driver.repository.DriverRepository
import com.example.busmanager.domain.fleet.repository.FleetRepository
import com.example.busmanager.domain.fleet.usecase.ReconcileBusOperatingStateUseCase
import com.example.busmanager.domain.fleet.usecase.SellBusResult
import com.example.busmanager.domain.fleet.usecase.SellBusUseCase
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.DailySchedulePlan
import com.example.busmanager.domain.route.model.DepartureSlotCalculator
import com.example.busmanager.domain.route.model.OccupiedSlotInfo
import com.example.busmanager.domain.route.model.RouteCategory
import com.example.busmanager.domain.route.model.TerminalDepartureSlot
import com.example.busmanager.domain.route.repository.RoutePricingRepository
import com.example.busmanager.domain.route.repository.RouteRepository
import com.example.busmanager.domain.route.repository.RouteScheduleRepository
import com.example.busmanager.domain.route.usecase.AssignBusToActiveRouteUseCase
import com.example.busmanager.domain.route.usecase.GetRouteOperationalStatusUseCase
import com.example.busmanager.domain.route.usecase.ScheduleConflictResult
import com.example.busmanager.domain.route.usecase.ValidateScheduleConflictUseCase
import com.example.busmanager.domain.simulation.model.ActiveBusTrip
import com.example.busmanager.domain.simulation.model.RouteDirection
import com.example.busmanager.domain.simulation.usecase.GetActiveTripsUseCase
import com.example.busmanager.presentation.common.FeedbackToastData
import com.example.busmanager.presentation.route.wizard.SlotTimeFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GarageViewModel(
    private val fleetRepository: FleetRepository,
    private val driverRepository: DriverRepository,
    private val routeRepository: RouteRepository,
    private val routePricingRepository: RoutePricingRepository,
    private val companyRepository: CompanyRepository,
    private val getActiveTripsUseCase: GetActiveTripsUseCase? = null,
    private val sellBusUseCase: SellBusUseCase? = null,
    private val reconcileBusOperatingStateUseCase: ReconcileBusOperatingStateUseCase? = null,
    private val routeScheduleRepository: RouteScheduleRepository? = null,
    private val assignBusToActiveRouteUseCase: AssignBusToActiveRouteUseCase? = null,
    private val validateScheduleConflictUseCase: ValidateScheduleConflictUseCase? = null,
    private val getRouteOperationalStatusUseCase: GetRouteOperationalStatusUseCase? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(GarageUiState(isLoading = true))
    val uiState: StateFlow<GarageUiState> = _uiState.asStateFlow()

    init {
        observeGarageData()
    }

    private fun observeGarageData() {
        viewModelScope.launch {
            reconcileBusOperatingStateUseCase?.invoke()
            val activeTripsFlow: Flow<List<ActiveBusTrip>> = getActiveTripsUseCase?.invoke() ?: MutableStateFlow(emptyList())
            val schedulesFlow = routeScheduleRepository?.getAllSchedules() ?: MutableStateFlow(emptyList())
            combine(
                fleetRepository.getOwnedBuses(),
                driverRepository.getAllDrivers(),
                companyRepository.getCompany(),
                activeTripsFlow,
                schedulesFlow
            ) { buses, drivers, company, activeTrips, schedules ->
                val busItems = buses.map { bus ->
                    val activeTrip = activeTrips.find { it.busId == bus.id }
                    val isOperating = activeTrip != null || bus.isOperating
                    val driver = bus.driverId?.let { dId -> drivers.find { it.id == dId } }
                    val route = activeTrip?.route ?: bus.routeId?.let { rId -> routeRepository.getRouteById(rId) }
                    val pricings = (activeTrip?.route?.id ?: bus.routeId)?.let { rId ->
                        routePricingRepository.getPricingsListForRoute(rId)
                    } ?: emptyList()
                    val schedule = schedules.find { it.assignedBusId == bus.id }
                    val hasSchedules = schedules.any { it.assignedBusId == bus.id }

                    GarageBusItem(
                        bus = bus,
                        driverName = driver?.name,
                        routeName = route?.name,
                        hasPricings = pricings.isNotEmpty(),
                        isOperating = isOperating,
                        activeTripDirection = activeTrip?.let { "Menuju ${it.currentDestinationName} dari ${it.currentOriginName}" },
                        activeTripProgressPercent = activeTrip?.let { (it.progressFraction * 100).toInt() },
                        headwayMinutes = schedule?.headwayMinutes ?: 60,
                        transitDwellMinutes = schedule?.transitDwellMinutes ?: 10,
                        hasSchedules = hasSchedules
                    )
                }

                val operatingCount = busItems.count { it.isOnDuty }
                val standbyCount = busItems.count { !it.isOnDuty }
                val currentFilter = _uiState.value.selectedFilter

                _uiState.update { state ->
                    state.copy(
                        allBuses = busItems,
                        filteredBuses = filterBuses(busItems, currentFilter),
                        selectedFilter = currentFilter,
                        totalBusCount = busItems.size,
                        operatingBusCount = operatingCount,
                        standbyBusCount = standbyCount,
                        companyName = company?.name ?: "PO TransNusantara",
                        isLoading = false
                    )
                }
            }.collect {}
        }
    }

    fun setFilter(filter: GarageFilter) {
        _uiState.update { state ->
            state.copy(
                selectedFilter = filter,
                filteredBuses = filterBuses(state.allBuses, filter)
            )
        }
    }

    private fun filterBuses(buses: List<GarageBusItem>, filter: GarageFilter): List<GarageBusItem> {
        return when (filter) {
            GarageFilter.ALL -> buses
            GarageFilter.OPERATING -> buses.filter { it.isOnDuty }
            GarageFilter.STANDBY -> buses.filter { !it.isOnDuty }
        }
    }

    fun sellBus(busId: String, onResult: (SellBusResult) -> Unit = {}) {
        viewModelScope.launch {
            val targetBus = _uiState.value.allBuses.find { it.bus.id == busId }?.bus
            val result = sellBusUseCase?.invoke(busId) ?: SellBusResult.BusNotFound
            if (result is SellBusResult.Success) {
                _uiState.update {
                    it.copy(
                        feedbackToast = FeedbackToastData(
                            iconEmoji = "💰",
                            title = "Armada Berhasil Dijual!",
                            subtitle = "${targetBus?.busCode ?: "Armada"} terjual seharga ${result.refundAmount.formatRupiah()}",
                            durationMillis = 1000L
                        )
                    )
                }
            }
            onResult(result)
        }
    }

    fun openEditSchedule(item: GarageBusItem) {
        val routeName = item.routeName ?: "Trayek Operasional"
        _uiState.update {
            it.copy(
                editingScheduleTarget = GarageScheduleTarget(
                    bus = item.bus,
                    routeName = routeName,
                    currentHeadwayMinutes = item.headwayMinutes
                )
            )
        }
    }

    fun dismissEditSchedule() {
        _uiState.update { it.copy(editingScheduleTarget = null) }
    }

    fun saveSchedule(headwayMinutes: Int) {
        val target = _uiState.value.editingScheduleTarget ?: return
        viewModelScope.launch {
            val busId = target.bus.id
            val schedules = routeScheduleRepository?.getSchedulesForBus(busId) ?: emptyList()
            if (schedules.isNotEmpty()) {
                schedules.forEach { sched ->
                    routeScheduleRepository?.updateSchedule(
                        sched.copy(
                            headwayMinutes = headwayMinutes,
                            transitDwellMinutes = BusRoute.STANDARD_TRANSIT_DWELL_MINUTES
                        )
                    )
                }
            } else if (target.bus.routeId != null) {
                val routeSchedules = routeScheduleRepository?.getSchedulesListForRoute(target.bus.routeId) ?: emptyList()
                if (routeSchedules.isNotEmpty()) {
                    routeSchedules.forEach { sched ->
                        routeScheduleRepository?.updateSchedule(
                            sched.copy(
                                headwayMinutes = headwayMinutes,
                                transitDwellMinutes = BusRoute.STANDARD_TRANSIT_DWELL_MINUTES
                            )
                        )
                    }
                }
            }
            _uiState.update { it.copy(editingScheduleTarget = null) }
        }
    }

    private data class SlotsAndPlanResult(
        val category: RouteCategory,
        val slots: List<TerminalDepartureSlot>,
        val selectedSlot: TerminalDepartureSlot?,
        val targetRitCount: Int,
        val plan: DailySchedulePlan?
    )

    private suspend fun computeSlotsAndPlan(
        route: BusRoute?,
        direction: RouteDirection,
        preferredHour: Int,
        preferredMinute: Int,
        targetRitCount: Int
    ): SlotsAndPlanResult {
        if (route == null) {
            return SlotsAndPlanResult(
                category = RouteCategory.AKDP,
                slots = emptyList(),
                selectedSlot = null,
                targetRitCount = 2,
                plan = null
            )
        }

        val category = RouteCategory.fromRoute(route)
        val (depTerminal, destTerminal) = if (direction == RouteDirection.ORIGIN_TO_DESTINATION) {
            route.originTerminal to route.destinationTerminal
        } else {
            route.destinationTerminal to route.originTerminal
        }

        val allSchedules = routeScheduleRepository?.getAllSchedules()?.firstOrNull() ?: emptyList()
        val allRoutes = routeRepository.getAllRoutes().associateBy { it.id }
        val allBuses = fleetRepository.getOwnedBuses().firstOrNull()?.associateBy { it.id } ?: emptyMap()

        val occupiedSlots = allSchedules.mapNotNull { schedule ->
            val r = allRoutes[schedule.routeId] ?: return@mapNotNull null
            val scheduleOriginTerminalId = if (schedule.direction == RouteDirection.ORIGIN_TO_DESTINATION) {
                r.originTerminal.id
            } else {
                r.destinationTerminal.id
            }
            val scheduleDestCity = if (schedule.direction == RouteDirection.ORIGIN_TO_DESTINATION) {
                r.destinationTerminal.city
            } else {
                r.originTerminal.city
            }
            val assignedBusCode = schedule.assignedBusId?.let { busId -> allBuses[busId]?.busCode } ?: schedule.assignedBusId
            OccupiedSlotInfo(
                terminalId = scheduleOriginTerminalId,
                hour = schedule.departureHour,
                minute = schedule.departureMinute,
                destinationCity = scheduleDestCity,
                routeId = schedule.routeId,
                busCode = assignedBusCode,
                corridor = r.pathType
            )
        }

        val slots = DepartureSlotCalculator.generate24HourSlots(
            terminalId = depTerminal.id,
            targetDestinationCity = destTerminal.city,
            targetCorridor = route.pathType,
            existingOccupiedSlots = occupiedSlots
        )

        val chosenSlot = DepartureSlotCalculator.findNextAvailableSlot(slots, preferredHour, preferredMinute)
            ?: slots.firstOrNull { it.isAvailable }

        val effectiveRitCount = if (category == RouteCategory.AKAP) 1 else 2
        val plan = if (chosenSlot != null) {
            DepartureSlotCalculator.calculateDailyPlan(
                routeCategory = category,
                originTerminalId = route.originTerminal.id,
                destTerminalId = route.destinationTerminal.id,
                estimatedDurationMinutes = route.totalDurationWithDwellMinutes,
                departureHour = chosenSlot.hour,
                departureMinute = chosenSlot.minute,
                targetRitCount = effectiveRitCount,
                initialDirection = direction
            )
        } else null

        return SlotsAndPlanResult(
            category = category,
            slots = slots,
            selectedSlot = chosenSlot,
            targetRitCount = effectiveRitCount,
            plan = plan
        )
    }

    fun openQuickAssignDialog(item: GarageBusItem) {
        viewModelScope.launch {
            val allDrivers = driverRepository.getHiredDrivers().firstOrNull() ?: driverRepository.getAllDrivers().first()
            val allBuses = fleetRepository.getOwnedBuses().first()
            val otherAssignedDriverIds = allBuses.filter { it.id != item.bus.id }.mapNotNull { it.driverId }.toSet()
            val currentBusDriver = item.bus.driverId?.let { dId -> allDrivers.find { it.id == dId } }
            val availableDrivers = allDrivers.filter { it.isHired && (!otherAssignedDriverIds.contains(it.id) || it.id == item.bus.driverId) }
            val selectedDriver = currentBusDriver ?: availableDrivers.firstOrNull()

            val operationalRoutes = getRouteOperationalStatusUseCase?.invoke()?.firstOrNull() ?: emptyList()
            val availableRoutes = operationalRoutes.map { it.route }
            val selectedRoute = item.bus.routeId?.let { rId -> availableRoutes.find { it.id == rId } }
                ?: availableRoutes.firstOrNull()

            val initialDirection = RouteDirection.ORIGIN_TO_DESTINATION
            val initialSlotsAndPlan = computeSlotsAndPlan(
                route = selectedRoute,
                direction = initialDirection,
                preferredHour = 7,
                preferredMinute = 0,
                targetRitCount = 2
            )

            val initialAssignState = QuickAssignState(
                bus = item.bus,
                availableRoutes = availableRoutes,
                selectedRoute = selectedRoute,
                availableDrivers = availableDrivers,
                selectedDriver = selectedDriver,
                routeCategory = initialSlotsAndPlan.category,
                availableSlots = initialSlotsAndPlan.slots,
                selectedSlot = initialSlotsAndPlan.selectedSlot,
                selectedTimeFilter = SlotTimeFilter.MORNING,
                targetRitCount = initialSlotsAndPlan.targetRitCount,
                dailySchedulePlan = initialSlotsAndPlan.plan,
                departureHour = initialSlotsAndPlan.selectedSlot?.hour ?: 7,
                departureMinute = initialSlotsAndPlan.selectedSlot?.minute ?: 0,
                direction = initialDirection,
                transitDwellMinutes = item.transitDwellMinutes
            )
            _uiState.update { it.copy(quickAssignState = initialAssignState) }
        }
    }

    fun selectQuickAssignRoute(route: BusRoute) {
        val current = _uiState.value.quickAssignState ?: return
        viewModelScope.launch {
            val res = computeSlotsAndPlan(
                route = route,
                direction = current.direction,
                preferredHour = current.departureHour,
                preferredMinute = current.departureMinute,
                targetRitCount = current.targetRitCount
            )
            _uiState.update { state ->
                state.copy(
                    quickAssignState = state.quickAssignState?.copy(
                        selectedRoute = route,
                        routeCategory = res.category,
                        availableSlots = res.slots,
                        selectedSlot = res.selectedSlot,
                        targetRitCount = res.targetRitCount,
                        dailySchedulePlan = res.plan,
                        departureHour = res.selectedSlot?.hour ?: current.departureHour,
                        departureMinute = res.selectedSlot?.minute ?: current.departureMinute,
                        conflictError = null
                    )
                )
            }
        }
    }

    fun selectQuickAssignDriver(driver: Driver) {
        _uiState.update { state ->
            state.copy(quickAssignState = state.quickAssignState?.copy(selectedDriver = driver))
        }
    }

    fun setQuickAssignDirection(direction: RouteDirection) {
        val current = _uiState.value.quickAssignState ?: return
        val route = current.selectedRoute ?: return
        viewModelScope.launch {
            val res = computeSlotsAndPlan(
                route = route,
                direction = direction,
                preferredHour = current.departureHour,
                preferredMinute = current.departureMinute,
                targetRitCount = current.targetRitCount
            )
            _uiState.update { state ->
                state.copy(
                    quickAssignState = state.quickAssignState?.copy(
                        direction = direction,
                        availableSlots = res.slots,
                        selectedSlot = res.selectedSlot,
                        dailySchedulePlan = res.plan,
                        departureHour = res.selectedSlot?.hour ?: current.departureHour,
                        departureMinute = res.selectedSlot?.minute ?: current.departureMinute,
                        conflictError = null
                    )
                )
            }
        }
    }

    fun selectQuickAssignSlot(slot: TerminalDepartureSlot) {
        val current = _uiState.value.quickAssignState ?: return
        val slots = current.availableSlots
        var redirectNotice: String? = null
        val effectiveSlot = if (slot.isAvailable) {
            slot
        } else {
            val (nextH, nextM) = DepartureSlotCalculator.alignToNext15MinuteSlot(slot.totalMinutesOfDay + DepartureSlotCalculator.SLOT_INTERVAL_MINUTES)
            val nextSlot = DepartureSlotCalculator.findNextAvailableSlot(slots, nextH, nextM)
                ?: DepartureSlotCalculator.findNextAvailableSlot(slots, slot.hour, slot.minute)
            if (nextSlot != null) {
                val busInfo = slot.occupiedBusCode?.let { " oleh armada $it" } ?: ""
                redirectNotice = "Slot ${slot.formattedTime} tujuan ${slot.occupiedDestinationCity ?: "sama"} telah terisi$busInfo. Dialihkan ke slot ${nextSlot.formattedTime}."
                nextSlot
            } else {
                _uiState.update { state ->
                    state.copy(
                        quickAssignState = state.quickAssignState?.copy(
                            conflictError = "Slot ${slot.formattedTime} telah terisi dan tidak ada slot alternatif."
                        )
                    )
                }
                return
            }
        }
        applyQuickAssignSlot(effectiveSlot, redirectNotice)
    }

    private fun applyQuickAssignSlot(slot: TerminalDepartureSlot, noticeMessage: String? = null) {
        val current = _uiState.value.quickAssignState ?: return
        val route = current.selectedRoute ?: return
        val plan = DepartureSlotCalculator.calculateDailyPlan(
            routeCategory = current.routeCategory,
            originTerminalId = route.originTerminal.id,
            destTerminalId = route.destinationTerminal.id,
            estimatedDurationMinutes = route.totalDurationWithDwellMinutes,
            departureHour = slot.hour,
            departureMinute = slot.minute,
            targetRitCount = current.targetRitCount,
            initialDirection = current.direction
        )
        _uiState.update { state ->
            state.copy(
                quickAssignState = state.quickAssignState?.copy(
                    selectedSlot = slot,
                    departureHour = slot.hour,
                    departureMinute = slot.minute,
                    dailySchedulePlan = plan,
                    conflictError = noticeMessage
                )
            )
        }
    }

    fun setQuickAssignTargetRitCount(ritCount: Int) {
        val current = _uiState.value.quickAssignState ?: return
        if (current.routeCategory == RouteCategory.AKAP) return
        val route = current.selectedRoute ?: return
        val slot = current.selectedSlot ?: return
        val safeCount = if (current.routeCategory == RouteCategory.AKAP) 1 else 2
        val updatedPlan = DepartureSlotCalculator.calculateDailyPlan(
            routeCategory = current.routeCategory,
            originTerminalId = route.originTerminal.id,
            destTerminalId = route.destinationTerminal.id,
            estimatedDurationMinutes = route.totalDurationWithDwellMinutes,
            departureHour = slot.hour,
            departureMinute = slot.minute,
            targetRitCount = safeCount,
            initialDirection = current.direction
        )
        _uiState.update { state ->
            state.copy(
                quickAssignState = state.quickAssignState?.copy(
                    targetRitCount = safeCount,
                    dailySchedulePlan = updatedPlan
                )
            )
        }
    }

    fun setQuickAssignTimeFilter(filter: SlotTimeFilter) {
        _uiState.update { state ->
            state.copy(quickAssignState = state.quickAssignState?.copy(selectedTimeFilter = filter))
        }
    }

    fun setQuickAssignDepartureTime(hour: Int, minute: Int) {
        val current = _uiState.value.quickAssignState ?: return
        val matchingSlot = current.availableSlots.find { it.hour == hour && it.minute == minute }
        if (matchingSlot != null) {
            selectQuickAssignSlot(matchingSlot)
        } else {
            _uiState.update { state ->
                state.copy(
                    quickAssignState = state.quickAssignState?.copy(
                        departureHour = hour.coerceIn(0, 23),
                        departureMinute = minute.coerceIn(0, 59)
                    )
                )
            }
        }
    }

    fun setQuickAssignHeadway(minutes: Int) {
        _uiState.update { state ->
            state.copy(quickAssignState = state.quickAssignState?.copy(headwayMinutes = minutes.coerceIn(30, 240)))
        }
    }

    fun confirmQuickAssign() {
        val target = _uiState.value.quickAssignState ?: return
        val route = target.selectedRoute ?: return
        val driver = target.selectedDriver ?: return
        val slot = target.selectedSlot ?: return
        val useCase = assignBusToActiveRouteUseCase ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(quickAssignState = it.quickAssignState?.copy(isSubmitting = true)) }
            val result = useCase(
                busId = target.bus.id,
                routeId = route.id,
                driverId = driver.id,
                departureHour = slot.hour,
                departureMinute = slot.minute,
                direction = target.direction,
                headwayMinutes = target.headwayMinutes,
                transitDwellMinutes = BusRoute.STANDARD_TRANSIT_DWELL_MINUTES,
                dailySchedulePlan = target.dailySchedulePlan
            )

            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        quickAssignState = null,
                        userMessage = "Armada ${target.bus.busCode} berhasil ditugaskan ke ${route.name}!",
                        feedbackToast = FeedbackToastData(
                            iconEmoji = "🚌",
                            title = "Armada Berhasil Ditugaskan!",
                            subtitle = "${target.bus.busCode} siap melayani rute ${route.name}",
                            durationMillis = 1000L
                        )
                    )
                }
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Gagal menugaskan armada ke trayek"
                _uiState.update {
                    it.copy(quickAssignState = it.quickAssignState?.copy(conflictError = errorMsg, isSubmitting = false))
                }
            }
        }
    }

    fun dismissQuickAssignDialog() {
        _uiState.update { it.copy(quickAssignState = null) }
    }

    fun dismissUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    fun dismissFeedbackToast() {
        _uiState.update { it.copy(feedbackToast = null) }
    }
}
