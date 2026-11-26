package com.example.busmanager.presentation.route.wizard.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmanager.presentation.route.wizard.RouteWizardStep
import com.example.busmanager.ui.theme.AppDimens
import com.example.busmanager.ui.theme.CardBorder
import com.example.busmanager.ui.theme.DisabledBackground
import com.example.busmanager.ui.theme.IceBlue
import com.example.busmanager.ui.theme.PrimaryBlue
import com.example.busmanager.ui.theme.PrimaryBlueDark
import com.example.busmanager.ui.theme.SuccessGreen
import com.example.busmanager.ui.theme.SurfaceWhite
import com.example.busmanager.ui.theme.TextDark
import com.example.busmanager.ui.theme.TextLight
import com.example.busmanager.ui.theme.TextMuted

@Composable
fun WizardStepperHeader(
    currentStep: RouteWizardStep,
    onStepClick: (RouteWizardStep) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceWhite)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(AppDimens.radiusPill))
                    .background(IceBlue)
                    .clickable(onClick = onBackClick)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Kembali",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlueDark
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Buka Trayek Baru",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    text = "Langkah ${currentStep.stepNumber} dari 4: ${currentStep.title}",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RouteWizardStep.entries.forEachIndexed { index, step ->
                val isCompleted = step.stepNumber < currentStep.stepNumber
                val isCurrent = step == currentStep

                val circleBg = when {
                    isCurrent -> PrimaryBlue
                    isCompleted -> SuccessGreen
                    else -> DisabledBackground
                }
                val textColor = when {
                    isCurrent || isCompleted -> Color.White
                    else -> TextLight
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(circleBg)
                            .clickable { onStepClick(step) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${step.stepNumber}",
                            color = textColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (index < RouteWizardStep.entries.size - 1) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(3.dp)
                                .padding(horizontal = 4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (isCompleted) SuccessGreen else CardBorder)
                        )
                    }
                }
            }
        }
    }
}
