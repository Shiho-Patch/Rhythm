package chromahub.rhythm.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

/**
 * Progress bar style options for MiniPlayer and Player
 * Following Compose December 2025 best practices with optimized animations
 */
enum class ProgressStyle {
    NORMAL,     // Standard LinearProgressIndicator
    WAVY,       // Animated wavy line
    ROUNDED,    // Rounded pill-shaped progress
    THIN,       // Thin elegant line
    THICK,      // Thick bold progress bar
    GRADIENT,   // Gradient colored progress
    SEGMENTED,  // Segmented/dotted progress
    DOTS        // Dots indicator
}

/**
 * Unified progress bar composable that renders different styles
 */
@Composable
fun StyledProgressBar(
    progress: Float,
    style: ProgressStyle,
    modifier: Modifier = Modifier,
    progressColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
    height: Dp = 4.dp,
    isPlaying: Boolean = true,
    animated: Boolean = true,
    showThumb: Boolean = false,
    thumbSize: Dp = 12.dp,
    waveFrequency: Float = 4f
) {
    when (style) {
        ProgressStyle.NORMAL -> NormalProgressBar(
            progress = progress,
            modifier = modifier,
            progressColor = progressColor,
            trackColor = trackColor,
            height = height,
            showThumb = showThumb,
            thumbSize = thumbSize
        )
        ProgressStyle.WAVY -> WavyProgressBar(
            progress = progress,
            modifier = modifier,
            progressColor = progressColor,
            trackColor = trackColor,
            height = height,
            isPlaying = isPlaying && animated,
            waveFrequency = waveFrequency
        )
        ProgressStyle.ROUNDED -> RoundedProgressBar(
            progress = progress,
            modifier = modifier,
            progressColor = progressColor,
            trackColor = trackColor,
            height = height
        )
        ProgressStyle.THIN -> ThinProgressBar(
            progress = progress,
            modifier = modifier,
            progressColor = progressColor,
            trackColor = trackColor
        )
        ProgressStyle.THICK -> ThickProgressBar(
            progress = progress,
            modifier = modifier,
            progressColor = progressColor,
            trackColor = trackColor
        )
        ProgressStyle.GRADIENT -> GradientProgressBar(
            progress = progress,
            modifier = modifier,
            trackColor = trackColor,
            height = height
        )
        ProgressStyle.SEGMENTED -> SegmentedProgressBar(
            progress = progress,
            modifier = modifier,
            progressColor = progressColor,
            trackColor = trackColor,
            height = height
        )
        ProgressStyle.DOTS -> DotsProgressBar(
            progress = progress,
            modifier = modifier,
            activeColor = progressColor,
            inactiveColor = trackColor
        )
    }
}

/**
 * Standard Material3 LinearProgressIndicator
 */
@Composable
private fun NormalProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    progressColor: Color,
    trackColor: Color,
    height: Dp,
    showThumb: Boolean = false,
    thumbSize: Dp = 12.dp
) {
    if (showThumb) {
        Canvas(
            modifier = modifier
                .fillMaxWidth()
                .height(height.coerceAtLeast(thumbSize))
        ) {
            val progressWidth = size.width * progress.coerceIn(0f, 1f)
            val centerY = size.height / 2
            val trackHeight = height.toPx()
            
            // Draw track
            drawRoundRect(
                color = trackColor,
                topLeft = Offset(0f, centerY - trackHeight / 2),
                size = androidx.compose.ui.geometry.Size(size.width, trackHeight),
                cornerRadius = CornerRadius(trackHeight / 2)
            )
            
            // Draw progress
            if (progressWidth > 0) {
                drawRoundRect(
                    color = progressColor,
                    topLeft = Offset(0f, centerY - trackHeight / 2),
                    size = androidx.compose.ui.geometry.Size(progressWidth, trackHeight),
                    cornerRadius = CornerRadius(trackHeight / 2)
                )
            }
            
            // Draw thumb
            if (progressWidth > 0) {
                drawCircle(
                    color = progressColor,
                    radius = thumbSize.toPx() / 2,
                    center = Offset(progressWidth, centerY)
                )
            }
        }
    } else {
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = modifier
                .fillMaxWidth()
                .height(height),
            color = progressColor,
            trackColor = trackColor
        )
    }
}

/**
 * Wavy animated progress bar - playful and musical
 */
