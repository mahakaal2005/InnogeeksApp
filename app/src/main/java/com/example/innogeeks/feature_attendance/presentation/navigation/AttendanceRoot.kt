package com.example.innogeeks.feature_attendance.presentation.navigation

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.navigation.NavDestination.Companion.hasRoute
import com.example.innogeeks.feature_attendance.presentation.components.AttendanceView
import com.example.innogeeks.feature_attendance.presentation.components.AttendanceViewSwitch
import com.example.innogeeks.feature_attendance.presentation.myattendance.MyAttendanceRoot
import com.example.innogeeks.feature_attendance.presentation.roster.SessionRosterRoot
import com.example.innogeeks.feature_attendance.presentation.sessions.DomainSessionsRoot
import dev.chrisbanes.haze.HazeState
import edu.kiet.innogeeks.R
import kotlinx.coroutines.launch

// Members get the plain screen; coordinators and admins also get the domain sessions they manage.
@Composable
fun AttendanceRoot(
    hazeState: HazeState,
    canManage: Boolean,
    onBottomBarVisibilityChanged: (Boolean) -> Unit = {}
) {
    if (!canManage) {
        MyAttendanceRoot(hazeState = hazeState)
        return
    }

    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var view by rememberSaveable { mutableStateOf(AttendanceView.DOMAIN) }

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    // The roster needs the whole screen for its sticky bar, so the glass nav hides there.
    LaunchedEffect(currentBackStackEntry) {
        onBottomBarVisibilityChanged(currentBackStackEntry?.destination?.hasRoute<SessionRosterRoute>() != true)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = AttendanceHomeRoute,
            enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
        ) {
            composable<AttendanceHomeRoute> {
                val switch = @Composable { AttendanceViewSwitch(selected = view, onSelect = { view = it }) }
                when (view) {
                    AttendanceView.MINE -> MyAttendanceRoot(hazeState = hazeState, topContent = switch)
                    AttendanceView.DOMAIN -> DomainSessionsRoot(
                        hazeState = hazeState,
                        onOpenRoster = { navController.navigate(SessionRosterRoute(it)) },
                        topContent = switch
                    )
                }
            }
            composable<SessionRosterRoute> { backStackEntry ->
                val route: SessionRosterRoute = backStackEntry.toRoute()
                SessionRosterRoot(
                    sessionId = route.sessionId,
                    hazeState = hazeState,
                    onClose = { navController.popBackStack() },
                    onSaved = { present, absent ->
                        navController.popBackStack()
                        scope.launch {
                            snackbarHostState.showSnackbar(context.getString(R.string.attendance_saved, present, absent))
                        }
                    }
                )
            }
        }
        // Lives here, not in the list, so the message survives the pop back to the list.
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 96.dp, start = 16.dp, end = 16.dp)
        )
    }
}
