package com.example.habbitjournal.feature.calendar

import java.time.YearMonth

data class CalendarUiState(
    val month: YearMonth = YearMonth.now(),
    val dailyCount: Map<Int, Int> = emptyMap(),
)