@Composable
private fun WavyProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    progressColor: Color,
    trackColor: Color,
    height: Dp,
    isPlaying: Boolean,
    waveFrequency: Float = 4f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wavyProgress")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isPlaying) 2 * PI.toFloat() else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveOffset"
    )
    
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height.coerceAtLeast(8.dp))
    ) {
        val width = size.width
        val centerY = size.height / 2
        val progressWidth = width * progress.coerceIn(0f, 1f)
        val waveAmplitude = (size.height / 3).coerceAtLeast(2f)
        val strokeWidth = (size.height / 2).coerceIn(2f, 6f)
        
        // Draw track
        drawLine(
            color = trackColor,
            start = Offset(0f, centerY),
            end = Offset(width, centerY),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        
        // Draw wavy progress
        if (progressWidth > 0) {
            val path = Path()
            path.moveTo(0f, centerY)
            
            var x = 0f
            val step = 2f
            while (x <= progressWidth) {
                val y = centerY + sin((x / width * waveFrequency * PI) + waveOffset).toFloat() * waveAmplitude
                if (x == 0f) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
                x += step
            }
            
            drawPath(
                path = path,
                color = progressColor,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
    }
}

/**
 * Rounded pill-shaped progress bar
 */
@Composable
private fun RoundedProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    progressColor: Color,
    trackColor: Color,
    height: Dp
) {
    val actualHeight = height.coerceAtLeast(6.dp)
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(actualHeight)
            .clip(RoundedCornerShape(50))
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(actualHeight)
                .clip(RoundedCornerShape(50))
                .background(progressColor)
        )
    }
}

/**
 * Thin elegant progress line - 2dp height
 */
@Composable
private fun ThinProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    progressColor: Color,
    trackColor: Color
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(2.dp)
    ) {
        val width = size.width
        val centerY = size.height / 2
        
        // Track
        drawLine(
            color = trackColor,
            start = Offset(0f, centerY),
            end = Offset(width, centerY),
            strokeWidth = size.height,
            cap = StrokeCap.Round
        )
        
        // Progress
        val progressWidth = width * progress.coerceIn(0f, 1f)
        if (progressWidth > 0) {
            drawLine(
                color = progressColor,
                start = Offset(0f, centerY),
                end = Offset(progressWidth, centerY),
                strokeWidth = size.height,
                cap = StrokeCap.Round
            )
        }
    }
}

/**
 * Thick bold progress bar - 8dp height
 */
@Composable
private fun ThickProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    progressColor: Color,
    trackColor: Color
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(progressColor)
        )
    }
}

/**
 * Gradient colored progress bar
 */
@Composable
private fun GradientProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    trackColor: Color,
    height: Dp
) {
    val gradientColors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary
    )
    
    val actualHeight = height.coerceAtLeast(4.dp)
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(actualHeight)
            .clip(RoundedCornerShape(50))
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(actualHeight)
                .clip(RoundedCornerShape(50))
                .background(
                    brush = Brush.horizontalGradient(gradientColors)
                )
        )
    }
}

/**
 * Segmented progress bar with gaps
 */
@Composable
private fun SegmentedProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    progressColor: Color,
    trackColor: Color,
    height: Dp
) {
    val segments = 20
    val actualHeight = height.coerceAtLeast(4.dp)
    val filledSegments = (progress * segments).toInt()
    
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(actualHeight)
    ) {
        val segmentWidth = (size.width - (segments - 1) * 3.dp.toPx()) / segments
        val cornerRadius = CornerRadius(size.height / 2)
        
        for (i in 0 until segments) {
            val x = i * (segmentWidth + 3.dp.toPx())
            val color = if (i < filledSegments) progressColor else trackColor
            
            drawRoundRect(
                color = color,
                topLeft = Offset(x, 0f),
                size = Size(segmentWidth, size.height),
                cornerRadius = cornerRadius
            )
        }
    }
}

/**
 * Dots progress indicator
 */
@Composable
private fun DotsProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    activeColor: Color,
    inactiveColor: Color
) {
    val dotCount = 12
    val activeDots = (progress * dotCount).toInt()
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until dotCount) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (i < activeDots) activeColor else inactiveColor)
            )
        }
    }
}

/**
 * Compact mini progress bar for MiniPlayer - optimized for small spaces
 */
@Composable
fun MiniProgressBar(
    progress: Float,
    style: String,
    modifier: Modifier = Modifier,
    progressColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
    isPlaying: Boolean = true
) {
    val progressStyle = try {
        ProgressStyle.valueOf(style.uppercase())
    } catch (e: IllegalArgumentException) {
        ProgressStyle.NORMAL
    }
    
    StyledProgressBar(
        progress = progress,
        style = progressStyle,
        modifier = modifier,
        progressColor = progressColor,
        trackColor = trackColor,
        height = when (progressStyle) {
            ProgressStyle.THIN -> 2.dp
            ProgressStyle.THICK -> 6.dp
            ProgressStyle.WAVY -> 8.dp
            ProgressStyle.DOTS -> 6.dp
            ProgressStyle.SEGMENTED -> 4.dp
            else -> 4.dp
        },
        isPlaying = isPlaying,
        animated = true
    )
}

/**
 * Circular styled progress bar that wraps around content (like play/pause button)
 * Supports all progress styles including wavy, segmented, dots, etc.
 */
