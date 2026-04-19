package com.example.habbitjournal.feature.home

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
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: GoodHabitRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeLatestLog().collect { latest ->
                if (latest == null) {
                    _uiState.value = HomeUiState(daysSinceLast = null, lastRecordDateText = "暂无记录")
                } else {
                    val days = ChronoUnit.DAYS.between(latest.recordDate, LocalDate.now())
                    _uiState.value = HomeUiState(
                        daysSinceLast = days,
                        lastRecordDateText = latest.recordDate.toString(),
                    )
                }
            }
        }
    }

    fun addTodayLog() {
        viewModelScope.launch {
            repository.addTodayLog()
        }
    }
}
