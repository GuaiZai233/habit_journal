package com.example.habbitjournal.feature.home

data class HomeUiState(
    val daysSinceLast: Long? = null,
    val lastRecordDateText: String = "暂无记录",
    val isSyncing: Boolean = false,
    val syncMessage: String = "",
)