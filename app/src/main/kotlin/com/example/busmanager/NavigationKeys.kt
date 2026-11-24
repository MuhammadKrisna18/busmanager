package com.example.busmanager

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object SplashRoute : NavKey

@Serializable
data object HomeRoute : NavKey

@Serializable
data object RoutePlannerRoute : NavKey

@Serializable
data object ShowroomRoute : NavKey

@Serializable
data object SimulationMapRoute : NavKey

@Serializable
data object DriverManagementRoute : NavKey

@Serializable
data object GarageRoute : NavKey

@Serializable
data object FinanceRoute : NavKey

@Serializable
data object TerminalReportRoute : NavKey

@Serializable
data object RouteInfoRoute : NavKey
