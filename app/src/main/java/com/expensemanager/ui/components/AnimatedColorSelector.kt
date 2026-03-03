package com.expensemanager.ui.components

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun AnimatedColorSelector(
    colors: List<Color>,
    selected: Color,
    onSelect: (Color) -> Unit
) {
    val context = LocalContext.current

    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        colors.forEach { color ->
            val scale by animateFloatAsState(
                targetValue = if (selected == color) 1.2f else 1f,
                label = ""
            )

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .clip(CircleShape)
                    .background(color)
                    .clickable {
                        val vibrator =
                            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                        vibrator.vibrate(
                            VibrationEffect.createOneShot(
                                30,
                                VibrationEffect.DEFAULT_AMPLITUDE
                            )
                        )
                        onSelect(color)
                    }
            )
        }
    }
}