package com.example.innogeeks.feature_events.presentation.events

import android.content.res.Configuration
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.toRoute
import edu.kiet.innogeeks.R
import com.example.innogeeks.core.presentation.components.liquidGlass
import com.example.innogeeks.feature_events.domain.model.ClubEvent
import com.example.innogeeks.feature_events.presentation.events.components.EventCard
import com.example.innogeeks.feature_events.presentation.events.components.EventImage
import com.example.innogeeks.feature_events.presentation.events.components.EventImagePlaceholder
import com.example.innogeeks.feature_events.presentation.events.components.EventPhotoRow
import com.example.innogeeks.feature_events.presentation.events.components.ZoomableImageDialog
import com.example.innogeeks.ui.theme.InnogeeksTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlinx.datetime.LocalDate
import org.koin.androidx.compose.koinViewModel
import com.example.innogeeks.core.presentation.UiText
import androidx.compose.ui.res.stringResource

// Nested NavHost scoped to this tab; the bottom bar visibility is reported up via onBottomBarVisibilityChanged.
@Composable
fun EventsRoot(
    hazeState: HazeState,
    onBottomBarVisibilityChanged: (Boolean) -> Unit = {},
    // Set when another tab (e.g. Home's Class Culture card) wants to open one event directly.
    initialEventId: String? = null,
    onInitialEventConsumed: () -> Unit = {},
    viewModel: EventsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val navController = rememberNavController()

    val backStackEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(backStackEntry) {
        onBottomBarVisibilityChanged(backStackEntry?.destination?.hasRoute<EventDetailRoute>() != true)
    }

    // Pushed on top of the list start destination, so Back lands on the list, not the Home tab.
    LaunchedEffect(initialEventId) {
        if (initialEventId != null) {
            navController.navigate(EventDetailRoute(initialEventId))
            onInitialEventConsumed()
        }
    }

    NavHost(
        navController = navController,
        startDestination = EventListRoute,
        enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
    ) {
        composable<EventListRoute> {
            EventsScreen(
                state = state,
                hazeState = hazeState,
                onAction = viewModel::onAction,
                onEventClick = { eventId -> navController.navigate(EventDetailRoute(eventId)) }
            )
        }
        composable<EventDetailRoute> { detailBackStackEntry ->
            val route: EventDetailRoute = detailBackStackEntry.toRoute()
            val event = state.events.find { it.id == route.eventId }
            if (event != null) {
                EventDetailScreen(
                    event = event,
                    hazeState = hazeState,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun EventsScreen(
    state: EventsState,
    hazeState: HazeState,
    onAction: (EventsAction) -> Unit,
    onEventClick: (String) -> Unit = {}
) {
    val scheme = MaterialTheme.colorScheme

    Box(modifier = Modifier.fillMaxSize()) {
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Box
        }

        if (state.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = state.error.asString(), color = scheme.error)
            }
            return@Box
        }

        EventListScreen(state = state, hazeState = hazeState, onEventClick = onEventClick)
    }
}

@Composable
private fun EventListScreen(
    state: EventsState,
    hazeState: HazeState,
    onEventClick: (String) -> Unit
) {
    val scheme = MaterialTheme.colorScheme

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .hazeSource(hazeState),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.common_events),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface
                )
                Text(
                    text = stringResource(R.string.events_everything_the_club_runs_display),
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
        }

        items(state.sortedEvents, key = { it.id }) { event ->
            EventCard(
                title = event.title,
                blurb = event.description,
                day = dayLabel(event.date),
                month = monthLabel(event.date),
                attendees = event.attendees,
                cadence = event.cadence,
                cardImageRes = event.cardImageRes,
                onClick = { onEventClick(event.id) },
                hazeState = hazeState
            )
        }
    }
}

