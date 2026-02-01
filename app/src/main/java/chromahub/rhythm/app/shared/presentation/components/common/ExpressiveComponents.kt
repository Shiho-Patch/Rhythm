@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package chromahub.rhythm.app.shared.presentation.components.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonShapes
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFloatingActionButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import chromahub.rhythm.app.shared.presentation.components.icons.RhythmIcons
import kotlinx.coroutines.delay

// ============================================================================
// EXPRESSIVE SHAPES
// ============================================================================

/**
 * Expressive shape tokens for Material 3 Expressive design
 * Uses organic, rounded shapes inspired by M3 Expressive guidelines
 */
object ExpressiveShapes {
    // Fully rounded pill shapes for buttons and FABs
    val Full = CircleShape
    val ExtraLarge = RoundedCornerShape(28.dp)
    val Large = RoundedCornerShape(24.dp)
    val Medium = RoundedCornerShape(16.dp)
    val Small = RoundedCornerShape(12.dp)
    val ExtraSmall = RoundedCornerShape(8.dp)
    
    // Squircle-inspired shapes (rounded with flatter curves)
    val SquircleExtraLarge = RoundedCornerShape(32.dp)
    val SquircleLarge = RoundedCornerShape(28.dp)
    val SquircleMedium = RoundedCornerShape(20.dp)
    val SquircleSmall = RoundedCornerShape(14.dp)
}

/**
 * Expressive elevation values
 */
object ExpressiveElevation {
    val Level0 = 0.dp
    val Level1 = 1.dp
    val Level2 = 3.dp
    val Level3 = 6.dp
    val Level4 = 8.dp
    val Level5 = 12.dp
    
    // Pressed state elevations
    val PressedButton = 2.dp
    val HoveredButton = 4.dp
}

// ============================================================================
// EXPRESSIVE BUTTONS
// ============================================================================

/**
 * Expressive filled button with bouncy animation and pill shape
 * Perfect for primary actions like "Play All", "Save", "Continue"
 */
@Composable
fun ExpressiveFilledButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ExpressiveShapes.Full,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit
) {
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "button_scale"
    )
    
    Button(
        onClick = onClick,
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        enabled = enabled,
        shape = shape,
        colors = colors,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content
    )
}

/**
 * Expressive filled tonal button for secondary actions
 */
@Composable
fun ExpressiveFilledTonalButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ExpressiveShapes.Full,
    colors: ButtonColors = ButtonDefaults.filledTonalButtonColors(),
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit
) {
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "button_scale"
    )
    
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        enabled = enabled,
        shape = shape,
        colors = colors,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content
    )
}

/**
 * Expressive outlined button for tertiary actions
 */
@Composable
fun ExpressiveOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ExpressiveShapes.Full,
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors(),
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit
) {
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "button_scale"
    )
    
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        enabled = enabled,
        shape = shape,
        colors = colors,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content
    )
}

/**
 * Expressive text button for low-emphasis actions
 */
@Composable
fun ExpressiveTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ExpressiveShapes.Full,
    colors: ButtonColors = ButtonDefaults.textButtonColors(),
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit
) {
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "button_scale"
    )
    
    TextButton(
        onClick = onClick,
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        enabled = enabled,
        shape = shape,
        colors = colors,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content
    )
}

// ============================================================================
// EXPRESSIVE ICON BUTTONS
// ============================================================================

/**
 * Expressive filled icon button with bouncy press animation
 * Perfect for player controls, action buttons
 */
@Composable
fun ExpressiveFilledIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ExpressiveShapes.Full,
    colors: IconButtonColors = IconButtonDefaults.filledIconButtonColors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit
) {
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "icon_button_scale"
    )
    
    FilledIconButton(
        onClick = onClick,
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        enabled = enabled,
        shape = shape,
        colors = colors,
        interactionSource = interactionSource,
        content = content
    )
}

/**
 * Expressive filled tonal icon button
 */
@Composable
fun ExpressiveFilledTonalIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ExpressiveShapes.Full,
    colors: IconButtonColors = IconButtonDefaults.filledTonalIconButtonColors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit
) {
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "icon_button_scale"
    )
    
    FilledTonalIconButton(
        onClick = onClick,
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        enabled = enabled,
        shape = shape,
        colors = colors,
        interactionSource = interactionSource,
        content = content
    )
}

