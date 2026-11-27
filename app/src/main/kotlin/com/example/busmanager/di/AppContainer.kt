package com.example.busmanager.di

import android.content.Context
import com.example.busmanager.core.update.AppUpdateManager
import com.example.busmanager.data.local.database.BusManagerDatabase
import com.example.busmanager.data.repository.ChassisCatalogRepositoryImpl
import com.example.busmanager.data.repository.CompanyRepositoryImpl
import com.example.busmanager.data.repository.DriverRepositoryImpl
import com.example.busmanager.data.repository.FinancialTransactionRepositoryImpl
import com.example.busmanager.data.repository.FleetRepositoryImpl
import com.example.busmanager.data.repository.KaroseriCatalogRepositoryImpl
import com.example.busmanager.data.repository.QuestRepositoryImpl
import com.example.busmanager.data.repository.RouteCatalogRepositoryImpl
import com.example.busmanager.data.repository.RoutePricingRepositoryImpl
import com.example.busmanager.data.repository.RouteScheduleRepositoryImpl
import com.example.busmanager.data.repository.ShowroomCatalogRepositoryImpl
import com.example.busmanager.data.repository.SimulationStateRepositoryImpl
import com.example.busmanager.data.repository.SimulationTimeRepositoryImpl
import com.example.busmanager.data.repository.SimulationTripRepositoryImpl
import com.example.busmanager.data.repository.TerminalCatalogRepositoryImpl
import com.example.busmanager.data.repository.TerminalPassengerRepositoryImpl
import com.example.busmanager.domain.company.repository.CompanyRepository
import com.example.busmanager.domain.company.usecase.CalculateCompanyReputationUseCase
import com.example.busmanager.domain.company.usecase.CalculateCompanyTierUseCase
import com.example.busmanager.domain.company.usecase.GetCompanyUseCase
import com.example.busmanager.domain.company.usecase.ResetGameUseCase
import com.example.busmanager.domain.company.usecase.StartNewGameUseCase
import com.example.busmanager.domain.driver.repository.DriverRepository
import com.example.busmanager.domain.driver.usecase.AssignDriverToBusUseCase
import com.example.busmanager.domain.driver.usecase.GetAvailableDriversUseCase
import com.example.busmanager.domain.driver.usecase.GetHiredDriversUseCase
import com.example.busmanager.domain.driver.usecase.HireDriverUseCase
import com.example.busmanager.domain.finance.repository.FinancialTransactionRepository
import com.example.busmanager.domain.finance.usecase.GetFinancialTransactionsUseCase
import com.example.busmanager.domain.finance.usecase.RecordFinancialTransactionUseCase
import com.example.busmanager.domain.fleet.repository.ChassisCatalogRepository
import com.example.busmanager.domain.fleet.repository.FleetRepository
import com.example.busmanager.domain.fleet.repository.KaroseriCatalogRepository
import com.example.busmanager.domain.fleet.repository.ShowroomCatalogRepository
import com.example.busmanager.domain.fleet.usecase.BuyBusUseCase
import com.example.busmanager.domain.fleet.usecase.GetOwnedFleetUseCase
import com.example.busmanager.domain.fleet.usecase.GetShowroomBusesUseCase
import com.example.busmanager.domain.fleet.usecase.ReconcileBusOperatingStateUseCase
import com.example.busmanager.domain.fleet.usecase.SellBusUseCase
import com.example.busmanager.domain.fleet.usecase.ValidateBusOperationUseCase
import com.example.busmanager.domain.quest.repository.QuestRepository
import com.example.busmanager.domain.quest.usecase.ClaimQuestRewardUseCase
import com.example.busmanager.domain.quest.usecase.GetStarterQuestsUseCase
import com.example.busmanager.domain.route.repository.RoutePricingRepository
import com.example.busmanager.domain.route.repository.RouteRepository
import com.example.busmanager.domain.route.repository.RouteScheduleRepository
import com.example.busmanager.domain.route.repository.TerminalRepository
import com.example.busmanager.domain.terminal.repository.TerminalPassengerRepository
import com.example.busmanager.domain.route.usecase.AddRouteScheduleUseCase
import com.example.busmanager.domain.route.usecase.AssignBusToScheduleUseCase
import com.example.busmanager.domain.route.usecase.BuildCustomRouteUseCase
import com.example.busmanager.domain.route.usecase.DeleteRouteScheduleUseCase
import com.example.busmanager.domain.route.usecase.DeleteRouteUseCase
import com.example.busmanager.domain.route.usecase.FindAvailablePathsUseCase
import com.example.busmanager.domain.route.usecase.GetAvailableRoutesUseCase
import com.example.busmanager.domain.route.usecase.GetAvailableTerminalsUseCase
import com.example.busmanager.domain.route.usecase.GetRouteOperationalStatusUseCase
import com.example.busmanager.domain.route.usecase.GetRoutePricingsUseCase
import com.example.busmanager.domain.route.usecase.GetRouteSchedulesUseCase
import com.example.busmanager.domain.route.usecase.GetRouteTransitStopsUseCase
import com.example.busmanager.domain.route.usecase.InitializeDefaultRoutePricingsUseCase
import com.example.busmanager.domain.route.usecase.SaveCustomRouteUseCase
import com.example.busmanager.data.repository.SimulationLogRepositoryImpl
import com.example.busmanager.domain.route.usecase.AssignBusToActiveRouteUseCase
import com.example.busmanager.domain.route.usecase.LaunchCustomRouteUseCase
import com.example.busmanager.domain.route.usecase.SaveRoutePricingUseCase
import com.example.busmanager.domain.route.usecase.ValidateScheduleConflictUseCase
import com.example.busmanager.domain.simulation.repository.SimulationLogRepository
import com.example.busmanager.domain.simulation.repository.SimulationStateRepository
import com.example.busmanager.domain.simulation.repository.SimulationTimeRepository
import com.example.busmanager.domain.simulation.repository.SimulationTripRepository
import com.example.busmanager.domain.simulation.usecase.DispatchBusUseCase
import com.example.busmanager.domain.simulation.usecase.GetActiveTripsUseCase
import com.example.busmanager.domain.simulation.usecase.GetSimulationLogsUseCase
import com.example.busmanager.domain.simulation.usecase.GetSimulationTimeUseCase
import com.example.busmanager.domain.simulation.usecase.ProcessCompletedTripsUseCase
import com.example.busmanager.domain.simulation.usecase.ProcessOfflineProgressionUseCase
import com.example.busmanager.domain.simulation.usecase.RecordSimulationLogUseCase
import com.example.busmanager.domain.simulation.usecase.SaveSimulationStateUseCase
import com.example.busmanager.domain.simulation.usecase.SetSimulationSpeedUseCase
import com.example.busmanager.domain.simulation.usecase.ToggleSimulationPlayUseCase

