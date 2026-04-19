package com.example.habbitjournal.feature.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habbitjournal.domain.repository.GoodHabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val repository: GoodHabitRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        observeMonth()
    }

    fun previousMonth() {
        _uiState.update { it.copy(month = it.month.minusMonths(1)) }
        observeMonth()
    }

    fun nextMonth() {
        _uiState.update { it.copy(month = it.month.plusMonths(1)) }
        observeMonth()
    }

    private fun observeMonth() {
        viewModelScope.launch {
            repository.observeMonthLogs(_uiState.value.month).collect { logs ->
                _uiState.update { state ->
                    state.copy(dailyCount = logs.associate { it.recordDate.dayOfMonth to it.count })
                }
            }
        }
    }
}
