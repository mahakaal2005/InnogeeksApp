package com.example.innogeeks.core.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow

// Lifecycle-safe collector for one-shot event flows. @Composable so it can use Compose
// tools (LocalLifecycleOwner, LaunchedEffect) and tie its listening to the screen's life —
// it draws nothing. Generic <T> so any screen's event type can reuse it.
@Composable
fun <T> ObserveAsEvents(flow: Flow<T>, onEvent: (T) -> Unit) {
    // .current gives the OWNER (the screen that HAS a lifecycle); .lifecycle reaches the
    // actual lifecycle state machine inside it.
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(flow, lifecycleOwner.lifecycle) {
        // Run the block ONLY while the screen is >= STARTED (visible); auto-cancel when
        // backgrounded, auto-restart when it returns. STARTED is an arg, not the receiver.
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            // Park here listening: every event sent down the flow -> onEvent(it).
            flow.collect { event ->
                onEvent(event)
            }
        }
    }
}