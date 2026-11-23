package com.example.busmanager

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.busmanager.di.AppContainer
import com.example.busmanager.presentation.dashboard.HomeScreen
import com.example.busmanager.presentation.dashboard.HomeScreenViewModel
import com.example.busmanager.presentation.route.RoutePlannerScreen
import com.example.busmanager.presentation.route.RoutePlannerViewModel
import com.example.busmanager.presentation.showroom.ShowroomScreen
import com.example.busmanager.presentation.showroom.ShowroomViewModel
import com.example.busmanager.presentation.splash.SplashScreen

@Composable
fun MainNavigation(appContainer: AppContainer) {
    val backStack = rememberNavBackStack(SplashRoute)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<SplashRoute> {
                SplashScreen(
                    startNewGameUseCase = appContainer.startNewGameUseCase,
                    onNavigateToHome = {
                        backStack.clear()
                        backStack.add(HomeRoute)
                    }
                )
            }
            entry<HomeRoute> {
                val viewModel: HomeScreenViewModel = viewModel {
                    HomeScreenViewModel(
                        getCompanyUseCase = appContainer.getCompanyUseCase,
                        getOwnedFleetUseCase = appContainer.getOwnedFleetUseCase,
                        startNewGameUseCase = appContainer.startNewGameUseCase,
                        appUpdateManager = appContainer.appUpdateManager
                    )
                }
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToShowroom = {
                        backStack.add(ShowroomRoute)
                    },
                    onNavigateToRoutes = {
                        backStack.add(RoutePlannerRoute)
                    }
                )
            }
            entry<ShowroomRoute> {
                val viewModel: ShowroomViewModel = viewModel {
                    ShowroomViewModel(
                        getCompanyUseCase = appContainer.getCompanyUseCase,
                        getShowroomBusesUseCase = appContainer.getShowroomBusesUseCase,
                        buyBusUseCase = appContainer.buyBusUseCase,
                        getOwnedFleetUseCase = appContainer.getOwnedFleetUseCase
                    )
                }
                ShowroomScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        backStack.removeLastOrNull()
                    }
                )
            }
            entry<RoutePlannerRoute> {
                val viewModel: RoutePlannerViewModel = viewModel {
                    RoutePlannerViewModel(
                        getAvailableTerminalsUseCase = appContainer.getAvailableTerminalsUseCase,
                        getAvailableRoutesUseCase = appContainer.getAvailableRoutesUseCase,
                        getRouteTransitStopsUseCase = appContainer.getRouteTransitStopsUseCase,
                        calculateRouteCostUseCase = appContainer.calculateRouteCostUseCase,
                        getOwnedFleetUseCase = appContainer.getOwnedFleetUseCase,
                        routeRepository = appContainer.routeRepository
                    )
                }
                RoutePlannerScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        backStack.removeLastOrNull()
                    }
                )
            }
        }
    )
}
