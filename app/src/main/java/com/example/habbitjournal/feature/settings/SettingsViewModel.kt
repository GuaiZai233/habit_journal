package com.example.habbitjournal.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habbitjournal.core.datastore.AppSettingsDataStore
import com.example.habbitjournal.domain.repository.GoodHabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStore: AppSettingsDataStore,
    private val repository: GoodHabitRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(settingsDataStore.serverUrl, settingsDataStore.githubUrl) { server, github ->
                server to github
            }.collect { (server, github) ->
                _uiState.update { it.copy(serverUrl = server, githubUrl = github) }
            }
        }
    }

    fun saveServerUrl(url: String) {
        viewModelScope.launch {
            settingsDataStore.setServerUrl(url)
        }
    }

    fun saveGithubUrl(url: String) {
        viewModelScope.launch {
            settingsDataStore.setGithubUrl(url)
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            val message = repository.sync(_uiState.value.serverUrl)
            _uiState.update { it.copy(syncMessage = message) }
        }
    }

    fun exportCsv() {
        viewModelScope.launch {
            val csv = repository.exportCsv()
            _uiState.update { it.copy(csvContent = csv) }
        }
    }
}
