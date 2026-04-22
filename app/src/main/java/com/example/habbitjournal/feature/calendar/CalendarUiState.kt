package com.example.habbitjournal.feature.calendar

import java.time.LocalDate
import java.time.YearMonth

data class CalendarUiState(
    val month: YearMonth = YearMonth.now(),
    val dailyCount: Map<Int, Int> = emptyMap(),
    val selectedDate: LocalDate? = null,
    val selectedDateCount: Int = 0,
)