/**
 * Expressive standard icon button
 */
@Composable
fun ExpressiveIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit
) {
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "icon_button_scale"
    )
    
    IconButton(
        onClick = onClick,
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        enabled = enabled,
        colors = colors,
        interactionSource = interactionSource,
        content = content
    )
}

/**
 * Large expressive icon button for primary player controls
 * Uses larger hit target and more prominent visual feedback
 */
@Composable
fun ExpressiveLargeIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    size: Dp = 72.dp,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit
) {
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "large_icon_button_scale"
    )
    
    val elevation by animateDpAsState(
        targetValue = if (isPressed) ExpressiveElevation.Level2 else ExpressiveElevation.Level3,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "large_icon_button_elevation"
    )
    
    Surface(
        onClick = onClick,
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        enabled = enabled,
        shape = ExpressiveShapes.Full,
        color = containerColor,
        contentColor = contentColor,
        tonalElevation = elevation,
        shadowElevation = elevation,
        interactionSource = interactionSource
    ) {
        Box(contentAlignment = Alignment.Center) {
            content()
        }
    }
}

// ============================================================================
// EXPRESSIVE FLOATING ACTION BUTTONS
// ============================================================================

/**
 * Expressive small FAB with bouncy animation
 */
@Composable
fun ExpressiveSmallFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = ExpressiveShapes.Large,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit
) {
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "small_fab_scale"
    )
    
    SmallFloatingActionButton(
        onClick = onClick,
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        shape = shape,
        containerColor = containerColor,
        contentColor = contentColor,
        interactionSource = interactionSource,
        content = content
    )
}

/**
 * Expressive medium FAB - standard size with bouncy animation
 */
@Composable
fun ExpressiveMediumFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = ExpressiveShapes.Large,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit
) {
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "medium_fab_scale"
    )
    
    MediumFloatingActionButton(
        onClick = onClick,
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        shape = shape,
        containerColor = containerColor,
        contentColor = contentColor,
        interactionSource = interactionSource,
        content = content
    )
}

/**
 * Expressive large FAB for primary actions
 */
@Composable
fun ExpressiveLargeFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = ExpressiveShapes.ExtraLarge,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit
) {
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "large_fab_scale"
    )
    
    LargeFloatingActionButton(
        onClick = onClick,
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        shape = shape,
        containerColor = containerColor,
        contentColor = contentColor,
        interactionSource = interactionSource,
        content = content
    )
}

/**
 * Expressive extended FAB with text label
 */
@Composable
fun ExpressiveExtendedFAB(
    onClick: () -> Unit,
    text: @Composable () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    expanded: Boolean = true,
    shape: Shape = ExpressiveShapes.Full,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "extended_fab_scale"
    )
    
    androidx.compose.material3.ExtendedFloatingActionButton(
        onClick = onClick,
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        expanded = expanded,
        icon = icon,
        text = text,
        shape = shape,
        containerColor = containerColor,
        contentColor = contentColor,
        interactionSource = interactionSource
    )
}

// ============================================================================
// EXPRESSIVE CARDS
// ============================================================================

/**
 * Expressive card with organic shape and subtle elevation animation
 */
@Composable
fun ExpressiveCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: Shape = ExpressiveShapes.Large,
    colors: CardColors = CardDefaults.cardColors(),
    content: @Composable () -> Unit
) {
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            colors = colors
        ) {
            content()
        }
    } else {
        Card(
            modifier = modifier,
            shape = shape,
            colors = colors
        ) {
            content()
        }
    }
}

/**
 * Expressive elevated card with shadow and depth
 */
@Composable
fun ExpressiveElevatedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: Shape = ExpressiveShapes.Large,
    colors: CardColors = CardDefaults.elevatedCardColors(),
    content: @Composable () -> Unit
) {
    if (onClick != null) {
        ElevatedCard(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            colors = colors
        ) {
            content()
        }
    } else {
        ElevatedCard(
            modifier = modifier,
            shape = shape,
            colors = colors
        ) {
            content()
        }
    }
}

/**
 * Expressive outlined card
 */
@Composable
fun ExpressiveOutlinedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: Shape = ExpressiveShapes.Large,
    colors: CardColors = CardDefaults.outlinedCardColors(),
    content: @Composable () -> Unit
) {
    if (onClick != null) {
        OutlinedCard(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            colors = colors
        ) {
            content()
        }
    } else {
        OutlinedCard(
            modifier = modifier,
            shape = shape,
            colors = colors
        ) {
            content()
        }
    }
}

