package com.example.habbitjournal.feature.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habbitjournal.core.datastore.AppSettingsDataStore
import com.example.habbitjournal.core.network.SyncApiClient
import com.example.habbitjournal.domain.repository.GoodHabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStore: AppSettingsDataStore,
    private val repository: GoodHabitRepository,
    private val syncApiClient: SyncApiClient,
) : ViewModel() {
    companion object {
        private const val TAG = "SettingsViewModel"
    }

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsDataStore.serverUrl.collect { server ->
                _uiState.update { it.copy(serverUrl = server) }
            }
        }
    }

    fun checkServerHealth(url: String) {
        val normalizedUrl = normalizeUrl(url)
        if (normalizedUrl.isBlank()) {
            _uiState.update {
                it.copy(
                    serverHealthMessage = "请先输入服务器地址",
                    serverSaveMessage = "",
                )
            }
            return
        }

        viewModelScope.launch {
            try {
                syncApiClient.checkHealth(normalizedUrl)
                _uiState.update {
                    it.copy(
                        serverHealthMessage = "健康检查通过：$normalizedUrl",
                        serverSaveMessage = "",
                    )
                }
            } catch (e: Exception) {
                val error = formatError(e)
                Log.e(TAG, "Health check failed for $normalizedUrl: $error", e)
                _uiState.update {
                    it.copy(
                        serverHealthMessage = "健康检查失败：$error",
                        serverSaveMessage = "",
                    )
                }
            }
        }
    }

    fun saveServerUrl(url: String) {
        val normalizedUrl = normalizeUrl(url)
        if (normalizedUrl.isBlank()) {
            _uiState.update {
                it.copy(
                    serverSaveMessage = "服务器地址不能为空",
                    serverHealthMessage = "",
                )
            }
            return
        }

        viewModelScope.launch {
            try {
                syncApiClient.checkHealth(normalizedUrl)
                settingsDataStore.setServerUrl(normalizedUrl)
                _uiState.update {
                    it.copy(
                        serverSaveMessage = "保存成功，且已通过健康检查",
                        serverHealthMessage = "健康检查通过：$normalizedUrl",
                    )
                }
            } catch (e: Exception) {
                val error = formatError(e)
                Log.e(TAG, "Save server url failed for $normalizedUrl: $error", e)
                _uiState.update {
                    it.copy(
                        serverSaveMessage = "保存失败：健康检查未通过（$error）",
                    )
                }
            }
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

    private fun normalizeUrl(url: String): String {
        val trimmed = url.trim().trimEnd('/')
        if (trimmed.isBlank()) return ""
        return if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            trimmed
        } else {
            "http://$trimmed"
        }
    }

    private fun formatError(error: Throwable): String {
        val root = generateSequence(error) { it.cause }.last()
        val detail = root.message?.takeIf { it.isNotBlank() }
        return if (detail != null) {
            "${root::class.java.simpleName}: $detail"
        } else {
            root::class.java.simpleName
        }
    }
}