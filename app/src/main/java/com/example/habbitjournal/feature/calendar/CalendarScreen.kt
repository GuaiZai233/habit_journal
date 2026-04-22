package com.example.habbitjournal.feature.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek

private val WeekLabels = listOf("日", "一", "二", "三", "四", "五", "六")

@Composable
fun CalendarScreen(
    uiState: CalendarUiState,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
) {
    val daysInMonth = uiState.month.lengthOfMonth()
    val firstDay = uiState.month.atDay(1).dayOfWeek
    val leadingEmpty = sundayBasedIndex(firstDay)
    val baseCells = List(leadingEmpty) { null } + (1..daysInMonth).toList()
    val totalCells = if (baseCells.size <= 35) 35 else 42
    val paddedCells = baseCells + List(totalCells - baseCells.size) { null }
    val weekRows = paddedCells.chunked(7)

    val placeholderCellColor = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) {
        Color(0xFF2B2E34)
    } else {
        Color(0xFFF1F2F6)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = onPrevMonth) { Text("上一月") }
            Text(uiState.month.toString(), style = MaterialTheme.typography.titleMedium)
            Button(onClick = onNextMonth) { Text("下一月") }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WeekLabels.forEach { label ->
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(label, style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            weekRows.forEach { week ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    week.forEach { day ->
                        val count = if (day != null) uiState.dailyCount[day] ?: 0 else 0
                        DayCell(
                            day = day,
                            count = count,
                            placeholderColor = placeholderCellColor,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int?,
    count: Int,
    placeholderColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .background(if (day == null) placeholderColor else MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (day != null) {
            Text(day.toString(), style = MaterialTheme.typography.labelMedium)
            if (count > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(3.dp)
                        .size(10.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                )
                if (count > 1) {
                    Text(
                        text = count.toString(),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

private fun sundayBasedIndex(dayOfWeek: DayOfWeek): Int {
    return dayOfWeek.value % 7
}