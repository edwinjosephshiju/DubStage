package com.example.dubstage.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dubstage.ui.theme.Gold
import com.example.dubstage.ui.theme.Red

@Composable
fun CountdownOverlay(
    countdownNumber: Int,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(200)) + scaleIn(tween(200)),
        exit = fadeOut(tween(200)) + scaleOut(tween(200)),
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xCC0A0B12)),
            contentAlignment = Alignment.Center
        ) {
            val scale by animateFloatAsState(
                targetValue = 1.2f,
                animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
                label = "countdown_scale"
            )

            // Neon Radial Glow Circle
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = if (countdownNumber == 0) listOf(Red.copy(alpha = 0.8f), Color.Transparent)
                            else listOf(com.example.dubstage.ui.theme.Acc.copy(alpha = 0.7f), Color.Transparent)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (countdownNumber == 0) "GO!" else countdownNumber.toString(),
                    fontSize = if (countdownNumber == 0) 52.sp else 64.sp,
                    fontWeight = FontWeight.Black,
                    color = if (countdownNumber == 0) Color.White else com.example.dubstage.ui.theme.Acc
                )
            }
        }
    }
}