class AppContainer(context: Context) {

    private val database: BusManagerDatabase = BusManagerDatabase.getInstance(context)

    val chassisCatalogRepository: ChassisCatalogRepository = ChassisCatalogRepositoryImpl()
    val karoseriCatalogRepository: KaroseriCatalogRepository = KaroseriCatalogRepositoryImpl()
    val showroomCatalogRepository: ShowroomCatalogRepository = ShowroomCatalogRepositoryImpl()
    val terminalRepository: TerminalRepository = TerminalCatalogRepositoryImpl()
    val routeRepository: RouteRepository = RouteCatalogRepositoryImpl(
        customRouteDao = database.customRouteDao(),
        terminalRepository = terminalRepository,
        routeScheduleDao = database.routeScheduleDao(),
        routePricingDao = database.routePricingDao()
    )
    val companyRepository: CompanyRepository = CompanyRepositoryImpl(database.companyDao())
    val fleetRepository: FleetRepository = FleetRepositoryImpl(
        database.ownedBusDao(),
        chassisCatalogRepository,
        karoseriCatalogRepository
    )
    val driverRepository: DriverRepository = DriverRepositoryImpl(
        database.driverDao(),
        database.ownedBusDao()
    )
    val routePricingRepository: RoutePricingRepository = RoutePricingRepositoryImpl(
        database.routePricingDao()
    )
    val routeScheduleRepository: RouteScheduleRepository = RouteScheduleRepositoryImpl(
        database.routeScheduleDao(),
        database.ownedBusDao()
    )
    val financialTransactionRepository: FinancialTransactionRepository = FinancialTransactionRepositoryImpl(
        database.financialTransactionDao()
    )
    val terminalPassengerRepository: TerminalPassengerRepository = TerminalPassengerRepositoryImpl(
        database.terminalPassengerRecordDao()
    )
    val questRepository: QuestRepository = QuestRepositoryImpl(context)