@Composable
fun CircularStyledProgressBar(
    progress: Float,
    style: ProgressStyle,
    modifier: Modifier = Modifier,
    progressColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
    trackColor: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Dp = 3.dp,
    isPlaying: Boolean = true,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        when (style) {
            ProgressStyle.WAVY -> WavyCircularProgress(
                progress = progress,
                progressColor = progressColor,
                trackColor = trackColor,
                strokeWidth = strokeWidth,
                isPlaying = isPlaying
            )
            ProgressStyle.SEGMENTED -> SegmentedCircularProgress(
                progress = progress,
                progressColor = progressColor,
                trackColor = trackColor,
                strokeWidth = strokeWidth
            )
            ProgressStyle.DOTS -> DottedCircularProgress(
                progress = progress,
                progressColor = progressColor,
                trackColor = trackColor,
                strokeWidth = strokeWidth
            )
            ProgressStyle.GRADIENT -> GradientCircularProgress(
                progress = progress,
                progressColor = progressColor,
                trackColor = trackColor,
                strokeWidth = strokeWidth
            )
            else -> {
                // Standard circular progress for NORMAL, THIN, THICK, ROUNDED
                androidx.compose.material3.CircularProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxSize(),
                    color = progressColor,
                    strokeWidth = when (style) {
                        ProgressStyle.THIN -> strokeWidth * 0.6f
                        ProgressStyle.THICK -> strokeWidth * 1.5f
                        else -> strokeWidth
                    },
                    trackColor = trackColor,
                    strokeCap = if (style == ProgressStyle.ROUNDED) StrokeCap.Round else StrokeCap.Butt
                )
            }
        }
        
        content()
    }
}

@Composable
private fun WavyCircularProgress(
    progress: Float,
    progressColor: Color,
    trackColor: Color,
    strokeWidth: Dp,
    isPlaying: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wavyCircular")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isPlaying) 2 * PI.toFloat() else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveOffset"
    )
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val radius = (size.minDimension / 2) - strokeWidth.toPx()
        val center = Offset(size.width / 2, size.height / 2)
        
        // Draw track
        drawCircle(
            color = trackColor,
            radius = radius,
            center = center,
            style = Stroke(width = strokeWidth.toPx())
        )
        
        // Draw wavy progress
        if (progress > 0f) {
            val path = Path()
            val sweepAngle = 360f * progress
            val steps = 200
            
            for (i in 0..steps) {
                val angle = (i.toFloat() / steps) * sweepAngle
                if (angle > sweepAngle) break
                
                val angleRad = Math.toRadians((angle - 90).toDouble())
                val wave = sin((angle / 360f * 8 * PI) + waveOffset) * strokeWidth.toPx() * 0.3f
                val currentRadius = radius + wave
                
                val x = center.x + (currentRadius * kotlin.math.cos(angleRad)).toFloat()
                val y = center.y + (currentRadius * kotlin.math.sin(angleRad)).toFloat()
                
                if (i == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }
            
            drawPath(
                path = path,
                color = progressColor,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
private fun SegmentedCircularProgress(
    progress: Float,
    progressColor: Color,
    trackColor: Color,
    strokeWidth: Dp
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val radius = (size.minDimension / 2) - strokeWidth.toPx()
        val center = Offset(size.width / 2, size.height / 2)
        val segments = 20
        val segmentAngle = 360f / segments
        val gapAngle = 4f
        
        for (i in 0 until segments) {
            val startAngle = i * segmentAngle - 90f
            val segmentProgress = ((progress * segments) - i).coerceIn(0f, 1f)
            
            drawArc(
                color = if (segmentProgress > 0) progressColor else trackColor,
                startAngle = startAngle + gapAngle / 2,
                sweepAngle = (segmentAngle - gapAngle) * segmentProgress,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round),
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2)
            )
        }
    }
}

@Composable
private fun DottedCircularProgress(
    progress: Float,
    progressColor: Color,
    trackColor: Color,
    strokeWidth: Dp
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val radius = (size.minDimension / 2) - strokeWidth.toPx()
        val center = Offset(size.width / 2, size.height / 2)
        val dots = 24
        val dotRadius = strokeWidth.toPx() * 0.8f
        
        for (i in 0 until dots) {
            val angle = (i.toFloat() / dots) * 360f - 90f
            val angleRad = Math.toRadians(angle.toDouble())
            val dotProgress = ((progress * dots) - i).coerceIn(0f, 1f)
            
            val x = center.x + (radius * kotlin.math.cos(angleRad)).toFloat()
            val y = center.y + (radius * kotlin.math.sin(angleRad)).toFloat()
            
            drawCircle(
                color = if (dotProgress > 0) progressColor else trackColor,
                radius = dotRadius * (0.5f + dotProgress * 0.5f),
                center = Offset(x, y)
            )
        }
    }
}

@Composable
private fun GradientCircularProgress(
    progress: Float,
    progressColor: Color,
    trackColor: Color,
    strokeWidth: Dp
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val radius = (size.minDimension / 2) - strokeWidth.toPx()
        val center = Offset(size.width / 2, size.height / 2)
        
        // Draw track
        drawCircle(
            color = trackColor,
            radius = radius,
            center = center,
            style = Stroke(width = strokeWidth.toPx())
        )
        
        // Draw gradient progress
        if (progress > 0f) {
            val sweepAngle = 360f * progress
            
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        progressColor,
                        progressColor.copy(alpha = 0.7f),
                        progressColor.copy(alpha = 0.9f),
                        progressColor
                    ),
                    center = center
                ),
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round),
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2)
            )
        }
    }
}