// ============================================================================
// EXPRESSIVE CHIPS
// ============================================================================

/**
 * Expressive filter chip with pill shape and bouncy animation
 */
@Composable
fun ExpressiveFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    shape: Shape = ExpressiveShapes.Full,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "filter_chip_scale"
    )
    
    androidx.compose.material3.FilterChip(
        selected = selected,
        onClick = onClick,
        label = label,
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        enabled = enabled,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        shape = shape,
        interactionSource = interactionSource
    )
}

/**
 * Expressive assist chip
 */
@Composable
fun ExpressiveAssistChip(
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    shape: Shape = ExpressiveShapes.Full,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "assist_chip_scale"
    )
    
    androidx.compose.material3.AssistChip(
        onClick = onClick,
        label = label,
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        enabled = enabled,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        shape = shape,
        interactionSource = interactionSource
    )
}

/**
 * Expressive input chip
 */
@Composable
fun ExpressiveInputChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    avatar: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    shape: Shape = ExpressiveShapes.Full,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "input_chip_scale"
    )
    
    androidx.compose.material3.InputChip(
        selected = selected,
        onClick = onClick,
        label = label,
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        enabled = enabled,
        leadingIcon = leadingIcon,
        avatar = avatar,
        trailingIcon = trailingIcon,
        shape = shape,
        interactionSource = interactionSource
    )
}

/**
 * Expressive suggestion chip
 */
@Composable
fun ExpressiveSuggestionChip(
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: @Composable (() -> Unit)? = null,
    shape: Shape = ExpressiveShapes.Full,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "suggestion_chip_scale"
    )
    
    androidx.compose.material3.SuggestionChip(
        onClick = onClick,
        label = label,
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        enabled = enabled,
        icon = icon,
        shape = shape,
        interactionSource = interactionSource
    )
}

// ============================================================================
// EXPRESSIVE SURFACES
// ============================================================================

/**
 * Expressive surface with organic shape
 */
@Composable
fun ExpressiveSurface(
    modifier: Modifier = Modifier,
    shape: Shape = ExpressiveShapes.Large,
    color: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    tonalElevation: Dp = 0.dp,
    shadowElevation: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = color,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
        content = content
    )
}

/**
 * Expressive clickable surface
 */
@Composable
fun ExpressiveClickableSurface(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ExpressiveShapes.Large,
    color: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    tonalElevation: Dp = 0.dp,
    shadowElevation: Dp = 0.dp,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit
) {
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "surface_scale"
    )
    
    Surface(
        onClick = onClick,
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        enabled = enabled,
        shape = shape,
        color = color,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
        interactionSource = interactionSource,
        content = content
    )
}

// ============================================================================
// CONVENIENCE COMPOSABLES
// ============================================================================

/**
 * Expressive button with icon and text
 */
