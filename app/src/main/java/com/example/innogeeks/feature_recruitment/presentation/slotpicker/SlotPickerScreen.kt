package com.example.innogeeks.feature_recruitment.presentation.slotpicker

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.innogeeks.core.presentation.ObserveAsEvents
import com.example.innogeeks.core.presentation.UiText
import com.example.innogeeks.feature_recruitment.domain.model.SlotKind
import com.example.innogeeks.ui.theme.InnogeeksTheme
import edu.kiet.innogeeks.R
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

// Bottom sheet over the Tracker; the ViewModel is keyed per kind and reloads on every open.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlotPickerSheet(
    kind: SlotKind,
    onDismiss: () -> Unit,
    onBooked: () -> Unit,
    viewModel: SlotPickerViewModel = koinViewModel(key = "slot-picker-$kind", parameters = { parametersOf(kind) })
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) { viewModel.onAction(SlotPickerAction.OnSheetShown) }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            // Slide the sheet away first, then tell the Tracker to refresh.
            SlotPickerEvent.BookingConfirmed -> scope.launch { sheetState.hide() }.invokeOnCompletion { onBooked() }
            is SlotPickerEvent.ShowMessage -> scope.launch {
                snackbarHostState.showSnackbar(event.message.asString(context))
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Box {
            SlotPickerContent(state = state, onAction = viewModel::onAction)
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 72.dp, start = 16.dp, end = 16.dp)
            )
        }
    }
}

@Composable
fun SlotPickerContent(
    state: SlotPickerState,
    onAction: (SlotPickerAction) -> Unit
) {
    val scheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 18.dp, end = 18.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(
                if (state.kind == SlotKind.TEST) R.string.slot_picker_title_test
                else R.string.slot_picker_title_interview
            ),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = scheme.onSurface
        )

        Text(
            text = stringResource(R.string.slot_picker_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant
        )

        if (!state.canChange) {
            Text(
                text = stringResource(R.string.slot_switching_off),
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSecondaryContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(scheme.secondaryContainer)
                    .padding(12.dp)
            )
        }

        Box(
            modifier = Modifier
                .weight(1f, fill = false)
                .fillMaxWidth()
                .heightIn(min = 160.dp)
        ) {
            when {
                state.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                state.error != null -> Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = state.error.asString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.error,
                        textAlign = TextAlign.Center
                    )
                    Button(onClick = { onAction(SlotPickerAction.OnRetryClick) }) {
                        Text(text = stringResource(R.string.slot_retry))
                    }
                }

                state.slots.isEmpty() -> Text(
                    text = stringResource(R.string.slot_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center)
                )

                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(state.slots, key = { it.id }) { slot ->
                        SlotCard(
                            slot = slot,
                            isSelected = slot.id == state.selectedSlotId,
                            enabled = state.canChange && !state.isBooking && (!slot.isFull || slot.isMine),
                            onClick = { onAction(SlotPickerAction.OnSlotClick(slot.id)) }
                        )
                    }
                }
            }
        }

        Button(
            onClick = { onAction(SlotPickerAction.OnConfirmClick) },
            enabled = state.canConfirm,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(
                    when {
                        state.isBooking -> R.string.slot_confirming
                        state.currentSlotId != null -> R.string.slot_confirm_switch
                        else -> R.string.slot_confirm
                    }
                )
            )
        }
    }
}

@Composable
private fun SlotCard(
    slot: SlotUi,
    isSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(18.dp)
    val dimmed = slot.isFull && !slot.isMine

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(scheme.surfaceContainerHigh)
            .border(if (isSelected) 2.dp else 1.dp, if (isSelected) scheme.primary else scheme.outlineVariant, shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RadioButton(selected = isSelected, onClick = null, enabled = enabled)

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = slot.dateLabel,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (dimmed) scheme.onSurfaceVariant else scheme.onSurface
            )
            Text(
                text = slot.timeLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant
            )
            if (slot.location != null) {
                Text(
                    text = slot.location,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant
                )
            }
        }

        when {
            slot.isMine -> Text(
                text = stringResource(R.string.slot_yours),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = scheme.primary
            )
            slot.isFull -> Text(
                text = stringResource(R.string.slot_full),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = scheme.error
            )
            else -> Text(
                text = stringResource(R.string.slot_seats_left, slot.remaining, slot.capacity),
                style = MaterialTheme.typography.labelMedium,
                color = scheme.onSurfaceVariant
            )
        }
    }
}

private val previewSlots = listOf(
    SlotUi("s1", "Thu, Aug 15", "10:00 AM – 11:30 AM", null, 12, 20, isFull = false, isMine = false),
    SlotUi("s2", "Thu, Aug 15", "12:00 PM – 1:30 PM", null, 0, 20, isFull = true, isMine = false),
    SlotUi("s3", "Fri, Aug 16", "10:00 AM – 11:30 AM", null, 1, 20, isFull = false, isMine = false)
)

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 640)
@Composable
private fun SlotPickerLoadingPreview() {
    InnogeeksTheme {
        SlotPickerContent(state = SlotPickerState(isLoading = true), onAction = {})
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 640)
@Composable
private fun SlotPickerListPreview() {
    InnogeeksTheme {
        SlotPickerContent(
            state = SlotPickerState(isLoading = false, slots = previewSlots),
            onAction = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 640)
@Composable
private fun SlotPickerSelectedPreview() {
    InnogeeksTheme {
        SlotPickerContent(
            state = SlotPickerState(isLoading = false, slots = previewSlots, selectedSlotId = "s1"),
            onAction = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 640)
@Composable
private fun SlotPickerSwitchPreview() {
    InnogeeksTheme {
        SlotPickerContent(
            state = SlotPickerState(
                isLoading = false,
                slots = previewSlots.map { if (it.id == "s3") it.copy(isMine = true) else it },
                currentSlotId = "s3",
                selectedSlotId = "s1"
            ),
            onAction = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 640)
@Composable
private fun SlotPickerBookingPreview() {
    InnogeeksTheme {
        SlotPickerContent(
            state = SlotPickerState(isLoading = false, slots = previewSlots, selectedSlotId = "s1", isBooking = true),
            onAction = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 640)
@Composable
private fun SlotPickerSwitchingOffPreview() {
    InnogeeksTheme {
        SlotPickerContent(
            state = SlotPickerState(
                isLoading = false,
                slots = previewSlots.map { if (it.id == "s3") it.copy(isMine = true) else it },
                currentSlotId = "s3",
                switchingEnabled = false
            ),
            onAction = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 640)
@Composable
private fun SlotPickerInterviewPreview() {
    InnogeeksTheme {
        SlotPickerContent(
            state = SlotPickerState(
                kind = SlotKind.INTERVIEW,
                isLoading = false,
                slots = previewSlots.map { it.copy(location = "Room 204, Innovation Block") }
            ),
            onAction = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 640)
@Composable
private fun SlotPickerEmptyPreview() {
    InnogeeksTheme {
        SlotPickerContent(state = SlotPickerState(isLoading = false), onAction = {})
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 640)
@Composable
private fun SlotPickerErrorPreview() {
    InnogeeksTheme {
        SlotPickerContent(
            state = SlotPickerState(isLoading = false, error = UiText.DynamicString("Network error")),
            onAction = {}
        )
    }
}
