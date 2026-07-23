package com.example.innogeeks.feature_onboarding.presentation.intro

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.innogeeks.core.presentation.ObserveAsEvents
import com.example.innogeeks.feature_onboarding.presentation.components.AuthGlowBackground
import com.example.innogeeks.feature_onboarding.presentation.components.liquidGlass
import com.example.innogeeks.ui.theme.InnogeeksTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel


@Composable
fun IntroRoot(
    onNavigateToLogin: () -> Unit,
    viewModel: IntroViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // The pager's scroll position is Compose-owned UI state (the one allowed exception to
    // "all state in the ViewModel") — see the compose-ui skill.
    val pagerState = rememberPagerState(pageCount = { state.pages.size })

    // Scope for the pager's suspend animateScrollToPage (ObserveAsEvents' lambda isn't suspend).
    val scope = rememberCoroutineScope()

    // Swipe -> tell the ViewModel which page we settled on (so isLastPage stays correct).
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .collect { viewModel.onAction(IntroAction.OnPageChanged(it)) }
    }

    // ViewModel events: Next -> animate the pager; Skip/GetStarted -> leave.
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is IntroEvent.ScrollToPage -> scope.launch { pagerState.animateScrollToPage(event.page) }
            is IntroEvent.NavigateToLogin -> onNavigateToLogin()
        }
    }

    IntroScreen(
        state = state,
        pagerState = pagerState,
        onAction = viewModel::onAction
    )
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun IntroScreen(
    state: IntroState,
    pagerState: androidx.compose.foundation.pager.PagerState,
    onAction: (IntroAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val hazeState = remember { HazeState() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AuthGlowBackground(hazeState = hazeState)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Skip lives top-right, out of the way — pure liquid glass over plain text,
            // NO blue overlay (that would read as tinted glassmorphism, not liquid glass).
            TextButton(
                onClick = { onAction(IntroAction.OnSkipClick) },
                modifier = Modifier
                    .align(Alignment.End)
                    .liquidGlass(hazeState, cornerRadius = 20.dp)
            ) {
                Text("Skip", color = Color.White.copy(alpha = 0.85f))
            }

            // The swipeable slides — one glass card per page. weight(1f) takes the space
            // above the controls.
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .weight(1f)
            ) { pageIndex ->
                val page = state.pages[pageIndex]
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    IntroGlassCard(page = page, hazeState = hazeState)
                }
            }

            Spacer(Modifier.height(24.dp))

            // Animated dot indicators — the active one stretches into a pill and brightens.
            PageIndicators(
                pageCount = state.pages.size,
                currentPage = state.currentPage
            )

            Spacer(Modifier.height(24.dp))

            // Last page shows a full-width "Get Started"; earlier pages show "Next".
            if (state.isLastPage) {
                Button(
                    onClick = { onAction(IntroAction.OnGetStartedClick) },
                    shape = RoundedCornerShape(percent = 50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("Get Started", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = { onAction(IntroAction.OnNextClick) },
                    shape = RoundedCornerShape(percent = 50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("Next", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// One liquid-glass slide card (shared recipe, 32dp radius).
@Composable
private fun IntroGlassCard(page: IntroPage, hazeState: HazeState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlass(hazeState, cornerRadius = 32.dp)
            .padding(horizontal = 28.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Illustration for this slide (microchip / network / rocket), clipped to rounded corners.
        Image(
            painter = painterResource(page.imageRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(20.dp))
        )
        Spacer(Modifier.height(28.dp))
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = page.subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.75f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PageIndicators(pageCount: Int, currentPage: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(pageCount) { index ->
            val selected = index == currentPage
            // Animate width + color below the recomposition layer for smooth transitions.
            val width by animateDpAsState(if (selected) 24.dp else 8.dp, label = "dotWidth")
            val color by animateColorAsState(
                if (selected) MaterialTheme.colorScheme.secondary else Color.White.copy(alpha = 0.3f),
                label = "dotColor"
            )
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun IntroScreenFirstPagePreview() {
    InnogeeksTheme {
        val pagerState = rememberPagerState(pageCount = { defaultIntroPages.size })
        IntroScreen(
            state = IntroState(),
            pagerState = pagerState,
            onAction = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun IntroScreenMiddlePagePreview() {
    InnogeeksTheme {
        val pagerState = rememberPagerState(initialPage = 1, pageCount = { defaultIntroPages.size })
        IntroScreen(
            state = IntroState(currentPage = 1),
            pagerState = pagerState,
            onAction = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun IntroScreenLastPagePreview() {
    InnogeeksTheme {
        val pagerState = rememberPagerState(initialPage = 2, pageCount = { defaultIntroPages.size })
        IntroScreen(
            state = IntroState(currentPage = 2),
            pagerState = pagerState,
            onAction = {}
        )
    }
}
