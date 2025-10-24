package chromahub.rhythm.app.ui.screens.tuner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import chromahub.rhythm.app.ui.components.CollapsibleHeaderScreen
import chromahub.rhythm.app.data.AppSettings
import chromahub.rhythm.app.ui.components.RhythmIcons

@Composable
fun PlaceholderScreen(
    title: String,
    description: String = "This feature is under development.",
    onBackClick: () -> Unit
) {
    CollapsibleHeaderScreen(
        title = title,
        showBackButton = true,
        onBackClick = onBackClick
    ) { modifier ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Coming soon...",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// Individual screens with actual settings
@Composable
fun NotificationsSettingsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val appSettings = AppSettings.getInstance(context)
    val useCustomNotification by appSettings.useCustomNotification.collectAsState()
    
    CollapsibleHeaderScreen(
        title = "Notifications",
        showBackButton = true,
        onBackClick = onBackClick
    ) { modifier ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            
            item {
                TunerSettingCard(
                    title = "Custom Notifications",
                    description = "Use app's custom notification style instead of system media notification",
                    icon = Icons.Default.Notifications,
                    checked = useCustomNotification,
                    onCheckedChange = { appSettings.setUseCustomNotification(it) }
                )
            }
            
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

//@Composable
//fun ThemingSettingsScreen(onBackClick: () -> Unit) {
//    PlaceholderScreen(
//        title = "Theming",
//        description = "Customize app theme, colors, and appearance.",
//        onBackClick = onBackClick
//    )
//}

@Composable
fun StreamingSettingsScreen(onBackClick: () -> Unit) {
    PlaceholderScreen(
        title = "Streaming",
        description = "Configure streaming sources and quality settings.",
        onBackClick = onBackClick
    )
}

@Composable
fun AudioSettingsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val appSettings = AppSettings.getInstance(context)
    val useSystemVolume by appSettings.useSystemVolume.collectAsState()
    val gaplessPlayback by appSettings.gaplessPlayback.collectAsState()
    val crossfade by appSettings.crossfade.collectAsState()
    val audioNormalization by appSettings.audioNormalization.collectAsState()
    val replayGain by appSettings.replayGain.collectAsState()
    
    CollapsibleHeaderScreen(
        title = "Audio",
        showBackButton = true,
        onBackClick = onBackClick
    ) { modifier ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            
            item {
                TunerSettingCard(
                    title = "System Volume Control",
                    description = "Use device volume controls for music playback",
                    icon = RhythmIcons.Player.VolumeUp,
                    checked = useSystemVolume,
                    onCheckedChange = { appSettings.setUseSystemVolume(it) }
                )
            }
            
            item {
                TunerSettingCard(
                    title = "Gapless Playback",
                    description = "Eliminate gaps between tracks for continuous listening",
                    icon = Icons.Default.QueueMusic,
                    checked = gaplessPlayback,
                    onCheckedChange = { appSettings.setGaplessPlayback(it) }
                )
            }
            
            item {
                TunerSettingCard(
                    title = "Crossfade",
                    description = "Smoothly transition between songs",
                    icon = Icons.Default.Shuffle,
                    checked = crossfade,
                    onCheckedChange = { appSettings.setCrossfade(it) }
                )
            }
            
            item {
                TunerSettingCard(
                    title = "Audio Normalization",
                    description = "Adjust volume levels to consistent loudness",
                    icon = Icons.Default.TuneRounded,
                    checked = audioNormalization,
                    onCheckedChange = { appSettings.setAudioNormalization(it) }
                )
            }
            
            item {
                TunerSettingCard(
                    title = "ReplayGain",
                    description = "Apply ReplayGain tags for consistent playback volume",
                    icon = Icons.Default.GraphicEq,
                    checked = replayGain,
                    onCheckedChange = { appSettings.setReplayGain(it) }
                )
            }
            
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

@Composable
fun DownloadsSettingsScreen(onBackClick: () -> Unit) {
    PlaceholderScreen(
        title = "Downloads",
        description = "Manage downloaded music and storage preferences.",
        onBackClick = onBackClick
    )
}

@Composable
fun OfflineModeSettingsScreen(onBackClick: () -> Unit) {
    PlaceholderScreen(
        title = "Offline Mode",
        description = "Configure offline playback and sync settings.",
        onBackClick = onBackClick
    )
}

@Composable
fun PlaylistsSettingsScreen(onBackClick: () -> Unit) {
    PlaceholderScreen(
        title = "Playlists",
        description = "Manage playlist preferences and behavior.",
        onBackClick = onBackClick
    )
}

@Composable
fun MediaScanSettingsScreen(onBackClick: () -> Unit) {
    PlaceholderScreen(
        title = "Media Scan",
        description = "Configure how media files are scanned and indexed.",
        onBackClick = onBackClick
    )
}

@Composable
fun AboutScreen(onBackClick: () -> Unit) {
    PlaceholderScreen(
        title = "About Rhythm",
        description = "Rhythm Music Player - Tuner Beta\n\nVersion information and app details.",
        onBackClick = onBackClick
    )
}

@Composable
fun UpdatesSettingsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val appSettings = AppSettings.getInstance(context)
    val updatesEnabled by appSettings.updatesEnabled.collectAsState()
    val autoCheckForUpdates by appSettings.autoCheckForUpdates.collectAsState()
    
    CollapsibleHeaderScreen(
        title = "Updates",
        showBackButton = true,
        onBackClick = onBackClick
    ) { modifier ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            
            item {
                TunerSettingCard(
                    title = "Enable Updates",
                    description = "Allow the app to check for and download updates",
                    icon = Icons.Default.SystemUpdate,
                    checked = updatesEnabled,
                    onCheckedChange = { appSettings.setUpdatesEnabled(it) }
                )
            }
            
            if (updatesEnabled) {
                item {
                    TunerSettingCard(
                        title = "Periodic Check",
                        description = "Automatically check for updates from Rhythm's GitHub repo",
                        icon = Icons.Default.Update,
                        checked = autoCheckForUpdates,
                        onCheckedChange = { appSettings.setAutoCheckForUpdates(it) }
                    )
                }
            }
            
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

@Composable
fun ExperimentalFeaturesScreen(onBackClick: () -> Unit) {
    PlaceholderScreen(
        title = "Experimental Features",
        description = "Enable or disable experimental features and beta functionality.",
        onBackClick = onBackClick
    )
}

// Reusable setting card component
@Composable
fun TunerSettingCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean? = null,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (checked != null && onCheckedChange != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }
    }
}

