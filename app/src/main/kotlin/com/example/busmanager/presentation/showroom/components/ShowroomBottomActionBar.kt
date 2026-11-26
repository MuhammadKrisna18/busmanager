package com.example.busmanager.presentation.showroom.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.presentation.showroom.ShowroomStep
import com.example.busmanager.ui.theme.AppDimens
import com.example.busmanager.ui.theme.DisabledGray
import com.example.busmanager.ui.theme.PrimaryBlue
import com.example.busmanager.ui.theme.SuccessGreen
import com.example.busmanager.ui.theme.SurfaceWhite

@Composable
fun ShowroomBottomActionBar(
    currentStep: ShowroomStep,
    canProceed: Boolean,
    canAfford: Boolean,
    isPurchasing: Boolean,
    batchQuantity: Int,
    totalInvestment: Money,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onConfirmPurchase: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = SurfaceWhite,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppDimens.paddingMedium, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(AppDimens.paddingSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentStep != ShowroomStep.CHASSIS) {
                OutlinedButton(
                    onClick = onPrevious,
                    shape = RoundedCornerShape(AppDimens.radiusMedium),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("◀ Kembali", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            if (currentStep == ShowroomStep.REVIEW) {
                Button(
                    onClick = onConfirmPurchase,
                    enabled = canProceed && canAfford && !isPurchasing,
                    shape = RoundedCornerShape(AppDimens.radiusMedium),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canAfford) SuccessGreen else DisabledGray
                    ),
                    modifier = Modifier.weight(if (currentStep == ShowroomStep.CHASSIS) 1f else 2f)
                ) {
                    val purchaseText = if (batchQuantity > 1) {
                        "🛒 Beli $batchQuantity Armada (${totalInvestment.formatRupiah()})"
                    } else {
                        "🛒 Beli Armada (${totalInvestment.formatRupiah()})"
                    }
                    Text(
                        text = if (isPurchasing) "Memproses..." else if (canAfford) purchaseText else "Saldo Tidak Cukup",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            } else {
                Button(
                    onClick = onNext,
                    enabled = canProceed,
                    shape = RoundedCornerShape(AppDimens.radiusMedium),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    modifier = Modifier.weight(if (currentStep == ShowroomStep.CHASSIS) 1f else 2f)
                ) {
                    Text("Lanjut Langkah Berikutnya ➔", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}
