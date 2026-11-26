package com.example.busmanager.domain.company.usecase

import com.example.busmanager.domain.company.repository.CompanyRepository
import com.example.busmanager.domain.driver.repository.DriverRepository
import com.example.busmanager.domain.finance.repository.FinancialTransactionRepository
import com.example.busmanager.domain.fleet.repository.FleetRepository
import com.example.busmanager.domain.route.repository.RoutePricingRepository
import com.example.busmanager.domain.route.repository.RouteRepository
import com.example.busmanager.domain.route.repository.RouteScheduleRepository
import com.example.busmanager.domain.simulation.model.GameCalendar
import com.example.busmanager.domain.simulation.repository.SimulationLogRepository
import com.example.busmanager.domain.simulation.repository.SimulationStateRepository
import com.example.busmanager.domain.simulation.repository.SimulationTimeRepository
import com.example.busmanager.domain.simulation.repository.SimulationTripRepository
import com.example.busmanager.domain.terminal.repository.TerminalPassengerRepository

class ResetGameUseCase(
    private val companyRepository: CompanyRepository,
    private val fleetRepository: FleetRepository,
    private val driverRepository: DriverRepository,
    private val routeScheduleRepository: RouteScheduleRepository,
    private val routePricingRepository: RoutePricingRepository,
    private val financialTransactionRepository: FinancialTransactionRepository? = null,
    private val terminalPassengerRepository: TerminalPassengerRepository? = null,
    private val simulationTripRepository: SimulationTripRepository? = null,
    private val simulationTimeRepository: SimulationTimeRepository? = null,
    private val simulationStateRepository: SimulationStateRepository? = null,
    private val routeRepository: RouteRepository? = null,
    private val simulationLogRepository: SimulationLogRepository? = null
) {
    suspend operator fun invoke() {
        simulationTripRepository?.clearTrips()
        simulationTimeRepository?.pause()
        simulationTimeRepository?.setTime(GameCalendar.initial())
        simulationStateRepository?.clearState()
        simulationLogRepository?.clearLogs()
        fleetRepository.removeAllBuses()
        driverRepository.resetAllDrivers()
        driverRepository.seedCandidateDriversIfEmpty()
        routeScheduleRepository.deleteAllSchedules()
        routePricingRepository.deleteAllPricings()
        routeRepository?.deleteAllCustomRoutes()
        financialTransactionRepository?.clearTransactions()
        terminalPassengerRepository?.clearAllRecords()
        companyRepository.resetCompany(
            name = StartNewGameUseCase.DEFAULT_COMPANY_NAME,
            initialBalance = StartNewGameUseCase.DEFAULT_STARTING_BALANCE
        )
    }
}
