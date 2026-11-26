package com.example.busmanager.presentation.showroom.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmanager.domain.company.model.Company
import com.example.busmanager.domain.fleet.model.Chassis
import com.example.busmanager.domain.fleet.model.KaroseriBody
import com.example.busmanager.presentation.showroom.ShowroomMode
import com.example.busmanager.presentation.showroom.ShowroomStep
import com.example.busmanager.ui.theme.AppDimens
import com.example.busmanager.ui.theme.IceBlue
import com.example.busmanager.ui.theme.PrimaryBlue
import com.example.busmanager.ui.theme.PrimaryBlueDark
import com.example.busmanager.ui.theme.SurfaceWhite
import com.example.busmanager.ui.theme.TextDark
import com.example.busmanager.ui.theme.TextMuted

@Composable
fun ShowroomTopBar(onNavigateBack: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = SurfaceWhite,
        shadowElevation = AppDimens.cardElevation
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppDimens.paddingMedium, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(IceBlue)
                    .clickable(onClick = onNavigateBack),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "◀",
                    color = PrimaryBlueDark,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(AppDimens.paddingMedium))

            Column {
                Text(
                    text = "Perakitan & Pembelian Armada",
                    color = TextDark,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Showroom & Karoseri PO TransNusantara",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun CompanyBalanceBanner(company: Company?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDimens.radiusMedium),
        colors = CardDefaults.cardColors(containerColor = PrimaryBlueDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppDimens.paddingMedium, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = company?.name ?: "PO TransNusantara",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp
                )
                Text(
                    text = "Saldo Kas Tersedia",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = company?.balance?.formatRupiah() ?: "Rp 0",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
fun ShowroomStepIndicator(
    currentStep: ShowroomStep,
    selectedChassis: Chassis?,
    selectedBody: KaroseriBody?,
    onStepClick: (ShowroomStep) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppDimens.radiusSmall))
            .background(IceBlue)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ShowroomStep.entries.forEach { step ->
            val isCurrent = step == currentStep
            val isCompleted = when (step) {
                ShowroomStep.CHASSIS -> selectedChassis != null
                ShowroomStep.BODY -> selectedBody != null
                ShowroomStep.CLASS -> selectedChassis != null && selectedBody != null
                ShowroomStep.REVIEW -> false
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isCurrent) PrimaryBlue else Color.Transparent)
                    .clickable { onStepClick(step) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isCompleted && !isCurrent) "${step.stepNumber}. ✓" else step.title,
                    color = if (isCurrent) Color.White else PrimaryBlueDark,
                    fontSize = 11.sp,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun ShowroomModeSelector(
    currentMode: ShowroomMode,
    onSelectMode: (ShowroomMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppDimens.radiusSmall))
            .background(IceBlue)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(6.dp))
                .background(if (currentMode == ShowroomMode.CUSTOM_ASSEMBLY) PrimaryBlue else Color.Transparent)
                .clickable { onSelectMode(ShowroomMode.CUSTOM_ASSEMBLY) }
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🛠️ Perakitan Kustom",
                color = if (currentMode == ShowroomMode.CUSTOM_ASSEMBLY) Color.White else PrimaryBlueDark,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(6.dp))
                .background(if (currentMode == ShowroomMode.INSTANT_PURCHASE) PrimaryBlue else Color.Transparent)
                .clickable { onSelectMode(ShowroomMode.INSTANT_PURCHASE) }
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "⚡ Beli Instan Pabrikan",
                color = if (currentMode == ShowroomMode.INSTANT_PURCHASE) Color.White else PrimaryBlueDark,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
