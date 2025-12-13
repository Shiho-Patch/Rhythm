package chromahub.rhythm.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import chromahub.rhythm.app.data.stats.PlaybackStatsRepository
import chromahub.rhythm.app.data.stats.StatsTimeRange
import chromahub.rhythm.app.ui.components.CollapsibleHeaderScreen
import chromahub.rhythm.app.ui.components.RhythmIcons
import chromahub.rhythm.app.util.HapticUtils
import chromahub.rhythm.app.viewmodel.MusicViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Rhythm Stats - Your Musical Journey
 * Redesigned with cosmic/comic theme, Library-style tabs, and time-based decorations
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListeningStatsScreen(
    navController: NavController,
    viewModel: MusicViewModel = viewModel()
) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    
    // Get data
    val songs by viewModel.songs.collectAsState()
    
    // UI State
    var selectedRange by remember { mutableStateOf(StatsTimeRange.ALL_TIME) }
    var statsSummary by remember { mutableStateOf<PlaybackStatsRepository.PlaybackStatsSummary?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val tabRowState = rememberLazyListState()
    
    // Screen entrance animation
    var showContent by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(50)
        showContent = true
    }
    
    val contentAlpha by animateFloatAsState(
        targetValue = if (showContent) 1f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "contentAlpha"
    )
    
    val contentOffset by animateFloatAsState(
        targetValue = if (showContent) 0f else 30f,
        animationSpec = tween(durationMillis = 450),
        label = "contentOffset"
    )
    
    // Load stats when range changes
    LaunchedEffect(selectedRange, songs) {
        isLoading = true
        statsSummary = viewModel.loadPlaybackStats(selectedRange)
        isLoading = false
    }
    
    // Auto-scroll tab row when selection changes
    LaunchedEffect(selectedRange) {
        val index = StatsTimeRange.entries.indexOf(selectedRange)
        tabRowState.animateScrollToItem(index.coerceAtLeast(0))
    }
    
    CollapsibleHeaderScreen(
        title = "Rhythm Stats",
        showBackButton = true,
        onBackClick = {
            HapticUtils.performHapticFeedback(context, hapticFeedback, HapticFeedbackType.LongPress)
            navController.popBackStack()
        }
    ) { modifier ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = contentAlpha
                    translationY = contentOffset
                }
                .verticalScroll(rememberScrollState())
                .padding(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Time Range Tabs - Library style
            RhythmTimeRangeTabs(
                selectedRange = selectedRange,
                onRangeSelected = { range ->
                    HapticUtils.performHapticFeedback(context, hapticFeedback, HapticFeedbackType.TextHandleMove)
                    selectedRange = range
                },
                tabRowState = tabRowState
            )
            
            // Content with animated transitions
            AnimatedContent(
                targetState = Pair(isLoading, selectedRange),
                transitionSpec = {
                    (fadeIn(animationSpec = tween(300)) + slideInHorizontally { it / 4 }) togetherWith
                    (fadeOut(animationSpec = tween(200)) + slideOutHorizontally { -it / 4 })
                },
                label = "statsContentTransition",
                modifier = Modifier.padding(horizontal = 16.dp)
            ) { (loading, _) ->
                if (loading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else if (statsSummary == null || statsSummary!!.totalPlayCount == 0) {
                    EmptyRhythmView()
                } else {
                    val stats = statsSummary!!
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Cosmic Time Widget with time-of-day decoration
                        CosmicListeningTimeWidget(stats.totalDurationMs)
                        
                        // Quick Stats Row
                        QuickStatsRow(stats)
                        
                        // Musical Journey Section
                        MusicalJourneyCard(stats)
                        
                        // Top Vibes (Songs)
                        if (stats.topSongs.isNotEmpty()) {
                            TopVibesCard(stats.topSongs)
                        }
                        
                        // Star Artists
                        if (stats.topArtists.isNotEmpty()) {
                            StarArtistsCard(stats.topArtists)
                        }
                        
                        // Genre Mix
                        if (stats.topGenres.isNotEmpty()) {
                            GenreMixCard(stats.topGenres)
                        }
                        
                        // Beat Timeline
                        if (stats.timeline.isNotEmpty()) {
                            BeatTimelineCard(stats.timeline)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Time Range Tabs - Library style with animations
 */
@Composable
private fun RhythmTimeRangeTabs(
    selectedRange: StatsTimeRange,
    onRangeSelected: (StatsTimeRange) -> Unit,
    tabRowState: androidx.compose.foundation.lazy.LazyListState
) {
    val tabNames = mapOf(
        StatsTimeRange.TODAY to "Today",
        StatsTimeRange.WEEK to "This Week",
        StatsTimeRange.MONTH to "This Month",
        StatsTimeRange.ALL_TIME to "All Time"
    )
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 0.dp
    ) {
        LazyRow(
            state = tabRowState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            itemsIndexed(StatsTimeRange.entries) { index, range ->
                val isSelected = range == selectedRange
                val animatedScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.05f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "tabScale"
                )
                
                val animatedContainerColor by animateColorAsState(
                    targetValue = if (isSelected) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.surfaceContainerLow,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "tabContainerColor"
                )
                
                val animatedContentColor by animateColorAsState(
                    targetValue = if (isSelected) 
                        MaterialTheme.colorScheme.onPrimary 
                    else 
                        MaterialTheme.colorScheme.onSurface,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "tabContentColor"
                )
                
                Button(
                    onClick = { onRangeSelected(range) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = animatedContainerColor,
                        contentColor = animatedContentColor
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.graphicsLayer {
                        scaleX = animatedScale
                        scaleY = animatedScale
                    },
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = when (range) {
                                StatsTimeRange.TODAY -> Icons.Outlined.Today
                                StatsTimeRange.WEEK -> Icons.Outlined.DateRange
                                StatsTimeRange.MONTH -> Icons.Outlined.CalendarMonth
                                StatsTimeRange.ALL_TIME -> Icons.Outlined.AllInclusive
                            },
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = tabNames[range] ?: range.displayName,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

/**
 * Get time of day for decoration
 */
private fun getTimeOfDay(): TimeOfDay {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> TimeOfDay.MORNING
        in 12..16 -> TimeOfDay.AFTERNOON
        in 17..20 -> TimeOfDay.EVENING
        else -> TimeOfDay.NIGHT
    }
}

private enum class TimeOfDay {
    MORNING, AFTERNOON, EVENING, NIGHT
}

/**
 * Cosmic Listening Time Widget with time-of-day themed decoration
 */
@Composable
private fun CosmicListeningTimeWidget(totalDurationMs: Long) {
    val timeOfDay = remember { getTimeOfDay() }
    
    // Time-based gradient colors
    val gradientColors = when (timeOfDay) {
        TimeOfDay.MORNING -> listOf(
            Color(0xFFFFD54F), // Warm yellow
            Color(0xFFFFB74D), // Orange
            Color(0xFFFF8A65)  // Coral
        )
        TimeOfDay.AFTERNOON -> listOf(
            Color(0xFF81D4FA), // Light blue
            Color(0xFF4FC3F7), // Sky blue
            Color(0xFF29B6F6)  // Bright blue
        )
        TimeOfDay.EVENING -> listOf(
            Color(0xFFFF8A65), // Coral
            Color(0xFFE57373), // Soft red
            Color(0xFFBA68C8)  // Purple
        )
        TimeOfDay.NIGHT -> listOf(
            Color(0xFF7C4DFF), // Deep purple
            Color(0xFF536DFE), // Indigo
            Color(0xFF448AFF)  // Blue
        )
    }
    
    val decorationIcon = when (timeOfDay) {
        TimeOfDay.MORNING -> Icons.Outlined.WbSunny
        TimeOfDay.AFTERNOON -> Icons.Outlined.LightMode
        TimeOfDay.EVENING -> Icons.Outlined.WbTwilight
        TimeOfDay.NIGHT -> Icons.Outlined.NightsStay
    }
    
    val decorationText = when (timeOfDay) {
        TimeOfDay.MORNING -> "Rise & Rhythm"
        TimeOfDay.AFTERNOON -> "Midday Beats"
        TimeOfDay.EVENING -> "Evening Vibes"
        TimeOfDay.NIGHT -> "Night Grooves"
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(gradientColors)
                )
                .padding(24.dp)
        ) {
            // Decorative elements
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Top decoration row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = decorationIcon,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = decorationText,
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    // Decorative stars for night, sun rays for day
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        when (timeOfDay) {
                            TimeOfDay.NIGHT -> {
                                repeat(3) {
                                    Text("✦", color = Color.White.copy(alpha = 0.7f))
                                }
                            }
                            TimeOfDay.MORNING -> {
                                Text("🌅", style = MaterialTheme.typography.titleMedium)
                            }
                            TimeOfDay.AFTERNOON -> {
                                Text("☀️", style = MaterialTheme.typography.titleMedium)
                            }
                            TimeOfDay.EVENING -> {
                                Text("🌆", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Main content
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icon container
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AccessTime,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = formatDuration(totalDurationMs),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Total Listening Time",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
                
                // Bottom decorative wave pattern
                if (timeOfDay == TimeOfDay.NIGHT) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "✧ ･ﾟ: *✧･ﾟ:* 🎵 *:･ﾟ✧*:･ﾟ✧",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Quick Stats Row
 */
@Composable
private fun QuickStatsRow(stats: PlaybackStatsRepository.PlaybackStatsSummary) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickStatChip(
            icon = Icons.Outlined.PlayCircle,
            value = "${stats.totalPlayCount}",
            label = "Plays",
            modifier = Modifier.weight(1f)
        )
        QuickStatChip(
            icon = Icons.Outlined.MusicNote,
            value = "${stats.uniqueSongs}",
            label = "Tracks",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickStatChip(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(44.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Empty state view
 */
@Composable
private fun EmptyRhythmView() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(96.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.BarChart,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Text(
                text = "No Listening Data",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Start your musical journey!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Play some tunes to see your stats ✨",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Musical Journey Card - Listening habits
 */
@Composable
private fun MusicalJourneyCard(stats: PlaybackStatsRepository.PlaybackStatsSummary) {
    RhythmSectionCard(
        title = "Your Journey",
        icon = Icons.Outlined.Explore
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            JourneyStatRow(Icons.Outlined.CalendarToday, "Active Days", "${stats.activeDays}")
            JourneyStatRow(Icons.Outlined.Restore, "Sessions", "${stats.totalSessions}")
            JourneyStatRow(Icons.Outlined.Timer, "Avg Session", formatDuration(stats.averageSessionDurationMs))
            if (stats.peakDayOfWeek != null) {
                JourneyStatRow(Icons.Outlined.Whatshot, "Peak Day", stats.peakDayOfWeek)
            }
        }
    }
}

@Composable
private fun JourneyStatRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceContainerLow,
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * Top Vibes - Top Songs
 */
@Composable
private fun TopVibesCard(songs: List<PlaybackStatsRepository.SongPlaybackSummary>) {
    RhythmSectionCard(
        title = "Top Vibes",
        icon = Icons.Outlined.Favorite
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            songs.take(5).forEachIndexed { index, song ->
                RhythmRankItem(
                    rank = index + 1,
                    title = song.title,
                    subtitle = song.artist,
                    plays = song.playCount,
                    duration = song.totalDurationMs,
                    isTopItem = index == 0
                )
            }
        }
    }
}

/**
 * Star Artists
 */
@Composable
private fun StarArtistsCard(artists: List<PlaybackStatsRepository.ArtistPlaybackSummary>) {
    RhythmSectionCard(
        title = "Star Artists",
        icon = Icons.Outlined.Stars
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            artists.take(5).forEachIndexed { index, artist ->
                RhythmRankItem(
                    rank = index + 1,
                    title = artist.artist,
                    subtitle = "${artist.uniqueSongs} tracks",
                    plays = artist.playCount,
                    duration = artist.totalDurationMs,
                    isTopItem = index == 0
                )
            }
        }
    }
}

/**
 * Genre Mix
 */
@Composable
private fun GenreMixCard(genres: List<PlaybackStatsRepository.GenrePlaybackSummary>) {
    RhythmSectionCard(
        title = "Genre Mix",
        icon = Icons.Outlined.Category
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            genres.take(5).forEach { genre ->
                GenreMixRow(
                    name = genre.genre,
                    percentage = genre.percentage
                )
            }
        }
    }
}

@Composable
private fun GenreMixRow(name: String, percentage: Float) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${(percentage * 100).toInt()}%",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        LinearProgressIndicator(
            progress = { percentage },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        )
    }
}

/**
 * Beat Timeline
 */
@Composable
private fun BeatTimelineCard(timeline: List<PlaybackStatsRepository.TimelineEntry>) {
    RhythmSectionCard(
        title = "Beat Timeline",
        icon = Icons.Outlined.Timeline
    ) {
        val maxPlays = timeline.maxOf { it.playCount }.coerceAtLeast(1)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            timeline.take(7).forEach { entry ->
                BeatTimelineRow(
                    label = entry.label,
                    plays = entry.playCount,
                    percentage = entry.playCount.toFloat() / maxPlays
                )
            }
        }
    }
}

@Composable
private fun BeatTimelineRow(label: String, plays: Int, percentage: Float) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(72.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        
        Box(
            modifier = Modifier
                .weight(1f)
                .height(28.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(percentage.coerceAtLeast(0.05f))
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        )
                    )
            )
        }
        
        Text(
            text = "$plays",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(36.dp),
            textAlign = TextAlign.End
        )
    }
}

/**
 * Rhythm Rank Item for top songs/artists
 */
@Composable
private fun RhythmRankItem(
    rank: Int,
    title: String,
    subtitle: String,
    plays: Int,
    duration: Long,
    isTopItem: Boolean
) {
    val bgColor = if (isTopItem) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank badge
        val rankBgColor = if (isTopItem) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceContainerHighest
        }
        
        val rankTextColor = if (isTopItem) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }
        
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .background(rankBgColor, RoundedCornerShape(8.dp))
        ) {
            Text(
                text = if (isTopItem) "👑" else "$rank",
                style = if (isTopItem) MaterialTheme.typography.labelLarge else MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = rankTextColor
            )
        }
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isTopItem) FontWeight.Bold else FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "$plays plays",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.tertiary
            )
            Text(
                text = formatDuration(duration),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

/**
 * Rhythm Section Card - Base card for sections
 */
@Composable
private fun RhythmSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            content()
        }
    }
}

private fun formatDuration(ms: Long): String {
    val seconds = ms / 1000
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    
    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m"
        else -> "< 1m"
    }
}
