package com.example.innogeeks.feature_profile.presentation.profile

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.innogeeks.core.domain.model.UserDomain
import com.example.innogeeks.core.domain.model.UserRole
import com.example.innogeeks.core.domain.session.Session
import com.example.innogeeks.core.presentation.UiText
import com.example.innogeeks.core.presentation.components.ExpandableRow
import com.example.innogeeks.core.presentation.components.GlassIntensity
import com.example.innogeeks.core.presentation.components.SectionLabel
import com.example.innogeeks.core.presentation.components.liquidGlass
import com.example.innogeeks.feature_profile.presentation.profile.components.ProfileHero
import com.example.innogeeks.ui.theme.InnogeeksTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileRoot(
    hazeState: HazeState,
    onNavigateToAuth: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                ProfileEvent.NavigateToAuth -> onNavigateToAuth()
            }
        }
    }

    ProfileScreen(state = state, hazeState = hazeState, onAction = viewModel::onAction)
}

@Composable
fun ProfileScreen(
    state: ProfileState,
    hazeState: HazeState,
    onAction: (ProfileAction) -> Unit
) {
    val scheme = MaterialTheme.colorScheme

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .hazeSource(hazeState),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = "Profile",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface
                )
            }

            when (val session = state.session) {
                Session.Guest -> guestProfile(hazeState = hazeState, onAction = onAction)
                is Session.Authenticated -> registeredProfile(
                    state = state,
                    hazeState = hazeState,
                    onAction = onAction
                )
            }
        }

        if (state.isLogOutDialogVisible) {
            LogOutDialog(
                hazeState = hazeState,
                onConfirm = { onAction(ProfileAction.OnLogOutConfirmed) },
                onDismiss = { onAction(ProfileAction.OnLogOutDismissed) }
            )
        }
    }
}

// Identity -> action -> info, all on reduced glass so guest reads quieter than registered.
private fun LazyListScope.guestProfile(hazeState: HazeState, onAction: (ProfileAction) -> Unit) {
    item {
        ProfileHero(
            initials = "?",
            name = "Guest",
            subtitle = "You're browsing Innogeeks without an account.",
            roleChip = "Not signed in",
            filled = false,
            modifier = Modifier.padding(vertical = 6.dp)
        )
    }

    item {
        ProfileButton(
            text = "Log In",
            isPrimary = true,
            onClick = { onAction(ProfileAction.OnLoginClick) },
            hazeState = hazeState
        )
    }

    item {
        InfoPanel(
            title = "Already registered?",
            body = "Accounts are created for students who completed the offline registration. " +
                "Check your inbox — we email your college ID and a password.",
            hazeState = hazeState,
            intensity = GlassIntensity.REDUCED
        )
    }

    item { SectionLabel(text = "About Innogeeks", modifier = Modifier.padding(top = 10.dp)) }

    item {
        InfoPanel(
            title = "A student tech community at KIET",
            body = "We build, break and ship things together — hackathons, workshops, " +
                "research projects and open source, run entirely by students.",
            hazeState = hazeState,
            intensity = GlassIntensity.REDUCED
        )
    }

    item {
        InfoPanel(
            title = "Domains & how to join",
            body = "Web Dev · App Dev · AI / ML · AR / VR · Cybersecurity · Design — open the " +
                "Domains tab to see what each one works on. Recruitment opens once a year: " +
                "register during the offline drive, clear the aptitude test and the interview.",
            hazeState = hazeState,
            intensity = GlassIntensity.REDUCED
        )
    }
}

