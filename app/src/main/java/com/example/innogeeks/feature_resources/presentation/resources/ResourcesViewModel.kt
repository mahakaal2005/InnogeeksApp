package com.example.innogeeks.feature_resources.presentation.resources

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.innogeeks.core.domain.repository.DomainsRepository
import com.example.innogeeks.feature_resources.domain.ResourcesRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.innogeeks.core.presentation.UiText
import edu.kiet.innogeeks.R

class ResourcesViewModel(
    private val domainsRepository: DomainsRepository,
    private val resourcesRepository: ResourcesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ResourcesState())
    val state = _state.asStateFlow()

    private val _events = Channel<ResourcesEvent>()
    val events = _events.receiveAsFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val domainsResult = domainsRepository.getDomains()
            val resourcesResult = resourcesRepository.getResources()

            if (domainsResult.isSuccess && resourcesResult.isSuccess) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        domains = domainsResult.getOrThrow(),
                        resources = resourcesResult.getOrThrow()
                    )
                }
            } else {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = UiText.StringResource(R.string.resources_load_error)
                    )
                }
            }
        }
    }

    fun onAction(action: ResourcesAction) {
        when (action) {
            is ResourcesAction.OnResourceItemClicked -> {
                viewModelScope.launch {
                    _events.send(ResourcesEvent.OpenUrl(action.url))
                }
            }

            ResourcesAction.OnRetry -> load()
        }
    }
}
