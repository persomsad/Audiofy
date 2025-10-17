package com.audiofy.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.audiofy.app.ui.theme.AudiofyColors
import com.audiofy.app.ui.theme.AudiofySpacing

@Composable
fun CircularProgressWithLabel(progress: Float, modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(128.dp), contentAlignment = Alignment.Center) {
        val animatedProgress by animateFloatAsState(
            targetValue = progress,
            animationSpec = tween(300)
        )
        Canvas(Modifier.fillMaxSize()) {
            val stroke = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
            drawCircle(color = AudiofyColors.Neutral400, style = stroke)
            drawArc(
                color = AudiofyColors.Primary500,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = stroke
            )
        }
        Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.headlineMedium, color = AudiofyColors.Primary500)
    }
}

@Composable
fun ProcessStepCard(steps: List<Triple<String, Boolean, Boolean>>, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.padding(AudiofySpacing.Space5), verticalArrangement = Arrangement.spacedBy(AudiofySpacing.Space4)) {
            steps.forEach { (name, completed, inProgress) ->
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Row(horizontalArrangement = Arrangement.spacedBy(AudiofySpacing.Space3), verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            Modifier.size(32.dp),
                            shape = CircleShape,
                            color = when {
                                completed -> AudiofyColors.Primary100
                                inProgress -> AudiofyColors.Primary500
                                else -> AudiofyColors.Neutral400
                            }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                when {
                                    completed -> Icon(Icons.Default.Check, null, tint = AudiofyColors.Primary500, modifier = Modifier.size(16.dp))
                                    inProgress -> CircularProgressIndicator(Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                                    else -> Box(Modifier.size(8.dp))
                                }
                            }
                        }
                        Text(name, style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(
                        when {
                            completed -> "完成"
                            inProgress -> "进行中"
                            else -> "等待中"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = when {
                            completed || inProgress -> AudiofyColors.Primary500
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}
