package com.example.busmanager.presentation.route

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.RoutePathType
import com.example.busmanager.domain.route.model.RouteTripEstimate
import com.example.busmanager.domain.route.model.Terminal
import com.example.busmanager.presentation.route.components.EastJavaMapCanvas
import com.example.busmanager.presentation.route.components.RouteMapLegend
import com.example.busmanager.ui.theme.AccentBlue
import com.example.busmanager.ui.theme.AppDimens
import com.example.busmanager.ui.theme.BackgroundWhite
import com.example.busmanager.ui.theme.CardBorder
import com.example.busmanager.ui.theme.DisabledBackground
import com.example.busmanager.ui.theme.IceBlue
import com.example.busmanager.ui.theme.PrimaryBlue
import com.example.busmanager.ui.theme.PrimaryBlueDark
import com.example.busmanager.ui.theme.SkyBlue
import com.example.busmanager.ui.theme.SuccessGreen
import com.example.busmanager.ui.theme.SurfaceWhite
import com.example.busmanager.ui.theme.TextDark
import com.example.busmanager.ui.theme.TextLight
import com.example.busmanager.ui.theme.TextMuted
import com.example.busmanager.ui.theme.WarningAmber

@Composable
fun RoutePlannerScreen(
    viewModel: RoutePlannerViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            RouteTopBar(onNavigateBack = onNavigateBack)
        },
        containerColor = BackgroundWhite,
        modifier = modifier
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = AppDimens.paddingMedium),
                contentPadding = PaddingValues(vertical = AppDimens.paddingSmall),
                verticalArrangement = Arrangement.spacedBy(AppDimens.paddingMedium)
            ) {
                item {
                    EastJavaMapCanvas(
                        terminals = uiState.terminals,
                        allSegments = uiState.allSegments,
                        selectedRoute = uiState.selectedRoute,
                        selectedOrigin = uiState.selectedOrigin,
                        selectedDestination = uiState.selectedDestination,
                        transitTerminals = uiState.selectedRoute?.transitTerminals ?: emptyList(),
                        onTerminalClick = { terminal ->
                            viewModel.onTerminalMapClick(terminal)
                        }
                    )
                }

                item {
                    RouteMapLegend()
                }

                item {
                    TerminalSelectorCard(
                        terminals = uiState.terminals,
                        selectedOrigin = uiState.selectedOrigin,
                        selectedDestination = uiState.selectedDestination,
                        onOriginSelect = { viewModel.selectOrigin(it) },
                        onDestinationSelect = { viewModel.selectDestination(it) },
                        onSwapClick = { viewModel.swapOriginDestination() }
                    )
                }

                item {
                    PathTypeFilterRow(
                        selectedPathType = uiState.preferredPathType,
                        onPathTypeSelected = { viewModel.setPreferredPathType(it) }
                    )
                }

                item {
                    Text(
                        text = "Pilihan Koridor Jalur (${uiState.availableRoutes.size})",
                        color = TextDark,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (uiState.availableRoutes.isEmpty()) {
                    item {
                        EmptyRouteNotice()
                    }
                } else {
                    items(uiState.availableRoutes) { route ->
                        RouteOptionCard(
                            route = route,
                            isSelected = uiState.selectedRoute?.id == route.id,
                            onSelect = { viewModel.selectRoute(route) }
                        )
                    }
                }

                if (uiState.tripEstimate != null) {
                    item {
                        TripEstimateCard(estimate = uiState.tripEstimate!!)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(AppDimens.paddingLarge))
                }
            }
        }
    }
}

@Composable
private fun RouteTopBar(onNavigateBack: () -> Unit) {
    Surface(
        color = SurfaceWhite,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppDimens.paddingSmall, vertical = AppDimens.paddingSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Text(text = "←", fontSize = 24.sp, color = PrimaryBlue, fontWeight = FontWeight.Bold)
            }

            Column(modifier = Modifier.padding(start = 4.dp)) {
                Text(
                    text = "Rute & Jalur Trayek",
                    color = TextDark,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Peta Koridor Antar Terminal Jawa Timur",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun TerminalSelectorCard(
    terminals: List<Terminal>,
    selectedOrigin: Terminal?,
    selectedDestination: Terminal?,
    onOriginSelect: (Terminal) -> Unit,
    onDestinationSelect: (Terminal) -> Unit,
    onSwapClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDimens.radiusMedium),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(AppDimens.paddingMedium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Terminal Keberangkatan (Asal)",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    TerminalDropdownSelector(
                        terminals = terminals,
                        selected = selectedOrigin,
                        dotColor = SuccessGreen,
                        onSelect = onOriginSelect
                    )
                }

                IconButton(
                    onClick = onSwapClick,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .clip(CircleShape)
                        .background(AccentBlue)
                ) {
                    Text(text = "⇄", fontSize = 18.sp, color = PrimaryBlueDark, fontWeight = FontWeight.Bold)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Terminal Tujuan (Akhir)",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    TerminalDropdownSelector(
                        terminals = terminals,
                        selected = selectedDestination,
                        dotColor = PrimaryBlue,
                        onSelect = onDestinationSelect
                    )
                }
            }
        }
    }
}