// Registered user displays 6 profile fields from GET /me (§12).
private fun LazyListScope.registeredProfile(
    state: ProfileState,
    hazeState: HazeState,
    onAction: (ProfileAction) -> Unit
) {
    val session = state.session as? Session.Authenticated ?: return
    val profile = state.profile

    item {
        ProfileHero(
            initials = (profile?.fullName ?: session.collegeEmail).toInitials(),
            name = profile?.fullName ?: session.collegeEmail.substringBefore('@'),
            subtitle = session.collegeEmail,
            roleChip = profile?.role?.replace('_', ' ') ?: "Registered",
            modifier = Modifier.padding(vertical = 6.dp)
        )
    }

    // Loading state
    if (state.isLoadingProfile) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        return
    }

    // Error state
    state.profileError?.let { error ->
        item {
            InfoPanel(
                title = "Couldn't load profile",
                body = error.asString(),
                hazeState = hazeState
            )
        }
        item {
            ProfileButton(
                text = "Retry",
                isPrimary = true,
                onClick = { onAction(ProfileAction.OnRetryClick) },
                hazeState = hazeState,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
        return
    }

    // Success state with profile data
    if (profile != null) {
        item {
            if (state.isEditing) {
                EditProfileCard(
                    fullName = state.editableFullName,
                    phone = state.editablePhone,
                    isSaving = state.isSaving,
                    saveError = state.saveError,
                    onFullNameChange = { onAction(ProfileAction.OnFullNameChange(it)) },
                    onPhoneChange = { onAction(ProfileAction.OnPhoneChange(it)) },
                    onSaveClick = { onAction(ProfileAction.OnSaveClick) },
                    onCancelClick = { onAction(ProfileAction.OnCancelEditClick) },
                    hazeState = hazeState
                )
            } else {
                ProfileButton(
                    text = "Edit Profile",
                    isPrimary = false,
                    onClick = { onAction(ProfileAction.OnEditClick) },
                    hazeState = hazeState
                )
            }
        }

        item {
            ExpandableRow(
                title = "Contact & Club",
                subtitle = listOfNotNull(
                    profile.phone,
                    profile.role.replace('_', ' ')
                ).take(1).joinToString(" • ").ifEmpty { "Role: ${profile.role.replace('_', ' ')}" },
                isExpanded = state.expandedSection == ProfileSection.CLUB,
                onToggle = { onAction(ProfileAction.OnSectionToggled(ProfileSection.CLUB)) },
                hazeState = hazeState,
                leading = {
                    IconChip(emoji = "🚀", background = MaterialTheme.colorScheme.secondaryContainer)
                }
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    profile.phone?.let { ProfileField(label = "Phone", value = it) }
                    ProfileField(label = "Role", value = profile.role.replace('_', ' '))
                }
            }
        }

        item {
            ExpandableRow(
                title = "Academic Details",
                subtitle = listOfNotNull(
                    profile.batch,
                    profile.year?.let { "Year $it" }
                ).joinToString(" • ").ifEmpty { "Not provided" },
                isExpanded = state.expandedSection == ProfileSection.ACADEMIC,
                onToggle = { onAction(ProfileAction.OnSectionToggled(ProfileSection.ACADEMIC)) },
                hazeState = hazeState,
                leading = { IconChip(emoji = "🎓", background = MaterialTheme.colorScheme.primary) }
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    profile.batch?.let { ProfileField(label = "Batch", value = it) }
                    profile.year?.let { ProfileField(label = "Year", value = it.toString()) }
                    if (profile.batch == null && profile.year == null) {
                        Text(
                            text = "No academic details provided yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // Batch/year come from your registration record — only the club admin can change them.
                    Text(
                        text = "Managed by Innogeeks admin",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    item {
        ProfileButton(
            text = "Log Out",
            isPrimary = false,
            onClick = { onAction(ProfileAction.OnLogOutClick) },
            hazeState = hazeState,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

// Rendered in-place inside ProfileScreen's Box (not a system Dialog window) so it shares
// the LazyColumn's hazeSource and the card actually blurs the profile content behind it.
@Composable
private fun LogOutDialog(
    hazeState: HazeState,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    BackHandler(onBack = onDismiss)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .fillMaxWidth()
                .liquidGlass(hazeState = hazeState, cornerRadius = 24.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Log out?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "You'll go back to browsing as a guest. You can log in again anytime.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) { Text(text = "Cancel") }
                TextButton(onClick = onConfirm) {
                    Text(text = "Log Out", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun AwaitingDataRow(modifier: Modifier = Modifier) {
    Text(
        text = "Awaiting profile data from the club.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(vertical = 6.dp)
    )
}

@Composable
private fun ProfileField(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun InfoPanel(
    title: String,
    body: String,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
    intensity: GlassIntensity = GlassIntensity.FULL
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlass(hazeState = hazeState, cornerRadius = 18.dp, intensity = intensity)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = scheme.onSurface
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodySmall,
            color = scheme.onSurfaceVariant
        )
    }
}

@Composable
private fun IconChip(
    emoji: String,
    background: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(38.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(background.copy(alpha = 0.2f))
            .border(1.dp, background.copy(alpha = 0.5f), RoundedCornerShape(11.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = emoji, fontSize = 18.sp)
    }
}

@Composable
private fun ProfileButton(
    text: String,
    isPrimary: Boolean,
    onClick: () -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
    intensity: GlassIntensity = GlassIntensity.FULL
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isPrimary) {
                    Modifier
                        .clip(RoundedCornerShape(percent = 50))
                        .background(scheme.primary)
                        .border(1.dp, scheme.primary, RoundedCornerShape(percent = 50))
                } else {
                    Modifier.liquidGlass(hazeState = hazeState, cornerRadius = 999.dp, intensity = intensity)
                }
            )
            .clickable(onClick = onClick)
            .padding(vertical = 11.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = if (isPrimary) scheme.onPrimary else scheme.onSurface
        )
    }
}

// Inline edit form for the two self-editable fields — batch/year/email are read-only elsewhere.
@Composable
private fun EditProfileCard(
    fullName: String,
    phone: String,
    isSaving: Boolean,
    saveError: UiText?,
    onFullNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlass(hazeState = hazeState, cornerRadius = 18.dp)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Edit Profile",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        OutlinedTextField(
            value = fullName,
            onValueChange = onFullNameChange,
            label = { Text("Full name") },
            singleLine = true,
            enabled = !isSaving,
            colors = OutlinedTextFieldDefaults.colors(),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = phone,
            onValueChange = onPhoneChange,
            label = { Text("Phone") },
            singleLine = true,
            enabled = !isSaving,
            colors = OutlinedTextFieldDefaults.colors(),
            modifier = Modifier.fillMaxWidth()
        )

        saveError?.let { error ->
            Text(
                text = error.asString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ProfileButton(
                text = if (isSaving) "Saving…" else "Save",
                isPrimary = true,
                onClick = onSaveClick,
                hazeState = hazeState
            )
            ProfileButton(
                text = "Cancel",
                isPrimary = false,
                onClick = onCancelClick,
                hazeState = hazeState
            )
        }
    }
}

// Same stopgap as the Home top bar — the email local-part is the only initials source.
private fun String.toInitials(): String =
    substringBefore('@')
        .split('.', '_', '-')
        .filter { it.isNotBlank() }
        .take(2)
        .map { it.first().uppercaseChar() }
        .joinToString("")
        .ifEmpty { "?" }

private val registeredSession = Session.Authenticated(
    collegeEmail = "ayush.kumar@kiet.edu",
    role = UserRole.REGISTERED,
    domain = null
)

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 900)
@Composable
private fun ProfileScreenGuestPreview() {
    InnogeeksTheme {
        ProfileScreen(
            state = ProfileState(session = Session.Guest),
            hazeState = HazeState(),
            onAction = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 820)
@Composable
private fun ProfileScreenRegisteredPreview() {
    InnogeeksTheme {
        ProfileScreen(
            state = ProfileState(session = registeredSession),
            hazeState = HazeState(),
            onAction = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 900)
@Composable
private fun ProfileScreenRegisteredExpandedPreview() {
    InnogeeksTheme {
        ProfileScreen(
            state = ProfileState(
                session = Session.Authenticated(
                    collegeEmail = "ayush.kumar@kiet.edu",
                    role = UserRole.COORDINATOR,
                    domain = UserDomain.ANDROID
                ),
                expandedSection = ProfileSection.ACADEMIC,
                profile = com.example.innogeeks.feature_profile.domain.model.StudentProfile(
                    collegeEmail = "atul@kiet.edu",
                    fullName = "Atul Kumar",
                    phone = "+91 98765 43210",
                    batch = "2024-28",
                    year = 2,
                    role = "COORDINATOR",
                    domain = "ANDROID"
                )
            ),
            hazeState = HazeState(),
            onAction = {}
        )
    }
}

private val previewProfile = com.example.innogeeks.feature_profile.domain.model.StudentProfile(
    collegeEmail = "atul@kiet.edu",
    fullName = "Atul Kumar",
    phone = "+91 98765 43210",
    batch = "2025-29",
    year = 1,
    role = "REGISTERED",
    domain = null
)

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 900)
@Composable
private fun ProfileScreenEditingPreview() {
    InnogeeksTheme {
        ProfileScreen(
            state = ProfileState(
                session = registeredSession,
                profile = previewProfile,
                isEditing = true,
                editableFullName = "Atul Kumar",
                editablePhone = "+91 98765 43210"
            ),
            hazeState = HazeState(),
            onAction = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 900)
@Composable
private fun ProfileScreenSavingPreview() {
    InnogeeksTheme {
        ProfileScreen(
            state = ProfileState(
                session = registeredSession,
                profile = previewProfile,
                isEditing = true,
                editableFullName = "Atul Kumar",
                editablePhone = "+91 98765 43210",
                isSaving = true
            ),
            hazeState = HazeState(),
            onAction = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 900)
@Composable
private fun ProfileScreenSaveErrorPreview() {
    InnogeeksTheme {
        ProfileScreen(
            state = ProfileState(
                session = registeredSession,
                profile = previewProfile,
                isEditing = true,
                editableFullName = "Atul Kumar",
                editablePhone = "",
                saveError = UiText.DynamicString("Phone number can't be empty")
            ),
            hazeState = HazeState(),
            onAction = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 820)
@Composable
private fun ProfileScreenAccessDeniedPreview() {
    InnogeeksTheme {
        ProfileScreen(
            state = ProfileState(
                session = registeredSession,
                profileError = UiText.StringResource(com.example.innogeeks.R.string.error_app_access_denied)
            ),
            hazeState = HazeState(),
            onAction = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 820)
@Composable
private fun ProfileScreenLoadingPreview() {
    InnogeeksTheme {
        ProfileScreen(
            state = ProfileState(
                session = registeredSession,
                isLoadingProfile = true
            ),
            hazeState = HazeState(),
            onAction = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 820)
@Composable
private fun ProfileScreenErrorPreview() {
    InnogeeksTheme {
        ProfileScreen(
            state = ProfileState(
                session = registeredSession,
                profileError = com.example.innogeeks.core.presentation.UiText.DynamicString("Network error")
            ),
            hazeState = HazeState(),
            onAction = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 820)
@Composable
private fun ProfileScreenLogOutDialogPreview() {
    InnogeeksTheme {
        ProfileScreen(
            state = ProfileState(session = registeredSession, isLogOutDialogVisible = true),
            hazeState = HazeState(),
            onAction = {}
        )
    }
}
