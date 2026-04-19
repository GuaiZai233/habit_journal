package com.example.habbitjournal.feature.settings

data class SettingsUiState(
    val serverUrl: String = "",
    val githubUrl: String = "https://github.com/GuaiZai233/habit_journal",
    val syncMessage: String = "",
    val csvContent: String = "",
)

