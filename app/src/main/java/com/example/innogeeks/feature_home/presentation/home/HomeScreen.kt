package com.example.innogeeks.feature_home.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.innogeeks.feature_home.domain.model.ClubStats
import com.example.innogeeks.feature_home.domain.model.DomainPreview
import com.example.innogeeks.feature_home.domain.model.EventPreview
import com.example.innogeeks.feature_home.presentation.home.components.ClubStatsRow
import com.example.innogeeks.feature_home.presentation.home.components.DomainCard
import com.example.innogeeks.feature_home.presentation.home.components.EventCard
import com.example.innogeeks.feature_home.presentation.home.components.JoinCtaCard
import com.example.innogeeks.ui.theme.InnogeeksTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import org.koin.androidx.compose.koinViewModel

// Root composable that connects to the ViewModel and receives shared HazeState.
@Composable
fun HomeRoot(
    hazeState: HazeState,
    viewModel: HomeViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    HomeScreen(state = state, hazeState = hazeState, onAction = viewModel::onAction)
}

// Coordinator UI composable for the Guest Home screen.
@Composable
fun HomeScreen(
    state: HomeState,
    hazeState: HazeState,
    onAction: (HomeAction) -> Unit
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
                Text(text = state.error, color = MaterialTheme.colorScheme.error)
            }
            return@Box
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Text(
                    text = "Welcome to Innogeeks",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            state.stats?.let { stats ->
                item { ClubStatsRow(stats = stats) }
            }

            item {
                Text(
                    text = "Our Domains",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(state.domains) { domain ->
                        DomainCard(
                            domain = domain,
                            hazeState = hazeState,
                            onClick = { onAction(HomeAction.OnDomainClick(domain.id)) }
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Upcoming Events",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    state.events.forEach { event ->
                        EventCard(
                            event = event,
                            hazeState = hazeState,
                            onClick = { onAction(HomeAction.OnEventClick(event.id)) }
                        )
                    }
                }
            }

            item {
                JoinCtaCard(hazeState = hazeState, onClick = { onAction(HomeAction.OnJoinClick) })
            }
        }
    }
}

@Preview(uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeScreenLoadingPreview() {
    InnogeeksTheme {
        HomeScreen(state = HomeState(isLoading = true), hazeState = HazeState(), onAction = {})
    }
}

@Preview(uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeScreenErrorPreview() {
    InnogeeksTheme {
        HomeScreen(state = HomeState(isLoading = false, error = "Failed to load"), hazeState = HazeState(), onAction = {})
    }
}

@Preview(uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeScreenSuccessPreview() {
    InnogeeksTheme {
        HomeScreen(
            state = HomeState(
                isLoading = false,
                stats = ClubStats(150, 45, 5, 24),
                domains = listOf(
                    DomainPreview("web", "Web", Icons.Filled.Computer, "Web dev"),
                    DomainPreview("android", "Android", Icons.Filled.Computer, "App dev")
                ),
                events = listOf(
                    EventPreview("e1", "Hackathon", "15 Oct", "Main Hall")
                )
            ),
            hazeState = HazeState(),
            onAction = {}
        )
    }
}
