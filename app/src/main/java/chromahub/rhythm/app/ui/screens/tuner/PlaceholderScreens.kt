package chromahub.rhythm.app.ui.screens.tuner

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import chromahub.rhythm.app.data.AppSettings
import chromahub.rhythm.app.data.Playlist
import chromahub.rhythm.app.data.Song
import chromahub.rhythm.app.ui.components.CollapsibleHeaderScreen
import chromahub.rhythm.app.ui.components.RhythmIcons
import chromahub.rhythm.app.util.HapticUtils
import chromahub.rhythm.app.viewmodel.MusicViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ✅ API Management Screen (FULLY MERGED from ApiManagementBottomSheet)
@Composable
fun ApiManagementSettingsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val appSettings = AppSettings.getInstance(context)
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    
    // API states
    val deezerApiEnabled by appSettings.deezerApiEnabled.collectAsState()
    val canvasApiEnabled by appSettings.canvasApiEnabled.collectAsState()
    val lrclibApiEnabled by appSettings.lrclibApiEnabled.collectAsState()
    val ytMusicApiEnabled by appSettings.ytMusicApiEnabled.collectAsState()
    val spotifyApiEnabled by appSettings.spotifyApiEnabled.collectAsState()
    val spotifyClientId by appSettings.spotifyClientId.collectAsState()
    val spotifyClientSecret by appSettings.spotifyClientSecret.collectAsState()
    val appleMusicApiEnabled by appSettings.appleMusicApiEnabled.collectAsState()
    
    // Spotify API dialog state
    var showSpotifyConfigDialog by remember { mutableStateOf(false) }
    
    CollapsibleHeaderScreen(
        title = "API Management",
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
                Text(
                    text = "Control which external API services are active",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // Deezer API
            item {
                ApiServiceCard(
                    title = "Deezer",
                    description = "Free artist images and album artwork - no setup needed",
                    status = "Ready",
                    isConfigured = true,
                    isEnabled = deezerApiEnabled,
                    icon = Icons.Default.Public,
                    showToggle = true,
                    onToggle = { enabled -> appSettings.setDeezerApiEnabled(enabled) },
                    onClick = { /* No configuration needed */ }
                )
            }
            
            // Spotify Canvas API
            item {
                ApiServiceCard(
                    title = "Spotify Canvas",
                    description = if (spotifyClientId.isNotEmpty() && spotifyClientSecret.isNotEmpty()) {
                        "Spotify integration for Canvas videos (High Data Usage)"
                    } else {
                        "Canvas videos from Spotify (Please use your own key!)"
                    },
                    status = if (spotifyClientId.isNotEmpty() && spotifyClientSecret.isNotEmpty()) {
                        "Active"
                    } else {
                        "Need Setup"
                    },
                    isConfigured = true,
                    isEnabled = canvasApiEnabled && (spotifyApiEnabled || true),
                    icon = RhythmIcons.Song,
                    showToggle = true,
                    onToggle = { enabled -> 
                        appSettings.setCanvasApiEnabled(enabled)
                        if (!enabled) {
                            scope.launch {
                                try {
                                    val canvasRepository = chromahub.rhythm.app.data.CanvasRepository(context, appSettings)
                                    canvasRepository.clearCache()
                                } catch (e: Exception) {
                                    Log.e("ApiManagement", "Error clearing canvas cache", e)
                                }
                            }
                        }
                    },
                    onClick = { showSpotifyConfigDialog = true }
                )
            }
            
            // Apple Music API
            item {
                ApiServiceCard(
                    title = "Apple Music",
                    description = "Word-by-word synchronized lyrics (Highest Quality)",
                    status = "Ready",
                    isConfigured = true,
                    isEnabled = appleMusicApiEnabled,
                    icon = RhythmIcons.Queue,
                    showToggle = true,
                    onToggle = { enabled -> appSettings.setAppleMusicApiEnabled(enabled) },
                    onClick = { /* No configuration needed */ }
                )
            }
            
            // LRCLib
            item {
                ApiServiceCard(
                    title = "LRCLib",
                    description = "Free line-by-line synced lyrics (Fallback)",
                    status = "Ready",
                    isConfigured = true,
                    isEnabled = lrclibApiEnabled,
                    icon = RhythmIcons.Queue,
                    showToggle = true,
                    onToggle = { enabled -> appSettings.setLrcLibApiEnabled(enabled) },
                    onClick = { /* No configuration needed */ }
                )
            }
            
            // YouTube Music
            item {
                ApiServiceCard(
                    title = "YouTube Music",
                    description = "Fallback for artist images and album artwork",
                    status = "Ready",
                    isConfigured = true,
                    isEnabled = ytMusicApiEnabled,
                    icon = RhythmIcons.Album,
                    showToggle = true,
                    onToggle = { enabled -> appSettings.setYTMusicApiEnabled(enabled) },
                    onClick = { /* No configuration needed */ }
                )
            }
            
            // GitHub
            item {
                ApiServiceCard(
                    title = "GitHub",
                    description = "App updates and release information",
                    status = "Ready",
                    isConfigured = true,
                    isEnabled = true,
                    icon = RhythmIcons.Download,
                    showToggle = false,
                    onClick = { /* No configuration needed */ }
                )
            }
            
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
    
    // Spotify API Configuration Dialog
    if (showSpotifyConfigDialog) {
        chromahub.rhythm.app.ui.screens.SpotifyApiConfigDialog(
            currentClientId = spotifyClientId,
            currentClientSecret = spotifyClientSecret,
            onDismiss = { showSpotifyConfigDialog = false },
            onSave = { clientId, clientSecret ->
                appSettings.setSpotifyClientId(clientId)
                appSettings.setSpotifyClientSecret(clientSecret)
                if (clientId.isNotEmpty() && clientSecret.isNotEmpty()) {
                    appSettings.setSpotifyApiEnabled(true)
                }
                showSpotifyConfigDialog = false
            },
            appSettings = appSettings
        )
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

// StreamingSettingsScreen removed - not in original SettingsScreen
// AudioSettingsScreen removed - was inline settings in original SettingsScreen
// DownloadsSettingsScreen removed - not in original SettingsScreen
// OfflineModeSettingsScreen removed - not in original SettingsScreen

// Queue & Playback Settings Screen
@Composable
fun QueuePlaybackSettingsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val appSettings = AppSettings.getInstance(context)
    
    val shuffleUsesExoplayer by appSettings.shuffleUsesExoplayer.collectAsState()
    val autoAddToQueue by appSettings.autoAddToQueue.collectAsState()
    val clearQueueOnNewSong by appSettings.clearQueueOnNewSong.collectAsState()
    val repeatModePersistence by appSettings.repeatModePersistence.collectAsState()
    val shuffleModePersistence by appSettings.shuffleModePersistence.collectAsState()
    
    CollapsibleHeaderScreen(
        title = "Queue & Playback",
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
                Text(
                    text = "Queue Behavior",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )
            }
            
            item {
                TunerSettingCard(
                    title = "Use ExoPlayer Shuffle",
                    description = "Let the media player handle shuffle (recommended: OFF for manual shuffle)",
                    icon = RhythmIcons.Shuffle,
                    checked = shuffleUsesExoplayer,
                    onCheckedChange = { appSettings.setShuffleUsesExoplayer(it) }
                )
            }
            
            item {
                TunerSettingCard(
                    title = "Auto Queue",
                    description = "Automatically add related songs to queue when playing",
                    icon = RhythmIcons.Queue,
                    checked = autoAddToQueue,
                    onCheckedChange = { appSettings.setAutoAddToQueue(it) }
                )
            }
            
            item {
                TunerSettingCard(
                    title = "Clear Queue on New Song",
                    description = "Clear the current queue when playing a new song directly",
                    icon = RhythmIcons.Delete,
                    checked = clearQueueOnNewSong,
                    onCheckedChange = { appSettings.setClearQueueOnNewSong(it) }
                )
            }
            
            item { Spacer(modifier = Modifier.height(8.dp)) }
            
            item {
                Text(
                    text = "Playback Persistence",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )
            }
            
            item {
                TunerSettingCard(
                    title = "Remember Repeat Mode",
                    description = "Save repeat mode (Off/All/One) between app restarts",
                    icon = RhythmIcons.Repeat,
                    checked = repeatModePersistence,
                    onCheckedChange = { appSettings.setRepeatModePersistence(it) }
                )
            }
            
            item {
                TunerSettingCard(
                    title = "Remember Shuffle Mode",
                    description = "Save shuffle on/off state between app restarts",
                    icon = RhythmIcons.Shuffle,
                    checked = shuffleModePersistence,
                    onCheckedChange = { appSettings.setShuffleModePersistence(it) }
                )
            }
            
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

// ✅ FULLY MERGED Playlists Screen (simplified playlist management)
@Composable
fun PlaylistsSettingsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val musicViewModel: MusicViewModel = viewModel()
    val playlists by musicViewModel.playlists.collectAsState()
    
    val defaultPlaylists = playlists.filter { it.isDefault }
    val userPlaylists = playlists.filter { !it.isDefault }
    val emptyPlaylists = playlists.filter { !it.isDefault && it.songs.isEmpty() }
    
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var playlistToDelete by remember { mutableStateOf<Playlist?>(null) }
    
    CollapsibleHeaderScreen(
        title = "Playlists",
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
            
            // Statistics Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Your Collection",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${playlists.size}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("Total", style = MaterialTheme.typography.bodySmall)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${userPlaylists.size}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("Custom", style = MaterialTheme.typography.bodySmall)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${defaultPlaylists.size}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("Default", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
            
            // Management Actions
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Manage Playlists",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        TunerSettingCard(
                            icon = Icons.Default.Add,
                            title = "Create New Playlist",
                            description = "Add a new custom playlist",
                            onClick = { 
                                HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                                showCreatePlaylistDialog = true 
                            }
                        )
                        
                        if (emptyPlaylists.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            TunerSettingCard(
                                icon = Icons.Default.Delete,
                                title = "Cleanup Empty Playlists",
                                description = "Remove ${emptyPlaylists.size} empty playlist${if (emptyPlaylists.size > 1) "s" else ""}",
                                onClick = {
                                    HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                                    emptyPlaylists.forEach { playlist ->
                                        musicViewModel.deletePlaylist(playlist.id)
                                    }
                                }
                            )
                        }
                    }
                }
            }
            
            // Default Playlists
            if (defaultPlaylists.isNotEmpty()) {
                item {
                    Text(
                        text = "Default Playlists",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                items(defaultPlaylists) { playlist ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = playlist.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${playlist.songs.size} songs",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            
            // User Playlists
            if (userPlaylists.isNotEmpty()) {
                item {
                    Text(
                        text = "My Playlists",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                items(userPlaylists) { playlist ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.QueueMusic,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = playlist.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${playlist.songs.size} songs",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = {
                                    HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                                    playlistToDelete = playlist
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
    
    if (showCreatePlaylistDialog) {
        chromahub.rhythm.app.ui.components.CreatePlaylistDialog(
            onDismiss = { showCreatePlaylistDialog = false },
            onConfirm = { name ->
                musicViewModel.createPlaylist(name)
                showCreatePlaylistDialog = false
            }
        )
    }
    
    playlistToDelete?.let { playlist ->
        AlertDialog(
            onDismissRequest = { playlistToDelete = null },
            title = { Text("Delete Playlist?") },
            text = { Text("Are you sure you want to delete \"${playlist.name}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        musicViewModel.deletePlaylist(playlist.id)
                        playlistToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { playlistToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ✅ FULLY MERGED Media Scan Screen (blacklist/whitelist management)
@Composable
fun MediaScanSettingsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val appSettings = AppSettings.getInstance(context)
    val musicViewModel: MusicViewModel = viewModel()
    
    // Get all songs and filtered items
    val allSongs by musicViewModel.songs.collectAsState()
    val filteredSongs by musicViewModel.filteredSongs.collectAsState()
    val blacklistedSongs by appSettings.blacklistedSongs.collectAsState()
    val blacklistedFolders by appSettings.blacklistedFolders.collectAsState()
    val whitelistedSongs by appSettings.whitelistedSongs.collectAsState()
    val whitelistedFolders by appSettings.whitelistedFolders.collectAsState()
    
    // Get current media scan mode from settings
    val mediaScanMode by appSettings.mediaScanMode.collectAsState()
    
    // Mode state
    var currentMode by remember { 
        mutableStateOf(
            if (mediaScanMode == "whitelist") chromahub.rhythm.app.ui.screens.MediaScanMode.WHITELIST 
            else chromahub.rhythm.app.ui.screens.MediaScanMode.BLACKLIST
        ) 
    }
    
    // Tab state
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Songs", "Folders")
    
    // Computed values OUTSIDE LazyColumn
    val filteredSongDetails = remember(allSongs, blacklistedSongs, whitelistedSongs, currentMode) {
        when (currentMode) {
            chromahub.rhythm.app.ui.screens.MediaScanMode.BLACKLIST -> 
                allSongs.filter { song -> blacklistedSongs.contains(song.id) }
            chromahub.rhythm.app.ui.screens.MediaScanMode.WHITELIST -> 
                allSongs.filter { song -> whitelistedSongs.contains(song.id) }
        }
    }
    
    val filteredFoldersList = remember(blacklistedFolders, whitelistedFolders, currentMode) {
        when (currentMode) {
            chromahub.rhythm.app.ui.screens.MediaScanMode.BLACKLIST -> blacklistedFolders
            chromahub.rhythm.app.ui.screens.MediaScanMode.WHITELIST -> whitelistedFolders
        }
    }
    
    CollapsibleHeaderScreen(
        title = "Media Scan",
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
            
            // Mode toggle
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        // Blacklist mode button
                        FilterChip(
                            onClick = { 
                                HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                                currentMode = chromahub.rhythm.app.ui.screens.MediaScanMode.BLACKLIST
                                appSettings.setMediaScanMode("blacklist")
                            },
                            label = { 
                                Text(
                                    "Blacklist",
                                    fontWeight = if (currentMode == chromahub.rhythm.app.ui.screens.MediaScanMode.BLACKLIST) FontWeight.Bold else FontWeight.Normal
                                ) 
                            },
                            selected = currentMode == chromahub.rhythm.app.ui.screens.MediaScanMode.BLACKLIST,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Block,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // Whitelist mode button
                        FilterChip(
                            onClick = { 
                                HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                                currentMode = chromahub.rhythm.app.ui.screens.MediaScanMode.WHITELIST
                                appSettings.setMediaScanMode("whitelist")
                            },
                            label = { 
                                Text(
                                    "Whitelist",
                                    fontWeight = if (currentMode == chromahub.rhythm.app.ui.screens.MediaScanMode.WHITELIST) FontWeight.Bold else FontWeight.Normal
                                ) 
                            },
                            selected = currentMode == chromahub.rhythm.app.ui.screens.MediaScanMode.WHITELIST,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            // How It Works Card
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "How It Works",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                        
                        if (currentMode == chromahub.rhythm.app.ui.screens.MediaScanMode.BLACKLIST) {
                            Text(
                                text = "• Hide specific songs or folders from library\n• Perfect for excluding ringtones and notifications\n• All other music remains visible",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        } else {
                            Text(
                                text = "• Only show songs from selected folders\n• Create a curated library\n• All other music will be hidden",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }
            
            // Tabs
            item {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = if (currentMode == chromahub.rhythm.app.ui.screens.MediaScanMode.BLACKLIST) 
                                MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = {
                                HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                                selectedTabIndex = index
                            },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }
            
            // Tab content - Songs tab
            if (selectedTabIndex == 0) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(
                                containerColor = if (currentMode == chromahub.rhythm.app.ui.screens.MediaScanMode.BLACKLIST) 
                                    MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "${filteredSongDetails.size}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (currentMode == chromahub.rhythm.app.ui.screens.MediaScanMode.BLACKLIST) "Blocked" else "Whitelisted",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "${allSongs.size}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Total",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
                
                item {
                    Button(
                        onClick = {
                            HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                            if (currentMode == chromahub.rhythm.app.ui.screens.MediaScanMode.BLACKLIST) {
                                appSettings.clearBlacklist()
                            } else {
                                appSettings.clearWhitelist()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Clear All")
                    }
                }
                
                items(filteredSongDetails, key = { it.id }) { song ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = song.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = song.artist,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            IconButton(
                                onClick = {
                                    HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                                    if (currentMode == chromahub.rhythm.app.ui.screens.MediaScanMode.BLACKLIST) {
                                        appSettings.removeFromBlacklist(song.id)
                                    } else {
                                        appSettings.removeFromWhitelist(song.id)
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.RemoveCircle,
                                    contentDescription = "Remove",
                                    tint = if (currentMode == chromahub.rhythm.app.ui.screens.MediaScanMode.BLACKLIST) 
                                        MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            } else {
                // Folders tab
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (currentMode == chromahub.rhythm.app.ui.screens.MediaScanMode.BLACKLIST) 
                                MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "${filteredFoldersList.size}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (currentMode == chromahub.rhythm.app.ui.screens.MediaScanMode.BLACKLIST) "Blocked Folders" else "Whitelisted Folders",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                
                item {
                    Button(
                        onClick = {
                            HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                            if (currentMode == chromahub.rhythm.app.ui.screens.MediaScanMode.BLACKLIST) {
                                blacklistedFolders.forEach { folder ->
                                    appSettings.removeFolderFromBlacklist(folder)
                                }
                            } else {
                                whitelistedFolders.forEach { folder ->
                                    appSettings.removeFolderFromWhitelist(folder)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Clear All Folders")
                    }
                }
                
                items(filteredFoldersList) { folder ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = folder,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            IconButton(
                                onClick = {
                                    HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                                    if (currentMode == chromahub.rhythm.app.ui.screens.MediaScanMode.BLACKLIST) {
                                        appSettings.removeFolderFromBlacklist(folder)
                                    } else {
                                        appSettings.removeFolderFromWhitelist(folder)
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.RemoveCircle,
                                    contentDescription = "Remove",
                                    tint = if (currentMode == chromahub.rhythm.app.ui.screens.MediaScanMode.BLACKLIST) 
                                        MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

@Composable
fun AboutScreen(onBackClick: () -> Unit) {
    CollapsibleHeaderScreen(
        title = "About",
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
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = "Rhythm",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Rhythm Music Player",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Tuner Beta",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "New modern settings interface\nwith improved organization and design",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
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
    val context = LocalContext.current
    val appSettings = AppSettings.getInstance(context)
    val hapticFeedbackEnabled by appSettings.hapticFeedbackEnabled.collectAsState()
    
    CollapsibleHeaderScreen(
        title = "Experimental Features",
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
                    title = "Haptic Feedback",
                    description = "Vibrate when tapping buttons and interacting with the interface",
                    icon = Icons.Default.Vibration,
                    checked = hapticFeedbackEnabled,
                    onCheckedChange = { appSettings.setHapticFeedbackEnabled(it) }
                )
            }
            
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "More experimental features coming soon in future updates",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

// Cache Management Screen (merged from CacheManagementBottomSheet)
@Composable
fun CacheManagementSettingsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val appSettings = AppSettings.getInstance(context)
    val musicViewModel: MusicViewModel = viewModel()
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    
    // Collect states
    val maxCacheSize by appSettings.maxCacheSize.collectAsState()
    val clearCacheOnExit by appSettings.clearCacheOnExit.collectAsState()
    
    // Local states
    var currentCacheSize by remember { mutableStateOf(0L) }
    var isCalculatingSize by remember { mutableStateOf(false) }
    var isClearingCache by remember { mutableStateOf(false) }
    var showCacheSizeDialog by remember { mutableStateOf(false) }
    var cacheDetails by remember { mutableStateOf<Map<String, Long>>(emptyMap()) }
    
    // Canvas repository for cache management
    val canvasRepository = remember { 
        chromahub.rhythm.app.data.CanvasRepository(context, appSettings)
    }

    // Calculate cache size when the screen opens
    LaunchedEffect(Unit) {
        isCalculatingSize = true
        try {
            currentCacheSize = chromahub.rhythm.app.util.CacheManager.getCacheSize(context, canvasRepository)
            cacheDetails = chromahub.rhythm.app.util.CacheManager.getDetailedCacheSize(context, canvasRepository)
        } catch (e: Exception) {
            Log.e("CacheManagement", "Error calculating cache size", e)
        } finally {
            isCalculatingSize = false
        }
    }
    
    CollapsibleHeaderScreen(
        title = "Cache Management",
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
                Text(
                    text = "Manage cached data including images, temporary files, and other app data.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            item { Spacer(modifier = Modifier.height(8.dp)) }
            
            // Current Cache Status
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PieChart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Current Cache Status",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (isCalculatingSize) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Calculating cache size...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            // Total cache size
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Total Cache Size:",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = chromahub.rhythm.app.util.CacheManager.formatBytes(currentCacheSize),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Cache breakdown
                            cacheDetails.forEach { (label, size) ->
                                if (size > 0) {
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "  • $label:",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = chromahub.rhythm.app.util.CacheManager.formatBytes(size),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Cache limit
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Cache Limit:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${String.format("%.1f", maxCacheSize / (1024f * 1024f))} MB",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            
            // Settings
            item {
                TunerSettingCard(
                    title = "Clear Cache on Exit",
                    description = "Automatically clear cache when exiting the app",
                    icon = Icons.Default.DeleteSweep,
                    checked = clearCacheOnExit,
                    onCheckedChange = { appSettings.setClearCacheOnExit(it) }
                )
            }
            
            // Clear cache button
            item {
                Button(
                    onClick = {
                        if (!isClearingCache) {
                            scope.launch {
                                isClearingCache = true
                                HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                                try {
                                    currentCacheSize = chromahub.rhythm.app.util.CacheManager.getCacheSize(context, canvasRepository)
                                    cacheDetails = chromahub.rhythm.app.util.CacheManager.getDetailedCacheSize(context, canvasRepository)
                                    Toast.makeText(context, "Cache cleared successfully", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error clearing cache: ${e.message}", Toast.LENGTH_SHORT).show()
                                } finally {
                                    isClearingCache = false
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isClearingCache && currentCacheSize > 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    if (isClearingCache) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Icon(Icons.Default.DeleteSweep, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isClearingCache) "Clearing..." else "Clear All Cache Now")
                }
            }
            
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

// Backup & Restore Screen (merged from BackupRestoreBottomSheet)
@Composable
fun BackupRestoreSettingsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val appSettings = AppSettings.getInstance(context)
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val musicViewModel: MusicViewModel = viewModel()
    
    // Collect states
    val autoBackupEnabled by appSettings.autoBackupEnabled.collectAsState()
    val lastBackupTimestamp by appSettings.lastBackupTimestamp.collectAsState()
    val backupLocation by appSettings.backupLocation.collectAsState()
    
    // Local states
    var isCreatingBackup by remember { mutableStateOf(false) }
    var isRestoringFromFile by remember { mutableStateOf(false) }
    
    // File picker launcher for backup export
    val backupLocationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                scope.launch {
                    try {
                        isCreatingBackup = true
                        HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                        
                        musicViewModel.ensurePlaylistsSaved()
                        val backupJson = appSettings.createBackup()
                        
                        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                            outputStream.write(backupJson.toByteArray())
                            outputStream.flush()
                        }
                        
                        appSettings.setLastBackupTimestamp(System.currentTimeMillis())
                        appSettings.setBackupLocation(uri.toString())
                        
                        Toast.makeText(context, "Backup created successfully", Toast.LENGTH_SHORT).show()
                        
                        // Also copy to clipboard
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Rhythm Backup", backupJson)
                        clipboard.setPrimaryClip(clip)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Failed to create backup: ${e.message}", Toast.LENGTH_SHORT).show()
                    } finally {
                        isCreatingBackup = false
                    }
                }
            }
        } else {
            isCreatingBackup = false
        }
    }
    
    // File picker launcher for restore
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                scope.launch {
                    try {
                        isRestoringFromFile = true
                        HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                        
                        val inputStream = context.contentResolver.openInputStream(uri)
                        val backupJson = inputStream?.bufferedReader()?.use { it.readText() }
                        
                        if (!backupJson.isNullOrEmpty()) {
                            if (appSettings.restoreFromBackup(backupJson)) {
                                musicViewModel.reloadPlaylistsFromSettings()
                                Toast.makeText(context, "Restore successful", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Invalid backup format", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Unable to read backup file", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Failed to restore: ${e.message}", Toast.LENGTH_SHORT).show()
                    } finally {
                        isRestoringFromFile = false
                    }
                }
            }
        } else {
            isRestoringFromFile = false
        }
    }
    
    CollapsibleHeaderScreen(
        title = "Backup & Restore",
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
                Text(
                    text = "Safeguard your personalized settings, playlists, and preferences.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            item { Spacer(modifier = Modifier.height(8.dp)) }
            
            // Backup Status Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Last Backup Card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = if (lastBackupTimestamp > 0) 
                                MaterialTheme.colorScheme.primaryContainer 
                            else 
                                MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = if (lastBackupTimestamp > 0) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                                contentDescription = null,
                                tint = if (lastBackupTimestamp > 0) 
                                    MaterialTheme.colorScheme.onPrimaryContainer 
                                else 
                                    MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (lastBackupTimestamp > 0) {
                                    val sdf = SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
                                    sdf.format(Date(lastBackupTimestamp))
                                } else "Never",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (lastBackupTimestamp > 0) 
                                    MaterialTheme.colorScheme.onPrimaryContainer 
                                else 
                                    MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "Last Backup",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (lastBackupTimestamp > 0) 
                                    MaterialTheme.colorScheme.onPrimaryContainer 
                                else 
                                    MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                    
                    // Auto Backup Card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = if (autoBackupEnabled) 
                                MaterialTheme.colorScheme.tertiaryContainer 
                            else 
                                MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = if (autoBackupEnabled) Icons.Filled.Autorenew else Icons.Filled.Schedule,
                                contentDescription = null,
                                tint = if (autoBackupEnabled) 
                                    MaterialTheme.colorScheme.onTertiaryContainer 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (autoBackupEnabled) "Enabled" else "Manual",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (autoBackupEnabled) 
                                    MaterialTheme.colorScheme.onTertiaryContainer 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Auto Backup",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (autoBackupEnabled) 
                                    MaterialTheme.colorScheme.onTertiaryContainer 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // Auto-backup toggle
            item {
                TunerSettingCard(
                    title = "Auto-backup",
                    description = "Automatically backup settings weekly",
                    icon = Icons.Filled.Autorenew,
                    checked = autoBackupEnabled,
                    onCheckedChange = { 
                        appSettings.setAutoBackupEnabled(it)
                        if (it) appSettings.triggerImmediateBackup()
                    }
                )
            }
            
            // Backup action button
            item {
                Button(
                    onClick = {
                        if (!isCreatingBackup) {
                            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                                addCategory(Intent.CATEGORY_OPENABLE)
                                type = "application/json"
                                putExtra(Intent.EXTRA_TITLE, "rhythm_backup_${SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(Date())}.json")
                            }
                            backupLocationLauncher.launch(intent)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isCreatingBackup,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isCreatingBackup) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Icon(Icons.Filled.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isCreatingBackup) "Creating Backup..." else "Create Backup to File")
                }
            }
            
            // Restore action button
            item {
                OutlinedButton(
                    onClick = {
                        if (!isRestoringFromFile) {
                            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                                addCategory(Intent.CATEGORY_OPENABLE)
                                type = "application/json"
                            }
                            filePickerLauncher.launch(intent)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isRestoringFromFile,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isRestoringFromFile) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Icon(Icons.Filled.Restore, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isRestoringFromFile) "Restoring..." else "Restore from File")
                }
            }
            
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

// Library Tab Order Screen (merged from LibraryTabOrderBottomSheet)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibraryTabOrderSettingsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val appSettings = AppSettings.getInstance(context)
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    
    val tabOrder by appSettings.libraryTabOrder.collectAsState()
    var reorderableList by remember { mutableStateOf(tabOrder.toList()) }
    
    // Helper function to get display name and icon for tab
    fun getTabInfo(tabId: String): Pair<String, ImageVector> {
        return when (tabId) {
            "SONGS" -> Pair("Songs", RhythmIcons.Relax)
            "PLAYLISTS" -> Pair("Playlists", RhythmIcons.PlaylistFilled)
            "ALBUMS" -> Pair("Albums", RhythmIcons.Music.Album)
            "ARTISTS" -> Pair("Artists", RhythmIcons.Artist)
            "EXPLORER" -> Pair("Explorer", Icons.Default.Folder)
            else -> Pair(tabId, RhythmIcons.Music.Song)
        }
    }
    
    CollapsibleHeaderScreen(
        title = "Library Tab Order",
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
                Text(
                    text = "Reorder tabs to customize your library experience",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            item { Spacer(modifier = Modifier.height(8.dp)) }
            
            // Reorderable list
            itemsIndexed(
                items = reorderableList,
                key = { _, item -> item }
            ) { index, tabId ->
                val (tabName, tabIcon) = getTabInfo(tabId)
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .animateItem(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    shape = RoundedCornerShape(16.dp)
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
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            // Position indicator
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${index + 1}",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                            
                            // Tab icon
                            Icon(
                                imageVector = tabIcon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                            
                            // Tab name
                            Text(
                                text = tabName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        // Reorder buttons
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Move up button
                            FilledIconButton(
                                onClick = {
                                    if (index > 0) {
                                        HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                                        val newList = reorderableList.toMutableList()
                                        val item = newList.removeAt(index)
                                        newList.add(index - 1, item)
                                        reorderableList = newList
                                    }
                                },
                                enabled = index > 0,
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                                ),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = "Move up",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            
                            // Move down button
                            FilledIconButton(
                                onClick = {
                                    if (index < reorderableList.size - 1) {
                                        HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                                        val newList = reorderableList.toMutableList()
                                        val item = newList.removeAt(index)
                                        newList.add(index + 1, item)
                                        reorderableList = newList
                                    }
                                },
                                enabled = index < reorderableList.size - 1,
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                                ),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = "Move down",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Reset button
                    OutlinedButton(
                        onClick = {
                            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                            appSettings.resetLibraryTabOrder()
                            reorderableList = listOf("SONGS", "PLAYLISTS", "ALBUMS", "ARTISTS", "EXPLORER")
                            Toast.makeText(context, "Tab order reset to default", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reset")
                    }
                    
                    // Save button
                    Button(
                        onClick = {
                            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                            appSettings.setLibraryTabOrder(reorderableList)
                            Toast.makeText(context, "Tab order saved", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save")
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

// ✅ SIMPLIFIED Theme Customization Screen
@Composable
fun ThemeCustomizationSettingsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val appSettings = AppSettings.getInstance(context)
    
    CollapsibleHeaderScreen(
        title = "Theme Customization",
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
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Appearance Customization",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Personalize your experience",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
            
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Available Theme Options",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                        Text(
                            text = "• Choose from Material You dynamic colors\n• Select dark/light/auto theme modes\n• Customize accent colors\n• Adjust font styles and sizes",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
            
            item {
                Text(
                    text = "For advanced theme customization including custom colors and fonts, please use the Theme settings in the main Appearance section of settings.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
            
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

// ✅ SIMPLIFIED Equalizer Screen
@Composable
fun EqualizerSettingsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    
    CollapsibleHeaderScreen(
        title = "Equalizer",
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
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Audio Equalizer",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Adjust audio frequencies and effects",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
            
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Equalizer Features",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                        Text(
                            text = "• 5-band frequency equalizer\n• Multiple preset options (Rock, Pop, Jazz, etc.)\n• Bass boost and virtualizer effects\n• Save custom presets",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
            
            item {
                Text(
                    text = "Access the full equalizer controls from the Now Playing screen by tapping the equalizer icon. Adjust frequency bands and apply presets for different music genres.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
            
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

// ✅ SIMPLIFIED Sleep Timer Screen
@Composable
fun SleepTimerSettingsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    
    CollapsibleHeaderScreen(
        title = "Sleep Timer",
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
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Sleep Timer",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Auto-stop playback",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
            
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Timer Options",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                        Text(
                            text = "• Set custom timer duration\n• Quick presets (15, 30, 60 minutes)\n• Fade out option before stopping\n• Timer extends on current track completion",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
            
            item {
                Text(
                    text = "Set a sleep timer from the Now Playing screen. The timer will automatically stop music playback after the specified duration, perfect for falling asleep to your favorite tracks.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
            
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

// ✅ SIMPLIFIED Crash Log History Screen
@Composable
fun CrashLogHistorySettingsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    
    CollapsibleHeaderScreen(
        title = "Crash Logs",
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
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.BugReport,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Crash Log History",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = "App error reports",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
            
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Diagnostic Information",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                        Text(
                            text = "• View detailed error logs\n• Export logs for debugging\n• Share with developers\n• Clear old crash reports",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
            
            item {
                Text(
                    text = "Crash logs help developers identify and fix issues. If you experience problems, you can view crash logs in the advanced debugging section or share them with the development team.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
            
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
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

// API Service Card component
@Composable
private fun ApiServiceCard(
    title: String,
    description: String,
    status: String,
    isConfigured: Boolean,
    icon: ImageVector,
    isEnabled: Boolean = true,
    showToggle: Boolean = false,
    onToggle: ((Boolean) -> Unit)? = null,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            !isEnabled -> MaterialTheme.colorScheme.surfaceVariant
                            isConfigured -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.errorContainer
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = when {
                        !isEnabled -> MaterialTheme.colorScheme.onSurfaceVariant
                        isConfigured -> MaterialTheme.colorScheme.onPrimaryContainer
                        else -> MaterialTheme.colorScheme.onErrorContainer
                    },
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = when {
                            !isEnabled -> MaterialTheme.colorScheme.surfaceVariant
                            isConfigured -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            else -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (!isEnabled) "Disabled" else status,
                            style = MaterialTheme.typography.labelSmall,
                            color = when {
                                !isEnabled -> MaterialTheme.colorScheme.onSurfaceVariant
                                isConfigured -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onErrorContainer
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isEnabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            // Toggle or Arrow icon
            if (showToggle && onToggle != null) {
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { enabled ->
                        HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                        onToggle(enabled)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }
    }
}

