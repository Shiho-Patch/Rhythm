package chromahub.rhythm.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import chromahub.rhythm.app.ui.theme.RhythmTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollapsibleHeaderScreen(
    title: String,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    actions: @Composable () -> Unit = {},
    filterDropdown: @Composable () -> Unit = {}, // New parameter for the filter dropdown
    scrollBehaviorKey: String? = null, // Key for preserving scroll behavior state
    showAppIcon: Boolean = false,
    iconVisibilityMode: Int = 0, // 0=Both, 1=Expanded Only, 2=Collapsed Only
    content: @Composable (Modifier) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState(),
        canScroll = { true }
    )

    // Entrance animation state
    var showContent by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(50) // Small delay for smoother transition
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

    val lazyListState = rememberLazyListState()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            val collapsedFraction = scrollBehavior.state.collapsedFraction
            val fontSize = (24 + (32 - 24) * (1 - collapsedFraction)).sp // Interpolate between 24sp and 32sp
            val containerColor = Color.Transparent // Always transparent

            Column {
                Spacer(modifier = Modifier.height(5.dp)) // Add more padding before the header starts
                LargeTopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(start = 14.dp)
                        ) {
                            // Icon visibility logic: 0=Both, 1=Expanded Only (collapsedFraction < 0.5), 2=Collapsed Only (collapsedFraction >= 0.5)
                            val shouldShowIcon = showAppIcon && when (iconVisibilityMode) {
                                0 -> true // Always show
                                1 -> collapsedFraction < 0.5f // Show only when expanded
                                2 -> collapsedFraction >= 0.5f // Show only when collapsed
                                else -> true
                            }
                            
                            if (shouldShowIcon) {
                                androidx.compose.foundation.Image(
                                    painter = androidx.compose.ui.res.painterResource(id = chromahub.rhythm.app.R.drawable.rhythm_splash_logo),
                                    contentDescription = "App Icon",
                                    modifier = Modifier.size((48 + (36 - 28) * (1 - collapsedFraction)).dp)
                                )
                            }
                            Text(
                                text = title,
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontFamily = FontFamily.Default,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = fontSize
                                )
                            )
                        }
                    },
                    navigationIcon = {
                        if (showBackButton) {
                            IconButton(
                                onClick = onBackClick,
                                modifier = Modifier.padding(start = 8.dp) // Add padding to the back button
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp) // Adjust size as needed
                                        .clip(RoundedCornerShape(50))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)), // Circular background
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    },
                    actions = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 8.dp) // Match left-side padding
                        ) {
                            filterDropdown() // Place the filter dropdown here
                            actions()
                        }
                    },
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = containerColor,
                        scrolledContainerColor = containerColor
                    )
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .graphicsLayer {
                    alpha = contentAlpha
                    translationY = contentOffset
                }
        ) {
            content(Modifier.fillMaxSize())
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CollapsibleHeaderScreenPreview() {
    RhythmTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            CollapsibleHeaderScreen(
                title = "Settings",
                showBackButton = true,
                onBackClick = { /* Handle back click in preview */ }
            ) { modifier ->
                LazyColumn(
                    modifier = modifier.padding(horizontal = 16.dp) // Consistent horizontal padding for content
                ) {
                    items(50) { index ->
                        Text(text = "Item $index", modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }
        }
    }
}
