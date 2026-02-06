package chromahub.rhythm.app.features.local.presentation.components.bottomsheets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import chromahub.rhythm.app.shared.data.model.LyricsData
import chromahub.rhythm.app.shared.presentation.components.icons.RhythmIcons
import chromahub.rhythm.app.util.HapticUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtraControlBottomSheet(
    onDismiss: () -> Unit,
    sheetState: SheetState,
    hiddenChips: Set<String>,
    equalizerEnabled: Boolean,
    sleepTimerActive: Boolean,
    sleepTimerRemainingSeconds: Long,
    lyrics: LyricsData?,
    onAddToPlaylist: () -> Unit,
    onPlaybackSpeed: () -> Unit,
    onEqualizer: () -> Unit,
    onSleepTimer: () -> Unit,
    onLyricsEditor: () -> Unit,
    onSongInfo: () -> Unit,
    haptic: HapticFeedback,
    isExtraSmallWidth: Boolean = false,
    isCompactWidth: Boolean = false
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Animation states
    var showContent by remember { mutableStateOf(false) }
    val contentAlpha by animateFloatAsState(
        targetValue = if (showContent) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "extraControlContentAlpha"
    )
    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
    }

    // Helper to dismiss sheet properly then perform action
    fun dismissAndDo(action: () -> Unit) {
        scope.launch {
            sheetState.hide()
            onDismiss()
            action()
        }
    }

    @Composable
    fun BottomSheetActionRow(
        icon: ImageVector,
        label: String,
        isActive: Boolean = false,
        onClick: () -> Unit
    ) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(
                when {
                    isExtraSmallWidth -> 10.dp
                    isCompactWidth -> 11.dp
                    else -> 12.dp
                }
            ),
            color = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(
                        horizontal = when {
                            isExtraSmallWidth -> 12.dp
                            isCompactWidth -> 14.dp
                            else -> 16.dp
                        },
                        vertical = when {
                            isExtraSmallWidth -> 12.dp
                            isCompactWidth -> 13.dp
                            else -> 14.dp
                        }
                    )
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(
                        when {
                            isExtraSmallWidth -> 20.dp
                            isCompactWidth -> 22.dp
                            else -> 24.dp
                        }
                    ),
                    tint = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(
                    modifier = Modifier.width(
                        when {
                            isExtraSmallWidth -> 12.dp
                            isCompactWidth -> 14.dp
                            else -> 16.dp
                        }
                    )
                )
                Text(
                    label,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = when {
                            isExtraSmallWidth -> 14.sp
                            isCompactWidth -> 15.sp
                            else -> 16.sp
                        }
                    ),
                    color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.primary)
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    bottom = when {
                        isExtraSmallWidth -> 20.dp
                        isCompactWidth -> 22.dp
                        else -> 24.dp
                    }
                )
        ) {
            // Standard header with animation
            StandardBottomSheetHeader(
                title = "Player Controls",
                subtitle = "Additional features",
                visible = showContent
            )

            Spacer(modifier = Modifier.height(
                when {
                    isExtraSmallWidth -> 12.dp
                    isCompactWidth -> 14.dp
                    else -> 16.dp
                }
            ))

            // Scrollable action list with content animation
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = when {
                            isExtraSmallWidth -> 16.dp
                            isCompactWidth -> 20.dp
                            else -> 24.dp
                        }
                    )
                    .graphicsLayer(alpha = contentAlpha),
                verticalArrangement = Arrangement.spacedBy(
                    when {
                        isExtraSmallWidth -> 6.dp
                        isCompactWidth -> 7.dp
                        else -> 8.dp
                    }
                )
            ) {
                BottomSheetActionRow(
                    icon = RhythmIcons.AddToPlaylist,
                    label = "Add to Playlist",
                    onClick = {
                        HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
                        dismissAndDo { onAddToPlaylist() }
                    }
                )

                if ("PLAYBACK_SPEED" !in hiddenChips) {
                    BottomSheetActionRow(
                        icon = Icons.Filled.Speed,
                        label = "Playback Speed",
                        onClick = {
                            HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
                            dismissAndDo { onPlaybackSpeed() }
                        }
                    )
                }

                if ("EQUALIZER" !in hiddenChips) {
                    BottomSheetActionRow(
                        icon = Icons.Filled.GraphicEq,
                        label = if (equalizerEnabled) "Equalizer (ON)" else "Equalizer",
                        isActive = equalizerEnabled,
                        onClick = {
                            HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
                            dismissAndDo { onEqualizer() }
                        }
                    )
                }

                if ("SLEEP_TIMER" !in hiddenChips) {
                    BottomSheetActionRow(
                        icon = if (sleepTimerActive) Icons.Rounded.AccessTime else Icons.Filled.AccessTime,
                        label = if (sleepTimerActive) {
                            val minutes = sleepTimerRemainingSeconds / 60
                            val seconds = sleepTimerRemainingSeconds % 60
                            "Sleep Timer (${minutes}:${seconds.toString().padStart(2, '0')})"
                        } else "Sleep Timer",
                        isActive = sleepTimerActive,
                        onClick = {
                            HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
                            dismissAndDo { onSleepTimer() }
                        }
                    )
                }

                if ("LYRICS" !in hiddenChips) {
                    BottomSheetActionRow(
                        icon = if (lyrics?.getBestLyrics()?.isNotEmpty() == true) Icons.Rounded.Edit else Icons.Rounded.Lyrics,
                        label = if (lyrics?.getBestLyrics()?.isNotEmpty() == true) "Edit Lyrics" else "Add Lyrics",
                        onClick = {
                            HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
                            dismissAndDo { onLyricsEditor() }
                        }
                    )
                }

                if ("SONG_INFO" !in hiddenChips) {
                    BottomSheetActionRow(
                        icon = Icons.Rounded.Info,
                        label = "Song Information",
                        onClick = {
                            HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
                            dismissAndDo { onSongInfo() }
                        }
                    )
                }
            }
        }
    }
}
