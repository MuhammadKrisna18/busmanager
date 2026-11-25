package com.example.busmanager.presentation.route.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmanager.domain.route.model.Terminal
import com.example.busmanager.ui.theme.AccentBlue
import com.example.busmanager.ui.theme.AppDimens
import com.example.busmanager.ui.theme.CardBorder
import com.example.busmanager.ui.theme.PrimaryBlue
import com.example.busmanager.ui.theme.PrimaryBlueDark
import com.example.busmanager.ui.theme.SkyBlue
import com.example.busmanager.ui.theme.SuccessGreen
import com.example.busmanager.ui.theme.SurfaceWhite
import com.example.busmanager.ui.theme.TextDark
import com.example.busmanager.ui.theme.WarningAmber

enum class TerminalNodeRole {
    ORIGIN,
    DESTINATION,
    TRANSIT,
    NEXT_CANDIDATE,
    DEFAULT
}

@Composable
fun TerminalMarker(
    terminal: Terminal,
    role: TerminalNodeRole,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val nodeColor = when (role) {
        TerminalNodeRole.ORIGIN -> SuccessGreen
        TerminalNodeRole.DESTINATION -> PrimaryBlue
        TerminalNodeRole.TRANSIT -> WarningAmber
        TerminalNodeRole.NEXT_CANDIDATE -> Color(0xFFFFB300)
        TerminalNodeRole.DEFAULT -> SurfaceWhite
    }

    val badgeText = when (role) {
        TerminalNodeRole.ORIGIN -> "Asal"
        TerminalNodeRole.DESTINATION -> "Tujuan"
        TerminalNodeRole.TRANSIT -> null
        TerminalNodeRole.NEXT_CANDIDATE -> "✨ Sambung"
        TerminalNodeRole.DEFAULT -> null
    }

    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (badgeText != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(AppDimens.radiusPill))
                    .background(nodeColor)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = badgeText,
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
        }

        Box(
            modifier = Modifier
                .size(if (role != TerminalNodeRole.DEFAULT) 22.dp else 16.dp)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(nodeColor)
                .border(
                    width = if (role != TerminalNodeRole.DEFAULT) 2.5.dp else 1.5.dp,
                    color = if (role != TerminalNodeRole.DEFAULT) Color.White else PrimaryBlue,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (role != TerminalNodeRole.DEFAULT) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(SurfaceWhite.copy(alpha = 0.92f))
                .border(0.5.dp, CardBorder, RoundedCornerShape(4.dp))
                .padding(horizontal = 4.dp, vertical = 1.dp)
        ) {
            Text(
                text = terminal.city,
                color = TextDark,
                fontSize = 10.sp,
                fontWeight = if (role != TerminalNodeRole.DEFAULT) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}
