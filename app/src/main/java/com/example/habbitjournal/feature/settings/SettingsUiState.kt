package com.example.habbitjournal.feature.settings

data class SettingsUiState(
    val serverUrl: String = "",
    val syncMessage: String = "",
    val csvContent: String = "",
)
