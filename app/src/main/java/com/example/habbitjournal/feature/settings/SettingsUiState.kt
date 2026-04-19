package com.example.habbitjournal.feature.settings

data class SettingsUiState(
    val serverUrl: String = "",
    val githubUrl: String = "https://github.com/",
    val syncMessage: String = "",
    val csvContent: String = "",
)
