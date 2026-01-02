package chromahub.rhythm.app.features.streaming.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import chromahub.rhythm.app.ui.components.CollapsibleHeaderScreen

/**
 * Streaming Search Screen matching local SearchScreen design.
 * Uses CollapsibleHeaderScreen with search bar, filters, and category browsing.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreamingSearchScreen(
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    var showFilterOptions by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("All") }
    
    // Filter states
    var filterSongs by remember { mutableStateOf(true) }
    var filterAlbums by remember { mutableStateOf(true) }
    var filterArtists by remember { mutableStateOf(true) }
    var filterPlaylists by remember { mutableStateOf(true) }
    
    val categories = listOf("All", "Songs", "Albums", "Artists", "Playlists")
    val activeFilters = listOf(
        "Songs" to filterSongs,
        "Albums" to filterAlbums,
        "Artists" to filterArtists,
        "Playlists" to filterPlaylists
    ).count { it.second }
    
    CollapsibleHeaderScreen(
        title = "Search Streaming",
        onBackClick = onBack,
        actions = {
            // Filter button with badge
            Box {
                IconButton(onClick = { showFilterOptions = !showFilterOptions }) {
                    Icon(
                        imageVector = Icons.Rounded.FilterList,
                        contentDescription = "Filter",
                        tint = if (activeFilters < 4) MaterialTheme.colorScheme.primary 
                               else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (activeFilters < 4) {
                    Badge(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-8).dp, y = 8.dp),
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = activeFilters.toString(),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp)
        )
        {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text("Search songs, albums, artists...")
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = { focusManager.clearFocus() }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .focusRequester(focusRequester),
                shape = RoundedCornerShape(28.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            
            // Filter Options
            AnimatedVisibility(
                visible = showFilterOptions,
                enter = expandVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "Filter Results",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = filterSongs,
                            onClick = { filterSongs = !filterSongs },
                            label = { Text("Songs") },
                            leadingIcon = if (filterSongs) {
                                { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(18.dp)) }
                            } else null
                        )
                        FilterChip(
                            selected = filterAlbums,
                            onClick = { filterAlbums = !filterAlbums },
                            label = { Text("Albums") },
                            leadingIcon = if (filterAlbums) {
                                { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(18.dp)) }
                            } else null
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = filterArtists,
                            onClick = { filterArtists = !filterArtists },
                            label = { Text("Artists") },
                            leadingIcon = if (filterArtists) {
                                { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(18.dp)) }
                            } else null
                        )
                        FilterChip(
                            selected = filterPlaylists,
                            onClick = { filterPlaylists = !filterPlaylists },
                            label = { Text("Playlists") },
                            leadingIcon = if (filterPlaylists) {
                                { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(18.dp)) }
                            } else null
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            // Category Pills
            if (searchQuery.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category) }
                        )
                    }
                }
            }
            
            // Content
            if (searchQuery.isEmpty()) {
                BrowseContent(
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                SearchResultsPlaceholder(
                    query = searchQuery,
                    category = selectedCategory,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun BrowseContent(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Browse All",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            val genres = listOf(
                Triple("Pop", Icons.Default.MusicNote, MaterialTheme.colorScheme.primary),
                Triple("Rock", Icons.Default.Album, MaterialTheme.colorScheme.secondary),
                Triple("Hip-Hop", Icons.Default.Mic, MaterialTheme.colorScheme.tertiary),
                Triple("Jazz", Icons.Default.Piano, MaterialTheme.colorScheme.error),
                Triple("Classical", Icons.Default.LibraryMusic, MaterialTheme.colorScheme.primaryContainer),
                Triple("Electronic", Icons.Default.Equalizer, MaterialTheme.colorScheme.secondaryContainer),
                Triple("R&B", Icons.Default.Headphones, MaterialTheme.colorScheme.tertiaryContainer),
                Triple("Country", Icons.Default.Audiotrack, MaterialTheme.colorScheme.surfaceVariant),
                Triple("Indie", Icons.Default.Star, MaterialTheme.colorScheme.inversePrimary),
                Triple("Latin", Icons.Default.Nightlife, MaterialTheme.colorScheme.errorContainer)
            )
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.height((genres.size / 2 * 112).dp)
            ) {
                items(genres) { (name, icon, color) ->
                    GenreCard(
                        name = name,
                        icon = icon,
                        color = color,
                        enabled = false
                    )
                }
            }
        }
    }
}

@Composable
private fun GenreCard(
    name: String,
    icon: ImageVector,
    color: Color,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = if (enabled) 0.5f else 0.3f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.TopEnd),
                tint = color.copy(alpha = if (enabled) 0.7f else 0.5f)
            )
            
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(
                    alpha = if (enabled) 1f else 0.5f
                ),
                modifier = Modifier.align(Alignment.BottomStart)
            )
        }
    }
}

@Composable
private fun SearchResultsPlaceholder(
    query: String,
    category: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CloudQueue,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Search Coming Soon",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Streaming search for \"$query\" in $category will be available when streaming services are connected.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
