package com.example.busmanager.presentation.route.wizard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.busmanager.domain.route.model.Terminal
import com.example.busmanager.presentation.route.components.EastJavaMapCanvas
import com.example.busmanager.presentation.route.wizard.RouteWizardUiState
import com.example.busmanager.ui.theme.AppDimens
import com.example.busmanager.ui.theme.CardBorder
import com.example.busmanager.ui.theme.IceBlue
import com.example.busmanager.ui.theme.PrimaryBlue
import com.example.busmanager.ui.theme.PrimaryBlueDark
import com.example.busmanager.ui.theme.SurfaceWhite
import com.example.busmanager.ui.theme.TextDark
import com.example.busmanager.ui.theme.TextMuted

@Composable
fun Slide1OriginDestination(
    uiState: RouteWizardUiState,
    onSelectOrigin: (Terminal) -> Unit,
    onSelectDestination: (Terminal) -> Unit,
    onTerminalMapClick: (Terminal) -> Unit = {},
    onSwapClick: () -> Unit = {},
    onResetClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showOriginPicker by remember { mutableStateOf(false) }
    var showDestinationPicker by remember { mutableStateOf(false) }

    val origin = uiState.selectedOrigin
    val destination = uiState.selectedDestination
    val route = uiState.builtRoute

    if (showOriginPicker) {
        TerminalPickerModal(
            title = "Pilih Kota Asal Keberangkatan",
            terminals = uiState.allTerminals,
            selectedTerminalId = origin?.id,
            onSelect = {
                onSelectOrigin(it)
                showOriginPicker = false
            },
            onDismiss = { showOriginPicker = false }
        )
    }

    if (showDestinationPicker) {
        TerminalPickerModal(
            title = "Pilih Kota Tujuan Akhir",
            terminals = uiState.allTerminals.filter { it.id != origin?.id },
            selectedTerminalId = destination?.id,
            onSelect = {
                onSelectDestination(it)
                showDestinationPicker = false
            },
            onDismiss = { showDestinationPicker = false }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        item {
            Text(
                text = "Titik Asal & Tujuan Trayek",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Sentuh terminal di peta atau pilih dari daftar kota",
                fontSize = 12.sp,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                ) {
                    EastJavaMapCanvas(
                        terminals = uiState.allTerminals,
                        allSegments = uiState.allSegments,
                        selectedRoute = uiState.builtRoute,
                        selectedOrigin = origin,
                        selectedDestination = destination,
                        transitTerminals = route?.transitTerminals ?: emptyList(),
                        candidateTerminalIds = emptySet(),
                        onTerminalClick = { terminal ->
                            onTerminalMapClick(terminal)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )

                    val guideStatusText = when {
                        origin == null -> "👉 Sentuh kota keberangkatan asal (contoh: Jember)"
                        destination == null -> "👉 Sentuh kota tujuan akhir (contoh: Ngawi)"
                        else -> "✅ ${origin.city} ➔ ${destination.city} (${uiState.availablePaths.size} rute ditemukan)"
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp)
                            .shadow(4.dp, RoundedCornerShape(AppDimens.radiusPill))
                            .clip(RoundedCornerShape(AppDimens.radiusPill))
                            .background(Color(0xFF0F172A).copy(alpha = 0.90f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = guideStatusText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    if (origin != null || destination != null) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(10.dp)
                                .shadow(4.dp, RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFEE2E2))
                                .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(8.dp))
                                .clickable { onResetClick() }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🔄", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Reset",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFDC2626)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Terminal Operasional Rute",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (origin != null) Color(0xFFF0FDF4) else Color(0xFFF8FAFC))
                            .border(
                                1.dp,
                                if (origin != null) Color(0xFF86EFAC) else CardBorder,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { showOriginPicker = true }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFDCFCE7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "🟢",
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (origin != null) "${origin.city} (${origin.name})" else "Pilih Kota Asal",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (origin != null) TextDark else TextMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Titik Keberangkatan Awal Bus",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(PrimaryBlue.copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (origin != null) "Ganti ▾" else "Pilih ▾",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(CardBorder)
                        )

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SurfaceWhite)
                                .border(1.dp, CardBorder, CircleShape)
                                .clickable { onSwapClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "⇅",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (destination != null) Color(0xFFFEF2F2) else Color(0xFFF8FAFC))
                            .border(
                                1.dp,
                                if (destination != null) Color(0xFFFCA5A5) else CardBorder,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { showDestinationPicker = true }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFEE2E2)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "🔴",
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (destination != null) "${destination.city} (${destination.name})" else "Pilih Kota Tujuan",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (destination != null) TextDark else TextMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Titik Akhir Kedatangan Bus",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(PrimaryBlue.copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (destination != null) "Ganti ▾" else "Pilih ▾",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        if (origin != null && destination != null && route != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF86EFAC)))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "✨", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Jalur Algoritma Terbentuk",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF15803D)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(AppDimens.radiusPill))
                                    .background(Color(0xFFDCFCE7))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${uiState.availablePaths.size} Opsi Jalur",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF15803D)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceWhite)
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "Total Jarak", fontSize = 10.sp, color = TextMuted)
                                Text(
                                    text = "${route.totalDistanceKm} km",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlueDark
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "Estimasi Waktu", fontSize = 10.sp, color = TextMuted)
                                Text(
                                    text = "${uiState.estimatedDurationMinutes / 60}j ${uiState.estimatedDurationMinutes % 60}m",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlueDark
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "Kota Transit", fontSize = 10.sp, color = TextMuted)
                                Text(
                                    text = "${route.transitTerminals.size} Kota",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlueDark
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "💡 Tekan 'Lanjut' di bar bawah untuk memilih variasi jalur (Tol / Arteri) dan menentukan terminal singgah di Slide 2.",
                            fontSize = 11.sp,
                            color = Color(0xFF166534),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }
        }
    }
}

@Composable
private fun TerminalPickerModal(
    title: String,
    terminals: List<Terminal>,
    selectedTerminalId: String?,
    onSelect: (Terminal) -> Unit,
    onDismiss: () -> Unit
) {
    val sortedTerminals = remember(terminals) {
        terminals.sortedBy { it.city }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    TextButton(onClick = onDismiss) {
                        Text(text = "Tutup", fontSize = 12.sp, color = PrimaryBlue)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(sortedTerminals) { terminal ->
                        val isSelected = terminal.id == selectedTerminalId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) IceBlue else Color(0xFFF8FAFC))
                                .border(
                                    1.dp,
                                    if (isSelected) PrimaryBlue else Color(0xFFE2E8F0),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { onSelect(terminal) }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = terminal.city,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) PrimaryBlueDark else TextDark
                                )
                                Text(
                                    text = terminal.name,
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }

                            if (isSelected) {
                                Text(
                                    text = "✓ Terpilih",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlue
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
