package com.example.innogeeks.feature_profile.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.innogeeks.core.domain.session.Session
import com.example.innogeeks.core.domain.session.SessionRevoker
import com.example.innogeeks.core.domain.session.SessionRepository
import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.core.presentation.mapper.toUiText
import com.example.innogeeks.feature_profile.domain.use_case.GetProfileUseCase
import com.example.innogeeks.feature_profile.domain.use_case.RequestAccountDeletionUseCase
import com.example.innogeeks.feature_profile.domain.use_case.UpdateProfileUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class ProfileViewModel(
    private val sessionRepository: SessionRepository,
    private val sessionRevoker: SessionRevoker,
    private val getProfileUseCase: GetProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val requestAccountDeletionUseCase: RequestAccountDeletionUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state = _state.asStateFlow()

    private val _events = Channel<ProfileEvent>()
    val events = _events.receiveAsFlow()

    private var loadJob: Job? = null
    private var signedInEmail: String? = null

    init {
        viewModelScope.launch {
            sessionRepository.session.collect { session ->
                val email = (session as? Session.Authenticated)?.collegeEmail
                if (email != signedInEmail) {
                    // A different user (or none) must never see the previous user's cached profile.
                    signedInEmail = email
                    loadJob?.cancel()
                    _state.value = ProfileState(session = session)
                } else {
                    _state.update { it.copy(session = session) }
                }
                if (session is Session.Authenticated && _state.value.profile == null) {
                    loadProfile()
                }
            }
        }
    }

    fun onAction(action: ProfileAction) {
        when (action) {
            is ProfileAction.OnSectionToggled -> _state.update {
                it.copy(
                    expandedSection = if (it.expandedSection == action.section) {
                        null
                    } else {
                        action.section
                    }
                )
            }

            ProfileAction.OnLoginClick -> viewModelScope.launch {
                _events.send(ProfileEvent.NavigateToAuth)
            }

            ProfileAction.OnLogOutClick -> _state.update { it.copy(isLogOutDialogVisible = true) }

            ProfileAction.OnLogOutDismissed ->
                _state.update { it.copy(isLogOutDialogVisible = false) }

            ProfileAction.OnLogOutConfirmed -> viewModelScope.launch {
                _state.update { it.copy(isLogOutDialogVisible = false, expandedSection = null) }
                // Revoke first (needs the token), but sign out locally whatever happens.
                try {
                    withTimeoutOrNull(LOGOUT_REVOKE_TIMEOUT_MS) { sessionRevoker.revokeRemoteSession() }
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    // Ignored: the token still expires on its own.
                } finally {
                    sessionRepository.signOut()
                }
            }

            ProfileAction.OnRetryClick -> loadProfile()

            ProfileAction.OnEditClick -> _state.update {
                it.copy(
                    isEditing = true,
                    saveError = null,
                    editableFullName = it.profile?.fullName.orEmpty(),
                    editablePhone = it.profile?.phone.orEmpty()
                )
            }

            is ProfileAction.OnFullNameChange ->
                _state.update { it.copy(editableFullName = action.value) }

            is ProfileAction.OnPhoneChange ->
                _state.update { it.copy(editablePhone = action.value) }

            ProfileAction.OnCancelEditClick ->
                _state.update { it.copy(isEditing = false, saveError = null) }

            ProfileAction.OnSaveClick -> saveProfile()

            ProfileAction.OnDeleteAccountClick -> _state.update {
                it.copy(isDeleteAccountDialogVisible = true, deleteConfirmationInput = "")
            }

            ProfileAction.OnDeleteAccountDismissed -> _state.update {
                it.copy(isDeleteAccountDialogVisible = false, deleteConfirmationInput = "", deleteAccountError = null)
            }

            is ProfileAction.OnDeleteConfirmationInputChange -> _state.update {
                it.copy(deleteConfirmationInput = action.value)
            }

            ProfileAction.OnDeleteAccountConfirmed -> requestAccountDeletion()
        }
    }

    // Calls the real endpoint, which starts the 14-day grace period and revokes the token, so signOut() only clears local state.
    private fun requestAccountDeletion() {
        viewModelScope.launch {
            _state.update { it.copy(isRequestingDeletion = true, deleteAccountError = null) }
            when (val result = requestAccountDeletionUseCase()) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            isRequestingDeletion = false,
                            isDeleteAccountDialogVisible = false,
                            deleteConfirmationInput = "",
                            expandedSection = null
                        )
                    }
                    sessionRepository.signOut()
                }
                is Result.Error -> _state.update {
                    it.copy(isRequestingDeletion = false, deleteAccountError = result.error.toUiText())
                }
            }
        }
    }

    private fun loadProfile() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.update { it.copy(isLoadingProfile = true, profileError = null) }
            when (val result = getProfileUseCase()) {
                is Result.Success -> _state.update {
                    it.copy(isLoadingProfile = false, profile = result.data)
                }
                is Result.Error -> _state.update {
                    it.copy(isLoadingProfile = false, profileError = result.error.toUiText())
                }
            }
        }
    }

    private fun saveProfile() {
        val current = _state.value
        val fullName = current.editableFullName.trim().ifEmpty { null }
        val phone = current.editablePhone.trim().ifEmpty { null }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, saveError = null) }
            when (val result = updateProfileUseCase(fullName = fullName, phone = phone)) {
                is Result.Success -> _state.update {
                    it.copy(isSaving = false, isEditing = false, profile = result.data)
                }
                is Result.Error -> _state.update {
                    it.copy(isSaving = false, saveError = result.error.toUiText())
                }
            }
        }
    }

    private companion object {
        const val LOGOUT_REVOKE_TIMEOUT_MS = 5_000L
    }
}
