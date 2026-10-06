package com.example.innogeeks.feature_home.presentation.home

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.innogeeks.core.presentation.ObserveAsEvents
import com.example.innogeeks.core.domain.session.Session
import com.example.innogeeks.core.presentation.components.SectionLabel
import com.example.innogeeks.feature_home.domain.model.Achievement
import com.example.innogeeks.feature_home.domain.model.ClubStats
import com.example.innogeeks.feature_home.domain.model.CultureMoment
import com.example.innogeeks.feature_home.presentation.home.components.AchievementsRow
import com.example.innogeeks.feature_home.presentation.home.components.ClassCultureCard
import com.example.innogeeks.feature_home.presentation.home.components.DomainWheel
import com.example.innogeeks.feature_home.presentation.home.components.HomeHero
import com.example.innogeeks.feature_home.presentation.home.components.HomeTopBar
import com.example.innogeeks.feature_home.presentation.home.components.KeywordTicker
import com.example.innogeeks.feature_home.presentation.home.components.previewDomains
import com.example.innogeeks.ui.theme.InnogeeksTheme
import edu.kiet.innogeeks.R
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import org.koin.androidx.compose.koinViewModel
import androidx.compose.ui.res.stringResource
import com.example.innogeeks.core.presentation.UiText

// Root composable that connects to the ViewModel and receives shared HazeState.
@Composable
fun HomeRoot(
    hazeState: HazeState,
    session: Session,
    onNavigateToProfile: () -> Unit,
    onNavigateToAuth: () -> Unit,
    onNavigateToEvents: (eventId: String) -> Unit = {},
    viewModel: HomeViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            HomeEvent.NavigateToProfile -> onNavigateToProfile()
            is HomeEvent.NavigateToEvents -> onNavigateToEvents(event.eventId)
        }
    }

    HomeScreen(
        state = state,
        hazeState = hazeState,
        onAction = viewModel::onAction,
        // Until a /me endpoint exists, the email local-part is the only initials source.
        initials = (session as? Session.Authenticated)?.collegeEmail?.toInitials(),
        onLoginClick = onNavigateToAuth
    )
}

private fun String.toInitials(): String =
    substringBefore('@')
        .split('.', '_', '-')
        .filter { it.isNotBlank() }
        .take(2)
        .map { it.first().uppercaseChar() }
        .joinToString("")
        .ifEmpty { "?" }

@Composable
fun HomeScreen(
    state: HomeState,
    hazeState: HazeState,
    onAction: (HomeAction) -> Unit,
    // null means guest — the top bar shows a Log in pill instead of an avatar.
    initials: String? = null,
    onLoginClick: () -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Box
        }

        if (state.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = state.error.asString(), color = MaterialTheme.colorScheme.error)
            }
            return@Box
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .hazeSource(hazeState),
            // Bottom padding clears the floating glass nav pill.
            contentPadding = PaddingValues(bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                HomeTopBar(
                    initials = initials,
                    onProfileClick = { onAction(HomeAction.OnProfileClick) },
                    onLoginClick = onLoginClick
                )
            }

            item { HomeHero() }

            item { KeywordTicker(rows = state.tickerRows) }

            item {
                SectionLabel(
                    text = stringResource(R.string.common_domains),
                    modifier = Modifier.padding(horizontal = 18.dp)
                )
            }

            item {
                DomainWheel(
                    domains = state.domains,
                    selectedId = state.selectedDomain?.id,
                    onDomainSelected = { onAction(HomeAction.OnDomainSelected(it)) }
                )
            }

            item {
                SectionLabel(
                    text = stringResource(R.string.home_class_culture),
                    modifier = Modifier.padding(horizontal = 18.dp)
                )
            }

            item {
                ClassCultureCard(
                    moments = state.cultureMoments,
                    onMomentClick = { moment -> onAction(HomeAction.OnClassCultureClick(moment.eventId)) }
                )
            }

            item {
                SectionLabel(
                    text = stringResource(R.string.home_achievements),
                    modifier = Modifier.padding(horizontal = 18.dp)
                )
            }

            item { AchievementsRow(achievements = state.achievements) }
        }
    }
}

private val previewState = HomeState(
    isLoading = false,
    stats = ClubStats(150, 45, 6, 24),
    domains = previewDomains,
    achievements = listOf(
        Achievement("a1", "🏆", "Finalist", "Smart India Hackathon"),
        Achievement("a2", "🚀", "Nominee", "NASA Space Apps — Global"),
        Achievement("a3", "🥈", "Top 50", "Flipkart GRiD 5.0")
    ),
    tickerRows = listOf(
        listOf("Technology", "Design", "Robotics"),
        listOf("Innovation", "Community", "Mentorship"),
        listOf("Hackathon", "Code", "Workshops")
    ),
    cultureMoments = listOf(
        CultureMoment(
            "cm1", "NASA Space Apps Challenge", "Ghaziabad Edition · 2025",
            "150+ innovators, 35+ teams, and a ₹75,000 prize pool.",
            R.drawable.event_nasa_a, "e27"
        ),
        CultureMoment(
            "cm2", "InnoForge", "2025",
            "A hands-on build sprint where teams shipped working prototypes in a day.",
            R.drawable.event_innoforge_a, "e26"
        ),
        CultureMoment(
            "cm3", "Winter of Code 3.0", "2024–25",
            "Innogeeks' flagship open-source program.",
            R.drawable.event_iwoc3, "e23"
        )
    ),
    selectedDomainId = "appd"
)

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 900)
@Composable
private fun HomeScreenSuccessPreview() {
    InnogeeksTheme {
        HomeScreen(state = previewState, hazeState = HazeState(), onAction = {})
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeScreenLoadingPreview() {
    InnogeeksTheme {
        HomeScreen(state = HomeState(isLoading = true), hazeState = HazeState(), onAction = {})
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeScreenErrorPreview() {
    InnogeeksTheme {
        HomeScreen(
            state = HomeState(isLoading = false, error = UiText.DynamicString("Failed to load home data.")),
            hazeState = HazeState(),
            onAction = {}
        )
    }
}