    val simulationStateRepository: SimulationStateRepository = SimulationStateRepositoryImpl(context)
    val simulationLogRepository: SimulationLogRepository =
        SimulationLogRepositoryImpl(database.simulationLogDao())
    val getSimulationLogsUseCase = GetSimulationLogsUseCase(simulationLogRepository)
    val recordSimulationLogUseCase = RecordSimulationLogUseCase(simulationLogRepository)

    val simulationTripRepository: SimulationTripRepository = SimulationTripRepositoryImpl(
        activeTripDao = database.activeTripDao(),
        routeRepository = routeRepository,
        recordSimulationLogUseCase = recordSimulationLogUseCase
    )
    val processCompletedTripsUseCase: ProcessCompletedTripsUseCase =
        ProcessCompletedTripsUseCase(
            fleetRepository = fleetRepository,
            simulationTripRepository = simulationTripRepository,
            companyRepository = companyRepository,
            financialTransactionRepository = financialTransactionRepository,
            terminalPassengerRepository = terminalPassengerRepository,
            routeScheduleRepository = routeScheduleRepository,
            driverRepository = driverRepository,
            recordSimulationLogUseCase = recordSimulationLogUseCase
        )
    val simulationTimeRepository: SimulationTimeRepository = SimulationTimeRepositoryImpl(
        simulationTripRepository = simulationTripRepository,
        simulationStateRepository = simulationStateRepository,
        processCompletedTripsUseCase = processCompletedTripsUseCase,
        initialPlayState = true
    )

    val startNewGameUseCase = StartNewGameUseCase(companyRepository, driverRepository)
    val resetGameUseCase = ResetGameUseCase(
        companyRepository = companyRepository,
        fleetRepository = fleetRepository,
        driverRepository = driverRepository,
        routeScheduleRepository = routeScheduleRepository,
        routePricingRepository = routePricingRepository,
        financialTransactionRepository = financialTransactionRepository,
        terminalPassengerRepository = terminalPassengerRepository,
        simulationTripRepository = simulationTripRepository,
        simulationTimeRepository = simulationTimeRepository,
        simulationStateRepository = simulationStateRepository,
        routeRepository = routeRepository,
        simulationLogRepository = simulationLogRepository,
        questRepository = questRepository
    )
    val getCompanyUseCase = GetCompanyUseCase(companyRepository)
    val getOwnedFleetUseCase = GetOwnedFleetUseCase(fleetRepository)
    val getShowroomBusesUseCase = GetShowroomBusesUseCase(showroomCatalogRepository)
    val buyBusUseCase = BuyBusUseCase(companyRepository, fleetRepository)
    val reconcileBusOperatingStateUseCase = ReconcileBusOperatingStateUseCase(
        fleetRepository = fleetRepository,
        simulationTripRepository = simulationTripRepository,
        routeRepository = routeRepository
    )
    val sellBusUseCase = SellBusUseCase(
        fleetRepository = fleetRepository,
        companyRepository = companyRepository,
        driverRepository = driverRepository,
        routeScheduleRepository = routeScheduleRepository,
        financialTransactionRepository = financialTransactionRepository,
        simulationTripRepository = simulationTripRepository
    )

    val getAvailableDriversUseCase = GetAvailableDriversUseCase(driverRepository)
    val getHiredDriversUseCase = GetHiredDriversUseCase(driverRepository)
    val hireDriverUseCase = HireDriverUseCase(driverRepository)
    val assignDriverToBusUseCase = AssignDriverToBusUseCase(driverRepository)

    val getAvailableTerminalsUseCase = GetAvailableTerminalsUseCase(terminalRepository)
    val getAvailableRoutesUseCase = GetAvailableRoutesUseCase(routeRepository)
    val getRouteTransitStopsUseCase = GetRouteTransitStopsUseCase(routeRepository)
    val findAvailablePathsUseCase = FindAvailablePathsUseCase(terminalRepository, routeRepository)
    val buildCustomRouteUseCase = BuildCustomRouteUseCase(routeRepository)
    val saveCustomRouteUseCase = SaveCustomRouteUseCase(routeRepository)
    val deleteRouteUseCase = DeleteRouteUseCase(
        routeRepository,
        fleetRepository,
        simulationTripRepository,
        routeScheduleRepository,
        routePricingRepository
    )

