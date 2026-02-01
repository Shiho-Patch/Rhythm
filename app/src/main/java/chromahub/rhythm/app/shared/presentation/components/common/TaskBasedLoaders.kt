package chromahub.rhythm.app.shared.presentation.components.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Task-Based Loader Components
 * These loaders are expressive and self-documenting based on their purpose.
 * 
 * Purpose: Replace generic M3LinearLoader, M3CircularLoader, etc. with 
 * task-specific implementations that clearly communicate intent.
 */

/**
 * MediaScanningLoader - For media/library scanning operations
 * 
 * Use when:
 * - Scanning music library from device storage
 * - Indexing media files
 * - Building song/album/artist catalogs
 * 
 * Features: Four-color circular animation to indicate comprehensive scanning
 */
@Composable
fun MediaScanningLoader(
    modifier: Modifier = Modifier,
    strokeWidth: Float = 4f,
    isExpressive: Boolean = true
) {
    M3FourColorCircularLoader(
        modifier = modifier,
        strokeWidth = strokeWidth,
        isExpressive = isExpressive
    )
}

/**
 * DataProcessingLoader - For data processing and transformation operations
 * 
 * Use when:
 * - Processing/filtering large datasets
 * - Applying transformations to data
 * - Computing statistics or aggregations
 * - Loading and parsing configuration files
 * 
 * Features: Linear progress for determinate or shimmer effect for indeterminate
 */
@Composable
fun DataProcessingLoader(
    progress: Float? = null,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    isExpressive: Boolean = true
) {
    M3LinearLoader(
        progress = progress,
        modifier = modifier,
        color = color,
        fourColor = false,
        isExpressive = isExpressive
    )
}

/**
 * ActionProgressLoader - For quick, short-lived actions
 * 
 * Use when:
 * - Adding/removing items from blacklist/whitelist
 * - Quick file operations
 * - Saving/deleting individual items
 * - Toggling settings
 * 
 * Features: Small, compact circular loader that doesn't distract
 */
@Composable
fun ActionProgressLoader(
    modifier: Modifier = Modifier,
    size: Dp = 16.dp,
    strokeWidth: Dp = 2.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    isExpressive: Boolean = true
) {
    SimpleCircularLoader(
        modifier = modifier,
        size = size,
        strokeWidth = strokeWidth,
        color = color,
        isExpressive = isExpressive
    )
}

/**
 * ContentLoadingIndicator - For loading content in screens/views
 * 
 * Use when:
 * - Loading songs, albums, artists lists
 * - Fetching remote content
 * - Waiting for API responses
 * - Loading screen data during navigation
 * 
 * Features: Standard circular loader that indicates data is being fetched
 */
@Composable
fun ContentLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Float = 4f,
    isExpressive: Boolean = true
) {
    M3CircularLoader(
        progress = null, // Indeterminate
        modifier = modifier,
        color = color,
        strokeWidth = strokeWidth,
        fourColor = false,
        isExpressive = isExpressive
    )
}

/**
 * PlaybackBufferingLoader - For media playback buffering
 * 
 * Use when:
 * - Buffering audio/video content
 * - Loading media for playback
 * - Preparing player for playback
 * - Streaming content preparation
 * 
 * Features: Pulse animation to indicate active buffering
 */
@Composable
fun PlaybackBufferingLoader(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    isExpressive: Boolean = true
) {
    M3PulseLoader(
        modifier = modifier,
        color = color,
        isExpressive = isExpressive
    )
}

/**
 * InitializationLoader - For app/feature initialization
 * 
 * Use when:
 * - App first-time setup
 * - Feature initialization
 * - Permission setup flows
 * - Onboarding processes
 * 
 * Features: Four-color animation indicating multi-stage initialization
 */
@Composable
fun InitializationLoader(
    modifier: Modifier = Modifier,
    strokeWidth: Float = 4f,
    isExpressive: Boolean = true
) {
    M3FourColorCircularLoader(
        modifier = modifier,
        strokeWidth = strokeWidth,
        isExpressive = isExpressive
    )
}

/**
 * FileOperationLoader - For file I/O operations
 * 
 * Use when:
 * - Reading/writing files
 * - Importing/exporting playlists
 * - Backup/restore operations
 * - File system operations
 * 
 * Features: Linear progress with optional determinate progress tracking
 */
@Composable
fun FileOperationLoader(
    progress: Float? = null,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    isExpressive: Boolean = true
) {
    M3LinearLoader(
        progress = progress,
        modifier = modifier,
        color = color,
        fourColor = false,
        isExpressive = isExpressive
    )
}

/**
 * NetworkOperationLoader - For network requests
 * 
 * Use when:
 * - Fetching data from APIs
 * - Downloading content
 * - Uploading data
 * - Syncing with remote services
 * 
 * Features: Circular loader with subtle animation
 */
@Composable
fun NetworkOperationLoader(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Float = 4f,
    isExpressive: Boolean = true
) {
    M3CircularLoader(
        progress = null,
        modifier = modifier,
        color = color,
        strokeWidth = strokeWidth,
        fourColor = false,
        isExpressive = isExpressive
    )
}

/**
 * ImageLoadingPlaceholder - For image loading operations
 * 
 * Use when:
 * - Loading album art
 * - Loading artist images
 * - Loading playlist covers
 * - Any image asset loading
 * 
 * Features: Subtle pulse animation that doesn't overpower the UI
 */
@Composable
fun ImageLoadingPlaceholder(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surfaceVariant,
    isExpressive: Boolean = true
) {
    M3PulseLoader(
        modifier = modifier,
        color = color,
        isExpressive = isExpressive
    )
}

/**
 * SearchingLoader - For search operations
 * 
 * Use when:
 * - Performing search queries
 * - Filtering large datasets
 * - Real-time search results
 * 
 * Features: Linear shimmer effect to indicate active searching
 */
@Composable
fun SearchingLoader(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    isExpressive: Boolean = true
) {
    M3LinearLoader(
        progress = null, // Indeterminate
        modifier = modifier,
        color = color,
        fourColor = false,
        isExpressive = isExpressive
    )
}

/**
 * MultiStageOperationLoader - For operations with multiple stages
 * 
 * Use when:
 * - Multi-step setup wizards
 * - Complex processing pipelines
 * - Staged data migration
 * - Multi-phase initialization
 * 
 * Features: Four-color linear progress indicating multiple stages
 */
@Composable
fun MultiStageOperationLoader(
    progress: Float? = null,
    modifier: Modifier = Modifier,
    isExpressive: Boolean = true
) {
    M3FourColorLinearLoader(
        modifier = modifier,
        isExpressive = isExpressive
    )
}

/**
 * CacheOperationLoader - For cache-related operations
 * 
 * Use when:
 * - Building cache
 * - Clearing cache
 * - Cache validation
 * - Preloading cached data
 * 
 * Features: Circular loader with moderate emphasis
 */
@Composable
fun CacheOperationLoader(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.tertiary,
    strokeWidth: Float = 3f,
    isExpressive: Boolean = true
) {
    M3CircularLoader(
        progress = null,
        modifier = modifier,
        color = color,
        strokeWidth = strokeWidth,
        fourColor = false,
        isExpressive = isExpressive
    )
}
