package com.example.innogeeks.feature_home.presentation

import android.content.res.Configuration
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.innogeeks.core.presentation.components.AuthGlowBackground
import com.example.innogeeks.core.presentation.components.liquidGlass
import com.example.innogeeks.feature_home.domain.model.ClubStats
import com.example.innogeeks.feature_home.presentation.home.HomeRoot
import com.example.innogeeks.feature_home.presentation.home.HomeScreen
import com.example.innogeeks.feature_home.presentation.home.HomeState
import com.example.innogeeks.ui.theme.InnogeeksTheme
import dev.chrisbanes.haze.HazeState

private data class BottomNavTab(
    val label: String,
    val icon: ImageVector,
)

private val guestTabs = listOf(
    BottomNavTab("Home", Icons.Filled.Home),
    BottomNavTab("Domains", Icons.Filled.Category),
    BottomNavTab("Events", Icons.Filled.CalendarMonth),
    BottomNavTab("Profile", Icons.Filled.Person),
)

@Composable
fun MainScaffold(
    homeContent: @Composable (HazeState) -> Unit = { hazeState -> HomeRoot(hazeState = hazeState) }
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val hazeState = remember { HazeState() }

    Box(modifier = Modifier.fillMaxSize()) {
        AuthGlowBackground(hazeState = hazeState)

        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedTab) {
                0 -> homeContent(hazeState)
                1 -> TabPlaceholder("Domains")
                2 -> TabPlaceholder("Events")
                3 -> TabPlaceholder("Profile")
            }
        }

        // Floating Glassmorphic Bottom Navigation Bar
        InnogeeksBottomNav(
            tabs = guestTabs,
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            hazeState = hazeState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        )
    }
}

// Floating Glassmorphic Bottom Nav Bar with Liquid Stretch + Expanding Text
@Composable
private fun InnogeeksBottomNav(
    tabs: List<BottomNavTab>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .liquidGlass(hazeState = hazeState, cornerRadius = 32.dp)
            .padding(6.dp)
    ) {
        val tabBounds = remember { androidx.compose.runtime.mutableStateMapOf<Int, androidx.compose.ui.geometry.Rect>() }

        Box(modifier = Modifier.fillMaxWidth()) {
            val targetRect = tabBounds[selectedTab] ?: androidx.compose.ui.geometry.Rect.Zero

            var previousTab by remember { mutableIntStateOf(selectedTab) }
            LaunchedEffect(selectedTab) { previousTab = selectedTab }
            val isMovingRight = selectedTab > previousTab

            val leftStiffness = if (isMovingRight) 40f else 350f
            val rightStiffness = if (isMovingRight) 350f else 40f

            val density = androidx.compose.ui.platform.LocalDensity.current

            val animatedLeft by animateDpAsState(
                targetValue = with(density) { targetRect.left.toDp() },
                animationSpec = spring(dampingRatio = 0.65f, stiffness = leftStiffness),
                label = "leftEdge"
            )
            val animatedRight by animateDpAsState(
                targetValue = with(density) { targetRect.right.toDp() },
                animationSpec = spring(dampingRatio = 0.65f, stiffness = rightStiffness),
                label = "rightEdge"
            )

            // The stretching Liquid Pill background (only renders if we have bounds)
            if (targetRect != androidx.compose.ui.geometry.Rect.Zero) {
                Box(
                    modifier = Modifier
                        .offset(x = animatedLeft)
                        .width(animatedRight - animatedLeft)
                        .height(48.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.20f),
                            RoundedCornerShape(22.dp)
                        )
                )
            }

            // The Icons and Text
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = selectedTab == index
                    val iconScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.1f else 1.0f,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "iconScale"
                    )
                    val iconColor by androidx.compose.animation.animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(
                            alpha = 0.6f
                        ),
                        animationSpec = androidx.compose.animation.core.tween(300),
                        label = "iconColor"
                    )

                    Box(
                        modifier = Modifier
                            .height(48.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onTabSelected(index) }
                            )
                            .onGloballyPositioned { coordinates ->
                                // Convert bounds to parent BoxWithConstraints coordinates
                                val boundsInParent = coordinates.boundsInParent()
                                tabBounds[index] = boundsInParent
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                                tint = iconColor,
                                modifier = Modifier
                                    .size(24.dp)
                                    .graphicsLayer(scaleX = iconScale, scaleY = iconScale)
                            )

                            androidx.compose.animation.AnimatedVisibility(
                                visible = isSelected,
                                enter = androidx.compose.animation.fadeIn(
                                    androidx.compose.animation.core.tween(
                                        300
                                    )
                                ) + androidx.compose.animation.expandHorizontally(spring(stiffness = Spring.StiffnessMediumLow)),
                                exit = androidx.compose.animation.fadeOut(
                                    androidx.compose.animation.core.tween(
                                        200
                                    )
                                ) + androidx.compose.animation.shrinkHorizontally(spring(stiffness = Spring.StiffnessMediumLow))
                            ) {
                                Text(
                                    text = tab.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    modifier = Modifier.padding(start = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TabPlaceholder(name: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "$name — coming soon")
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MainScaffoldPreview() {
    InnogeeksTheme {
        MainScaffold(
            homeContent = { hazeState ->
                HomeScreen(
                    state = HomeState(
                        isLoading = false,
                        stats = ClubStats(150, 45, 5, 24),
                        domains = emptyList(),
                        events = emptyList()
                    ),
                    hazeState = hazeState,
                    onAction = {}
                )
            }
        )
    }
}
