package chromahub.rhythm.app.features.streaming.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import chromahub.rhythm.app.ui.components.CollapsibleHeaderScreen
import io.ktor.http.invoke

/**
 * Streaming Home Screen with design inspired by local HomeScreen.
 * Shows personalized content from streaming services (placeholder for now).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreamingHomeScreen(
    onNavigateToSearch: () -> Unit = {},
    onNavigateToLibrary: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onSwitchToLocalMode: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    CollapsibleHeaderScreen(
        title = "Streaming",
        showBackButton = false,
        actions = {
            IconButton(onClick = onNavigateToSearch) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }
            IconButton(onClick = onNavigateToSettings) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        }
    ) { contentModifier ->
        LazyColumn(
            modifier = contentModifier
                .fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp)
        ) {
            // Coming Soon Banner
            item {
                Spacer(modifier = Modifier.height(16.dp))
                StreamingComingSoonCard(
                    onSwitchToLocalMode = onSwitchToLocalMode
                )
            }
            
            // Quick Access Section (Placeholder)
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Quick Access",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    QuickAccessCard(
                        icon = Icons.Default.Favorite,
                        title = "Liked Songs",
                        subtitle = "Your favorites",
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    QuickAccessCard(
                        icon = Icons.Default.History,
                        title = "Recent",
                        subtitle = "Recently played",
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Featured Playlists Section (Placeholder)
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Featured Playlists",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                PlaceholderCarousel(
                    items = listOf("Today's Hits", "Chill Vibes", "Workout Mix", "Focus Flow"),
                    icon = Icons.Default.QueueMusic
                )
            }
            
            // New Releases Section (Placeholder)
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "New Releases",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                PlaceholderCarousel(
                    items = listOf("Album 1", "Album 2", "Album 3", "Album 4"),
                    icon = Icons.Default.Album
                )
            }
            
            // Top Charts Section (Placeholder)
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Top Charts",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                PlaceholderCarousel(
                    items = listOf("Top 50", "Viral", "Trending", "Rising"),
                    icon = Icons.Default.TrendingUp
                )
            }
        }
    }
}

@Composable
private fun StreamingComingSoonCard(
    onSwitchToLocalMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CloudQueue,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Streaming Coming Soon",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "We're working on integrating your favorite streaming services. Stay tuned for updates!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            FilledTonalButton(
                onClick = onSwitchToLocalMode,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f),
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Switch to Local Music")
            }
        }
    }
}

@Composable
private fun QuickAccessCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
private fun PlaceholderCarousel(
    items: List<String>,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
    ) {
        items(items) { item ->
            PlaceholderItemCard(
                title = item,
                icon = icon
            )
        }
    }
}

@Composable
private fun PlaceholderItemCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.size(140.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}