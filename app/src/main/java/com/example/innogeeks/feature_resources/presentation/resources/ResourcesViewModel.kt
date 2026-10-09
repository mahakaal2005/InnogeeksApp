package com.example.innogeeks.feature_resources.presentation.resources

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.innogeeks.core.domain.model.UserRole
import com.example.innogeeks.core.domain.repository.DomainsRepository
import com.example.innogeeks.core.domain.session.Session
import com.example.innogeeks.core.domain.session.SessionRepository
import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.core.presentation.UiText
import com.example.innogeeks.feature_resources.domain.ResourcesRepository
import com.example.innogeeks.feature_resources.domain.model.ResourceDraft
import com.example.innogeeks.feature_resources.domain.model.ResourceItem
import com.example.innogeeks.feature_resources.domain.model.detectLink
import com.example.innogeeks.feature_resources.domain.use_case.CreateResourceUseCase
import com.example.innogeeks.feature_resources.domain.use_case.DeleteResourceUseCase
import com.example.innogeeks.feature_resources.domain.use_case.UpdateResourceUseCase
import edu.kiet.innogeeks.R
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ResourcesViewModel(
    private val domainsRepository: DomainsRepository,
    private val resourcesRepository: ResourcesRepository,
    private val sessionRepository: SessionRepository,
    private val createResource: CreateResourceUseCase,
    private val updateResource: UpdateResourceUseCase,
    private val deleteResource: DeleteResourceUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ResourcesState())
    val state = _state.asStateFlow()

    private val _events = Channel<ResourcesEvent>()
    val events = _events.receiveAsFlow()

    init {
        observeEditRights()
        load()
    }

    // Coordinators and admins may change exactly one domain: their own.
    private fun observeEditRights() {
        viewModelScope.launch {
            sessionRepository.session.collect { session ->
                val own = (session as? Session.Authenticated)
                    ?.takeIf { it.role == UserRole.COORDINATOR || it.role == UserRole.ADMIN }
                    ?.domain?.contentDomainId
                _state.update { it.copy(editableDomainId = own) }
            }
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val domainsResult = domainsRepository.getDomains()
            val resourcesResult = resourcesRepository.getResources()

            if (domainsResult.isSuccess && resourcesResult is Result.Success) {
                _state.update {
                    it.copy(isLoading = false, domains = domainsResult.getOrThrow(), resources = resourcesResult.data)
                }
            } else {
                _state.update { it.copy(isLoading = false, error = UiText.StringResource(R.string.resources_load_error)) }
            }
        }
    }

    fun onAction(action: ResourcesAction) {
        when (action) {
            is ResourcesAction.OnResourceItemClicked -> send(ResourcesEvent.OpenUrl(action.url))
            ResourcesAction.OnRetry -> load()

            ResourcesAction.OnAddClick -> _state.update { it.copy(editor = ResourceEditorState()) }
            is ResourcesAction.OnEditClick -> openEditor(action.id)
            is ResourcesAction.OnLongPress -> _state.update { it.copy(actionSheetFor = action.id) }
            ResourcesAction.OnActionSheetDismiss -> _state.update { it.copy(actionSheetFor = null) }
            is ResourcesAction.OnCopyLink -> copyLink(action.id)
            is ResourcesAction.OnRemove -> remove(action.id)
            is ResourcesAction.OnUndoRemove -> _state.update { it.copy(pendingRemovalIds = it.pendingRemovalIds - action.id) }
            is ResourcesAction.OnRemovalCommitted -> commitRemoval(action.id)
            ResourcesAction.OnDismissTip -> _state.update { it.copy(showTip = false) }

            is ResourcesAction.OnUrlChange -> onUrlChange(action.value)
            is ResourcesAction.OnTitleChange -> editor { it.copy(title = action.value) }
            is ResourcesAction.OnDescriptionChange -> editor { it.copy(description = action.value) }
            is ResourcesAction.OnSourceChange -> editor { it.copy(source = action.value, sourceTouched = true, sourceAuto = false) }
            is ResourcesAction.OnTypeChange -> editor { it.copy(type = action.value, typeTouched = true, typeAuto = false) }
            is ResourcesAction.OnLevelChange -> editor { it.copy(level = action.value) }
            ResourcesAction.OnSaveClick -> save()
            ResourcesAction.OnEditorDismiss -> _state.update { it.copy(editor = null) }
        }
    }

    private fun editor(change: (ResourceEditorState) -> ResourceEditorState) {
        _state.update { current -> current.editor?.let { current.copy(editor = change(it).copy(error = null)) } ?: current }
    }

    private fun openEditor(id: String) {
        val item = _state.value.resources.firstOrNull { it.id == id } ?: return
        _state.update {
            it.copy(
                actionSheetFor = null,
                editor = ResourceEditorState(
                    editingId = item.id, url = item.url, title = item.title, description = item.description,
                    source = item.author, type = item.type, level = item.level,
                    typeTouched = true, sourceTouched = true
                )
            )
        }
    }

    // Type and source follow the pasted link until the coordinator changes them by hand.
    private fun onUrlChange(value: String) {
        val detected = detectLink(value)
        editor { current ->
            current.copy(
                url = value,
                type = if (!current.typeTouched && detected != null) detected.type else current.type,
                typeAuto = !current.typeTouched && detected != null,
                source = if (!current.sourceTouched && detected != null) detected.source else current.source,
                sourceAuto = !current.sourceTouched && detected != null
            )
        }
    }

    private fun copyLink(id: String) {
        val item = _state.value.resources.firstOrNull { it.id == id } ?: return
        _state.update { it.copy(actionSheetFor = null) }
        send(ResourcesEvent.CopyToClipboard(item.url))
    }

    private fun remove(id: String) {
        val item = _state.value.resources.firstOrNull { it.id == id } ?: return
        _state.update { it.copy(actionSheetFor = null, pendingRemovalIds = it.pendingRemovalIds + id, showTip = false) }
        send(ResourcesEvent.ShowRemoved(id, item.title))
    }

    // The undo window closed without Undo, so the delete is real now.
    private fun commitRemoval(id: String) {
        if (id !in _state.value.pendingRemovalIds) return
        viewModelScope.launch {
            when (val result = deleteResource(id)) {
                is Result.Success -> _state.update {
                    it.copy(resources = it.resources.filterNot { r -> r.id == id }, pendingRemovalIds = it.pendingRemovalIds - id)
                }
                is Result.Error -> {
                    _state.update { it.copy(pendingRemovalIds = it.pendingRemovalIds - id) }
                    _events.send(ResourcesEvent.ShowMessage(result.error.toUiText()))
                }
            }
        }
    }

    private fun save() {
        val editor = _state.value.editor ?: return
        if (editor.isSaving) return
        if (editor.title.isBlank() || !editor.linkValid) {
            _state.update { it.copy(editor = editor.copy(submitted = true)) }
            return
        }
        val draft = ResourceDraft(editor.type, editor.title, editor.description, editor.source, editor.level, editor.url)
        _state.update { it.copy(editor = editor.copy(isSaving = true, submitted = true, error = null)) }
        viewModelScope.launch {
            val result = if (editor.editingId == null) createResource(draft) else updateResource(editor.editingId, draft)
            when (result) {
                is Result.Success -> onSaved(result.data, wasEdit = editor.isEditing)
                is Result.Error -> _state.update {
                    it.copy(editor = it.editor?.copy(isSaving = false, error = result.error.toUiText()))
                }
            }
        }
    }

    private suspend fun onSaved(item: ResourceItem, wasEdit: Boolean) {
        val domainName = _state.value.domains.firstOrNull { it.id == item.domainId }?.name.orEmpty()
        _state.update { current ->
            current.copy(
                editor = null,
                resources = if (wasEdit) current.resources.map { if (it.id == item.id) item else it } else listOf(item) + current.resources,
                newIds = if (wasEdit) current.newIds else current.newIds + item.id,
                showTip = false
            )
        }
        _events.send(
            ResourcesEvent.ShowMessage(
                if (wasEdit) UiText.StringResource(R.string.resources_saved)
                else UiText.StringResource(R.string.resources_added_to, arrayOf(domainName))
            )
        )
    }

    private fun send(event: ResourcesEvent) {
        viewModelScope.launch { _events.send(event) }
    }
}
