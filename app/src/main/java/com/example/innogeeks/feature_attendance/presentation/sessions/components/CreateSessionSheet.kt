package com.example.innogeeks.feature_attendance.presentation.sessions.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.innogeeks.core.presentation.UiText
import com.example.innogeeks.feature_attendance.presentation.sessions.formatSessionDate
import com.example.innogeeks.ui.theme.InnogeeksTheme
import edu.kiet.innogeeks.R
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import kotlinx.datetime.Instant
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSessionSheet(
    title: String,
    date: LocalDate,
    isCreating: Boolean,
    error: UiText?,
    onTitleChange: (String) -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onCreateClick: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        CreateSessionContent(title, date, isCreating, error, onTitleChange, onDateChange, onCreateClick)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSessionContent(
    title: String,
    date: LocalDate,
    isCreating: Boolean,
    error: UiText?,
    onTitleChange: (String) -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onCreateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    val yesterday = remember(today) { today.minus(DatePeriod(days = 1)) }
    var showPicker by remember { mutableStateOf(false) }
    val isCustomDate = date != today && date != yesterday

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
    ) {
        Text(
            text = stringResource(R.string.attendance_sheet_title),
            style = MaterialTheme.typography.titleLarge,
            color = scheme.onSurface
        )
        Text(
            text = stringResource(R.string.attendance_sheet_sub),
            style = MaterialTheme.typography.bodySmall,
            color = scheme.outline,
            modifier = Modifier.padding(top = 4.dp)
        )

        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text(stringResource(R.string.attendance_title_label)) },
            placeholder = { Text(stringResource(R.string.attendance_title_hint)) },
            isError = error != null,
            supportingText = error?.let { { Text(it.asString()) } },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp)
        )

        Text(
            text = stringResource(R.string.attendance_date_label),
            style = MaterialTheme.typography.labelLarge,
            color = scheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 12.dp)
        )
        Row(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = date == today,
                onClick = { onDateChange(today) },
                label = { Text(stringResource(R.string.attendance_today)) }
            )
            FilterChip(
                selected = date == yesterday,
                onClick = { onDateChange(yesterday) },
                label = { Text(stringResource(R.string.attendance_yesterday)) }
            )
            FilterChip(
                selected = isCustomDate,
                onClick = { showPicker = true },
                label = { Text(if (isCustomDate) formatSessionDate(date) else stringResource(R.string.attendance_pick_date)) }
            )
        }

        Button(
            onClick = onCreateClick,
            enabled = !isCreating,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp)
                .height(50.dp)
        ) {
            if (isCreating) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text(stringResource(R.string.attendance_create_and_mark))
            }
        }
    }

    if (showPicker) {
        // Sessions are recorded after they happen, so future dates stay disabled.
        val endOfToday = today.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds() + 86_399_999L
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= endOfToday
            }
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        onDateChange(Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date)
                    }
                    showPicker = false
                }) { Text(stringResource(android.R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text(stringResource(android.R.string.cancel)) }
            }
        ) { DatePicker(state = pickerState) }
    }
}

private val previewDate = LocalDate(2026, 10, 9)

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CreateSessionContentEmptyPreview() {
    InnogeeksTheme {
        CreateSessionContent("", previewDate, false, null, {}, {}, {})
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CreateSessionContentFilledPreview() {
    InnogeeksTheme {
        CreateSessionContent("Compose workshop", previewDate, false, null, {}, {}, {})
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CreateSessionContentErrorPreview() {
    InnogeeksTheme {
        CreateSessionContent(
            "", previewDate, false, UiText.StringResource(R.string.attendance_title_required), {}, {}, {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CreateSessionContentCreatingPreview() {
    InnogeeksTheme {
        CreateSessionContent("Compose workshop", previewDate, true, null, {}, {}, {})
    }
}