@Composable
fun ExpressiveButtonWithIcon(
    onClick: () -> Unit,
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    iconOnStart: Boolean = true
) {
    ExpressiveFilledButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled
    ) {
        if (iconOnStart) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(text)
        if (!iconOnStart) {
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Expressive tonal button with icon and text
 */
@Composable
fun ExpressiveTonalButtonWithIcon(
    onClick: () -> Unit,
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    iconOnStart: Boolean = true
) {
    ExpressiveFilledTonalButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled
    ) {
        if (iconOnStart) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(text)
        if (!iconOnStart) {
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Play button - primary action for music playback
 */
@Composable
fun ExpressivePlayButton(
    onClick: () -> Unit,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp
) {
    val playIcon = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow
    
    ExpressiveLargeIconButton(
        onClick = onClick,
        modifier = modifier,
        size = size,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Icon(
            imageVector = playIcon,
            contentDescription = if (isPlaying) "Pause" else "Play",
            modifier = Modifier.size(size * 0.5f)
        )
    }
}

/**
 * Action row with multiple expressive buttons
 */
@Composable
fun ExpressiveActionRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(12.dp),
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

// ============================================================================
// EXPRESSIVE BUTTON GROUP
// ============================================================================

/**
 * Material 3 Expressive Button Group for connected button actions
 * Perfect for Play/Shuffle, Sort/Filter, etc.
 * 
 * Creates visually grouped buttons with continuous background and minimal spacing
 */
@Composable
fun ExpressiveButtonGroup(
    modifier: Modifier = Modifier,
    style: ButtonGroupStyle = ButtonGroupStyle.Tonal,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

/**
 * Button Group Style
 */
enum class ButtonGroupStyle {
    Filled,
    Tonal,
    Outlined
}

/**
 * Individual button within an ExpressiveButtonGroup
 * Automatically handles start/end/middle positioning
 */
@Composable
fun ExpressiveGroupButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isStart: Boolean = false,
    isEnd: Boolean = false,
    content: @Composable RowScope.() -> Unit
) {
    val shape = when {
        isStart && isEnd -> ExpressiveShapes.Full // Single button
        isStart -> RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp, topEnd = 8.dp, bottomEnd = 8.dp)
        isEnd -> RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp, topEnd = 20.dp, bottomEnd = 20.dp)
        else -> RoundedCornerShape(8.dp) // Middle button
    }
    
    ExpressiveFilledTonalButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        content = content
    )
}

// ============================================================================
// EXPRESSIVE SETTINGS GROUP
// ============================================================================

/**
 * Material 3 Expressive Settings Group
 * Replaces individual settings with dividers by using a unified card container
 * with subtle spacing between items
 */
@Composable
fun ExpressiveSettingsGroup(
    modifier: Modifier = Modifier,
    title: String? = null,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        title?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )
        }
        
        ExpressiveCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            shape = ExpressiveShapes.Large
        ) {
            Column(
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                content()
            }
        }
    }
}

/**
 * Individual setting item within an ExpressiveSettingsGroup
 * No dividers - spacing handled by padding
 */
@Composable
fun ExpressiveSettingItem(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        content()
    }
}

// ============================================================================
// EXPRESSIVE PLAYER CONTROLS
// ============================================================================

/**
 * Expressive Player Control Button Group
 * Groups playback controls (Prev, SeekBack, Play/Pause, SeekForward, Next) 
 * with unified background and native expressive grouping for spacing.
 */
@Composable
fun ExpressivePlayerControlGroup(
    isPlaying: Boolean,
    showSeekButtons: Boolean,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onSeekBack: () -> Unit,
    onSeekForward: () -> Unit,
    modifier: Modifier = Modifier,
    isExtraSmallWidth: Boolean = false,
    isCompactWidth: Boolean = false,
    isLoading: Boolean = false
) {
    // Container height and padding
    val containerHeight = when {
        isExtraSmallWidth -> 64.dp
        isCompactWidth -> 72.dp
        else -> 80.dp
    }
    
    val containerPadding = when {
        isExtraSmallWidth -> 12.dp
        isCompactWidth -> 16.dp
        else -> 16.dp
    }
    
    // Button sizes
    val prevNextSize = when {
        isExtraSmallWidth -> 42.dp
        isCompactWidth -> 46.dp
        else -> 50.dp
    }
    
    val seekButtonSize = when {
        isExtraSmallWidth -> 48.dp
        isCompactWidth -> 54.dp
        else -> 60.dp
    }
    
    // Spacing using native expressive pattern
    val buttonSpacing = when {
        isExtraSmallWidth -> 6.dp
        isCompactWidth -> 8.dp
        else -> 8.dp
    }
    
    // Unified background surface
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(
            when {
                isExtraSmallWidth -> 32.dp
                isCompactWidth -> 36.dp
                else -> 40.dp
            }
        ),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(containerHeight)
                .padding(horizontal = containerPadding, vertical = containerPadding),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous button
            FilledIconButton(
                onClick = onPrevious,
                modifier = Modifier.size(prevNextSize),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary
                ),
                shapes = IconButtonDefaults.shapes()
            ) {
                Icon(
                    imageVector = RhythmIcons.SkipPrevious,
                    contentDescription = "Previous",
                    modifier = Modifier.size(prevNextSize * 0.5f)
                )
            }
            
            // Seek back button - always visible if enabled
            if (showSeekButtons) {
                FilledTonalIconButton(
                    onClick = onSeekBack,
                    modifier = Modifier.size(seekButtonSize),
                    shapes = IconButtonDefaults.shapes()
                ) {
                    Icon(
                        imageVector = RhythmIcons.Replay10,
                        contentDescription = "Seek back",
                        modifier = Modifier.size(seekButtonSize * 0.5f)
                    )
                }
            }
            
            // Play/Pause button - center
            ExpressiveMorphingPlayPauseButton(
                isPlaying = isPlaying,
                onClick = onPlayPause,
                isExtraSmallWidth = isExtraSmallWidth,
                isCompactWidth = isCompactWidth,
                isLoading = isLoading,
                showSeekButtons = showSeekButtons
            )
            
            // Seek forward button - always visible if enabled
            if (showSeekButtons) {
                FilledTonalIconButton(
                    onClick = onSeekForward,
                    modifier = Modifier.size(seekButtonSize),
                    shapes = IconButtonDefaults.shapes()
                ) {
                    Icon(
                        imageVector = RhythmIcons.Forward10,
                        contentDescription = "Seek forward",
                        modifier = Modifier.size(seekButtonSize * 0.5f)
                    )
                }
            }
            
            // Next button
            FilledIconButton(
                onClick = onNext,
                modifier = Modifier.size(prevNextSize),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary
                ),
                shapes = IconButtonDefaults.shapes()
            ) {
                Icon(
                    imageVector = RhythmIcons.SkipNext,
                    contentDescription = "Next",
                    modifier = Modifier.size(prevNextSize * 0.5f)
                )
            }
        }
    }
}