@Composable
private fun TerminalDropdownSelector(
    terminals: List<Terminal>,
    selected: Terminal?,
    dotColor: Color,
    onSelect: (Terminal) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(AppDimens.radiusSmall))
                .background(Color(0xFFF1F5F9))
                .border(1.dp, CardBorder, RoundedCornerShape(AppDimens.radiusSmall))
                .clickable { expanded = true }
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = selected?.city ?: "Pilih...",
                    color = TextDark,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
            Text(text = "▾", color = TextMuted, fontSize = 12.sp)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            terminals.forEach { terminal ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(
                                text = terminal.city,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = terminal.name,
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    },
                    onClick = {
                        onSelect(terminal)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun PathTypeFilterRow(
    selectedPathType: RoutePathType?,
    onPathTypeSelected: (RoutePathType?) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppDimens.paddingSmall)
    ) {
        FilterChip(
            selected = selectedPathType == null,
            onClick = { onPathTypeSelected(null) },
            label = { Text("Semua Jalur", fontSize = 12.sp) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = PrimaryBlue,
                selectedLabelColor = Color.White
            )
        )

        FilterChip(
            selected = selectedPathType == RoutePathType.TOLL,
            onClick = { onPathTypeSelected(RoutePathType.TOLL) },
            label = { Text("Tol Trans-Jawa", fontSize = 12.sp) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = PrimaryBlue,
                selectedLabelColor = Color.White
            )
        )

        FilterChip(
            selected = selectedPathType == RoutePathType.NON_TOLL,
            onClick = { onPathTypeSelected(RoutePathType.NON_TOLL) },
            label = { Text("Arteri / Non-Tol", fontSize = 12.sp) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = PrimaryBlue,
                selectedLabelColor = Color.White
            )
        )
    }
}

@Composable
private fun RouteOptionCard(
    route: BusRoute,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val borderColor = if (isSelected) PrimaryBlue else CardBorder
    val borderWidth = if (isSelected) 2.dp else 1.dp
    val containerColor = if (isSelected) IceBlue else SurfaceWhite

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(AppDimens.radiusMedium),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = androidx.compose.foundation.BorderStroke(borderWidth, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(AppDimens.paddingMedium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = route.name,
                    color = TextDark,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(AppDimens.radiusPill))
                        .background(if (route.pathType == RoutePathType.TOLL) AccentBlue else Color(0xFFF1F5F9))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = route.pathType.displayName,
                        color = if (route.pathType == RoutePathType.TOLL) PrimaryBlueDark else TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MetricSnippet(label = "Jarak", value = "${route.totalDistanceKm.toInt()} km")
                MetricSnippet(
                    label = "Waktu",
                    value = "${route.totalEstimatedDurationMinutes / 60}j ${route.totalEstimatedDurationMinutes % 60}m"
                )
                MetricSnippet(
                    label = "Biaya Tol",
                    value = if (route.totalTollFee.amount > 0) route.totalTollFee.formatRupiah() else "Gratis"
                )
            }

            if (route.transitTerminals.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFFEF3C7).copy(alpha = 0.5f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🚌 Transit:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = route.transitTerminals.joinToString(" • ") { "${it.name} (${it.city})" },
                        fontSize = 11.sp,
                        color = Color(0xFF78350F)
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricSnippet(label: String, value: String) {
    Column {
        Text(text = label, color = TextLight, fontSize = 10.sp)
        Text(text = value, color = TextDark, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun TripEstimateCard(estimate: RouteTripEstimate) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDimens.radiusMedium),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(AppDimens.paddingMedium)) {
            Text(
                text = "Estimasi Operasional Perjalanan",
                color = TextDark,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Konsumsi Solar (${String.format("%.1f", estimate.estimatedFuelLiters)} L)", color = TextMuted, fontSize = 12.sp)
                Text(text = estimate.estimatedFuelCost.formatRupiah(), color = TextDark, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Tarif e-Toll", color = TextMuted, fontSize = 12.sp)
                Text(text = estimate.tollFee.formatRupiah(), color = TextDark, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Retribusi Terminal & Peron", color = TextMuted, fontSize = 12.sp)
                Text(text = estimate.terminalRetributionFee.formatRupiah(), color = TextDark, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(CardBorder))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Total Biaya Perjalanan", color = TextDark, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(text = estimate.totalOperationalCost.formatRupiah(), color = PrimaryBlue, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun EmptyRouteNotice() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDimens.radiusMedium),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.paddingLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "🗺️", fontSize = 32.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tidak Ada Rute Langsung",
                color = TextDark,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Pilih kombinasi terminal asal dan tujuan lain di peta Jawa Timur di atas.",
                color = TextMuted,
                fontSize = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
