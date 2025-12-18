package chromahub.rhythm.app.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.rounded.Cast
import androidx.compose.material.icons.rounded.CastConnected
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import chromahub.rhythm.app.data.AppSettings
import chromahub.rhythm.app.util.HapticUtils
import chromahub.rhythm.app.util.PlayerStateTransfer
import chromahub.rhythm.app.viewmodel.MusicViewModel
import kotlinx.coroutines.delay

/**
 * Data class representing a Cast device
 */
data class CastDevice(
    val id: String,
    val name: String,
    val deviceType: CastDeviceType,
    val isConnected: Boolean = false
)

enum class CastDeviceType {
    TV, SPEAKER, SOUNDBAR, PHONE, UNKNOWN
}

/**
 * Cast Bottom Sheet for managing cast/streaming devices
 * Uses PlayerStateTransfer for seamless playback transfer
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CastBottomSheet(
    musicViewModel: MusicViewModel,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    
    // Animation states
    var showContent by remember { mutableStateOf(false) }
    var isScanning by remember { mutableStateOf(false) }
    var connectedDevice by remember { mutableStateOf<CastDevice?>(null) }
    
    // Mock available devices - in real implementation, this would come from Cast SDK
    var availableDevices by remember { mutableStateOf<List<CastDevice>>(emptyList()) }
    
    val contentAlpha by animateFloatAsState(
        targetValue = if (showContent) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "contentAlpha"
    )
    
    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
    }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { 
            BottomSheetDefaults.DragHandle(
                color = MaterialTheme.colorScheme.primary
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onBackground,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            // Header
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it }
            ) {
                CastHeader(
                    isConnected = connectedDevice != null,
                    connectedDeviceName = connectedDevice?.name,
                    haptics = haptics
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Content
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Connected Device Card
                if (connectedDevice != null) {
                    item {
                        ConnectedDeviceCard(
                            device = connectedDevice!!,
                            onDisconnect = {
                                HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                                connectedDevice = null
                                Toast.makeText(context, "Disconnected from ${connectedDevice?.name}", Toast.LENGTH_SHORT).show()
                            },
                            haptics = haptics
                        )
                    }
                }
                
                // Scan for Devices Card
                item {
                    ScanDevicesCard(
                        isScanning = isScanning,
                        onScan = {
                            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                            isScanning = true
                            // Simulate scanning
                            coroutineScope.launch {
                                delay(2000)
                                availableDevices = listOf(
                                    CastDevice("1", "Living Room TV", CastDeviceType.TV),
                                    CastDevice("2", "Kitchen Speaker", CastDeviceType.SPEAKER),
                                    CastDevice("3", "Bedroom Soundbar", CastDeviceType.SOUNDBAR)
                                )
                                isScanning = false
                            }
                        },
                        haptics = haptics
                    )
                }
                
                // Available Devices
                if (availableDevices.isNotEmpty()) {
                    item {
                        Text(
                            text = "Available Devices",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    
                    items(availableDevices) { device ->
                        DeviceCard(
                            device = device,
                            onClick = {
                                HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                                // Use PlayerStateTransfer logic here
                                connectedDevice = device.copy(isConnected = true)
                                availableDevices = emptyList()
                                Toast.makeText(context, "Connected to ${device.name}", Toast.LENGTH_SHORT).show()
                            },
                            haptics = haptics
                        )
                    }
                }
                
                // Info Card
                item {
                    CastInfoCard()
                }
                
                // Bottom spacing
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun CastHeader(
    isConnected: Boolean,
    connectedDeviceName: String?,
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "castPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Cast icon with glow effect when connected
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (isConnected) {
                                Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = pulseAlpha * 0.3f),
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    )
                                )
                            } else {
                                Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.surfaceContainerHigh,
                                        MaterialTheme.colorScheme.surfaceContainerHigh
                                    )
                                )
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isConnected) Icons.Rounded.CastConnected else Icons.Rounded.Cast,
                        contentDescription = null,
                        tint = if (isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                Column {
                    Text(
                        text = "Cast & Stream",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text(
                        text = if (isConnected) "Connected to $connectedDeviceName" else "Stream to your devices",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ConnectedDeviceCard(
    device: CastDevice,
    onDisconnect: () -> Unit,
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "connectedPulse")
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "borderAlpha"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = borderAlpha),
                        MaterialTheme.colorScheme.tertiary.copy(alpha = borderAlpha)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Device icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getDeviceIcon(device.deviceType),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                Column {
                    Text(
                        text = device.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color.Green)
                        )
                        Text(
                            text = "Connected",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            FilledTonalButton(
                onClick = onDisconnect,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Disconnect",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Disconnect")
            }
        }
    }
}

@Composable
private fun ScanDevicesCard(
    isScanning: Boolean,
    onScan: () -> Unit,
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback,
    modifier: Modifier = Modifier
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isScanning) 360f else 0f,
        animationSpec = tween(1000),
        label = "rotationAngle"
    )
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isScanning) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 3.dp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Scanning for devices...",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            } else {
                Button(
                    onClick = onScan,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Scan for Devices",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun DeviceCard(
    device: CastDevice,
    onClick: () -> Unit,
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Device icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getDeviceIcon(device.deviceType),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Column {
                    Text(
                        text = device.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text(
                        text = getDeviceTypeName(device.deviceType),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Icon(
                imageVector = Icons.Default.Cast,
                contentDescription = "Connect",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun CastInfoCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(24.dp)
            )
            
            Column {
                Text(
                    text = "About Casting",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Cast your music to compatible devices like TVs, speakers, and soundbars. Playback state is automatically transferred using Media3 PlayerStateTransfer for seamless transitions.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

private fun getDeviceIcon(deviceType: CastDeviceType): ImageVector {
    return when (deviceType) {
        CastDeviceType.TV -> Icons.Default.Tv
        CastDeviceType.SPEAKER -> Icons.Default.Speaker
        CastDeviceType.SOUNDBAR -> Icons.Default.Speaker
        CastDeviceType.PHONE -> Icons.Default.PhoneAndroid
        CastDeviceType.UNKNOWN -> Icons.Default.Cast
    }
}

private fun getDeviceTypeName(deviceType: CastDeviceType): String {
    return when (deviceType) {
        CastDeviceType.TV -> "Television"
        CastDeviceType.SPEAKER -> "Smart Speaker"
        CastDeviceType.SOUNDBAR -> "Soundbar"
        CastDeviceType.PHONE -> "Phone"
        CastDeviceType.UNKNOWN -> "Cast Device"
    }
}
