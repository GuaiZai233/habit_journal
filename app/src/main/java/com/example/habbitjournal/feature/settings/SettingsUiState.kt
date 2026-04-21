package com.example.habbitjournal.feature.settings

enum class ServerSaveDialogState {
    HIDDEN,
    CHECKING,
    SUCCESS,
    ERROR,
}

data class SettingsUiState(
    val serverUrl: String = "",
    val serverHealthMessage: String = "",
    val serverSaveMessage: String = "",
    val serverSaveSucceeded: Boolean? = null,
    val serverSaveDialogState: ServerSaveDialogState = ServerSaveDialogState.HIDDEN,
    val serverSaveDialogMessage: String = "",
    val syncMessage: String = "",
    val csvContent: String = "",
)