package com.example.habbitjournal.feature.settings

data class SettingsUiState(
    val serverUrl: String = "",
    val serverHealthMessage: String = "",
    val serverSaveMessage: String = "",
    val syncMessage: String = "",
    val csvContent: String = "",
)
