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
import com.example.busmanager.data.repository.RouteCatalogRepositoryImpl
import com.example.busmanager.data.repository.RoutePricingRepositoryImpl
import com.example.busmanager.data.repository.RouteScheduleRepositoryImpl
import com.example.busmanager.data.repository.ShowroomCatalogRepositoryImpl
import com.example.busmanager.data.repository.SimulationTimeRepositoryImpl
import com.example.busmanager.data.repository.SimulationTripRepositoryImpl
import com.example.busmanager.data.repository.TerminalCatalogRepositoryImpl
import com.example.busmanager.data.repository.TerminalPassengerRepositoryImpl
import com.example.busmanager.domain.company.repository.CompanyRepository
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
import com.example.busmanager.domain.fleet.usecase.SellBusUseCase
import com.example.busmanager.domain.fleet.usecase.ValidateBusOperationUseCase
import com.example.busmanager.domain.route.repository.RoutePricingRepository
import com.example.busmanager.domain.route.repository.RouteRepository
import com.example.busmanager.domain.route.repository.RouteScheduleRepository
import com.example.busmanager.domain.route.repository.TerminalRepository
import com.example.busmanager.domain.terminal.repository.TerminalPassengerRepository
import com.example.busmanager.domain.route.usecase.AddRouteScheduleUseCase
import com.example.busmanager.domain.route.usecase.AssignBusToScheduleUseCase
import com.example.busmanager.domain.route.usecase.BuildCustomRouteUseCase
import com.example.busmanager.domain.route.usecase.CalculateRouteCostUseCase
import com.example.busmanager.domain.route.usecase.DeleteRouteScheduleUseCase
import com.example.busmanager.domain.route.usecase.FindAvailablePathsUseCase
import com.example.busmanager.domain.route.usecase.GetAvailableRoutesUseCase
import com.example.busmanager.domain.route.usecase.GetAvailableTerminalsUseCase
import com.example.busmanager.domain.route.usecase.GetConnectedTerminalsUseCase
import com.example.busmanager.domain.route.usecase.GetRouteOperationalStatusUseCase
import com.example.busmanager.domain.route.usecase.GetRoutePricingsUseCase
import com.example.busmanager.domain.route.usecase.GetRouteSchedulesUseCase
import com.example.busmanager.domain.route.usecase.GetRouteTransitStopsUseCase
import com.example.busmanager.domain.route.usecase.InitializeDefaultRoutePricingsUseCase
import com.example.busmanager.domain.route.usecase.SaveCustomRouteUseCase
import com.example.busmanager.domain.route.usecase.SaveRoutePricingUseCase
import com.example.busmanager.domain.simulation.repository.SimulationTimeRepository
import com.example.busmanager.domain.simulation.repository.SimulationTripRepository
import com.example.busmanager.domain.simulation.usecase.DispatchBusUseCase
import com.example.busmanager.domain.simulation.usecase.GetActiveTripsUseCase
import com.example.busmanager.domain.simulation.usecase.GetSimulationTimeUseCase
import com.example.busmanager.domain.simulation.usecase.SetSimulationSpeedUseCase
import com.example.busmanager.domain.simulation.usecase.ToggleSimulationPlayUseCase

class AppContainer(context: Context) {

    private val database: BusManagerDatabase = BusManagerDatabase.getInstance(context)

    val chassisCatalogRepository: ChassisCatalogRepository = ChassisCatalogRepositoryImpl()
    val karoseriCatalogRepository: KaroseriCatalogRepository = KaroseriCatalogRepositoryImpl()
    val showroomCatalogRepository: ShowroomCatalogRepository = ShowroomCatalogRepositoryImpl()
    val terminalRepository: TerminalRepository = TerminalCatalogRepositoryImpl()
    val routeRepository: RouteRepository = RouteCatalogRepositoryImpl()
    val simulationTripRepository: SimulationTripRepository = SimulationTripRepositoryImpl()
    val simulationTimeRepository: SimulationTimeRepository = SimulationTimeRepositoryImpl(
        simulationTripRepository = simulationTripRepository
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

    val startNewGameUseCase = StartNewGameUseCase(companyRepository)
    val resetGameUseCase = ResetGameUseCase(
        companyRepository = companyRepository,
        fleetRepository = fleetRepository,
        driverRepository = driverRepository,
        routeScheduleRepository = routeScheduleRepository,
        routePricingRepository = routePricingRepository,
        financialTransactionRepository = financialTransactionRepository,
        terminalPassengerRepository = terminalPassengerRepository,
        simulationTripRepository = simulationTripRepository,
        simulationTimeRepository = simulationTimeRepository
    )
    val getCompanyUseCase = GetCompanyUseCase(companyRepository)
    val getOwnedFleetUseCase = GetOwnedFleetUseCase(fleetRepository)
    val getShowroomBusesUseCase = GetShowroomBusesUseCase(showroomCatalogRepository)
    val buyBusUseCase = BuyBusUseCase(companyRepository, fleetRepository)
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
    val calculateRouteCostUseCase = CalculateRouteCostUseCase()
    val getConnectedTerminalsUseCase = GetConnectedTerminalsUseCase(terminalRepository, routeRepository)
    val findAvailablePathsUseCase = FindAvailablePathsUseCase(terminalRepository, routeRepository)
    val buildCustomRouteUseCase = BuildCustomRouteUseCase(routeRepository)
    val saveCustomRouteUseCase = SaveCustomRouteUseCase(routeRepository)

    val getRoutePricingsUseCase = GetRoutePricingsUseCase(routePricingRepository)
    val saveRoutePricingUseCase = SaveRoutePricingUseCase(routePricingRepository)
    val initializeDefaultRoutePricingsUseCase = InitializeDefaultRoutePricingsUseCase(routePricingRepository)

    val getRouteSchedulesUseCase = GetRouteSchedulesUseCase(routeScheduleRepository)
    val addRouteScheduleUseCase = AddRouteScheduleUseCase(routeScheduleRepository)
    val assignBusToScheduleUseCase = AssignBusToScheduleUseCase(routeScheduleRepository)
    val deleteRouteScheduleUseCase = DeleteRouteScheduleUseCase(routeScheduleRepository)

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

    val appUpdateManager = AppUpdateManager()
}
