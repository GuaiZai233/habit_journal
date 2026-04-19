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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun CalendarScreen(
    uiState: CalendarUiState,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
) {
    val days = (1..uiState.month.lengthOfMonth()).toList()
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

        LazyVerticalGrid(columns = GridCells.Fixed(7), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(days) { day ->
                val count = uiState.dailyCount[day] ?: 0
                DayCell(day = day, count = count)
            }
        }
    }
}

@Composable
private fun DayCell(day: Int, count: Int) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Text(day.toString(), style = MaterialTheme.typography.labelMedium)
        if (count > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(3.dp)
                    .size(12.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.primary)
            )
            if (count > 1) {
                Text(
                    text = count.toString(),
                    modifier = Modifier.align(Alignment.TopEnd).padding(2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
