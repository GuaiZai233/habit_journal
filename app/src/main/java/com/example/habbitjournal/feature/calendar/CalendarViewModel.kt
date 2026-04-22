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
import java.time.LocalDate
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

    fun selectDate(day: Int) {
        val date = _uiState.value.month.atDay(day)
        if (date.isAfter(LocalDate.now())) {
            return
        }
        val count = _uiState.value.dailyCount[day] ?: 0
        _uiState.update { it.copy(selectedDate = date, selectedDateCount = count) }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedDate = null, selectedDateCount = 0) }
    }

    fun increaseCount() {
        _uiState.update { it.copy(selectedDateCount = it.selectedDateCount + 1) }
    }

    fun decreaseCount() {
        if (_uiState.value.selectedDateCount > 0) {
            _uiState.update { it.copy(selectedDateCount = it.selectedDateCount - 1) }
        }
    }

    fun updateCountValue(newValue: Int) {
        val count = newValue.coerceAtLeast(0)
        _uiState.update { it.copy(selectedDateCount = count) }
    }

    fun saveCount() {
        val selectedDate = _uiState.value.selectedDate ?: return
        val newCount = _uiState.value.selectedDateCount

        viewModelScope.launch {
            repository.updateLogCount(selectedDate, newCount)
            observeMonth()
            clearSelection()
        }
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
