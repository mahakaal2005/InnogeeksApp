package com.example.innogeeks.feature_domains.presentation.domains

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.innogeeks.core.domain.repository.DomainsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.innogeeks.core.presentation.UiText
import edu.kiet.innogeeks.R

class DomainsViewModel(
    private val repository: DomainsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DomainsState())
    val state = _state.asStateFlow()

    init {
        loadDomains()
    }

    private fun loadDomains() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            repository.getDomains()
                .onSuccess { domains ->
                    _state.update { it.copy(isLoading = false, domains = domains) }
                }
                .onFailure {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = UiText.StringResource(R.string.domains_load_error)
                        )
                    }
                }
        }
    }

    fun onAction(action: DomainsAction) {
        when (action) {
            DomainsAction.OnRetry -> loadDomains()
        }
    }
}
