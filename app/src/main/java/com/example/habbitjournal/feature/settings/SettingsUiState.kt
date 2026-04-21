package com.example.habbitjournal.feature.settings

data class SettingsUiState(
    val serverUrl: String = "",
    val serverSaveMessage: String = "",
    val serverSaveSucceeded: Boolean? = null,
    val isSavingServer: Boolean = false,
    val syncMessage: String = "",
    val csvContent: String = "",
)