/**
 * Morphing Play/Pause button that expands to pill when seek buttons disabled
 */
@Composable
private fun ExpressiveMorphingPlayPauseButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    isExtraSmallWidth: Boolean,
    isCompactWidth: Boolean,
    isLoading: Boolean = false,
    showSeekButtons: Boolean = true,
    modifier: Modifier = Modifier
) {
    // Show PAUSE text after 2 seconds when paused
    var showPauseText by remember { mutableStateOf(false) }
    
    LaunchedEffect(isPlaying) {
        if (!isPlaying && !showSeekButtons) {
            delay(2000)
            showPauseText = true
        } else {
            showPauseText = false
        }
    }
    
    val width by animateDpAsState(
        targetValue = when {
            !showSeekButtons -> when {
                isExtraSmallWidth -> if (showPauseText) 120.dp else if (isPlaying) 110.dp else 120.dp
                isCompactWidth -> if (showPauseText) 140.dp else 130.dp
                else -> if (showPauseText) 150.dp else 140.dp
            }
            else -> when {
                isExtraSmallWidth -> 48.dp
                isCompactWidth -> 54.dp
                else -> 60.dp
            }
        },
        label = "playPauseWidth"
    )
    
    val height = when {
        isExtraSmallWidth -> 48.dp
        isCompactWidth -> 54.dp
        else -> 60.dp
    }
    
    if (!showSeekButtons && !isLoading) {
        // Pill button with text when seek buttons disabled
        Button(
            onClick = onClick,
            modifier = modifier
                .width(width)
                .height(height),
            shapes = ButtonDefaults.shapes(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            contentPadding = PaddingValues(horizontal = 12.dp)
        ) {
            Icon(
                imageVector = if (isPlaying) RhythmIcons.Pause else RhythmIcons.Play,
                contentDescription = if (isPlaying) "Pause" else "Play",
                modifier = Modifier.size(if (isExtraSmallWidth) 18.dp else 24.dp)
            )
            AnimatedVisibility(
                visible = !isPlaying || showPauseText,
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally()
            ) {
                Text(
                    text = if (isPlaying) "PAUSE" else "PLAY",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    } else {
        // Icon button when seek buttons enabled or loading
        FilledIconButton(
            onClick = onClick,
            modifier = modifier.size(height),
            shapes = IconButtonDefaults.shapes(),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            // When loading, show ONLY the loader, hide everything else
            if (isLoading) {
                val rotation by animateFloatAsState(
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1200, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "rotation"
                )
                
                Box(
                    modifier = Modifier
                        .size(if (isExtraSmallWidth) 20.dp else 24.dp)
                        .graphicsLayer { rotationZ = rotation }
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                }
            } else {
                Icon(
                    imageVector = if (isPlaying) RhythmIcons.Pause else RhythmIcons.Play,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(if (isExtraSmallWidth) 20.dp else 24.dp)
                )
            }
        }
    }
}

/**
 * Expressive Toggle Button Group for secondary actions
 * Groups Shuffle, Lyrics, and Repeat buttons with unified styling
 * and morphing pill animations
 */
@Composable
fun ExpressiveToggleButtonGroup(
    shuffleEnabled: Boolean,
    lyricsVisible: Boolean,
    repeatMode: Int,
    onToggleShuffle: () -> Unit,
    onToggleLyrics: () -> Unit,
    onToggleRepeat: () -> Unit,
    showLyrics: Boolean,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = false
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Shuffle toggle button
        ExpressiveMorphingToggleButton(
            isActive = shuffleEnabled,
            onClick = onToggleShuffle,
            icon = RhythmIcons.Shuffle,
            label = "Shuffle",
            isDarkTheme = isDarkTheme
        )
        
        // Lyrics toggle button (only if lyrics are enabled)
        if (showLyrics) {
            ExpressiveMorphingToggleButton(
                isActive = lyricsVisible,
                onClick = onToggleLyrics,
                icon = RhythmIcons.Player.Lyrics,
                label = "Lyrics",
                isDarkTheme = isDarkTheme
            )
        }
        
        // Repeat toggle button
        ExpressiveMorphingToggleButton(
            isActive = repeatMode != 0,
            onClick = onToggleRepeat,
            icon = when (repeatMode) {
                1 -> RhythmIcons.RepeatOne
                2 -> RhythmIcons.Repeat
                else -> RhythmIcons.Repeat
            },
            label = "Repeat",
            isDarkTheme = isDarkTheme
        )
    }
}

/**
 * Morphing toggle button that expands when inactive to show label
 */
@Composable
private fun ExpressiveMorphingToggleButton(
    isActive: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val containerColor by animateColorAsState(
        targetValue = if (isActive) {
            if (isDarkTheme) MaterialTheme.colorScheme.inverseSurface 
            else MaterialTheme.colorScheme.surfaceContainerLowest
        } else {
            MaterialTheme.colorScheme.surfaceContainerHighest
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "toggleButtonColor"
    )
    
    val contentColor by animateColorAsState(
        targetValue = if (isActive) {
            if (isDarkTheme) MaterialTheme.colorScheme.inverseOnSurface 
            else MaterialTheme.colorScheme.onSurface
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "toggleContentColor"
    )
    
    val width by animateDpAsState(
        targetValue = if (isActive) 48.dp else 100.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "toggleButtonWidth"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "toggleButtonScale"
    )
    
    Surface(
        onClick = onClick,
        modifier = modifier
            .width(width)
            .height(48.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(24.dp),
        color = containerColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        interactionSource = interactionSource
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(20.dp),
                tint = contentColor
            )
            
            AnimatedVisibility(
                visible = !isActive,
                enter = fadeIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + expandHorizontally(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ),
                exit = fadeOut(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessHigh
                    )
                ) + shrinkHorizontally(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessHigh
                    )
                )
            ) {
                Row {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        ),
                        color = contentColor
                    )
                }
            }
        }
    }
}