@Composable
private fun EventDetailScreen(
    event: ClubEvent,
    hazeState: HazeState,
    onBackClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val uriHandler = LocalUriHandler.current
    var zoomedImageRes by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .hazeSource(hazeState)
            .padding(horizontal = 18.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .liquidGlass(hazeState = hazeState, cornerRadius = 999.dp)
                        .clickable(onClick = onBackClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.common_back),
                        tint = scheme.onSurface
                    )
                }
                Text(
                    text = stringResource(R.string.events_event_details),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface
                )
            }

            if (event.eventLink.isNotBlank()) {
                Text(
                    text = stringResource(R.string.events_view_post),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = scheme.primary,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { uriHandler.openUri(event.eventLink) }
                )
            }
        }

        if (event.cardImageRes != null) {
            EventImage(
                imageRes = event.cardImageRes,
                contentDescription = event.title,
                height = 180.dp,
                modifier = Modifier.clickable { zoomedImageRes = event.cardImageRes }
            )
        } else {
            EventImagePlaceholder(height = 180.dp)
        }

        Text(
            text = event.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = scheme.onSurface
        )

        val attendeesText = stringResource(R.string.events_attendees, event.attendees)
        Text(
            text = buildString {
                append(dayLabel(event.date))
                append(' ')
                append(monthLabel(event.date))
                append(' ')
                append(event.date.year)
                if (event.timeAndPlace.isNotBlank()) append(" · ${event.timeAndPlace}")
                if (event.cadence.isNotBlank()) append(" · ${event.cadence}")
                if (event.attendees > 0) append(" · $attendeesText")
            },
            style = MaterialTheme.typography.bodySmall,
            color = scheme.onSurfaceVariant
        )

        Text(
            text = event.description,
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant
        )

        if (event.galleryImageRes.isNotEmpty()) {
            EventPhotoRow(images = event.galleryImageRes, onImageClick = { zoomedImageRes = it })
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    zoomedImageRes?.let { imageRes ->
        ZoomableImageDialog(imageRes = imageRes, onDismiss = { zoomedImageRes = null })
    }
}

private fun dayLabel(date: LocalDate): String = date.dayOfMonth.toString().padStart(2, '0')

private fun monthLabel(date: LocalDate): String = date.month.name.take(3)

private val previewEvents = listOf(
    ClubEvent(
        id = "u1",
        title = "NASA Space Apps Challenge 2025 | Ghaziabad Edition",
        date = LocalDate(2025, 9, 27),
        description = "A globally recognized innovation hackathon hosted at KIET Group of Institutions, focused on solving real-world challenges using NASA's open data.",
        attendees = 150,
        eventLink = "https://www.spaceappschallenge.org/2025/local-events/ghaziabad/",
        cardImageRes = R.drawable.event_nasa_a,
        galleryImageRes = listOf(R.drawable.event_nasa_a, R.drawable.event_nasa_b, R.drawable.event_nasa_c)
    ),
    ClubEvent(
        id = "u2",
        title = "Innohacks 3.0",
        date = LocalDate(2024, 3, 11),
        description = "A thrilling national-level hackathon organized by Innogeeks, bringing together the brightest minds to code, create, and conquer."
    ),
    ClubEvent(
        id = "p1",
        title = "CoderSpree 1.0",
        date = LocalDate(2021, 10, 1),
        attendees = 100,
        description = "A competitive coding arena fostering peer-to-peer learning, irrespective of programming language.",
        eventLink = "https://www.instagram.com/p/CWfhC-0tZai/",
        cardImageRes = R.drawable.event_coderspree1
    ),
    ClubEvent(
        id = "p2",
        title = "Smart India Hackathon — Internal Round",
        date = LocalDate(2026, 2, 10),
        attendees = 96,
        description = "Internal selection round for SIH, with 24 teams pitching problem-statement solutions to a panel of faculty judges."
    )
)

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 780)
@Composable
private fun EventsScreenListPreview() {
    InnogeeksTheme {
        EventsScreen(
            state = EventsState(isLoading = false, events = previewEvents),
            hazeState = HazeState(),
            onAction = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 780)
@Composable
private fun EventsScreenUpcomingDetailPreview() {
    InnogeeksTheme {
        EventDetailScreen(
            event = previewEvents.first { it.id == "u1" },
            hazeState = HazeState(),
            onBackClick = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 780)
@Composable
private fun EventsScreenPastDetailPreview() {
    InnogeeksTheme {
        EventDetailScreen(
            event = previewEvents.first { it.id == "p1" },
            hazeState = HazeState(),
            onBackClick = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EventsScreenLoadingPreview() {
    InnogeeksTheme {
        EventsScreen(state = EventsState(), hazeState = HazeState(), onAction = {})
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EventsScreenErrorPreview() {
    InnogeeksTheme {
        EventsScreen(
            state = EventsState(isLoading = false, error = UiText.DynamicString("Failed to load events.")),
            hazeState = HazeState(),
            onAction = {}
        )
    }
}
