package com.example.busmanager.presentation.route.wizard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import com.example.busmanager.presentation.route.wizard.RouteWizardStep
import com.example.busmanager.ui.theme.CardBorder
import com.example.busmanager.ui.theme.PrimaryBlue
import com.example.busmanager.ui.theme.SuccessGreen
import com.example.busmanager.ui.theme.SurfaceWhite
import com.example.busmanager.ui.theme.TextDark
import com.example.busmanager.ui.theme.TextMuted

@Composable
fun WizardBottomBar(
    currentStep: RouteWizardStep,
    canProceed: Boolean,
    isSaving: Boolean,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onLaunchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = SurfaceWhite,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentStep != RouteWizardStep.ORIGIN_DESTINATION) {
                OutlinedButton(
                    onClick = onPreviousClick,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.height(46.dp)
                ) {
                    Text(
                        text = "← Kembali",
                        color = TextDark,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(46.dp))
            }

            if (currentStep == RouteWizardStep.BUS_DRIVER_SCHEDULE) {
                Button(
                    onClick = onLaunchClick,
                    enabled = canProceed && !isSaving,
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SuccessGreen,
                        disabledContainerColor = Color(0xFFA7F3D0)
                    ),
                    modifier = Modifier.height(46.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Menyimpan...",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = "🚀 Jalankan & Simpan",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                val nextTitle = when (currentStep) {
                    RouteWizardStep.ORIGIN_DESTINATION -> "Lanjut ke Jalur →"
                    RouteWizardStep.PATH_AND_STOPS -> "Lanjut ke Tarif →"
                    RouteWizardStep.PRICING_MATRIX -> "Lanjut ke Armada →"
                    RouteWizardStep.BUS_DRIVER_SCHEDULE -> "Lanjut →"
                }

                Button(
                    onClick = onNextClick,
                    enabled = canProceed,
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue
                    ),
                    modifier = Modifier.height(46.dp)
                ) {
                    Text(
                        text = nextTitle,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