// ============================================================================
// ADDITIONAL EXPRESSIVE ENHANCEMENTS
// ============================================================================

/**
 * Expressive elevated player control group with shadow and depth
 * More prominent visual hierarchy for featured player sections
 */
@Composable
fun ExpressiveElevatedPlayerControlGroup(
    isPlaying: Boolean,
    showSeekButtons: Boolean,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onSeekBack: () -> Unit,
    onSeekForward: () -> Unit,
    modifier: Modifier = Modifier,
    isExtraSmallWidth: Boolean = false,
    isCompactWidth: Boolean = false
) {
    // Animated elevation based on playing state
    val elevation by animateDpAsState(
        targetValue = if (isPlaying) 8.dp else 4.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "controlGroupElevation"
    )
    
    val groupWidth by animateDpAsState(
        targetValue = when {
            !isPlaying && !showSeekButtons -> 220.dp
            !isPlaying && showSeekButtons -> 300.dp
            isPlaying && !showSeekButtons -> 200.dp
            else -> 340.dp
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "elevatedControlGroupWidth"
    )
    
    Surface(
        modifier = modifier
            .width(groupWidth)
            .height(when {
                isExtraSmallWidth -> 64.dp
                isCompactWidth -> 72.dp
                else -> 80.dp
            }),
        shape = RoundedCornerShape(40.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = elevation,
        shadowElevation = elevation
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalIconButton(
                onClick = onPrevious,
                modifier = Modifier.size(when {
                    isExtraSmallWidth -> 44.dp
                    isCompactWidth -> 48.dp
                    else -> 52.dp
                }),
                shapes = IconButtonDefaults.shapes()
            ) {
                Icon(
                    imageVector = RhythmIcons.SkipPrevious,
                    contentDescription = "Previous",
                    modifier = Modifier.size(when {
                        isExtraSmallWidth -> 22.dp
                        isCompactWidth -> 24.dp
                        else -> 26.dp
                    })
                )
            }
            
            AnimatedVisibility(
                visible = isPlaying && showSeekButtons,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                FilledTonalIconButton(
                    onClick = onSeekBack,
                    modifier = Modifier.size(when {
                        isExtraSmallWidth -> 48.dp
                        isCompactWidth -> 52.dp
                        else -> 60.dp
                    }),
                    shapes = IconButtonDefaults.shapes()
                ) {
                    Icon(
                        imageVector = RhythmIcons.Replay10,
                        contentDescription = "Seek back",
                        modifier = Modifier.size(when {
                            isExtraSmallWidth -> 24.dp
                            isCompactWidth -> 26.dp
                            else -> 30.dp
                        })
                    )
                }
            }
            
            ExpressiveMorphingPlayPauseButton(
                isPlaying = isPlaying,
                onClick = onPlayPause,
                isExtraSmallWidth = isExtraSmallWidth,
                isCompactWidth = isCompactWidth
            )
            
            AnimatedVisibility(
                visible = isPlaying && showSeekButtons,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                FilledTonalIconButton(
                    onClick = onSeekForward,
                    modifier = Modifier.size(when {
                        isExtraSmallWidth -> 48.dp
                        isCompactWidth -> 52.dp
                        else -> 60.dp
                    }),
                    shapes = IconButtonDefaults.shapes()
                ) {
                    Icon(
                        imageVector = RhythmIcons.Forward10,
                        contentDescription = "Seek forward",
                        modifier = Modifier.size(when {
                            isExtraSmallWidth -> 24.dp
                            isCompactWidth -> 26.dp
                            else -> 30.dp
                        })
                    )
                }
            }
            
            FilledTonalIconButton(
                onClick = onNext,
                modifier = Modifier.size(when {
                    isExtraSmallWidth -> 44.dp
                    isCompactWidth -> 48.dp
                    else -> 52.dp
                }),
                shapes = IconButtonDefaults.shapes()
            ) {
                Icon(
                    imageVector = RhythmIcons.SkipNext,
                    contentDescription = "Next",
                    modifier = Modifier.size(when {
                        isExtraSmallWidth -> 22.dp
                        isCompactWidth -> 24.dp
                        else -> 26.dp
                    })
                )
            }
        }
    }
}

/**
 * Expressive compact player controls for mini player or compact layouts
 * Minimalist design with essential controls only
 */
@Composable
fun ExpressiveCompactPlayerControls(
    isPlaying: Boolean,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ExpressiveFilledTonalIconButton(
            onClick = onPrevious,
            shape = CircleShape
        ) {
            Icon(
                imageVector = RhythmIcons.SkipPrevious,
                contentDescription = "Previous",
                modifier = Modifier.size(20.dp)
            )
        }
        
        ExpressiveFilledIconButton(
            onClick = onPlayPause,
            shape = CircleShape
        ) {
            Icon(
                imageVector = if (isPlaying) RhythmIcons.Pause else RhythmIcons.Play,
                contentDescription = if (isPlaying) "Pause" else "Play",
                modifier = Modifier.size(24.dp)
            )
        }
        
        ExpressiveFilledTonalIconButton(
            onClick = onNext,
            shape = CircleShape
        ) {
            Icon(
                imageVector = RhythmIcons.SkipNext,
                contentDescription = "Next",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Expressive progress indicator with animated wave
 * Shows visual feedback during buffering or loading
 */
@Composable
fun ExpressiveLoadingPlayButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 60.dp
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(1000),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "loadingPulse"
    )
    
    Surface(
        onClick = onClick,
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 4.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            androidx.compose.material3.CircularProgressIndicator(
                modifier = Modifier.size(size * 0.7f),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                strokeWidth = 3.dp
            )
        }
    }
}