    val getRoutePricingsUseCase = GetRoutePricingsUseCase(routePricingRepository)
    val saveRoutePricingUseCase = SaveRoutePricingUseCase(routePricingRepository)
    val initializeDefaultRoutePricingsUseCase = InitializeDefaultRoutePricingsUseCase(routePricingRepository)

    val launchCustomRouteUseCase = LaunchCustomRouteUseCase(
        routeRepository = routeRepository,
        routePricingRepository = routePricingRepository,
        driverRepository = driverRepository,
        fleetRepository = fleetRepository,
        routeScheduleRepository = routeScheduleRepository
    )

    val getRouteSchedulesUseCase = GetRouteSchedulesUseCase(routeScheduleRepository)
    val addRouteScheduleUseCase = AddRouteScheduleUseCase(routeScheduleRepository)
    val getStarterQuestsUseCase = GetStarterQuestsUseCase(questRepository)
    val claimQuestRewardUseCase = ClaimQuestRewardUseCase(
        questRepository = questRepository,
        companyRepository = companyRepository,
        financialTransactionRepository = financialTransactionRepository
    )
    val assignBusToScheduleUseCase = AssignBusToScheduleUseCase(routeScheduleRepository)
    val deleteRouteScheduleUseCase = DeleteRouteScheduleUseCase(routeScheduleRepository)
    val validateScheduleConflictUseCase = ValidateScheduleConflictUseCase(routeScheduleRepository, routeRepository)
    val assignBusToActiveRouteUseCase = AssignBusToActiveRouteUseCase(
        fleetRepository = fleetRepository,
        routeRepository = routeRepository,
        driverRepository = driverRepository,
        routeScheduleRepository = routeScheduleRepository,
        validateScheduleConflictUseCase = validateScheduleConflictUseCase
    )

    val validateBusOperationUseCase = ValidateBusOperationUseCase(driverRepository, routePricingRepository)
    val getRouteOperationalStatusUseCase = GetRouteOperationalStatusUseCase(
        routeRepository = routeRepository,
        routeScheduleRepository = routeScheduleRepository,
        routePricingRepository = routePricingRepository,
        fleetRepository = fleetRepository,
        driverRepository = driverRepository,
        simulationTripRepository = simulationTripRepository
    )
    val getFinancialTransactionsUseCase = GetFinancialTransactionsUseCase(financialTransactionRepository)
    val recordFinancialTransactionUseCase = RecordFinancialTransactionUseCase(financialTransactionRepository, companyRepository)

    val getSimulationTimeUseCase = GetSimulationTimeUseCase(simulationTimeRepository)
    val toggleSimulationPlayUseCase = ToggleSimulationPlayUseCase(simulationTimeRepository)
    val setSimulationSpeedUseCase = SetSimulationSpeedUseCase(simulationTimeRepository)
    val getActiveTripsUseCase = GetActiveTripsUseCase(simulationTripRepository)
    val dispatchBusUseCase = DispatchBusUseCase(simulationTripRepository)
    val saveSimulationStateUseCase = SaveSimulationStateUseCase(simulationStateRepository, simulationTimeRepository)
    val processOfflineProgressionUseCase = ProcessOfflineProgressionUseCase(
        simulationStateRepository = simulationStateRepository,
        simulationTimeRepository = simulationTimeRepository,
        companyRepository = companyRepository,
        fleetRepository = fleetRepository,
        driverRepository = driverRepository,
        routeRepository = routeRepository,
        routeScheduleRepository = routeScheduleRepository,
        routePricingRepository = routePricingRepository,
        financialTransactionRepository = financialTransactionRepository,
        terminalPassengerRepository = terminalPassengerRepository
    )

    val appUpdateManager = AppUpdateManager()

    val calculateCompanyReputationUseCase = CalculateCompanyReputationUseCase()
    val calculateCompanyTierUseCase = CalculateCompanyTierUseCase()
}
